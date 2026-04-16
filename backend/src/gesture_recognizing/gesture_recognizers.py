from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult
from utils.finger_checks import is_finger_straight, is_finger_curled, is_thumb_straight

def is_L_gesture(rcg_result: GestureRecognizerResult):
    if not rcg_result.hand_landmarks:
        return False

    hand_landmarks = rcg_result.hand_landmarks[0]
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