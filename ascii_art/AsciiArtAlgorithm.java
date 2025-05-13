package ascii_art;

import image.Image;
import image.Padding;
import image.SubImages;
import image_char_matching.SubImgCharMatcher;

/**
 * The AsciiArtAlgorithm class processes an image and generates a two-dimensional array of characters
 * representing the image as ASCII art. It utilizes image padding, sub-image brightness calculations,
 * and character matching based on brightness levels.
 */
public class AsciiArtAlgorithm {

    private final Image image; // The source image to process
    private final int resolution; // Resolution for the ASCII art
    private final SubImgCharMatcher subImgCharMatcher; // Matcher for selecting characters based on brightness
    private static Snapshot memento; // Cache for brightness data to optimize repeated calls

    /**
     * Constructor initializes the algorithm with the given image, resolution, and character matcher.
     *
     * @param image            the source image.
     * @param resolution       the resolution for the ASCII art.
     * @param subImgCharMatcher the character matcher for brightness-based matching.
     */
    public AsciiArtAlgorithm(Image image, int resolution, SubImgCharMatcher subImgCharMatcher) {
        this.image = image;
        this.resolution = resolution;
        this.subImgCharMatcher = subImgCharMatcher;
    }

    /**
     * Processes the image and generates a 2D array of characters representing the ASCII art.
     *
     * @return a 2D array of characters forming the ASCII art.
     */
    public char[][] run() {
        int asciiImgLength = (image.getHeight() * resolution) / image.getWidth();
        int asciiImgWidth = resolution;
        char[][] asciiImage = new char[asciiImgLength][asciiImgWidth];
        double[][] subImgBrightness = getSubImgBrightness(asciiImgLength, asciiImgWidth);
        for (int i = 0; i < asciiImgLength; i++) {
            for (int j = 0; j < asciiImgWidth; j++) {
                asciiImage[i][j] = subImgCharMatcher.getCharByImageBrightness(subImgBrightness[i][j]);
            }
        }
        return asciiImage;
    }

    /**
     * Compares the current image with another image for equality.
     *
     * @param other the other image to compare.
     * @return true if the images are equal, false otherwise.
     */
    private boolean isImgEqual(Image other) {
        if (image == other) {
            return true;
        }
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                if (image.getPixel(i, j) != other.getPixel(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Computes the brightness of sub-images and retrieves cached values if possible.
     *
     * @param asciiImgLength the length of the ASCII image.
     * @param asciiImgWidth  the width of the ASCII image.
     * @return a 2D array of brightness values for the sub-images.
     */
    private double[][] getSubImgBrightness(int asciiImgLength, int asciiImgWidth) {
        if (memento != null) {
            if (isImgEqual(memento.image) && memento.resolution == resolution) {
                return memento.subImgBrightness;
            }
        }
        double[][] subImgBrightness = createSubImgBrightness(asciiImgLength, asciiImgWidth);
        memento = new Snapshot(image, resolution, subImgBrightness);
        return subImgBrightness;
    }

    private double[][] createSubImgBrightness(int asciiImgLength, int asciiImgWidth) {
        double[][] subImgBrightness = new double[asciiImgLength][asciiImgWidth];
        Image[][] subImages = SubImages.getSubImages(image, resolution);
        for (int i = 0; i < asciiImgLength; i++) {
            for (int j = 0; j < asciiImgWidth; j++) {
                subImgBrightness[i][j] = SubImages.getImageBrightness(subImages[i][j]);
            }
        }
        return subImgBrightness;
    }

    /**
     * Inner class representing a cached snapshot of brightness data.
     */
    private static class Snapshot {
        private final double[][] subImgBrightness; // Cached brightness values
        private final Image image; // The image associated with the cached data
        private final int resolution; // The resolution associated with the cached data

        /**
         * Constructor initializes the snapshot with the given image, resolution, and brightness data.
         *
         * @param image            the image associated with the snapshot.
         * @param resolution       the resolution of the snapshot.
         * @param subImgBrightness the brightness data to cache.
         */
        private Snapshot(Image image, int resolution, double[][] subImgBrightness) {
            this.image = image;
            this.resolution = resolution;
            this.subImgBrightness = subImgBrightness;
        }
    }
}

//todo make sure that the max res is correct!!