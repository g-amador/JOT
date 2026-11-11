package gui;

import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import com.jogamp.opengl.GL2;
import jot.gui.Game;
import jot.io.device.handlers.KeyBoard;
import jot.manager.SceneManager;
import static util.GameConstants.setGameConstants;

/**
 * Class that performs the main game loop.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class GameMain extends Game {

    private static final Logger log = getLogger(GameMain.class.getName());
    private static final int UPDATE_RATE = 33; //Number of Frames or Updates Per Second, i.e., FPS or UPS.
    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 600;

    /**
     * The main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        setGameConstants();

        log.setLevel(Level.INFO);
        //log.setLevel(Level.WARNING);

        A_Game game;// = null;
        //while (game == null) {
        //if user ID provided and ...
        if (args.length >= 1) {
            game = new GameMain(args[0]);
        } else {
            game = new GameMain("");
        }
        //}    

        game.gameRun(UPDATE_RATE);
    }

    /**
     * The id for the player running on this node.
     */
    public String playerID;

    /**
     * The scene manager object.
     */
    public SimpleSceneManager sceneManager;

    private final float size = 100;

    /**
     * Constructor, receives a player unique id, other parameters can be
     * included.
     *
     * @param pID player unique id.
     */
    public GameMain(String pID) {
        super("Project template", CANVAS_WIDTH, CANVAS_HEIGHT);

        this.playerID = pID;

        //activate the keyboard module
        log.info("Setting up input controller keyboard.");
        keyBoardHandler = new KeyBoardHandler();

        //TODO: activate modules, i.e., controller, AI, etc.
    }

    @Override
    public void gameInit() {
        //TODO: setup load models/textures.

        //TODO: setup sceneManager.        
        sceneManager = new SimpleSceneManager(null);
        sceneManager.setLocalPlayerID(playerID);

        //TODO: setup camera(s)
        sceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "UPPER_VIEW", Camera.CameraType.UPPER_VIEW_FOLLOW));
        sceneManager.setCameraToUse("UPPER_VIEW");

        //TODO: setup light(s)
        //TODO: setup floor, load HOG2 map, or generate maze/terrain.        
        //TODO: setup skyBox/skyDome
        //TODO: setup model(s)    
        log.info("Options:");
        //TODO: more options here.
        log.info("Q/q quit.");
    }
    
    @Override
    public void gameLoadContent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void gameProcessInput() {
        synchronized (this) {
            if (state == State.INITIALIZED || state == State.PLAYING) {
                if (keyBoardHandler.isDetecting("Q") || keyBoardHandler.isDetecting("q")) {
                    gameShutdown(gl);
                }

                //TODO: input options here.
            }
        }
    }

    @Override
    public void gameRender(GL2 gl) {
        synchronized (this) {
            switch (state) {
                case LOADING:
                    //TODO: loading menu.
                    break;
                case INITIALIZED:
                    //TODO: start menu here ...
                    state = State.PLAYING;
                    break;
                case PLAYING:
                    //or here ...
                    sceneManager.render(gl);
                    break;
                case PAUSED:
                    //TODO: pause menu here.
                    break;
                case GAME_OVER:
                    //TODO: game over menu here.
                    break;
            }
        }
    }

    @Override
    public void gameShutdown() {
        log.info("Closing ...");
        super.gameShutdown();
    }

    @Override
    public void gameUpdate(float dt) {
        synchronized (this) {
            if (state == State.GAME_OVER) {
                return;
            }

            if (state == State.PLAYING) {
                //TODO: (optional) code imput behaviour
                //TODO: (optional) code physics update
                //TODO: (optional) code AI update
                //TODO: (optional) code network update
                //sceneManager.updateCamera();
            }
        }
    }
}
