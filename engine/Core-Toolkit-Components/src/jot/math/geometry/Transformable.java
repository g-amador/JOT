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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import jot.math.geometry.bounding.AbstractBoundingVolume;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Interface that a class transformGroup must implement, i.e., an object that
 * contains one or more sub-meshes each with one associated BV.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public interface Transformable {

    /**
     *
     * Get one of this transformGroup bounding volumes given the bounding volume
     * Id.
     *
     * @param Id one of this transformGroup boundingVolumes Id.
     * @return this transformGroup boundingVolume with the given Id.
     */
    AbstractBoundingVolume getBoundingVolume(int Id);

    /**
     *
     * Get a copy of one of this transformGroup bounding volumes given the
     * bounding volume Id.
     *
     * @param Id one of this transformGroup boundingVolumes Id.
     * @return this transformGroup boundingVolume with the given Id.
     */
    AbstractBoundingVolume getBoundingVolumeCopy(int Id);

    /**
     * Set this transformGroup bounding volume.
     *
     * @param Id one of this transformGroup boundingVolumes Id.
     * @param boundingVolume this transformGroup bounding volume.
     */
    void setBoundingVolume(int Id, AbstractBoundingVolume boundingVolume);

    /**
     * Get this TransformGroup Bounding Volumes.
     *
     * @return this TransformGroup Bounding Volumes.
     */
    CopyOnWriteArrayList<AbstractBoundingVolume> getBoundingVolumes();

    /**
     * Get this TransformGroup Meshes.
     *
     * @return this TransformGroup Meshes.
     */
    ArrayList<Mesh> getMeshes();

    /**
     * Get this transformGroup present translation, i.e., present position in
     * Cartesian space relative to the referential origin.
     *
     * @return this transformGroup present translation.
     */
    Vector3D getTranslation();

    /**
     * Set this transformGroup translation, i.e., position in Cartesian space
     * relative to the referential origin. Also, update the same information for
     * the respective bounding volume.
     *
     * @param translation this transformGroup new translation.
     */
    void setTranslation(Vector3D translation);

    /**
     * Get this transformGroup past translation, i.e., past position in
     * Cartesian space relative to the referential origin.
     *
     * @return this transformGroup past translation.
     */
    Vector3D getPastTranslation();

    /**
     * Set this transformGroup past translation, i.e., position in Cartesian
     * space relative to the referential origin. Also, update the same
     * information for the respective bounding volume.
     *
     * @param translation this transformGroup new past translation.
     */
    void setPastTranslation(Vector3D translation);

    /**
     * Get this transformGroup rotation.
     *
     * @return this transformGroup rotation.
     */
    Vector3D getRotation();

    /**
     * Set this transformGroup rotation, in the interval [-180;180]. Also,
     * update the same information for the respective bounding volume.
     *
     * @param rotation this transformGroup new rotation.
     */
    void setRotation(Vector3D rotation);

    /**
     * Set this transformGroup rotation in X, in the interval [-180;180]. Also,
     * update the same information for the respective bounding volume.
     *
     * @param rotation this transformGroup new rotation in X.
     */
    void setRotationX(float rotation);

    /**
     * Set this transformGroup rotation in Y, in the interval [-180;180]. Also,
     * update the same information for the respective bounding volume.
     *
     * @param rotation this transformGroup new rotation in Y.
     */
    void setRotationY(float rotation);

    /**
     * Set this transformGroup rotation in Z, in the interval [-180;180]. Also,
     * update the same information for the respective bounding volume.
     *
     * @param rotation this transformGroup new rotation in Z.
     */
    void setRotationZ(float rotation);

    /**
     * Get the scale factor for this transformGroup.
     *
     * @return this transformGroup scaling.
     */
    Vector3D getScaling();

    /**
     * Set the scale factor for this transformGroup.
     *
     * @param scaling this transformGroup new scale factor.
     */
    void setScaling(Vector3D scaling);

    /**
     * Add a child node to this TransformGroup (a transformGroup may have one or
     * more associated mesh).
     *
     * @param node the new child node to add.
     * @return TRUE if child node added, FALSE otherwise.
     */
    boolean addChild(Node node);

    /**
     * Remove a indicated child node from this TransformGroup.
     *
     * @param node the child node to remove.
     * @return TRUE if child node removed, FALSE otherwise.
     */
    boolean removeChild(Node node);

    /**
     * See if given Id corresponds to one of this transformGroup children.
     *
     * @param Id of the child to search.
     * @return TRUE if the provided Id corresponds to one of this transformGroup
     * children, FALSE otherwise.
     */
    boolean isChild(String Id);

    /**
     * Get a transformGroup child.
     *
     * @param Id of the transformGroup child.
     * @return the transformGroup child with the provided Id, NULL otherwise.
     */
    Node getChild(String Id);

    /**
     * Return the identifiers of all the child nodes of this transformGroup.
     *
     * @return the identifiers of all the child nodes.
     */
    Iterator<Node> childIterator();

//    /**
//     * Get this TransformGroup rotation plus its first transformGroup child
//     * rotation.
//     *
//     * @return this TransformGroup rotation plus its first transformGroup child
//     * rotation.
//     */
//    Vector3D childRotation();
//
//    /**
//     * Get this TransformGroup translation plus its first transformGroup child
//     * translation.
//     *
//     * @return this TransformGroup translation plus its first transformGroup
//     * child translation.
//     */
//    Vector3D childTranslation();
//
//    /**
//     * Get this TransformGroup scaling plus its first transformGroup child
//     * scaling.
//     *
//     * @return this TransformGroup scaling plus its first transformGroup child
//     * scaling.
//     */
//    Vector3D childScaling();
    /**
     * Update this transformGroup translation, i.e., position in Cartesian space
     * relative to the referential origin. Also, update the same information for
     * the respective bounding volume.
     *
     * @param update this transformGroup translation variation to add.
     */
    void updateTranslation(Vector3D update);

    /**
     * Update this transformGroup rotation in X. Also, update the same
     * information for the respective bounding volume.
     *
     * @param update this transformGroup rotation in X variation.
     */
    void updateRotationX(float update);

    /**
     * Update this transformGroup rotation in Y. Also, update the same
     * information for the respective bounding volume.
     *
     * @param update this transformGroup rotation in Y variation.
     */
    void updateRotationY(float update);

    /**
     * Update this transformGroup rotation in Z. Also, update the same
     * information for the respective bounding volume.
     *
     * @param update this transformGroup rotation in Z variation.
     */
    void updateRotationZ(float update);
}
