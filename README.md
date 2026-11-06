# VisionStudio — Offline Android Computer Vision & Image Processing Suite

![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android_SDK-26%2B_(ARM64)-3DDC84?logo=android&logoColor=white)
![OpenCV](https://img.shields.io/badge/OpenCV-4.10.0-5C3EE8?logo=opencv&logoColor=white)
![TensorFlow Lite](https://img.shields.io/badge/LiteRT_%2F_TFLite-2.17.0-FF6F00?logo=tensorflow&logoColor=white)
![CameraX](https://img.shields.io/badge/CameraX-1.4.1-4285F4)

**VisionStudio** is an end-to-end computer vision and image processing project combining an experimental **Python / Google Colab research pipeline** with a **100% offline native Android application** built using **Kotlin, Jetpack Compose, CameraX, OpenCV 4.10, and LiteRT (TensorFlow Lite)**.

---

## 🌟 نظرة عامة (Overview)

يهدف المشروع إلى بناء بيئة متكاملة لمعالجة الصور والفيديو والكاميرا المباشرة على أجهزة أندرويد دون الحاجة للاتصال بالإنترنت (Offline-First Edge AI)، بدءاً من بناء واختبار الخوارزميات في دفاتر تفاعلية على Google Colab وحتى نشرها داخل تطبيق أندرويد أصلي.

### المميزات الرئيسية (Key Features)
- **معالجة الكاميرا الحية (Live CameraX Preview Filters):** تطبيق فلاتر OpenCV لحظياً مع دعم الكاميرا الأمامية والخلفية وتصحيح اتجاه الإطارات تلقائياً.
- **أدوات المعالجة التقليدية الشاملة (Classical Image Processing):**
  - التحويلات اللونية والعتبات (`Grayscale`, `Binary Threshold`, `Otsu`, `Adaptive Threshold`, `HSV Color Isolation`).
  - الفلاتر والتحسين (`Gaussian`, `Median`, `Bilateral`, `CLAHE`, `Gamma Correction`, `Sharpening`).
  - كشف الحواف والمعالم (`Sobel`, `Laplacian`, `Canny Edge Detection`, `Contours`).
  - العمليات المورفولوجية وتقسيم الصور (`Erosion`, `Dilation`, `Opening`, `Closing`, `K-Means`, `GrabCut`).
- **الرؤية الحاسوبية والتعلم العميق محلياً (On-Device ML):**
  - كشف الوجوه والعيون باستخدام مصنفات **Haar Cascades**.
  - كشف الأجسام لـ 80 فئة (COCO) باستخدام نموذج **YOLO26n** محول إلى صيغة **LiteRT / TFLite (`FP32`)** مع تنفيذ Non-Maximum Suppression (NMS) مخصص.

---

## 📂 هيكلية المستودع (Repository Structure)

```text
VisionStudio-Android-ImageProcessing/
├── android-app/          # Native Android project (Kotlin, Compose, OpenCV 4.10, TFLite 2.17, CameraX)
├── notebooks/            # Step-by-step Google Colab notebooks (00 to 10 + integrated pipelines)
├── models/               # Offline models: Haar Cascade XMLs, LiteRT TFLite model, and COCO labels
├── sample-data/          # Lightweight test images for quick experimentation
├── .gitignore            # Excludes build artifacts, caches, and private signing keys
└── README.md             # Project documentation
```

---

## 📱 تحميل وتجربة التطبيق (Download APK)

يمكنك تنزيل ملف الـ APK النهائي الجاهز للتثبيت مباشرة على أجهزة Android (معمارية `arm64-v8a`) من قسم **[Releases](../../releases)**.
