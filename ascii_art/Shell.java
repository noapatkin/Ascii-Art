package ascii_art;

import ascii_output.ConsoleAsciiOutput;
import ascii_output.HtmlAsciiOutput;
import exceptions.BoundariesExceptions;
import exceptions.IncorrectFormatException;
import image.Image;
import image.Padding;
import image_char_matching.SubImgCharMatcher;

import java.io.IOException;
import java.util.*;

/**
 * Shell class serves as a command-line interface for generating ASCII art from an image.
 * It supports various commands to manipulate resolution, character sets, output destinations, and more.
 */
public class Shell {

    // Shell-related constants
    private static final String SHELL_PROMPT = ">>> ";
    private static final String EXIT_CMD = "exit";
    private static final String CHAR_CMD = "chars";
    private static final String ADD_CMD = "add";
    private static final String REMOVE_CMD = "remove";
    private static final String OUTPUT_CMD = "output";
    private static final String RESOLUTION_CMD = "res";
    private static final String ROUND_CMD = "round";
    private static final String ASCII_ART_CMD = "asciiArt";

    // Output-related constants
    private static final String CONSOLE_OUTPUT = "console";
    private static final String HTML_OUTPUT = "html";

    // Resolution and rounding constants
    private static final String UP = "up";
    private static final String DOWN = "down";
    private static final String ABS = "abs";

    // Character handling constants
    private static final String ALL = "all";
    private static final String SPACE = "space";
    private static final char START_SPACE_CHAR = 32; // ASCII code for space
    private static final char END = 126; // ASCII code for '~'
    private static final char RANGE_DIVIDER = '-';

    // Error messages and information
    private static final String RES_INFO = "Resolution set to ";
    private static final String ROUND_EXCEPTION_MESSAGE = "Did not change rounding method due to incorrect format.";
    private static final String REMOVE_EXCEPTION_MESSAGE = "Did not remove due to incorrect format.";
    private static final String ADD_EXCEPTION_MESSAGE = "Did not add due to incorrect format.";
    private static final String RES_OUT_BOUNDARIES = "Did not change resolution due to exceeding boundaries.";
    private static final String RES_ERR = "Did not change resolution due to incorrect format.";
    private static final String OUTPUT_DESTINATION_ERR = "Did not change output method due to incorrect format.";
    private static final String SMALL_CHARSET_ERR = "Did not execute. Charset is too small.";
    private static final String INCORRECT_CMD_ERR = "Did not execute due to incorrect command.";
    private static final String INVALID_IMAGE_PATH = "Did not execute due to incorrect command.";
    // Default settings
    private static final char[] DEFAULT_CHARSET = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final int DEFAULT_RES = 2;
    private static final String HTML_STREAM = "out.html";
    private static final String HTML_FONT = "Courier New";
    private static final int MIN_CHAR_ARR_SIZE = 2;

    // Fields
    private final SubImgCharMatcher subImgCharMatcher;
    private int resolution;
    private String output_destination;
    private String round_method;
    private Image image;
    public Set<Character> charArray = new HashSet<>();

    /**
     * Constructor initializes the shell with default settings and character set.
     */
    Shell() {
        this.output_destination = CONSOLE_OUTPUT;
        this.round_method = ABS;
        this.resolution = DEFAULT_RES;
        this.subImgCharMatcher = new SubImgCharMatcher(DEFAULT_CHARSET);
        for (char c : DEFAULT_CHARSET) {
            charArray.add(c);
        }
    }

    /**
     * Runs the shell, allowing user interaction via commands.
     *
     * @param imageName the name of the image file to process.
     * @throws IOException if there is an error reading input or processing the image.
     */
    public void run(String imageName) {
        try{
            this.image = new Image(imageName);
        }
        catch (IOException ioException){
            System.err.println(INVALID_IMAGE_PATH);
        }
        this.image = Padding.pad(image);
        System.out.print(SHELL_PROMPT);
        String output = KeyboardInput.readLine();
        while (!Objects.equals(output, EXIT_CMD)) {
            try {
                String[] splittedOutput = output.split(" ");
                handleCmd(splittedOutput[0], splittedOutput.length < 2 ? "" : splittedOutput[1]);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            System.out.print(SHELL_PROMPT);
            output = KeyboardInput.readLine();
        }
    }

    /**
     * Handles various shell commands.
     *
     * @param cmd   the command to execute.
     * @param param the parameter for the command, if applicable.
     * @throws IOException if the command fails.
     */
    private void handleCmd(String cmd, String param) throws Exception {
        switch (cmd) {
            case CHAR_CMD:
                printAvailableChars();
                break;
            case ADD_CMD:
                processChars(param, true);
                break;
            case REMOVE_CMD:
                processChars(param, false);
                break;
            case OUTPUT_CMD:
                setOutputDestination(param);
                break;
            case ROUND_CMD:
                setRound(param);
                break;
            case ASCII_ART_CMD:
                asciiArt();
                break;
            case RESOLUTION_CMD:
                setResolution(param);
                break;

            default:
                throw new IncorrectFormatException(INCORRECT_CMD_ERR);
        }
    }

    /**
     * Prints available characters in the character set.
     */
    private void printAvailableChars() {
        for (char c : charArray) {
            System.out.print(c + " ");
        }
        System.out.println();
    }

    /**
     * Processes character addition or removal based on the input parameter.
     *
     * @param param the character(s) to add or remove.
     * @param isAdd true to add characters, false to remove.
     * @throws IOException if the format is invalid.
     */
    private void processChars(String param, boolean isAdd) throws Exception {
        if (param.length() == 1 && validateChar(param.charAt(0))) {
            handleChar(param.charAt(0), isAdd);
        } else if (param.length() == 3 && param.charAt(1) == RANGE_DIVIDER &&
                validateChar(param.charAt(2)) && validateChar(param.charAt(0))) {
            char rangeStart = (char) Math.min(param.charAt(0), param.charAt(2));
            char rangeEnd = (char) Math.max(param.charAt(0), param.charAt(2));
            handleRange(rangeStart, rangeEnd, isAdd);
        } else if (Objects.equals(param, SPACE)) {
            handleChar(START_SPACE_CHAR, isAdd);
        } else if (Objects.equals(param, ALL)) {
            handleRange(START_SPACE_CHAR, END, isAdd);
        } else {
            throw new IncorrectFormatException(isAdd ? ADD_EXCEPTION_MESSAGE : REMOVE_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Sets the rounding method for image character matching.
     *
     * @param method the rounding method ("up", "down", or "abs").
     * @throws IOException if the method is invalid.
     */
    private void setRound(String method) throws Exception {
        if (method.equals(UP) || method.equals(DOWN) || method.equals(ABS)) {
            this.round_method = method;
        } else {
            throw new IncorrectFormatException(ROUND_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Changes the output destination for ASCII art.
     *
     * @param destination "console" or "html".
     */
    private void setOutputDestination(String destination) throws Exception {
        if (destination.equals(CONSOLE_OUTPUT) || destination.equals(HTML_OUTPUT)) {
            this.output_destination = destination;
        } else {
            throw new IncorrectFormatException(OUTPUT_DESTINATION_ERR);
        }
    }

    /**
     * Adjusts the resolution of the ASCII art.
     *
     * @param resolution the new resolution or direction ("up"/"down").
     */
    private void setResolution(String resolution) throws Exception{
        int minCharsInRow = Math.max(1, this.image.getWidth() / this.image.getHeight());

        switch (resolution) {
            case UP:
                if (this.resolution * 2 > this.image.getWidth()) {
                    throw new BoundariesExceptions(RES_OUT_BOUNDARIES);
                } else {
                    this.resolution *= 2;
                    System.out.println(RES_INFO + this.resolution);
                }
                break;
            case DOWN:
                if (this.resolution / 2 < minCharsInRow) {
                    throw new BoundariesExceptions(RES_OUT_BOUNDARIES);
                } else {
                    this.resolution /= 2;
                    System.out.println(RES_INFO + this.resolution);
                }
                break;
            case "":
                System.out.println(RES_INFO + this.resolution);
                break;
            default:
                throw new IncorrectFormatException(RES_ERR);
        }
    }

    /**
     * Generates ASCII art and outputs it based on the current settings.
     */
    private void asciiArt() throws Exception {
        if (charArray.size() <= MIN_CHAR_ARR_SIZE) {
            throw new BoundariesExceptions(SMALL_CHARSET_ERR);
        }

        subImgCharMatcher.setDefaultRound(round_method);
        AsciiArtAlgorithm asciiArtAlgorithm = new AsciiArtAlgorithm(image, resolution, subImgCharMatcher);
        char[][] asciiImage = asciiArtAlgorithm.run();
        if (output_destination.equals(HTML_OUTPUT)) {
            HtmlAsciiOutput htmlAsciiOutput = new HtmlAsciiOutput(HTML_STREAM, HTML_FONT);
            htmlAsciiOutput.out(asciiImage);
        } else {
            ConsoleAsciiOutput consoleAsciiOutput = new ConsoleAsciiOutput();
            consoleAsciiOutput.out(asciiImage);
        }
    }

    /**
     * Entry point of the program. Initializes and runs the shell.
     *
     * @param args command-line arguments.
     * @throws IOException if there is an error during execution.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            return;
        }
        Shell shell = new Shell();
        shell.run(args[1]);
    }

    /**
     * Handles a range of characters for addition or removal.
     *
     * @param rangeStart the starting character of the range.
     * @param rangeEnd   the ending character of the range.
     * @param isAdd      true to add characters, false to remove.
     */
    private void handleRange(char rangeStart, char rangeEnd, boolean isAdd) {
        if (isAdd) {
            addRange(rangeStart, rangeEnd);
        } else {
            removeRange(rangeStart, rangeEnd);
        }
    }

    /**
     * Handles a single character for addition or removal.
     *
     * @param c     the character to handle.
     * @param isAdd true to add, false to remove.
     */
    private void handleChar(char c, boolean isAdd) {
        if (isAdd) {
            charArray.add(c);
            subImgCharMatcher.addChar(c);
        } else {
            charArray.remove(c);
            subImgCharMatcher.removeChar(c);
        }
    }

    /**
     * Adds a range of characters to the character set.
     *
     * @param start the starting character of the range.
     * @param end   the ending character of the range.
     */
    private void addRange(char start, char end) {
        for (char i = start; i <= end; i++) {
            charArray.add(i);
            subImgCharMatcher.addChar(i);
        }
    }

    /**
     * Removes a range of characters from the character set.
     *
     * @param start the starting character of the range.
     * @param end   the ending character of the range.
     */
    private void removeRange(char start, char end) {
        for (char i = start; i <= end; i++) {
            charArray.remove(i);
            subImgCharMatcher.removeChar(i);
        }
    }

    /**
     * Validates if a character is within the allowed range.
     *
     * @param c the character to validate.
     * @return true if valid, false otherwise.
     */
    private boolean validateChar(char c) {
        return c >= START_SPACE_CHAR && c <= END;
    }
}
