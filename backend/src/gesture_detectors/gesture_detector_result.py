from dataclasses import dataclass, field
from typing import Dict

@dataclass
class GestureDetectorResult:
    type: str
    payload: Dict
    pose_label: str = "Unnamed gesture"
    packet: dict = field(default=None, init=False)

    def get_packet(self):
        if self.packet is None:
            self.packet = {"type": self.type} | self.payload
        return self.packet