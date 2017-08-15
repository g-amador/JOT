/*
 * This file is part of the JOT game engine geometry extension toolkit 
 * component.
 * Copyright(C) 2014 Gonçalo Amador & Abel Gomes
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
 * Class that sets the default geometry options.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class ExtensionGeometryOptions {

    static final Logger log = getLogger("ExtensionGeometryOptions");

    public static final HashMap<String, Boolean> extensionGeometryOptions = new HashMap<>();

    static {
        log.setLevel(OFF);

        //Geometry generators options
        /**
         * Use terrain generators on/off.
         */
        extensionGeometryOptions.put("useTerrainGenerators", false);

        /**
         * Use terrain where the all heights bellow middle between maximum and
         * minimum are set to middle on/off.
         */
        extensionGeometryOptions.put("useTerrainGeneratorsWithMiddleMinimumHeight", false);

        /**
         * Use maze generators on/off.
         */
        extensionGeometryOptions.put("useMazeGenerators", false);

        /**
         * Use maze generators debug on/off.
         */
        extensionGeometryOptions.put("useMazeGeneratorsDebug", false);
    }

    /**
     * Displays in terminal all the extension geometry options available and
     * their values.
     */
    public static void showExtensionGeometryOptions() {
        log.setLevel(INFO);
        log.info("All available extension geometry options values:");

        extensionGeometryOptions.keySet().stream().forEach(
                key -> log.info(format(key + " = " + extensionGeometryOptions.get(key))));

        log.info("");
        log.setLevel(OFF);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private ExtensionGeometryOptions() {
    }
}
