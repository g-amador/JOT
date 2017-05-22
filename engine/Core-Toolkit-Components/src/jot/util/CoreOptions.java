/*
 * This file is part of the JOT game engine core toolkit component.
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
package jot.util;

import static java.lang.String.format;
import java.util.HashMap;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * Class that sets the default geometry and i/o options.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class CoreOptions {

    static final Logger log = getLogger("CoreOptions");

    /**
     * The core options identifier status pairs.
     */
    public static final HashMap<String, Boolean> coreOptions = new HashMap<>();

    static {
        log.setLevel(OFF);

        //Text display Options
        /**
         * Show FPS text on/off.
         */
        coreOptions.put("showFPS", true);

        //Geometry Options
        /**
         * Show textures on/off.
         */
        coreOptions.put("showTextures", true);

        /**
         * Show wireframe on/off.
         */
        coreOptions.put("showWireframe", false);

        /**
         * Toggle On/Off OpenGL display list for mesh rendering instead of
         * drawArrays or drawElements.
         */
        coreOptions.put("useDisplayLists", false);

        /**
         * Toggle On/Off diagonals when generating HOG2 maps graph.
         */
        coreOptions.put("useDiagonalEdges", true);

        /**
         * Show Floor on/off.
         */
        coreOptions.put("Floor", true);

        //Input options
        /**
         * Toggle On/Off HOG2Maps usage.
         */
        coreOptions.put("HOG2Maps", false);
        
         /**
         * Toggle On/Off HOG2Maps usage.
         */
        coreOptions.put("HOG2MapsDebug", false);

        /**
         * Toggle On/Off swamp cells as obstacles.
         */
        coreOptions.put("HOG2MapsWithScells", true);

        /**
         * Toggle On/Off mouse usage.
         */
        coreOptions.put("useMouse", false);

        /**
         * Toggle On/Off mouse debug usage.
         */
        coreOptions.put("useMouseDebug", false);

        /**
         * Toggle On/Off mouse cursor usage.
         */
        coreOptions.put("useMouseCursor", false);

        /**
         * Toggle On/Off mouse off screen shift in last on screen recorded
         * direction.
         */
        coreOptions.put("useMouseOffScreenPositionShift", false);

        /**
         * Toggle On/Off keyboard usage.
         */
        coreOptions.put("useKeyBoard", false);

        /**
         * Toggle On/Off keyboard debug usage.
         */
        coreOptions.put("useKeyBoardDebug", false);

        /**
         * Toggle On/Off sound debug usage.
         */
        coreOptions.put("useSoundDebug", false);

        //GUI options
        /**
         * Toggle On/Off full screen usage.
         */
        coreOptions.put("useFullScreen", false);

        /**
         * Toggle On/Off path finders where the cost of going from a node to is
         * neighbor is SQRT(2) if a diagonal 1 otherwise.
         */
        coreOptions.put("useUniformRegularGridCosts", false);
    }

    /**
     * Displays in terminal all the core options available and their values.
     */
    public static void showCoreOptions() {
        log.setLevel(INFO);
        log.info("All available core options and values:");

        coreOptions.keySet().stream().forEach(
                key -> log.info(format(key + " = " + coreOptions.get(key))));

        log.info("");
        log.setLevel(OFF);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private CoreOptions() {
    }
}
