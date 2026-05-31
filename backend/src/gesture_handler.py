from typing import List

from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult

from .gesture_detectors.base_gesture_detector import GestureDetector
from .gesture_detectors.gesture_detector_result import GestureDetectorResult


class GestureHandler:
    def __init__(self, gesture_detectors: List[GestureDetector]):
        """
        Класс обработчика результатов распознавания жестов от MediaPipe

        :param gesture_detectors: Список объектов детекторов жестов (GestureDetector). Порядок элементов определяет их приоритет при распознавании
        """
        self.__gesture_detectors = gesture_detectors
        self.__detected_action = None
        self.__detected_pose = None

    def handle(self, rcg_results: GestureRecognizerResult) -> GestureDetectorResult | None:
        detected_action, detected_pose = None, None
        for detector in self.__gesture_detectors:
            detector_result = detector.detect(rcg_results)
            if detector_result is not None:
                if detected_pose is None:
                    detected_pose = detector_result
                if detected_action is None and detector.is_detected():
                    detected_action = detector_result
            
        self.__detected_pose = detected_pose
        self.__detected_action = detected_action
                
        return detected_action

    def get_last_pose(self) -> GestureDetectorResult:
        return self.__detected_pose


