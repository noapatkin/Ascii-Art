package image_char_matching;

import java.util.*;

/**
 * The SubImgCharMatcher class maps sub-image brightness to characters, allowing for ASCII art generation.
 * It supports adding and removing characters, normalizing brightness values, and matching brightness
 * to characters based on different rounding strategies.
 */
public class SubImgCharMatcher {

    private static final int DEFAULT_RESOLUTION = 16; // Default resolution for brightness calculation
    private final HashSet<Character> charSet; // Set of characters available for matching
    private TreeMap<Double, Character> normBrightMap; // Normalized brightness to character mapping

    private static final String UP = "up";
    private static final String DOWN = "down";
    private static final String ABS = "abs";
    private String defaultRound = ABS; // Default rounding method
    private boolean shouldChange = true; // Flag indicating if brightness mapping needs normalization
    private final CharBrightnessSnapShot snapshot; // Snapshot of character brightness values

    /**
     * Constructor initializes the matcher with a character set and calculates initial brightness values.
     *
     * @param charset array of characters to use in matching.
     */
    public SubImgCharMatcher(char[] charset) {
        snapshot = new CharBrightnessSnapShot();
        charSet = new HashSet<>();
        for (char c : charset) {
            charSet.add(c);
            snapshot.add(c, getBrightness(c));
        }
    }

    /**
     * Matches a brightness value to the closest character in the normalized brightness map.
     *
     * @param brightness the brightness value to match.
     * @return the character corresponding to the brightness.
     */
    public char getCharByImageBrightness(double brightness) {
        if (shouldChange) {
            normalizeBrightMap();
        }

        double ceilingClosest = normBrightMap.ceilingKey(brightness);
        double floorClosest = normBrightMap.floorKey(brightness);
        double closest;
        switch (defaultRound) {
            case UP:
                closest = ceilingClosest;
                break;
            case DOWN:
                closest = floorClosest;
                break;
            default:
                closest = (brightness - floorClosest > ceilingClosest - brightness) ? ceilingClosest : floorClosest;
        }
        return normBrightMap.get(closest);
    }

    /**
     * Adds a character to the character set and updates brightness mappings.
     *
     * @param c the character to add.
     */
    public void addChar(char c) {
        if (snapshot.getBrightness(c) == null) {
            snapshot.add(c, getBrightness(c));
        }
        charSet.add(c);
        shouldChange = true;
    }

    /**
     * Removes a character from the character set and marks the brightness map for update.
     *
     * @param c the character to remove.
     */
    public void removeChar(char c) {
        charSet.remove(c);
        shouldChange = true;
    }

    /**
     * Calculates the brightness of a character based on its pixel representation.
     *
     * @param c the character whose brightness is to be calculated.
     * @return the brightness value of the character.
     */
    private double getBrightness(char c) {
        double brightness = 0;
        boolean[][] cArray = CharConverter.convertToBoolArray(c);
        for (boolean[] row : cArray) {
            for (boolean cell : row) {
                if (cell) {
                    brightness += 1;
                }
            }
        }
        return brightness / DEFAULT_RESOLUTION;
    }

    /**
     * Normalizes the brightness map for the current character set.
     */
    private void normalizeBrightMap() {
        normBrightMap = new TreeMap<>();
        double[] minmax = snapshot.getMaxMin(charSet);
        double minBrightness = minmax[1];
        double maxBrightness = minmax[0];

        for (char c : charSet) {
            double normalized = (snapshot.getBrightness(c) - minBrightness) / (maxBrightness - minBrightness);
            if (normBrightMap.containsKey(normalized)) {
                if (normBrightMap.get(normalized) > c) {
                    normBrightMap.put(normalized, c);
                }
            } else {
                normBrightMap.put(normalized, c);
            }
        }
        shouldChange = false;
    }

    /**
     * Sets the default rounding strategy for brightness matching.
     *
     * @param defaultRound the rounding strategy ("up", "down", or "abs").
     */
    public void setDefaultRound(String defaultRound) {
        this.defaultRound = defaultRound;
    }

    /**
     * Inner class for managing brightness snapshots of characters.
     */
    private static class CharBrightnessSnapShot {
        private static final HashMap<Character, Double> brightMap = new HashMap<>();

        /**
         * Adds a character and its brightness value to the snapshot.
         *
         * @param c the character to add.
         * @param brightness the brightness value of the character.
         */
        private void add(char c, double brightness) {
            brightMap.put(c, brightness);
        }

        /**
         * Retrieves the brightness value of a character from the snapshot.
         *
         * @param c the character whose brightness is to be retrieved.
         * @return the brightness value of the character, or null if not found.
         */
        private Double getBrightness(char c) {
            return brightMap.get(c);
        }

        /**
         * Computes the maximum and minimum brightness values for a set of characters.
         *
         * @param charSet the set of characters to analyze.
         * @return an array containing the maximum and minimum brightness values.
         */
        private double[] getMaxMin(HashSet<Character> charSet) {
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;
            for (Character c : charSet) {
                double brightness = brightMap.get(c);
                if (brightness > max) {
                    max = brightness;
                }
                if (brightness < min) {
                    min = brightness;
                }
            }
            return new double[]{max, min};
        }
    }
}
