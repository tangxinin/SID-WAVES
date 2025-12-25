import os
import cv2
import pandas as pd
from typing import Dict, List

from preprocessing import is_image_file
from detection import draw_rotated_boxes_with_order
from cropping import crop_and_rotate_boxes
from classification import classify_crop


def process_folder(folder_path: str, output_folder: str, model_yolo, model_classify) -> None:
    """Run detection, visualization, crop classification, and CSV export over a folder."""
    os.makedirs(output_folder, exist_ok=True)
    records: List[Dict] = []

    for image_name in os.listdir(folder_path):
        if not is_image_file(image_name):
            continue
        image_path = os.path.join(folder_path, image_name)

        # Detection and visualization
        results = model_yolo(image_path)
        visualized_image, boxes = draw_rotated_boxes_with_order(image_path, results)

        output_path = os.path.join(output_folder, image_name)
        cv2.imwrite(output_path, visualized_image)
        print(f"Processed and saved visualization: {output_path}")

        # Crop and classify
        image = cv2.imread(image_path)
        crops = crop_and_rotate_boxes(image, boxes, output_folder, image_name)

        group_preds = {
            "Group1": {"NC": None, "H16": None, "H18": None},
            "Group2": {"NC": None, "H16": None, "H18": None},
            "Group3": {"NC": None, "H16": None, "H18": None},
        }

        for idx, crop in enumerate(crops, start=1):
            pred = classify_crop(crop, model_classify)

            if idx in [1, 4, 7]:
                group = "Group2" if idx == 1 else ("Group1" if idx == 4 else "Group3")
                group_preds[group]["NC"] = pred
            elif idx in [2, 5, 8]:
                group = "Group2" if idx == 2 else ("Group1" if idx == 5 else "Group3")
                group_preds[group]["H16"] = pred
            elif idx in [3, 6, 9]:
                group = "Group2" if idx == 3 else ("Group1" if idx == 6 else "Group3")
                group_preds[group]["H18"] = pred

        for group_name, preds in group_preds.items():
            records.append({
                "Name": image_name,
                "Group": group_name,
                "NC": preds["NC"],
                "H16": preds["H16"],
                "H18": preds["H18"],
            })

    # Export CSV
    df = pd.DataFrame(records)
    csv_path = os.path.join(output_folder, "red_ratio_report.csv")
    df.to_csv(csv_path, index=False, encoding="utf-8-sig")
    print(f"Prediction table saved: {csv_path}")
