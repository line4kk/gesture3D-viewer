from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult

from .base_gesture_detector import GestureDetector
from ..gesture_recognizing.gesture_recognizers import is_L_gesture
from .gesture_detector_result import GestureDetectorResult


class RotateZDetector(GestureDetector):
    def __init__(self):
        self.__prev_x = None
        self.__type = "rotate_z"
        self.__pose_label = "\"L\" gesture"
        self.__is_detected = False

    def is_detected(self):
        return self.__is_detected

    def reset_state(self):
        self.__prev_x = None
        self.__is_detected = False

    def detect(self, rcg_results: GestureRecognizerResult, hand_idx=0):
        if len(rcg_results.gestures) != 1:
            self.reset_state()
            return None

        if not is_L_gesture(rcg_results):
            self.reset_state()
            return None

        is_first_frame = False
        if not self.__prev_x:
            is_first_frame = True

        hand_landmarks = rcg_results.hand_landmarks[hand_idx]
        payload = {}
        if not is_first_frame:
            payload["dz"] = hand_landmarks[0].x - self.__prev_x

        self.__prev_x = hand_landmarks[0].x

        self.__is_detected = not is_first_frame
        return GestureDetectorResult(self.__type, payload, self.__pose_label)
