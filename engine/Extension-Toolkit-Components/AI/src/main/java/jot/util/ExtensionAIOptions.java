/*
 * This file is part of the JOT game engine A.I. extension toolkit component. Copyright (C) 2014
 * Gonçalo Amador & Abel Gomes
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
 * Class that sets the default A.I. options.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class ExtensionAIOptions {

    static final Logger log = getLogger("ExtensionAIOptions");

    public static final HashMap<String, Boolean> extensionAIOptions = new HashMap<>();

    static {
        log.setLevel(OFF);

        //A.I. options
        /**
         * Render path found by either pathfinder.
         */
        extensionAIOptions.put("showPath", true);

        /**
         * Toggle on/off steering behaviors.
         */
        extensionAIOptions.put("useSteeringBehaviors", false);

        /**
         * Toggle on/off steering behaviors debug.
         */
        extensionAIOptions.put("useSteeringBehaviorsDebug", false);

        /**
         * Toggle on/off path follow, i.e., if off the A.I. controlled agent
         * does not translate.
         */
        extensionAIOptions.put("useSteeringBehaviorsPathFollow", false);

        /**
         * Toggle on/off cyclical path follow, i.e., when at end of path go to
         * first path position and repeat path follow
         */
        extensionAIOptions.put("useSteeringBehaviorsLoopPathFollow", false);

        /**
         * Toggle on/off path finders.
         */
        extensionAIOptions.put("usePathFinders", false);

        /**
         * Toggle on/off path finders debug.
         */
        extensionAIOptions.put("usePathFindersDebug", false);

        /**
         * Toggle on/off path not found by timeout in path finders.
         */
        extensionAIOptions.put("usePathFindersTimeout", false);

        /**
         * Toggle on/off path finders reseting visited nodes from the last
         * search.
         */
        extensionAIOptions.put("usePathFindersResetVisited", true);

        /**
         * Toggle on/off path finders with a low level (e.g., visibility graph).
         */
        extensionAIOptions.put("usePathFindersLowLevelGraph", false);
    }

    /**
     * Displays in terminal all the extension A.I. options available and their
     * values.
     */
    public static void showExtensionAIOptions() {
        log.setLevel(INFO);
        log.info("All available extension A.I. options values:");

        extensionAIOptions.keySet().stream().forEach(key
                -> log.info(format(key + " = " + extensionAIOptions.get(key))));

        log.info("");
        log.setLevel(OFF);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private ExtensionAIOptions() {
    }
}
