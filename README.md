# 🍅 FarmDoc: AI-Powered Tomato Leaf Disease Scanner

FarmDoc is an offline-first Android application designed to assist agricultural workers in identifying tomato leaf diseases with high accuracy. Utilizing a custom, fine-tuned **MobileNetV2** deep learning model running entirely on the edge, FarmDoc provides near-instantaneous diagnoses without requiring an active internet connection.

This project was developed as an academic proof-of-concept demonstrating the integration of modern Android development components (CameraX, Room Database) with edge-based Machine Learning via Google Play Services.

---

## ✨ Key Features
- **Offline Edge AI:** Runs a lightweight (2.5MB) `.tflite` neural network natively on the device, ensuring privacy and eliminating cloud computing costs or latency.
- **High Accuracy:** Capable of diagnosing 10 distinct tomato leaf conditions (e.g., Early Blight, Late Blight, Septoria Leaf Spot, Healthy) with ~94% validation accuracy.
- **Native Camera Integration:** Built with Android's modern CameraX API for seamless, hardware-agnostic image capture.
- **Persistent Scan History:** Automatically logs all scans, including thumbnail, timestamp, and confidence scores, into a local SQLite database for future reference.
- **Modern UI/UX:** Built with a tabbed bottom navigation interface following Google's Material Design guidelines.

---

## 🛠 Tech Stack

| Category | Technology | Purpose |
| :--- | :--- | :--- |
| **Language** | Kotlin | Primary programming language; utilized for asynchronous Coroutines & Flows. |
| **Machine Learning** | TensorFlow Lite | Edge inference. Dynamically utilizes Play Services TFLite API to support the newest Ops. |
| **Local Database** | Room Persistence Library | Abstracted ORM layer over SQLite for secure, offline data storage. |
| **Camera Interface** | CameraX | Lifecycle-aware camera hardware abstraction for taking photos. |
| **Navigation** | Navigation Component | Manages UI fragments, back-stack states, and bottom tab routing. |
| **Image Processing** | TFLite Support Library | Resizes and casts `UINT8` Bitmap pixels into `FLOAT32` Tensors prior to inference. |

---

## 🚀 Setup and Installation

### Prerequisites
- **Android Studio:** Jellyfish | 2023.3.1 (or newer)
- **Android SDK:** API Level 34 (Minimum API 23)
- **Physical Device / Emulator:** A device with Google Play Services installed (required for the TFLite runtime).

### Build Instructions
1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/FarmDoc.git
   ```
2. **Open the project** in Android Studio.
3. **Sync Gradle:** Wait for Gradle to download dependencies (specifically `play-services-tflite-java`).
4. **Run the App:** Connect your Android device via USB debugging or start an emulator, and click the **Run** button (`Shift + F10`).

> [!NOTE]
> Ensure your Android device has an active internet connection *only on the very first run* so Google Play Services can quietly download the TFLite runtime module if it isn't already cached on the system. 

---

## 🧠 Modifying the AI Model

FarmDoc is designed to be easily extensible. If you train a newer, more accurate model, you can swap it out without rewriting the Kotlin logic.

1. Train an image classification model (e.g., using TensorFlow/Keras in Python) taking `224x224` pixel inputs.
2. Export your model as a TensorFlow Lite flatbuffer (`.tflite`).
3. Replace the existing model located at `app/src/main/assets/mobilenetv2_finetuned.tflite` with your new file.
4. Ensure your labels match the model's output nodes. Update `app/src/main/assets/labels.txt` with your ordered class names (one per line).
5. Open `app/src/main/java/com/starforge/farmdoc/ml/DiseaseClassifier.kt` and update the constants:
   ```kotlin
   private val MODEL_NAME = "your_new_model.tflite"
   private val LABELS_NAME = "your_new_labels.txt"
   ```

---

## 📁 Folder Structure

```text
FarmDoc/
├── app/src/main/
│   ├── assets/                 # Contains the .tflite model and labels.txt
│   ├── java/com/starforge/farmdoc/
│   │   ├── db/                 # Room Database entities, DAOs, and configuration
│   │   ├── ml/                 # Edge AI pipeline (DiseaseClassifier)
│   │   └── ui/                 # Fragments, Adapters, and MainActivity
│   └── res/
│       ├── layout/             # XML UI files
│       └── navigation/         # Navigation graph
└── gradle/                     # Build configurations and dependency versions
```

---

## 📄 License

MIT License

Copyright (c) 2026 Chibueze Martins

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
