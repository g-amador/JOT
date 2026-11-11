/*
 * This file is part of the JOT game engine geometry module.
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

/**
 * Class to set up the game constants.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class GameConstants {

    static final Logger log = getLogger(GameConstants.class.getName());

    /**
     * Method to set up the defaults for the game constants options.
     */
    public static void setGameConstants() {
        //TODO: If no options set defaults are used instead. 
        //Set the the defaults for the game constants options assuming all 
        //libraries are loaded as in this template, e.g.: 
        //Input devices options
        //InputDevicesOptions.useMouse = false;
        //Geometry options
        //GeometryOptions.showTextures = false;
        //Physics options
        //PhysicsOptions.doCollisionDetection = false;
        //AI options
        //AIOptions.useSteeringBehaviors = false;
        //Extras options
        //ExtrasOptions.useLights = false;

        //TODO: read options from file
    }

    /**
     * Default constructor
     */
    GameConstants() {
    }
}
