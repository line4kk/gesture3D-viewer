import zmq
import time
import logging

class DataSender:
    def __init__(self, address="tcp://*:5555"):
        self.context = zmq.Context()
        self.socket = self.context.socket(zmq.PUB)
        self.socket.bind(address)
        self.socket.setsockopt(zmq.LINGER, 0)
        self.closed = False

        self.logger = logging.getLogger(__name__)
        time.sleep(1)

    def send(self, data: dict):
        if self.closed:
            raise DataSenderException("Sending in closed socket")
        self.socket.send_json(data)
        self.logger.debug(f"Sending: {data}")

    def close(self):
        if not self.closed:
            self.closed = True
            self.socket.close()
            self.context.term()

class DataSenderException(Exception):
    pass
