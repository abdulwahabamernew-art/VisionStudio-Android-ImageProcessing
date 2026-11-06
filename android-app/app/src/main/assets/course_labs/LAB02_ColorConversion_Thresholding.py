# LAB 2 - Color Conversion and Thresholding
import cv2
import numpy as np

img = cv2.imread("images/home.jpg")
if img is None:
    raise FileNotFoundError("Image not found")

# 1. Convert color image to grayscale
gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

# 2. Convert grayscale image to binary
T = 127
_, binary = cv2.threshold(gray, T, 255, cv2.THRESH_BINARY)

# 3. Effect of different threshold values
_, binary_80 = cv2.threshold(gray, 80, 255, cv2.THRESH_BINARY)
_, binary_127 = cv2.threshold(gray, 127, 255, cv2.THRESH_BINARY)
_, binary_180 = cv2.threshold(gray, 180, 255, cv2.THRESH_BINARY)

# 4. OpenCV thresholding
_, cv_binary = cv2.threshold(gray, 127, 255, cv2.THRESH_BINARY)

# Manual thresholding using NumPy
manual_binary = np.zeros_like(gray)
manual_binary[gray >= 127] = 255

cv2.imshow("Original", img)
cv2.imshow("Gray", gray)
cv2.imshow("Binary", binary)
cv2.imshow("Manual Binary", manual_binary)
cv2.waitKey(0)
cv2.destroyAllWindows()
