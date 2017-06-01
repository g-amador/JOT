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

import java.util.HashMap;
import java.util.List;
import jot.math.geometry.TransformGroup;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Abstract class that each generic game object class, each corresponding to a
 * game state, must extend.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class CoreGameObject extends TransformGroup {

    /**
     * Constructor.
     *
     * @param Id the unique identifier for this game object.
     */
    public CoreGameObject(String Id) {
        super(Id);
    }

    /**
     * Constructor.
     *
     * @param Id the unique identifier for this game object.
     * @param tg another transformGroup.
     */
    public CoreGameObject(String Id, TransformGroup tg) {
        super(Id, tg);
    }

    /**
     * Get either Immutable object, mutable object, or player associated with
     * this game object creation time.
     *
     * @return game object t0.
     */
    public abstract long getT0();

    /**
     * Get the behaviors set to true of this game object, e.g., moving,
     * attacking, etc.
     *
     * @return a list with this game object active behaviors.
     */
    public abstract List<String> getActiveBehaviours();

    /**
     * Set the value of a specific behavior of this game object, e.g., moving,
     * attacking, etc.
     *
     * @param behaviorId to set from this game object.
     * @param value to set to a specific behavior, e.g., attacking to
     * TRUE/FALSE.
     */
    public abstract void setBehaviour(String behaviorId, boolean value);

    /**
     * Get either Immutable object, mutable object, or player associated with
     * this game object property/attribute given its identifier.
     *
     * @param attributeId of the attribute value to get.
     * @return attribute value from a given identifier, e.g., lives, health,
     * mana.
     */
    public abstract long getAttribute(String attributeId);

    /**
     * Set either Immutable object, mutable object, or player associated with
     * this game object property/attribute given its identifier.
     *
     * @param attributeId of the attribute value to set.
     * @param attributeValue to set to the provided attributeId.
     */
    public abstract void setAttribute(String attributeId, long attributeValue);

    /**
     * Get either Immutable object, mutable object, or player associated with
     * this game object properties/attributes.
     *
     * @return properties/attributes from this game object.
     */
    public abstract HashMap<String, Long> getAttributes();

    /**
     * Set either Immutable object, mutable object, or player associated with
     * this game object properties/attributes.
     *
     * @param attributes to set to this game object.
     */
    public abstract void setAttributes(HashMap<String, Long> attributes);

    /**
     * Get the bounding radius of interaction of this game object with other
     * game objects.
     *
     * @return the bounding radius of interaction of this game object with other
     * games objects.
     */
    public abstract float getBoundingRadius();

    /**
     * Set the bounding radius of interaction of this game object with other
     * games objects.
     *
     * @param radius of interaction of this game object with other game objects.
     */
    public abstract void setBoundingRadius(float radius);

    /**
     * Get the maximum speed at which this game object can go.
     *
     * @return the maximum speed at which this game object can go.
     */
    public abstract float getMaxSpeed();

    /**
     * Set the maximum speed at which this game object can go.
     *
     * @param maxSpeed at which this game object can go.
     */
    public abstract void setMaxSpeed(float maxSpeed);

    /**
     * Get the current equipment items/set of this game object, e.g., gun for a
     * FPS or gun, armor, shield, etc. for a RPG.
     *
     * @param equipmentId of the equipment.
     * @return this game object current equipment items/set.
     */
    public abstract String getEquipmentItem(String equipmentId);

    /**
     * Set a specific equipment item/set of this game object, e.g., gun for a
     * FPS or gun, armor, shield, etc. for a RPG.
     *
     * @param equipmentId of the equipment.
     * @param equipmentItem to set to this game object.
     */
    public abstract void setEquipmentItem(String equipmentId, String equipmentItem);

    /**
     * Get the current equipment items/set of this game object, e.g., gun for a
     * FPS or gun, armor, shield, etc. for a RPG.
     *
     * @return this game object current equipment items/set.
     */
    public abstract HashMap<String, String> getEquipmentItems();

    /**
     * Set the equipment items/set of this game object, e.g., gun for a FPS or
     * gun, armor, shield, etc. for a RPG.
     *
     * @param equipmentItems to set to this game object.
     */
    public abstract void setEquipmentItems(HashMap<String, String> equipmentItems);

    /**
     * Get this game object previous position.
     *
     * @return this game object previous position.
     */
    public abstract Vector3D getPastPosition();

    /**
     * Set this game object previous position.
     *
     * @param position this game object previous position.
     */
    public abstract void setPastPosition(Vector3D position);

    /**
     * Get this game object position.
     *
     * @return this game object position.
     */
    public abstract Vector3D getPosition();

    /**
     * Set this game object position.
     *
     * @param position this game object position.
     */
    public abstract void setPosition(Vector3D position);

    /**
     * Get this game object velocity.
     *
     * @return this game object velocity.
     */
    public abstract Vector3D getVelocity();

    /**
     * Set this game object velocity.
     *
     * @param velocity this game object velocity.
     */
    public abstract void setVelocity(Vector3D velocity);

    /**
     * Update this game object position.
     *
     * @param update this game object position variation to add.
     */
    public abstract void updatePosition(Vector3D update);

    /**
     * Update this game object velocity.
     *
     * @param update this game object velocity variation to add.
     */
    public abstract void updateVelocity(Vector3D update);

    @Override
    public CoreGameObject clone() throws CloneNotSupportedException {
        super.clone();

        return (CoreGameObject) super.clone();
    }
}
