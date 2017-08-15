package jot.demo.gui;

import static com.jogamp.newt.event.KeyEvent.VK_ESCAPE;
import static com.jogamp.newt.event.MouseEvent.BUTTON1;
import static com.jogamp.newt.event.MouseEvent.BUTTON3;
import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import static com.jogamp.opengl.util.gl2.GLUT.BITMAP_HELVETICA_18;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.random;
import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.demo.util.GameConstants.setGameConstants;
import jot.gui.A_Game;
import static jot.gui.A_Game.State.GAME_OVER;
import static jot.gui.A_Game.State.INITIALIZED;
import static jot.gui.A_Game.State.LOADING;
import static jot.gui.A_Game.State.PLAYING;
import jot.gui.Camera;
import static jot.gui.Camera.CameraType.PERSPECTIVE;
import jot.manager.SimpleSceneManager;
import static jot.physics.particle_system.sph.Particle.particlesCount;
import jot.physics.particle_system.sph.ParticleSystem;
import static jot.physics.particle_system.sph.ParticleSystem.GHEIGHT;
import static jot.physics.particle_system.sph.ParticleSystem.GWIDTH;
import static jot.physics.particle_system.sph.ParticleSystem.TIMESTEP;
import static jot.physics.particle_system.sph.ParticleSystem.WORLD_SCALE;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that performs the main game loop.
 *
 * @author G. Amador & A. Gomes
 */
public class GameMainSPH extends A_Game {

    private static final Logger log = getLogger("GameMainSPH");

    //Number of Frames or Updates Per Second, i.e., FPS or UPS.
    private static final int UPDATE_RATE = 60;
    private static final int CANVAS_WIDTH = GWIDTH;
    private static final int CANVAS_HEIGHT = GHEIGHT;

    /**
     * The main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        setGameConstants();

        log.setLevel(INFO);
        //log.setLevel(WARNING);

        A_Game game = new GameMainSPH();
        game.gameRun(UPDATE_RATE);
    }

    private GLUT glut;
    private final float size = 100;
    private final ParticleSystem particleSystem;

    /**
     * The last time an updated was performed.
     */
    protected long lastTimeStamp = currentTimeMillis();

    /**
     * Auxiliary time counters and update counter.
     */
    protected long beginTime, endTime, updateCount = 0;

    /**
     * Constructor, receives a player unique id, mays set up (if a valid AI is
     * chosen) the AI to use, and configures a communication node with a
     * personalized configuration (if provided a valid path to a configuration
     * file).
     *
     */
    public GameMainSPH() {
        super("SPH Fluid Simulation", CANVAS_WIDTH, CANVAS_HEIGHT);

        //Setup particle system.
        particleSystem = new ParticleSystem();
        particleSystem.reset();

        //Activate the input module log output
        //log.info("Setting up input controllers output (keyboard and mouse).");
        //getLogger("MouseHandler").setLevel(ALL);
        //getLogger("KeyBoardHandler").setLevel(ALL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameInit() {
        state = LOADING;

        log.info("Setting up simpleSceneManager...");
        log.info("Load model textures.");
        simpleSceneManager = new SimpleSceneManager(null);
        simpleSceneManager.setSceneSize(size);

        //Setup camera
        simpleSceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "PERSPECTIVE", PERSPECTIVE));
        simpleSceneManager.setCamera2Use("PERSPECTIVE");
        //Vector3D cameraTranslation = new Vector3D(size * -.8, size * 0.35, size * -.8);
        //Vector3D cameraTranslation = new Vector3D(-size / 2, size / 2, -size * 4);
        Vector3D cameraTranslation = new Vector3D(-size / 2, size / 2, -size);
        //Vector3D cameraTranslation = new Vector3D(-size / 2, size / 2, - size - size / 10);
        simpleSceneManager.getCamera("PERSPECTIVE").setCameraViewPoint(
                new Vector3D(-size / 2, size / 2, 0));
        simpleSceneManager.getCamera("PERSPECTIVE").setCameraTranslation(cameraTranslation);
        log.info("Camera setup done.");

        //Register input events to keys
        keyBoardHandler.registerInputEvent("R", 'r');
        keyBoardHandler.registerInputEvent("r", 'r');
        keyBoardHandler.registerInputEvent("+TimeStep", ':');
        keyBoardHandler.registerInputEvent("+TimeStep", '.');
        keyBoardHandler.registerInputEvent("-TimeStep", ';');
        keyBoardHandler.registerInputEvent("-TimeStep", ',');
        keyBoardHandler.registerInputEvent("Quit", 'Q');
        keyBoardHandler.registerInputEvent("Quit", 'q');
        keyBoardHandler.registerInputEvent("Quit", VK_ESCAPE);

        //Register input events to mouse buttons
        mouseHandler.registerInputEvent("left", BUTTON1);
        mouseHandler.registerInputEvent("right", BUTTON3);

        log.info("Options:");
        log.info("R/r key: Reset solver.");
        log.info(":/. key: Increase timestep.");
        log.info(";/, key: Reduce timestep.");
        log.info("Left mouse button - add fluid particles.");
        log.info("Right mouse button - add osbtacle particles.");
        log.info("Esc or Q/q - to quit.");

        state = INITIALIZED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameProcessInput() {
        //log.info("Process Input.");
        //synchronized (this) {
        if (state == INITIALIZED || state == PLAYING) {
            if (keyBoardHandler.isDetecting("Quit")) {
                gameShutdown();
            }

            // reset solver
            if (keyBoardHandler.isDetecting("r") || keyBoardHandler.isDetecting("R")) {
                particleSystem.reset();
            }

            // increase timestep
            if (keyBoardHandler.isDetecting("+TimeStep")) {
                if (TIMESTEP > 1) {
                    return;
                }

                TIMESTEP += 0.05f;

                // kill fp errors
                TIMESTEP = round(TIMESTEP * 100);
                TIMESTEP /= 100;
            }

            // reduce timestep
            if (keyBoardHandler.isDetecting("-TimeStep")) {
                if (TIMESTEP < 0.1f) {
                    return;
                }

                TIMESTEP -= 0.05f;

                // kill fp errors
                TIMESTEP = round(TIMESTEP * 100);
                TIMESTEP /= 100;
            }

            //Add some water
            if (mouseHandler.isDetecting("left")) {
                Vector3D pos = new Vector3D(mouseHandler.getPosition().toArray());
                particleSystem.freeRect(
                        (float) pos.getX() - 41, (float) pos.getY() - 41,
                        (float) pos.getX() + 41, (float) pos.getY() + 41);

                for (double x = max(pos.getX() - 40, 0);
                        x <= min(pos.getX() + 40, GAME_CANVAS_WIDTH);
                        x += 30 * WORLD_SCALE) {
                    for (double y = max(pos.getY() - 40, 0);
                            y <= min(pos.getY() + 40, GAME_CANVAS_HEIGHT);
                            y += 30 * WORLD_SCALE) {
                        particleSystem.AddParticle(new Vector3D(
                                x + random() - 0.5,
                                y + random() - 0.5,
                                0.0f));
                    }
                }
            }

            //Place a wall
            if (mouseHandler.isDetecting("right")) {
                Vector3D pos = new Vector3D(mouseHandler.getPosition().toArray());
                particleSystem.freeRect(
                        (float) pos.getX() - 9, (float) pos.getY() - 9,
                        (float) pos.getX() + 9, (float) pos.getY() + 9);

                for (double x = max(pos.getX() - 10, 0);
                        x <= min(pos.getX() + 10, GAME_CANVAS_WIDTH);
                        x += 30 * WORLD_SCALE) {
                    for (double y = max(pos.getY() - 10, 0);
                            //((GAME_CANVAS_HEIGHT - pos.getY()) / GAME_CANVAS_HEIGHT)
                            y <= min(pos.getY() + 10, GAME_CANVAS_HEIGHT);
                            y += 30 * WORLD_SCALE) {
                        particleSystem.AddBoundaryParticle(new Vector3D(x, y, 0.0f));
                    }
                }
            }
        }
        //}
    }

    /**
     * {@inheritDoc}
     *
     * @param gl
     */
    @Override
    public void gameRender(GL2 gl) {
        //synchronized (this) {
        //log.info("Render.");
        switch (state) {
            case LOADING:
                log.info("Loading ...");
                //TODO: (SceneManagement Extras) loading menu.
                break;
            case INITIALIZED:
                log.info("Loaded ...");
                state = PLAYING;
                break;
            case PLAYING:
                if (glu == null) {
                    glu = new GLU();
                }

                gl.glDisable(GL_LIGHTING);
                gl.glPushMatrix();
                 {
                    //Draw red lines
                    gl.glColor3f(1, 0, 0);
                    gl.glBegin(GL_LINES);
                    {
                        gl.glColor3f(1, 0, 0);
                        gl.glVertex3f(0, 0, 0);
                        gl.glVertex3f(-size, 0, 0);

                        gl.glColor3f(0, 1, 0);
                        gl.glVertex3f(0, 0, 0);
                        gl.glVertex3f(0, size, 0);

                        gl.glColor3f(0, 0, 1);
                        gl.glVertex3f(0, 0, 0);
                        gl.glVertex3f(0, 0, -size);
                    }
                    gl.glEnd();
                }
                gl.glPopMatrix();
                gl.glEnable(GL_LIGHTING);

                gl.glPushMatrix();
                 {
                    gl.glRotatef(180, 0, 1, 0);
                    gl.glRotatef(180, 1, 0, 0);

                    if (GWIDTH == 800 && GHEIGHT == 600) {
                        gl.glScalef(0.12f, 0.12f, 0.12f);
                        gl.glTranslated(-2, -610, 0);
                    }

                    if (GWIDTH == 1_920 && GHEIGHT == 1_080) {
                        gl.glScalef(0.08f, 0.08f, 0.08f);
                        gl.glTranslated(-2, -1_080, 0);
                    }

                    particleSystem.render(gl);
                }
                gl.glPopMatrix();

                gl.glDisable(GL_LIGHTING);
                if (glut == null) {
                    glut = new GLUT();
                }
                gl.glPushMatrix();
                 {
                    //TODO: impreve when rescale occours.
                    gl.glColor3f(1, 0, 0);
                    if (GWIDTH == 800 && GHEIGHT == 600) {
                        gl.glRasterPos2d(-88.0, 6.0);
                    }
                    if (GWIDTH == 1_920 && GHEIGHT == 1_080) {
                        gl.glRasterPos2d(-88.0, 6.0);
                    }
                    glut.glutBitmapString(BITMAP_HELVETICA_18, "Particles: " + particlesCount);

                    gl.glColor3f(1, 0, 0);
                    if (GWIDTH == 800 && GHEIGHT == 600) {
                        gl.glRasterPos2d(-88.0, 2.0);
                    }
                    if (GWIDTH == 1_920 && GHEIGHT == 1_080) {
                        gl.glRasterPos2d(-88.0, 2.0);
                    }
                    glut.glutBitmapString(BITMAP_HELVETICA_18, "Timestep: " + TIMESTEP);
                    gl.glFlush();
                }
                gl.glPopMatrix();
                gl.glEnable(GL_LIGHTING);

                break;
            case PAUSED:
                //......
                break;
            case GAME_OVER:
                //......
                break;
        }
        //}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameShutdown() {
        log.info("Closing...");
        super.gameShutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameUpdate() {
        //synchronized (this) {
        if (state == GAME_OVER) {
            return;
        }

        if (state == PLAYING) {
            //log.info("Update.");
            if (updateCount == UPDATE_RATE) {
                updateCount = 0;
            }
            updateCount++;

            particleSystem.update(updateCount / 60);

            //simpleSceneManager.updateCamera();
        }
        //}
    }
}
