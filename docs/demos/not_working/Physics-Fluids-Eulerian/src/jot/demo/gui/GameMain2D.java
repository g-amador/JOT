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
import static java.lang.Math.round;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.demo.util.GameConstants.setGameConstants;
import jot.gui.Game;
import static jot.gui.Game.State.GAME_OVER;
import static jot.gui.Game.State.INITIALIZED;
import static jot.gui.Game.State.LOADING;
import static jot.gui.Game.State.PLAYING;
import jot.gui.camera.Camera;
import jot.manager.SceneManager;
import jot.physics.fluids.eulerian.AbstractEulerianFluidSolver2D;
import jot.physics.fluids.eulerian.PracticalFluids2D;
import jot.physics.fluids.eulerian.StableFluids2D;
import jot.physics.fluids.eulerian.linear_solvers.ConjugateGradient2D;
import jot.physics.fluids.eulerian.linear_solvers.GaussSeidel2D;
import jot.physics.fluids.eulerian.linear_solvers.Jacobi2D;
import jot.physics.fluids.eulerian.linear_solvers.MultiGrid2D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that performs the main game loop.
 *
 * @author G. Amador & A. Gomes
 */
public class GameMain2D extends Game {

    private static final Logger log = getLogger("GameMain2D");

    //Number of Frames or Updates Per Second, i.e., FPS or UPS.
    private static final int UPDATE_RATE = 25;

    private static final int CANVAS_WIDTH = 1_080;
    private static final int CANVAS_HEIGHT = 1_080;
    //private static final int CANVAS_WIDTH = 600;
    //private static final int CANVAS_HEIGHT = 600;

    /**
     * The main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        setGameConstants();

        log.setLevel(INFO);
        //log.setLevel(Level.WARNING);

        Game game = new GameMain2D();
        game.gameRun(UPDATE_RATE);
    }

    private GLUT glut;
    private final float size = 200;

    //Solver variables    
    private float dt;
    private final int n = 128; //cell count in x an y
    private final float d = 200; //cell dimension
    //private float visc = 0.0f;
    //private float diff = 0.0f;
    private float force;
    private float source;

    //private float h;  //cell dimensions        
    //the fluids solver
    private AbstractEulerianFluidSolver2D fs;

    //Display options
    private boolean vkey = false;   //flag to display velocity field
    private Vector3D posOld; //mouse old position    
    private long updateCount = 0;

    /**
     * Constructor.
     *
     */
    public GameMain2D() {
        super("Eulerian Fluids", CANVAS_WIDTH, CANVAS_HEIGHT);

        //activate the io devices output
        //log.info("Setting up input controllers (keyboard and mouse) loggers.");
        //getLogger("MouseHandler").setLevel(Level.ALL);
        //getLogger("KeyBoardHandler").setLevel(Level.ALL);
        posOld = mouseHandler.getPosition();

        if (useStableFluids) {
            fs = new StableFluids2D();
        }

        if (usePracticalFluids) {
            fs = new PracticalFluids2D();
        }

        //calculate cell deimensions
        fs.h = d / n;

        if (useStableFluids) {
            dt = 0.2f; //time step       
            force = 0.5f;
            source = 100.0f;

            fs.diffusion_iterations = 20;
            if (useJacobiDiffusion) {
                fs.setup(n, dt, new Jacobi2D(n));
            } else if (useGaussSeidelDiffusion) {
                fs.setup(n, dt, new GaussSeidel2D(n));
            } else if (useConjugateGradientDiffusion) {
                fs.setup(n, dt, new ConjugateGradient2D(n));
            } else if (useMultiGridJacobiDiffusion) {
                fs.setup(n, dt, new MultiGrid2D(n, new Jacobi2D(n)));
            } else if (useMultiGridGaussSeidelDiffusion) {
                fs.setup(n, dt, new MultiGrid2D(n, new GaussSeidel2D(n)));
            } else if (useMultiGridConjugateGradientDiffusion) {
                fs.setup(n, dt, new MultiGrid2D(n, new ConjugateGradient2D(n)));
            }

            ((StableFluids2D) fs).projection_iterations = 20;
            if (useJacobiProjection) {
                ((StableFluids2D) fs).linearSolverProjection = new Jacobi2D(n);
            } else if (useGaussSeidelProjection) {
                ((StableFluids2D) fs).linearSolverProjection = new GaussSeidel2D(n);
            } else if (useConjugateGradientProjection) {
                ((StableFluids2D) fs).linearSolverProjection = new ConjugateGradient2D(n);
            } else if (useMultiGridJacobiProjection) {
                ((StableFluids2D) fs).linearSolverProjection = new MultiGrid2D(n, new Jacobi2D(n));
            } else if (useMultiGridGaussSeidelProjection) {
                ((StableFluids2D) fs).linearSolverProjection = new MultiGrid2D(n, new GaussSeidel2D(n));
            } else if (useMultiGridConjugateGradientProjection) {
                ((StableFluids2D) fs).linearSolverProjection = new MultiGrid2D(n, new ConjugateGradient2D(n));
            }
        }

        if (usePracticalFluids) {
            dt = 0.02f; //time step
            force = 2.5f;
            source = 1.5f;

            fs.diffusion_iterations = 1;
            if (useJacobiDiffusion) {
                fs.setup(n, dt, new Jacobi2D(n));
            } else if (useGaussSeidelDiffusion) {
                fs.setup(n, dt, new GaussSeidel2D(n));
            } else if (useConjugateGradientDiffusion) {
                fs.setup(n, dt, new ConjugateGradient2D(n));
            } else if (useMultiGridJacobiDiffusion) {
                fs.setup(n, dt, new MultiGrid2D(n, new Jacobi2D(n)));
            } else if (useMultiGridGaussSeidelDiffusion) {
                fs.setup(n, dt, new MultiGrid2D(n, new GaussSeidel2D(n)));
            } else if (useMultiGridConjugateGradientDiffusion) {
                fs.setup(n, dt, new MultiGrid2D(n, new ConjugateGradient2D(n)));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameInit() {
        //log.info("Init.");
        state = LOADING;

        log.info("Setting up SimpleSceneManager...");
        sceneManager = new SceneManager(null);
        sceneManager.setSceneSize(size);

        //Setup camera
        sceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "PERSPECTIVE", PERSPECTIVE));
        sceneManager.setCamera2Use("PERSPECTIVE");
        sceneManager.getCamera("PERSPECTIVE").setCameraViewPoint(
                new Vector3D(-size / 2, size / 2, 0));
        sceneManager.getCamera("PERSPECTIVE").setCameraTranslation(
                new Vector3D(-size / 2, size / 2, -size - size / 10));
        log.info("Camera setup done.");

        //Register input keys
        keyBoardHandler.registerInputEvent("V", 'V');
        keyBoardHandler.registerInputEvent("V", 'v');
        keyBoardHandler.registerInputEvent("R", 'R');
        keyBoardHandler.registerInputEvent("R", 'r');
        keyBoardHandler.registerInputEvent("IncDt", ':');
        keyBoardHandler.registerInputEvent("IncDt", '.');
        keyBoardHandler.registerInputEvent("DecDt", ';');
        keyBoardHandler.registerInputEvent("DecDt", ',');
        keyBoardHandler.registerInputEvent("Quit", 'Q');
        keyBoardHandler.registerInputEvent("Quit", 'q');
        keyBoardHandler.registerInputEvent("Quit", VK_ESCAPE);

        mouseHandler.registerInputEvent("Left Button", BUTTON1);
        mouseHandler.registerInputEvent("Right Button", BUTTON3);

        log.info("Options:");
        log.info("V/v key: Set flag for rawing velocity field.");
        log.info("R/r key: Reset solver.");
        log.info(":/. key: Increase timestep.");
        log.info(";/, key: Reduce timestep.");
        log.info("Left mouse button to add fluid.");
        log.info("Right mouse button to add velocity.");
        log.info("Esc or Q/q to quit.");
        //TODO: more options

        state = INITIALIZED;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void gameLoadContent() {
        state = LOADING;
     
        //TODO: code here
        
        state = PLAYING;        
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

            //set flag for drawing velocity field
            if (keyBoardHandler.isDetecting("V")) {
                //log.info("Pressed v");
                vkey = !vkey;
            }

            //reset solver
            if (keyBoardHandler.isDetecting("R")) {
                //log.info("Pressed r");
                fs.reset();
            }

            //increase timestep
            if (keyBoardHandler.isDetecting("IncDt")) {
                //log.info("Pressed . or :");
                if (dt > 1) {
                    return;
                }

                dt += 0.001f;

                //kill fp errors
                dt = round(dt * 1_000);
                dt /= 1_000;

                fs.dt = dt;
            }

            //reduce timestep
            if (keyBoardHandler.isDetecting("DecDt")) {
                //log.info("Pressed , or ;");
                if (dt < 0.001f) {
                    return;
                }

                dt -= 0.001f;

                //kill fp errors
                dt = round(dt * 1_000);
                dt /= 1_000;

                fs.dt = dt;
            }

            updateLocation();
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
                //TODO: (Extras) loading menu.
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

                    //draw status
                    gl.glPushMatrix();
                    //TODO: impreve when rescale occours.
                    if (glut == null) {
                        glut = new GLUT();
                    }

                    gl.glColor3f(1, 0, 0);
                    gl.glRasterPos2d(-185.f, -6.f);
                    glut.glutBitmapString(BITMAP_HELVETICA_18, "Grid: " + n + "x" + n);
                    gl.glRasterPos2d(-185.f, -12.f);
                    glut.glutBitmapString(BITMAP_HELVETICA_18, "Timestep: " + dt);
                    gl.glFlush();
                    gl.glPopMatrix();

                    if (vkey) {
                        //draw fluid velocity field                      
                        fs.drawVelocity(gl);
                    }

                    //draw fluid density   
                    fs.drawDensity(gl);
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
    public void gameUpdate(float dt) {
        //synchronized (this) {
        //log.info("Update.");
        if (state == GAME_OVER) {
            return;
        }

        if (state == PLAYING) {
            //log.info("UpdatmouseKeyBoardHandler.");
            if (updateCount == UPDATE_RATE) {
                updateCount = 0;
            }
            updateCount++;

            if (usePracticalFluids) {
//                    //Presets for a smoke effect                
//                    ((PracticalFluids2D) fs).d_force = .4f;
//                    ((PracticalFluids2D) fs).d_advection = 136.0f;
//                    ((PracticalFluids2D) fs).vorticity = .36f;
//
//                //Presets for a large ground detonation effect
//                //fs.reset();
//                //fs.SetSimulationDefaults();
//                fs.SetField(fs.pressureOld, 1.0f); //should be 1.0f, lower is more like gas, higher less compressible)
//                fs.SetField(fs.heatOld, 1.0f); //should be 1.0f, lower is more like gas, higher less compressible)
//                fs.ink_force = 3.2f;
//                fs.ink_advection = 348.0f;
//                fs.velocity_advection = 400.0f;
//                fs.vorticity = 0.27f;
////               fs.VelX = 0.0f;
////               fs.VelY = 3.8f;
////               fs.VelZ = 0.0f;
//                fs.heat_force = 0.31f;
//                fs.heat_advection = 248.0f;
//                fs.heat_diffusion = 4.6f;
//                
//                //Presets for a large scale fog effect
//                //fs.reset();
//                //fs.SetSimulationDefaults();
//                fs.SetField(fs.pressureOld, 1.0f); //should be 1.0f, lower is more like gas, higher less compressible)
//                fs.SetField(fs.heatOld, 1.0f); //should be 1.0f, lower is more like gas, higher less compressible)                
//                fs.ink_force = 10.0f;
//                fs.ink_advection = 160.0f;
//                fs.velocity_advection = 250.0f;
//                fs.vorticity = 0.50f;
//
//                //Presets for a fast explosion and smoke simulation
//                //fs.reset();
//                //fs.SetSimulationDefaults();
//                fs.SetField(fs.pressureOld, 1.0f); //should be 1.0f, lower is more like gas, higher less compressible)
//                fs.SetField(fs.heatOld, 1.0f); //should be 1.0f, lower is more like gas, higher less compressible)                
//                fs.ink_force = 10.0f;
//                fs.ink_advection = 360.0f;
//                fs.vorticity = 0.17f;
//                
//                //Presets for ????
//                ((PracticalFluids2D)fs).SetSimulationDefaults();
//
//                ((PracticalFluids2D)fs).velX = 0.0f;
//                ((PracticalFluids2D)fs).VelY = 2.0f;
//                ((PracticalFluids2D)fs).ink_force =  1.0f;
//                ((PracticalFluids2D)fs).ink_advection = 200.0f;
//                ((PracticalFluids2D)fs).vorticity = 0.15f;
//                float y = 0;
//
//                float m;
//                m = sin(g_fTotalElapsedDemoTime);
//                x += m * 2;
//                y = 0;                
//                //Default simulation velocity impulse               
//                ((PracticalFluids2D) fs).DistributeFloatingPoint(fs.uOld, n / 2, n / 2, 0.5f);
//                ((PracticalFluids2D) fs).DistributeFloatingPoint(fs.vOld, n / 2, n / 2, 0.5f);
//
//                //Drop the ink in
//                ((PracticalFluids2D) fs).DistributeFloatingPoint(
//                        fs.dOld, n / 2, n / 2, ((PracticalFluids2D) fs).d_force);
//                if (updateCount == 1) {
//                    ((PracticalFluids2D) fs).SetField(fs.dOld, 0.0f);
//                }
            }

            fs.update();
                    //fs.uOld = new float[n * n];
            //fs.vOld = new float[n * n];
            //fs.inkOld = new float[n * n];

            //simpleSceneManager.updateCamera();
        }
        //}
    }

    /**
     *
     */
    private void updateLocation() {
        Vector3D pos = mouseHandler.getPosition();

        //get index for fluid cell under mouse position
        int i = (int) ((pos.getX() / GAME_CANVAS_WIDTH) * n + 1);
        int j = (int) (((GAME_CANVAS_HEIGHT - pos.getY()) / GAME_CANVAS_HEIGHT) * n + 1);

        //set boundries
        if (i > n) {
            i = n;
        }
        if (i < 1) {
            i = 1;
        }
        if (j > n) {
            j = n;
        }
        if (j < 1) {
            j = 1;
        }

        //add density or velocity
        if (mouseHandler.isContinuouslyDetecting("Left Button")) {
            //log.info("Mouse Left Button Continuously Pressed");                                    
            fs.dOld[fs.I(i, j)] = source;
        }

        if (mouseHandler.isContinuouslyDetecting("Right Button")) {
            //log.info("Mouse Right Button Continuously Pressed");                                    
            fs.uOld[fs.I(i, j)] = (float) (force * (pos.getX() - posOld.getX()));
            fs.vOld[fs.I(i, j)] = (float) (force * (pos.getY() - posOld.getY()));
        }

        posOld = pos;
    }
}
