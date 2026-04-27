import time

from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult

from .base_gesture_detector import GestureDetector
from ..gesture_recognizing.gesture_recognizers import is_here_gesture
from ..gesture_recognizing.utils.finger_checks import is_index_codirectional

import numpy as np

from ..gesture_recognizing.utils.hand_checks import is_hands_parallel_to


class ResetViewDetector(GestureDetector):
    def __init__(self, error=0.05, latency=1):
        self.__type = "reset_view"
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
        if not is_here_gesture(rcg_results,0) or not is_here_gesture(rcg_results, 1):
            self.reset_state()
            return {}

        hand1_landmarks = rcg_results.hand_landmarks[0]
        hand2_landmarks = rcg_results.hand_landmarks[1]
        OY = np.array([0, 1, 0])

        # Проверяем приблизительную сонаправленность указательного пальца и оси OY
        if not is_index_codirectional(hand1_landmarks, OY) or not is_index_codirectional(hand2_landmarks, OY):
            self.reset_state()
            return {}


        OX = np.array([1, 0, 0])
        # Проверяем, что руки находятся на одном уровне
        if not is_hands_parallel_to(rcg_results.hand_landmarks, OX):
            self.reset_state()
            return {}

        first_hand_wrist = hand1_landmarks[0]
        second_hand_wrist = hand2_landmarks[0]

        is_first_frame = False
        if self.__first_hand_x is None:
            is_first_frame = True

        if not is_first_frame:
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
