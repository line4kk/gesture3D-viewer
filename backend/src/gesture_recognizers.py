from typing import List

from mediapipe.tasks.python.components.containers.landmark import NormalizedLandmark
from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult
import numpy as np


class IncorrectFinger(Exception):
    pass


class GestureDebug:
    def __init__(self):
        self.short_errors = []
        self.parts = []
        self.score_parts = []

    def add_short_error(self, text: str):
        self.short_errors.append(text)

    def add_part(self, text: str):
        self.parts.append(text)

    def add_score_part(self, text: str):
        self.score_parts.append(text)

    def build_short_error(self):
        return " ".join(self.short_errors).strip()

    def build_detail_error(self):
        result = []

        if self.score_parts:
            result.append("score_details: " + ", ".join(self.score_parts))

        if self.parts:
            result.append("checks: " + " | ".join(self.parts))

        return " || ".join(result)


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


def is_thumb_straight(landmarks: List[NormalizedLandmark], min_angle_between=150, debug: GestureDebug = None):
    thumb_cmc = landmark_to_np_point(landmarks[1])
    thumb_mcp = landmark_to_np_point(landmarks[2])
    thumb_ip = landmark_to_np_point(landmarks[3])
    thumb_tip = landmark_to_np_point(landmarks[4])

    degrees_between234 = angle(thumb_tip, thumb_ip, thumb_mcp)
    if not degrees_between234:
        if debug:
            debug.add_part(f"thumb_straight FAIL: angle234={degrees_between234}, expected > 0")
        return False

    degrees_between123 = angle(thumb_ip, thumb_mcp, thumb_cmc)
    if not degrees_between123:
        if debug:
            debug.add_part(f"thumb_straight FAIL: angle123={degrees_between123}, expected > 0")
        return False

    result = degrees_between123 >= min_angle_between# and degrees_between234 >= min_angle_between

    if debug:
        if result:
            debug.add_part(
                f"thumb_straight OK: angle123={degrees_between123:.2f}, angle234={degrees_between234:.2f}, expected>={min_angle_between}"
            )
        else:
            debug.add_part(
                f"thumb_straight FAIL: angle123={degrees_between123:.2f}, angle234={degrees_between234:.2f}, expected both>={min_angle_between}"
            )

    return result


def is_finger_straight(landmarks: List[NormalizedLandmark], finger, min_angle_between=160, debug: GestureDebug = None):
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
        if debug:
            debug.add_part(f"finger_{finger}_straight FAIL: angle123={angle123}, angle234={angle234}, expected > 0")
        return False

    result = angle123 >= min_angle_between and angle234 >= min_angle_between

    if debug:
        if result:
            debug.add_part(
                f"finger_{finger}_straight OK: angle123={angle123:.2f}, angle234={angle234:.2f}, expected both>={min_angle_between}"
            )
        else:
            debug.add_part(
                f"finger_{finger}_straight FAIL: angle123={angle123:.2f}, angle234={angle234:.2f}, expected both>={min_angle_between}"
            )

    return result


def is_finger_curled(landmarks: List[NormalizedLandmark], finger, max_angle_between=115, debug: GestureDebug = None):
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
        if debug:
            debug.add_part(f"finger_{finger}_curled FAIL: angle123={angle123}, angle234={angle234}, expected > 0")
        return False

    result = angle123 <= max_angle_between  # or angle234 <= max_angle_between

    if debug:
        if result:
            debug.add_part(
                f"finger_{finger}_curled OK: angle123={angle123:.2f}, angle234={angle234:.2f}, expected angle123<={max_angle_between}"
            )
        else:
            debug.add_part(
                f"finger_{finger}_curled FAIL: angle123={angle123:.2f}, angle234={angle234:.2f}, expected angle123<={max_angle_between}"
            )

    return result


def is_angle_between_thumb_and_index(landmarks: List[NormalizedLandmark], min_degrees_between_fingers=30, debug: GestureDebug = None):
    thumb_start = landmark_to_np_point(landmarks[1])
    thumb_end = landmark_to_np_point(landmarks[4])

    index_start = landmark_to_np_point(landmarks[5])
    index_end = landmark_to_np_point(landmarks[8])

    thumb_vector = to_np_vector(thumb_start, thumb_end)
    index_vector = to_np_vector(index_start, index_end)

    degrees = angle_between(thumb_vector, index_vector)
    result = degrees >= min_degrees_between_fingers

    if debug:
        if degrees is None:
            debug.add_part("thumb_index_angle FAIL: angle=None")
        elif result:
            debug.add_part(
                f"thumb_index_angle OK: angle={degrees:.2f}, expected>={min_degrees_between_fingers}"
            )
        else:
            debug.add_part(
                f"thumb_index_angle FAIL: angle={degrees:.2f}, expected>={min_degrees_between_fingers}"
            )

    return result


error = ""
detail_error = ""


def is_L_gesture(rcg_result: GestureRecognizerResult):
    # if not rcg_result.gestures:
    #     return False
    global error
    global detail_error

    error = ""
    detail_error = ""

    debug = GestureDebug()

    hand_landmarks = rcg_result.hand_landmarks[0]
    score = 0

    if not is_thumb_straight(hand_landmarks, debug=debug):
        print("False, большой палец не прямой")
        error += "thumb "
        debug.add_short_error("thumb")
        debug.add_score_part("thumb=0/0.5")
    else:
        score += 0.5
        debug.add_score_part("thumb=0.5/0.5")

    if not is_finger_straight(hand_landmarks, 1, debug=debug):
        print("False, указательный палец не прямой")
        error += "index "
        debug.add_short_error("index")
        debug.add_score_part("index=0/1")
    else:
        score += 1
        debug.add_score_part("index=1/1")

    for finger in range(2, 5):
        if not is_finger_curled(hand_landmarks, finger, debug=debug):
            print("False, палец", finger, "не согнут")
            error += f"{finger} "
            debug.add_short_error(str(finger))
            debug.add_score_part(f"finger_{finger}=0/0.5")
        else:
            score += 0.5
            debug.add_score_part(f"finger_{finger}=0.5/0.5")

    """if not is_angle_between_thumb_and_index(hand_landmarks, debug=debug):
        return False"""

    result = score >= 3
    print(result, score)

    if not result:
        error += f"{score}"

    debug.add_part(f"final_result={'OK' if result else 'FAIL'}")
    debug.add_part(f"final_score={score}")
    debug.add_part("required_score=3")

    detail_error = debug.build_detail_error()

    return result