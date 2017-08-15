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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Interface that defines the core methods to implement a networking module that
 * runs over a communication grid.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
//TODO: if a node leaves or crashes and is not removed from the players hastTable lag may appear that will penalize all existing nodes. try to use events to fix this.
//TODO: reboot players that have less than a certain minimum X value of UPS.
public interface Communicator {

    /**
     * Initialize the zone coordinates for each value of the array baryCenters
     * with dimensions zonesLength and zonesWidth (number of tiles in z and x).
     */
    void setZonesBaryCenters();

    /**
     * Given a point p1 obtain the closest baryCenter.
     *
     * @param position position of an player or a baryCenter value
     * @return the baryCenter which is closer to p1
     */
    Vector3D getClosestZoneBaryCenter(Vector3D position);

    /**
     * Get the zones length.
     *
     * @return the number of zones in x.
     */
    int getZonesLength();

    /**
     * Get the zones width.
     *
     * @return the number of zones in y.
     */
    int getZonesWidth();

    /**
     * Test if a player given its past and current positions has exited a zone
     * or not.
     *
     * @param pastPosition the past position of a given player.
     * @param presentPosition the present position of a given player.
     * @return TRUE if player exited a zone, FALSE otherwise.
     */
    //TODO: place in state management
    boolean exitedZone(Vector3D pastPosition, Vector3D presentPosition);

    /**
     * Given a player A current position (point p) and a game update with the
     * position of a remote player B, if B is outside A AOI then if in A scene B
     * should be removed.
     *
     * @param position a local player current position.
     * @param gameUpdate a remote player game update.
     * @return TRUE if player should be removed from scene, FALSE otherwise.
     */
    boolean toRemove(Vector3D position, GameUpdate gameUpdate);

    /**
     * Setup grid node.
     *
     * @param gridConfig location of .xml with the grid configuration to use.
     */
    void startGridNode(String gridConfig);

    /**
     * Stop this grid node and remove event listeners.
     */
    void stopGridNode();

    /**
     * Test if this node is the only one in the grid.
     *
     * @return TRUE if only one node in the grid, FALSE otherwise.
     */
    boolean isOnlyGridNode();

    /**
     * Verify if the method startGridNode successfully finished, i.e., if this
     * node is initialized and running.
     *
     * @return TRUE if node initialized and running, FALSE otherwise.
     */
    boolean isGridNodeStarted();

    /**
     * Verify if the method startGridNode successfully finished, i.e., if this
     * node is initialized and running, and if this in not the only available
     * grid node.
     *
     * @return TRUE if node initialized and running and the number of grid nodes
     * greater than 1 otherwise return FALSE.
     */
    boolean isGridRunning();

    /**
     * Get this grid node ID.
     *
     * @return a grid node ID.
     */
    Object getLocalNodeId();

    /**
     * Return the total number of playerNodes in the grid.
     *
     * @return the total number of playerNodes in the grid.
     */
    float getPlayerNodesCount();

    /**
     * Return the number of playerNodes perceived by this player node.
     *
     * @return the number of playerNodes perceived by this player node.
     */
    float getPerceivedPlayerNodesCount();

    /**
     * Send an GameUpdate to all players in players hash table if the local
     * player did not left the zone otherwise send a remove request to its
     * monitor, find the new closest monitor and set it as this player node
     * monitor, request the new monitor to send all is players data and their
     * direct neighbor monitors to do the same. Regardless if local player left
     * or not the zone, the final operation of this method is to send the
     * provided gameUpdate to the local player's monitor.
     *
     * @param gameUpdate provided local player gameUpdate.
     * @param exitedZone boolean that is either true if zone changed or false
     * otherwise.
     */
    void sendUpdate(GameUpdate gameUpdate, boolean exitedZone);

    /**
     * Send a game update to a provided destination node.
     *
     * @param gameUpdate GameUpdate to send.
     * @param destinationNodeId target node to GameUpdate, if NULL multicast to
     * all other grid nodes is sent instead.
     */
    void sendUpdate(GameUpdate gameUpdate, Object destinationNodeId);

    /**
     * Process all received gameUpdates.
     *
     * @param dt the amount of elapsed game time since the last frame.
     */
    void update(float dt);
}
