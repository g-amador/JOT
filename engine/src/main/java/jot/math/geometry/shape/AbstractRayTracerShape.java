/*
 * This file is part of the JOT game engine geometry framework toolkit
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
package jot.math.geometry.shape;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Abstract class that each simplex shape must implement.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractRayTracerShape extends AbstractShape {

    /**
     * The emission value of a shape.
     */
    public Vector3D emission;

    /**
     * The color the shape.
     */
    public Vector3D color;

    /**
     * The type of the shape.
     */
    public Shape shape;

    /**
     * Types of shapes: SPHERE, PLANE, and TRIANGLE.
     */
    public enum Shape {

        SPHERE, PLANE, TRIANGLE
    }
}
