from typing import List

from mediapipe.tasks.python.components.containers.landmark import NormalizedLandmark
from src.gesture_recognizing.utils.geometry import landmark_to_np_point, angle, to_np_vector, is_parallel, is_normal, \
    distance, landmark_to_xOy_projection
import numpy as np


def validate_finger_number(finger):
    if finger < 1 or finger > 4 or not isinstance(finger, int):
        raise ValueError("Переданный палец должен быть целым числом в промежутке от 1 до 4, где 1 - указательный палец, 4 - мизинец")

def is_thumb_straight(landmarks: List[NormalizedLandmark], min_angle_between=150):
    thumb_cmc = landmark_to_np_point(landmarks[1])
    thumb_mcp = landmark_to_np_point(landmarks[2])
    thumb_ip = landmark_to_np_point(landmarks[3])

    degrees_between123 = angle(thumb_ip, thumb_mcp, thumb_cmc)
    if degrees_between123 is None:
        return False

    result = degrees_between123 >= min_angle_between

    return result

def is_finger_straight(landmarks: List[NormalizedLandmark], finger, min_angle_between=160):
    """
    Является ли палец выпрямленным (любой палец, кроме большого)
    :param landmarks: Массив landmarks одной руки
    :param finger: Номер пальца от 1 до 4, где 1 - указательный, 4 - мизинец
    :param min_angle_between: Необходимый минимальный угол в суставе
    :return: True, если палец прямой (углы суставов больше или равны min_angle_between)
    """

    validate_finger_number(finger)

    mcp = landmark_to_np_point(landmarks[1 + finger*4])
    pip = landmark_to_np_point(landmarks[2 + finger*4])
    dip = landmark_to_np_point(landmarks[3 + finger*4])
    tip = landmark_to_np_point(landmarks[4 + finger*4])

    angle123 = angle(mcp, pip, dip)
    angle234 = angle(pip, dip, tip)

    if angle123 is None or angle234 is None:
        return False

    result = angle123 >= min_angle_between and angle234 >= min_angle_between

    return result


def is_finger_curled(landmarks: List[NormalizedLandmark], finger, max_angle_between=115):
    """
    Является ли палец согнутым (любой палец, кроме большого)
    :param landmarks: Массив landmarks одной руки
    :param finger: Номер пальца от 1 до 4, где 1 - указательный, 4 - мизинец
    :param max_angle_between: Максимально допустимый угол в суставе
    :return: True, если палец согнутый (угол в суставе меньше или равен max_angle_between)
    """
    validate_finger_number(finger)

    mcp = landmark_to_np_point(landmarks[1 + finger*4])
    pip = landmark_to_np_point(landmarks[2 + finger*4])
    dip = landmark_to_np_point(landmarks[3 + finger*4])

    angle123 = angle(mcp, pip, dip)

    if angle123 is None:
        return False

    result = angle123 <= max_angle_between

    return result

def is_index_parallel_to_ox(landmarks: List[NormalizedLandmark], max_deviation_angle=20):
    if not is_finger_straight(landmarks, 1):
        return False

    mcp = landmark_to_np_point(landmarks[5])
    tip = landmark_to_np_point(landmarks[8])

    index_vector = to_np_vector(mcp, tip)
    index_vector[2] = 0  # Проекция на xOy
    ox_vector = np.array([1, 0, 0])

    return is_parallel(index_vector, ox_vector, max_deviation_angle)

def is_thumb_normal_to_ox(landmarks: List[NormalizedLandmark], max_deviation_angle=15):
    if not is_thumb_straight(landmarks):
        return False

    cmc = landmark_to_np_point(landmarks[1])
    tip = landmark_to_np_point(landmarks[4])

    thumb_vector = to_np_vector(cmc, tip)
    thumb_vector[2] = 0
    ox_vector = np.array([1, 0, 0])

    return is_normal(thumb_vector, ox_vector, max_deviation_angle)


def is_tips_close(landmarks: List[List[NormalizedLandmark]], max_distance_coefficient=1):
    hand1_landmarks = landmarks[0]
    hand2_landmarks = landmarks[1]

    index1_tip = landmark_to_xOy_projection(hand1_landmarks[8])
    index2_tip = landmark_to_xOy_projection(hand2_landmarks[8])
    index1_ip = landmark_to_xOy_projection(hand1_landmarks[7])
    index2_ip = landmark_to_xOy_projection(hand2_landmarks[7])
    thumb1_tip = landmark_to_xOy_projection(hand1_landmarks[4])
    thumb2_tip = landmark_to_xOy_projection(hand2_landmarks[4])

    max_distance1 = distance(index1_tip, index1_ip) * max_distance_coefficient
    max_distance2 = distance(index2_tip, index2_ip) * max_distance_coefficient

    distance1 = distance(index1_tip, thumb2_tip)
    distance2 = distance(index2_tip, thumb1_tip)

    return distance1 <= max_distance1 and distance2 <= max_distance2
