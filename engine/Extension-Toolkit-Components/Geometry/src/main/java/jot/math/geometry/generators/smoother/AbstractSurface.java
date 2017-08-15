/*
 * This file is part of the JOT game engine geometry extension toolkit 
 * component.
 * Copyright(C) 2014 Gonçalo Amador & Abel Gomes
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
package jot.math.geometry.generators.smoother;

import java.util.ArrayList;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Abstract class that each surface generator must implement.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractSurface {

    /**
     * Retrieve the last surface generated.
     *
     * @return the last surface generated. If no surface was ever generated
     * returns an empty {@literal ArrayList<ArrayList<Vector3D>>}.
     */
    public abstract ArrayList<ArrayList<Vector3D>> getSurface();

    /**
     * Generate a surface S(u,v).
     *
     * @param Heightfield the height field of control points.
     * @param smoothFactor relates to either the number of iterations to perform
     * the smooth algorithm or to the amount of extra points required to
     * generate.
     */
    public abstract void generateSurface(ArrayList<ArrayList<Vector3D>> Heightfield, int smoothFactor);
}
