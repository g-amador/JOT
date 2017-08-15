package jot.demo.gui;

import static com.jogamp.newt.event.KeyEvent.VK_ESCAPE;
import static com.jogamp.newt.event.MouseEvent.BUTTON1;
import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static java.lang.Math.random;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.demo.util.GameConstants.setGameConstants;
import jot.geometry.TransformGroup;
import static jot.geometry.bounding.A_BoundingVolume.BoundingVolumeType.OBB;
import jot.gui.A_Game;
import static jot.gui.A_Game.State.GAME_OVER;
import static jot.gui.A_Game.State.INITIALIZED;
import static jot.gui.A_Game.State.LOADING;
import static jot.gui.A_Game.State.PLAYING;
import jot.gui.Camera;
import static jot.gui.Camera.CameraType.FIRST_PERSON;
import static jot.gui.Camera.CameraType.PERSPECTIVE;
import static jot.gui.Camera.CameraType.PERSPECTIVE_FOLLOW;
import static jot.gui.Camera.CameraType.THIRD_PERSON;
import static jot.gui.Camera.CameraType.UPPER_VIEW;
import static jot.gui.Camera.CameraType.UPPER_VIEW_FOLLOW;
import jot.manager.SimpleSceneManager;
import jot.physics.particle_system.explosion.ParticleSystem;
import static jot.util.ExtensionPhysicsOptions.useBlockCentroid;
import static jot.util.FrameworkOptions.Floor;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that performs the main game loop.
 *
 * @author G. Amador & A. Gomes
 */
public class GameMainExplosion extends A_Game {

    private static final Logger log = getLogger("GameMainExplosion");

    //Number of Frames or Updates Per Second, i.e., FPS or UPS.
    private static final int UPDATE_RATE = 25;
    private static final int CANVAS_WIDTH = 1_280;
    private static final int CANVAS_HEIGHT = 900;
    //private static final int CANVAS_WIDTH = 800;
    //private static final int CANVAS_HEIGHT = 600;

    /**
     * The main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        setGameConstants();

        log.setLevel(INFO);
        //log.setLevel(WARNING);

        A_Game game = null;
        while (game == null) {
            //if user Id provided and ...
            if (args.length >= 1) {
                game = new GameMainExplosion(args[0]);
            } else {
                game = new GameMainExplosion("player");
            }
        }
        game.gameRun(UPDATE_RATE);
    }

    /**
     * The id for the player running on this node.
     */
    public String playerId;

    private final float size = 100;
    private final Vector3D velocity = new Vector3D(0.5f, 0.5f, 0.5f);   //the velocity for any moving avatar or bullet in the  simpleSceneManager.
    private final ConcurrentHashMap<String, TransformGroup> models = new ConcurrentHashMap<>();
    private final ArrayList<ParticleSystem> particleSystems;

    /**
     * The last time an updated was performed.
     */
    protected long lastTimeStamp = currentTimeMillis();

    /**
     * Auxiliary time counters and update counter.
     */
    protected long beginTime, endTime, updateCount = 0;

    private boolean generate = true;

    /**
     * Constructor, receives a player unique id, mays set up (if a valid AI is
     * chosen) the AI to use, and configures a communication node with a
     * personalized configuration (if provided a valid path to a configuration
     * file).
     *
     * @param pId player unique Id.
     */
    public GameMainExplosion(String pId) {
        super("Explosion Effect", CANVAS_WIDTH, CANVAS_HEIGHT);

        this.playerId = pId;     //the players Id, i.e., user name

        //Setup particle system.
        particleSystems = new ArrayList<>();

        //Activate the input module log output
        //log.info("Setting up input controllers output (keyboard and mouse).");
        //getLogger("MouseHandler").setLevel(ALL);
        //getLogger("KeyBoardHandler").setLevel(ALL);
    }

    @Override
    public void gameInit() {
        state = LOADING;

        log.info("Setting up simpleSceneManager...");
        log.info("Load model textures.");
        ConcurrentHashMap<String, String> texturesPaths = new ConcurrentHashMap<>();
        texturesPaths.put("cow", "assets/models/dae/cow/Cow.png");
        texturesPaths.put("duck", "assets/models/dae/duck/duckCM.jpg");
        texturesPaths.put("brick1", "assets/textures/materials/brick1.jpg");
        simpleSceneManager = new SimpleSceneManager(texturesPaths);
        simpleSceneManager.setSceneSize(size);

        //Cameras setup
        simpleSceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "FIRST_PERSON", FIRST_PERSON));
        simpleSceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "THIRD_PERSON", THIRD_PERSON));
        simpleSceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "PERSPECTIVE", PERSPECTIVE));
        simpleSceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "PERSPECTIVE_FOLLOW", PERSPECTIVE_FOLLOW));
        simpleSceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "UPPER_VIEW", UPPER_VIEW));
        simpleSceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "UPPER_VIEW_FOLLOW", UPPER_VIEW_FOLLOW));
        //simpleSceneManager.setCamera2Use("FIRST_PERSON");
        //simpleSceneManager.setCamera2Use("THIRD_PERSON");
        //simpleSceneManager.setCamera2Use("UPPER_VIEW");
        simpleSceneManager.setCamera2Use("PERSPECTIVE");
        simpleSceneManager.getCamera("PERSPECTIVE").setCameraTranslation(new Vector3D(-15, 5, -15));
        //simpleSceneManager.getCamera("PERSPECTIVE").setCameraTranslation(new Vector3D(-5, 5, -5));
        log.info("Cameras setup done.");

        //Setup floor
        if (Floor) {
            TransformGroup floor;
            floor = simpleSceneManager.buildFloor(size, "assets/textures/skybox/World_01/SkyBox2_down.jpg");
            //floor.setRotation(new Vector3D(-90, 0, 0));
            floor.updateTranslation(new Vector3D(0, size / 2, 0));

            simpleSceneManager.setFloor(floor);
            log.info("Floor created.");
        }

        if (useBlockCentroid) {
            //Setup obstacle
            GameObject obstacle = simpleSceneManager.buildBlock("obstacle", "brick1", 1.0f, OBB);
            obstacle.setTranslation(new Vector3D(0, 1.0f, 0));
            obstacle.getBoundingVolume(0).setRenderable(true);
            simpleSceneManager.addImmutableObject(obstacle);
            log.info("Obstacle added");
        }

        //Setup models        
        models.put("cow", simpleSceneManager.loadFormat("assets/models/dae/cow/", "cow.dae", 1, OBB));
        models.get("cow").setRotationZ(90.0f);
        models.get("cow").setRotationX(-90.0f);

        models.put("duck", simpleSceneManager.loadFormat("assets/models/dae/duck/", "duck_triangulate.dae", 250, OBB));
        models.get("duck").setRotationY(-90.0f);

        models.put("duke", simpleSceneManager.loadFormat("assets/models/dae/", "Duke_posed.dae", 1, OBB));
        models.get("duke").setRotationY(180);

        try {
            TransformGroup playerRotate = models.get("duke").clone();
            GameObject player = new GameObject(playerId, models.get("duke"));
            //player.setBoundingVolume(0, null);
            //playerRotate.setBoundingVolume(0, null);
            //player.getBoundingVolume(0).setRenderBoundingVolume(true);
            player.addChild(playerRotate);
            player.setHealth(100);
            player.setVelocity(velocity);
            player.updateTranslation(new Vector3D(0, 0, -10));
            simpleSceneManager.addPlayer(player);
            simpleSceneManager.setLocalPlayerId(playerId);
            log.log(INFO, format("%s added to the simpleSceneManager.", playerId));

            if (!useBlockCentroid) {
                TransformGroup player2Rotate = models.get("cow").clone();
                GameObject player2 = new GameObject("cow", models.get("cow"));
                //TransformGroup player2Rotate = models.get("duck").clone();
                //GameObject player2 = new GameObject("duck", models.get("duck"));
                //player2.setBoundingVolume(0, null);
                //player2Rotate.setBoundingVolume(0, null);
                //player2.getBoundingVolume(0).setRenderBoundingVolume(true);
                player2.addChild(player2Rotate);
                player2.setHealth(100);
                player2.setVelocity(velocity);
                player2.updateTranslation(new Vector3D(0, 0, 0));
                simpleSceneManager.addPlayer(player2);
                log.info("dead added to the simpleSceneManager.");
            }
        } catch (CloneNotSupportedException ex) {
            log.log(SEVERE, null, ex);
        }

        //Register input events to keys
        keyBoardHandler.registerInputEvent("1", '1');
        keyBoardHandler.registerInputEvent("2", '2');
        keyBoardHandler.registerInputEvent("3", '3');
        keyBoardHandler.registerInputEvent("Quit", 'Q');
        keyBoardHandler.registerInputEvent("Quit", 'q');
        keyBoardHandler.registerInputEvent("Quit", VK_ESCAPE);

        //Register input events to mouse buttons
        mouseHandler.registerInputEvent("left", BUTTON1);

        if (!"true".equals(getProperty("JOT.scenerotate"))) {
            log.info("");
            log.info("Options");
            log.info("1 - toggle on/off lights.");
            log.info("2 - toggle on/off textures.");
            log.info("3 - toggle on/off wireframe.");
            log.info("Left mouse button - add particle explosion.");
            log.info("Esc or Q/q - to quit.");
        }

        state = INITIALIZED;
    }

    @Override
    public void gameProcessInput() {
        //log.info("Process Input.");
        //synchronized (this) {
        if (state == INITIALIZED || state == PLAYING) {
            if (keyBoardHandler.isDetecting("Quit")) {
                gameShutdown();
            }

            if (keyBoardHandler.isDetecting("1")) {
                simpleSceneManager.setLights();
            }

            if (keyBoardHandler.isDetecting("2")) {
                simpleSceneManager.setShowTextures();
            }

            if (keyBoardHandler.isDetecting("3")) {
                simpleSceneManager.setShowWireframe();
            }

            if (mouseHandler.isDetecting("left") && generate) {
                Vector3D acc0 = new Vector3D(
                        random() * .1,
                        random() * .1,
                        random() * .1);

                Vector3D pos0;
                if (useBlockCentroid) {
                    pos0 = simpleSceneManager.getGameObject("obstacle").getPresentTranslation();
                } else {
                    pos0 = simpleSceneManager.getGameObject("cow").getPresentTranslation();
                }

//                        Vector3D pos0 = new Vector3D(mouseHandler.getPosition().toArray());
//                        pos0 = new Vector3D(
//                                -(pos0.getX() / GAME_CANVAS_WIDTH) * size,
//                                ((GAME_CANVAS_HEIGHT - pos0.getY()) / GAME_CANVAS_HEIGHT) * size,
//                                -1);
                particleSystems.add(new ParticleSystem(1_000, (float) random() * 5, 4, acc0, pos0, ZERO));
                if (useBlockCentroid) {
                    acc0 = new Vector3D(
                            random() + 5,
                            random() + 5,
                            random() + 5);
                    particleSystems.add(new ParticleSystem(simpleSceneManager.getGameObject("obstacle"), 4, acc0));
                    simpleSceneManager.removeImmutableObject(simpleSceneManager.getGameObject("obstacle"));
                } else {
                    particleSystems.add(new ParticleSystem(simpleSceneManager.getPlayer("cow"), 4, acc0));
                    simpleSceneManager.removePlayer(simpleSceneManager.getGameObject("cow"));
                }
                generate = false;
            }
        }
        //}
    }

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

                simpleSceneManager.render(gl);

                gl.glDisable(GL_LIGHTING);

                //Draw red lines
                gl.glPushMatrix();
                gl.glColor3f(1, 0, 0);
                gl.glBegin(GL_LINES);

                gl.glColor3f(1, 0, 0);
                gl.glVertex3f(0, 0, 0);
                gl.glVertex3f(-size, 0, 0);

                gl.glColor3f(0, 1, 0);
                gl.glVertex3f(0, 0, 0);
                gl.glVertex3f(0, size, 0);

                gl.glColor3f(0, 0, 1);
                gl.glVertex3f(0, 0, 0);
                gl.glVertex3f(0, 0, -size);

                gl.glEnd();
                gl.glPopMatrix();

                if (particleSystems != null && !particleSystems.isEmpty()) {
                    //Java 7
                    //for (ParticleSystem ps : particleSystems) {
                    //Java 8
                    particleSystems.forEach(ps -> {
                        ps.render(gl);
                    });
                }
                gl.glEnable(GL_LIGHTING);

                break;
            case PAUSED:
                // ......
                break;
            case GAME_OVER:
                // ......
                break;
        }
        //}
    }

    @Override
    public void gameShutdown() {
        log.info("Closing...");
        super.gameShutdown();
    }

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

            if (particleSystems != null
                    && !particleSystems.isEmpty()) {
                ArrayList<ParticleSystem> particleSystemsToRemove = new ArrayList<>();
                //Java 7
                //for (ParticleSystem ps : particleSystems) {
                //Java 8
                particleSystems.forEach(ps -> {
                    ps.update(0.02f);
                    if (ps.isDead()) {
                        particleSystemsToRemove.add(ps);
                    }
                });

                //Java 7
                //for (ParticleSystem ps : particleSystemsToRemove) {
                //Java 8
                particleSystemsToRemove.forEach(ps -> {
                    particleSystems.remove(ps);
                });
                simpleSceneManager.updateCamera();
            }
        }
        //}
    }
}
