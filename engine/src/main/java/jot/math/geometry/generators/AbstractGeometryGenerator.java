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
package jot.math.geometry.generators;

import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.math.geometry.PolygonMesh;

/**
 * Abstract class that each geometry generator must implement.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractGeometryGenerator extends PolygonMesh {

    private static final Logger log = getLogger("AbstractGeometryGenerator");

    static {
        log.setLevel(OFF);
    }

    /**
     * This method generates a geometry.
     */
    public abstract void generateGeometry();

    /**
     * Get the geometry length/rows.
     *
     * @return the geometry length/rows.
     */
    public abstract int getGeometryLength();

    /**
     * Get the geometry width/columns.
     *
     * @return the geometry width/columns.
     */
    public abstract int getGeometryWidth();
}
