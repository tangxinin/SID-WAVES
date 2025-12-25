import os
import argparse
from ultralytics import YOLO
from tensorflow.keras.models import load_model
from pipeline import process_folder


def parse_args():
    parser = argparse.ArgumentParser(description="YOLO OBB detect + crop classify pipeline")
    parser.add_argument("--input", required=True, help="Input images folder")
    parser.add_argument("--output", required=True, help="Output folder for visualizations and CSV")
    parser.add_argument("--yolo", default="/kaggle/working/best.pt", help="Path to YOLO model .pt")
    parser.add_argument("--cls", default="/kaggle/working/final_model.keras", help="Path to Keras classify model")
    return parser.parse_args()


def main():
    args = parse_args()

    os.makedirs(args.output, exist_ok=True)

    model_yolo = YOLO(args.yolo)
    model_classify = load_model(args.cls)

    process_folder(args.input, args.output, model_yolo, model_classify)


if __name__ == "__main__":
    main()
