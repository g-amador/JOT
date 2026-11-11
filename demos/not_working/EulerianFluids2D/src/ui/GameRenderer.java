package ui;

import com.jogamp.opengl.util.Animator;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;

/**
 * Class that performs game render and window management tasks, i.e., implements
 * JOGL and frame/window methods. The main class of the game engine.
 *
 * @author G. Amador & A. Gomes
 */
public class GameRenderer extends GameMain implements GLEventListener, WindowListener {

    private GLU glu;
    private FPSCounter fps;         //fps counter object
    private Frame frame;
    private long sleepTime, overTime = 0, timeBeyondPeriod = 0;

    /**
     * Constructor, receives a player unique id, mays set up (if a valid AI is
     * chosen) the AI to use, and configures a communication node with a
     * personalized configuration (if provided a valid path to a configuration
     * file).
     *
     * @param pId player unique id.
     * @param aiLogic the AI logic to use.
     * @param gridConfig a valid path to a configuration file.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public GameRenderer(String pId, String aiLogic, String gridConfig) {
        super(pId, aiLogic);

        frame = new Frame(gameName);
        frame.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        frame.setLocationRelativeTo(null);  //Middle of screen
        frame.setBackground(Color.white);
        GLCanvas canvas = new GLCanvas();
        frame.addWindowListener(this);
        canvas.addGLEventListener(this);

        canvas.addKeyListener(mouseKeyBoardHandler);
        canvas.addMouseListener(mouseKeyBoardHandler);
        canvas.addMouseMotionListener(mouseKeyBoardHandler);
        System.out.println("Done!");

        //Off mouse cursor render
        //BufferedImage cursorImage = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
        //canvas.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(3, 3), "invisible"));

        frame.add(canvas);
        frame.setVisible(true);
        canvas.requestFocus();
        Animator animator = new Animator(canvas);
        animator.start();

        state = State.INITIALIZED;
    }

    /**
     * The main method. Calls the class constructor and then starts the game
     * loop, i.e., a thread.
     *
     * @param args the command line or shell arguments
     */
    public static void main(String[] args) {

        /**
         * Setup gameRenderer
         */
        GameRenderer gameRenderer = null;

        while (gameRenderer == null) {
            //if user Id provided and ...
            if (args.length >= 1) {
                //if only user Id provid
                gameRenderer = new GameRenderer(args[0], "", "");
            } else {
                System.setProperty("JMOGE.scenerotate", "true");

                //if neither user id, A.I. proper logic, or grid config provided.
                gameRenderer = new GameRenderer("player", "scenerotate", "singleplayer");
            }
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        //System.out.println("Init!");
        GL2 gl = (GL2) drawable.getGL();

        gl.glEnable(GL2.GL_LINE_SMOOTH);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_DONT_CARE);
        gl.glLineWidth(1.5f);

        gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glMatrixMode(GL2.GL_MODELVIEW);

        fps = new FPSCounter(drawable, 36);
        fps.setColor(1, 0, 0, 1);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        //System.err.println("Dispose.");
        GL2 gl = (GL2) drawable.getGL();
        gameShutdown(gl);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        //System.out.println("Display!");
        GL2 gl = (GL2) drawable.getGL();

        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        beginTime = System.nanoTime();

        gameUpdate();
        gameRender(gl);

        endTime = System.nanoTime();

        sleepTime = GAME_UPDATE_PERIOD - (endTime - beginTime) - overTime; //in nanoseconds.
        if (sleepTime > 0) {
            try {
                //Provides the necessary delay and also yields control so that other thread can do work.
                Thread.sleep(sleepTime / 1000000); //in milliseconds
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
                //e.printStackTrace();
            }
            overTime = System.nanoTime() - endTime - sleepTime;
        } else {
            timeBeyondPeriod -= sleepTime;
            overTime = 0;

            while (timeBeyondPeriod > GAME_UPDATE_PERIOD) {
                gameUpdate();
                //timeBeyondPeriod = -GAME_UPDATE_PERIOD;
                timeBeyondPeriod -= GAME_UPDATE_PERIOD;
            }
        }

        //try {
        //   Thread.sleep(20);
        //} catch (InterruptedException e) {
        //   //e.printStackTrace();
        //}

        //Use the FPS object renderer to render the fps.
        fps.render();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        //System.out.println("Reshape!");
        GL2 gl = (GL2) drawable.getGL();

        if (height == 0) {
            height = 1;
        }

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        /*
         * note we divide our width by our height to get the aspect ratio
         */
        if (glu == null) {
            glu = new GLU();
        }

        gl.glTranslatef(-0.65f, -0.65f, 0);

        glu.gluPerspective(25, width / height, 1, 100);
        glu.gluLookAt(0.0, 0.0, 25.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
        //glu.gluLookAt(25.0, 25.0, 25.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        frame.dispose();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
        windowActive = true;
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        windowActive = false;
    }
}