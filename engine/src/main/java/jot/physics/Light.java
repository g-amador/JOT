/*
 * This file is part of the JOT game engine framework toolkit component.
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

import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRONT;
import static com.jogamp.opengl.GL.GL_FRONT_AND_BACK;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_VIEWPORT;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.GL2ES2.GL_DEPTH_COMPONENT;
import static com.jogamp.opengl.GL2GL3.GL_FILL;
import static com.jogamp.opengl.GL2GL3.GL_LINE;
import com.jogamp.opengl.GLAutoDrawable;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_DIFFUSE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_EMISSION;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_POSITION;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SHININESS;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SPECULAR;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW_MATRIX;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION_MATRIX;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.String.format;
import java.nio.FloatBuffer;
import static java.nio.FloatBuffer.allocate;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.math.geometry.Mesh;
import static jot.util.CoreOptions.coreOptions;

/**
 * Class that implements a light source. Based on the shadow maps tutorial
 * available at: http://vitorpamplona.com/wiki/Shadow+Mapping+sem+Shaders
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Light {

    static final Logger log = getLogger("Light");

    static {
        log.setLevel(OFF);
    }

    /**
     * This light id.
     */
    public String lightId;

    /**
     * This light GL id.
     */
    public int GL_LightId;

    private GLU glu;
    private GLUT glut;

    private float lightRadius = 5;
    private float lightAngleInRadians = 0;
    private float lightPos[] = {20, 35, 20};

    private float[] color = {1, 1, 0, 0};

    private double[] lightLimit1;
    private double[] lightLimit2;
    private double[] lightLimit3;
    private double[] lightLimit4;

    private final Mesh lightMesh;

    private int listLight;

    /**
     *
     * Constructor.
     *
     * @param lightId the unique identifier for this light.
     * @param GL_LightId the unique GL identifier for this light.
     * @param lightMesh the mesh associated with this light.
     */
    public Light(String lightId, int GL_LightId, Mesh lightMesh) {
        this.lightId = lightId;
        this.GL_LightId = GL_LightId;
        this.lightMesh = lightMesh;
    }

    /**
     * Get light position.
     *
     * @return value of light position.
     */
    public float[] getLightPosition() {
        return this.lightPos;
    }

    /**
     * Set light position.
     *
     * @param lightPos value of light position.
     */
    public void setLightPosition(float[] lightPos) {
        this.lightPos = lightPos;
    }

    /**
     * Set light color.
     *
     * @param color of light rotation.
     */
    public void setLightColor(float[] color) {
        this.color = color;
    }

    /**
     * Set light radius.
     *
     * @param radius of the light geometry.
     */
    public void setLightRadius(float radius) {
        this.lightRadius = radius;
    }

    /**
     * Update light rotation.
     *
     * @param gl
     * @param signal rotate light around the center of the scene to the left or
     * right side depending if signal is negative or positive.
     */
    public void updateLightRotation(GL2 gl, int signal) {
        log.info(format("Position before rotation: (" + this.lightPos[0] + " " + this.lightPos[1] + " " + this.lightPos[2] + ")"));

        if (signal <= 0) {
            this.lightAngleInRadians += PI / 180;
        } else {
            this.lightAngleInRadians -= PI / 180;
        }

        this.lightPos[0] = (float) (cos(this.lightAngleInRadians) * 120);
        this.lightPos[2] = (float) (sin(this.lightAngleInRadians) * 120);

        gl.glLightfv(this.GL_LightId, GL_POSITION, this.lightPos, 1);
        log.info(format("Position after rotation: (" + this.lightPos[0] + " " + this.lightPos[1] + " " + this.lightPos[2] + ")"));
    }

    /**
     *
     * Update light position.
     *
     * @param gl
     * @param tra_x the translation in X.
     * @param tra_y the translation in Y.
     * @param tra_z the translation in Z.
     */
    public void updateLightPosition(GL2 gl, int tra_x, int tra_y, int tra_z) {
        this.lightPos[0] += tra_x;
        this.lightPos[1] += tra_y;
        this.lightPos[2] += tra_z;

        gl.glLightfv(this.GL_LightId, GL_POSITION, this.lightPos, 0);
        log.info(format("Position after translation: (" + this.lightPos[0] + " " + this.lightPos[1] + " " + this.lightPos[2] + ")"));
    }

    /**
     * Enable and set light components.
     *
     * @param gl
     */
    public void enableLight(GL2 gl) {
        gl.glEnable(this.GL_LightId);

        float lightColorAmb[] = {0.5f, 0.5f, 0.5f, 1.0f};
        float lightColorSpc[] = {0.4f, 0.4f, 0.4f, 1.0f};
        float lightColorDif[] = {1.0f, 1.0f, 1.0f, 1.0f};

        gl.glLightfv(this.GL_LightId, GL_AMBIENT, lightColorAmb, 0);
        gl.glLightfv(this.GL_LightId, GL_DIFFUSE, lightColorDif, 0);
        gl.glLightfv(this.GL_LightId, GL_SPECULAR, lightColorSpc, 0);
        gl.glLightfv(this.GL_LightId, GL_POSITION, this.lightPos, 0);

        //gl.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, lmodel_ambient);
        //gl.glLightModelfv(GL_LIGHT_MODEL_LOCAL_VIEWER, local_view);

        /*Vector3D posLight = new Vector3D(lightPos[0], lightPos[1], lightPos[2]);
         Vector3D orig = new Vector3D(0, 0, 0);
         posLight.calcVector(orig);
        
         float spotLightColour[] = {1.0f,1.0f,1.0f}; //spot light colour
         float[] spotlightDirection = {-posLight.getX(), -posLight.getY(), -posLight.getZ()}; //spot light direction
         gl.glLightf(GL_LightId,GL_SPOT_CUTOFF,5.0f); //spot light cut-off
         gl.glLightfv(GL_LightId,GL_SPOT_DIRECTION,spotlightDirection,0);
         gl.glLightfv(GL_LightId,GL_SPECULAR,spotLightColour,0);
         gl.glLightf(GL_LIGHT1,GL_SPOT_EXPONENT,7.0f); //spot light exponent*/
    }

    /**
     * Disable light.
     *
     * @param gl
     */
    public void disableLight(GL2 gl) {
        gl.glDisable(this.GL_LightId);
    }

    /**
     * Render the light geometry and its influence area.
     *
     * @param gl
     */
    public void renderLight(GL2 gl) {
        if (this.lightMesh != null) {
            this.lightMesh.render(gl);
        } else {

            if (this.glut == null) {
                this.glut = new GLUT();
            }

            gl.glPushMatrix();
            {
                gl.glPolygonMode(GL_FRONT_AND_BACK,
                        coreOptions.get("showWireframe") ? GL_LINE : GL_FILL);

                gl.glDeleteLists(this.listLight, 1);
                this.listLight = gl.glGenLists(1);
                gl.glNewList(this.listLight, GL_COMPILE);
                {
                    gl.glPushMatrix();
                    gl.glTranslatef(this.lightPos[0], this.lightPos[1], this.lightPos[2]);
                    gl.glColor3f(this.color[0], this.color[1], this.color[2]);
                    this.setLightMaterialProperties(gl, this.color[0], this.color[1], this.color[2]);
                    this.glut.glutSolidSphere(this.lightRadius, 15, 15);
                    gl.glPopMatrix();

                    gl.glBegin(GL_LINES);
                    gl.glVertex3f(this.lightPos[0], this.lightPos[1], this.lightPos[2]);
                    gl.glVertex3d(this.lightLimit1[0], this.lightLimit1[1], this.lightLimit1[2]);
                    gl.glVertex3f(this.lightPos[0], this.lightPos[1], this.lightPos[2]);
                    gl.glVertex3d(this.lightLimit2[0], this.lightLimit2[1], this.lightLimit2[2]);
                    gl.glVertex3f(this.lightPos[0], this.lightPos[1], this.lightPos[2]);
                    gl.glVertex3d(this.lightLimit3[0], this.lightLimit3[1], this.lightLimit3[2]);
                    gl.glVertex3f(this.lightPos[0], this.lightPos[1], this.lightPos[2]);
                    gl.glVertex3d(this.lightLimit4[0], this.lightLimit4[1], this.lightLimit4[2]);
                    gl.glEnd();
                }
                gl.glEndList();

                gl.glCallList(this.listLight);
            }
            gl.glPopMatrix();
        }
    }

    /**
     * Get the drawable limits of the scene.
     *
     * @param drawable
     */
    public void getLimitScreen(GLAutoDrawable drawable) {
        this.lightLimit1 = this.getWorldCoord(drawable, 0, 0);
        this.lightLimit2 = this.getWorldCoord(drawable, drawable.getSurfaceWidth() - 1, 0);
        this.lightLimit3 = this.getWorldCoord(drawable, drawable.getSurfaceWidth() - 1, drawable.getSurfaceHeight() - 1);
        this.lightLimit4 = this.getWorldCoord(drawable, 0, drawable.getSurfaceHeight() - 1);
    }

    private void setLightMaterialProperties(GL2 gl, float r, float g, float b) {
        float[] ambient = {r, g, b};
        float[] specular = {r, g, b};
        float[] diffuse = {r, g, b};
        float[] emission = {r, g, b};
        gl.glMaterialfv(GL_FRONT, GL_AMBIENT, ambient, 0);
        gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse, 0);
        gl.glMaterialfv(GL_FRONT, GL_SPECULAR, specular, 0);
        gl.glMaterialfv(GL_FRONT, GL_EMISSION, emission, 0);
        gl.glMaterialf(GL_FRONT, GL_SHININESS, 14.0f);
    }

    private double[] getWorldCoord(GLAutoDrawable drawable, int x, int y) {
        GL2 gl = (GL2) drawable.getGL();
        gl.glViewport(0, 0, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
        double model_view[] = new double[16];
        double projection[] = new double[16];
        int viewport[] = new int[4];
        double x2 = x;
        double wcoord[] = new double[3];//xyz coords
        FloatBuffer winZ = allocate(1);

        gl.glReadPixels(x, y, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, winZ);

        gl.glGetDoublev(GL_MODELVIEW_MATRIX, model_view, 0);
        gl.glGetDoublev(GL_PROJECTION_MATRIX, projection, 0);
        gl.glGetIntegerv(GL_VIEWPORT, viewport, 0);
        double y2 = (double) viewport[3] - y;
        if (this.glu == null) {
            this.glu = new GLU();
        }
        this.glu.gluUnProject(x2, y2, winZ.get(0), model_view, 0, projection, 0, viewport, 0, wcoord, 0);

        return wcoord;
    }
}
