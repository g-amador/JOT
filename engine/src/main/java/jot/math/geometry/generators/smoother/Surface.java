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
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements the default surface smoother/generator.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Surface extends AbstractSurface {

    static final Logger log = getLogger("Surface");

    static {
        log.setLevel(OFF);
    }

    //The geometry.
    private ArrayList<ArrayList<Vector3D>> surface;

    @Override
    public ArrayList<ArrayList<Vector3D>> getSurface() {
        return this.surface;
    }

    //Based on the source available at 
    //http://nic-gamedev.blogspot.pt/2013/02/simple-terrain-smoothing.html
    @Override
    public void generateSurface(ArrayList<ArrayList<Vector3D>> Heightfield, int smoothFactor) {
        while (smoothFactor > 0) {
            smoothFactor--;

            // Note:surface.size() and surface.get(0).size() should be equal and power-of-two values 
            this.surface = Heightfield;

            for (int x = 0; x < this.surface.size(); x++) {
                for (int y = 0; y < this.surface.get(0).size(); y++) {
                    int adjacentSections = 0;
                    float sectionsTotal = 0.0f;

                    if ((x - 1) > 0) // Check to left
                    {
                        sectionsTotal += Heightfield.get(x - 1).get(y).getY();
                        adjacentSections++;

                        if ((y - 1) > 0) // Check up and to the left
                        {
                            sectionsTotal += Heightfield.get(x - 1).get(y - 1).getY();
                            adjacentSections++;
                        }

                        if ((y + 1) < this.surface.get(0).size()) // Check down and to the left
                        {
                            sectionsTotal += Heightfield.get(x - 1).get(y + 1).getY();
                            adjacentSections++;
                        }
                    }

                    if ((x + 1) < this.surface.size()) // Check to right
                    {
                        sectionsTotal += Heightfield.get(x + 1).get(y).getY();
                        adjacentSections++;

                        if ((y - 1) > 0) // Check up and to the right
                        {
                            sectionsTotal += Heightfield.get(x + 1).get(y - 1).getY();
                            adjacentSections++;
                        }

                        if ((y + 1) < this.surface.get(0).size()) // Check down and to the right
                        {
                            sectionsTotal += Heightfield.get(x + 1).get(y + 1).getY();
                            adjacentSections++;
                        }
                    }

                    if ((y - 1) > 0) // Check above
                    {
                        sectionsTotal += Heightfield.get(x).get(y - 1).getY();
                        adjacentSections++;
                    }

                    if ((y + 1) < this.surface.get(0).size()) // Check below
                    {
                        sectionsTotal += Heightfield.get(x).get(y + 1).getY();
                        adjacentSections++;
                    }
                    this.surface.get(x).set(y, new Vector3D(
                            Heightfield.get(x).get(y).getX(),
                            (Heightfield.get(x).get(y).getY() + (sectionsTotal / adjacentSections)) * 0.5f,
                            Heightfield.get(x).get(y).getZ()
                    ));
                }
            }

            // Overwrite the Heightfield info with our new smoothed info
            for (int x = 0; x < this.surface.size(); x++) {
                for (int y = 0; y < this.surface.get(0).size(); y++) {
                    Heightfield.get(x).set(y, this.surface.get(x).get(y));
                }
            }
        }
    }
//    public void generateSurface(ArrayList<ArrayList<Vector3D>> Heightfield, int smoothFactor) {
//        for (int h = 0; h < smoothFactor; h++) {
//            float sum;
//            float x_pos = cellSideSize;
//            for (int x = 1; x < dimension - 1; x++, x_pos += cellSideSize) {
//                float z_pos = cellSideSize;
//                for (int z = 1; z < dimension - 1; z++, z_pos += cellSideSize) {
//                    sum = (float) (surface.get(x).get(z).getY()
//                            + surface.get(x + 1).get(z).getY()
//                            + surface.get(x - 1).get(z).getY()
//                            + surface.get(x).get(z + 1).getY()
//                            + surface.get(x).get(z - 1).getY()
//                            + surface.get(x + 1).get(z - 1).getY()
//                            + surface.get(x - 1).get(z - 1).getY()
//                            + surface.get(x + 1).get(z + 1).getY()
//                            + surface.get(x - 1).get(z + 1).getY());
//                    surface.get(x).set(z, new Vector3D(x_pos, sum / 9, z_pos));
//                }
//            }
//
//            //Corner smooth
//            x_pos = cellSideSize;
//            for (int x = 1; x < dimension - 1; x++, x_pos += cellSideSize) {
//                sum = (float) (surface.get(x).get(0).getY()
//                        + surface.get(x + 1).get(0).getY()
//                        + surface.get(x - 1).get(0).getY());
//                surface.get(x).set(0, new Vector3D(x_pos, sum / 3, 0));
//
//                sum = (float) (surface.get(x).get(dimension - 1).getY()
//                        + surface.get(x + 1).get(dimension - 1).getY()
//                        + surface.get(x - 1).get(dimension - 1).getY());
//                surface.get(x).set(dimension - 1, new Vector3D(x_pos, sum / 3, (dimension - 1) * cellSideSize));
//            }
//
//            float z_pos = cellSideSize;
//            for (int z = 1; z < dimension - 1; z++, z_pos += cellSideSize) {
//                sum = (float) (surface.get(0).get(z).getY()
//                        + surface.get(0).get(z + 1).getY()
//                        + surface.get(0).get(z - 1).getY());
//                surface.get(0).set(z, new Vector3D(0, sum / 3, z_pos));
//
//                sum = (float) (surface.get(dimension - 1).get(z).getY()
//                        + surface.get(dimension - 1).get(z + 1).getY()
//                        + surface.get(dimension - 1).get(z - 1).getY());
//                surface.get(dimension - 1).set(z, new Vector3D((dimension - 1) * cellSideSize, sum / 3, z_pos));
//            }
//        }
//    }
}
