from typing import List

from mediapipe.tasks.python.components.containers.landmark import NormalizedLandmark
from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult
import numpy as np

class IncorrectFinger(Exception):
    pass

def landmark_to_np_point(landmark: NormalizedLandmark):
    return np.array([landmark.x, landmark.y, landmark.z], dtype=float)

def to_np_vector(np_point_1, np_point_2):
    return np_point_2 - np_point_1

def angle_between(v1, v2):
    n1 = np.linalg.norm(v1)
    n2 = np.linalg.norm(v2)

    if n1 == 0 or n2 == 0:
        return None
    cos_similarity = np.dot(v1, v2) / (n1 * n2)
    cos_similarity = np.clip(cos_similarity, -1, 1)

    return np.degrees(np.arccos(cos_similarity))

def angle(dot1, dot2, dot3):
    # Угол между векторами dot2-dot1 и dot2-dot3
    v1 = to_np_vector(dot2, dot1)
    v2 = to_np_vector(dot2, dot3)
    return angle_between(v1, v2)

def is_thumb_straight(landmarks: List[NormalizedLandmark], min_angle_between=150):
    thumb_cmc = landmark_to_np_point(landmarks[1])
    thumb_mcp = landmark_to_np_point(landmarks[2])
    thumb_ip = landmark_to_np_point(landmarks[3])
    thumb_tip = landmark_to_np_point(landmarks[4])

    degrees_between234 = angle(thumb_tip, thumb_ip, thumb_mcp)
    if not degrees_between234:
        return False

    degrees_between123 = angle(thumb_ip, thumb_mcp, thumb_cmc)
    if not degrees_between123:
        return False

    #print(degrees_between)
    return degrees_between123 >= min_angle_between and degrees_between234 >= min_angle_between

def is_finger_straight(landmarks: List[NormalizedLandmark], finger, min_angle_between=160):
    """
    Является ли палец выпрямленным (любой палец, кроме большого)
    :param landmarks: Массив landmarks одной руки
    :param finger: Номер пальца от 1 до 4, где 1 - указательный, 4 - мизинец
    :param min_angle_between: Необходимый минимальный угол в суставе
    :return: True, если палец прямой (углы суставов больше или равны min_angle_between)
    """

    if finger < 1 or finger > 4 or not isinstance(finger, int):
        raise IncorrectFinger("Переданный палец должен быть целым числом в промежутке от 1 до 4, где 1 - указательный палец, 4 - мизинец")

    mcp = landmark_to_np_point(landmarks[1 + finger*4])
    pip = landmark_to_np_point(landmarks[2 + finger*4])
    dip = landmark_to_np_point(landmarks[3 + finger*4])
    tip = landmark_to_np_point(landmarks[4 + finger*4])

    angle123 = angle(mcp, pip, dip)
    angle234 = angle(pip, dip, tip)

    if not angle123 or not angle234:
        return False

    return angle123 >= min_angle_between and angle234 >= min_angle_between


def is_finger_curled(landmarks: List[NormalizedLandmark], finger, max_angle_between=100):
    """
    Является ли палец согнутым (любой палец, кроме большого)
    :param landmarks: Массив landmarks одной руки
    :param finger: Номер пальца от 1 до 4, где 1 - указательный, 4 - мизинец
    :param max_angle_between: Максимально допустимый угол в суставе
    :return: True, если палец согнутый (хотя бы один угол в суставах меньше или равен max_angle_between)
    """
    if finger < 1 or finger > 4 or not isinstance(finger, int):
        raise IncorrectFinger("Переданный палец должен быть целым числом в промежутке от 1 до 4, где 1 - указательный палец, 4 - мизинец")

    mcp = landmark_to_np_point(landmarks[1 + finger*4])
    pip = landmark_to_np_point(landmarks[2 + finger*4])
    dip = landmark_to_np_point(landmarks[3 + finger*4])
    tip = landmark_to_np_point(landmarks[4 + finger*4])

    angle123 = angle(mcp, pip, dip)
    angle234 = angle(pip, dip, tip)

    if not angle123 or not angle234:
        return False

    return angle123 <= max_angle_between# or angle234 <= max_angle_between

def is_angle_between_thumb_and_index(landmarks: List[NormalizedLandmark], min_degrees_between_fingers=30):
    thumb_start = landmark_to_np_point(landmarks[1])
    thumb_end = landmark_to_np_point(landmarks[4])

    index_start = landmark_to_np_point(landmarks[5])
    index_end = landmark_to_np_point(landmarks[8])

    thumb_vector = to_np_vector(thumb_start, thumb_end)
    index_vector = to_np_vector(index_start, index_end)

    return angle_between(thumb_vector, index_vector) >= min_degrees_between_fingers

error = ""
def is_L_gesture(rcg_result: GestureRecognizerResult):
    # if not rcg_result.gestures:
    #     return False
    global error
    hand_landmarks = rcg_result.hand_landmarks[0]
    if not is_thumb_straight(hand_landmarks):
        print("False, большой палец не прямой")
        error = "False, thumb is not extended"
        return False
    if not is_finger_straight(hand_landmarks, 1):
        print("False, указательный палец не прямой")
        error = "False, index is not extended"
        return False

    for finger in range(2, 5):
        if not is_finger_curled(hand_landmarks, finger):
            print("False, палец", finger, "не согнут")
            error = f"False, {finger} is not curled"
            return False

    """if not is_angle_between_thumb_and_index(hand_landmarks):
        return False"""

    print("True, все гудик")
    return True