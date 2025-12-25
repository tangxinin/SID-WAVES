# SID-WAVES: Smartphone-based Intelligent Diagnosis for Microfluidic Chips

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)]()
[![YOLO](https://img.shields.io/badge/Model-YOLOv8-blue.svg)]()

SID-WAVES is a mobile-based intelligent system designed for the convenient and accurate interpretation of microfluidic chip detection results. By integrating a YOLO-based detection model and a CNN-based classification/quantification model, it provides an end-to-end solution from image capture to result output.

---

## 📸 Demo & Workflow

### Table of Contents (TOC)
![TOC](images/demo_image/TOC.png)

### System Workflow
![Workflow](images/demo_image/workflow.png)
*Figure: The pipeline of SID-WAVES, including image preprocessing, ROI detection (YOLO), and result interpretation (CNN).*

---

## 📂 Repository Structure

```text
SID-WAVES/
├── android_app/               # Android Studio Project
│   ├── build.gradle           # Project-level gradle config
│   ├── app/
│   │   ├── build.gradle       # Module-level gradle config
│   │   ├── src/               # Java Source Code (MainActivity, DetecTool, etc.)
│   │   └── assets/            # Compiled TFLite models for mobile deployment
├── inference_scripts/         # Python pipeline for verification/desktop usage
│   ├── main.py                # Main script to run the full pipeline
│   ├── pipeline.py            # Integrated logic for YOLO + CNN
│   ├── detection.py           # YOLO detection wrapper
│   ├── classification.py      # CNN classification wrapper
│   ├── cropping.py            # ROI extraction logic
│   ├── preprocessing.py       # Image enhancement and normalization
│   └── requirements.txt       # Python dependencies
├── weights/                   # Model weights
│   ├── yolo_model.pt           # Original YOLO weights (PyTorch)
│   └── cnn_model.keras            # Original CNN weights (Keras/TensorFlow)
├── images/                    
│   ├── demo_image/            # Figures for README
│   └── samples/               # 10 test sample images for microfluidic chips
├── .gitattributes
├── README.md
└── LICENSE
```
---

## 🚀Getting Started

### 1. Android Application
The app is built with Java in Android Studio.

  * **Prerequisites**: Android SDK 30 or higher.
  * **Source Code**:
    * `MainActivity.java`: Handles UI, camera, and the main application lifecycle.
    * `DetecTool.java`: Manages TFLite model loading and inference logic.
    * `Box.java`: Utility class for detection bounding box coordinates.
  * Setup:
    * Open the `android_app` folder in Android Studio.
    * Sync Gradle files and build the project.
    * Deploy the `.apk` to an Android device.
### 2. Python Inference Pipeline
  * Environment: Python 3.8+
  * Installation:
    ```bash
    cd inference_scripts
    pip install -r requirements.txt
    ```
 
### 3. Model Development & Conversion
The system utilizes a dual-model architecture for high-precision interpretation:
  * **Detection Model (YOLOv11)**: Trained to identify the precise coordinates of reaction chambers on the microfluidic chip.
  * **Quantification Model (Xception)**: A classification network designed to analyze the signal intensity within the detected regions.
  * **TFLite Conversion**:
    * The original models (`.pt` for YOLOv11 and `.keras` for Xception) were optimized and converted to `.tflite` format using the TensorFlow Lite Converter and the Ultralytics export tool.
    * These optimized files are located in `android_app/app/src/main/assets/` for mobile deployment.
      
### 4. Testing with Sample Images
We have provided 10 sample images of microfluidic chips in the `images/samples/` directory for verification.
 1. Ensure you have installed the dependencies in inference_scripts/requirements.txt.
 2. Run the pipeline:
    ```bash
    python main.py --input ../images/samples/ --output path/to/output --yolo ../weights/yolo_model.pt --cls ../weights/cnn_model.keras
    ```
  3. Rusults
     
---
## 📜License
This project is licensed under the MIT License - see the [LICENSE](https://github.com/tangxinin/SID-WAVES/LICENSE) file for details. This means the code is free to use for both academic and commercial purposes with proper attribution.


