/**
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
 * E-mail Contacts: gamador@it.ubi.pt & A. Gomes (agomes@di.ubi.pt)
 */
package util;

import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * Class to set up the game constants.
 *
 * @author G. Amador & A. Gomes
 */
public class GameConstants {

    private static final Logger log = getLogger(GameConstants.class.getName());

    /**
     * Method to set up the defaults for the game constants options.
     */
    public static void setGameConstants() {
        //Input devices options
        InputDevicesOptions.useMouse = true;
        InputDevicesOptions.useMouseCursor = false;
        InputDevicesOptions.useKeyBoard = true;

        //Geometry options
        GeometryOptions.showTextures = true;
        GeometryOptions.showWireframe = false;

        //Geometry Scene options
        GeometryOptions.Floor = false;
        GeometryOptions.SkyDome = false;
        GeometryOptions.SkyBox = false;
        GeometryOptions.addSkyBoxCeil = true;
        GeometryOptions.addSkyBoxWalls = true;

        //Geometry generators options
        GeometryOptions.useMazeGenerators = false;

        //Input HOG2Map files options
        GeometryOptions.HOG2Maps = false;

        //Physics options
        PhysicsOptions.doCollisionDetection = false;
        PhysicsOptions.doNarrowPhaseCollisionDetection = false;

        //AI options
        AIOptions.useSteeringBehaviors = true;
        AIOptions.usePathFinders = false;
        AIOptions.usePathFindersDebug = false;
        AIOptions.usePathFindersTimeout = false;
        AIOptions.usePathFindersGraphHashMap = false;
        AIOptions.usePathFindersPotentialFields = false;

        //Extras options
        ExtrasOptions.useLights = true;

        //TODO: read from file        
    }

    /**
     * Default constructor.
     */
    GameConstants() {
    }
}
