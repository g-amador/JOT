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

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Class that implements geometric transformations given a vector, i.e.,shear,
 * rotate, scale, and translate.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Transformations {

    static final Logger log = getLogger("Transformations");

    static {
        log.setLevel(OFF);
    }

    /**
     * Get the transformation matrix.
     *
     * @param y offset.
     * @param z offset.
     * @return the transformation matrix.
     */
    public static float[] getShearXMatrix(float y, float z) {
        float[] shearingMatrix = {
            1, y, z, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1};

        return shearingMatrix;
    }

    /**
     * Get the transformation matrix.
     *
     * @param x offset.
     * @param z offset.
     * @return the transformation matrix.
     */
    public static float[] getShearYMatrix(float x, float z) {
        float[] shearingMatrix = {
            1, 0, 0, 0,
            x, 1, z, 0,
            0, 0, 1, 0,
            0, 0, 0, 1};

        return shearingMatrix;
    }

    /**
     * Get the transformation matrix.
     *
     * @param x offset.
     * @param y offset.
     * @return the transformation matrix.
     */
    public static float[] getShearZMatrix(float x, float y) {
        float[] shearingMatrix = {
            1, 0, 0, 0,
            0, 1, 0, 0,
            x, y, 1, 0,
            0, 0, 0, 1};

        return shearingMatrix;
    }

    /**
     * Apply the rotation to a provided vector v around the X axis.
     *
     * @param rotation to apply in the X axis.
     * @param translation in the XYZ axis.
     * @param v the vector to rotate around the X axis.
     * @return the value of v after rotated.
     */
    public static Vector3D rotateX(double rotation, Vector3D translation, Vector3D v) {
        double[][] rotationMatrix = {
            {1, 0, 0, 0},
            {0, cos(toRadians(rotation)), -sin(toRadians(rotation)), 0},
            {0, sin(toRadians(rotation)), cos(toRadians(rotation)), 0},
            {0, 0, 0, 1}};

        RealMatrix matrix = createRealMatrix(rotationMatrix);

        return rotateXYZ(translation, v, matrix);
    }

    /**
     * Apply the rotation to a provided vector v around the Y axis.
     *
     * @param rotation to apply in the Y axis.
     * @param translation in the XYZ axis.
     * @param v the vector to rotate around the Y axis.
     * @return the value of v after rotated.
     */
    public static Vector3D rotateY(double rotation, Vector3D translation, Vector3D v) {
        double[][] rotationMatrix = {
            {cos(toRadians(rotation)), 0, sin(toRadians(rotation)), 0},
            {0, 1, 0, 0},
            {-sin(toRadians(rotation)), 0, cos(toRadians(rotation)), 0},
            {0, 0, 0, 1}};

        RealMatrix matrix = createRealMatrix(rotationMatrix);

        return rotateXYZ(translation, v, matrix);
    }

    /**
     * Apply the rotation to a provided vector v around the Z axis.
     *
     * @param rotation to apply in the Z axis.
     * @param translation in the XYZ axis.
     * @param v the vector to rotate around the Z axis.
     * @return the value of v after rotated.
     */
    public static Vector3D rotateZ(double rotation, Vector3D translation, Vector3D v) {
        double[][] rotationMatrix = {
            {cos(toRadians(rotation)), -sin(toRadians(rotation)), 0, 0},
            {sin(toRadians(rotation)), cos(toRadians(rotation)), 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}};

        RealMatrix matrix = createRealMatrix(rotationMatrix);

        return rotateXYZ(translation, v, matrix);
    }

    /**
     * Initialize all matrixes common to all XYZ rotations for a given vector v
     * and returns the value of v rotated in either XYZ.
     *
     * @param translation in the XYZ axis.
     * @param v the vector to rotate around the XYZ axis.
     * @param m3 the rotation matrix.
     * @return the value of v after rotated in either XYZ.
     */
    private static Vector3D rotateXYZ(Vector3D translation, Vector3D v, RealMatrix m3) {
        double[][] matrix0 = {
            {v.getX(), 0, 0, 0},
            {v.getY(), 0, 0, 0},
            {v.getZ(), 0, 0, 0},
            {1, 0, 0, 0}};
        double[][] matrix1 = {
            {1, 0, 0, translation.getX()},
            {0, 1, 0, translation.getY()},
            {0, 0, 1, translation.getZ()},
            {0, 0, 0, 1}};
        double[][] matrix2 = {
            {1, 0, 0, -translation.getX()},
            {0, 1, 0, -translation.getY()},
            {0, 0, 1, -translation.getZ()},
            {0, 0, 0, 1}};
        double[][] matrix4 = {
            {1, 0, 0, translation.getX()},
            {0, 1, 0, translation.getY()},
            {0, 0, 1, translation.getZ()},
            {0, 0, 0, 1}};

        RealMatrix m0 = createRealMatrix(matrix0);
        RealMatrix m1 = createRealMatrix(matrix1);
        RealMatrix m2 = createRealMatrix(matrix2);
        RealMatrix m4 = createRealMatrix(matrix4);

        m4 = m4.multiply(m3.multiply(m2.multiply(m1.multiply(m0))));

        return new Vector3D(m4.getEntry(0, 0), m4.getEntry(1, 0), m4.getEntry(2, 0));
    }

    /**
     * A scaling matrix has a scale multiplier for each axis. Essentially, it
     * adjusts the length of the direction vectors in the first three columns.
     *
     * @param v the vector to transform.
     * @param offset XYZ offset.
     * @return the transformed vector.
     */
    public static Vector3D scale(Vector3D v, Vector3D offset) {
        double[][] matrix = {
            {v.getX(), 0, 0, 0},
            {v.getY(), 0, 0, 0},
            {v.getZ(), 0, 0, 0},
            {1, 0, 0, 0}};
        double[][] scalingMatrix = {
            {offset.getX(), 0, 0, 0},
            {0, offset.getY(), 0, 0},
            {0, 0, offset.getZ(), 0},
            {0, 0, 0, 1}};

        RealMatrix m0 = createRealMatrix(matrix);
        RealMatrix m1 = createRealMatrix(scalingMatrix);
        RealMatrix m2 = m1.multiply(m0);

        return new Vector3D(m2.getEntry(0, 0), m2.getEntry(1, 0), m2.getEntry(2, 0));
    }

    /**
     * A translation matrix is very simple. The spatial offset on each axis is
     * assigned to a cell in the fourth column. When a vector is multiplied by
     * it, this has an effect equivalent to adding each translation value to its
     * corresponding vector component.
     *
     * @param v the vector to transform.
     * @param offset XYZ offset.
     * @return the transformed vector.
     */
    public static Vector3D translate(Vector3D v, Vector3D offset) {
        double[][] matrix = {
            {v.getX(), 0, 0, 0},
            {v.getY(), 0, 0, 0},
            {v.getZ(), 0, 0, 0},
            {1, 0, 0, 0}};
        double[][] translationMatrix = {
            {1, 0, 0, offset.getX()},
            {0, 1, 0, offset.getY()},
            {0, 0, 1, offset.getZ()},
            {0, 0, 0, 1}};

        RealMatrix m0 = createRealMatrix(matrix);
        RealMatrix m1 = createRealMatrix(translationMatrix);
        RealMatrix m2 = m1.multiply(m0);

        return new Vector3D(m2.getEntry(0, 0), m2.getEntry(1, 0), m2.getEntry(2, 0));
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private Transformations() {
    }
}
