from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult

from .base_gesture_detector import GestureDetector
from .gesture_detector_result import GestureDetectorResult

import numpy as np

class CameraPanDetector(GestureDetector):
    def __init__(self, min_cos_similarity=0.95):
        self.__min_cos_similarity = min_cos_similarity
        self.__first_hand_prev_x = None
        self.__first_hand_prev_y = None
        self.__second_hand_prev_x = None
        self.__second_hand_prev_y = None
        self.__type = "camera_pan"
        self.__pose_label = "Two open palms"

        self.__is_detected = False

    def is_detected(self):
        return self.__is_detected

    def reset_state(self):
        self.__first_hand_prev_x = None
        self.__first_hand_prev_y = None
        self.__second_hand_prev_x = None
        self.__second_hand_prev_y = None
        self.__is_detected = False

    def detect(self, rcg_results: GestureRecognizerResult, hand_idx=0):
        if len(rcg_results.gestures) != 2:
            self.reset_state()
            return None
        if rcg_results.gestures[0][0].category_name != "Open_Palm" or rcg_results.gestures[1][0].category_name != "Open_Palm":
            self.reset_state()
            return None

        is_first_frame = False
        if (not self.__first_hand_prev_x or not self.__first_hand_prev_y
                or not self.__second_hand_prev_x or not self.__second_hand_prev_y):
            is_first_frame = True

        first_hand_x = rcg_results.hand_landmarks[0][0].x
        first_hand_y = rcg_results.hand_landmarks[0][0].y
        second_hand_x = rcg_results.hand_landmarks[1][0].x
        second_hand_y = rcg_results.hand_landmarks[1][0].y
        payload = {}
        if not is_first_frame:
            first_hand_vector = np.array([first_hand_x - self.__first_hand_prev_x,
                                          first_hand_y - self.__first_hand_prev_y],
                                         dtype=np.float64)
            second_hand_vector = np.array([second_hand_x - self.__second_hand_prev_x,
                                           second_hand_y - self.__second_hand_prev_y],
                                          dtype=np.float64)
            vectors_dot = np.dot(first_hand_vector, second_hand_vector)
            cos_similarity = vectors_dot / (np.linalg.norm(first_hand_vector) * np.linalg.norm(second_hand_vector))

            if cos_similarity < self.__min_cos_similarity:
                is_first_frame = True
            else:
                shortest_vector = first_hand_vector if np.linalg.norm(first_hand_vector) < np.linalg.norm(second_hand_vector) else second_hand_vector
                payload["dx"] = shortest_vector[0]
                payload["dy"] = shortest_vector[1]

        self.__first_hand_prev_x = first_hand_x
        self.__first_hand_prev_y = first_hand_y
        self.__second_hand_prev_x = second_hand_x
        self.__second_hand_prev_y = second_hand_y


        self.__is_detected = not is_first_frame
        return GestureDetectorResult(self.__type, payload, self.__pose_label)
