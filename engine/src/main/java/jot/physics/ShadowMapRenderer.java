/*
 * This file is part of the JOT game engine lightning framework toolkit
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
package jot.physics;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_POLYGON_OFFSET_FILL;
import static com.jogamp.opengl.GL.GL_TEXTURE;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPARE_R_TO_TEXTURE;
import static com.jogamp.opengl.GL2.GL_DEPTH_TEXTURE_MODE;
import static com.jogamp.opengl.GL2.GL_ENABLE_BIT;
import static com.jogamp.opengl.GL2.GL_EYE_LINEAR;
import static com.jogamp.opengl.GL2.GL_EYE_PLANE;
import static com.jogamp.opengl.GL2.GL_INTENSITY;
import static com.jogamp.opengl.GL2.GL_LIGHTING_BIT;
import static com.jogamp.opengl.GL2.GL_Q;
import static com.jogamp.opengl.GL2.GL_R;
import static com.jogamp.opengl.GL2.GL_S;
import static com.jogamp.opengl.GL2.GL_T;
import static com.jogamp.opengl.GL2.GL_TEXTURE_BIT;
import static com.jogamp.opengl.GL2.GL_TEXTURE_GEN_MODE;
import static com.jogamp.opengl.GL2.GL_TEXTURE_GEN_Q;
import static com.jogamp.opengl.GL2.GL_TEXTURE_GEN_R;
import static com.jogamp.opengl.GL2.GL_TEXTURE_GEN_S;
import static com.jogamp.opengl.GL2.GL_TEXTURE_GEN_T;
import static com.jogamp.opengl.GL2.GL_TRANSFORM_BIT;
import static com.jogamp.opengl.GL2.GL_VIEWPORT_BIT;
import static com.jogamp.opengl.GL2ES2.GL_CLAMP_TO_BORDER;
import static com.jogamp.opengl.GL2ES2.GL_DEPTH_COMPONENT;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_COMPARE_FUNC;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_COMPARE_MODE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_FLAT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW_MATRIX;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION_MATRIX;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_TEXTURE_MATRIX;
import java.nio.FloatBuffer;
import static java.nio.FloatBuffer.allocate;
import static java.nio.FloatBuffer.wrap;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * Class that implements shadows setup source. Based on the shadow maps tutorial
 * available at: http://vitorpamplona.com/wiki/Shadow+Mapping+sem+Shaders
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class ShadowMapRenderer {

    static final Logger log = getLogger("ShadowMapRenderer");

    static {
        log.setLevel(OFF);
    }

    private int[] shadowMapTexture = new int[1];
    private int shadowMapResolutionWidth;
    private int shadowMapResolutionHeight;
    private final FloatBuffer textureTrasnformS;
    private final FloatBuffer textureTrasnformT;
    private final FloatBuffer textureTrasnformR;
    private final FloatBuffer textureTrasnformQ;

    /**
     *
     * Constructor
     *
     * @param resolutionWidth of the shadowMap texture.
     * @param resolutionHeight of the shadowMap texture.
     */
    public ShadowMapRenderer(int resolutionWidth, int resolutionHeight) {
        this.shadowMapTexture = null;
        this.shadowMapResolutionWidth = resolutionWidth;
        this.shadowMapResolutionHeight = resolutionHeight;
        this.textureTrasnformS = allocate(4);
        this.textureTrasnformT = allocate(4);
        this.textureTrasnformR = allocate(4);
        this.textureTrasnformQ = allocate(4);
    }

    /**
     * Set the resolution of the shadowmMap.
     *
     * @param resolutionWidth of the shadowMap texture.
     * @param resolutionHeight of the shadowMap texture.
     */
    public void setShadowMapResolution(int resolutionWidth, int resolutionHeight) {
        this.shadowMapResolutionWidth = resolutionWidth;
        this.shadowMapResolutionHeight = resolutionHeight;
    }

    /**
     * Activate depth capture texture generation.
     *
     * @param gl
     */
    public void enableDepthCapture(GL2 gl) {
        //Protects the code that precedes this function.
        gl.glPushAttrib(GL_ENABLE_BIT | GL_TEXTURE_BIT | GL_LIGHTING_BIT | GL_VIEWPORT_BIT | GL_COLOR_BUFFER_BIT);

        //If the depth texture was not generated yet do so.
        if (this.shadowMapTexture == null) {
            this.createDepthTexture(gl);
        }

        //Set the viewport to the same size of the texture. The size of the
        //viewport cannot be higher than the size of the screen, otherwise
        //offline rendering and FBOs should be used instead.
        gl.glViewport(0, 0, this.shadowMapResolutionWidth, this.shadowMapResolutionHeight);

        //Calculate the space transform of the camera into the light space and
        //stores the transformation to be used in the shadow rendering test.
        this.loadTextureTransform(gl);

        //Enable Offset in order to avoid flickering and dislocate the height
        //map 1.9 times + 4.00 backward.
        gl.glPolygonOffset(1.9f, 4.00f);
        gl.glEnable(GL_POLYGON_OFFSET_FILL);

        //Flat shading for speed.
        gl.glShadeModel(GL_FLAT);

        //Disable Lighting for performance.  
        gl.glDisable(GL_LIGHTING);

        //Write solely on the depth buffer not in the color buffer.
        gl.glColorMask(false, false, false, false);
    }

    /**
     * Returns to the previous OpenGL state and copies the DepthBuffer into the
     * already created texture.
     *
     * @param gl
     */
    public void disableDepthCapture(GL2 gl) {
        //Copies the Depth buffer into the texture.   
        gl.glBindTexture(GL_TEXTURE_2D, this.shadowMapTexture[0]);

        //SubTexture does not reallocate hole texture, as glCopyTexImage2D.
        gl.glCopyTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 0, 0, this.shadowMapResolutionWidth, this.shadowMapResolutionHeight);

        //Clean the Depth buffer.
        gl.glClear(GL_DEPTH_BUFFER_BIT);

        //Returns to the confifurations before the depthCapture.
        gl.glPopAttrib();
    }

    /**
     * Activates the automatic and linear generation of the texture coordinates
     * used to consult the depth texture and to repass the transformation matrix
     * dismembered to OpenGL.
     *
     * @param gl
     */
    public void enableShadowTest(GL2 gl) {
        //Protects the code before this method.
        gl.glPushAttrib(GL_TEXTURE_BIT | GL_ENABLE_BIT);

        //Enables the automatic generation of texture coordinates from the
        //camera point of view.
        gl.glTexGeni(GL_S, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        gl.glTexGeni(GL_T, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        gl.glTexGeni(GL_R, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        gl.glTexGeni(GL_Q, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);

        //Applies the transformation of these coordinates into the light space.
        gl.glTexGenfv(GL_S, GL_EYE_PLANE, this.textureTrasnformS);
        gl.glTexGenfv(GL_T, GL_EYE_PLANE, this.textureTrasnformT);
        gl.glTexGenfv(GL_R, GL_EYE_PLANE, this.textureTrasnformR);
        gl.glTexGenfv(GL_Q, GL_EYE_PLANE, this.textureTrasnformQ);

        //Activates.  
        gl.glEnable(GL_TEXTURE_GEN_S);
        gl.glEnable(GL_TEXTURE_GEN_T);
        gl.glEnable(GL_TEXTURE_GEN_R);
        gl.glEnable(GL_TEXTURE_GEN_Q);

        //Bind & enable shadow map texture.
        gl.glEnable(GL_TEXTURE_2D);
        gl.glBindTexture(GL_TEXTURE_2D, this.shadowMapTexture[0]);
    }

    /**
     * Return to the previous program configurations.
     *
     * @param gl
     */
    public void disableShadowTest(GL2 gl) {
        gl.glPopAttrib();
    }

    /**
     * Create a repository for the Depth Buffer.
     *
     * @param gl
     */
    private void createDepthTexture(GL2 gl) {
        //Create the shadow map texture.
        this.shadowMapTexture = new int[1];
        gl.glGenTextures(1, this.shadowMapTexture, 0);
        gl.glBindTexture(GL_TEXTURE_2D, this.shadowMapTexture[0]);

        //Setup linear filtering and warping.
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); //GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR); //GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER); //GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER); //GL_CLAMP_TO_EDGE);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, this.shadowMapResolutionWidth, this.shadowMapResolutionHeight, 0,
                GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, null);

        //Enable shadow comparison.
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE);

        //Shadow comparison should be true, i.e., not in shadow if r<=texture.
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        //Shadow comparison should generate an INTENSITY result.
        gl.glTexParameteri(GL_TEXTURE_2D, GL_DEPTH_TEXTURE_MODE, GL_INTENSITY);
    }

    /**
     * Capture the transform matrixes for the light coordinate system and
     * prepare them to be used in the second step.
     *
     * @param gl
     */
    private void loadTextureTransform(GL2 gl) {
        FloatBuffer lightProjectionMatrix = allocate(16);
        FloatBuffer lightViewMatrix = allocate(16);

        //Search the light view and projection matrixes.  
        gl.glGetFloatv(GL_PROJECTION_MATRIX, lightProjectionMatrix);
        gl.glGetFloatv(GL_MODELVIEW_MATRIX, lightViewMatrix);

        //Save the state of the matrix mode. 
        gl.glPushAttrib(GL_TRANSFORM_BIT);
        gl.glMatrixMode(GL_TEXTURE);
        gl.glPushMatrix();
        {
            //Calculate texture matrix for projection.  
            //This matrix takes us from eye space to the light's clip space.  
            //It is postmultiplied by the inverse of the current view matrix when 
            //specifying texgen.  
            float tempBias[] = {0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.5f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f}; //bias from [-1, 1] to [0, 1].

            FloatBuffer biasMatrix = wrap(tempBias);
            FloatBuffer textureMatrix = allocate(16);

            //Apply the 3 matrixes in one, taking a 3D fragment to the camera
            //canonical space.
            gl.glLoadMatrixf(biasMatrix);
            gl.glMultMatrixf(lightProjectionMatrix);
            gl.glMultMatrixf(lightViewMatrix);
            gl.glGetFloatv(GL_TEXTURE_MATRIX, textureMatrix);

            //Separate the colons distinct in arrays because of OpenGL.
            for (int i = 0; i < 4; i++) {
                this.textureTrasnformS.put(i, textureMatrix.get(i * 4));
                this.textureTrasnformT.put(i, textureMatrix.get(i * 4 + 1));
                this.textureTrasnformR.put(i, textureMatrix.get(i * 4 + 2));
                this.textureTrasnformQ.put(i, textureMatrix.get(i * 4 + 3));
            }
        }
        gl.glPopMatrix();
        gl.glPopAttrib();
    }
}
