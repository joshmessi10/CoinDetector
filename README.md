# Colombian Coin Detector (Android + OpenCV)

An Android application that uses the camera and OpenCV to detect Colombian coins of **50**, **100**, and **200** pesos in real time. It identifies coins by detecting their circular shape and estimating their size ratio to determine their value. The app displays the quantity of each denomination and the total amount of money detected.

## 📱 Features

- 🔍 Real-time detection using OpenCV Hough Circle Transform  
- 📸 Uses device camera to capture live video feed  
- 💰 Classifies coins into **50**, **100**, or **200** peso categories based on size  
- 🧮 Displays count and subtotal for each coin denomination  
- 🧾 Calculates the total amount of money  
- 🖥️ Clean, minimal UI with title and dynamic result display  

## 🛠️ Technologies Used

- **Kotlin**
- **OpenCV for Android**
- **Android SDK**
- **CameraBridgeViewBase / JavaCameraView**

## 🧠 How It Works

1. Converts each frame to grayscale and applies a Gaussian blur.
2. Detects circles using `Imgproc.HoughCircles`.
3. Calculates the radius of detected coins.
4. Uses the ratio of each radius to the smallest one as a base to classify coin value:
   - `r < 1.08 → 50`
   - `r < 1.27 → 100`
   - `else → 200`
5. Displays:
   - Count of each coin
   - Subtotal per denomination
   - Total sum in pesos
  
It should always have a **50** peso coin in the image. However, more coins can be added as new ratios change.

## 📷 Screenshots

![CoinDetector1](https://github.com/user-attachments/assets/5729d2b2-c0a7-4f39-82e7-5eab7c28800f)

![CoinDetector2](https://github.com/user-attachments/assets/38580a1d-1365-4232-8e71-c1e25b7cf4a8)

![CoinDetector3](https://github.com/user-attachments/assets/67eeb10f-1f3c-4233-b4f7-0f6a1527c1f3)


## 🔐 Permissions

This app requires the following permission:

- **CAMERA**: To access the device’s camera for real-time detection.

Make sure to grant permission when prompted or manually enable it in app settings.

# 👥 Credits

Josh Sebastián López Murcia
