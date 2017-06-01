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
package jot.gui;

import com.jogamp.newt.Display;
import static com.jogamp.newt.NewtFactory.createDisplay;
import static com.jogamp.newt.NewtFactory.createScreen;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import static com.jogamp.newt.opengl.GLWindow.create;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_TEXTURE;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_TRANSFORM_BIT;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import static com.jogamp.opengl.GLProfile.getDefault;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_NORMALIZE;
import com.jogamp.opengl.util.Animator;
import static java.lang.System.exit;
import static java.lang.System.gc;
import static java.lang.System.nanoTime;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.io.device.handlers.KeyBoard;
import jot.io.device.handlers.Mouse;
import jot.io.sound.handlers.Sound3D;
import static jot.util.CoreOptions.coreOptions;

/**
 * Abstract Class that performs game render and window management tasks, i.e.,
 * implements JOGL and frame/window methods. Based on source available at (last
 * consulted on 29 - 07 - 2012):
 * http://www3.ntu.edu.sg/home/ehchua/programming/java/J8d_Game_Framework.html
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class SimpleGame implements GLEventListener, WindowListener {

    protected static final Logger log = getLogger("SimpleGame");

    /**
     * GL object.
     */
    protected static GL2 gl;

    /**
     * FPS counter object.
     */
    protected static FPSCounter fps;

    /**
     * Text object.
     */
    protected static Text text;

    static {
        log.setLevel(OFF);
    }

    //Input handlers
    protected Mouse mouse = new Mouse();
    protected KeyBoard keyBoard = new KeyBoard();
    protected Sound3D sound;

    //Other globals
    protected int GAME_CANVAS_WIDTH = 800;
    protected int GAME_CANVAS_HEIGHT = 600;
    //protected GLUT glut;
    protected float fovy = 60;
    protected float aspect = this.GAME_CANVAS_WIDTH / this.GAME_CANVAS_HEIGHT;
    protected float zNear = 1;
    protected float zFar = 500;

    protected GLWindow glw;
    protected Animator animator;

    /**
     * The name for the game window/frame.
     */
    protected String gameName = "JOTProject";

    protected long previous, current, elapsed;

    /**
     * Constructor.
     */
    public SimpleGame() {
        GLProfile glp = getDefault();
        GLCapabilities glc = new GLCapabilities(glp);

        Display display = createDisplay(null); // local display
        Screen screen = createScreen(display, 0); // screen 0
        screen.createNative(); // instantiate for resolution query and keep it alive !

        this.glw = create(glc);
        this.glw.setTitle(this.gameName);
        if (coreOptions.get("useFullScreen")) {
            this.glw.setFullscreen(true);
        } else {
            this.glw.setSize(this.GAME_CANVAS_WIDTH, this.GAME_CANVAS_HEIGHT);
            this.glw.setPosition(
                    (screen.getWidth() - this.GAME_CANVAS_WIDTH) / 2,
                    (screen.getHeight() - this.GAME_CANVAS_HEIGHT) / 2); //Specific position
        }
        this.glw.setVisible(true);
        this.glw.requestFocus();

        //activate the input module
        if (coreOptions.get("useKeyBoard")) {
            this.glw.addKeyListener(this.keyBoard);
        }
        if (coreOptions.get("useMouse")) {
            this.glw.addMouseListener(this.mouse);
            //On/Off mouse cursor render
            if (coreOptions.get("useMouseCursor")) {
                this.glw.setPointerVisible(true);
            }
        }

        this.glw.addWindowListener(SimpleGame.this);
        this.glw.addGLEventListener(SimpleGame.this);

        //init audio handler
        this.sound = new Sound3D();
    }

    /**
     * Constructor, receives the name of the application window.
     *
     * @param gameName the application window name.
     */
    public SimpleGame(String gameName) {
        GLProfile glp = getDefault();
        GLCapabilities glc = new GLCapabilities(glp);

        Display display = createDisplay(null); // local display
        Screen screen = createScreen(display, 0); // screen 0
        screen.createNative(); // instantiate for resolution query and keep it alive !

        this.glw = create(glc);
        this.glw.setTitle(gameName);
        if (coreOptions.get("useFullScreen")) {
            this.glw.setFullscreen(true);
        } else {
            this.glw.setSize(this.GAME_CANVAS_WIDTH, this.GAME_CANVAS_HEIGHT);
            this.glw.setPosition(
                    (screen.getWidth() - this.GAME_CANVAS_WIDTH) / 2,
                    (screen.getHeight() - this.GAME_CANVAS_HEIGHT) / 2); //Specific position
        }
        this.glw.setVisible(true);
        this.glw.requestFocus();

        //activate the input module
        if (coreOptions.get("useKeyBoard")) {
            this.glw.addKeyListener(this.keyBoard);
        }
        if (coreOptions.get("useMouse")) {
            this.glw.addMouseListener(this.mouse);
            //On/Off mouse cursor render
            if (coreOptions.get("useMouseCursor")) {
                this.glw.setPointerVisible(true);
            }
        }

        this.glw.addWindowListener(SimpleGame.this);
        this.glw.addGLEventListener(SimpleGame.this);

        this.gameName = gameName;

        //init audio handler
        this.sound = new Sound3D();
    }

    /**
     * Constructor, receives the CANVAS_WIDTH and the CANVAS_HEIGHT.
     *
     * @param GAME_CANVAS_WIDTH of the window.
     * @param GAME_CANVAS_HEIGHT of the window.
     */
    public SimpleGame(int GAME_CANVAS_WIDTH, int GAME_CANVAS_HEIGHT) {
        GLProfile glp = getDefault();
        GLCapabilities glc = new GLCapabilities(glp);

        Display display = createDisplay(null); // local display
        Screen screen = createScreen(display, 0); // screen 0
        screen.createNative(); // instantiate for resolution query and keep it alive !

        this.glw = create(glc);
        this.glw.setTitle(this.gameName);
        if (coreOptions.get("useFullScreen")) {
            this.glw.setFullscreen(true);
        } else {
            this.glw.setSize(GAME_CANVAS_WIDTH, GAME_CANVAS_HEIGHT);
            this.glw.setPosition(
                    (screen.getWidth() - GAME_CANVAS_WIDTH) / 2,
                    (screen.getHeight() - GAME_CANVAS_HEIGHT) / 2); //Specific position
        }
        this.glw.setVisible(true);
        this.glw.requestFocus();

        //activate the input module
        if (coreOptions.get("useKeyBoard")) {
            this.glw.addKeyListener(this.keyBoard);
        }
        if (coreOptions.get("useMouse")) {
            this.glw.addMouseListener(this.mouse);
            //On/Off mouse cursor render
            if (coreOptions.get("useMouseCursor")) {
                this.glw.setPointerVisible(true);
            }
        }

        this.glw.addWindowListener(SimpleGame.this);
        this.glw.addGLEventListener(SimpleGame.this);

        this.GAME_CANVAS_WIDTH = GAME_CANVAS_WIDTH;
        this.GAME_CANVAS_HEIGHT = GAME_CANVAS_HEIGHT;

        this.aspect = GAME_CANVAS_WIDTH / GAME_CANVAS_HEIGHT;

        //init audio handler
        this.sound = new Sound3D();
    }

    /**
     * Constructor, receives the name of the application window, the
     * CANVAS_WIDTH, and the CANVAS_HEIGHT.
     *
     * @param gameName the application window name.
     * @param GAME_CANVAS_WIDTH of the window.
     * @param GAME_CANVAS_HEIGHT of the window.
     */
    public SimpleGame(String gameName, int GAME_CANVAS_WIDTH, int GAME_CANVAS_HEIGHT) {
        GLProfile glp = getDefault();
        GLCapabilities glc = new GLCapabilities(glp);

        Display display = createDisplay(null); // local display
        Screen screen = createScreen(display, 0); // screen 0
        screen.createNative(); // instantiate for resolution query and keep it alive !

        this.glw = create(glc);
        this.glw.setTitle(gameName);
        if (coreOptions.get("useFullScreen")) {
            this.glw.setFullscreen(true);
        } else {
            this.glw.setSize(GAME_CANVAS_WIDTH, GAME_CANVAS_HEIGHT);
            this.glw.setPosition(
                    (screen.getWidth() - GAME_CANVAS_WIDTH) / 2,
                    (screen.getHeight() - GAME_CANVAS_HEIGHT) / 2); //Specific position
        }
        this.glw.setVisible(true);
        this.glw.requestFocus();

        //activate the input module
        if (coreOptions.get("useKeyBoard")) {
            this.glw.addKeyListener(this.keyBoard);
        }
        if (coreOptions.get("useMouse")) {
            this.glw.addMouseListener(this.mouse);
            //On/Off mouse cursor render
            if (coreOptions.get("useMouseCursor")) {
                this.glw.setPointerVisible(true);
            }
        }

        this.glw.addWindowListener(SimpleGame.this);
        this.glw.addGLEventListener(SimpleGame.this);

        this.gameName = gameName;
        this.GAME_CANVAS_WIDTH = GAME_CANVAS_WIDTH;
        this.GAME_CANVAS_HEIGHT = GAME_CANVAS_HEIGHT;

        this.aspect = GAME_CANVAS_WIDTH / GAME_CANVAS_HEIGHT;

        //init audio handler
        this.sound = new Sound3D();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        log.info("Init.");
        gl = (GL2) drawable.getGL();
        //glu = new GLU();
        //glut = new GLUT();

        //Create informative text box
        text = new Text(drawable, 18);
        text.setColor(1, 0, 0, 1);

        try {
            synchronized (this) {
                this.gameInit();
                this.gameLoadContent();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_NORMALIZE);

        this.previous = nanoTime();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        log.info("Dispose.");
        //gl = (GL2) drawable.getGL();        
        this.gameShutdown();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        log.info("Display.");
        //gl = (GL2) drawable.getGL();

        //Create the fps counter
        if (fps == null && this.animator != null) {
            //Create the FPS counter
            fps = new FPSCounter(drawable, this.animator, 36);
            //fps = new FPSCounter(drawable, fpsAnimator, 36);
            fps.setColor(1, 0, 0, 1);
        }

        this.current = nanoTime();
        this.elapsed = this.current - this.previous;
        this.previous = this.current;

        synchronized (this) {
            this.gameProcessInput();
            this.gameUpdate(this.elapsed / 1_000_000_000F);
            this.gameRender(gl);
        }

        //Use the FPS object renderer to render the fps.
        if (fps != null && this.animator != null) {
            fps.render();
        }

        //Use the text object renderer to render the informative text.
        if (text != null) {
            text.render();
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        log.info("Reshape.");
        gl = (GL2) drawable.getGL();

        if (height == 0) {
            height = 1;
        }

        this.fovy = 60.0f;
        this.aspect = width / (float) height;
        this.zNear = 1f;
        this.zFar = 500f;

        this.GAME_CANVAS_WIDTH = width;
        this.GAME_CANVAS_HEIGHT = height;
    }

    @Override
    public void windowResized(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void windowMoved(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void windowDestroyNotify(WindowEvent e) {
        //log.info("windowDestroyNotify");
        //gameShutdown(gl);
        //this.dispose(glw);
        //throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void windowDestroyed(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void windowRepaint(WindowUpdateEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); 
    }

    /**
     * Initialize the game, i.e., load the menus/controls devices and show
     * startup info.
     */
    public abstract void gameInit();

    /**
     * Load all default game content, i.e., player, obstacles, SkyBox, and
     * floor.
     */
    public abstract void gameLoadContent();

    /**
     * Process the input handlers events.
     */
    public abstract void gameProcessInput();

    /**
     * Refresh the display.
     *
     * @param gl
     */
    public abstract void gameRender(GL2 gl);

    /**
     * Starts running the game main thread.
     */
    public void gameRun() {
        this.animator = new Animator(this.glw);
        this.animator.setRunAsFastAsPossible(true);
        this.animator.start();
    }

    /**
     * Shutdown the game, clean up code that runs only once.
     */
    public void gameShutdown() {
        log.info("gameShutdown");

        new Thread(() -> {
            if (this.animator != null) {
                this.animator.stop();
            }
            this.sound.dispose();
            gc();
            exit(0);
        }).start();
    }

    /**
     * Update the state and position of all the game objects, detect collisions
     * and provide responses.
     *
     * @param dt the amount of elapsed game time since the last frame.
     */
    public abstract void gameUpdate(float dt);

    /**
     * Enable texture flip, since all loaded textures are upside down.
     */
    public void enableTextureTransforms() {
        gl.glPushAttrib(GL_TRANSFORM_BIT);
        gl.glMatrixMode(GL_TEXTURE);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glTranslatef(0.5f, 0.5f, 0.0f);
        gl.glRotatef(180, 1, 0, 0);
        gl.glTranslatef(-0.5f, -0.5f, 0.0f);
        gl.glPopAttrib();
    }

    /**
     * Disable texture flip, since all loaded textures are upside down.
     */
    public void disableTextureTransforms() {
        gl.glMatrixMode(GL_TEXTURE);
        gl.glPopMatrix();
    }
}
