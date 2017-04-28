/*
 * This file is part of the JOT game engine core template.
 * Copyright (C) 2014 Gonçalo Amador & Abel Gomes
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
 * E-mail Contacts: gamador@it.ubi.pt & A. Gomes (agomes@di.ubi.pt)
 */
package gui;

import static com.jogamp.newt.event.KeyEvent.VK_ESCAPE;
import static com.jogamp.newt.event.MouseEvent.BUTTON1;
import static com.jogamp.newt.event.MouseEvent.BUTTON3;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_FRONT_AND_BACK;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2ES3.GL_QUADS;
import static com.jogamp.opengl.GL2GL3.GL_FILL;
import static com.jogamp.opengl.GL2GL3.GL_LINE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_POSITION;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import com.jogamp.opengl.glu.GLU;
import static java.lang.Integer.valueOf;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.gui.SimpleGame;
import static jot.io.data.format.ColladaScene.loadFormat;
import jot.math.geometry.TransformGroup;
import jot.math.geometry.bounding.AbstractBoundingVolume;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.AABB;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.OBB;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.SPHERE;
import static jot.util.CoreOptions.coreOptions;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;
import static util.GameConstants.setGameConstants;

/**
 * Class that performs the main game loop.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class GameMain extends SimpleGame {

    static final Logger log = getLogger("GameMain");

    /**
     * The main method.
     *
     * @param args terminal arguments (if any)
     */
    public static void main(String[] args) {
        setGameConstants();

        //Default Logger level is INFO
        log.setLevel(INFO);
        //log.setLevel(Level.OFF);
        //log.setLevel(Level.WARNING);

        GameMain game;
        switch (args.length) {
            case 0:
                game = new GameMain();
                break;
            case 1:
                game = new GameMain(args[0]);
                break;
            case 2:
                game = new GameMain(valueOf(args[0]), valueOf(args[1]));
                break;
            default:
                game = new GameMain(args[0], valueOf(args[1]), valueOf(args[2]));
        }
        game.gameRun();
    }

    //formats to load
    private GameObject player1;
    private GameObject player2;

    //the velocity for any moving avatar.
    private final Vector3D velocity = new Vector3D(0.5f, 0.5f, 0.5f);

    private float direction;
    boolean isMoving, isRotating;

    //an hash map to store all distinct loaded models, i.e., individual models only need to be loaded once and may be reused by cloning them
    private final ConcurrentHashMap<String, TransformGroup> models = new ConcurrentHashMap<>();

    //toggle on/off render loaded models or render user defined geometries
    private boolean loadedModels = false;

    //audio background music to play.    
    private final String backgroundSound = "music3.wav";

    //GL stuff
    private GLU glu;
    private final float[] clearColorWhite = {1.0f, 1.0f, 1.0f, 1.0f};       //White.
    //private final float[] clearColorPurple = {0.3f, 0.3f, 0.7f, 0.0f};      //Purple.
    //private final float[] clearColorPitchblack = {0.3f, 0.3f, 0.3f, 0.3f};  //Pitch black.    

    /**
     * {@inheritDoc}
     */
    public GameMain() {
        super();
    }

    /**
     * {@inheritDoc}
     *
     * @param gameName
     */
    public GameMain(String gameName) {
        super(gameName);
    }

    /**
     * {@inheritDoc}
     *
     * @param GAME_CANVAS_WIDTH
     * @param GAME_CANVAS_HEIGHT
     */
    public GameMain(int GAME_CANVAS_WIDTH, int GAME_CANVAS_HEIGHT) {
        super(GAME_CANVAS_WIDTH, GAME_CANVAS_HEIGHT);
    }

    /**
     * {@inheritDoc}
     *
     * @param gameName
     * @param GAME_CANVAS_WIDTH
     * @param GAME_CANVAS_HEIGHT
     */
    public GameMain(String gameName, int GAME_CANVAS_WIDTH, int GAME_CANVAS_HEIGHT) {
        super(gameName, GAME_CANVAS_WIDTH, GAME_CANVAS_HEIGHT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameInit() {
        gl.glEnable(GL_LIGHT0);
        gl.glLightfv(GL_LIGHT0, GL_POSITION, new float[]{0, 15, 0, 0}, 0);
        gl.glLightfv(GL_LIGHT0, GL_AMBIENT, new float[]{1, 1, 1, 0}, 0);

        clearColor = clearColorWhite;
        //clearColor = clearColorPurple;
        //clearColor = clearColorPitchblack;        

        //Register input keys
        keyBoard.registerInputEvent("1", '1');
        keyBoard.registerInputEvent("2", '2');
        keyBoard.registerInputEvent("R", 'R');
        keyBoard.registerInputEvent("R", 'r');
        keyBoard.registerInputEvent("T", 'T');
        keyBoard.registerInputEvent("T", 't');
        keyBoard.registerInputEvent("W", 'W');
        keyBoard.registerInputEvent("W", 'w');
        keyBoard.registerInputEvent("A", 'A');
        keyBoard.registerInputEvent("A", 'a');
        keyBoard.registerInputEvent("S", 'S');
        keyBoard.registerInputEvent("S", 's');
        keyBoard.registerInputEvent("D", 'D');
        keyBoard.registerInputEvent("D", 'd');
        keyBoard.registerInputEvent("V", 'V');
        keyBoard.registerInputEvent("V", 'v');
        keyBoard.registerInputEvent("B", 'B');
        keyBoard.registerInputEvent("B", 'b');
        keyBoard.registerInputEvent("N", 'N');
        keyBoard.registerInputEvent("N", 'n');
        keyBoard.registerInputEvent(",", ',');
        keyBoard.registerInputEvent(".", '.');
        keyBoard.registerInputEvent(";", ';');
        keyBoard.registerInputEvent(":", ':');
        keyBoard.registerInputEvent("Quit", VK_ESCAPE);

        //Register mouse commands
        mouse.registerInputEvent("Left Button", BUTTON1);
        mouse.registerInputEvent("Right Button", BUTTON3);

        //TODO: (optional) more options here
        text.setTextLine("");
        text.setTextLine("Options:");
        if (coreOptions.get("useMouse")) {
            text.setTextLine("Mouse move - rotate Duke model.");
            text.setTextLine("Mouse left button click - does nothing.");
            text.setTextLine("Mouse right button continuous press - does nothing.");
        }
        if (coreOptions.get("useKeyBoard")) {
            text.setTextLine("1 key - toggle on/off textures.");
            text.setTextLine("2 key - toggle on/off wireframe.");
            text.setTextLine("T/t key - toggle on/off render loaded models or render user defined geometries.");
            text.setTextLine("R/r key - reload player2 model with different bounding volume.");
            text.setTextLine(",/. keys - to increase/decrease the gain/volume of the background music.");
            text.setTextLine(";/: keys - to increase/decrease the pitch of the background music.");
            text.setTextLine("V/v,B/b,N/n keys - play, pause, stop background music.");
            text.setTextLine("W/w,S/s,A/a,D/d keys - move Duke model.");
            text.setTextLine("Esc key - to quit.\n");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameLoadContent() {
        //load models      
        models.put("cow1", loadFormat("assets/models/dae/cow/", "cow.dae", 1, AABB));
        //models.get("cow1").setRotationY(-180);
        //models.get("cow1").setRotationZ(90);
        //models.get("cow1").setRotationX(90);
        models.get("cow1").setRotationZ(90.0f);
        models.get("cow1").setRotationX(-90.0f);

        models.put("cow2", loadFormat("assets/models/dae/cow/", "cow.dae", 1, OBB));
        //models.get("cow2").setRotationY(-180);
        //models.get("cow2").setRotationZ(90);
        //models.get("cow2").setRotationX(90);
        models.get("cow2").setRotationZ(90.0f);
        models.get("cow2").setRotationX(-90.0f);

        models.put("cow3", loadFormat("assets/models/dae/cow/", "cow.dae", 1, SPHERE));
        //models.get("cow3").setRotationY(-180);
        //models.get("cow3").setRotationZ(90);
        //models.get("cow3").setRotationX(90);
        models.get("cow3").setRotationZ(90.0f);
        models.get("cow3").setRotationX(-90.0f);

        models.put("duke", loadFormat("assets/models/dae/", "Duke_posed.dae", 1, OBB));
        models.get("duke").setRotationY(180);

        try {
            TransformGroup player1Rotate = models.get("duke").clone();
            player1 = new GameObject("player1", models.get("duke"));
            player1.getBoundingVolume(0).setRenderable(true);
            player1.addChild(player1Rotate);
            player1.setAttribute("health", 100);
            log.info("player1 loaded.");

            TransformGroup player2Rotate = models.get("cow3").clone();
            player2Rotate.updateTranslation(new Vector3D(10, 0, -2));   //Option 2
            player2Rotate.getBoundingVolume(0).setRenderable(true);     //Option 2
            TransformGroup player3Rotate = models.get("cow1").clone();  //Option 2
            player3Rotate.updateTranslation(new Vector3D(10, 0, 2));    //Option 2
            player3Rotate.getBoundingVolume(0).setRenderable(true);     //Option 2
            //player2 = new GameObject("cow3", models.get("cow3"));       //Option 1
            player2 = new GameObject("cow3");                           //Option 2
            //player2.getBoundingVolume(0).setRenderable(true);           //Option 1
            player2.addChild(player2Rotate);
            player2.addChild(player3Rotate);                            //Option 2
            player2.setAttribute("health", 100);
            //player2.updatePosition(new Vector3D(10, 0, -2));            //Option 1
            //player2.updateRotationY(45);                                //Option 1
            log.info("player2 loaded.");
        } catch (CloneNotSupportedException ex) {
            log.severe(ex.getMessage());
        }

        //load audio
        sound.load("assets/sounds/Effects/", "cow.wav", false);
        sound.load("assets/sounds/Music/", backgroundSound, true);

        //show options text on terminal
        text.showText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameProcessInput() {
        //TODO: (optional) code input behaviour
        if (keyBoard.isDetecting("1")) {
            coreOptions.put("showTextures", !coreOptions.get("showTextures"));
        }
        if (keyBoard.isDetecting("2")) {
            coreOptions.put("showWireframe", !coreOptions.get("showWireframe"));
        }

        if (keyBoard.isDetecting(",")) {
            sound.setGain(backgroundSound, sound.getGain(backgroundSound) - 0.1f);
        }
        if (keyBoard.isDetecting(".")) {
            sound.setGain(backgroundSound, sound.getGain(backgroundSound) + 0.1f);
        }
        if (keyBoard.isDetecting(";")) {
            sound.setPitch(backgroundSound, sound.getPitch(backgroundSound) - 0.1f);
        }
        if (keyBoard.isDetecting(":")) {
            sound.setPitch(backgroundSound, sound.getPitch(backgroundSound) + 0.1f);
        }
        if (keyBoard.isDetecting("V")) {
            sound.play(backgroundSound);
            ////show options text on terminal
            //text.showText();
        }
        if (keyBoard.isDetecting("B")) {
            sound.pause(backgroundSound);
            ////show options text on terminal
            //text.showText();
        }
        if (keyBoard.isDetecting("N")) {
            sound.stop(backgroundSound);
            ////show options text on terminal
            //text.showText();
        }

        if (keyBoard.isDetecting("Quit")) {
            this.gameShutdown();
        }

        if (keyBoard.isDetecting("R")) {
            reloadPlayer2();
            ////show options text on terminal
            //text.showText();
        }
        if (keyBoard.isDetecting("T")) {
            loadedModels = !loadedModels;
        }

        if (mouse.isDetecting("Left Button")) {
            log.info("Mouse Left Button Pressed.");
            ////show options text on terminal
            //text.showText();
        }
        if (mouse.isContinuouslyDetecting("Right Button")) {
            log.info("Mouse Right Button Continuously Pressed.");
            ////show options text on terminal
            //text.showText();
        }

        //WSAD keys processing, i.e., movement processing
        boolean wDown = keyBoard.isContinuouslyDetecting("W");
        boolean sDown = keyBoard.isContinuouslyDetecting("S");
        boolean aDown = keyBoard.isContinuouslyDetecting("A");
        boolean dDown = keyBoard.isContinuouslyDetecting("D");
        //log.info(format("%b %b %b %b", wDown, sDown, dDown, aDown));

        if (wDown && sDown) {
            wDown = false;
            sDown = false;
        }
        if (aDown && dDown) {
            aDown = false;
            dDown = false;
        }

        //Moving & rotating
        direction = 0;
        isMoving = false;
        isRotating = false;

        if (wDown || sDown || aDown || dDown) {
            isMoving = true;
            if (wDown) {
                if (aDown) {
                    direction = 45;
                } else if (dDown) {
                    direction = -45;
                } else {
                    direction = 0;
                }
            } else if (sDown) {
                if (aDown) {
                    direction = 135;
                } else if (dDown) {
                    direction = -135;
                } else {
                    direction = 180;
                }
            } else if (aDown) {
                direction = 90;
            } else if (dDown) {
                direction = -90;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param gl2
     */
    @Override
    public void gameRender(GL2 gl2) {
        //TODO: (optional) code render update      

        //clear the background of our window
        gl.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);

        //clean the buffers
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //setup window for rendering
        gl.glMatrixMode(GL_PROJECTION); //reset projection matrix stack.
        gl.glLoadIdentity(); //establish clipping volume (left, right, bottom, top, near, far)

        if (glu == null) {
            glu = new GLU();
        }
        glu.gluPerspective(fovy, aspect, zNear, zFar);

        gl.glMatrixMode(GL_MODELVIEW); //reset model-view matrix stack
        gl.glLoadIdentity();
        //end of setup window for rendering

        //setup look at camera        
        glu.gluLookAt(-8.0, 4.5, -8.0, 0, 0, 0, 0, 1, 0);

        gl.glDisable(GL_LIGHTING);

        //Render Floor
        gl.glBegin(GL_QUADS);
        gl.glColor3f(0.2f, 0.2f, 0.2f);
        gl.glVertex3f(-100.0f, 0.0f, -100.0f);
        gl.glColor3f(0.4f, 0.4f, 0.4f);
        gl.glVertex3f(-100.0f, 0.0f, 100.0f);
        gl.glColor3f(0.6f, 0.6f, 0.6f);
        gl.glVertex3f(100.0f, 0.0f, 100.0f);
        gl.glColor3f(0.8f, 0.8f, 0.8f);
        gl.glVertex3f(100.0f, 0.0f, -100.0f);
        gl.glEnd();

        if (!loadedModels) {
            gl.glDisable(GL_LIGHTING);
            //render basic geometry
            drawcube(5, 5, 2);
        } else {
            gl.glEnable(GL_LIGHTING);

            //render model with materials
            player1.render(gl);

            //render cow textured model
            //Enable texture flip, since all loaded textures are upside down
            enableTextureTransforms();
            player2.render(gl);
            //Disable texture flip, since all loaded textures are upside down
            disableTextureTransforms();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameShutdown() {
        super.gameShutdown();
        player1.dispose(gl);
        player2.dispose(gl);
    }

    /**
     * {@inheritDoc}
     *
     * @param dt
     */
    @Override
    public void gameUpdate(float dt) {
        //if using loaded models, otherwise no input processing is required
        if (loadedModels) {
            //Mouse based rotation
            Vector3D newMouseCursorPosition = mouse.getPositionShift();

            Vector3D rotation = player1.getRotation();
            if (newMouseCursorPosition.getX() != 0) {
                isRotating = true;
                rotation = new Vector3D(rotation.getX(), rotation.getY() + newMouseCursorPosition.getX() + .3, rotation.getZ());
                player1.updateRotationY((float) rotation.getY());
                sound.setListenerOri((int) rotation.getY());
            }

            //TODO: (optional) code physics update
            if (isMoving) {
                Vector3D newPosition = translatePolar(ZERO, velocity, (float) rotation.getY(), direction, 1);
                AbstractBoundingVolume boundingVolume = player1.getBoundingVolumeCopy(0);
                boundingVolume.min = boundingVolume.min.add(newPosition);
                boundingVolume.max = boundingVolume.max.add(newPosition);
                GameObject temp = new GameObject(player1.getId());
                temp.setBoundingVolume(0, boundingVolume);

                if (!player2.getBoundingVolume(0).isCollide(temp.getBoundingVolume(0))) {
                    player1.updatePosition(newPosition);
                    sound.setPos("cow.wav",
                            (float) newPosition.getX(),
                            (float) newPosition.getY(),
                            (float) newPosition.getZ());
                }
            }
        }

        //TODO: (optional) code AI update
        //TODO: (optional) code network update        
    }

//    //TODO: (optional) more options here
//    private void optionsInfo() {
//        log.info("");
//        log.info("Options:");
//        if (coreOptions.get("useMouse")) {
//            log.info("Mouse move - rotate Duke model.");
//            log.info("Mouse left button click - does nothing.");
//            log.info("Mouse right button continuous press - does nothing.");
//        }
//        if (coreOptions.get("useKeyBoard")) {
//            log.info("1 key - toggle on/off textures.");
//            log.info("2 key - toggle on/off wireframe.");
//            log.info("T/t key - toggle on/off render loaded models or render user defined geometries.");
//            log.info("R/r key - reload player2 model with different bounding volume.");
//            log.info(",/. keys - to increase/decrease the gain/volume of the background music.");
//            log.info(";/: keys - to increase/decrease the pitch of the background music.");
//            log.info("V/v,B/b,N/n keys - play, pause, stop background music.");
//            log.info("W/w,S/s,A/a,D/d keys - move Duke model.");
//            log.info("Esc key - to quit.\n");
//        }
//    }
    //Auxiliar method to render a cube.
    private void drawcube(int x_offset, int z_offset, int color) {
        /*
         * this function draws a cube centerd at (x_offset, z_offset) x and z
         * _big are the back and rightmost points, x and z _small are the front
         * and leftmost points
         */
        float x_big = x_offset + 0.5f;
        float z_big = z_offset + 0.5f;
        float x_small = x_offset - 0.5f;
        float z_small = z_offset - 0.5f;
        switch (color) {
            case 1:
                gl.glColor3f(1.0f, 0.0f, 0.0f);
                break;
            case 2:
                gl.glColor3f(0.0f, 1.0f, 0.0f);
                break;
            case 3:
                gl.glColor3f(0.0f, 0.0f, 1.0f);
                break;
        }

        //Show wireframe meshes test.
        gl.glPolygonMode(GL_FRONT_AND_BACK,
                coreOptions.get("showWireframe")
                ? GL_LINE : GL_FILL);

        gl.glBegin(GL_QUADS);
        /*
         * front
         */
        gl.glVertex3f(x_small, 1.0f, z_big);
        gl.glVertex3f(x_small, 0.0f, z_big);
        gl.glVertex3f(x_big, 0.0f, z_big);
        gl.glVertex3f(x_big, 1.0f, z_big);

        /*
         * back
         */
        gl.glVertex3f(x_big, 1.0f, z_small);
        gl.glVertex3f(x_big, 0.0f, z_small);
        gl.glVertex3f(x_small, 0.0f, z_small);
        gl.glVertex3f(x_small, 1.0f, z_small);


        /*
         * right
         */
        gl.glVertex3f(x_big, 1.0f, z_big);
        gl.glVertex3f(x_big, 0.0f, z_big);
        gl.glVertex3f(x_big, 0.0f, z_small);
        gl.glVertex3f(x_big, 1.0f, z_small);

        /*
         * left
         */
        gl.glVertex3f(x_small, 1.0f, z_small);
        gl.glVertex3f(x_small, 0.0f, z_small);
        gl.glVertex3f(x_small, 0.0f, z_big);
        gl.glVertex3f(x_small, 1.0f, z_big);

        /*
         * top
         */
        gl.glVertex3f(x_small, 1.0f, z_big);
        gl.glVertex3f(x_big, 1.0f, z_big);
        gl.glVertex3f(x_big, 1.0f, z_small);
        gl.glVertex3f(x_small, 1.0f, z_small);

        /*
         * bottom
         */
        gl.glVertex3f(x_small, 0.0f, z_small);
        gl.glVertex3f(x_big, 0.0f, z_small);
        gl.glVertex3f(x_big, 0.0f, z_big);
        gl.glVertex3f(x_small, 0.0f, z_big);
        gl.glEnd();
    }

    //Auxiliary method to change player2 bounding volume by cloning the model.
    private void reloadPlayer2() {
        try {
            TransformGroup player2Rotate;
            switch (player2.getId()) {
                case "cow1":
                    player2Rotate = models.get("cow2").clone();
                    player2 = new GameObject("cow2", models.get("cow2"));
                    break;
                case "cow2":
                    player2Rotate = models.get("cow3").clone();
                    player2 = new GameObject("cow3", models.get("cow3"));
                    break;
                default:
                    player2Rotate = models.get("cow1").clone();
                    player2 = new GameObject("cow1", models.get("cow1"));
            }
            player2.getBoundingVolume(0).setRenderable(true);
            player2.addChild(player2Rotate);
            player2.setAttribute("health", 100);
            player2.updatePosition(new Vector3D(10, 0, -2));
            player2.updateRotationY(45);
            sound.play("cow.wav");
        } catch (CloneNotSupportedException ex) {
            log.severe(ex.getMessage());
        }
    }

    /**
     * Update a position in polar coordinates.
     *
     * @param position current position of an avatar.
     * @param velocity velocity of an avatar.
     * @param rotation rotation of an avatar in degrees.
     * @param direction direction in which the avatar is moving in degrees.
     * @param dt the physics update time step.
     * @return the updated position in polar coordinates
     */
    private Vector3D translatePolar(
            Vector3D position,
            Vector3D velocity,
            float rotation,
            float direction,
            float dt) {
        float x = (float) (position.getX()
                + sin(toRadians(rotation + direction)) * velocity.getX() * dt);
        float y = (float) position.getY();
        float z = (float) (position.getZ()
                + cos(toRadians(rotation + direction)) * velocity.getZ() * dt);
        return new Vector3D(x, y, z);
    }
}
