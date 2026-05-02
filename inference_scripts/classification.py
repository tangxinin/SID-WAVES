import numpy as np
from typing import Optional
from preprocessing import preprocess_for_classify


def classify_crop(image: np.ndarray, model_classify) -> int:

    if image is None:
        raise ValueError("Input image is None. Please check the input.")

    batch = preprocess_for_classify(image)
    classify_result = model_classify.predict(batch, verbose=0)
    pred_value = float(classify_result[0][0])
    return pred_value
