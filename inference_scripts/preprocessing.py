import os
import cv2
import numpy as np


def is_image_file(filename: str) -> bool:
    """Return True if filename has a common image extension."""
    return filename.lower().endswith((
        ".png", ".jpg", ".jpeg", ".bmp", ".tif", ".tiff"
    ))


def preprocess_for_classify(image: np.ndarray, size=(224, 224)) -> np.ndarray:
    """Resize and normalize an image for binary classification.

    Returns a batch of shape (1, H, W, C) with float32 in [0, 1].
    """
    resized = cv2.resize(image, size)
    normalized = resized.astype(np.float32) / 255.0
    batch = np.expand_dims(normalized, axis=0)
    return batch
