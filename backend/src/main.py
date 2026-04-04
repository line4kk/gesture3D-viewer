from datasender import DataSender
import cv2
import mediapipe as mp
from mediapipe.tasks.python import vision
from mediapipe.tasks.python.vision import GestureRecognizerOptions
import logging
import time
logging.basicConfig(level=logging.DEBUG)

prev_x = -1
prev_y = -1
sumdx = 0
sumdy = 0

pockets = 0
time_to_sending = 0
if __name__ == "__main__":

    VisionRunningMode = mp.tasks.vision.RunningMode
    BaseOptions = mp.tasks.BaseOptions

    # Путь к модели — путь до скачанного файла gesture_recognizer.task
    MODEL_PATH = "../resources/models/gesture_recognizer.task"

    # Настройки
    base_options = BaseOptions(model_asset_path=MODEL_PATH)
    options = GestureRecognizerOptions(
        base_options=base_options,
        running_mode=VisionRunningMode.VIDEO,
        num_hands=1,
        min_hand_detection_confidence=0.5,
        min_hand_presence_confidence=0.5,
        min_tracking_confidence=0.5,
    )

    # Создаём распознаватель
    recognizer = vision.GestureRecognizer.create_from_options(options)

    cap = cv2.VideoCapture(0)

    sender = DataSender()
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


        data = {}
        if results.gestures:
            gesture = results.gestures[0][0]

            if gesture.category_name == "Open_Palm":
                data["type"] = "rotate"
                hand_landmarks = results.hand_landmarks[0]

                if prev_x != -1:
                    dx = hand_landmarks[0].x - prev_x
                else:
                    dx = 0
                if prev_y != -1:
                    dy = hand_landmarks[0].y - prev_y
                else:
                    dy = 0

                prev_x = hand_landmarks[0].x
                prev_y = hand_landmarks[0].y

                data["dx"] = dx
                data["dy"] = dy
                sender.send(data)
                sumdx += dx
                sumdy += dy
        else:
            prev_x = -1
            prev_y = -1
        cv2.imshow("Gesture Recognition", frame)

        if cv2.waitKey(1) & 0xFF == 27:
            break

    cap.release()
    cv2.destroyAllWindows()
    sender.close()



