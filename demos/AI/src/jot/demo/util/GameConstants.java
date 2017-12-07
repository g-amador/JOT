/*
 * This file is part of the PathFinder program. This is a simple program that
 * serves as a testbed for steering behaviors, pathFinding, and maze generation
 * algorithms. The program features a JogAmp-based graphical component, to
 * visualize the graph to traverse, the found (if one exists) path and the
 * traversed nodes.
 *
 * The program also includes a loader for Collada 1.4 models and HOG2
 * Pathfinding Benchmarks, available at <http://www.movingai.com/benchmarks/>.
 * Copyright (C) 2014 Gonçalo Amador & Abel Gomes
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * E-mail Contacts: G. Amador (g.n.p.amador@gmail.com) &
 *                  A. Gomes (agomes@it.ubi.pt)
 */
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
 * @author G. Amador {@literal &} A. Gomes
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
