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
package jot.math.geometry;

import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import static java.lang.String.format;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.math.geometry.Transformations.rotateX;
import static jot.math.geometry.Transformations.rotateY;
import static jot.math.geometry.Transformations.rotateZ;
import jot.math.geometry.bounding.AABB;
import jot.math.geometry.bounding.AbstractBoundingVolume;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.AABB;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.OBB;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.SPHERE;
import jot.math.geometry.bounding.BoundingSphere;
import jot.math.geometry.bounding.OBB;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements a transformGroup, i.e., an object that contains one or
 * more sub-meshes each with one associated BV.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class TransformGroup implements Cloneable, Node, Transformable {

    static final Logger log = getLogger("TransformGroup");

    static {
        log.setLevel(OFF);
    }

    /**
     * Reusable object to access to the OpenGL Utility Library (GLU)
     */
    protected GLU glu;

    /**
     * The identifier associated with this transformGroup.
     */
    protected String Id;

    /**
     * This transformGroup past translation, i.e., past position in Cartesian
     * space relative to the referential origin.
     */
    protected Vector3D pastTranslation;

    /**
     * This transformGroup translation, i.e., present position in Cartesian
     * space relative to the referential origin.
     */
    protected Vector3D translation;

    /**
     * This transformGroup rotation.
     */
    protected Vector3D rotation;

    /**
     * The scale factor for this transformGroup.
     */
    protected Vector3D scaling;

    /**
     * This transformGroup bounding volumes.
     */
    protected final CopyOnWriteArrayList<AbstractBoundingVolume> boundingVolumes;

    /**
     * This transformGroup associated Node nodes.
     */
    protected final ConcurrentLinkedQueue<Node> children;

    /**
     * Constructor.
     */
    public TransformGroup() {
        this.Id = "TransformGroup";
        this.children = new ConcurrentLinkedQueue<>();
        this.pastTranslation = ZERO;
        this.translation = ZERO;
        this.rotation = ZERO;
        this.scaling = new Vector3D(1, 1, 1);
        this.boundingVolumes = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 2; i++) {
            this.boundingVolumes.add(new BoundingSphere(null));
        }
    }

    /**
     * Constructor.
     *
     * @param Id the unique identifier for this transformGroup.
     */
    public TransformGroup(String Id) {
        this.Id = Id;
        this.children = new ConcurrentLinkedQueue<>();
        this.pastTranslation = ZERO;
        this.translation = ZERO;
        this.rotation = ZERO;
        this.scaling = new Vector3D(1, 1, 1);
        this.boundingVolumes = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 2; i++) {
            this.boundingVolumes.add(new BoundingSphere(null));
        }
    }

    /**
     * Constructor.
     *
     * @param Id the unique identifier for this transformGroup.
     * @param tg another transformGroup.
     */
    public TransformGroup(String Id, TransformGroup tg) {
        this.Id = Id;
        this.children = new ConcurrentLinkedQueue<>();
        this.pastTranslation = ZERO;
        this.translation = ZERO;
        this.rotation = ZERO;
        this.scaling = new Vector3D(1, 1, 1);
        this.boundingVolumes = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 2; i++) {
            switch (tg.getBoundingVolumes().get(i).boundingVolumeType) {
                case SPHERE:
                    this.boundingVolumes.add(new BoundingSphere(tg.getBoundingVolumes().get(i)));
                    break;
                case AABB:
                    this.boundingVolumes.add(new AABB(tg.getBoundingVolumes().get(i)));
                    break;
                case OBB:
                    this.boundingVolumes.add(new OBB(tg.getBoundingVolumes().get(i)));
                    break;
                default:
                    log.info("Not supported BV.");
            }
        }
    }

    @Override
    public TransformGroup clone() throws CloneNotSupportedException {
        log.info(this.Id);
        log.info(this.getTranslation().toString());
        log.info(this.getPastTranslation().toString());
        log.info(this.getRotation().toString());
        log.info(this.getScaling().toString());
        log.info(this.getBoundingVolume(0).toString());

        this.boundingVolumes.stream()
                .filter(bv -> bv != null)
                .forEach(bv -> {
                    log.info(bv.max.toString());
                    log.info(bv.min.toString());
                    log.info(bv.max0.toString());
                    log.info(bv.min0.toString());
                    log.info("");
                });

        return (TransformGroup) super.clone();
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
     * {@inheritDoc}
     */
    @Override
    public Node getNode() {
        Iterator<Node> it = this.children.iterator();
        if (it.hasNext()) {
            Node child = it.next();
            return child.getNode();
        }
        //for (Node child : this.children) {
        //    return child.getNode();
        //}
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GL2 gl) {
        if (!this.boundingVolumes.isEmpty()) {
            this.boundingVolumes.stream()
                    .filter(bv -> bv != null && bv.isRenderable())
                    .forEach(bv -> {
                        switch (bv.boundingVolumeType) {
                            case AABB:
                                gl.glPushMatrix();
                                 {
                                    bv.getMaterial().applyMaterialProperties(gl);
                                    //gl.glMaterialfv(GL_FRONT, GL_EMISSION, bv.material.color, 0);
                                    //gl.glMaterialfv(GL_FRONT, GL_AMBIENT, bv.material.color, 0);
                                    //gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, bv.material.color, 0);

                                    //gl.glTranslated(translation.getX(), translation.getY(), translation.getZ());
                                    //if (rotation.getX() != 0) {
                                    //    gl.glRotated(rotation.getX(), 1, 0, 0);
                                    //}
                                    //if (rotation.getY() != 0) {
                                    //    gl.glRotated(rotation.getY(), 0, 1, 0);
                                    //}
                                    //if (rotation.getZ() != 0) {
                                    //    gl.glRotated(rotation.getZ(), 0, 0, 1);
                                    //}
                                    //((AABB) bv).render(gl);
                                    //((Node) bv).render(gl);
                                    bv.render(gl);
                                }
                                gl.glPopMatrix();
                                break;
                            case OBB:
                                gl.glPushMatrix();
                                 {
                                    bv.getMaterial().applyMaterialProperties(gl);
                                    //gl.glMaterialfv(GL_FRONT, GL_EMISSION, bv.material.color, 0);
                                    //gl.glMaterialfv(GL_FRONT, GL_AMBIENT, bv.material.color, 0);
                                    //gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, bv.material.color, 0);

                                    gl.glBegin(GL_LINES);
                                    {
                                        gl.glVertex3d(bv.min.getX(), bv.min.getY(), bv.min.getZ());
                                        gl.glVertex3d(bv.max.getX(), bv.max.getY(), bv.max.getZ());
                                    }
                                    gl.glEnd();

                                    gl.glTranslated(this.translation.getX(), this.translation.getY(), this.translation.getZ());

                                    if (this.rotation.getX() != 0) {
                                        gl.glRotated(this.rotation.getX(), 1, 0, 0);
                                    }
                                    if (this.rotation.getY() != 0) {
                                        gl.glRotated(this.rotation.getY(), 0, 1, 0);
                                    }
                                    if (this.rotation.getZ() != 0) {
                                        gl.glRotated(this.rotation.getZ(), 0, 0, 1);
                                    }

                                    //((OBB) bv).render(gl);                                    
                                    //((Node) bv).render(gl);
                                    bv.render(gl);
                                }
                                gl.glPopMatrix();
                                break;
                            case SPHERE:
                                gl.glPushMatrix();
                                 {
                                    bv.getMaterial().applyMaterialProperties(gl);
                                    //gl.glMaterialfv(GL_FRONT, GL_EMISSION, bv.material.color, 0);
                                    //gl.glMaterialfv(GL_FRONT, GL_AMBIENT, bv.material.color, 0);
                                    //gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, bv.material.color, 0);

                                    Vector3D BVBarycentre = bv.barycentre();
                                    gl.glTranslated(BVBarycentre.getX(), BVBarycentre.getY(), BVBarycentre.getZ());
                                    if (this.rotation.getX() != 0) {
                                        gl.glRotated(this.rotation.getX(), 1, 0, 0);
                                    }
                                    if (this.rotation.getY() != 0) {
                                        gl.glRotated(this.rotation.getY(), 0, 1, 0);
                                    }
                                    if (this.rotation.getZ() != 0) {
                                        gl.glRotated(this.rotation.getZ(), 0, 0, 1);
                                    }

                                    //((BoundingSphere) bv).render(gl);
                                    //((Node) bv).render(gl);
                                    bv.render(gl);
                                }
                                gl.glPopMatrix();
                                break;
                        }
                    });
        }

        gl.glPushMatrix();
        {
            gl.glTranslated(this.translation.getX(), this.translation.getY(), this.translation.getZ());
            if (this.rotation.getX() != 0) {
                gl.glRotated(this.rotation.getX(), 1, 0, 0);
            }
            if (this.rotation.getY() != 0) {
                gl.glRotated(this.rotation.getY(), 0, 1, 0);
            }
            if (this.rotation.getZ() != 0) {
                gl.glRotated(this.rotation.getZ(), 0, 0, 1);
            }
            gl.glScaled(this.scaling.getX(), this.scaling.getY(), this.scaling.getZ());

            this.children.stream().forEach(child -> child.render(gl));
        }
        gl.glPopMatrix();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(GL2 gl) {
        if (gl != null) {
            log.info("Dispose.");

            this.children.stream()
                    .filter(child -> child != null)
                    .forEach(child -> child.dispose(gl));

            this.boundingVolumes.stream()
                    .filter(bv -> bv != null)
                    .forEach(bv -> bv.dispose(gl));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<Mesh> getMeshes() {
        ArrayList<Mesh> meshes = new ArrayList<>();
        this.children.stream().forEach(child -> meshes.add((Mesh) child.getNode()));

        return meshes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractBoundingVolume getBoundingVolume(int Id) {
        return this.boundingVolumes.get(Id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractBoundingVolume getBoundingVolumeCopy(int Id) {
        switch (this.boundingVolumes.get(Id).boundingVolumeType) {
            case SPHERE:
                return new BoundingSphere(this.boundingVolumes.get(Id));
            case AABB:
                return new AABB(this.boundingVolumes.get(Id));
            case OBB:
                return new OBB(this.boundingVolumes.get(Id));
            default:
                throw new AssertionError(this.boundingVolumes.get(Id).boundingVolumeType.name());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBoundingVolume(int Id, AbstractBoundingVolume boundingVolume) {
        this.boundingVolumes.set(Id, boundingVolume);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CopyOnWriteArrayList<AbstractBoundingVolume> getBoundingVolumes() {
        return this.boundingVolumes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getTranslation() {
        return this.translation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTranslation(Vector3D translation) {
        this.pastTranslation = new Vector3D(this.translation.toArray());
        this.translation = translation;
        if (!this.boundingVolumes.isEmpty()) {
            this.boundingVolumes.stream()
                    .filter(boundingVolume -> boundingVolume != null)
                    .map(boundingVolume -> {
                        log.info(format("%s %s",
                                boundingVolume.max.toString(),
                                boundingVolume.min.toString()));
                        return boundingVolume;
                    })
                    .map(boundingVolume -> {
                        boundingVolume.max = new Vector3D(
                                boundingVolume.max0.toArray()).add(translation);
                        return boundingVolume;
                    })
                    .map(boundingVolume -> {
                        boundingVolume.min = new Vector3D(
                                boundingVolume.min0.toArray()).add(translation);
                        return boundingVolume;
                    })
                    .forEachOrdered(boundingVolume -> {
                        log.info(format("%s %s\n",
                                boundingVolume.max.toString(),
                                boundingVolume.min.toString()));
                    });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getPastTranslation() {
        return this.pastTranslation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPastTranslation(Vector3D translation) {
        this.pastTranslation = translation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getRotation() {
        return this.rotation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRotation(Vector3D rotation) {
        //set all values of rotation in interval [-180;180]
        float x = (float) rotation.getX();
        float y = (float) rotation.getY();
        float z = (float) rotation.getZ();

        if (rotation.getX() < -180) {
            x += 360;
        }
        if (rotation.getX() > 180) {
            x -= 360;
        }
        if (rotation.getY() < -180) {
            y += 360;
        }
        if (rotation.getY() > 180) {
            y -= 360;
        }
        if (rotation.getZ() < -180) {
            z += 360;
        }
        if (rotation.getZ() > 180) {
            z -= 360;
        }

        this.rotation = new Vector3D(x, y, z);
    }

    /**
     * Get the minimum XYZ coordinates of two given vectors.
     *
     * @param v1 a given vector.
     * @param v2 a given vector.
     * @return a new vector with the minimum XYZ coordinates of two given
     * vectors.
     */
    protected Vector3D getMin(Vector3D v1, Vector3D v2) {
        float x = (float) (v1.getX() < v2.getX() ? v1.getX() : v2.getX());
        float y = (float) (v1.getY() < v2.getY() ? v1.getY() : v2.getY());
        float z = (float) (v1.getZ() < v2.getZ() ? v1.getZ() : v2.getZ());

        return new Vector3D(x, y, z);
    }

    /**
     * Get the maximum XYZ coordinates oof two given vectors.
     *
     * @param v1 a given vector.
     * @param v2 a given vector.
     * @return a new vector with the maximum XYZ coordinates of two given
     * vectors.
     */
    protected Vector3D getMax(Vector3D v1, Vector3D v2) {
        float x = (float) (v1.getX() > v2.getX() ? v1.getX() : v2.getX());
        float y = (float) (v1.getY() > v2.getY() ? v1.getY() : v2.getY());
        float z = (float) (v1.getZ() > v2.getZ() ? v1.getZ() : v2.getZ());

        return new Vector3D(x, y, z);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRotationX(float rotation) {
        float x = rotation;

        //set x rotation in interval [-180;180]
        if (rotation < -180) {
            x += 360;
        }
        if (rotation > 180) {
            x -= 360;
        }
        this.rotation = new Vector3D(x, this.rotation.getY(), this.rotation.getZ());

        if (!this.boundingVolumes.isEmpty()) {
            this.boundingVolumes.stream()
                    .filter(boundingVolume -> boundingVolume != null)
                    .forEachOrdered(boundingVolume -> {
                        if (boundingVolume.boundingVolumeType != AABB) {
                            boundingVolume.min = rotateX(this.rotation.getX(), this.translation, boundingVolume.min0);
                            boundingVolume.max = rotateX(this.rotation.getX(), this.translation, boundingVolume.max0);
                            boundingVolume.min0 = rotateX(this.rotation.getX(), this.translation, boundingVolume.min0);
                            boundingVolume.max0 = rotateX(this.rotation.getX(), this.translation, boundingVolume.max0);
                        } else {
                            Vector3D v1 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.min0.toArray()));
                            Vector3D v2 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.max0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v3 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.min0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v4 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.max0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v5 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.min0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v6 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.min0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v7 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.max0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v8 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.max0.toArray()));

                            Vector3D min = this.getMin(this.getMin(this.getMin(this.getMin(this.getMin(this.getMin(
                                    this.getMin(v1, v2), v3), v4), v5), v6), v7), v8);
                            Vector3D max = this.getMax(this.getMax(this.getMax(this.getMax(this.getMax(this.getMax(
                                    this.getMax(v1, v2), v3), v4), v5), v6), v7), v8);

                            boundingVolume.min0 = boundingVolume.min0.subtract(min);
                            boundingVolume.max0 = boundingVolume.max0.subtract(max);
                            boundingVolume.min = boundingVolume.min.subtract(boundingVolume.min0);
                            boundingVolume.max = boundingVolume.max.subtract(boundingVolume.max0);
                            boundingVolume.min0 = min;
                            boundingVolume.max0 = max;
                        }
                    });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRotationY(float rotation) {
        float y = rotation;

        //set x rotation in interval [-180;180]
        if (rotation < -180) {
            y += 360;
        }
        if (rotation > 180) {
            y -= 360;
        }
        this.rotation = new Vector3D(this.rotation.getX(), y, this.rotation.getZ());

        if (!this.boundingVolumes.isEmpty()) {
            this.boundingVolumes.stream()
                    .filter(boundingVolume -> boundingVolume != null)
                    .forEachOrdered(boundingVolume -> {
                        if (boundingVolume.boundingVolumeType != AABB) {
                            boundingVolume.min = rotateY(this.rotation.getY(), this.translation, boundingVolume.min0);
                            boundingVolume.max = rotateY(this.rotation.getY(), this.translation, boundingVolume.max0);
                            boundingVolume.min0 = rotateY(this.rotation.getY(), this.translation, boundingVolume.min0);
                            boundingVolume.max0 = rotateY(this.rotation.getY(), this.translation, boundingVolume.max0);
                        } else {
                            Vector3D v1 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.min0.toArray()));
                            Vector3D v2 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.max0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v3 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.min0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v4 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.max0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v5 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.min0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v6 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.min0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v7 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.max0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v8 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.max0.toArray()));

                            Vector3D min = this.getMin(this.getMin(this.getMin(this.getMin(this.getMin(this.getMin(
                                    this.getMin(v1, v2), v3), v4), v5), v6), v7), v8);
                            Vector3D max = this.getMax(this.getMax(this.getMax(this.getMax(this.getMax(this.getMax(
                                    this.getMax(v1, v2), v3), v4), v5), v6), v7), v8);

                            boundingVolume.min0 = boundingVolume.min0.subtract(min);
                            boundingVolume.max0 = boundingVolume.max0.subtract(max);
                            boundingVolume.min = boundingVolume.min.subtract(boundingVolume.min0);
                            boundingVolume.max = boundingVolume.max.subtract(boundingVolume.max0);
                            boundingVolume.min0 = min;
                            boundingVolume.max0 = max;
                        }
                    });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRotationZ(float rotation) {
        float z = rotation;

        //set x rotation in interval [-180;180]
        if (rotation < -180) {
            z += 360;
        }
        if (rotation > 180) {
            z -= 360;
        }
        this.rotation = new Vector3D(this.rotation.getX(), this.rotation.getY(), z);

        if (!this.boundingVolumes.isEmpty()) {
            this.boundingVolumes.stream()
                    .filter(boundingVolume -> boundingVolume != null)
                    .forEachOrdered(boundingVolume -> {
                        if (boundingVolume.boundingVolumeType != AABB) {
                            boundingVolume.min = rotateZ(this.rotation.getZ(), this.translation, boundingVolume.min0);
                            boundingVolume.max = rotateZ(this.rotation.getZ(), this.translation, boundingVolume.max0);
                            boundingVolume.min0 = rotateZ(this.rotation.getZ(), this.translation, boundingVolume.min0);
                            boundingVolume.max0 = rotateZ(this.rotation.getZ(), this.translation, boundingVolume.max0);
                        } else {
                            Vector3D v1 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.min0.toArray()));
                            Vector3D v2 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.max0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v3 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.min0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v4 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.max0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v5 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.min0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v6 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.min0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v7 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.max0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v8 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.max0.toArray()));

                            Vector3D min = this.getMin(this.getMin(this.getMin(this.getMin(this.getMin(this.getMin(
                                    this.getMin(v1, v2), v3), v4), v5), v6), v7), v8);
                            Vector3D max = this.getMax(this.getMax(this.getMax(this.getMax(this.getMax(this.getMax(
                                    this.getMax(v1, v2), v3), v4), v5), v6), v7), v8);

                            boundingVolume.min0 = boundingVolume.min0.subtract(min);
                            boundingVolume.max0 = boundingVolume.max0.subtract(max);
                            boundingVolume.min = boundingVolume.min.subtract(boundingVolume.min0);
                            boundingVolume.max = boundingVolume.max.subtract(boundingVolume.max0);
                            boundingVolume.min0 = min;
                            boundingVolume.max0 = max;
                        }
                    });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getScaling() {
        return this.scaling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setScaling(Vector3D scaling) {
        this.scaling = scaling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addChild(Node node) {
        return this.children.add(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeChild(Node node) {
        return this.children.remove(node);
        //return children.remove((Node) child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChild(String Id) {
        Iterator<?> itr = this.childIterator();
        while (itr.hasNext()) {
            Node gn = this.childIterator().next();
            if (gn.getId().equals(Id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getChild(String Id) {
        Iterator<?> itr = this.childIterator();
        while (itr.hasNext()) {
            Node gn = this.childIterator().next();
            if (gn.getId().equals(Id)) {
                return gn;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Node> childIterator() {
        return this.children.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !o.getClass().getName().endsWith("TransformGroup")) {
            return false;
        }

        TransformGroup tg = (TransformGroup) o;

        return this.Id.equals(tg.getId());
    }

    @Override
    public int hashCode() {
        return this.Id.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTranslation(Vector3D update) {
        this.pastTranslation = new Vector3D(this.translation.toArray());
        this.translation = this.translation.add(update);
        if (!this.boundingVolumes.isEmpty()) {
            this.boundingVolumes.stream()
                    .filter(bv -> bv != null)
                    .forEach(bv -> {
                        bv.min = bv.min.add(update);
                        bv.max = bv.max.add(update);
                    });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRotationX(float update) {
        float x = update;

        //set x rotation in interval [-180;180]
        if (update < -180) {
            x += 360;
        }
        if (update > 180) {
            x -= 360;
        }
        this.rotation = new Vector3D(x, this.rotation.getY(), this.rotation.getZ());

        if (!this.boundingVolumes.isEmpty()) {
            this.boundingVolumes.stream()
                    .filter(boundingVolume -> boundingVolume != null)
                    .forEachOrdered(boundingVolume -> {
                        if (boundingVolume.boundingVolumeType != AABB) {
                            boundingVolume.min = rotateX(this.rotation.getX(), this.translation, boundingVolume.min0);
                            boundingVolume.max = rotateX(this.rotation.getX(), this.translation, boundingVolume.max0);
                        } else {
                            Vector3D v1 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.min0.toArray()));
                            Vector3D v2 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.max0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v3 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.min0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v4 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.max0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v5 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.min0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v6 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.min0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v7 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.max0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v8 = rotateX(this.rotation.getX(), this.translation, new Vector3D(
                                    boundingVolume.max0.toArray()));

                            Vector3D min = this.getMin(this.getMin(this.getMin(this.getMin(this.getMin(this.getMin(
                                    this.getMin(v1, v2), v3), v4), v5), v6), v7), v8);
                            Vector3D max = this.getMax(this.getMax(this.getMax(this.getMax(this.getMax(this.getMax(
                                    this.getMax(v1, v2), v3), v4), v5), v6), v7), v8);

                            boundingVolume.min = min;
                            boundingVolume.max = max;
                        }
                    });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRotationY(float update) {
        float y = update;

        //set x rotation in interval [-180;180]
        if (update < -180) {
            y += 360;
        }
        if (update > 180) {
            y -= 360;
        }
        this.rotation = new Vector3D(this.rotation.getX(), y, this.rotation.getZ());

        if (!this.boundingVolumes.isEmpty()) {
            this.boundingVolumes.stream()
                    .filter(boundingVolume -> boundingVolume != null)
                    .forEachOrdered((boundingVolume) -> {
                        if (boundingVolume.boundingVolumeType != AABB) {
                            boundingVolume.min = rotateY(this.rotation.getY(), this.translation, boundingVolume.min0);
                            boundingVolume.max = rotateY(this.rotation.getY(), this.translation, boundingVolume.max0);
                        } else {
                            Vector3D v1 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.min0.toArray()));
                            Vector3D v2 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.max0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v3 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.min0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v4 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.max0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v5 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.min0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v6 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.min0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v7 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.max0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v8 = rotateY(this.rotation.getY(), this.translation, new Vector3D(
                                    boundingVolume.max0.toArray()));

                            Vector3D min = this.getMin(this.getMin(this.getMin(this.getMin(this.getMin(this.getMin(
                                    this.getMin(v1, v2), v3), v4), v5), v6), v7), v8);
                            Vector3D max = this.getMax(this.getMax(this.getMax(this.getMax(this.getMax(this.getMax(
                                    this.getMax(v1, v2), v3), v4), v5), v6), v7), v8);

                            boundingVolume.min = min;
                            boundingVolume.max = max;
                        }
                    });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRotationZ(float update) {
        float z = update;

        //set x rotation in interval [-180;180]
        if (update < -180) {
            z += 360;
        }
        if (update > 180) {
            z -= 360;
        }
        this.rotation = new Vector3D(this.rotation.getX(), this.rotation.getY(), z);

        if (!this.boundingVolumes.isEmpty()) {
            this.boundingVolumes.stream()
                    .filter(boundingVolume -> boundingVolume != null)
                    .forEachOrdered(boundingVolume -> {
                        if (boundingVolume.boundingVolumeType != AABB) {
                            boundingVolume.min = rotateZ(this.rotation.getZ(), this.translation, boundingVolume.min0);
                            boundingVolume.max = rotateZ(this.rotation.getZ(), this.translation, boundingVolume.max0);
                        } else {
                            Vector3D v1 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.min0.toArray()));
                            Vector3D v2 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.max0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v3 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.min0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v4 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.min0.getX(), boundingVolume.max0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v5 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.min0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v6 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.min0.getY(), boundingVolume.max0.getZ()));
                            Vector3D v7 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.max0.getX(), boundingVolume.max0.getY(), boundingVolume.min0.getZ()));
                            Vector3D v8 = rotateZ(this.rotation.getZ(), this.translation, new Vector3D(
                                    boundingVolume.max0.toArray()));

                            Vector3D min = this.getMin(this.getMin(this.getMin(this.getMin(this.getMin(this.getMin(
                                    this.getMin(v1, v2), v3), v4), v5), v6), v7), v8);
                            Vector3D max = this.getMax(this.getMax(this.getMax(this.getMax(this.getMax(this.getMax(
                                    this.getMax(v1, v2), v3), v4), v5), v6), v7), v8);

                            boundingVolume.min = min;
                            boundingVolume.max = max;
                        }
                    });
        }
    }
}
