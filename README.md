# Latarka (Shake Flashlight)

A native Android application to toggle the device's flashlight using a shake gesture, mimicking the classic Motorola "chop-chop" feature.

---

## 🇬🇧 English Documentation

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
1. Clone the repository: `git clone https://github.com/SebastianKuniec/Latarka.git`
2. Open the project folder in Android Studio (required JDK 21).
3. Allow Gradle to sync and compile, then run on your device.

### License
This project is licensed under the GNU General Public License v3.0 (GPLv3).

---
---

## 🇵🇱 Polska Dokumentacja

### Funkcje aplikacji
* **Sterowanie gestem:** Włączanie i wyłączanie latarki poprzez energiczne potrząśnięcie telefonem (funkcjonalność wzorowana na geście "chop-chop" z telefonów Motorola).
* **Praca w tle:** Aplikacja działa nieprzerwanie w tle dzięki zastosowaniu usługi systemowej typu Foreground Service.
* **Standardy Open-Source:** Pełne poszanowanie prywatności — brak skryptów śledzących, analityki oraz reklam (w pełni gotowe pod wymogi F-Droid).
* **Automatyzacja CI/CD:** Zintegrowany przepływ pracy przez GitHub Actions, który automatycznie sprawdza poprawność budowania wersji produkcyjnej (Release).

### Wymagania sprzętowe i systemowe
* **System operacyjny:** Android 7.0 (API 24) lub nowszy.
* **Komponenty sprzętowe:** Urządzenie musi posiadać sprawną diodę LED aparatu oraz czujnik przyspieszenia (akcelerometr).

### Znane ograniczenia techniczne
* **Konflikt dostępu do aparatu:** Sterowanie diodą błyskową wymaga wyłącznej blokady zasobu kamery (Android Camera2 API). Uruchomienie w tym samym czasie innej aplikacji korzystającej z aparatu (np. systemowego Aparatu) zablokuje dostęp usłudze latarki. W takim przypadku aplikacja może wymagać ręcznego restartu po zwolnieniu aparatu przez drugi program.

### Kompilacja i uruchomienie projektu
1. Sklonuj repozytorium: `git clone https://github.com/SebastianKuniec/Latarka.git`
2. Otwórz projekt w środowisku Android Studio (wymagane środowisko Java JDK 21).
3. Poczekaj na zakończenie synchronizacji Gradle i uruchom aplikację na telefonie.

### Licencja
Projekt dystrybuowany jest na warunkach licencji GNU General Public License v3.0 (GPLv3).
