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
 * Class that implements a SkyDome.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class SkyDome extends Quadric {

    static final Logger log = getLogger("SkyDome");

    static {
        log.setLevel(OFF);
    }

    /**
     * Constructor.
     *
     * @param radius the value of the SkyDome radius.
     * @param skyDomeTexture the SkyDome texture.
     */
    public SkyDome(float radius, Texture skyDomeTexture) {
        this.radius = radius;
        this.Id = "SkyDome";
        this.clipPlane = true;
        this.materials = new ArrayList<>();
        this.materials.add(new Material(this.Id));
        this.materials.get(0).setRenderable(true);
        this.materials.get(0).setTexture(skyDomeTexture);
    }
}
