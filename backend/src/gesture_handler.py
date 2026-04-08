from typing import List

from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult

from src.gesture_detectors.base_gesture_detector import GestureDetector


class GestureHandler:
    def __init__(self, single_gesture_detectors: List[GestureDetector], double_gesture_detectors: List[GestureDetector], main_hand=0):
        self.__main_hand = main_hand  # По умолчанию 0 - правая рука

        self.__single_gesture_detectors = single_gesture_detectors
        self.__double_gesture_detectors = double_gesture_detectors

    def __reset_single_gestures_state(self):
        for gesture in self.__single_gesture_detectors:
            gesture.reset_state()

    def __reset_double_gestures_state(self):
        for gesture in self.__double_gesture_detectors:
            gesture.reset_state()

    def __handle_single_gesture(self, rcg_results, hand_idx=0):
        detected = {}
        for detector in self.__single_gesture_detectors:
            if detected:
                detector.reset_state()
            else:
                detected = detector.detect(rcg_results, hand_idx)

        return detected

    def __handle_double_gesture(self, rcg_results):
        detected = {}
        for detector in self.__double_gesture_detectors:
            if detected:
                detector.reset_state()
            else:
                detected = detector.detect(rcg_results)

        return detected

    def handle(self, rcg_results: GestureRecognizerResult):
        result = {}
        if not rcg_results.gestures:
            self.__reset_single_gestures_state()
            self.__reset_double_gestures_state()
        elif len(rcg_results.gestures) == 1:
            self.__reset_double_gestures_state()
            result = self.__handle_single_gesture(rcg_results)
        elif len(rcg_results.gestures) == 2:
            result = self.__handle_double_gesture(rcg_results)

            if not result:
                left_hand_idx = rcg_results.handedness[0][0].index == 0  # index=0 - правая рука
                main_hand_idx = left_hand_idx if self.__main_hand == 1 else not left_hand_idx
                result = self.__handle_single_gesture(rcg_results, main_hand_idx)

        return result


