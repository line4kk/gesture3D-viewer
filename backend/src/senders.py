import zmq
import time
import logging
import queue
import threading

from src.utils.camera import get_index_by_name

logger = logging.getLogger(__name__)
handler = logging.StreamHandler()
formatter = logging.Formatter("%(asctime)s | %(levelname)s | %(name)s | %(message)s")
handler.setFormatter(formatter)
logger.addHandler(handler)
logger.setLevel(logging.INFO)

class DataSender:
    def __init__(self, context: zmq.Context, address="tcp://localhost:5555"):
        self.context = context
        self.socket = self.context.socket(zmq.PUB)
        self.socket.bind(address)
        self.socket.setsockopt(zmq.LINGER, 0)
        self.socket.setsockopt(zmq.SNDHWM, 1)
        self.closed = False

        self._queue = queue.Queue(maxsize=1)
        self._thread = threading.Thread(target=self._worker, daemon=True)
        self._thread.start()

        time.sleep(1)

    def _worker(self):
        while not self.closed:
            try:
                data = self._queue.get(timeout=0.5)
                self.socket.send_json(data)
                logger.debug(f"Sending: {data}")
            except queue.Empty:
                continue

    def send(self, data: dict):
        if self.closed:
            raise SenderException("Sending in closed socket")
        try:
            self._queue.put_nowait(data)
        except queue.Full:
            pass

    def close(self):
        if not self.closed:
            self.closed = True
            self._thread.join(timeout=1)
            self.socket.close()

    def __del__(self):
        self.close()


class UserVideoSender:
    def __init__(self, context: zmq.Context, address="tcp://localhost:5556"):
        self.context = context
        self.socket = self.context.socket(zmq.PUB)
        self.socket.setsockopt(zmq.SNDHWM, 1)
        self.socket.setsockopt(zmq.LINGER, 0)
        self.socket.bind(address)

        self._queue = queue.Queue(maxsize=1)
        self._closed = False

        self._thread = threading.Thread(target=self._worker, daemon=True)
        self._thread.start()

    def send_frame(self, frame_bytes: bytes):
        if self._closed:
            return
        try:
            self._queue.put_nowait(frame_bytes)
        except queue.Full:
            pass

    def _worker(self):
        while not self._closed:
            try:
                frame_bytes = self._queue.get(timeout=0.5)
                self.socket.send(frame_bytes)
            except queue.Empty:
                continue

    def close(self):
        if not self._closed:
            self._closed = True
            self._thread.join(timeout=1)
            self.socket.close()

    def __del__(self):
        self.close()

class CommandReceiver:
    def __init__(self, context: zmq.Context, address="tcp://localhost:5557"):
        self.context = context
        self.socket = self.context.socket(zmq.REP)
        self.socket.bind(address)

        self._video_streaming = threading.Event()

        self._change_camera = threading.Event()
        self._new_camera_ind = 0

        self._closed = False
        self._thread = threading.Thread(target=self._worker, daemon=True)
        self._thread.start()

    def is_video_streaming(self) -> bool:
        return self._video_streaming.is_set()

    def is_need_to_change_camera(self) -> bool:
        if self._change_camera.is_set():
            self._change_camera.clear()
            return True
        return False

    def get_new_camera_index(self) -> int:
        return self._new_camera_ind

    def _worker(self):
        while not self._closed:
            try:
                if self.socket.poll(500):
                    cmd = self.socket.recv_string()
                    cmd_splited = cmd.split()
                    logger.debug(cmd)
                    if cmd == "START_CAMERA":
                        self._video_streaming.set()
                        self.socket.send_string("OK")
                    elif cmd == "STOP_CAMERA":
                        self._video_streaming.clear()
                        self.socket.send_string("OK")
                    elif len(cmd_splited) > 1 and cmd_splited[0] == "SET_CAMERA":
                        index = get_index_by_name(" ".join(cmd_splited[1:]))
                        if index is not None:
                            self._change_camera.set()
                            self._new_camera_ind = index
                            self.socket.send_string("OK")
                        else:
                            self.socket.send_string("ERROR")
                    else:
                        self.socket.send_string("UNKNOWN_COMMAND")
            except zmq.ZMQError:
                break

    def close(self):
        if not self._closed:
            self._closed = True
            self._thread.join(timeout=1)
            self.socket.close()

    def __del__(self):
        self.close()

class SenderException(Exception):
    pass
