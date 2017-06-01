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

import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import java.util.ArrayList;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.math.GJK.BodiesIntersect;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.AABB;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.OBB;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.SPHERE;
import jot.physics.Material;
import jot.physics.Ray;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements a Object Aligned Bounding Box (OBB).
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class OBB extends AbstractBoundingVolume {

    static final Logger log = getLogger("OBB");

    static {
        log.setLevel(OFF);
    }

    private int listOBB;

    /**
     * Constructor for a object aligned bounding volume.
     *
     * @param min coordinates in the Cartesian system of the bounding Volume
     * minimum.
     * @param max coordinates in the Cartesian system of the bounding Volume
     * maximum.
     */
    public OBB(Vector3D min, Vector3D max) {
        this.Id = "OBB";
        this.min = new Vector3D(min.toArray());
        this.max = new Vector3D(max.toArray());
        this.min0 = new Vector3D(min.toArray());
        this.max0 = new Vector3D(max.toArray());
        this.boundingVolumeType = OBB;
        this.material = new Material();
    }

    /**
     * Constructor for a object aligned bounding volume.
     *
     * @param boundingVolume to copy from the minimum and maximum coordinates in
     * the Cartesian system.
     */
    public OBB(AbstractBoundingVolume boundingVolume) {
        if (boundingVolume != null) {
            this.Id = boundingVolume.Id;
            this.min = new Vector3D(boundingVolume.min.toArray());
            this.max = new Vector3D(boundingVolume.max.toArray());
            this.min0 = new Vector3D(boundingVolume.min0.toArray());
            this.max0 = new Vector3D(boundingVolume.max0.toArray());
        } else {
            this.Id = "OBB";
            this.min = ZERO;
            this.max = ZERO;
            this.min0 = ZERO;
            this.max0 = ZERO;
        }
        this.boundingVolumeType = OBB;
        this.material = new Material();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getRayIntersection(Ray ray) {
        //TODO: implement for OBB   
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean intersectsRay(Ray ray) {
        //TODO: test if possible values!!!!!!
        float tmin = (float) ((this.min.getX() - ray.getOrigin().getX()) / ray.getDirection().getX());
        float tmax = (float) ((this.max.getX() - ray.getOrigin().getX()) / ray.getDirection().getX());
        if (tmin > tmax) {
            float aux = tmin;
            tmin = tmax;
            tmax = aux;
        }
        float tymin = (float) ((this.min.getY() - ray.getOrigin().getY()) / ray.getDirection().getY());
        float tymax = (float) ((this.max.getY() - ray.getOrigin().getY()) / ray.getDirection().getY());
        if (tymin > tymax) {
            float aux = tymin;
            tymin = tymax;
            tymax = aux;
        }
        if ((tmin > tymax) || (tymin > tmax)) {
            return false;
        }
        if (tymin > tmin) {
            tmin = tymin;
        }
        if (tymax < tmax) {
            tmax = tymax;
        }
        float tzmin = (float) ((this.min.getZ() - ray.getOrigin().getZ()) / ray.getDirection().getZ());
        float tzmax = (float) ((this.max.getZ() - ray.getOrigin().getZ()) / ray.getDirection().getZ());
        if (tzmin > tzmax) {
            float aux = tzmin;
            tzmin = tzmax;
            tzmax = aux;
        }
        if ((tmin > tzmax) || (tzmin > tmax)) {
            return false;
        }
        if (tzmin > tmin) {
            tmin = tzmin;
        }
        if (tzmax < tmax) {
            tmax = tzmax;
        }
        if ((tmin > ray.getTmax()) || (tmax < ray.getTmin())) {
            return false;
        }
        if (ray.getTmin() < tmin) {
            ray.setTmin(tmin);
        }
        if (ray.getTmax() > tmax) {
            ray.setTmax(tmax);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCollide(AbstractBoundingVolume otherBoundingVolume) {
        //OBB with OBB/AABB collision test, using GJK.       
        if (otherBoundingVolume.boundingVolumeType == OBB
                || otherBoundingVolume.boundingVolumeType == AABB) {
            ArrayList<Vector3D> simplex1 = new ArrayList<>();
            simplex1.add(new Vector3D(this.min.toArray()));
            simplex1.add(new Vector3D(this.min.getX(), this.max.getY(), this.min.getZ()));
            simplex1.add(new Vector3D(this.min.getX(), this.min.getY(), this.max.getZ()));
            simplex1.add(new Vector3D(this.min.getX(), this.max.getY(), this.max.getZ()));
            simplex1.add(new Vector3D(this.max.getX(), this.min.getY(), this.min.getZ()));
            simplex1.add(new Vector3D(this.max.getX(), this.min.getY(), this.max.getZ()));
            simplex1.add(new Vector3D(this.max.getX(), this.max.getY(), this.min.getZ()));
            simplex1.add(new Vector3D(this.max.toArray()));

            ArrayList<Vector3D> simplex2 = new ArrayList<>();
            simplex2.add(new Vector3D(otherBoundingVolume.min.toArray()));
            simplex2.add(new Vector3D(otherBoundingVolume.min.getX(), otherBoundingVolume.max.getY(), otherBoundingVolume.min.getZ()));
            simplex2.add(new Vector3D(otherBoundingVolume.min.getX(), otherBoundingVolume.min.getY(), otherBoundingVolume.max.getZ()));
            simplex2.add(new Vector3D(otherBoundingVolume.min.getX(), otherBoundingVolume.max.getY(), otherBoundingVolume.max.getZ()));
            simplex2.add(new Vector3D(otherBoundingVolume.max.getX(), otherBoundingVolume.min.getY(), otherBoundingVolume.min.getZ()));
            simplex2.add(new Vector3D(otherBoundingVolume.max.getX(), otherBoundingVolume.min.getY(), otherBoundingVolume.max.getZ()));
            simplex2.add(new Vector3D(otherBoundingVolume.max.getX(), otherBoundingVolume.max.getY(), otherBoundingVolume.min.getZ()));
            simplex2.add(new Vector3D(otherBoundingVolume.max.toArray()));

            return this != otherBoundingVolume && BodiesIntersect(simplex1, simplex2);
        }

        //OBB with SPHERE collision test.
        if (otherBoundingVolume.boundingVolumeType == SPHERE) {
            return this.isCollideSPHERE(this, otherBoundingVolume);
        }

        return false;
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
    public OBB getNode() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GL2 gl) {
        gl.glDeleteLists(this.listOBB, 1);
        this.listOBB = gl.glGenLists(1);
        gl.glNewList(this.listOBB, GL_COMPILE);
        {
            gl.glBegin(GL_LINES);
            {
                gl.glVertex3d(this.min0.getX(), this.min0.getY(), this.min0.getZ());
                gl.glVertex3d(this.max0.getX(), this.min0.getY(), this.min0.getZ());

                gl.glVertex3d(this.max0.getX(), this.min0.getY(), this.min0.getZ());
                gl.glVertex3d(this.max0.getX(), this.max0.getY(), this.min0.getZ());

                gl.glVertex3d(this.max0.getX(), this.max0.getY(), this.min0.getZ());
                gl.glVertex3d(this.min0.getX(), this.max0.getY(), this.min0.getZ());

                gl.glVertex3d(this.min0.getX(), this.max0.getY(), this.min0.getZ());
                gl.glVertex3d(this.min0.getX(), this.min0.getY(), this.min0.getZ());

                gl.glVertex3d(this.min0.getX(), this.min0.getY(), this.min0.getZ());
                gl.glVertex3d(this.min0.getX(), this.min0.getY(), this.max0.getZ());

                gl.glVertex3d(this.min0.getX(), this.min0.getY(), this.max0.getZ());
                gl.glVertex3d(this.min0.getX(), this.max0.getY(), this.max0.getZ());

                gl.glVertex3d(this.min0.getX(), this.max0.getY(), this.max0.getZ());
                gl.glVertex3d(this.min0.getX(), this.max0.getY(), this.min0.getZ());

                gl.glVertex3d(this.max0.getX(), this.min0.getY(), this.min0.getZ());
                gl.glVertex3d(this.max0.getX(), this.min0.getY(), this.max0.getZ());

                gl.glVertex3d(this.max0.getX(), this.min0.getY(), this.max0.getZ());
                gl.glVertex3d(this.max0.getX(), this.max0.getY(), this.max0.getZ());

                gl.glVertex3d(this.max0.getX(), this.max0.getY(), this.max0.getZ());
                gl.glVertex3d(this.max0.getX(), this.max0.getY(), this.min0.getZ());

                gl.glVertex3d(this.min0.getX(), this.min0.getY(), this.max0.getZ());
                gl.glVertex3d(this.max0.getX(), this.min0.getY(), this.max0.getZ());

                gl.glVertex3d(this.min0.getX(), this.max0.getY(), this.max0.getZ());
                gl.glVertex3d(this.max0.getX(), this.max0.getY(), this.max0.getZ());
            }
            gl.glEnd();
        }
        gl.glEndList();

        gl.glCallList(this.listOBB);
    }
}
