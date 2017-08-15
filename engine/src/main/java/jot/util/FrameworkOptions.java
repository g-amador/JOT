/*
 * This file is part of the JOT game engine utility framework toolkit component.
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
import java.util.Set;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.util.CoreOptions.coreOptions;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import static jot.util.ExtensionGeometryOptions.extensionGeometryOptions;
import static jot.util.ExtensionPhysicsOptions.extensionPhysicsOptions;

/**
 * Class that sets the default extras options.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class FrameworkOptions {

    static final Logger log = getLogger("FrameworkOptions");

    public static final Options frameworkOptions = new Options();
    //public static final OptionsdHashMap<String, Boolean> frameworkOptions = new OptionsdHashMap<>();

    static {
        log.setLevel(OFF);

        //Render options
        /**
         * Lights usage on/off.
         */
        frameworkOptions.put("useLights", true);

        /**
         * Use RayTracer On/Off
         */
        frameworkOptions.put("useRayTracer", false);

        /**
         * Use unreachable sky on/off.
         */
        frameworkOptions.put("useUnreachableSky", false);

        /**
         * Use unreachable floor on/off.
         */
        frameworkOptions.put("useUnreachableFloor", false);

        /**
         * SkyDome usage on/off.
         */
        frameworkOptions.put("SkyDome", false);

        /**
         * SkyBox usage on/off.
         */
        frameworkOptions.put("SkyBox", false);

        /**
         * Show all geometries in the scene not including the floor on/off.
         */
        frameworkOptions.put("showGeometries", true);

        /**
         * Planar shadows usage on/off.
         */
        frameworkOptions.put("showPlanarShadows", false);

        /**
         * Shadow maps usage on/off.
         */
        frameworkOptions.put("showShadowMaps", false);

        /**
         * Show Z-buffer on/off.
         */
        frameworkOptions.put("showZBuffer", false);

        /**
         * Render path finders graph.
         */
        frameworkOptions.put("showGraph", false);

        /**
         * Render path finders visited list.
         */
        frameworkOptions.put("showVisited", false);

        /**
         * Toggle A.I. on/off
         */
        frameworkOptions.put("useAI", false);

        /**
         * Toggle showing "Path not found." in the terminal, when path not found
         * by a pathfinder, on/off
         */
        frameworkOptions.put("usePathFindersReconstructPathDebug", false);

        /**
         * use removing the middle node of the last found path from the graph
         * on/off.
         */
        frameworkOptions.put("usePathFindersAdaptivityTest", false);

        /**
         * use removing the middle node neighbors of the last found path from
         * the graph on/off.
         */
        frameworkOptions.put("usePathFindersRemoveNeighborsAdaptivityTest", false);

        /**
         * Show Sky regardless of the camera used.
         */
        frameworkOptions.put("testMode", false);

        /**
         * use framework options as a global options hashMap.
         */
        try {
            frameworkOptions.putAll(coreOptions);
            frameworkOptions.putAll(extensionGeometryOptions);
            frameworkOptions.putAll(extensionPhysicsOptions);
            frameworkOptions.putAll(extensionAIOptions);
        } catch (NoClassDefFoundError ex) {
            log.severe(ex.getMessage());
        }
    }

    /**
     * Displays in terminal all the framework options available and their
     * values.
     */
    public static void showFrameworkOptions() {
        log.setLevel(INFO);
        log.info("All available framework options values:");

        frameworkOptions.keySet().stream().forEach(
                key -> log.info(format(key + " = " + frameworkOptions.get(key))));

        log.info("");
        log.setLevel(OFF);
    }

    /**
     * Default constructor.
     */
    FrameworkOptions() {
    }

    public static class Options {

        HashMap<String, Boolean> options;

        public Options() {
            this.options = new HashMap<>();
        }

        public boolean get(String key) {
            return this.options.get(key);
        }

        public HashMap<String, Boolean> getOptions() {
            return this.options;
        }

        public void setOptions(HashMap<String, Boolean> options) {
            options.entrySet().stream().forEach(e -> {
                try {
                    if (coreOptions.containsKey(e.getKey())) {
                        coreOptions.put(e.getKey(), e.getValue());
                    }
                    if (extensionGeometryOptions.containsKey(e.getKey())) {
                        extensionGeometryOptions.put(e.getKey(), e.getValue());
                    }
                    if (extensionPhysicsOptions.containsKey(e.getKey())) {
                        extensionPhysicsOptions.put(e.getKey(), e.getValue());
                    }
                    if (extensionAIOptions.containsKey(e.getKey())) {
                        extensionAIOptions.put(e.getKey(), e.getValue());
                    }
                } catch (NoClassDefFoundError ex) {
                    log.severe(ex.getMessage());
                }
            });
        }

        public Set<String> keySet() {
            return this.options.keySet();
        }

        public void put(String key, boolean value) {
            try {
                if (coreOptions.containsKey(key)) {
                    coreOptions.put(key, value);
                }
                if (extensionGeometryOptions.containsKey(key)) {
                    extensionGeometryOptions.put(key, value);
                }
                if (extensionPhysicsOptions.containsKey(key)) {
                    extensionPhysicsOptions.put(key, value);
                }
                if (extensionAIOptions.containsKey(key)) {
                    extensionAIOptions.put(key, value);
                }
            } catch (NoClassDefFoundError ex) {
                log.severe(ex.getMessage());
            }
            this.options.put(key, value);
        }

        public void putAll(HashMap<String, Boolean> options) {
            this.options.putAll(options);
        }
    }
}
