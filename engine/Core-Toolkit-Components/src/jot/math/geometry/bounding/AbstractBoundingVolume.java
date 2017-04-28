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
package jot.math.geometry.bounding;

import com.jogamp.opengl.GL2;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.math.Distance.getDistance;
import jot.math.geometry.Node;
import jot.math.geometry.Renderable;
import jot.physics.Material;
import jot.physics.Ray;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Abstract class that all bounding volumes must extend.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractBoundingVolume implements Renderable, Node {

    private static final Logger log = getLogger("BV");

    static {
        log.setLevel(OFF);
    }

    /**
     * The type of this bounding volume.
     */
    public BoundingVolumeType boundingVolumeType;

    /**
     * This bounding volume current and initial minimum and maximum.
     */
    public Vector3D min, max, min0, max0;

    /**
     * The bounding volume identifier.
     */
    protected String Id;

    /**
     * The material of this bounding volume.
     */
    protected Material material;

    /**
     * The bounding volume display list.
     */
    protected int listBV;

    /**
     *
     * Get the material of this plane.
     *
     * @return this plane material.
     */
    public Material getMaterial() {
        return this.material;

    }

    /**
     * Set the material of this plane.
     *
     * @param material the material to set to this plane.
     */
    public void setMaterial(Material material) {
        this.material = material;
    }

    /**
     * Activate or deactivate render for this bounding volume object, by default
     * render bounding volume is set to false.
     *
     * @param renderBoundingVolume TRUE if this bounding volume object should be
     * render, FALSE otherwise.
     */
    @Override
    public void setRenderable(boolean renderBoundingVolume) {
        this.material.setRenderable(renderBoundingVolume);
    }

    /**
     * Calculate the barycentre between max and min.
     *
     * @return the barycentre between max and min.
     */
    public Vector3D barycentre() {
        return new Vector3D((this.max.getX() + this.min.getX()) / 2,
                (this.max.getY() + this.min.getY()) / 2,
                (this.max.getZ() + this.min.getZ()) / 2);
    }

    /**
     * Return the half the distance between min and max.
     *
     * @return half the distance between min and max.
     */
    public float halfDistance() {
        return (float) getDistance(this.barycentre(), this.max);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.Id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(String Id) {
        this.Id = Id;
    }

    /**
     * Dispose of bounding volume display list.
     */
    @Override
    public void dispose(GL2 gl) {
        if (gl != null) {
            log.info("Dispose listMesh.");
            gl.glDeleteLists(this.listBV, 1);
        }
    }

    /**
     * See if render for this bounding volume object, is activated or
     * deactivated.
     *
     * @return TRUE if this bounding volume object should be render, FALSE
     * otherwise.
     */
    @Override
    public boolean isRenderable() {
        return this.material.isRenderable();
    }

    /**
     * Test if two provided bounding volumes collide.
     *
     * @param otherBoundingVolume other transformGroup in the scene bounding
     * volume.
     * @return TRUE if the two provided bounding volumes collide, FALSE
     * otherwise.
     */
    public abstract boolean isCollide(AbstractBoundingVolume otherBoundingVolume);

    /**
     * Test if two bounding volumes collide knowing that one is a sphere,
     * assuming this object.
     *
     * @param otherBoundingVolume one of the bounding volumes.
     * @param boundingSphere one of the bounding volumes.
     * @return TRUE if the two provided bounding volumes collide, FALSE
     * otherwise.
     */
    protected boolean isCollideSPHERE(AbstractBoundingVolume otherBoundingVolume, AbstractBoundingVolume boundingSphere) {
        Vector3D v1 = new Vector3D(otherBoundingVolume.min.toArray());
        Vector3D v2 = new Vector3D(otherBoundingVolume.min.getX(), otherBoundingVolume.max.getY(), otherBoundingVolume.min.getZ());
        Vector3D v3 = new Vector3D(otherBoundingVolume.min.getX(), otherBoundingVolume.min.getY(), otherBoundingVolume.max.getZ());
        Vector3D v4 = new Vector3D(otherBoundingVolume.min.getX(), otherBoundingVolume.max.getY(), otherBoundingVolume.max.getZ());
        Vector3D v5 = new Vector3D(otherBoundingVolume.max.getX(), otherBoundingVolume.min.getY(), otherBoundingVolume.min.getZ());
        Vector3D v6 = new Vector3D(otherBoundingVolume.max.getX(), otherBoundingVolume.min.getY(), otherBoundingVolume.max.getZ());
        Vector3D v7 = new Vector3D(otherBoundingVolume.max.getX(), otherBoundingVolume.max.getY(), otherBoundingVolume.min.getZ());
        Vector3D v8 = new Vector3D(otherBoundingVolume.max.toArray());

        Vector3D boundingSphereBarycentre = boundingSphere.barycentre();

        float distance0 = POSITIVE_INFINITY;
        float distance1 = (float) getDistance(boundingSphereBarycentre, v1);
        float distance2 = (float) getDistance(boundingSphereBarycentre, v2);
        float distance3 = (float) getDistance(boundingSphereBarycentre, v3);
        float distance4 = (float) getDistance(boundingSphereBarycentre, v4);
        float distance5 = (float) getDistance(boundingSphereBarycentre, v5);
        float distance6 = (float) getDistance(boundingSphereBarycentre, v6);
        float distance7 = (float) getDistance(boundingSphereBarycentre, v7);
        float distance8 = (float) getDistance(boundingSphereBarycentre, v8);

        distance0 = distance1 > distance0 ? distance0 : distance1;
        distance0 = distance2 > distance0 ? distance0 : distance2;
        distance0 = distance3 > distance0 ? distance0 : distance3;
        distance0 = distance4 > distance0 ? distance0 : distance4;
        distance0 = distance5 > distance0 ? distance0 : distance5;
        distance0 = distance6 > distance0 ? distance0 : distance6;
        distance0 = distance7 > distance0 ? distance0 : distance7;
        distance0 = distance8 > distance0 ? distance0 : distance8;

        return boundingSphere.halfDistance() > otherBoundingVolume.halfDistance()
                ? distance0 < boundingSphere.halfDistance()
                : distance0 < (boundingSphere.halfDistance() * 2);
    }

    /**
     * Test if ray intersects with this bounding volume, or null if none exist.
     *
     * @param ray to test if intersects this bounding volume.
     * @return intersection of a Ray with this bounding volume, or NULL if none
     * exist.
     */
    public abstract Vector3D getRayIntersection(Ray ray);

    /**
     * Check if a ray intersects with a bounding volume implementation.
     *
     * @param ray to test if intersects this bounding volume.
     * @return TRUE if ray intersects this bounding volume, FALSE otherwise.
     */
    public abstract boolean intersectsRay(Ray ray);

    /**
     * Types of bounding volumes.
     */
    public enum BoundingVolumeType {

        /**
         * SPHERE
         */
        SPHERE,
        /**
         * Axis Aligned Bounding Box (AABB)
         */
        AABB,
        /**
         * Object Aligned Bounding Box (OBB)
         */
        OBB,
    }
}
