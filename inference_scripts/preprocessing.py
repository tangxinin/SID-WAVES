import os
import cv2
import numpy as np


def is_image_file(filename: str) -> bool:
    return filename.lower().endswith((
        ".png", ".jpg", ".jpeg", ".bmp", ".tif", ".tiff"
    ))


def preprocess_for_classify(image: np.ndarray, size=(224, 224)) -> np.ndarray:
    resized = cv2.resize(image, size)
    normalized = resized.astype(np.float32) / 255.0
    batch = np.expand_dims(normalized, axis=0)
    return batch
