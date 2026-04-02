from datasender import DataSender

import logging
logging.basicConfig(level=logging.DEBUG)


if __name__ == "__main__":
    sender = DataSender()

    data = {"type": "rotate", "dx": 0.1, "dy": 0.2}

    sender.send(data)
    sender.close()