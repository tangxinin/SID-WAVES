import math
import cv2
import numpy as np


def crop_and_rotate_boxes(image, boxes, output_folder, original_filename):
    """Crop and rotate detected oriented boxes from the image.

    Args:
        image: np.ndarray image.
        boxes: list of dicts from draw_rotated_boxes_with_order.
        output_folder: ensure existence (not saving crops here).
        original_filename: kept for compatibility.

    Returns:
        List[np.ndarray]: cropped and rotated patch images.
    """
    import os
    os.makedirs(output_folder, exist_ok=True)
    cropped_images = []

    for _, box in enumerate(boxes, start=1):
        center_x, center_y, width, height, angle = box["xywhr"]

        if width < height:
            width, height = height, width

        rect = ((center_x, center_y), (width, height), math.degrees(angle))
        box_points = cv2.boxPoints(rect).astype("float32")

        dst_pts = np.array([
            [0, height],
            [0, 0],
            [width, 0],
            [width, height]
        ], dtype="float32")

        M = cv2.getPerspectiveTransform(box_points, dst_pts)
        cropped = cv2.warpPerspective(image, M, (int(width), int(height)))

        cropped_rotated = cv2.rotate(cropped, cv2.ROTATE_90_CLOCKWISE)
        cropped_images.append(cropped_rotated)

    return cropped_images
