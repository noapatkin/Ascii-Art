package image;

import java.awt.*;

/**
 * The Padding class provides functionality to adjust an image's dimensions by adding padding,
 * ensuring the resulting dimensions are powers of two.
 */
public class Padding {

    /**
     * Pads an image so its width and height are powers of two. Padding is added evenly around the image.
     *
     * @param image the original image to be padded.
     * @return a new image with dimensions adjusted to the nearest powers of two.
     */
    public static Image pad(Image image) {
        int newWidth = calcNewLength(image.getWidth());
        int newHeight = calcNewLength(image.getHeight());
        int heightSpace = (newHeight - image.getHeight()) / 2;
        int widthSpace = (newWidth - image.getWidth()) / 2;
        Color[][] pixelArray = new Color[newHeight][newWidth];

        for (int i = 0; i < newHeight; i++) {
            for (int j = 0; j < newWidth; j++) {
                if (i >= heightSpace && i < (heightSpace + image.getHeight())
                        && j >= widthSpace && j < (widthSpace + image.getWidth())) {
                    pixelArray[i][j] = image.getPixel(i - heightSpace, j - widthSpace);
                } else {
                    pixelArray[i][j] = Color.WHITE;
                }
            }
        }

        return new Image(pixelArray, newWidth, newHeight);
    }

    /**
     * Calculates the nearest power of two greater than or equal to the given length.
     *
     * @param oldLength the original dimension length.
     * @return the nearest power of two greater than or equal to oldLength.
     */
    private static int calcNewLength(int oldLength) {
        int newLength = 2;
        while (newLength < oldLength) {
            newLength *= 2;
        }
        return newLength;
    }
}
