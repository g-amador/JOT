/*
 * This file is part of the JOT game engine core template.
 * Copyright (C) 2014 Gonçalo Amador & Abel Gomes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * E-mail Contacts: gamador@it.ubi.pt & A. Gomes (agomes@di.ubi.pt)
 */
package util;

import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.io.script.OptionsParser.parseFile;
import static jot.io.script.OptionsParser.showLoadedOptions;
import static jot.util.CoreOptions.coreOptions;
import static jot.util.CoreOptions.showCoreOptions;

/**
 * Class to set up the game constants.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class GameConstants {

    static final Logger log = getLogger("GameConstants");

    /**
     * Method to set up the defaults for the game constants options.
     */
    public static void setGameConstants() {
        //TODO: (optional) If no options set, defaults are used instead. 
        //read options from file    
        loadGameConstants("assets/scripts/", "GameConstants.js");

        //Set the the defaults for the game constants options assuming all 
        //libraries are loaded as in this template, e.g.: 
        //Input devices options
        //coreOptions.put("useKeyBoard", true);
        //coreOptions.put("useMouse", true);
        //coreOptions.put("useMouseCursor", true);
        //Geometry options
        //coreOptions.put("showTextures", true);
        //coreOptions.put("showWireframe", false);
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

        showCoreOptions();
        parseFile(filePath + fileName, "coreOptions", coreOptions);
        showLoadedOptions(true);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private GameConstants() {
    }
}
