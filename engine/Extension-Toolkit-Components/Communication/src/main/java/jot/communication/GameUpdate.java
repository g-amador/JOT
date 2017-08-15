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

import java.io.Serializable;
import java.time.LocalDateTime;
import static java.time.LocalDateTime.now;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.util.GameObject;

/**
 * Class to specify the types and data contained in a game Update.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class GameUpdate implements Serializable, NetworkGameObject {

    static final Logger log = getLogger("GameUpdate");
    private static final long serialVersionUID = 1L;

    static {
        log.setLevel(OFF);
    }

    /**
     * Network node Id8 identifier.
     */
    private final Object nodeId;

    /**
     * The game object with updated data to transmit.
     */
    private final GameObject gameObject;

    /**
     * The time in the ISO-8601 calendar system in which this game update was
     * sent.
     */
    private final LocalDateTime dateTimeSent;

    /**
     * Constructor.
     *
     * @param Id the unique identifier for this gameObject transformGroup.
     * @param gameObject the gameObject update associate with this game update.
     */
    public GameUpdate(String Id, GameObject gameObject) {
        this.nodeId = Id;
        this.dateTimeSent = now();
        this.gameObject = gameObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime getDateTimeSent() {
        return this.dateTimeSent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GameObject getGameObject() {
        return this.gameObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getNodeId() {
        return this.nodeId;
    }
}
