package jot.demo.gui;

import static com.jogamp.newt.event.KeyEvent.VK_ESCAPE;
import static com.jogamp.newt.event.MouseEvent.BUTTON1;
import static com.jogamp.newt.event.MouseEvent.BUTTON3;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import static com.jogamp.opengl.util.gl2.GLUT.BITMAP_HELVETICA_18;
import com.jogamp.opengl.util.texture.Texture;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.demo.util.GameConstants.setGameConstants;
import jot.gui.AbstractGame;
import static jot.gui.AbstractGame.State.GAME_OVER;
import static jot.gui.AbstractGame.State.INITIALIZED;
import static jot.gui.AbstractGame.State.LOADING;
import static jot.gui.AbstractGame.State.PLAYING;
import jot.gui.camera.Camera;
import static jot.gui.camera.Camera.CameraType.PERSPECTIVE;
import static jot.io.data.format.AbstractGenericFormat.loadTexture;
import jot.manager.scene.SceneManager;
import jot.physics.fluids.hybrid.Rain;
import jot.physics.fluids.hybrid.Ripple;
import static jot.util.ExtensionPhysicsOptions.useRain;
import static jot.util.ExtensionPhysicsOptions.useRipple;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that performs the main game loop.
 *
 * @author G. Amador & A. Gomes
 */
public class GameMainRippleEffect extends AbstractGame {

    static final Logger log = getLogger("GameMainRippleEffect");

    //Number of Updates Per Second, i.e., UPS.
    private static final int UPDATE_RATE = 25;
    private static final int CANVAS_WIDTH = 1_280;
    private static final int CANVAS_HEIGHT = 900;

    //Solver variables
    private static final int n = 100; //cell count in x an y

    /**
     * The main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        setGameConstants();

        //Default Logger level is INFO
        log.setLevel(INFO);
        //log.setLevel(Level.OFF);
        //log.setLevel(Level.WARNING);

        AbstractGame game = new GameMainRippleEffect();
        game.gameRun(UPDATE_RATE);
    }

    private Texture texture;

    private GLUT glut;
    private final int size = 200;

    //Display options
    private long updateCount = 0;

    private Rain rain;
    private Ripple ripple;
    private final int xstart = 50;
    private final int ystart = 50;

    /**
     * Constructor.
     *
     */
    public GameMainRippleEffect() {
        super("Ripple Effect", CANVAS_WIDTH, CANVAS_HEIGHT);

        if (useRipple) {
            ripple = new Ripple(n, size, 1_000);
        }
        if (useRain) {
            rain = new Rain(n, size);
        }

        //activate the input module log output
        //log.log(INFO, "Setting up input controllers output (keyboard and mouse).");
        //getLogger("MouseHandler").setLevel(ALL);
        //getLogger("KeyBoardHandler").setLevel(ALL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameInit() {
        mouseHandler.registerInputEvent("Mouse left button", BUTTON1);
        mouseHandler.registerInputEvent("Mouse right button", BUTTON3);
        keyBoardHandler.registerInputEvent("Quit", 'Q');
        keyBoardHandler.registerInputEvent("Quit", 'q');
        keyBoardHandler.registerInputEvent("Quit", VK_ESCAPE);

        //TODO: more options
        state = INITIALIZED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameLoadContent() {
        state = LOADING;

        log.log(INFO, "Setting up sceneManager...");
        log.log(INFO, "Load model textures.");
        sceneManager = new SceneManager(null);
        sceneManager.setSceneSize(size);

        //Setup camera
        float aspectRatio = (float) CANVAS_WIDTH / CANVAS_HEIGHT;
        sceneManager.addCamera(new Camera(60, aspectRatio, size, "PERSPECTIVE", PERSPECTIVE));
        sceneManager.setCamera2Use("PERSPECTIVE");
        //Vector3D cameraLookAt = new Vector3D(-size / 2, size / 2, 0);
        //Vector3D cameraTranslation = new Vector3D(size * -.8, size * 0.35, size * -.8);                    
        //Vector3D cameraTranslation = new Vector3D(-size / 2, size / 2, -size * 2);
        Vector3D cameraLookAt = new Vector3D(-size / 2, 0, -size / 2);
        Vector3D cameraTranslation = new Vector3D(-size / 2, size, -size + size / 3);
        sceneManager.getCamera("PERSPECTIVE").setCameraViewPoint(cameraLookAt);
        sceneManager.getCamera("PERSPECTIVE").setCameraTranslation(cameraTranslation);
        log.log(INFO, "Camera setup done.");

        texture = loadTexture("assets/textures/skybox/World_01/SkyBox2_down.jpg");

        state = PLAYING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameProcessInput() {
        //log.log(INFO, "Process Input.");
        if (state == INITIALIZED || state == PLAYING) {
            if (keyBoardHandler.isDetecting("Quit")) {
                gameShutdown();
            }

            //TODO: clean up the code
            //Vector3D pos = mouseHandler.getPosition();
            if (mouseHandler.isContinuouslyDetecting("Mouse left button")) {
//                    frozen = !frozen;
//                    Vector3D pos = mouseHandler.getPosition();
//
//                    //get index for fluid cell under mouse position
//                    int i = (int) ((pos.getX() / GAME_CANVAS_WIDTH) * n + 1);
//                    int j = (int) (((GAME_CANVAS_HEIGHT - pos.getY()) / GAME_CANVAS_HEIGHT) * n + 1);
//
//                    //set boundries
//                    if (i > n) {
//                        i = n;
//                    }
//                    if (i < 1) {
//                        i = 1;
//                    }
//                    if (j > n) {
//                        j = n;
//                    }
//                    if (j < 1) {
//                        j = 1;
//                    }

                if (useRipple) {
                    ripple.disturb(xstart, ystart);
                }

                if (useRain) {
                    rain.intensity += 1;
                }
            }
            if (mouseHandler.isContinuouslyDetecting("Mouse right button")
                    && useRain && rain.intensity > 0) {
                rain.intensity -= 1;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param gl
     */
    @Override
    public void gameRender(GL2 gl) {
        //log.log(INFO, "Render.");
        switch (state) {
            case INITIALIZED:
                log.log(INFO, "Initialized ...");
                log.log(INFO, "Loading ...");
                gameLoadContent();
                log.log(INFO, "Loaded ...\n");
                log.log(INFO, "Options:");
                log.log(INFO, "Mouse left button - to increase rain intensity.");
                log.log(INFO, "Mouse right button - to decrease rain intensity.");
                log.log(INFO, "Esc or Q/q - to quit.\n");
                //TODO: (SceneManagement Extras) loading menu.
                break;
            case PLAYING:
                if (glu == null) {
                    glu = new GLU();
                }

                gl.glDisable(GL_LIGHTING);
                 {
                    //Draw red lines
                    gl.glPushMatrix();
                    gl.glColor3f(1, 0, 0);
                    gl.glBegin(GL_LINES);
                    gl.glVertex3f(0, 0, 0);
                    gl.glVertex3f(-size, 0, 0);
                    gl.glVertex3f(0, 0, 0);
                    gl.glVertex3f(0, size, 0);
                    gl.glVertex3f(0, 0, 0);
                    gl.glVertex3f(0, 0, -size);
                    gl.glEnd();
                    gl.glPopMatrix();
                }
                gl.glEnable(GL_LIGHTING);
                 {
                    //Draw rain or ripple
                    if (useRipple) {
                        ripple.render(gl);
                    }

                    if (useRain) {
                        rain.render(gl);
                    }

                    //Draw floor with texture
                    texture.enable(gl);
                    texture.bind(gl);
                    gl.glPushMatrix();
                    gl.glBegin(GL_TRIANGLES);
                    {
                        //Triangle 1
                        gl.glTexCoord2f(0f, 0f);
                        gl.glVertex3f(0, 0, 0);
                        gl.glTexCoord2f(0f, 1f);
                        gl.glVertex3f(0, 0, -size);
                        gl.glTexCoord2f(1f, 1f);
                        gl.glVertex3f(-size, 0, -size);

                        //Triangle 2
                        gl.glTexCoord2f(0f, 0f);
                        gl.glVertex3f(0, 0, 0);
                        gl.glTexCoord2f(1f, 1f);
                        gl.glVertex3f(-size, 0, -size);
                        gl.glTexCoord2f(1f, 0f);
                        gl.glVertex3f(-size, 0, 0);
                    }
                    gl.glEnd();
                    gl.glPopMatrix();
                    texture.disable(gl);
                }
                gl.glDisable(GL_LIGHTING);
                 {
                    if (useRain) {
                        if (glut == null) {
                            glut = new GLUT();
                        }

                        gl.glColor3f(1, 0, 0);
                        gl.glPushMatrix();
                        {
                            //TODO: improve when rescale occours.
                            gl.glRasterPos3d(-126.0, 5.0, -180.0);
                            glut.glutBitmapString(BITMAP_HELVETICA_18, "Rain ripples amount: " + rain.ripplesCount);

                            gl.glRasterPos3d(-126.0, 5.0, -190.0);
                            glut.glutBitmapString(BITMAP_HELVETICA_18, "Rain intensity: " + rain.intensity);
                            gl.glFlush();
                        }
                        gl.glPopMatrix();
                    }
                }
                gl.glEnable(GL_LIGHTING);
                break;
            case PAUSED:
                //......
                break;
            case GAME_OVER:
                //......
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameShutdown() {
        log.log(INFO, "Closing...");
        super.gameShutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameUpdate() {
        //log.log(INFO, "Update.");
        if (state == GAME_OVER) {
            return;
        }

        if (state == PLAYING) {
            if (updateCount == UPDATE_RATE) {
                updateCount = 0;
            }
            updateCount++;

            if (useRipple) {
                ripple.update(8);

            }
            if (useRain) {
                rain.update(8);
            }
            //sceneManager.updateCamera();
        }
    }
}
