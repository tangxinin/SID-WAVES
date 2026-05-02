import os
import cv2
import pandas as pd
from typing import Dict, List

from preprocessing import is_image_file
from detection import draw_rotated_boxes_with_order
from cropping import crop_and_rotate_boxes
from classification import classify_crop


def process_folder(folder_path: str, output_folder: str, model_yolo, model_classify) -> None:

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
            "Group1": {"NC": None,"NC-result": None,  "H16": None, "H16-result": None,"H18": None,"H18-result": None,},
            "Group2": {"NC": None,"NC-result": None,  "H16": None, "H16-result": None,"H18": None,"H18-result": None,},
            "Group3": {"NC": None,"NC-result": None,  "H16": None, "H16-result": None,"H18": None,"H18-result": None,},
        }

        for idx, crop_pair in enumerate(crops, start=1):
            top_crop = crop_pair["top"]
            bottom_crop = crop_pair["bottom"]
            pred_top = classify_crop(top_crop, model_classify)
            pred_bottom = classify_crop(bottom_crop, model_classify)

            if idx in [1, 4, 7]:
                group = "Group2" if idx == 1 else ("Group1" if idx == 4 else "Group3")
                group_preds[group]["NC"] = pred_top - pred_bottom
                group_preds[group]["NC-result"] = 1 if group_preds[group]["NC"] < -0.5 else 0
            elif idx in [2, 5, 8]:
                group = "Group2" if idx == 2 else ("Group1" if idx == 5 else "Group3")
                group_preds[group]["H16"] = pred_top - pred_bottom
                group_preds[group]["H16-result"] = 1 if group_preds[group]["H16"] < -0.5 else 0
            elif idx in [3, 6, 9]:
                group = "Group2" if idx == 3 else ("Group1" if idx == 6 else "Group3")
                group_preds[group]["H18"] = pred_top - pred_bottom
                group_preds[group]["H18-result"] = 1 if group_preds[group]["H18"] < -0.5 else 0

        for group_name, preds in group_preds.items():
            records.append({
                "Name": image_name,
                "Group": group_name,
                "NC": preds["NC"],
                "NC-result": preds["NC-result"],
                "H16": preds["H16"],
                "H16-result": preds["H16-result"],
                "H18": preds["H18"],
                "H18-result": preds["H18-result"],
            })

    # Export CSV
    df = pd.DataFrame(records)
    csv_path = os.path.join(output_folder, "result_report.csv")
    df.to_csv(csv_path, index=False, encoding="utf-8-sig")
    print(f"Prediction table saved: {csv_path}")
