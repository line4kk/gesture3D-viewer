from pathlib import Path

import cv2
import mediapipe as mp
from mediapipe.tasks.python import vision
from mediapipe.tasks.python.vision import GestureRecognizerOptions

from .datasender import DataSender
from .gesture_detectors.camera_pan_detector import CameraPanDetector
from .gesture_detectors.reset_view_detector import ResetViewDetector
from .gesture_detectors.rotate_detector import RotateDetector
from .gesture_detectors.rotate_z_detector import RotateZDetector
from .gesture_detectors.scale_gesture_detector import ScaleDetector
from .gesture_detectors.screenshot_gesture import ScreenshotDetector
from .gesture_handler import GestureHandler

if __name__ == "__main__":

    VisionRunningMode = mp.tasks.vision.RunningMode
    BaseOptions = mp.tasks.BaseOptions

    # Путь к модели относительно директории backend.
    MODEL_PATH = Path(__file__).resolve().parents[1] / "resources" / "models" / "gesture_recognizer.task"

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

    cap = cv2.VideoCapture(1)

    sender = DataSender()

    gesture_handler = GestureHandler([CameraPanDetector(), ScaleDetector(), ScreenshotDetector(), RotateDetector(), RotateZDetector(), ResetViewDetector()])


    while cap.isOpened():
        ret, frame = cap.read()

        if not ret:
            break

        # OpenCV отдаёт BGR, а MediaPipe требует RGB
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

        # Запускаем распознавание
        results = recognizer.recognize_for_video(
            mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb_frame),
            int(cap.get(cv2.CAP_PROP_POS_MSEC))  # timestamp в мс
        )
        for hand_landmarks in results.hand_landmarks:
            for idx, landmark in enumerate(hand_landmarks):
                x = int(landmark.x * frame.shape[1])
                y = int(landmark.y * frame.shape[0])
                cv2.circle(frame, (x, y), 4, (255, 0, 0), -1)

        result_data = gesture_handler.handle(results)
        if result_data:
            sender.send(result_data.get_packet())

        cv2.putText(
            frame,
            f"{gesture_handler.get_last_pose().pose_label if gesture_handler.get_last_pose() else None}",
            (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2
        )
        cv2.imshow("Gesture Recognition", frame)

        if cv2.waitKey(1) & 0xFF == 27:
            break

    cap.release()
    cv2.destroyAllWindows()
    sender.close()



