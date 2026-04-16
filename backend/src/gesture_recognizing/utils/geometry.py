import numpy as np
from mediapipe.tasks.python.components.containers.landmark import NormalizedLandmark

def landmark_to_np_point(landmark: NormalizedLandmark):
    return np.array([landmark.x, landmark.y, landmark.z], dtype=float)

def landmark_to_xOy_projection(landmark: NormalizedLandmark):
    return np.array([landmark.x, landmark.y, 0], dtype=float)

def to_np_vector(np_point_1, np_point_2):
    return np_point_2 - np_point_1

def count_cos_similarity(v1, v2):
    n1 = np.linalg.norm(v1)
    n2 = np.linalg.norm(v2)

    if n1 == 0 or n2 == 0:
        return None
    cos_similarity = np.dot(v1, v2) / (n1 * n2)
    cos_similarity = np.clip(cos_similarity, -1, 1)

    return cos_similarity

def angle_between(v1, v2):
    return np.degrees(np.arccos(count_cos_similarity(v1, v2)))

def angle(dot1, dot2, dot3):
    # Угол между векторами dot2-dot1 и dot2-dot3
    v1 = to_np_vector(dot2, dot1)
    v2 = to_np_vector(dot2, dot3)
    return angle_between(v1, v2)


def is_parallel(v1, v2, max_angle_deviation=15):
    cos_similarity = count_cos_similarity(v1, v2)
    threshold = np.cos(np.deg2rad(max_angle_deviation))

    return abs(cos_similarity) >= threshold


def is_normal(v1, v2, max_angle_deviation=15):
    cos_similarity = count_cos_similarity(v1, v2)
    threshold = np.sin(np.deg2rad(max_angle_deviation))

    return abs(cos_similarity) <= threshold

def distance(dot1, dot2):
    return np.linalg.norm(to_np_vector(dot1, dot2))