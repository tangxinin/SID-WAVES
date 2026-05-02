import math
import cv2
import numpy as np


def crop_and_rotate_boxes(image, boxes, output_folder, original_filename):
    
    import os
    os.makedirs(output_folder, exist_ok=True)
    cropped_images = []

    for idx, box in enumerate(boxes, start=1):
        xyxyxyxy = box["xyxyxyxy"]
        box_points = np.array(xyxyxyxy, dtype=np.float32).reshape((-1, 2))
        center_x, center_y, width, height, angle = box["xywhr"]

        if width < height:
            width, height = height, width


        dst_pts = np.array([
            [0, height],
            [0, 0],
            [width, 0],
            [width, height]
        ], dtype="float32")

        M = cv2.getPerspectiveTransform(box_points, dst_pts)
        cropped = cv2.warpPerspective(image, M, (int(width), int(height)))

        cropped_rotated = cv2.rotate(cropped, cv2.ROTATE_90_CLOCKWISE)
        h, w = cropped_rotated.shape[:2]
        cropped_top_half = cropped_rotated[:int(h * 0.5), :]  
        cropped_bottom_half = cropped_rotated[int(h * 0.5):, :]  
        cropped_images.append({"top": cropped_top_half, "bottom": cropped_bottom_half})

    return cropped_images
