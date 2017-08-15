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
package jot.gui;

import static com.jogamp.opengl.GL.GL_ALWAYS;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_DST_COLOR;
import static com.jogamp.opengl.GL.GL_EQUAL;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_KEEP;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_LUMINANCE;
import static com.jogamp.opengl.GL.GL_NICEST;
import static com.jogamp.opengl.GL.GL_ONE;
import static com.jogamp.opengl.GL.GL_ONE_MINUS_DST_COLOR;
import static com.jogamp.opengl.GL.GL_ONE_MINUS_SRC_COLOR;
import static com.jogamp.opengl.GL.GL_REPLACE;
import static com.jogamp.opengl.GL.GL_SRC_COLOR;
import static com.jogamp.opengl.GL.GL_STENCIL_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_STENCIL_TEST;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_VIEWPORT;
import static com.jogamp.opengl.GL.GL_ZERO;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static com.jogamp.opengl.GL2ES2.GL_CONSTANT_COLOR;
import static com.jogamp.opengl.GL2ES2.GL_DEPTH_COMPONENT;
import static com.jogamp.opengl.GL2ES2.GL_ONE_MINUS_CONSTANT_COLOR;
import com.jogamp.opengl.GLAutoDrawable;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_NORMALIZE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_POSITION;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.gl2.GLUT;
import static java.lang.System.exit;
import static java.lang.System.gc;
import static java.lang.System.nanoTime;
import static java.lang.Thread.sleep;
import java.nio.FloatBuffer;
import static java.nio.FloatBuffer.allocate;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.gui.camera.Camera;
import jot.manager.AssetManager;
import jot.manager.SceneManager;
import static jot.manager.SceneManager.getSceneSize;
import static jot.manager.SceneManager.localAssetManager;
import static jot.math.geometry.Transformations.getShearYMatrix;
import jot.physics.Light;
import jot.physics.ShadowMapRenderer;
import static jot.util.CoreOptions.coreOptions;
import static jot.util.FrameworkOptions.frameworkOptions;

/**
 * Abstract Class that performs game render and window management tasks, i.e.,
 * implements JOGL and frame/window methods. Based on source available at (last
 * consulted on 29 - 07 - 2012):
 * http://www3.ntu.edu.sg/home/ehchua/programming/java/J8d_Game_Framework.html
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class Game extends SimpleGame {

    private static final Logger log = getLogger("Game");

    /**
     * The Light to use.
     */
    public static Light defaultLight;

    /**
     * The Camera2 to use.
     */
    public static Camera defaultCamera;

    /**
     * Current state of the game.
     */
    protected static State state;

    /**
     * FPS counter object.
     */
    protected static FPSCounter fps;

    static {
        log.setLevel(OFF);
    }

    protected SceneManager sceneManager = null;
    protected AssetManager assetManager = localAssetManager;

    protected final GLU glu = new GLU();
    protected final GLUT glut = new GLUT();

    protected float[] clearColor;

    public ShadowMapRenderer shadowMapRenderer = new ShadowMapRenderer(this.GAME_CANVAS_WIDTH, this.GAME_CANVAS_HEIGHT);

    //private GLUT glut;
    private double GAME_UPDATE_PERIOD; //seconds.

    private double nextTime;
    private int skippedFrames;
    private final double maxTimeDiff = 0.5;
    private final int maxSkippedFrames = 5;

    //float[] clearColor = [0]
    /**
     * Constructor.
     *
     */
    public Game() {
        super();
        //assetManager = new AssetManager();
    }

    /**
     * Constructor, receives the name of the application window.
     *
     * @param gameName the application window name.
     */
    public Game(String gameName) {
        super(gameName);
        //assetManager = new AssetManager();
    }

    /**
     * Constructor, receives the CANVAS_WIDTH and the CANVAS_HEIGHT.
     *
     * @param GAME_CANVAS_WIDTH of the window.
     * @param GAME_CANVAS_HEIGHT of the window.
     */
    public Game(int GAME_CANVAS_WIDTH, int GAME_CANVAS_HEIGHT) {
        super(GAME_CANVAS_WIDTH, GAME_CANVAS_HEIGHT);
        //assetManager = new AssetManager();
    }

    /**
     * Constructor, receives the name of the application window, the
     * CANVAS_WIDTH, and the CANVAS_HEIGHT.
     *
     * @param gameName the application window name.
     * @param GAME_CANVAS_WIDTH of the window.
     * @param GAME_CANVAS_HEIGHT of the window.
     */
    public Game(String gameName, int GAME_CANVAS_WIDTH, int GAME_CANVAS_HEIGHT) {
        super(gameName, GAME_CANVAS_WIDTH, GAME_CANVAS_HEIGHT);
        //assetManager = new AssetManager();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        log.info("Init.");
        gl = (GL2) drawable.getGL();

        //Create informative text box
        text = new Text(drawable, 18);
        text.setColor(1, 0, 0, 1);

        try {
            synchronized (this) {
                this.gameInit();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        gl.setSwapInterval(1);

        //Shading states
        gl.glShadeModel(GL_SMOOTH);
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        //Depth states
        gl.glClearDepth(1.0f); //Maximum depth buffer clean range
        gl.glDepthFunc(GL_LEQUAL);
        gl.glEnable(GL_DEPTH_TEST); //Enables depth test using DepthBuffer

        //gl.glEnable(GL_CULL_FACE); //Enables Shape culling
        //gl.glCullFace(GL_BACK); //Back-Shape Culling, i.e., cuts from the rendered scene faces whose normal is not pointing in the camera direction.
        //
        //Illumination
        gl.glEnable(GL_LIGHTING);
        gl.glEnable(GL_NORMALIZE);
        //gl.glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT);
        //gl.glColorMaterial(GL_FRONT_AND_BACK, GL_DIFFUSE);
        //gl.glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
        //gl.glColorMaterial(GL_FRONT_AND_BACK, GL_SPECULAR); 
        //gl.glColorMaterial(GL_FRONT_AND_BACK, GL_EMISSION); //Activated in the example
        //gl.glColorMaterial(GL_FRONT_AND_BACK, GL_SHININESS);
        //gl.glEnable(GL_COLOR_MATERIAL);      

        //TODO: clean stuff in not necessary 
////------------------------------------------------------------------------------
//        //Enable texturing so we can bind our frame buffer texture.
//        gl.glEnable(GL_TEXTURE_2D);
//
//        initFrameBuffer(); // Create our frame buffer object
////------------------------------------------------------------------------------
        this.nextTime = nanoTime() / 1_000_000_000.0;
        this.skippedFrames = 1;
        //lag = 0;
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        log.info("Display.");
        //gl = (GL2) drawable.getGL();

        if (fps == null && this.animator != null) {
            //Create the FPS counter
            fps = new FPSCounter(drawable, this.animator, 36);
            //fps = new FPSCounter(drawable, fpsAnimator, 36);
            fps.setColor(1, 0, 0, 1);
        }

        // convert the time to seconds
        double currTime = nanoTime() / 1_000_000_000.0;
        if ((currTime - this.nextTime) > this.maxTimeDiff) {
            this.nextTime = currTime;
        }
        if (currTime >= this.nextTime) {
            // assign the time for the next update
            this.nextTime += this.GAME_UPDATE_PERIOD;
            synchronized (this) {
                this.gameProcessInput();
                this.gameUpdate((float) this.GAME_UPDATE_PERIOD);
            }
            if ((currTime < this.nextTime) || (this.skippedFrames > this.maxSkippedFrames)) {
                //Render
                {
                    if (frameworkOptions.get("useLights")) {
                        //System.out.println("Lights On");
                        log.info("Lights On");
                        gl.glEnable(GL_LIGHTING);
                    } else {
                        //System.out.println("Lights Off");
                        log.info("Lights Off");
                        gl.glDisable(GL_LIGHTING);
                    }

                    //Clear the background of our window.    
                    if (this.clearColor == null) {
                        //default clear color white
                        this.clearColor = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
                    }
                    gl.glClearColor(this.clearColor[0], this.clearColor[1], this.clearColor[2], this.clearColor[3]);

                    //if (Floor) {
                    //gl.glClearColor(
                    //        clearColorPitchblack[0],
                    //        clearColorPitchblack[1],
                    //        clearColorPitchblack[2],
                    //        clearColorPitchblack[3]);
                    //} else {
                    //    gl.glClearColor(
                    //            clearColorWhite[0],
                    //            clearColorWhite[1],
                    //            clearColorWhite[2],
                    //            clearColorWhite[3]);
                    //}
                    //Clean the buffers.
                    gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                    //TODO: alternative to FPS class replace when properly implemented or removed awt dependencies in FPScounter
//                    float time = drawable.getAnimator().getLastFPS();
//                    gl.glPushMatrix();
//                    gl.glMatrixMode(GL2.GL_PROJECTION);
//                    gl.glPushMatrix();
//                    gl.glLoadIdentity();
//                    int[] viewport = new int[4];
//                    gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
//                    glu.gluOrtho2D(0, viewport[2], viewport[3], 0);
//                    gl.glDepthFunc(GL2.GL_ALWAYS);
//                    gl.glColor4f(1, 0, 0, 1);
//                    gl.glRasterPos2d(30, 30);
//                    glut.glutBitmapString(BITMAP_HELVETICA_18, "FPS: " + time);
//                    gl.glDepthFunc(GL2.GL_LESS);
//                    gl.glPopMatrix();
//                    gl.glMatrixMode(GL2.GL_MODELVIEW);
//                    gl.glPopMatrix();
//
//                    gl.glPushMatrix();
//                    gl.glColor3f(1, 0, 0);
//                    gl.glRasterPos2d(-185.f, -6.f);
//                    glut.glutBitmapString(BITMAP_HELVETICA_18, "FPS: " + time);
//                    gl.glFlush();
//                    gl.glPopMatrix();
                    //TODO: clean stuff in not necessary
////------------------------------------------------------------------------------                
//          if (false) {
//                glSetupWindow(false, zNear, null);
//
//                //Enable texture flip, since all loaded textures are upside down.
//                enableTextureTransforms();
//
//                if (defaultCamera != null) {
//                    defaultCamera.renderCamera(null);
//                }
//
//                if (useLights) {
//                    gl.glEnable(GL_LIGHT0);
//                    gl.glLightfv(GL_LIGHT0, GL_POSITION, new float[]{0, 15, 0, 0}, 0);
//                    gl.glLightfv(GL_LIGHT0, GL_AMBIENT, new float[]{1, 1, 1, 0}, 0);
//                }
//
//                gl.glClearStencil(0);
//                gl.glClear(GL_STENCIL_BUFFER_BIT);
//
//                gl.glEnable(GL_STENCIL_TEST); //Enable the stencil testing.
//                {
//                    //Render the mesh into the stencil buffer.
//                    {
//                        gl.glStencilFunc(GL_ALWAYS, 1, -1);
//                        gl.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
//
//                        gl.glLineWidth(1.0f);
//                        renderGeometries(false, false, false, false); //Draw geometries excluding the floor and sky.
//                    }
//
//                    //Render the thick wireframe version.
//                    {
//                        gl.glStencilFunc(GL_NOTEQUAL, 1, -1);
//                        gl.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
//
//                        if (useLights) {
//                            gl.glDisable(GL_LIGHTING);
//                        }
//                        gl.glLineWidth(3.0f);
//                        gl.glColor4f(1, 0, 0, 1);
//                        renderGeometries(true, false, false, false); //Draw geometries excluding the floor and sky.
//                        if (useLights) {
//                            gl.glEnable(GL_LIGHTING);
//                        }
//                    }
//                }
//                gl.glDisable(GL_STENCIL_TEST); //Disable the stencil testing.
//
//                //Disable texture flip, since all loaded textures are upside down.
//                disableTextureTransforms();
//            } else 
////------------------------------------------------------------------------------                
                    if (frameworkOptions.get("showPlanarShadows")) {
                        //FIXME: for multiple light sources/shadows won't work.
                        //Reshape Window and set gluLook.
                        this.glSetupWindow(true, this.zNear, null);

                        //Enable texture flip, since all loaded textures are upside down.
                        this.enableTextureTransforms();

                        //Configure camera normal.
                        defaultLight.getLimitScreen(drawable);

                        //Enable light.
                        defaultLight.enableLight(gl);

                        //Clear stencil buffer.
                        gl.glClearStencil(0);
                        gl.glClear(GL_STENCIL_BUFFER_BIT);

//start
                        gl.glColorMask(false, false, false, false); //Disable the color mask.
                        gl.glDepthMask(false); //Disable the depth mask.

                        gl.glEnable(GL_STENCIL_TEST); //Enable the stencil testing.
                        {
                            gl.glStencilFunc(GL_ALWAYS, 1, 0xFFFF_FFFF);
                            gl.glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE); //Set the stencil buffer to replace our next lot of data.

                            //Draw floor only and set the data plane to be replaced.
                            this.renderFloor(false);

                            gl.glColorMask(true, true, true, true); //Enable the color mask.
                            gl.glDepthMask(true); //Enable the depth mask.

                            gl.glStencilFunc(GL_EQUAL, 1, 0xFFFF_FFFF);
                            gl.glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP); //Set the stencil buffer to keep our next lot of data.

                            if (coreOptions.get("showTextures")) {
                                gl.glDisable(GL_TEXTURE_2D); //Disable texturing of the shadow.
                            }
                            gl.glDisable(GL_DEPTH_TEST); //Disable depth testing of the shadow.

                            //Draw shadows.
                            {
                                gl.glDisable(GL_LIGHTING);
                                gl.glPushMatrix();
                                gl.glScalef(1.0f, 0.0f, 1.0f); //Flip the shadow vertically and make it flat.

                                //Shear the shadow in the oposity direction of the light source.
                                float[] lightPos = new float[3];
                                lightPos[0] = defaultLight.getLightPosition()[0] / getSceneSize();
                                lightPos[1] = defaultLight.getLightPosition()[1] / getSceneSize();
                                lightPos[2] = defaultLight.getLightPosition()[2] / getSceneSize();
                                gl.glMultMatrixf(getShearYMatrix(-lightPos[0] / lightPos[1], -lightPos[2] / lightPos[1]), 0);
                                gl.glColor4f(0, 0, 0, 0.9f); //Color the shadow black.

                                //Draw geometries excluding the floor and sky.
                                this.renderGeometries(false, false, false, false);

                                gl.glPopMatrix();
                                gl.glEnable(GL_LIGHTING);
                            }

                            gl.glEnable(GL_DEPTH_TEST); //Enable depth testing-
                            if (coreOptions.get("showTextures")) {
                                gl.glEnable(GL_TEXTURE_2D); //Enable texturing-
                            }
                        }
                        gl.glDisable(GL_STENCIL_TEST); //Disable the stencil testing.
//end              
                        //TODO: clean stuff in not necessary
////------------------------------------------------------------------------------
//                gl.glClearStencil(0);
//                gl.glClear(GL_STENCIL_BUFFER_BIT);
//
//                gl.glEnable(GL_STENCIL_TEST); //Enable the stencil testing.
//                {
//                    //Render the mesh into the stencil buffer.
//                    {
//                        gl.glStencilFunc(GL_ALWAYS, 1, -1);
//                        gl.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
//
//                        gl.glLineWidth(1.0f);
//                        gl.glColor4f(0, 0, 0, 0.5f); //Color the shadow black.
//                        {
//                            //Draw floor only and set the data plane to be replaced.
//                            renderFloor(false);
//                        }
//                    }
//
//                    //Render the thick wireframe version.
//                    {
//                        gl.glStencilFunc(GL_NOTEQUAL, 1, -1);
//                        gl.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
//
//                        gl.glLineWidth(3.0f);
//                        gl.glColor4f(1, 0, 0, 0.9f);
//                        {
//                            //Draw floor only and set the data plane to be replaced.
//                            renderFloor(true);
//                        }
//                    }
//                }
//                gl.glDisable(GL_STENCIL_TEST); //Disable the stencil testing.
////------------------------------------------------------------------------------
////------------------------------------------------------------------------------
//                gl.glClearStencil(0);
//                gl.glClear(GL_STENCIL_BUFFER_BIT);
//
//                gl.glEnable(GL_STENCIL_TEST); //Enable the stencil testing.
//                {
//                    //Render the mesh into the stencil buffer.
//                    {
//                        gl.glStencilFunc(GL_ALWAYS, 1, -1);
//                        gl.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
//
//                        if (useLights) {
//                            gl.glDisable(GL_LIGHTING);
//                        }
//                        gl.glLineWidth(1.0f);
//                        gl.glColor4f(0, 0, 0, 0.5f); //Color the shadow black.
//                        {
//                            gl.glPushMatrix();
//                            gl.glScalef(1.0f, 0.0f, 1.0f); //Flip the shadow vertically and make it flat.
//
//                            //Shear the shadow in the oposity direction of the light source.
//                            float[] lightPos = new float[3];
//                            lightPos[0] = defaultLight.getLightPosition()[0] / getSceneSize();
//                            lightPos[1] = defaultLight.getLightPosition()[1] / getSceneSize();
//                            lightPos[2] = defaultLight.getLightPosition()[2] / getSceneSize();
//                            gl.glMultMatrixd(getShearYMatrix(-lightPos[0] / lightPos[1], -lightPos[2] / lightPos[1]), 0);
//
//                            //Draw geometries excluding the floor and sky.
//                            renderGeometries(false, false, false, false);
//
//                            gl.glPopMatrix();
//                        }
//                        if (useLights) {
//                            gl.glEnable(GL_LIGHTING);
//                        }
//                    }
//
//                    //Render the thick wireframe version.
//                    {
//                        gl.glStencilFunc(GL_NOTEQUAL, 1, -1);
//                        gl.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
//
//                        if (useLights) {
//                            gl.glDisable(GL_LIGHTING);
//                        }
//                        gl.glLineWidth(3.0f);
//                        gl.glColor4f(1, 0, 0, 0.5f);
//                        {
//                            gl.glPushMatrix();
//                            gl.glScalef(1.0f, 0.0f, 1.0f); //Flip the shadow vertically and make it flat.
//
//                            //Shear the shadow in the oposity direction of the light source.
//                            float[] lightPos = new float[3];
//                            lightPos[0] = defaultLight.getLightPosition()[0] / getSceneSize();
//                            lightPos[1] = defaultLight.getLightPosition()[1] / getSceneSize();
//                            lightPos[2] = defaultLight.getLightPosition()[2] / getSceneSize();
//                            gl.glMultMatrixd(getShearYMatrix(-lightPos[0] / lightPos[1], -lightPos[2] / lightPos[1]), 0);
//
//                            //Draw geometries excluding the floor and sky.
//                            renderGeometries(true, false, false, false);
//
//                            gl.glPopMatrix();
//                        }
//                        if (useLights) {
//                            gl.glEnable(GL_LIGHTING);
//                        }
//                    }
//                }
//                gl.glDisable(GL_STENCIL_TEST); //Disable the stencil testing.
////------------------------------------------------------------------------------
//                gl.glEnable(GL_DEPTH_TEST); //to enable writing to the depth buffer
//                gl.glDepthFunc(GL_ALWAYS); //to ensure everything you draw passes
//                gl.glDepthMask(true); //to allow writes to the depth buffer
//                gl.glColorMask(false, false, false, false);
//                //so that whatever we draw isn't actually visible
//
//                gl.glClear(GL_DEPTH_BUFFER_BIT); //for a fresh start
//
//                /* here: draw geometry to clip to the inside of, e.g. at z = -2 */
//                //renderFloor(false);
//                renderGeometries(true, false, false, false);
//                
//
//                gl.glDepthFunc(GL_GREATER); //so that the z test will actually be applied
//                gl.glColorMask(true, true, true, true);
//                //so that pixels are painted again...
//                gl.glDepthMask(false);  //... but don't change the clip area
//
//                /* here: draw the geometry to clip inside the old shape at a z further than -2 */
//                renderGeometries(true, false, false, false);
////------------------------------------------------------------------------------                
                        //Blend with textured floor.
                        {
                            gl.glEnable(GL_BLEND); //We enable blending.
                            //gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); //Set the alpha blending                    
                            this.glSetupBlendColor(0, 0, coreOptions.get("showTextures")
                                    ? 9 : 39); //Set color blending.

                            //Draw floor only.
                            this.renderFloor(false);
                            gl.glDisable(GL_BLEND);
                        }

                        //Render the light geometry.
                        defaultLight.renderLight(gl);

                        gl.glMatrixMode(GL_MODELVIEW);
                        gl.glLoadIdentity();

                        //Draw geometries excluding the floor only.
                        this.renderGeometries(false, true, false, true);

                        //Disable light.
                        defaultLight.disableLight(gl);

                        //Disable texture flip, since all loaded textures are upside down.
                        this.disableTextureTransforms();
                    } else if (frameworkOptions.get("showZBuffer")) { //render ZBuffer
                        //Reshape Window and set gluLook.
                        this.glSetupWindow(true, this.zNear * 2, null);

                        this.renderGeometries(false, false, true, false);

                        int[] viewport = new int[4];
                        gl.glGetIntegerv(GL_VIEWPORT, viewport, 0);
                        FloatBuffer depthZlight = allocate(viewport[2] * viewport[3]);
                        gl.glReadPixels(0, 0, viewport[2], viewport[3], GL_DEPTH_COMPONENT, GL_FLOAT, depthZlight);
                        gl.glDrawPixels(viewport[2], viewport[3], GL_LUMINANCE, GL_FLOAT, depthZlight);
                    } else if (frameworkOptions.get("showShadowMaps")) { //render with shadow maps
                        //FIXME: for multiple light sources/shadows won't work.
                        //TODO: perform culling to clean the extra shadows in the models bodies!!!!!

                        //Set camera in the light position.
                        this.glSetupWindow(true, this.zNear, defaultLight.getLightPosition());
                        //glSetupWindow(false, zNear, null);
                        //if (defaultCamera != null) {
                        //    defaultCamera.renderCamera(defaultLight.getLightPosition());
                        //}

                        //First step - captures the depth map.
                        this.shadowMapRenderer.enableDepthCapture(gl);
                        this.renderGeometries(false, false, true, false);
                        this.shadowMapRenderer.disableDepthCapture(gl);

                        //Configure camera normal.
                        defaultLight.getLimitScreen(drawable);

                        //Second step - render the scene testing the shadow pixels.
                        this.glSetupWindow(true, this.zNear, null);
                        //glSetupWindow(false, zNear, null);
                        //if (defaultCamera != null) {
                        //    defaultCamera.renderCamera(null);
                        //}

                        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                        this.shadowMapRenderer.enableShadowTest(gl);
                        defaultLight.enableLight(gl);
                        this.renderGeometries(false, false, true, false);
                        defaultLight.disableLight(gl);
                        this.shadowMapRenderer.disableShadowTest(gl);

                        //Render the light geometry.
                        defaultLight.renderLight(gl);

                        //Blend textured models with shadow maps.
                        if (coreOptions.get("showTextures")) {
                            //Enable texture flip, since all loaded textures are upside down.
                            this.enableTextureTransforms();

                            defaultLight.enableLight(gl);
                            gl.glEnable(GL_BLEND); //We enable blending.
                            this.glSetupBlendColor(0, 0, 9);
                            synchronized (this) {
                                this.gameRender(gl);
                            }
                            gl.glDisable(GL_BLEND);
                            defaultLight.disableLight(gl);

                            //Disable texture flip, since all loaded textures are upside down.
                            this.disableTextureTransforms();
                        }
                    } else { //Default render.
                        this.glSetupWindow(false, this.zNear, null);

                        //Enable texture flip, since all loaded textures are upside down.
                        this.enableTextureTransforms();

                        if (defaultCamera != null) {
                            defaultCamera.lookAt(null);
                        }

                        if (frameworkOptions.get("useLights")) {
                            gl.glEnable(GL_LIGHT0);
                            gl.glLightfv(GL_LIGHT0, GL_POSITION, new float[]{0, 15, 0, 0}, 0);
                            gl.glLightfv(GL_LIGHT0, GL_AMBIENT, new float[]{1, 1, 1, 0}, 0);
                        }

                        //gl.glEnable(GL_BLEND); //We enable blending.
                        //setupBlendColor(0, 0, 28);
                        synchronized (this) {
                            this.gameRender(gl);
                        }
                        //gl.glDisable(GL_BLEND);           

                        //Disable texture flip, since all loaded textures are upside down.
                        this.disableTextureTransforms();
                    }
                } //End of render
                this.skippedFrames = 1;
            } else {
                this.skippedFrames++;
            }
        } else {
            // calculate the time to sleep
            int sleepTime = (int) (1000.0 * (this.nextTime - currTime));
            // sanity check
            if (sleepTime > 0) {
                // sleep until the next update
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }

        if (this.animator != null) {
            //Use the FPS object renderer to render the fps.
            if (coreOptions.get("showFPS") && fps != null) {
                fps.render();
            }

            //Use the text object renderer to render text.
            if (coreOptions.get("showText") && text != null) {
                text.render();
            }
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        log.info("Reshape.");
        gl = (GL2) drawable.getGL();

        if (height == 0) {
            height = 1;
        }

        if (defaultCamera != null) {
            defaultCamera.setFieldOfView(60.0f);
            defaultCamera.setAspectRatio(width / (float) height);
        } else {
            this.fovy = 60.0f;
            this.aspect = width / (float) height;
        }

        this.zNear = 1f;
        this.zFar = 500f;

        this.GAME_CANVAS_WIDTH = width;
        this.GAME_CANVAS_HEIGHT = height;
    }

    private void glSetupBlendColor(int EquationSeparate, int blendFuncSeparate, int blendFunc) {
        switch (blendFunc) {
            case 1:
                //Darker colors transparency
                //WORKS !!!!!!!!!!! 
                gl.glBlendFunc(GL_ZERO, GL_SRC_COLOR);
                break;
            case 2:
                //Negative transparency
                //Negatives 
                gl.glBlendFunc(GL_ZERO, GL_ONE_MINUS_SRC_COLOR);
                break;
            case 3:
                gl.glBlendFunc(GL_ZERO, GL_DST_COLOR); //Dark
                break;
            case 4:
                //Dark gray transparency
                //All dark no textures with murder scene shadows frontiers
                gl.glBlendFunc(GL_ZERO, GL_ONE_MINUS_DST_COLOR);
                break;
            case 5:
                //Some negative             
                //????? without shadows
                gl.glBlendFunc(GL_ONE_MINUS_SRC_COLOR, GL_ZERO);
                break;
            case 6:
                //Darker colors transparency
                //WORKS !!!!!!!!!!!
                gl.glBlendFunc(GL_DST_COLOR, GL_ZERO);
                break;
            case 7:
                gl.glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ZERO); //Textures only in shadows
                break;
            case 8:
                //Caustics with lighning
                //Textures only in shadows
                gl.glBlendFunc(GL_ONE, GL_ONE);
                break;
            case 9:
                //Subtle transparency
                //WORKS with lightning !!!!!!!!!!!
                gl.glBlendFunc(GL_ONE, GL_SRC_COLOR);
                break;
            case 10:
                //Caustics
                //Textures only in shadows
                gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_COLOR);
                break;
            case 11:
                //Some transparency
                //Textures only in shadows
                gl.glBlendFunc(GL_ONE, GL_DST_COLOR);
                break;
            case 12:
                gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_DST_COLOR); //Murder scene shaadows without interior
                break;
            case 13:
                //Caustics with lighning
                //Textures only in shadows
                gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_CONSTANT_COLOR);
                break;
            case 14:
                //Darker colors Caustics
                //Textures only in shadows
                gl.glBlendFunc(GL_SRC_COLOR, GL_ONE);
                break;
            case 15:
                //Darker colors Caustics with some negative
                //Textures only in shadows with ?????
                gl.glBlendFunc(GL_ONE_MINUS_SRC_COLOR, GL_ONE);
                break;
            case 16:
                gl.glBlendFunc(GL_DST_COLOR, GL_ONE); //Darker colors Caustics
                break;
            case 17:
                //Darker colors Caustics
                //Textures only in shadows
                gl.glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE);
                break;
            case 18:
                //Caustics with lighning
                //Textures only in shadows
                gl.glBlendFunc(GL_ONE_MINUS_CONSTANT_COLOR, GL_ONE);
                break;
            case 19:
                //Subtle transparency
                //WORKS with lightning !!!!!!!!!!!
                gl.glBlendFunc(GL_SRC_COLOR, GL_SRC_COLOR);
                break;
            case 20:
                gl.glBlendFunc(GL_SRC_COLOR, GL_ONE_MINUS_SRC_COLOR); //Textures only in shadows everything else in negative
                break;
            case 21:
                //Subtle transparency with darker colors
                //Textures only in shadows
                gl.glBlendFunc(GL_SRC_COLOR, GL_DST_COLOR);
                break;
            case 22:
                //Darker/Swamp colors Caustics
                //Murder scene shaadows without interior
                gl.glBlendFunc(GL_SRC_COLOR, GL_ONE_MINUS_SRC_COLOR); //Darker/Swamp colors Caustics
                break;
            case 23:
                gl.glBlendFunc(GL_SRC_COLOR, GL_ONE_MINUS_DST_COLOR); //Darker colors Caustics
                break;
            case 24:
                //Darker colors Caustics
                //Textures only in shadows
                gl.glBlendFunc(GL_SRC_COLOR, GL_ONE_MINUS_CONSTANT_COLOR);
                break;
            case 25:
                //Transparency
                //WORKS with lightning !!!!!!!!!!!
                gl.glBlendFunc(GL_ONE_MINUS_SRC_COLOR, GL_SRC_COLOR);
                break;
            case 26:
                //Transparency
                //WORKS with lightning !!!!!!!!!!!
                gl.glBlendFunc(GL_DST_COLOR, GL_SRC_COLOR);
                break;
            case 27:
                //Darker colors transparency
                //WORKS !!!!!!!!!!!
                gl.glBlendFunc(GL_CONSTANT_COLOR, GL_SRC_COLOR);
                break;
            case 28:
                //Subtle transparency
                //WORKS with lightning !!!!!!!!!!!
                gl.glBlendFunc(GL_ONE_MINUS_CONSTANT_COLOR, GL_SRC_COLOR);
                break;
            case 29:
                //Darker colors transparency with negative
                //Textures only in shadows everything else in negative
                gl.glBlendFunc(GL_ONE_MINUS_SRC_COLOR, GL_ONE_MINUS_SRC_COLOR);
                break;
            case 30:
                //Darker colors transparency with some negative
                //Textures only in shadows some negative
                gl.glBlendFunc(GL_ONE_MINUS_SRC_COLOR, GL_DST_COLOR);
                break;
            case 31:
                //Darker colors caustics with some negative
                //Murder scene shaadows without interior some negative
                gl.glBlendFunc(GL_ONE_MINUS_SRC_COLOR, GL_ONE_MINUS_DST_COLOR);
                break;
            case 32:
                //Caustics with some negative
                //Textures only in shadows some negative
                gl.glBlendFunc(GL_ONE_MINUS_SRC_COLOR, GL_ONE_MINUS_CONSTANT_COLOR);
                break;
            case 33:
                //Darker colors caustics
                //Textures only in shadows everything else in negative except partially model textures
                gl.glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR);
                break;
            case 34:
                //Darker colors transparency with negative
                //Textures only in shadows everything else in negative
                gl.glBlendFunc(GL_CONSTANT_COLOR, GL_ONE_MINUS_SRC_COLOR);
                break;
            case 35:
                //Caustics
                //Textures only in shadows
                gl.glBlendFunc(GL_ONE_MINUS_CONSTANT_COLOR, GL_ONE_MINUS_SRC_COLOR);
                break;
            case 36:
                gl.glBlendFunc(GL_DST_COLOR, GL_DST_COLOR); //Darker colors transparency
                break;
            case 37:
                //Darker colors caustics
                //Murder scene shaadows darker models
                gl.glBlendFunc(GL_DST_COLOR, GL_ONE_MINUS_DST_COLOR);
                break;
            case 38:
                //Darker colors transparency without lights
                //WORKS !!!!!!!!!!!
                gl.glBlendFunc(GL_DST_COLOR, GL_CONSTANT_COLOR);
                break;
            case 39:
                gl.glBlendFunc(GL_DST_COLOR, GL_ONE_MINUS_CONSTANT_COLOR); //Darker colors caustics
                break;
            case 40:
                //Subtle transparency                
                //Textures only in shadows
                gl.glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_DST_COLOR);
                break;
            case 41:
                gl.glBlendFunc(GL_CONSTANT_COLOR, GL_DST_COLOR); //Dark
                break;
            case 42:
                //Transparency
                //Textures only in shadows
                gl.glBlendFunc(GL_ONE_MINUS_CONSTANT_COLOR, GL_DST_COLOR);
                break;
            case 43:
                //Transparency some refraction
                //Textures only in shadows frontier as murder scene everything else in dark
                gl.glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_DST_COLOR);
                break;
            case 44:
                //Subtle transparency some refraction
                //Textures only in shadows everything else in dark
                gl.glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_CONSTANT_COLOR); //Subtle transparency some refraction
                break;
            case 45:
                //Caustics
                //Textures only in shadows
                gl.glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_CONSTANT_COLOR);
                break;
            case 46:
                gl.glBlendFunc(GL_CONSTANT_COLOR, GL_ONE_MINUS_DST_COLOR); //Drak gray transparency
                break;
            case 47:
                gl.glBlendFunc(GL_ONE_MINUS_CONSTANT_COLOR, GL_ONE_MINUS_DST_COLOR); //Murder scene shaadows without interior
                break;
            case 48:
                //Caustics with lighning
                //Textures only in shadows
                gl.glBlendFunc(GL_ONE_MINUS_CONSTANT_COLOR, GL_ONE_MINUS_CONSTANT_COLOR);
                break;
            default:
                log.info("Non existent option.");
        }

        switch (EquationSeparate) {
            case 1:
                //gl.glBlendEquationSeparate(GL_FUNC_ADD, GL_FUNC_ADD);
                break;
            default:
                log.info("Non existent option.");
        }

        switch (blendFuncSeparate) {
            case 1:
                //gl.glBlendFuncSeparate(GL_DST_COLOR, GL_ONE_MINUS_DST_COLOR, GL_ONE, GL_ZERO);
                break;
            default:
                log.info("Non existent option.");
        }
    }

    private void glSetupWindow(boolean setupLookAt, float zNear, float[] position) {
        gl.glMatrixMode(GL_PROJECTION); //Reset projection matrix stack.
        gl.glLoadIdentity(); //Establish clipping volume (left, right, bottom, top, near, far).

        if (defaultCamera != null) {
            this.glu.gluPerspective(defaultCamera.getFieldOfView(),
                    defaultCamera.getAspectRatio(), zNear, this.zFar);
            if (setupLookAt) {
                defaultCamera.lookAt(position);
            }
        } else {
            this.glu.gluPerspective(this.fovy, this.aspect, zNear, this.zFar);
        }

        gl.glMatrixMode(GL_MODELVIEW); //Reset model-view matrix stack.
        gl.glLoadIdentity();

//        if (defaultCamera != null) {
//            if (setupLookAt) {
//                defaultCamera.lookAt(position);
//            }
//        }
//        gl.glViewport(0,0,100,100);  //Use the whole window for rendering.        
        //FIXME: not working for all cameras fix and then use. It includes culling and orthoview
//        if (defaultCamera == null) {
//            gl.glMatrixMode(GL_PROJECTION); //Reset projection matrix stack.
//            gl.glLoadIdentity(); //Establish clipping volume (left, right, bottom, top, near, far).
//
//            glu.gluPerspective(fovy, aspect, zNear, zFar);
//
//            gl.glMatrixMode(GL_MODELVIEW); //Reset model-view matrix stack.
//            gl.glLoadIdentity();
//
//            //gl.glViewport(0,0,100,100);  //Use the whole window for rendering.
//        } else {
//            defaultCamera.apply(gl, position, setupLookAt);
//        }
    }

    private void renderFloor(boolean wireframe) {
        if (wireframe) {
            coreOptions.put("showWireframe", !coreOptions.get("showWireframe"));
        }

        boolean changedShowGeometries = false;
        if (frameworkOptions.get("showGeometries")) {
            frameworkOptions.put("showGeometries",
                    !frameworkOptions.get("showGeometries"));
            changedShowGeometries = true;
        }

        boolean changedSkyBox = false;
        if (frameworkOptions.get("SkyBox")) {
            frameworkOptions.put("SkyBox", !frameworkOptions.get("SkyBox"));
            changedSkyBox = true;
        }

        boolean changedSkyDome = false;
        if (frameworkOptions.get("SkyDome")) {
            frameworkOptions.put("SkyDome", !frameworkOptions.get("SkyDome"));
            changedSkyDome = true;
        }

        synchronized (this) {
            this.gameRender(gl);
        }

        if (wireframe) {
            coreOptions.put("showWireframe", !coreOptions.get("showWireframe"));
        }

        if (changedShowGeometries) {
            frameworkOptions.put("showGeometries",
                    !frameworkOptions.get("showGeometries"));
        }

        if (changedSkyBox) {
            frameworkOptions.put("SkyBox", !frameworkOptions.get("SkyBox"));
        }

        if (changedSkyDome) {
            frameworkOptions.put("SkyDome", !frameworkOptions.get("SkyDome"));
        }
    }

    private void renderGeometries(boolean wireframe, boolean textures,
            boolean floor, boolean sky) {
        if (wireframe) {
            coreOptions.put("showWireframe", !coreOptions.get("showWireframe"));
        }

        boolean changedShowTextures = false;
        if (coreOptions.get("showTextures") && !textures) {
            coreOptions.put("showTextures", !coreOptions.get("showTextures"));
            changedShowTextures = true;
        }

        boolean changedFloor = false;
        if (coreOptions.get("Floor") && !floor) {
            coreOptions.put("Floor", !coreOptions.get("Floor"));
            changedFloor = true;
        }

        boolean changedSkyBox = false;
        if (frameworkOptions.get("SkyBox") && !sky) {
            frameworkOptions.put("SkyBox", !frameworkOptions.get("SkyBox"));
            changedSkyBox = true;
        }

        boolean changedSkyDome = false;
        if (frameworkOptions.get("SkyDome") && !sky) {
            frameworkOptions.put("SkyDome", !frameworkOptions.get("SkyDome"));
            changedSkyDome = true;
        }

        synchronized (this) {
            this.gameRender(gl);
        }

        if (wireframe) {
            coreOptions.put("showWireframe", !coreOptions.get("showWireframe"));
        }

        if (changedShowTextures) {
            coreOptions.put("showTextures", !coreOptions.get("showTextures"));
        }

        if (changedFloor) {
            coreOptions.put("Floor", !coreOptions.get("Floor"));
        }

        if (changedSkyBox) {
            frameworkOptions.put("SkyBox", !frameworkOptions.get("SkyBox"));
        }

        if (changedSkyDome) {
            frameworkOptions.put("SkyDome", !frameworkOptions.get("SkyDome"));
        }
    }

    //TODO: clean FBO stuff in not necessary
////------------------------------------------------------------------------------
//    int[] fbo; // The frame buffer object.
//    int[] fbo_depth; // The depth buffer for the frame buffer object.
//    int[] fbo_texture;
//    int resolution = 512;
//
//    void initFrameBufferDepthBuffer() {
//        fbo_depth = new int[1];
//
//        //Generate one render buffer and store the ID in fbo_depth.
//        gl.glGenRenderbuffers(1, fbo_depth, 0);
//
//        //Bind the fbo_depth render buffer.
//        gl.glBindRenderbuffer(GL_RENDERBUFFER, fbo_depth[0]);
//
//        //Set the render buffer storage to be a depth component, with a width and height of the window.
//        gl.glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, resolution, resolution);
//
//        //Set the render buffer of this buffer to the depth buffer.
//        gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, fbo_depth[0]);
//
//        //Unbind the render buffer.
//        gl.glBindRenderbuffer(GL_RENDERBUFFER, 0);
//    }
//
//    void initFrameBufferTexture() {
//        fbo_texture = new int[1];
//
//        //Generate one texture.
//        gl.glGenTextures(1, fbo_texture, 0);
//
//        //Bind the texture fbo_texture.
//        gl.glBindTexture(GL_TEXTURE_2D, fbo_texture[0]);
//
//        //Create a standard texture with the width and height of our window.
//        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, resolution, resolution, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
//
//        //Setup the basic texture parameters.
//        gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//        gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
//        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//
//        //Unbind the texture.
//        gl.glBindTexture(GL_TEXTURE_2D, 0);
//    }
//
//    void initFrameBuffer() {
//        //Initialize our frame buffer depth buffer.
//        initFrameBufferDepthBuffer();
//
//        //Initialize our frame buffer texture.
//        initFrameBufferTexture();
//
//        fbo = new int[1];
//
//        //Generate one frame buffer and store the ID in fbo.
//        gl.glGenFramebuffers(1, fbo, 0);
//
//        //Bind our frame buffer.
//        gl.glBindFramebuffer(GL_FRAMEBUFFER, fbo[0]);
//
//        //Attach the texture fbo_texture to the color buffer in our frame buffer.
//        gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fbo_texture[0], 0);
//
//        //Attach the depth buffer fbo_depth to our frame buffer.
//        gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, fbo_depth[0]);
//
//        //Check that status of our generated frame buffer.
//        int status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER);
//
//        //If the frame buffer does not report back as complete.
//        if (status != GL_FRAMEBUFFER_COMPLETE) {
//            log.info("Couldn't create frame buffer"); //Output an error to the console.
//            exit(0); // Exit the application
//        }
//
//        //Unbind our frame buffer.
//        gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
//    }
//
//    void enableFrameBufferCapture() {
//        gl.glBindFramebuffer(GL_FRAMEBUFFER, fbo[0]); //Bind our frame buffer for rendering
//        gl.glPushAttrib(GL_VIEWPORT_BIT | GL_ENABLE_BIT); //Push our glEnable and glViewport states
//        gl.glViewport(0, 0, GAME_CANVAS_WIDTH, GAME_CANVAS_HEIGHT); //Set the size of the frame buffer view port
//
//        //Clear the background of our window.
//        gl.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
//
//        //Clean the buffers.
//        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//
//        gl.glMatrixMode(GL_MODELVIEW);
//        gl.glLoadIdentity();
//    }
//
//    void disableFrameBufferCapture() {
//        gl.glPopAttrib(); // Restore our glEnable and glViewport states
//        gl.glBindFramebuffer(GL_FRAMEBUFFER, 0); // Unbind our texture
//    }    
    /**
     * Starts running the game main thread.
     *
     * @param GAME_UPDATE_RATE the update rate in updates per second (UPS) of
     * the game.
     */
    public void gameRun(int GAME_UPDATE_RATE) {
        this.GAME_UPDATE_PERIOD = 1 / (double) GAME_UPDATE_RATE;  //seconds.     

        this.animator = new Animator(this.glw);
        this.animator.setUpdateFPSFrames(GAME_UPDATE_RATE, null); //On FPSCounter
        //animator.setRunAsFastAsPossible(true);        
        this.animator.start();
    }

    /**
     * Shutdown the game, clean up code that runs only once.
     */
    @Override
    public void gameShutdown() {
        log.info("gameShutdown");

        new Thread(() -> {
            if (this.animator != null) {
                this.animator.stop();
            }
            if (this.sceneManager != null) {
                this.sceneManager.dispose(gl);
            }
            if (this.sceneManager != null) {
                this.sceneManager.dispose(gl);
            }
            this.sound.dispose();
            gc();
            exit(0);
        }).start();
    }

    /**
     * Types of states of the game.
     */
    public static enum State {

        LOADING, INITIALIZED, PLAYING, PAUSED, GAME_OVER
    }
}
