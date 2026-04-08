from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult

from src.gesture_detectors.base_gesture_detector import GestureDetector

class RotateDetector(GestureDetector):
    def __init__(self):
        self.__prev_x = -1
        self.__prev_y = -1

    def reset_state(self):
        self.__prev_x = -1
        self.__prev_y = -1

    def detect(self, rcg_results: GestureRecognizerResult, hand_idx=0):
        detected = {}
        if not rcg_results.gestures:
            self.reset_state()
            return {}

        gesture = rcg_results.gestures[hand_idx][0]

        if gesture.category_name == "Open_Palm":
            detected["type"] = "rotate"
            hand_landmarks = rcg_results.hand_landmarks[hand_idx]

            if self.__prev_x != -1:
                dx = hand_landmarks[0].x - self.__prev_x
            else:
                dx = 0
            if self.__prev_y != -1:
                dy = hand_landmarks[0].y - self.__prev_y
            else:
                dy = 0

            self.__prev_x = hand_landmarks[0].x
            self.__prev_y = hand_landmarks[0].y

            if dx == 0 and dy == 0:
                return {}

            detected["dx"] = dx
            detected["dy"] = dy
        else:
            self.reset_state()

        return detected
