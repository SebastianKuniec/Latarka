# Latarka (Shake Flashlight)

A native Android application to toggle the device's flashlight using a shake gesture, mimicking the classic Motorola "chop-chop" feature.

---

## 🌍 Language / Język
Choose your preferred language documentation below:
* [English Documentation](#english)
* [Polska Dokumentacja](#polska-dokumentacja)

---

## English

### Features
* **Gesture Control:** Toggle the flashlight on and off by shaking the phone (Motorola-style chop-chop gesture).
* **Background Service:** Runs efficiently in the background via a dedicated foreground Android Service.
* **F-Droid Ready:** Strictly respects user privacy—contains zero trackers, zero analytics, and zero advertisements.
* **CI/CD Integrated:** Automated build pipelines via GitHub Actions to compile and verify Release APK stability.

### Requirements
* **Operating System:** Android 7.0 (API 24) or newer.
* **Hardware:** A device equipped with a working camera LED flash and an accelerometer sensor.

### Technical Limitations & Known Issues
* **Camera Hardware Lock:** The Android Camera2 API locks the camera resource while controlling the LED flash. If another foreground application (like the official system Camera app) requests access to the camera hardware, the flashlight service will release the resource and may require a manual application restart to re-hook the hardware layer.

### How to Build & Run
The project uses Gradle with Kotlin DSL and is fully compatible with modern Android Studio toolchains:
1. Clone the repository:
   ```bash
   git clone [https://github.com/SebastianKuniec/Latarka.git](https://github.com/SebastianKuniec/Latarka.git)
