/*
 * This file is part of the JOT game engine geometry extension toolkit 
 * component.
 * Copyright(C) 2014 Gonçalo Amador & Abel Gomes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * E-mail Contacts: G. Amador (g.n.p.amador@gmail.com) & 
 *                  A. Gomes (agomes@it.ubi.pt)
 */
package jot.math.geometry.quadrics;

import com.jogamp.opengl.util.texture.Texture;
import java.util.ArrayList;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.physics.Material;

/**
 * Class that implements a astronomical/celestial object.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class CelestialObject extends Quadric {

    static final Logger log = getLogger("CelestialObject");

    static {
        log.setLevel(OFF);
    }

    /**
     * Constructor.
     *
     * @param rotationIncrement the rotation to increment at each render.
     * @param radius the value of the astronomical/celestial object radius.
     * @param celestialObjectTexture the astronomical/celestial texture.
     */
    public CelestialObject(float rotationIncrement, float radius,
            Texture celestialObjectTexture) {
        this.rotationIncrement = rotationIncrement;
        this.radius = radius;
        this.materials = new ArrayList<>();
        this.materials.add(new Material());
        this.materials.get(0).setRenderable(true);
        this.materials.get(0).setTexture(celestialObjectTexture);
        //rotate = true;
    }

    /**
     * Constructor.
     *
     * @param Id this celestial object Id.
     * @param rotationIncrement the rotation to increment at each render.
     * @param radius the value of the astronomical/celestial object radius.
     * @param celestialObjectTexture the astronomical/celestial texture.
     */
    public CelestialObject(String Id, float rotationIncrement, float radius,
            Texture celestialObjectTexture) {
        this.Id = Id;
        this.rotationIncrement = rotationIncrement;
        this.radius = radius;
        this.materials = new ArrayList<>();
        this.materials.add(new Material(Id));
        this.materials.get(0).setRenderable(true);
        this.materials.get(0).setTexture(celestialObjectTexture);
        //rotate = true;
    }
}
