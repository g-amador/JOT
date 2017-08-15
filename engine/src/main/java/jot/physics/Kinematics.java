/*
 * This file is part of the JOT game engine physics extension toolkit component.
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
package jot.physics;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements the movement Kinematics.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Kinematics {

    static final Logger log = getLogger("Kinematics");

    static {
        log.setLevel(OFF);
    }

    /**
     * Update a position in Cartesian coordinates.
     *
     * @param position current position of an avatar.
     * @param velocity of an avatar.
     * @param dt the physics update time step.
     * @return the updated position in polar coordinates Cartesian coordinates.
     */
    public static Vector3D translate(
            Vector3D position,
            Vector3D velocity,
            float dt) {
        //float x = (float) (position.getX() + velocity.getX() * dt);
        //float y = (float) (position.getY() + velocity.getY() * dt);
        //float z = (float) (position.getZ() + velocity.getZ() * dt);
        //return new Vector3D(x, y, z);
        return position.add(velocity.scalarMultiply(dt));
    }

    /**
     * Update a position in Cartesian coordinates.
     *
     * @param position current position of an avatar.
     * @param velocity of an avatar.
     * @param dt the physics update time step.
     * @return the updated position in Cartesian coordinates.
     */
    public static Vector3D translate(
            Vector3D position,
            float velocity,
            float dt) {
        //float x = (float) (position.getX() + velocity * dt);
        //float y = (float) (position.getY() + velocity * dt);
        //float z = (float) (position.getZ() + velocity * dt);
        //return new Vector3D(x, y, z);
        return position.add(
                new Vector3D(velocity * dt, velocity * dt, velocity * dt));
    }

    /**
     * Update a position in Cartesian coordinates using a ballistic trajectory
     * of a projectile.
     *
     * @param pastPosition of an mutable object.
     * @param presentPosition of an mutable object.
     * @param velocity of an mutable object.
     * @param gravity the value of the gravity attraction.
     * @param dt time passed since projectile started moving.
     * @return the updated position in Cartesian coordinates.
     */
    public static Vector3D translate(
            Vector3D pastPosition,
            Vector3D presentPosition,
            Vector3D velocity,
            float gravity,
            float dt) {
        //float x = (float) (presentPosition.getX() * velocity.getX());
        //float z = (float) (presentPosition.getZ() * velocity.getZ());
        float x = (float) (presentPosition.getX() + velocity.getX());
        float z = (float) (presentPosition.getZ() + velocity.getX());

        //Update y
        float y = (float) (pastPosition.getY()
                + velocity.getY() * dt + 0.5f * gravity * pow(dt, 2)
                - presentPosition.getY());

        return new Vector3D(x, y, z);
    }

    /**
     * Update a position in Cartesian coordinates using a ballistic trajectory
     * of a projectile.
     *
     * @param pastPosition of an mutable object.
     * @param presentPosition of an mutable object.
     * @param velocity of an mutable object.
     * @param gravity the value of the gravity attraction.
     * @param dt time passed since projectile started moving.
     * @return the updated position in Cartesian coordinates.
     */
    public static Vector3D translate(
            Vector3D pastPosition,
            Vector3D presentPosition,
            float velocity,
            float gravity,
            float dt) {
        //float x = (float) (presentPosition.getX() * velocity);
        //float z = (float) (presentPosition.getZ() * velocity);
        float x = (float) (presentPosition.getX() + velocity);
        float z = (float) (presentPosition.getZ() + velocity);

        //Update y
        float y = (float) (pastPosition.getY()
                + velocity * dt + 0.5f * gravity * pow(dt, 2)
                - presentPosition.getY());

        return new Vector3D(x, y, z);
    }

    /**
     * Update a position in polar coordinates.
     *
     * @param position current position of an avatar.
     * @param velocity of an avatar.
     * @param rotation of an avatar in degrees.
     * @param direction in which the avatar is moving in degrees.
     * @param dt the physics update time step.
     * @return the updated position in polar coordinates
     */
    public static Vector3D translatePolar(
            Vector3D position,
            Vector3D velocity,
            float rotation,
            float direction,
            float dt) {
        float x = (float) (position.getX()
                + sin(toRadians(rotation + direction)) * velocity.getX() * dt);
        float y = (float) position.getY();
        float z = (float) (position.getZ()
                + cos(toRadians(rotation + direction)) * velocity.getZ() * dt);
        return new Vector3D(x, y, z);
    }

    /**
     * Update a position in polar coordinates.
     *
     * @param position current position of an avatar.
     * @param velocity of an avatar.
     * @param rotation of an avatar in degrees.
     * @param direction in which the avatar is moving in degrees.
     * @param dt the physics update time step.
     * @return the updated position in polar coordinates
     */
    public static Vector3D translatePolar(
            Vector3D position,
            float velocity,
            float rotation,
            float direction,
            float dt) {
        float x = (float) (position.getX()
                + sin(toRadians(rotation + direction)) * velocity * dt);
        float y = (float) position.getY();
        float z = (float) (position.getZ()
                + cos(toRadians(rotation + direction)) * velocity * dt);
        return new Vector3D(x, y, z);
    }

    /**
     * Update a position in polar coordinates using a ballistic trajectory of a
     * projectile.
     *
     * @param pastPosition of an mutable object.
     * @param presentPosition of an mutable object.
     * @param velocity of an mutable object.
     * @param rotation of an mutable object in degrees.
     * @param direction in which the mutable object is moving in degrees.
     * @param gravity the value of the gravity attraction.
     * @param dt time passed since projectile started moving.
     * @return the updated position in polar coordinates
     */
    public static Vector3D translatePolar(
            Vector3D pastPosition,
            Vector3D presentPosition,
            Vector3D velocity,
            float rotation,
            float direction,
            float gravity,
            float dt) {
        float x = (float) (presentPosition.getX()
                + sin(toRadians(rotation + direction)) * velocity.getX());
        float z = (float) (presentPosition.getZ()
                + cos(toRadians(rotation + direction)) * velocity.getZ());

        //Update y
        float y = (float) (pastPosition.getY()
                + velocity.getY() * dt + 0.5f * gravity * pow(dt, 2)
                - presentPosition.getY());

        return new Vector3D(x, y, z);
    }

    /**
     * Update a position in polar coordinates using a ballistic trajectory of a
     * projectile.
     *
     * @param pastPosition of an mutable object.
     * @param presentPosition of an mutable object.
     * @param velocity of an mutable object.
     * @param rotation of an mutable object in degrees.
     * @param direction in which the mutable object is moving in degrees.
     * @param gravity the value of the gravity attraction.
     * @param dt time passed since projectile started moving.
     * @return the updated position in polar coordinates
     */
    public static Vector3D translatePolar(
            Vector3D pastPosition,
            Vector3D presentPosition,
            float velocity,
            float rotation,
            float direction,
            float gravity,
            float dt) {
        float x = (float) (presentPosition.getX()
                + sin(toRadians(rotation + direction)) * velocity);
        float z = (float) (presentPosition.getZ()
                + cos(toRadians(rotation + direction)) * velocity);

        //Update y
        float y = (float) (pastPosition.getY()
                + velocity * dt + 0.5f * gravity * pow(dt, 2)
                - presentPosition.getY());

        return new Vector3D(x, y, z);
    }

    /**
     * Update a position in spherical coordinates.
     *
     * @param position current position of an avatar.
     * @param velocity of an avatar.
     * @param rotationTheta of an avatar in degrees.
     * @param rotationPhi of an avatar in degrees.
     * @param directionTheta in which the avatar is moving in degrees.
     * @param directionPhi in which the avatar is moving in degrees.
     * @param dt the physics update time step.
     * @return the updated position in spherical coordinates
     */
    public static Vector3D translateSpherical(
            Vector3D position,
            Vector3D velocity,
            float rotationTheta,
            float rotationPhi,
            float directionTheta,
            float directionPhi,
            float dt) {
        float x = (float) (position.getX()
                + sin(toRadians(rotationTheta + directionTheta))
                * sin(toRadians(rotationPhi + directionPhi))
                * velocity.getX() * dt);
        float y = (float) (position.getY()
                + cos(toRadians(rotationTheta + directionTheta))
                * velocity.getX() * dt);
        float z = (float) (position.getZ()
                + sin(toRadians(rotationTheta + directionTheta))
                * cos(toRadians(rotationPhi + directionPhi))
                * velocity.getZ() * dt);
        return new Vector3D(x, y, z);
    }

    /**
     * Update a position in spherical coordinates.
     *
     * @param position current position of an avatar.
     * @param velocity of an avatar.
     * @param rotationTheta of an avatar in degrees.
     * @param rotationPhi of an avatar in degrees.
     * @param directionTheta in which the avatar is moving in degrees.
     * @param directionPhi in which the avatar is moving in degrees.
     * @param dt the physics update time step.
     * @return the updated position in spherical coordinates
     */
    public static Vector3D translateSpherical(
            Vector3D position,
            float velocity,
            float rotationTheta,
            float rotationPhi,
            float directionTheta,
            float directionPhi,
            float dt) {
        float x = (float) (position.getX()
                + sin(toRadians(rotationTheta + directionTheta))
                * sin(toRadians(rotationPhi + directionPhi))
                * velocity * dt);
        float y = (float) (position.getY()
                + cos(toRadians(rotationTheta + directionTheta))
                * velocity * dt);
        float z = (float) (position.getZ()
                + sin(toRadians(rotationTheta + directionTheta))
                * cos(toRadians(rotationPhi + directionPhi))
                * velocity * dt);
        return new Vector3D(x, y, z);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private Kinematics() {
    }
}
