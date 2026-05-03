from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult

from ..utils.finger_checks import *

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

    if not is_index_parallel_to(hand1_landmarks, np.array([1, 0, 0])) or not is_index_parallel_to(hand2_landmarks, np.array([1, 0, 0])):
        return False
    if not is_thumb_normal_to(hand1_landmarks, np.array([1, 0, 0])) or not is_thumb_normal_to(hand2_landmarks, np.array([1, 0, 0])):
        return False

    index1 = get_finger_vector(hand1_landmarks, 1)
    thumb1 = get_thumb_vector(hand1_landmarks)
    index2 = get_finger_vector(hand2_landmarks, 1)
    thumb2 = get_thumb_vector(hand2_landmarks)

    direction1 = index1 + thumb1
    direction2 = index2 + thumb2

    # Арктангенсы получаются отрицательными, т.к. в Mediapipe ось OY идет от верха экрана к низу
    arctg1 = -np.arctan2(direction1[1], direction1[0])
    arctg2 = -np.arctan2(direction2[1], direction2[0])
    if (arctg1 * arctg2) > 0:
        return False

    top_hand_index, bottom_hand_ind = sorted([0, 1], key=lambda x: [arctg1, arctg2][x])

    # Проверяем, что кончик указательного пальца верхней руки находится на кадре выше, чем кончик большого пальца нижней руки
    if rcg_result.hand_landmarks[top_hand_index][8].y > rcg_result.hand_landmarks[bottom_hand_ind][4].y:
        return False

    bottom_direction = (direction1, direction2)[bottom_hand_ind]
    vector_between_hands = to_np_vector(landmark_to_xOy_projection(rcg_result.hand_landmarks[bottom_hand_ind][1]),
                                        landmark_to_xOy_projection(rcg_result.hand_landmarks[top_hand_index][1]))

    if not is_codirectional(bottom_direction, vector_between_hands, 25):
        return False

    return True

def is_here_gesture(rcg_result: GestureRecognizerResult, hand_ind=0):
    if not rcg_result.hand_landmarks:
        return False

    hand_landmarks = rcg_result.hand_landmarks[hand_ind]
    score = 0

    if is_finger_straight(hand_landmarks, 1):
        score += 1

    for finger in range(2, 5):
        if is_finger_curled(hand_landmarks, finger):
            score += 0.5

    result = score == 2.5

    return result
