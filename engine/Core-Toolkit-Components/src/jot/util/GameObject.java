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

import static java.lang.System.nanoTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.math.geometry.TransformGroup;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that specifies the generic FPS game object, each corresponding to a FPS
 * game state.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class GameObject extends CoreGameObject {

    /**
     * Class debug logger
     */
    static final Logger log = getLogger("GameObject");

    static {
        log.setLevel(OFF);
    }

    private final long t0;
    private float boundingRadius;
    private float maxSpeed;

    private Vector3D velocity;      //Velocity.
    private Vector3D position;      //PastPosition in Cartesian coordinates.
    private Vector3D pastPosition;  //Previous Position in Cartesian coordinates.

    //The attributes of this game object, e.g., for a RPG: health of the game
    //object; level of the game object; available mana of the game object.
    private HashMap<String, Long> attributes;

    //The equipment of this game object, a game object equipment is unique
    //depending on the type of game, e.g., for a FPS it might be the gun, for a
    //RPG a set of items.       
    private HashMap<String, String> equipmentItems;

    //The possible behaviors for this game object, depending on the type of
    //game, e.g., for a FPS: attacking an enemy; if interacting with team
    //players (e.g., healing, trading) or using items; walking; running;
    //stooped.
    private final HashMap<String, Boolean> behaviors;

    /**
     * Constructor.
     *
     * @param Id the unique identifier for this game object.
     */
    public GameObject(String Id) {
        super(Id);

        this.t0 = nanoTime();
        this.velocity = ZERO;
        this.position = this.translation;
        this.pastPosition = this.pastTranslation;
        this.attributes = new HashMap<>();
        this.behaviors = new HashMap<>();
        this.equipmentItems = new HashMap<>();
        this.maxSpeed = (float) this.velocity.getNorm();
        this.boundingRadius = super.getBoundingVolume(0).halfDistance();
    }

    /**
     * Constructor.
     *
     * @param Id the unique identifier for this game object.
     * @param tg another transformGroup.
     */
    public GameObject(String Id, TransformGroup tg) {
        super(Id, tg);

        this.t0 = nanoTime();
        this.velocity = ZERO;
        this.position = this.translation;
        this.pastPosition = this.pastTranslation;
        this.attributes = new HashMap<>();
        this.behaviors = new HashMap<>();
        this.equipmentItems = new HashMap<>();
        this.maxSpeed = (float) this.velocity.getNorm();
        this.boundingRadius = super.getBoundingVolume(0).halfDistance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getT0() {
        return this.t0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getActiveBehaviours() {
        List<String> activeBehaviours = new ArrayList<>();
        this.behaviors.keySet().stream()
                .filter(key -> this.behaviors.get(key))
                .forEach(key -> activeBehaviours.add(key));

        return activeBehaviours;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBehaviour(String behaviorId, boolean value) {
        this.behaviors.put(behaviorId.toLowerCase(), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getAttribute(String attributeId) {
        return this.attributes.get(attributeId.toLowerCase());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String attributeId, long attributeValue) {
        this.attributes.put(attributeId.toLowerCase(), attributeValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<String, Long> getAttributes() {
        return this.attributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(HashMap<String, Long> attributes) {
        this.attributes = attributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getBoundingRadius() {
        return this.boundingRadius;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBoundingRadius(float radius) {
        this.boundingRadius = radius;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEquipmentItem(String equipmentId) {
        return this.equipmentItems.get(equipmentId.toLowerCase());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEquipmentItem(String equipmentId, String equipmentItem) {
        this.equipmentItems.put(equipmentId.toLowerCase(), equipmentItem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<String, String> getEquipmentItems() {
        return this.equipmentItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEquipmentItems(HashMap<String, String> equipmentItems) {
        this.equipmentItems = equipmentItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getMaxSpeed() {
        return this.maxSpeed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getPastPosition() {
        return this.pastPosition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPastPosition(Vector3D pastPosition) {
        super.setPastTranslation(pastPosition);
        this.pastPosition = pastPosition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getPosition() {
        return this.position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPosition(Vector3D position) {
        super.setTranslation(position);
        this.position = position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getVelocity() {
        return this.velocity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVelocity(Vector3D velocity) {
        this.velocity = velocity;
    }

    @Override
    public void updatePosition(Vector3D update) {
        super.updateTranslation(update);
        this.position = this.translation;
        this.pastPosition = this.pastTranslation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVelocity(Vector3D update) {
        this.velocity = this.velocity.add(update);
    }

    @Override
    public GameObject clone() throws CloneNotSupportedException {
        super.clone();

        this.velocity = ZERO;

        return (GameObject) super.clone();
    }
}
