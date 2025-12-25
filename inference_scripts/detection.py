import math
import cv2
import numpy as np


def draw_rotated_boxes_with_order(image_path, results):

    image = cv2.imread(image_path)
    h_img, w_img = image.shape[:2]
    center_img = (w_img / 2, h_img / 2)

    boxes = []
    for result in results:
        xywhr = result.obb.xywhr
        xyxyxyxy = result.obb.xyxyxyxy
        confs = result.obb.conf
        classes = result.obb.cls.int()
        names = [result.names[cls.item()] for cls in classes]

        for box, conf, name, xyxy in zip(xywhr, confs, names, xyxyxyxy):
            center_x, center_y, width, height, angle = map(float, box)

            if height > width:
                angle += math.pi / 2

            points = np.array(xyxy, dtype=np.float32).reshape((-1, 2))
            distances = [np.linalg.norm(np.array(center_img) - point) for point in points]
            sorted_indices = np.argsort(distances)
            if 0 in sorted_indices[:2]:
                angle += math.pi

            boxes.append({
                "xywhr": [center_x, center_y, width, height, angle],
                "conf": float(conf),
                "name": name,
                "angle": angle,
                "xyxyxyxy": xyxy
            })

    boxes.sort(key=lambda x: x["angle"])  # ascending by angle

    if len(boxes) >= 3:
        angle_diff_3_1 = boxes[2]["angle"] - boxes[0]["angle"]
        if angle_diff_3_1 >= 1:
            angle_diff_2_1 = boxes[1]["angle"] - boxes[0]["angle"]
            if angle_diff_2_1 < 1:
                boxes = boxes[2:] + boxes[:2]
            else:
                boxes = boxes[1:] + boxes[:1]

    # draw
    for idx, box in enumerate(boxes, start=1):
        xyxyxyxy = box["xyxyxyxy"]
        points = np.array(xyxyxyxy, dtype=np.int32).reshape((-1, 2))
        cv2.polylines(image, [points], isClosed=True, color=(0, 255, 0), thickness=2)
        label = f"#{idx}"
        label_position = (points[0][0], points[0][1] - 10)
        cv2.putText(image, label, label_position, cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 0, 0), 2)

    return image, boxes
