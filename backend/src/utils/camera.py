import cv2
from cv2_enumerate_cameras import enumerate_cameras

def get_index_by_name(target_name):
    """
    Находит индекс MSMF для камеры с указанным названием.
    Возвращает index или None
    """
    for camera_info in enumerate_cameras(cv2.CAP_MSMF):
        if target_name.lower() in camera_info.name.lower():
            return camera_info.index

    return None