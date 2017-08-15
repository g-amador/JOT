/*
 * This file is part of the JOT game engine communication extension toolkit
 * component. 
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
package jot.communication;

import java.time.LocalDateTime;
import jot.util.GameObject;

/**
 * Interface that each generic network game object class, each corresponding to
 * a network game state, must implement.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public interface NetworkGameObject {

    /**
     * Get either Immutable object, mutable object, or player associated with
     * this NetworkGameObject sent time in the ISO-8601 calendar system.
     *
     * @return NetworkGameObject set time in the ISO-8601 calendar system.
     */
    LocalDateTime getDateTimeSent();

    /**
     * Get the GameObject associated with this NetworkGameObject.
     *
     * @return NetworkGameObject node Id8 identifier.
     */
    GameObject getGameObject();

    /**
     * Get the node Id8 identifier associated with this NetworkGameObject.
     *
     * @return NetworkGameObject node Id8 identifier.
     */
    Object getNodeId();

}
