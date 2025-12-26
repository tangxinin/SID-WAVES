# SID-WAVES: Smartphone-Integrated Diagnostics with Wax-encoded Amplified and Versatile Evaluation System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)]()
[![YOLO](https://img.shields.io/badge/Model-YOLOv11-blue.svg)]()

**SID-WAVES** is a mobile-based intelligent system designed for the convenient and accurate interpretation of microfluidic chip detection results. By integrating a **YOLOv11-based detection model** and an **Xception-based classification model**, it provides an end-to-end solution from image capture to result output.

![Workflow](images/demo_image/workflow.png)

*Figure 1: Workflow of the SID-WAVES system.*

---

## 📲 Direct Installation 

- **Download APK**: Navigate to the [APK/](APK/) folder and download `app-sid-waves.apk`.
- **Installation**: 
   * 1. Transfer the `.apk` file to your Android device.
   * 2. Enable "Install from Unknown Sources" in your device settings.
   * 3. Install and launch **SID-WAVES**.
        
See SID-WAVES in action: From image acquisition (camera/gallery) to intelligent interpretation.

https://github.com/user-attachments/assets/4e76a5fc-0d88-4312-be18-0acd4bf31e4c

*Video: Full prediction process using the SID-WAVES app.*


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
  * **Detection Model (YOLOv11)**: Trained to identify the precise coordinates of reaction chambers using Oriented Bounding Boxes (OBB).
  * **Classification Model (Xception)**: A deep classification network designed to perform binary diagnostic interpretation (Positive/Negative) on the identified reaction zones.
  * **TFLite Conversion**:
    * The trained models (`.pt` for YOLOv11 and `.keras` for Xception) were converted to `.tflite` format using the TensorFlow Lite Converter and the Ultralytics export tool.
    * The converted TFLite files are located in `android_app/app/src/main/assets/` for mobile deployment.

### 2. Android Application
The app is built with **Java** in Android Studio.

  * **Prerequisites**: Android SDK 30 or higher.
  * **Key Source Code**:
    * `MainActivity.java`: Handles UI, camera, and the main application lifecycle.
    * `DetecTool.java`: Implements core inference logic by integrating both detection and classification models to generate final predictions.
    * `Box.java`: Manages post-processing of the detection results, including sorting and grouping the bounding boxes to ensure structured output.
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
  * **Expected Run Time** : 5–15 seconds for all 10 images on a standard desktop CPU.
    
4. Visualized Results
  * Detection Results
    
    ![Detection Result](images/demo_image/c%20(1).jpg)
  
    *Figure 2: Visualization of OBB detection results. Each reaction zone is precisely identified and assigned an index.*
    
  * Prediction Results
   The output table displays the diagnostic status for different groups:
   
     | Name | Group | NC | HPV16 | HPV18 |
     | :--- | :--- | :--- | :--- | :--- |
     | c (1).jpg | Group1 | 0 | 1 | 0 |
     | c (1).jpg | Group2 | 0 | 1 | 0 |
     | c (1).jpg | Group3 | 0 | 1 | 0 |
   
     *(note: **0** represents **Negative**, and **1** represents **Positive**.)*  
       
---
## 📜License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/tangxinin/SID-WAVES/LICENSE) file for details. 

---
## 📧Contact
**Tang Xin** - [tangxin0928@foxmail.com]


