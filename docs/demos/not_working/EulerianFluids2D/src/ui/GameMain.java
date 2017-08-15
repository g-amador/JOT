package ui;

import com.jogamp.opengl.util.gl2.GLUT;
import input.devices.MouseKeyBoardHandler;
import com.jogamp.opengl.GL2;
import javax.vecmath.Point3f;
import physics.EulerianFluids2D.FluidSolverEulerian2D;

/**
 * Class that performs the main game loop.
 *
 * @author G. Amador & A. Gomes
 */
public class GameMain extends GameUI {

    private GLUT glut;
    protected String gameName;  //the name for the game window/frammouseKeyBoardHandler.    
    // Solver variables    
    protected int n = 128; //cell count in x an y
    protected float d = 8; //cell dimension
    protected float dt = 0.2f; //time step
    protected float force = 0.5f;
    protected float source = 100.0f;
    protected FluidSolverEulerian2D fs = new FluidSolverEulerian2D();
    protected float h;  // cell dimensions    
    protected float dx, dy; // cell position    
    protected float u, v;   // fluid velocity
    private boolean vkey = false;   // flag to display velocity field
    private Point3f posOld; // mouse old position    
    protected MouseKeyBoardHandler mouseKeyBoardHandler;
    protected boolean windowActive;
    protected static final int CANVAS_WIDTH = 800;      // width of the game screen
    protected static final int CANVAS_HEIGHT = 800;     // heigth of the game screen
    protected static final int GAME_UPDATE_RATE = 50;     // number of Frames or Updates Per Second, i.mouseKeyBoardHandler., FPS or UPS.
    protected static final long GAME_UPDATE_PERIOD = 1000000000L / GAME_UPDATE_RATE;  // nanoseconds.   
    protected long lastTimeStamp = System.currentTimeMillis();
    protected long beginTime, endTime, updateCount = 0;

    /**
     * Constructor, receives a player unique id, mays set up (if a valid AI is
     * chosen) the AI to use, and configures a communication node with a
     * personalized configuration (if provided a valid path to a configuration
     * file).
     *
     * @param pId player unique id.
     * @param aiLogic the AI logic to usmouseKeyBoardHandler.
     */
    public GameMain(String pId, String aiLogic) {
        state = State.LOADING;

        //playerId = pId;     //the players ID, i.mouseKeyBoardHandler., user name
        gameName = "JMOGEProject";

        //activate the input module
        System.out.println("Setting up input controllers (keyboard and mouse).");
        mouseKeyBoardHandler = new MouseKeyBoardHandler();

        posOld = mouseKeyBoardHandler.getPosition();

        resetSimulation();

        System.out.println("Options:");
        System.out.println("V/v key: Set flag for rawing velocity field.");
        System.out.println("R/r key: Reset solver.");
        System.out.println(":/. key: Increase timestep.");
        System.out.println(";/, key: Reduce timestep.");
    }

    @Override
    public void gameInit() {
    }

    @Override
    public void gameShutdown(GL2 gl) {
        System.out.println("Closing...");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
        }
        System.exit(0);
    }

    @Override
    public void gameUpdate() {

        if (state == State.GAME_OVER) {
            return;
        }

        if (state == State.PLAYING) {
            //System.err.println("UpdatmouseKeyBoardHandler.");
            if (updateCount == GAME_UPDATE_RATE) {
                updateCount = 0;
            }
            updateCount++;

            synchronized (this) {
                // set flag for drawing velocity field
                if (mouseKeyBoardHandler.isDetecting("v") || mouseKeyBoardHandler.isDetecting("V")) {
                    //System.out.println("Pressed v");
                    vkey = !vkey;
                }

                // reset solver
                if (mouseKeyBoardHandler.isDetecting("r") || mouseKeyBoardHandler.isDetecting("R")) {
                    //System.out.println("Pressed r");
                    fs.reset();
                }

                // increase timestep
                if (mouseKeyBoardHandler.isDetecting(".") || mouseKeyBoardHandler.isDetecting(":")) {
                    //System.out.println("Pressed . or :");
                    if (dt > 1) {
                        return;
                    }

                    dt += 0.05f;

                    // kill fp errors
                    dt = (float) Math.round(dt * 100);
                    dt /= 100;

                    fs.dt = dt;
                }

                // reduce timestep
                if (mouseKeyBoardHandler.isDetecting(",") || mouseKeyBoardHandler.isDetecting(";")) {
                    //System.out.println("Pressed , or ;");
                    if (dt < 0.1f) {
                        return;
                    }

                    dt -= 0.05f;

                    // kill fp errors
                    dt = (float) Math.round(dt * 100);
                    dt /= 100;

                    fs.dt = dt;
                }

                if (mouseKeyBoardHandler.isDetecting("button1") || mouseKeyBoardHandler.isDetecting("button3")) {
                    updateLocation();
                }

                // solve fluid
                fs.velocitySolver();
                fs.densitySolver();
            }
        }
    }

    @Override
    public void gameRender(GL2 gl) {
        synchronized (this) {
            //System.err.println("Render.");
            switch (state) {
                case LOADING:
                    //System.out.println("Loading ...");
                    //TODO: (SceneManagement Extras) loading menu.
                    break;
                case INITIALIZED:
                    //System.out.println("Loaded ...");
                    state = State.PLAYING;
                    break;
                case PLAYING:
                    //Draw red lines
                    gl.glPushMatrix();
                    gl.glColor3f(1, 0, 0);
                    gl.glBegin(GL2.GL_LINES);
                    gl.glVertex3f(0, 0, 0);
                    gl.glVertex3f(8, 0, 0);
                    gl.glVertex3f(0, 0, 0);
                    gl.glVertex3f(0, 8, 0);
                    gl.glVertex3f(0, 0, 0);
                    gl.glVertex3f(0, 0, 8);
                    gl.glEnd();
                    gl.glPopMatrix();

                    // draw status
                    gl.glPushMatrix();
                    if (glut == null) {
                        glut = new GLUT();
                    }

                    gl.glColor3f(1, 0, 0);
                    gl.glRasterPos2d(6.4f, -0.7f);
                    glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Grid: " + n + "x" + n);
                    gl.glRasterPos2d(6.4f, -1.1f);
                    glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Timestep: " + dt);
                    gl.glFlush();
                    gl.glPopMatrix();

                    //draw fluid density/velocity
                    for (int i = 1; i <= n; i++) {
                        // x position of current cell
                        dx = ((i - 0.5f) * h);
                        for (int j = 1; j <= n; j++) {
                            // y position of current cell
                            dy = ((j - 0.5f) * h);

                            // draw velocity field  
                            drawVelocity(gl, i, j);

                            // draw density   
                            drawDensity(gl, i, j);
                        }
                    }
                    break;
                case PAUSED:
                    // ......
                    break;
                case GAME_OVER:
                    // ......
                    break;
            }
        }
    }

    /**
     * Method to reset the simulation.
     */
    private void resetSimulation() {
        // calculate cell deimensions
        h = d / n;

        fs.setup(n, dt);
    }

    /**
     *
     */
    //private void updateLocation(MouseEvent e) {
    private void updateLocation() {
        Point3f pos = mouseKeyBoardHandler.getPosition();
        // get index for fluid cell under mouse position
        int i = (int) ((pos.x / (float) CANVAS_HEIGHT) * n + 1);
        int j = (int) (((CANVAS_WIDTH - pos.y) / (float) CANVAS_WIDTH) * n + 1);

        // set boundries
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

        // add density or velocity
        if (mouseKeyBoardHandler.isDetecting("button1")) {

            fs.dOld[fs.I(i, j)] = source;
        }

        if (mouseKeyBoardHandler.isDetecting("button3")) {
            fs.uOld[fs.I(i, j)] = force * (pos.x - posOld.x);
            fs.vOld[fs.I(i, j)] = force * (pos.y - posOld.y);
        }

        posOld = new Point3f(pos);
    }

    /**
     *
     * @param gl
     * @param i
     * @param j
     */
    private void drawVelocity(GL2 gl, int i, int j) {
        //System.out.println(vkey);
        if (vkey) {
            u = fs.u[fs.I(i, j)];
            v = fs.v[fs.I(i, j)];

            gl.glPushMatrix();
            gl.glColor3f(1, 0, 0);
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex2f(dx, dy);
            gl.glVertex2f(dx + u, dy + v);
            gl.glEnd();
            gl.glPopMatrix();
        }
    }

    /**
     *
     * @param gl
     * @param i
     * @param j
     */
    private void drawDensity(GL2 gl, int i, int j) {
        gl.glPushMatrix();
        gl.glColor3f(1 - fs.d[fs.I(i, j)], 1 - fs.d[fs.I(i, j)], 1 - fs.d[fs.I(i, j)]);
        gl.glRectf(dx, dy, dx + h, dy + h);
        gl.glPopMatrix();
    }
}
