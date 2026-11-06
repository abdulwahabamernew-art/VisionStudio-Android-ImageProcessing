# LAB 9 - Image Segmentation
import cv2
import numpy as np
import matplotlib.pyplot as plt

# ---------------- 1. Threshold Segmentation ----------------
img = cv2.imread("images/water_coins.jpg")
if img is not None:
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    T = 127
    max_value = 255

    _, binary = cv2.threshold(gray, T, max_value, cv2.THRESH_BINARY)
    _, binary_inv = cv2.threshold(gray, T, max_value, cv2.THRESH_BINARY_INV)
    _, trunc = cv2.threshold(gray, T, max_value, cv2.THRESH_TRUNC)
    _, tozero = cv2.threshold(gray, T, max_value, cv2.THRESH_TOZERO)
    _, tozero_inv = cv2.threshold(gray, T, max_value, cv2.THRESH_TOZERO_INV)

    images = [img, gray, binary, binary_inv, trunc, tozero, tozero_inv]
    titles = [
        "Original Image", "Gray Image", "Binary",
        "Binary Inverse", "Truncate", "To Zero", "To Zero Inverse"
    ]

    plt.figure(figsize=(13, 7))
    for i in range(len(images)):
        plt.subplot(2, 4, i + 1)
        if i == 0:
            plt.imshow(cv2.cvtColor(images[i], cv2.COLOR_BGR2RGB))
        else:
            plt.imshow(images[i], cmap="gray")
        plt.title(titles[i])
        plt.axis("off")
    plt.tight_layout()
    plt.show()

# ---------------- 2. Otsu Threshold ----------------
img = cv2.imread("images/water_coins.jpg")
if img is not None:
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    _, binary = cv2.threshold(gray, 127, 255, cv2.THRESH_BINARY)
    otsu_value, otsu = cv2.threshold(
        gray, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU
    )

    print("Otsu Threshold Value =", otsu_value)

    cv2.imshow("Original", img)
    cv2.imshow("Gray", gray)
    cv2.imshow("Binary Threshold", binary)
    cv2.imshow("Otsu Threshold", otsu)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

# ---------------- 3. Adaptive Threshold ----------------
img = cv2.imread("images/sudoku.jpeg")
if img is not None:
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    adaptive_mean = cv2.adaptiveThreshold(
        gray, 255,
        cv2.ADAPTIVE_THRESH_MEAN_C,
        cv2.THRESH_BINARY,
        11, 2
    )

    adaptive_gaussian = cv2.adaptiveThreshold(
        gray, 255,
        cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY,
        11, 2
    )

    cv2.imshow("Original Image", img)
    cv2.imshow("Gray Image", gray)
    cv2.imshow("Adaptive Mean", adaptive_mean)
    cv2.imshow("Adaptive Gaussian", adaptive_gaussian)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

# ---------------- 4. Color Masking ----------------
sample_image = cv2.imread("images/shapes.webp")
if sample_image is not None:
    # OpenCV uses BGR. Convert to HSV for robust color segmentation.
    hsv = cv2.cvtColor(sample_image, cv2.COLOR_BGR2HSV)

    low = np.array([0, 80, 20])
    high = np.array([215, 255, 255])

    mask = cv2.inRange(hsv, low, high)
    result = cv2.bitwise_and(sample_image, sample_image, mask=mask)

    fig, axes = plt.subplots(1, 3, figsize=(15, 5))
    axes[0].imshow(cv2.cvtColor(sample_image, cv2.COLOR_BGR2RGB))
    axes[0].set_title("Original Image")
    axes[0].axis("off")

    axes[1].imshow(mask, cmap="gray")
    axes[1].set_title("Mask")
    axes[1].axis("off")

    axes[2].imshow(cv2.cvtColor(result, cv2.COLOR_BGR2RGB))
    axes[2].set_title("Result")
    axes[2].axis("off")

    plt.tight_layout()
    plt.show()

# ---------------- 5. K-Means Segmentation ----------------
img = cv2.imread("images/images1.webp")
if img is not None:
    Z = img.reshape((-1, 3))
    Z = np.float32(Z)

    criteria = (
        cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER,
        10,
        1.0
    )

    K = 3
    _, labels, centers = cv2.kmeans(
        Z, K, None, criteria, 10, cv2.KMEANS_RANDOM_CENTERS
    )

    centers = np.uint8(centers)
    res = centers[labels.flatten()]
    segmented_img = res.reshape(img.shape)

    plt.figure(figsize=(12, 5))
    plt.subplot(1, 2, 1)
    plt.imshow(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
    plt.title("Original Image")
    plt.axis("off")

    plt.subplot(1, 2, 2)
    plt.imshow(cv2.cvtColor(segmented_img, cv2.COLOR_BGR2RGB))
    plt.title("K-Means Segmentation")
    plt.axis("off")

    plt.tight_layout()
    plt.show()
