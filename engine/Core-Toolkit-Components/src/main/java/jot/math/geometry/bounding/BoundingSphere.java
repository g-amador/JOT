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
import com.jogamp.opengl.glu.GLU;
import static com.jogamp.opengl.glu.GLU.GLU_FLAT;
import static com.jogamp.opengl.glu.GLU.GLU_LINE;
import static com.jogamp.opengl.glu.GLU.GLU_OUTSIDE;
import com.jogamp.opengl.glu.GLUquadric;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.math.Distance.getDistance;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.SPHERE;
import jot.math.geometry.shape.Sphere;
import jot.physics.Material;
import jot.physics.Ray;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements a Bonding Sphere.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class BoundingSphere extends AbstractBoundingVolume {

    static final Logger log = getLogger("BoundingSphere");

    static {
        log.setLevel(OFF);
    }

    /**
     * Reusable object to access to the OpenGL Utility Library (GLU)
     */
    private final GLU glu;

    /**
     * Constructor for a bounding sphere.
     *
     * @param min coordinates in the Cartesian system of the bounding Volume
     * minimum.
     * @param max coordinates in the Cartesian system of the bounding Volume
     * maximum.
     */
    public BoundingSphere(Vector3D min, Vector3D max) {
        this.Id = "BoundingSphere";
        this.min = new Vector3D(min.toArray());
        this.max = new Vector3D(max.toArray());
        this.min0 = new Vector3D(min.toArray());
        this.max0 = new Vector3D(max.toArray());
        this.boundingVolumeType = SPHERE;
        this.glu = new GLU();
        this.material = new Material();
    }

    /**
     * Constructor for a bounding sphere.
     *
     * @param boundingVolume to copy from the minimum and maximum coordinates in
     * the Cartesian system.
     */
    public BoundingSphere(AbstractBoundingVolume boundingVolume) {
        if (boundingVolume != null) {
            this.Id = boundingVolume.Id;
            this.min = new Vector3D(boundingVolume.min.toArray());
            this.max = new Vector3D(boundingVolume.max.toArray());
            this.min0 = new Vector3D(boundingVolume.min0.toArray());
            this.max0 = new Vector3D(boundingVolume.max0.toArray());
        } else {
            this.Id = "BoundingSphere";
            this.min = ZERO;
            this.max = ZERO;
            this.min0 = ZERO;
            this.max0 = ZERO;
        }
        this.boundingVolumeType = SPHERE;
        this.glu = new GLU();
        this.material = new Material();
    }

    @Override
    public Vector3D getRayIntersection(Ray ray) {
        return (new Sphere(this.halfDistance(), this.barycentre(), this.material)).intersect(ray).getIntersectionPoint();
    }

    @Override
    public boolean intersectsRay(Ray ray) {
        return (new Sphere(this.halfDistance(), this.barycentre(), this.material)).intersect(ray).isHit();
    }

    @Override
    public boolean isCollide(AbstractBoundingVolume otherBoundingVolume) {
        Vector3D thisBoundingVolumeBarycentre = new Vector3D(this.barycentre().toArray());
        Vector3D otherBoundingVolumeBarycentre = new Vector3D(otherBoundingVolume.barycentre().toArray());

        //SPHERE with SPHERE collision test.
        if (otherBoundingVolume.boundingVolumeType == SPHERE) {
            return this != otherBoundingVolume && getDistance(
                    thisBoundingVolumeBarycentre, otherBoundingVolumeBarycentre)
                    < (this.halfDistance() + otherBoundingVolume.halfDistance());
        }

        //SPHERE with OBB/AABB collision test.
        if (otherBoundingVolume.boundingVolumeType != SPHERE) {
            return this.isCollideSPHERE(otherBoundingVolume, this);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoundingSphere getNode() {
        return this;
    }

    @Override
    public void render(GL2 gl) {
        // Draw sphere (possible styles: FILL, LINE, POINT).       
        GLUquadric wiredBoundingVolume = this.glu.gluNewQuadric();
        this.glu.gluQuadricDrawStyle(wiredBoundingVolume, GLU_LINE);
        this.glu.gluQuadricNormals(wiredBoundingVolume, GLU_FLAT);
        this.glu.gluQuadricOrientation(wiredBoundingVolume, GLU_OUTSIDE);
        float radius = this.halfDistance();
        int slices = 16;
        int stacks = 16;
        this.glu.gluSphere(wiredBoundingVolume, radius, slices, stacks);
        this.glu.gluDeleteQuadric(wiredBoundingVolume);
    }
}
