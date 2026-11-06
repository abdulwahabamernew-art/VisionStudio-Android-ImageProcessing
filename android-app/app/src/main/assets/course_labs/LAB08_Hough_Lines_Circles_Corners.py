# LAB 8 - Hough Lines, Hough Circles and Corner Detection
import cv2
import numpy as np
import matplotlib.pyplot as plt

# ---------------- Probabilistic Hough Line Detection ----------------
img = cv2.imread("images/houghlines.jpg")
if img is not None:
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    edges = cv2.Canny(gray, 50, 128)

    lines = cv2.HoughLinesP(
        edges,
        1,
        np.pi / 180,
        threshold=20,
        minLineLength=50,
        maxLineGap=10
    )

    if lines is not None:
        for line in lines:
            x1, y1, x2, y2 = line[0]
            cv2.line(img, (x1, y1), (x2, y2), (0, 255, 0), 2)

    cv2.imshow("Hough Lines", img)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

# ---------------- Hough Circle Detection ----------------
planets = cv2.imread("images/planets.jpg")
if planets is not None:
    gray = cv2.cvtColor(planets, cv2.COLOR_BGR2GRAY)
    gray = cv2.medianBlur(gray, 5)

    circles = cv2.HoughCircles(
        gray,
        cv2.HOUGH_GRADIENT,
        dp=1.2,
        minDist=20,
        param1=100,
        param2=30,
        minRadius=0,
        maxRadius=100
    )

    if circles is not None:
        circles = np.uint16(np.around(circles))
        for x, y, r in circles[0, :]:
            cv2.circle(planets, (x, y), r, (0, 255, 0), 2)
            cv2.circle(planets, (x, y), 2, (0, 0, 255), 3)

    cv2.imwrite("planets_hough_circles.jpg", planets)
    cv2.imshow("HoughCircles", planets)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

# ---------------- Ball Detection with HoughCircles ----------------
img = cv2.imread("images/foot_ball.png")
if img is not None:
    output = img.copy()
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    gray = cv2.medianBlur(gray, 5)

    circles = cv2.HoughCircles(
        gray,
        cv2.HOUGH_GRADIENT,
        dp=1.2,
        minDist=50,
        param1=100,
        param2=30,
        minRadius=28,
        maxRadius=80
    )

    if circles is not None:
        circles = np.uint16(np.around(circles))
        for x, y, r in circles[0, :]:
            cv2.circle(output, (x, y), r, (0, 255, 0), 2)

    cv2.imshow("Ball Detection", output)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

# ---------------- Shi-Tomasi Corner Detection ----------------
img = cv2.imread("images/corners.png")
if img is not None:
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    corners = cv2.goodFeaturesToTrack(
        gray,
        maxCorners=100,
        qualityLevel=0.01,
        minDistance=10
    )

    if corners is not None:
        corners = np.intp(corners)
        for corner in corners:
            x, y = corner.ravel()
            cv2.circle(img, (x, y), 4, (255, 0, 0), 1)

    plt.imshow(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
    plt.title("Corner Detection - Shi-Tomasi")
    plt.axis("off")
    plt.show()
