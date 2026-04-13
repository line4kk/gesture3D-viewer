from typing import List

from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult

from src.gesture_detectors.base_gesture_detector import GestureDetector

class GestureHandler:
    def __init__(self, gesture_detectors: List[GestureDetector]):
        """
        Класс обработчика результатов распознавания жестов от MediaPipe

        :param gesture_detectors: Список объектов детекторов жестов (GestureDetector). Порядок элементов определяет их приоритет при распознавании
        """
        self.__gesture_detectors = gesture_detectors

    def handle(self, rcg_results: GestureRecognizerResult):
        current_gesture_data = {}
        for detector in self.__gesture_detectors:
            detected = detector.detect(rcg_results)
            if not current_gesture_data and detector.is_detected():
                current_gesture_data = detected

        return current_gesture_data


