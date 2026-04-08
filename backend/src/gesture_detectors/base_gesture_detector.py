from abc import ABC, abstractmethod
from mediapipe.tasks.python.vision.gesture_recognizer_result import GestureRecognizerResult

class GestureDetector(ABC):
    @abstractmethod
    def detect(self, rcg_results: GestureRecognizerResult, hand_idx=0):
        pass

    @abstractmethod
    def reset_state(self):
        pass