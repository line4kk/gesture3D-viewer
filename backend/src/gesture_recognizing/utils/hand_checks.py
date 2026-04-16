from typing import List

from mediapipe.tasks.python.components.containers.landmark import NormalizedLandmark
from src.gesture_recognizing.utils.geometry import landmark_to_np_point, to_np_vector, is_parallel


def is_hands_parallel_to(landmarks: List[List[NormalizedLandmark]], vector, max_deviation_angle=10):
    hand1_wrist = landmark_to_np_point(landmarks[0][0])
    hand2_wrist = landmark_to_np_point(landmarks[1][0])

    hands_vec = to_np_vector(hand1_wrist, hand2_wrist)
    hands_vec[2] = 0  # Проекция на xOy

    return is_parallel(hands_vec, vector, max_deviation_angle)