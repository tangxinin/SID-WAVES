# SID-WAVES: Smartphone-Integrated Diagnostics with Wax-encoded Amplified and Versatile Evaluation System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)]()
[![YOLO](https://img.shields.io/badge/Model-YOLOv11-blue.svg)]()

---

## 📲 Direct Installation 

- **Download APK**: Navigate to the [APK/](APK/) folder and download `app-sid-waves.apk`.
- **Installation**: 
   * 1. Transfer the `.apk` file to your Android device.
   * 2. Enable "Install from Unknown Sources" in your device settings.
   * 3. Install and launch **SID-WAVES**.

---

## 📂 Repository Structure

```text
SID-WAVES/
├── APK/                       # Pre-compiled application package
│   └── app-sid-waves.apk      # Ready-to-install Android APK
├── android_app/               # Android Studio Project (Source Code)
│   ├── build.gradle           # Project-level gradle config
│   ├── app/
│   │   ├── build.gradle       # Module-level gradle config
│   │   ├── src/               # Java Source Code (MainActivity, DetecTool and Box)
│   │   └── assets/            # Compiled TFLite models for mobile deployment
├── inference_scripts/         # Python pipeline for verification/desktop usage
│   ├── main.py                # Main script to run the full pipeline
│   ├── pipeline.py            # Integrated logic for YOLO + CNN
│   ├── detection.py           # YOLO detection wrapper
│   ├── classification.py      # CNN classification wrapper
│   ├── cropping.py            # ROI extraction logic
│   ├── preprocessing.py       # Image normalization
│   └── requirements.txt       # Python dependencies
├── weights/                   # Model weights
│   ├── yolo_model.pt          # Original YOLO weights (PyTorch)
│   └── cnn_model.keras        # Original CNN weights (Keras)
├── images/                    
│   ├── demo_image/            # Figures and assets for README
│   └── samples/               # 10 test sample images
├── .gitattributes
├── README.md
└── LICENSE
```
---

## 🚀Getting Started


### 1. Model Development & Conversion
The system utilizes a dual-model architecture for high-precision interpretation:
  * **Detection Model**: `weights/yolo_model.pt`
  * **Classification Model**: `weights/cnn_model.keras`
  * **TFLite Conversion**:
    * The trained models were converted to `.tflite` format using the TensorFlow Lite Converter and the Ultralytics export tool.
    * The converted TFLite files are located in `android_app/app/src/main/assets/` for mobile deployment.

### 2. Android Application
The app is built with **Java** in Android Studio.

  * **Prerequisites**: Android SDK 30 or higher.
  * Setup:
    * Open the `android_app` folder in Android Studio.
    * Sync Gradle files and build the project.
    * Deploy to an Android device via USB or by generating a new APK.
    
### 3. Python Inference Pipeline
1. Environment: Python 3.8+
2. Installation:
  ```bash
  cd inference_scripts
  pip install -r requirements.txt
  ```
  * **Typical Install Time**: 2–5 minutes on a "normal" desktop computer (depending on internet speed).
  
3. **Testing with Sample Images**
  * We have provided 10 sample images in `images/samples/` for verification.
  * Run the pipeline, and output is automatically saved to `images/results/`:
    
  ```bash
  python main.py --input ../images/samples/
  ```
  * **Expected Run Time**: 5–15 seconds for all 10 images on a standard desktop CPU.
    
4. Visualized Results
  * Detection Results
    
    ![Detection Result](images/demo_image/c%20(1).jpg)
  
    *Figure: Visualization of detection results.*
    
  * Prediction Results
    
     The output table displays the diagnostic status for different groups:
   
     | Name | Group | NC | NC-result | HPV16 | H16-result | HPV18 | H18-result |
     | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
     | c (1).jpg | Group1 | 0.80831 | 0 | -0.99245 | 1 | -0.02142 | 0 |
     | c (1).jpg | Group2 | 0.00732 | 0 | -0.96398 | 1 | 0.00164 | 0 |
     | c (1).jpg | Group3 | 0.01724 | 0 | -0.99389 | 1 | -0.18824 | 0 |
   
     *(note: **0** represents **Negative**, and **1** represents **Positive**.)*  
       
---
## 📜License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/tangxinin/SID-WAVES/LICENSE) file for details. 

---



