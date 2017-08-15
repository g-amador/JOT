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

import static com.jogamp.opengl.GL.GL_FRONT_AND_BACK;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.GL2ES1.GL_CLIP_PLANE1;
import static com.jogamp.opengl.GL2GL3.GL_FILL;
import static com.jogamp.opengl.GL2GL3.GL_LINE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import com.jogamp.opengl.glu.GLU;
import static com.jogamp.opengl.glu.GLU.GLU_FILL;
import static com.jogamp.opengl.glu.GLU.GLU_FLAT;
import static com.jogamp.opengl.glu.GLU.GLU_INSIDE;
import com.jogamp.opengl.glu.GLUquadric;
import jot.math.geometry.PolygonMesh;
import static jot.util.CoreOptions.coreOptions;

/**
 * Abstract class that implements a quadric.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class Quadric extends PolygonMesh {

//    static final Logger log = getLogger("Quadric");
//
//    static {
//        log.setLevel(OFF);
//    }
    /**
     * The quadric radius.
     */
    protected float radius;

    /**
     * The display list pointer used to render the quadric.
     */
    protected int listQuadric;

//    /**
//     * The quadric texture.
//     */
//    protected Texture quadricTexture;
    /**
     * The quadric rotation.
     */
    protected float rotation = 0;

    /**
     * The quadric increment to the rotation at each render.
     */
    protected float rotationIncrement = 0;

    /**
     * Toggle On/Off the quadric rotation increment at each render.
     */
    //protected boolean rotate = false;
    /**
     * Toggle On/Off render half quadric.
     */
    protected boolean clipPlane = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GL2 gl) {
        if (this.materials.get(0).isRenderable()) {
            this.materials.get(0).applyMaterialProperties(gl);

            GLU glu = new GLU();
            GLUquadric quadricObject = glu.gluNewQuadric();
            {
                glu.gluQuadricTexture(quadricObject, true);
                glu.gluQuadricDrawStyle(quadricObject, GLU_FILL);
                glu.gluQuadricNormals(quadricObject, GLU_FLAT);
                //glu.gluQuadricOrientation(quadricObject, GLU_OUTSIDE);
                glu.gluQuadricOrientation(quadricObject, GLU_INSIDE);
                final int slices = 16;
                final int stacks = 16;

                gl.glDeleteLists(this.listQuadric, 1);
                this.listQuadric = gl.glGenLists(1);
                gl.glNewList(this.listQuadric, GL_COMPILE);
                {
                    gl.glRotatef(-90, 1, 0, 0);
                    //gl.glRotatef(90, 1, 0, 0);
                    //if (rotate) {
                    //gl.glRotatef(rotation, 1, 0, 0);
                    //gl.glRotatef(rotation, 0, 1, 0);
                    //gl.glRotatef(rotation, 0, 0, 1);
                    //}
                    gl.glColor3f(1.0f, 1.0f, 1.0f);

                    if (coreOptions.get("showTextures")
                            && this.materials.get(0).getTexture() != null) {
                        this.materials.get(0).getTexture().enable(gl);
                        this.materials.get(0).getTexture().bind(gl);
                    }

                    gl.glPolygonMode(GL_FRONT_AND_BACK,
                            coreOptions.get("showWireframe") ? GL_LINE : GL_FILL);

                    glu.gluSphere(quadricObject, this.radius, slices, stacks);

                    if (coreOptions.get("showTextures")
                            && this.materials.get(0).getTexture() != null) {
                        this.materials.get(0).getTexture().disable(gl);
                    }
                }
                gl.glEndList();
            }
            glu.gluDeleteQuadric(quadricObject);

            //if (rotate) {
            //    if (rotation >= 360) {
            //        rotation = 0;
            //    }
            //    rotation += rotationIncrement;
            //}
            if (this.clipPlane) {
                double clip_plane1[] = {0.0, 1.0, 0.0, 0.0};
                gl.glClipPlane(GL_CLIP_PLANE1, clip_plane1, 0);
                gl.glEnable(GL_CLIP_PLANE1);
            }

            gl.glDisable(GL_LIGHTING);
            gl.glCallList(this.listQuadric);
            gl.glEnable(GL_LIGHTING);

            if (this.clipPlane) {
                gl.glDisable(GL_CLIP_PLANE1);
            }
        }
    }
}
