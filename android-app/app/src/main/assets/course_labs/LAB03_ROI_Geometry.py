# LAB 3 - ROI and Image Geometry
import cv2
import numpy as np
import matplotlib.pyplot as plt

# ---------------- Cropping / ROI ----------------
img = cv2.imread("images/planet_glow.jpg")
if img is None:
    raise FileNotFoundError("Image not found")

# image[y1:y2, x1:x2]
roi = img[50:250, 100:350]
cv2.imwrite("cropped.jpg", roi)

# ---------------- Resizing ----------------
height, width = img.shape[:2]

new_width = width * 2
new_height = height * 2
zoomed_image = cv2.resize(
    img, (new_width, new_height), interpolation=cv2.INTER_CUBIC
)

new_width1 = width // 2
new_height1 = height // 2
shrink_image = cv2.resize(
    img, (new_width1, new_height1), interpolation=cv2.INTER_AREA
)

cv2.imshow("Original", img)
cv2.imshow("Zoomed", zoomed_image)
cv2.imshow("Shrink", shrink_image)
cv2.waitKey(0)
cv2.destroyAllWindows()

# ---------------- Rotation ----------------
img = cv2.imread("images/balloons.jpg")
if img is None:
    raise FileNotFoundError("Image not found")

h, w = img.shape[:2]
center = (w // 2, h // 2)

rotation_matrix = cv2.getRotationMatrix2D(center, 30, 1.0)
rotated_image = cv2.warpAffine(img, rotation_matrix, (w, h))

rotation_matrix2 = cv2.getRotationMatrix2D(center, -30, 1.0)
rotated_image2 = cv2.warpAffine(img, rotation_matrix2, (w, h))

# ---------------- Translation ----------------
tx, ty = 50, 30
translation_matrix = np.float32([
    [1, 0, tx],
    [0, 1, ty]
])
translated_image = cv2.warpAffine(img, translation_matrix, (w, h))

fig, axs = plt.subplots(1, 4, figsize=(14, 4))
for ax, image, title in zip(
    axs,
    [img, rotated_image, rotated_image2, translated_image],
    ["Original", "Rotate +30", "Rotate -30", "Translated"]
):
    ax.imshow(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
    ax.set_title(title)
    ax.axis("off")

plt.tight_layout()
plt.show()
