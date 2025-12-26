import os
import argparse
from ultralytics import YOLO
from tensorflow.keras.models import load_model
from pipeline import process_folder


def parse_args():
    parser = argparse.ArgumentParser(description="YOLO OBB detect + crop classify pipeline")
    parser.add_argument("--input", required=True, help="Input images folder")
    parser.add_argument("--output", required=False, default=None, help="Output folder (default: sibling 'results' folder of input)")
    parser.add_argument("--yolo", default="../weights/yolo_model.pt", help="Path to YOLO model")
    parser.add_argument("--cls", default="../weights/cnn_model.keras", help="Path to Keras classify model")
    return parser.parse_args()


def main():
    args = parse_args()

    if args.output is None:
        input_path = os.path.normpath(args.input)
        parent_dir = os.path.dirname(input_path)
        args.output = os.path.join(parent_dir, "results")

    os.makedirs(args.output, exist_ok=True)

    model_yolo = YOLO(args.yolo)
    model_classify = load_model(args.cls)

    process_folder(args.input, args.output, model_yolo, model_classify)


if __name__ == "__main__":
    main()
