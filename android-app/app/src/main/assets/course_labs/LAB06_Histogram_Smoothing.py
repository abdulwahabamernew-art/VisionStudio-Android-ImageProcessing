# LAB 6 - Histogram and Spatial Domain Filtering
import cv2
import numpy as np
import matplotlib.pyplot as plt

# ---------------- Histogram Equalization ----------------
img = cv2.imread("images/low_contrast.jpg", cv2.IMREAD_GRAYSCALE)
if img is None:
    raise FileNotFoundError("Image not found")

hist_original = cv2.calcHist([img], [0], None, [256], [0, 256])

equalized = cv2.equalizeHist(img)
hist_equalized = cv2.calcHist([equalized], [0], None, [256], [0, 256])

plt.figure(figsize=(10, 6))
plt.subplot(2, 2, 1)
plt.imshow(img, cmap="gray")
plt.title("Original Image")
plt.axis("off")

plt.subplot(2, 2, 2)
plt.plot(hist_original)
plt.title("Original Histogram")
plt.xlabel("Pixel Intensity")
plt.ylabel("Number of Pixels")
plt.xlim([0, 256])

plt.subplot(2, 2, 3)
plt.imshow(equalized, cmap="gray")
plt.title("Equalized Image")
plt.axis("off")

plt.subplot(2, 2, 4)
plt.plot(hist_equalized)
plt.title("Equalized Histogram")
plt.xlabel("Pixel Intensity")
plt.ylabel("Number of Pixels")
plt.xlim([0, 256])
plt.tight_layout()
plt.show()

# ---------------- Mean and Gaussian Filtering ----------------
img = cv2.imread("images/sunflower.jpg")
if img is not None:
    mean_filter = cv2.blur(img, (5, 5))
    gaussian_filter = cv2.GaussianBlur(img, (5, 5), 0)

    fig, axs = plt.subplots(1, 3, figsize=(12, 4))
    for ax, image, title in zip(
        axs,
        [img, mean_filter, gaussian_filter],
        ["Original", "Mean Filter", "Gaussian Filter"]
    ):
        ax.imshow(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
        ax.set_title(title)
        ax.axis("off")
    plt.tight_layout()
    plt.show()

# ---------------- Median Filtering ----------------
img = cv2.imread("images/noisysalterpepper.png")
if img is not None:
    median_filter = cv2.medianBlur(img, 5)

    fig, axs = plt.subplots(1, 2, figsize=(10, 4))
    axs[0].imshow(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
    axs[0].set_title("Original")
    axs[1].imshow(cv2.cvtColor(median_filter, cv2.COLOR_BGR2RGB))
    axs[1].set_title("Median Filter")

    for ax in axs:
        ax.axis("off")
    plt.tight_layout()
    plt.show()

# ---------------- Manual Median Filtering ----------------
img_noisy = cv2.imread("images/noisysalterpepper.png", cv2.IMREAD_GRAYSCALE)
if img_noisy is not None:
    m, n = img_noisy.shape
    img_new = np.zeros((m, n), dtype=np.uint8)

    for i in range(1, m - 1):
        for j in range(1, n - 1):
            neighborhood = img_noisy[i-1:i+2, j-1:j+2]
            img_new[i, j] = np.median(neighborhood)

    cv2.imwrite("new_median_filtered.png", img_new)

# ---------------- filter2D ----------------
img = cv2.imread("images/sunflower.jpg")
if img is not None:
    mask = np.ones((5, 5), np.float32) / 25
    img_filter = cv2.filter2D(src=img, ddepth=-1, kernel=mask)

    cv2.imshow("Original", img)
    cv2.imshow("filter2D", img_filter)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
