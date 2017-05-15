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
package jot.physics;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import jot.math.geometry.Renderable;

/**
 * Abstract class that a material class or classes must implement, in
 * RayTracers/RayCsasters this class might be extended.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractMaterial implements Renderable {

    /**
     * Get the material alpha property.
     *
     * @return the value of the alpha property.
     */
    public abstract float getD();

    /**
     * Set the material alpha property.
     *
     * @param d value of the alpha property.
     */
    public abstract void setD(float d);

    /**
     * Get the shininess color.
     *
     * @return the value of the shininess property.
     */
    public abstract float getNs();

    /**
     * Set the shininess color.
     *
     * @param ns value of the shininess property.
     */
    public abstract void setNs(float ns);

    /**
     * Get the ambient color.
     *
     * @return the values of the ambient color.
     */
    public abstract float[] getKa();

    /**
     * Set the ambient color.
     *
     * @param ka values of the ambient color.
     */
    public abstract void setKa(float[] ka);

    /**
     * Get the specular color.
     *
     * @return the values of the specular color.
     */
    public abstract float[] getKs();

    /**
     * Set the specular color.
     *
     * @param ks values of the specular color.
     */
    public abstract void setKs(float[] ks);

    /**
     * Get the diffuse color.
     *
     * @return the values of the diffuse color.
     */
    public abstract float[] getKd();

    /**
     * Set the diffuse color.
     *
     * @param kd values of the diffuse color.
     */
    public abstract void setKd(float[] kd);

    /**
     * Get the emission color.
     *
     * @return the values of the emission the color.
     */
    public abstract float[] getE();

    /**
     * Set the emission color.
     *
     * @param e values of the emission the color.
     */
    public abstract void setE(float[] e);

    /**
     * Get the color property.
     *
     * @return the values of this material color.
     */
    public abstract float[] getColor();

    /**
     * Set the color property.
     *
     * @param color the values of the color property.
     */
    public abstract void setColor(float[] color);

    /**
     *
     * Get the texture of this material.
     *
     * @return the texture of this material.
     */
    public abstract Texture getTexture();

    /**
     * Set the texture of this material.
     *
     * @param texture the texture to set to this material.
     */
    public abstract void setTexture(Texture texture);

//    /**
//     * Set the texture filter.
//     *
//     * @param gl
//     * @param tf the texture filter to set for this material texture.
//     */
//    public abstract void setTextureFilter(GL2 gl, TextureFilter tf);
//
    /**
     * Apply the material property.
     *
     * @param gl
     */
    public abstract void applyMaterialProperties(GL2 gl);

    /**
     * Show this material properties.
     */
    public abstract void showMaterial();

    /**
     * Test if a material is opaque. By default all materials are opaque
     * specialized materials must be extended from material.
     *
     * @return TRUE if material is opaque, FALSE otherwise.
     */
    public abstract boolean isOpaque();

//    /**
//     * The types of texture filters.
//     */
//    public enum TextureFilter {
//
//        NEAREST, LINEAR, MIPMAP
//    }
}
