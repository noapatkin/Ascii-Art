package image;

import java.awt.*;

/**
 * The SubImages class provides utilities for dividing an image into smaller sub-images and
 * calculating their brightness based on pixel color values.
 */
public class SubImages {

    private static final int MAX_RGB = 255; // Maximum RGB value for normalization
    private static final double RED_FACTOR = 0.2126; // Weight for red channel in brightness calculation
    private static final double GREEN_FACTOR = 0.7152; // Weight for green channel in brightness calculation
    private static final double BLUE_FACTOR = 0.0722; // Weight for blue channel in brightness calculation

    /**
     * Divides the given image into a grid of sub-images based on the specified resolution.
     *
     * @param image      the image to be divided.
     * @param resolution the number of sub-images along the width of the original image.
     * @return a 2D array of sub-images.
     */
    public static Image[][] getSubImages(Image image, int resolution) {
        int imageParam = image.getWidth() / resolution;
        if (imageParam == 0) {
            imageParam = 1; // Ensure a minimum sub-image size of 1 pixel
        }
        int imageHeight = image.getHeight() / imageParam;

        Image[][] subImages = new Image[imageHeight][resolution];
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < resolution; j++) {
                subImages[i][j] = createSubImage(i * imageParam, j * imageParam, imageParam, image);
            }
        }
        return subImages;
    }

    /**
     * Creates a sub-image from a specified region of the original image.
     *
     * @param row       the starting row index of the sub-image.
     * @param col       the starting column index of the sub-image.
     * @param imageParam the size of the sub-image.
     * @param image      the original image.
     * @return the created sub-image.
     */
    private static Image createSubImage(int row, int col, int imageParam, Image image) {
        Color[][] pixelArray = new Color[imageParam][imageParam];
        for (int i = 0; i < imageParam; i++) {
            for (int j = 0; j < imageParam; j++) {
                pixelArray[i][j] = image.getPixel(row + i, col + j);
            }
        }
        return new Image(pixelArray, imageParam, imageParam);
    }

    /**
     * Calculates the brightness of the given image using a weighted sum of its RGB values.
     *
     * @param image the image whose brightness is to be calculated.
     * @return the normalized brightness value, ranging from 0 to 1.
     */
    public static double getImageBrightness(Image image) {
        if (image == null) {
            return 0; // Return 0 brightness for a null image
        }
        double graySum = 0;
        Color pixel;
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                pixel = image.getPixel(i, j);
                graySum += pixel.getRed() * RED_FACTOR + pixel.getGreen() * GREEN_FACTOR +
                        pixel.getBlue() * BLUE_FACTOR;
            }
        }
        return graySum / (image.getWidth() * image.getHeight() * MAX_RGB);
    }
}
