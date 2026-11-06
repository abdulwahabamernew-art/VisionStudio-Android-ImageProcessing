# LAB 1 - Image Processing using OpenCV
import cv2

# Test OpenCV
print("OpenCV Version:", cv2.__version__)
print("OpenCV installed successfully.")

# Read an image
img = cv2.imread("images/home.jpg")
if img is None:
    raise FileNotFoundError("Image not found: images/home.jpg")

# Display, save, and close
cv2.imshow("Image", img)
cv2.waitKey(0)
cv2.imwrite("images/home.png", img)
cv2.destroyAllWindows()

# Read as grayscale
gray = cv2.imread("images/home.jpg", cv2.IMREAD_GRAYSCALE)

# Access a pixel: image[row, column]
pixel = img[100, 100]
print("Pixel value at (row=100, column=100):", pixel)

B, G, R = img[100, 100]
print("Blue =", B)
print("Green =", G)
print("Red =", R)

# Image properties
print("Shape :", img.shape)
print("Size :", img.size)
print("Data Type :", img.dtype)

height, width = img.shape[:2]
num_pixels = height * width
print("Number of Pixels :", num_pixels)
