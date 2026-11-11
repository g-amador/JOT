package jot.demo.util;

import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.io.script.OptionsParser.parseFile;
import static jot.io.script.OptionsParser.showLoadedOptions;
import static jot.util.FrameworkOptions.frameworkOptions;
import static jot.util.FrameworkOptions.showFrameworkOptions;

/**
 * Class to set up the game constants.
 *
 * @author G. Amador & A. Gomes
 */
public class GameConstants {

    static final Logger log = getLogger("GameConstants");

    /**
     * Method to set up the defaults for the game constants options.
     */
    public static void setGameConstants() {
        loadGameConstants("assets/scripts/", "GameConstants.js");
    }

    /**
     * Load game constants values from provided file in provided path.
     *
     * @param filePath the path to the file containing the game constants
     * values.
     * @param fileName the file from which to read the game constants values.
     */
    public static void loadGameConstants(String filePath, String fileName) {
        if (GameConstants.class
                .getResource(filePath.replace("assets", "") + fileName) != null) { //Using IDE
            filePath = GameConstants.class.getResource(filePath.replace("assets", "")).getPath();
        }

        showFrameworkOptions();
        frameworkOptions.setOptions(parseFile(filePath + fileName,
                "frameworkOptions", frameworkOptions.getOptions()));
        showLoadedOptions(true);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private GameConstants() {
    }
}
