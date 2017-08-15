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

import static com.jogamp.opengl.GL.GL_FRONT_AND_BACK;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_DIFFUSE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_EMISSION;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SHININESS;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SPECULAR;
import com.jogamp.opengl.util.texture.Texture;
import static java.lang.Float.NaN;
import static java.lang.String.format;
import java.util.Arrays;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * Class that implements material properties, in RayTracers/RayCsasters this
 * class might be extended.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Material extends AbstractMaterial {

    static final Logger log = getLogger("Material");

    static {
        log.setLevel(OFF);
    }

    /**
     * This material identifier.
     */
    private String Id;

    /**
     * This material alpha.
     */
    private float d;

    /**
     * This material shininess component.
     */
    private float ns;

    /**
     * This material ambient component.
     */
    private float[] ka;

    /**
     * This material specular component.
     */
    private float[] ks;

    /**
     * This material diffuse component.
     */
    private float[] kd;

    /**
     * This material emission component.
     */
    private float[] e;

    /**
     * This material color.
     */
    private float[] color;

    /**
     * This material associated texture.
     */
    private Texture texture;

    /**
     * This material associated texture filter.
     */
    //TextureFilter tf;
    //
    /**
     * Toggle on/off geometries associated to this material to be renderable.
     * Default false.
     */
    private boolean renderable = false;

    /**
     * Constructor.
     */
    public Material() {
        this.Id = "";
        this.d = 1.0f;
        this.ns = NaN; //0.0f; // 10;
        this.ka = null; //new float[]{1, 1, 1, 0};
        this.ks = null; //new float[]{1, 1, 1, 0};
        this.kd = null; //new float[]{1, 1, 1, 0};
        this.e = null; //new float[]{1, 1, 1, 0};
        this.color = new float[]{1, 1, 1, this.d};
        //tf = MIPMAP;
    }

    /**
     * Constructor.
     *
     * @param Id the material identifier.
     */
    public Material(String Id) {
        this.Id = Id;
        this.d = 1.0f;
        this.ns = NaN; //0.0f; // 10;
        this.ka = null; //new float[]{1, 1, 1, 0};
        this.ks = null; //new float[]{1, 1, 1, 0};
        this.kd = null; //new float[]{1, 1, 1, 0};
        this.e = null; //new float[]{1, 1, 1, 0};
        this.color = new float[]{1, 1, 1, this.d};
        //tf = MIPMAP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getD() {
        return this.d;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setD(float d) {
        this.d = d;
        if (this.color.length == 4) {
            this.color[3] = d;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getNs() {
        return this.ns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNs(float ns) {
        this.ns = ns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float[] getKa() {
        return this.ka;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setKa(float[] ka) {
        this.ka = ka;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float[] getKs() {
        return this.ks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setKs(float[] ks) {
        this.ks = ks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float[] getKd() {
        return this.kd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setKd(float[] kd) {
        this.kd = kd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float[] getE() {
        return this.e;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setE(float[] e) {
        this.e = e;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float[] getColor() {
        return this.color;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColor(float[] color) {
        this.color = color;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Texture getTexture() {
        return this.texture;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTexture(Texture texture) {
        this.texture = texture;
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void setTextureFilter(GL2 gl, TextureFilter tf) {
//        this.tf = tf;
//    }
//    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setRenderable(boolean renderable) {
        this.renderable = renderable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyMaterialProperties(GL2 gl) {
        if (this.renderable) {
            if (this.ka != null) {
                gl.glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, this.ka, 0);
            }

            if (this.kd != null) {
                gl.glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, this.kd, 0);
            }

            if (this.ks != null) {
                gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, this.ks, 0);
            }

            if (this.e != null) {
                gl.glMaterialfv(GL_FRONT_AND_BACK, GL_EMISSION, this.e, 0);
            }

            if (this.ns != NaN) {
                gl.glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, this.ns);
            }

//            switch (tf) {
//                case NEAREST:
//                    // Nearest filter is least compute-intensive
//                    // Use nearer filter if image is larger than the original texture
//                    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
//                    // Use nearer filter if image is smaller than the original texture
//                    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
//                    break;
//                case LINEAR:
//                    // Linear filter is more compute-intensive
//                    // Use linear filter if image is larger than the original texture
//                    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//                    // Use linear filter if image is smaller than the original texture
//                    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//                    break;
//                case MIPMAP:
//                    // Use mipmap filter if the image is smaller than the texture
//                    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//                    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
//                            GL_LINEAR_MIPMAP_NEAREST);
//                    break;
//                default:
//                    throw new AssertionError(tf.name());
//            }
            this.showMaterial();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showMaterial() {
        if (this.renderable) {
            log.info(this.Id);
            log.info(format("color: " + Arrays.toString(this.color)));
            log.info(format("ambient: " + Arrays.toString(this.ka)));
            log.info(format("diffuse: " + Arrays.toString(this.kd)));
            log.info(format("specular: " + Arrays.toString(this.ks)));
            log.info(format("emission: " + Arrays.toString(this.e)));
            log.info(format("shininess: " + this.ns));
            //log.info(format("texture filter: " + tf.toString()));
            log.info("\n");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpaque() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRenderable() {
        return this.renderable;
    }
}
