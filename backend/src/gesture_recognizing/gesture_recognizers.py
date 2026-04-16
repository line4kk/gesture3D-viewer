from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult

from src.gesture_recognizing.utils.finger_checks import *

def is_L_gesture(rcg_result: GestureRecognizerResult, hand_ind=0):
    if not rcg_result.hand_landmarks:
        return False

    hand_landmarks = rcg_result.hand_landmarks[hand_ind]
    score = 0

    if is_thumb_straight(hand_landmarks):
        score += 0.5

    if is_finger_straight(hand_landmarks, 1):
        score += 1

    for finger in range(2, 5):
        if is_finger_curled(hand_landmarks, finger):
            score += 0.5

    result = score >= 3

    return result

def is_photo_gesture(rcg_result: GestureRecognizerResult):
    if len(rcg_result.hand_landmarks) != 2:
        return False

    hand1_landmarks = rcg_result.hand_landmarks[0]
    hand2_landmarks = rcg_result.hand_landmarks[1]

    if not is_L_gesture(rcg_result, 0) or not is_L_gesture(rcg_result, 1):
        return False

    if not is_index_parallel_to_ox(hand1_landmarks) or not is_index_parallel_to_ox(hand2_landmarks):
        return False
    if not is_thumb_normal_to_ox(hand1_landmarks, 30) or not is_thumb_normal_to_ox(hand2_landmarks, 30):
        return False
    if not is_tips_close(rcg_result.hand_landmarks, 2):
        return False


    return True