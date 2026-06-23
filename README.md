# STY - Android Client

**STY** is a modern, native Android application designed to help users track their workouts, monitor progress, and automatically classify exercises using on-device Machine Learning.

This repository contains the mobile client, built entirely with modern Android development practices, ensuring a reactive and highly performant user experience.

---

## 📱 Features

* **Modern UI:** Fully declarative and reactive user interface built from the ground up with **Jetpack Compose**.
* **Smart Classification:** Integrates **TensorFlow Lite** for automatic workout movement recognition directly on the device, without relying on cloud processing.
* **Local-First Architecture:** Currently operating independently with robust local state management, ensuring a smooth experience while preparing for backend synchronization.

---

## 🛠️ Tech Stack

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose
* **Machine Learning:** TensorFlow Lite (.tflite)
* **Architecture:** MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF)
* **Build System:** Gradle (Kotlin DSL)

---
