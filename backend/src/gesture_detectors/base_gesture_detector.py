from abc import ABC, abstractmethod
from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult
from .gesture_detector_result import GestureDetectorResult

class GestureDetector(ABC):
    @abstractmethod
    def detect(self, rcg_results: GestureRecognizerResult, hand_idx=0):
        pass

    @abstractmethod
    def is_detected(self) -> GestureDetectorResult | None:
        pass