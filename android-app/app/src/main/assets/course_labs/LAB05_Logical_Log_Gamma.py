# LAB 5 - Logical Operations, Log and Power-Law Transformations
import cv2
import numpy as np
from matplotlib import pyplot as plt

# ---------------- Logical Operations ----------------
img1 = np.zeros((200, 200, 3), dtype=np.uint8)
img2 = np.zeros((200, 200, 3), dtype=np.uint8)

cv2.rectangle(img1, (50, 50), (150, 150), (255, 0, 0), -1)
cv2.rectangle(img1, (80, 80), (120, 120), (0, 0, 255), -1)
cv2.rectangle(img2, (100, 100), (180, 180), (0, 255, 0), -1)
cv2.rectangle(img2, (80, 80), (120, 120), (0, 0, 255), -1)

mask = np.zeros((200, 200), dtype=np.uint8)
cv2.circle(mask, (120, 120), 68, 255, -1)

res_and = cv2.bitwise_and(img1, img2, mask=mask)
res_or = cv2.bitwise_or(img1, img2, mask=mask)
res_xor = cv2.bitwise_xor(img1, img2, mask=mask)
res_not = cv2.bitwise_not(img1, mask=mask)

# ---------------- Object Extraction Using Mask ----------------
img = cv2.imread("images/butterfly.jpg")
if img is not None:
    img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    _, object_mask = cv2.threshold(gray, 130, 255, cv2.THRESH_BINARY)
    object_img = cv2.bitwise_and(img_rgb, img_rgb, mask=object_mask)

    plt.figure(figsize=(8, 7))
    images = [img_rgb, object_mask, object_img]
    titles = ["Original", "Mask", "Extracted Object"]

    for i, image in enumerate(images):
        plt.subplot(2, 2, i + 1)
        if image.ndim == 2:
            plt.imshow(image, cmap="gray")
        else:
            plt.imshow(image)
        plt.title(titles[i])
        plt.axis("off")
    plt.tight_layout()
    plt.show()

# ---------------- Log Transformation ----------------
img = cv2.imread("images/night_tree.jpg", cv2.IMREAD_GRAYSCALE)
if img is not None:
    c = 255 / np.log(1 + np.max(img))
    log_image = c * np.log(img.astype(np.float32) + 1)
    log_image = np.uint8(log_image)

    plt.figure(figsize=(8, 4))
    for i, image in enumerate([img, log_image]):
        plt.subplot(1, 2, i + 1)
        plt.imshow(image, cmap="gray")
        plt.title(["Original", "Log Transformation"][i])
        plt.axis("off")
    plt.tight_layout()
    plt.show()

# ---------------- Power-Law / Gamma Transformation ----------------
img = cv2.imread("images/Arithmetic.jpg")
if img is not None:
    img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    normalized = img_rgb.astype(np.float32) / 255.0

    gamma_values = [0.5, 1.5, 2.0]
    results = []

    for gamma in gamma_values:
        gamma_image = np.power(normalized, gamma)
        gamma_image = np.uint8(gamma_image * 255)
        results.append(gamma_image)

    plt.figure(figsize=(10, 6))
    plt.subplot(2, 2, 1)
    plt.imshow(img_rgb)
    plt.title("Original")
    plt.axis("off")

    for i, (gamma, result) in enumerate(zip(gamma_values, results), start=2):
        plt.subplot(2, 2, i)
        plt.imshow(result)
        plt.title(f"Gamma = {gamma}")
        plt.axis("off")

    plt.tight_layout()
    plt.show()
