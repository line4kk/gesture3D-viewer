import time

from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult

from .base_gesture_detector import GestureDetector
from ..gesture_recognizing.gesture_recognizers import is_photo_gesture


class ScreenshotDetector(GestureDetector):
    def __init__(self, error=0.05, latency=1):
        self.__type = "screenshot"
        self.__latency = latency
        self.__error = error

        self.__first_hand_x = None
        self.__first_hand_y = None
        self.__second_hand_x = None
        self.__second_hand_y = None
        self.__start_time = None

        self.__is_detected = False

    def is_detected(self):
        return self.__is_detected

    def reset_state(self):
        self.__first_hand_x = None
        self.__first_hand_y = None
        self.__second_hand_x = None
        self.__second_hand_y = None
        self.__start_time = None
        self.__is_detected = False

    def detect(self, rcg_results: GestureRecognizerResult, hand_idx=0):
        detected = {}
        if self.__is_detected:
            self.reset_state()
            return {}
        if len(rcg_results.gestures) != 2:
            self.reset_state()
            return {}
        if not is_photo_gesture(rcg_results):
            self.reset_state()
            return {}

        first_hand_wrist =  rcg_results.hand_landmarks[0][0]
        second_hand_wrist = rcg_results.hand_landmarks[1][0]

        is_first_frame = False
        if self.__first_hand_x is None:
            is_first_frame = True
        else:
            if (abs(first_hand_wrist.x - self.__first_hand_x) > self.__error
            or abs(first_hand_wrist.y - self.__first_hand_y) > self.__error
            or abs(second_hand_wrist.x - self.__second_hand_x) > self.__error
            or abs(second_hand_wrist.y - self.__second_hand_y) > self.__error):
                is_first_frame = True

        if is_first_frame:
            self.__start_time = time.monotonic()
            self.__first_hand_x = first_hand_wrist.x
            self.__first_hand_y = first_hand_wrist.y
            self.__second_hand_x = second_hand_wrist.x
            self.__second_hand_y = second_hand_wrist.y
        else:
            current_time = time.monotonic()
            if current_time - self.__start_time >= self.__latency:
                detected["type"] = self.__type
                self.__is_detected = True

        return detected
