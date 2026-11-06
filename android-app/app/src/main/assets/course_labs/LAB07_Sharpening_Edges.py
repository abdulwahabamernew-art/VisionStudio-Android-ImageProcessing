# LAB 7 - Sharpening and Edge Detection
import cv2
import numpy as np
import matplotlib.pyplot as plt

# ---------------- Laplacian-Based Sharpening ----------------
image = cv2.imread("images/coins.jpg")
if image is None:
    raise FileNotFoundError("Image not found")

laplacian_kernel = np.array([
    [0, 1, 0],
    [1, -4, 1],
    [0, 1, 0]
], dtype=np.float32)

sharpened = cv2.filter2D(image, -1, laplacian_kernel)

# Alternative: Original - alpha * Laplacian
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
blurred = cv2.GaussianBlur(gray, (3, 3), 0)
laplacian = cv2.Laplacian(blurred, cv2.CV_64F, ksize=3)

sharpened2 = gray.astype(np.float64) - 0.5 * laplacian
sharpened2 = np.clip(sharpened2, 0, 255).astype(np.uint8)
laplacian_display = cv2.convertScaleAbs(laplacian)

fig, axs = plt.subplots(1, 3, figsize=(9, 4))
axs[0].imshow(gray, cmap="gray")
axs[0].set_title("Original")
axs[1].imshow(laplacian_display, cmap="gray")
axs[1].set_title("Laplacian")
axs[2].imshow(sharpened2, cmap="gray")
axs[2].set_title("Sharpened")

for ax in axs:
    ax.axis("off")
plt.tight_layout()
plt.show()

# ---------------- Sobel Edge Detection ----------------
image = cv2.imread("images/sunflower.jpg")
if image is not None:
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    sobel_x = cv2.Sobel(gray, cv2.CV_64F, 1, 0, ksize=3)
    sobel_y = cv2.Sobel(gray, cv2.CV_64F, 0, 1, ksize=3)

    sobel_x = cv2.convertScaleAbs(sobel_x)
    sobel_y = cv2.convertScaleAbs(sobel_y)
    sobel = cv2.addWeighted(sobel_x, 0.5, sobel_y, 0.5, 0)

    lap = cv2.Laplacian(gray, cv2.CV_64F)
    lap = cv2.convertScaleAbs(lap)

    canny = cv2.Canny(gray, 100, 200)

    fig, axs = plt.subplots(1, 4, figsize=(14, 4))
    for ax, image2, title in zip(
        axs,
        [gray, sobel, lap, canny],
        ["Gray", "Sobel", "Laplacian", "Canny"]
    ):
        ax.imshow(image2, cmap="gray")
        ax.set_title(title)
        ax.axis("off")
    plt.tight_layout()
    plt.show()
