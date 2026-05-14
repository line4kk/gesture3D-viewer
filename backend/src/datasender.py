import zmq
import time
import logging
import queue
import threading

class DataSender:
    def __init__(self, address="tcp://*:5555"):
        self.context = zmq.Context()
        self.socket = self.context.socket(zmq.PUB)
        self.socket.bind(address)
        self.socket.setsockopt(zmq.LINGER, 0)
        self.socket.setsockopt(zmq.SNDHWM, 1)
        self.closed = False

        self._queue = queue.Queue(maxsize=1)
        self._thread = threading.Thread(target=self._worker, daemon=True)
        self._thread.start()

        self.logger = logging.getLogger(__name__)
        handler = logging.StreamHandler()
        formatter = logging.Formatter("%(asctime)s | %(levelname)s | %(name)s | %(message)s")
        handler.setFormatter(formatter)
        self.logger.addHandler(handler)
        self.logger.setLevel(logging.DEBUG)

        time.sleep(1)

    def _worker(self):
        while not self.closed:
            try:
                data = self._queue.get(timeout=0.5)
                self.socket.send_json(data)
                self.logger.debug(f"Sending: {data}")
            except queue.Empty:
                continue

    def send(self, data: dict):
        if self.closed:
            raise DataSenderException("Sending in closed socket")
        try:
            self._queue.put_nowait(data)
        except queue.Full:
            pass

    def close(self):
        if not self.closed:
            self.closed = True
            self._thread.join(timeout=1)
            self.socket.close()
            self.context.term()

    def __del__(self):
        self.close()

class DataSenderException(Exception):
    pass
