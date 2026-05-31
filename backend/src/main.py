import logging
import sys
import shutil
import tempfile
import time
from pathlib import Path

import cv2
import mediapipe as mp
import zmq
from mediapipe.tasks.python import vision
from mediapipe.tasks.python.vision import GestureRecognizerOptions

from src.senders import DataSender, CommandReceiver, UserVideoSender
from src.gesture_detectors.camera_pan_detector import CameraPanDetector
from src.gesture_detectors.reset_view_detector import ResetViewDetector
from src.gesture_detectors.rotate_detector import RotateDetector
from src.gesture_detectors.rotate_z_detector import RotateZDetector
from src.gesture_detectors.scale_gesture_detector import ScaleDetector
from src.gesture_detectors.screenshot_gesture_detector import ScreenshotDetector
from src.gesture_handler import GestureHandler

def set_cap(index):
    c = cv2.VideoCapture(index)
    c.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
    c.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
    c.set(cv2.CAP_PROP_BUFFERSIZE, 1)
    return c


def resolve_model_path() -> Path:
    if getattr(sys, "frozen", False):
        bundled_model = Path(getattr(sys, "_MEIPASS")) / "resources" / "models" / "gesture_recognizer.task"
        temp_model_dir = Path(tempfile.gettempdir()) / "gesture3d-viewer"
        temp_model_dir.mkdir(parents=True, exist_ok=True)
        temp_model = temp_model_dir / "gesture_recognizer.task"

        if not temp_model.is_file() or temp_model.stat().st_size != bundled_model.stat().st_size:
            shutil.copy2(bundled_model, temp_model)

        return temp_model

    return Path(__file__).resolve().parents[1] / "resources" / "models" / "gesture_recognizer.task"

def main():
    logger = logging.getLogger("main")
    handler = logging.StreamHandler()
    formatter = logging.Formatter("%(asctime)s | %(levelname)s | %(name)s | %(message)s")
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.setLevel(logging.INFO)

    VisionRunningMode = mp.tasks.vision.RunningMode
    BaseOptions = mp.tasks.BaseOptions

    # Путь к модели относительно директории backend.
    MODEL_PATH = resolve_model_path()

    # Настройки
    base_options = BaseOptions(model_asset_path=str(MODEL_PATH))
    options = GestureRecognizerOptions(
        base_options=base_options,
        running_mode=VisionRunningMode.VIDEO,
        num_hands=2,
        min_hand_detection_confidence=0.5,
        min_hand_presence_confidence=0.5,
        min_tracking_confidence=0.5,
    )
    # Создаём распознаватель
    recognizer = vision.GestureRecognizer.create_from_options(options)
    logger.info("Gesture recognizer has been started")

    context = zmq.Context()
    recognize_data_sender = DataSender(context)
    stats_sender = DataSender(context, "tcp://*:5558")
    video_sender = UserVideoSender(context)
    command_receiver = CommandReceiver(context)

    gesture_handler = GestureHandler([ScaleDetector(), CameraPanDetector(),  ScreenshotDetector(), RotateDetector(), RotateZDetector(), ResetViewDetector()])

    fps_counter = 0
    fps_display = 0

    start_time = time.perf_counter()

    while not command_receiver.is_need_to_change_camera():
        if (time.perf_counter() - start_time) > 5:
            logger.error("Camera data not received. Setting to default value")
            cap = set_cap(0)
            break
    else:
        logger.info("Setting camera to " + str(command_receiver.get_new_camera_index()))
        cap = set_cap(command_receiver.get_new_camera_index())


    while cap.isOpened():
        fps_counter += 1
        ret, frame = cap.read()

        if not ret:
            break

        # OpenCV отдаёт BGR, а MediaPipe требует RGB
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

        # Запускаем распознавание
        results = recognizer.recognize_for_video(
            mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb_frame),
            int(time.perf_counter() * 1000)  # timestamp в мс
        )
        for hand_landmarks in results.hand_landmarks:
            for idx, landmark in enumerate(hand_landmarks):
                x = int(landmark.x * frame.shape[1])
                y = int(landmark.y * frame.shape[0])
                cv2.circle(frame, (x, y), 4, (255, 0, 0), -1)

        if command_receiver.is_video_streaming():
            _, buf = cv2.imencode('.jpg', frame, [cv2.IMWRITE_JPEG_QUALITY, 70])
            video_sender.send_frame(buf.tobytes())

        result_data = gesture_handler.handle(results)
        if result_data:
            recognize_data_sender.send(result_data.get_packet())

        current_time = time.perf_counter()
        elapsed = current_time - start_time
        if elapsed >= 1.0:
            fps_display = fps_counter / elapsed
            fps_counter = 0
            start_time = current_time

        stats = {"fps": fps_display, "hands_num": len(results.hand_landmarks), "current_pose": gesture_handler.get_last_pose().pose_label if gesture_handler.get_last_pose() else None}
        stats_sender.send(stats)

        if command_receiver.is_need_to_change_camera():
            logger.info("Changing camera to " + str(command_receiver.get_new_camera_index()))
            cap = set_cap(command_receiver.get_new_camera_index())

        if cv2.waitKey(1) & 0xFF == 27:
            break

    cap.release()
    cv2.destroyAllWindows()

    recognize_data_sender.close()
    video_sender.close()
    command_receiver.close()
    context.term()

if __name__ == "__main__":
    main()
