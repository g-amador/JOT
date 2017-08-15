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
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Abstract class that each curve smoother/generator must implement.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractCurve {

    /**
     * Retrieve the last curve generated.
     *
     * @return the last curve generated. If no curve was ever generated returns
     * an empty {@literal ArrayList<Vector2D>}.
     */
    public abstract ArrayList<Vector2D> getCurve();

    /**
     * Generate a curve S(u,v).
     *
     * @param ControlPoints the set of control points.
     * @param smoothFactor relates to either the number of iterations to perform
     * the smooth algorithm or to the amount of extra points required to
     * generate.
     */
    public abstract void generateCurve(ArrayList<Vector2D> ControlPoints, int smoothFactor);
}
