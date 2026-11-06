# LAB 4 - Drawing and Arithmetic Operations
import cv2
import numpy as np
from matplotlib import pyplot as plt

# ---------------- Drawing on Images ----------------
canvas = np.zeros((512, 512, 3), dtype=np.uint8)

cv2.line(canvas, (0, 0), (512, 512), (255, 0, 0), 3)
cv2.rectangle(canvas, (50, 50), (200, 200), (0, 255, 0), 3)
cv2.circle(canvas, (350, 150), 80, (0, 0, 255), 3)
cv2.ellipse(canvas, (250, 350), (120, 60), 0, 0, 360, (255, 255, 0), 3)

points = np.array([[100, 400], [150, 300], [250, 400], [100, 400]])
cv2.polylines(canvas, [points], True, (255, 255, 255), 2)

cv2.imshow("Drawing", canvas)
cv2.waitKey(0)
cv2.destroyAllWindows()

# ---------------- Addition / Blending ----------------
img1 = cv2.imread("images/1-500x250-3.jpg")
img2 = cv2.imread("images/2-500x250-2.jpg")
if img1 is None or img2 is None:
    raise FileNotFoundError("Arithmetic-operation images not found")

img1 = cv2.resize(img1, (400, 300))
img2 = cv2.resize(img2, (400, 300))

result_add = cv2.add(img1, img2)
result_blend = cv2.addWeighted(img1, 0.7, img2, 0.3, 0)

# ---------------- Subtraction ----------------
result_subtract = cv2.subtract(img1, img2)
result_absdiff = cv2.absdiff(img1, img2)

# ---------------- Multiplication / Division ----------------
arithmetic = cv2.imread("images/Arithmetic.jpg")
if arithmetic is not None:
    arithmetic = cv2.resize(arithmetic, (400, 300))
    multiply_img = cv2.multiply(arithmetic, 2)
    divide_img = cv2.divide(arithmetic, 2)

images = [img1, img2, result_blend]
titles = ["Image 1", "Image 2", "Blending"]

plt.figure(figsize=(12, 4))
for i in range(len(images)):
    plt.subplot(1, 3, i + 1)
    plt.imshow(cv2.cvtColor(images[i], cv2.COLOR_BGR2RGB))
    plt.title(titles[i])
    plt.axis("off")
plt.tight_layout()
plt.show()
