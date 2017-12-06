package jot.demo.gui;

import static com.jogamp.newt.event.KeyEvent.VK_ESCAPE;
import static com.jogamp.newt.event.MouseEvent.BUTTON1;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.demo.util.GameConstants.setGameConstants;
import jot.gui.Game;
import static jot.gui.Game.State.GAME_OVER;
import static jot.gui.Game.State.INITIALIZED;
import static jot.gui.Game.State.LOADING;
import static jot.gui.Game.State.PLAYING;
import jot.gui.camera.FirstPerson;
import jot.gui.camera.Perspective;
import jot.gui.camera.PerspectiveFollow;
import jot.gui.camera.ThirdPerson;
import jot.gui.camera.UpperView;
import jot.gui.camera.UpperViewFollow;
import jot.manager.SceneManager;
import jot.math.geometry.TransformGroup;
import jot.math.geometry.bounding.AbstractBoundingVolume;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.OBB;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.SPHERE;
import static jot.physics.Kinematics.translatePolar;
import jot.physics.Light;
import static jot.util.FrameworkOptions.frameworkOptions;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that performs the main game loop.
 *
 * @author G. Amador & A. Gomes
 */
public class GameMain extends Game {

    private static final Logger log = getLogger("GameMain");

    //Number of Frames or Updates Per Second, i.e., FPS or UPS.
    private static final int UPDATE_RATE = 40;

    //private static final int CANVAS_WIDTH = 1_280;
    //private static final int CANVAS_HEIGHT = 900;
    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 600;

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

        Game game = null;
        while (game == null) {
            //if user Id provided and ...
            if (args.length >= 1) {
                game = new GameMain(args[0]);
            } else {
                setProperty("JOT.scenerotate", "true");
                game = new GameMain("player");
            }
        }
        game.gameRun(UPDATE_RATE);
    }

    /**
     * The id for the player running on this node.
     */
    public String playerId;

    private int bulletIdSequence = 1;
    //private boolean LeftShoot = false;
    private final float sceneSize = 100;
    private final Vector3D velocity = new Vector3D(0.5f, 0.5f, 0.5f);   //the velocity for any moving avatar or bullet in the sceneManager.
    private final ConcurrentHashMap<String, TransformGroup> models = new ConcurrentHashMap<>();

    /**
     * The last time an updated was performed.
     */
    protected long lastTimeStamp = currentTimeMillis();

    /**
     * Auxiliary time counters and update counter.
     */
    protected long beginTime, endTime;

    /**
     * Background color
     */
    //private final float[] clearColorWhite = {1.0f, 1.0f, 1.0f, 1.0f};       //White.
    //private final float[] clearColorPurple = {0.3f, 0.3f, 0.7f, 0.0f};      //Purple.
    private final float[] clearColorPitchblack = {0.3f, 0.3f, 0.3f, 0.3f};  //Pitch black.

    private float direction;
    boolean isMoving, isRotating;

    AbstractBoundingVolume.BoundingVolumeType modelsBoundingBoxType = OBB;

    /**
     * Constructor, receives a player unique id, mays set up (if a valid AI is
     * chosen) the AI to use, and configures a communication node with a
     * personalized configuration (if provided a valid path to a configuration
     * file).
     *
     * @param pId player unique Id.
     */
    public GameMain(String pId) {
        super("Collisions", CANVAS_WIDTH, CANVAS_HEIGHT);

        this.playerId = pId;     //the players Id, i.e., user name
        //LeftShoot = true;   //the first shoot that this players does should be from its left side
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameInit() {
        //clearColor = clearColorWhite;
        //clearColor = clearColorPurple;
        this.clearColor = this.clearColorPitchblack;

        //Register input events to keys
        if (null == getProperty("JOT.scenerotate")) {
            this.keyBoard.registerInputEvent("1", '1');
            this.keyBoard.registerInputEvent("2", '2');
            this.keyBoard.registerInputEvent("3", '3');
            this.keyBoard.registerInputEvent("4", '4');
            this.keyBoard.registerInputEvent("5", '5');
            this.keyBoard.registerInputEvent("6", '6');
            this.keyBoard.registerInputEvent("7", '7');
            this.keyBoard.registerInputEvent("8", '8');
            this.keyBoard.registerInputEvent("+", '+');
            this.keyBoard.registerInputEvent("-", '-');
            this.keyBoard.registerInputEvent("Z", 'Z');
            this.keyBoard.registerInputEvent("Z", 'z');
            this.keyBoard.registerInputEvent("X", 'X');
            this.keyBoard.registerInputEvent("X", 'x');
            this.keyBoard.registerInputEvent("C", 'C');
            this.keyBoard.registerInputEvent("C", 'c');
            this.keyBoard.registerInputEvent("Y", 'Y');
            this.keyBoard.registerInputEvent("Y", 'y');
            this.keyBoard.registerInputEvent("I", 'I');
            this.keyBoard.registerInputEvent("I", 'i');
            this.keyBoard.registerInputEvent("B", 'B');
            this.keyBoard.registerInputEvent("B", 'b');
            this.keyBoard.registerInputEvent("N", 'N');
            this.keyBoard.registerInputEvent("N", 'n');
            this.keyBoard.registerInputEvent("W", 'W');
            this.keyBoard.registerInputEvent("W", 'w');
            this.keyBoard.registerInputEvent("A", 'A');
            this.keyBoard.registerInputEvent("A", 'a');
            this.keyBoard.registerInputEvent("S", 'S');
            this.keyBoard.registerInputEvent("S", 's');
            this.keyBoard.registerInputEvent("D", 'D');
            this.keyBoard.registerInputEvent("D", 'd');
            this.keyBoard.registerInputEvent("U", 'U');
            this.keyBoard.registerInputEvent("U", 'u');
            this.keyBoard.registerInputEvent("H", 'H');
            this.keyBoard.registerInputEvent("H", 'h');
            this.keyBoard.registerInputEvent("J", 'J');
            this.keyBoard.registerInputEvent("J", 'j');
            this.keyBoard.registerInputEvent("K", 'K');
            this.keyBoard.registerInputEvent("K", 'k');
        }
        this.keyBoard.registerInputEvent("Quit", VK_ESCAPE);
        this.mouse.registerInputEvent("shoot", BUTTON1);

        //Set options text to display in main window.
        text.setTextLine("Options:");
        if (null == getProperty("JOT.scenerotate")) {
            if (frameworkOptions.get("useMouse")) {
                text.setTextLine("Mouse move - rotate Duke model.");
                text.setTextLine("Mouse left button - shoot.");
            }
            if (frameworkOptions.get("useKeyBoard")) {
                text.setTextLine("1 key - toggle on/off show FPS.");
                text.setTextLine("2 key - toggle on/off show help.");
                text.setTextLine("3 key - toggle on/off lights.");
                text.setTextLine("4 key - toggle on/off textures.");
                text.setTextLine("5 key - toggle on/off wireframe.");
                text.setTextLine("6 key - toggle on/off planar shadows.");
                text.setTextLine("7 key - toggle on/off shadow maps.");
                text.setTextLine("8 key - toggle on/off zBuffer.");
                text.setTextLine("Z/z key - use third person follow view camera.");
                text.setTextLine("X/x key - use perspective follow view camera.");
                text.setTextLine("C/c key - use upper view camera.");
                text.setTextLine("Y/y,I/i keys - rotate the light around the center of the scene.");
                text.setTextLine("+/- keys - translate the light down/up.");
                text.setTextLine("B/b key - toggle on/off broad phase collision detection.");
                text.setTextLine("N/n key - toggle on/off narrow phase collision detection.");
                text.setTextLine("W/w,A/a,S/s,D/d keys - to move player.");
                text.setTextLine("U/u,H/h,J/j,K/k keys - to move light.");
            }
        }
        if (frameworkOptions.get("useKeyBoard")) {
            text.setTextLine("Esc key - to quit.\n");
        }

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
        ConcurrentHashMap<String, String> texturesPaths = new ConcurrentHashMap<>();
        texturesPaths.put("cow", "assets/models/dae/cow/Cow.png");
        texturesPaths.put("duck", "assets/models/dae/duck/duckCM.jpg");
        texturesPaths.put("tie", "assets/models/dae/tie/tieskin.jpg");
        texturesPaths.put("xwing", "assets/models/dae/xwing/xwingskin.jpg");
        texturesPaths.put("brick1", "assets/textures/materials/brick1.jpg");
        texturesPaths.put("floor1", "assets/textures/materials/floor1.png");
        texturesPaths.put("floor2", "assets/textures/materials/floor2.png");
        this.sceneManager = new SceneManager(texturesPaths);
        this.sceneManager.setSceneSize(this.sceneSize);

        //Setup cameras
        //NOTE: camera Ids must differ and may not be necessarly the playerId, e.g., camera1, camera2, camera3.
        float aspectRatio = (float) CANVAS_WIDTH / CANVAS_HEIGHT;
        if ("true".equals(getProperty("JOT.scenerotate"))) {
            this.sceneManager.addCamera(new ThirdPerson(60, aspectRatio, this.sceneSize, this.playerId));
            this.sceneManager.setCamera2Use(this.playerId);
        } else {
            this.sceneManager.addCamera(new FirstPerson(60, aspectRatio, this.sceneSize, "FIRST_PERSON"));
            this.sceneManager.addCamera(new ThirdPerson(60, aspectRatio, this.sceneSize, "THIRD_PERSON"));
            this.sceneManager.addCamera(new Perspective(60, aspectRatio, this.sceneSize, "PERSPECTIVE"));
            this.sceneManager.addCamera(new PerspectiveFollow(60, aspectRatio, this.sceneSize, "PERSPECTIVE_FOLLOW"));
            this.sceneManager.addCamera(new UpperView(60, aspectRatio, this.sceneSize, "UPPER_VIEW"));
            this.sceneManager.addCamera(new UpperViewFollow(60, aspectRatio, this.sceneSize, "UPPER_VIEW_FOLLOW"));
            //sceneManager.setCamera2Use("FIRST_PERSON");
            this.sceneManager.setCamera2Use("THIRD_PERSON");
            //sceneManager.setCamera2Use("PERSPECTIVE");
            //sceneManager.setCamera2Use("UPPER_VIEW");
        }
        log.log(INFO, "Cameras setup done.");

        //Setup lights
        this.sceneManager.addLight(new Light("light0", GL_LIGHT0, null));
        this.sceneManager.setLight2Use("light0");
        log.log(INFO, "Lights setup done.");

        //Setup floor
        if (frameworkOptions.get("Floor")) {
            TransformGroup floor;
            floor = this.sceneManager.buildFloor(this.sceneSize,
                    "assets/textures/skybox/World_01/SkyBox2_down.jpg");
            //floor.setRotation(new Vector3D(-90, 0, 0));
            floor.updateTranslation(new Vector3D(0, this.sceneSize / 2, 0));
            this.sceneManager.setFloor(floor);
            log.log(INFO, "Floor created.");
        }

        //Setup skyBox
        if (frameworkOptions.get("SkyBox")) {
            TransformGroup skyBox = this.sceneManager.buildSkyBox(this.sceneSize,
                    "assets/textures/skybox/World_01/SkyBox2", "jpg");
            //skyBox.setScaling(new Vector3D(5, 5, 5));
            //skyBox.setRotation(new Vector3D(- 90, 0, 0));
            skyBox.setBoundingVolume(0, null);
            this.sceneManager.setSky(skyBox);
            log.log(INFO, "Sky Box added to the scene.");
        }

        //Setup skyDome
        if (frameworkOptions.get("SkyDome")) {
            TransformGroup skyDome = this.sceneManager.buildSkyDome(
                    this.sceneSize / 2,
                    "assets/textures/skydome/sky_povray1.jpg");
            //skyDome.setScaling(new Vector3D(5, 5, 5));
            //skyDome.setRotation(new Vector3D(- 90, 0, 0));
            skyDome.setBoundingVolume(0, null);
            this.sceneManager.setSky(skyDome);
            log.log(INFO, "Sky Dome added to the scene.");
        }

        //Setup obstacles
        int boxCount = 6;
        for (int i = 0; i < boxCount; i++) {
            float rotation = i * (360 / boxCount);
            //SPHERE or OBB or AABB            
            GameObject obstacle = this.sceneManager.buildBlock("obstacle" + (i + 1), "brick1", 1.0f, this.modelsBoundingBoxType);
            Vector3D obstaclePostion = translatePolar(ZERO, 15.0f, rotation, 0, 1);
            obstaclePostion = new Vector3D(obstaclePostion.getX(), obstaclePostion.getY() + 1, obstaclePostion.getZ());
            obstacle.setPosition(obstaclePostion);
            obstacle.getBoundingVolume(0).setRenderable(true);
            this.sceneManager.addImmutableObject(obstacle);
            log.log(INFO, format("Obstacle %d added", i + 1));
        }

        //Setup models        
        this.models.put("tie", this.assetManager.loadFormat("assets/models/dae/tie/", "tiefighter.dae", 70, this.modelsBoundingBoxType));

        this.models.put("xwing", this.assetManager.loadFormat("assets/models/dae/xwing/", "xwing.dae", 3_300, SPHERE));
        this.models.get("xwing").setRotationY(180.0f);

        this.models.put("cow", this.assetManager.loadFormat("assets/models/dae/cow/", "cow.dae", 1, this.modelsBoundingBoxType));
        //models.get("cow").setRotationY(-180);
        //models.get("cow").setRotationZ(90);
        //models.get("cow").setRotationX(90);
        this.models.get("cow").setRotationZ(90.0f);
        this.models.get("cow").setRotationX(-90.0f);

        this.models.put("duck", this.assetManager.loadFormat("assets/models/dae/duck/", "duck_triangulate.dae", 250, this.modelsBoundingBoxType));
        this.models.get("duck").setRotationY(-90.0f);

        this.models.put("bullet", this.assetManager.loadFormat("assets/models/dae/", "CoffeeBean2_out.dae", 1, this.modelsBoundingBoxType));

        try {
            TransformGroup playerRotate;
            GameObject player;
            playerRotate = this.models.get("tie").clone();
            player = new GameObject(this.playerId, this.models.get("tie"));
            player.getBoundingVolume(0).setRenderable(true);
            //playerRotate.setBoundingVolume(0, null);
            player.addChild(playerRotate);
            player.setAttribute("health", 100);
            player.setVelocity(this.velocity);
            this.sceneManager.addPlayer(player);
            this.sceneManager.setLocalPlayerId(this.playerId);
            this.sceneManager.getPlayer(this.playerId).updatePosition(new Vector3D(2, 0.5f, 2));
            log.log(INFO, format("%s added to the scene.", this.playerId));

            if (null == getProperty("JOT.scenerotate")) {
                TransformGroup player1Rotate;
                GameObject player1;
                player1Rotate = this.models.get("xwing").clone();
                player1 = new GameObject("player1", this.models.get("xwing"));
                player1.getBoundingVolume(0).setRenderable(true);
                //player1Rotate.setBoundingVolume(0, null);
                player1.addChild(player1Rotate);
                player1.setAttribute("health", 100);
                player1.setVelocity(this.velocity);
                this.sceneManager.addPlayer(player1);
                this.sceneManager.getPlayer("player1").updatePosition(new Vector3D(10, 0.5f, 10));
                log.log(INFO, "player1 added to the scene.");

                TransformGroup player2Rotate = this.models.get("duck").clone();
                GameObject player2 = new GameObject("player2", this.models.get("duck"));
                player2.getBoundingVolume(0).setRenderable(true);
                //player2Rotate.setBoundingVolume(0, null);
                player2.addChild(player2Rotate);
                player2.setAttribute("health", 100);
                player2.setVelocity(this.velocity);
                this.sceneManager.addPlayer(player2);
                //sceneManager.getPlayer("player2").updatePosition(new Vector3D(2, 0, 2));
                this.sceneManager.getPlayer("player2").updatePosition(new Vector3D(25, 0, 12));
                log.log(INFO, "player2 added to the scene.");

                TransformGroup player3Rotate = this.models.get("cow").clone();
                GameObject player3 = new GameObject("player3", this.models.get("cow"));
                player3.getBoundingVolume(0).setRenderable(true);
                //player3Rotate.setBoundingVolume(0, null);
                player3.addChild(player3Rotate);
                player3.setAttribute("health", 100);
                player3.setVelocity(this.velocity);
                this.sceneManager.addPlayer(player3);
                //sceneManager.getPlayer("player3").updatePosition(new Vector3D(-2, 0, -2));
                this.sceneManager.getPlayer("player3").updatePosition(new Vector3D(30, 0, -2));
                this.sceneManager.getPlayer("player3").updateRotationY(45);
                log.log(INFO, "player3 added to the scene.");
            }
        } catch (CloneNotSupportedException ex) {
            log.log(SEVERE, null, ex);
        }
        //FIXME: this increases quality of the shadow naos but decreases the space used by the shadowMapRenderer. Why????
        //this.shadowMapRenderer.setShadowMapResolution(2048, 2048);

        state = PLAYING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameProcessInput() {
        //log.log(INFO, "Process Input.");
        //ronized (this) {
        if (state == INITIALIZED || state == PLAYING) {
            if (this.keyBoard.isDetecting("Quit")) {
                this.gameShutdown();
            }

            if (this.keyBoard.isDetecting("1")) {
                this.sceneManager.toggleOnOffShowFPS();
            }
            if (this.keyBoard.isDetecting("2")) {
                this.sceneManager.toggleOnOffShowText();
            }
            if (this.keyBoard.isDetecting("3")) {
                this.sceneManager.toggleOnOffLights();
            }
            if (this.keyBoard.isDetecting("4")) {
                this.sceneManager.toggleOnOffShowTextures();
            }
            if (this.keyBoard.isDetecting("5")) {
                this.sceneManager.toggleOnOffShowWireframe();
            }
            if (this.keyBoard.isDetecting("6")) {
                this.sceneManager.toggleOnOffShowPlanarShadows();
            }
            if (this.keyBoard.isDetecting("7")) {
                this.sceneManager.toggleOnOffShowShadowMaps();
            }
            if (this.keyBoard.isDetecting("8")) {
                this.sceneManager.toggleOnOffShowZBuffer();
            }

            if (this.keyBoard.isDetecting("Z")) {
                this.sceneManager.setCamera2Use("THIRD_PERSON");
            }
            if (this.keyBoard.isDetecting("X")) {
                this.sceneManager.setCamera2Use("PERSPECTIVE_FOLLOW");
            }
            if (this.keyBoard.isDetecting("C")) {
                this.sceneManager.setCamera2Use("UPPER_VIEW_FOLLOW");
            }

            if (this.keyBoard.isDetecting("B")) {
                this.sceneManager.toggleOnOffBroadPhaseCollisionDetection();
            }
            if (this.keyBoard.isDetecting("N")) {
                this.sceneManager.toggleOnOffNarrowPhaseCollisionDetection();
            }

            //Light moving
            if (this.keyBoard.isContinuouslyDetecting("Y")) {
                this.sceneManager.getLight("light0").updateLightRotation(gl, -1);
            }
            if (this.keyBoard.isContinuouslyDetecting("I")) {
                this.sceneManager.getLight("light0").updateLightRotation(gl, 1);
            }
            if (this.keyBoard.isContinuouslyDetecting("+")) {
                this.sceneManager.getLight("light0").updateLightPosition(gl, 0, 1, 0);
            }
            if (this.keyBoard.isContinuouslyDetecting("-")) {
                this.sceneManager.getLight("light0").updateLightPosition(gl, 0, -1, 0);
            }
            if (this.keyBoard.isContinuouslyDetecting("U")) {
                this.sceneManager.getLight("light0").updateLightPosition(gl, 0, 0, -1);
            }
            if (this.keyBoard.isContinuouslyDetecting("J")) {
                this.sceneManager.getLight("light0").updateLightPosition(gl, 0, 0, 1);
            }
            if (this.keyBoard.isContinuouslyDetecting("H")) {
                this.sceneManager.getLight("light0").updateLightPosition(gl, -1, 0, 0);
            }
            if (this.keyBoard.isContinuouslyDetecting("K")) {
                this.sceneManager.getLight("light0").updateLightPosition(gl, 1, 0, 0);
            }

            boolean wDown = this.keyBoard.isContinuouslyDetecting("W");
            boolean sDown = this.keyBoard.isContinuouslyDetecting("S");
            boolean aDown = this.keyBoard.isContinuouslyDetecting("A");
            boolean dDown = this.keyBoard.isContinuouslyDetecting("D");
            //log.log(INFO, format("%b %b %b %b", wDown, sDown, dDown, aDown));

            if (wDown && sDown) {
                wDown = false;
                sDown = false;
            }
            if (aDown && dDown) {
                aDown = false;
                dDown = false;
            }

            //Moving & rotating
            this.direction = 0;
            this.isMoving = false;
            this.isRotating = false;

            if (wDown || sDown || aDown || dDown) {
                this.isMoving = true;
                if (wDown) {
                    if (aDown) {
                        this.direction = 45;
                    } else if (dDown) {
                        this.direction = -45;
                    } else {
                        this.direction = 0;
                    }
                } else if (sDown) {
                    if (aDown) {
                        this.direction = 135;
                    } else if (dDown) {
                        this.direction = -135;
                    } else {
                        this.direction = 180;
                    }
                } else if (aDown) {
                    this.direction = 90;
                } else if (dDown) {
                    this.direction = -90;
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
        //log.log(INFO, "Render.");
        switch (state) {
            case INITIALIZED:
                log.log(INFO, "Initialized ...");
                log.log(INFO, "Loading ...");
                this.gameLoadContent();
                log.log(INFO, "Loaded ...\n");
                //TODO: (SceneManagement Extras) loading menu.
                break;
            case PLAYING:
                this.sceneManager.render(gl);
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
        log.log(INFO, "Closing...");
        super.gameShutdown();
    }

    /**
     * {@inheritDoc}
     *
     * @param dt
     */
    @Override
    public void gameUpdate(float dt) {
        //synchronized (this) {
        if ("true".equals(getProperty("JOT.scenerotate"))) {
            if (this.sceneManager != null) {
                Vector3D cameraRotation = this.sceneManager.getCamera(this.playerId).getRotation();
                cameraRotation = new Vector3D(cameraRotation.getX(), cameraRotation.getY() + .3, cameraRotation.getZ());
                this.sceneManager.getCamera(this.playerId).setRotation(cameraRotation);
                this.sceneManager.updateCamera(1);
            }
        } else {
            if (state == GAME_OVER) {
                return;
            }

            if (state == PLAYING) {
                //log.log(INFO, "Update.");

                //Physics Update 1: Bullets Processing
                if (this.sceneManager != null) {
                    this.sceneManager.updateMutableObjects(1);
                }

                long timeStamp = currentTimeMillis();

                Vector3D newMouseCursorPosition = this.mouse.getPositionShift();

                if (this.sceneManager != null
                        && this.sceneManager.getPlayer(this.playerId) != null) {
                    Vector3D rotation = this.sceneManager
                            .getPlayer(this.playerId).getRotation();

                    if (newMouseCursorPosition.getX() != 0) {
                        this.isRotating = true;
                        rotation = new Vector3D(rotation.getX(), rotation.getY() + newMouseCursorPosition.getX() + .3, rotation.getZ());
                        this.sceneManager.getPlayer(this.playerId).updateRotationY((float) rotation.getY());
                    }

                    if (this.isMoving) {
                        //Vector3D position = sceneManager.getPlayer(playerId).getPosition();
                        Vector3D newPosition = translatePolar(ZERO, this.velocity, (float) rotation.getY(), this.direction, 1);
                        AbstractBoundingVolume boundingVolume
                                = this.sceneManager.getPlayer(this.playerId)
                                        .getBoundingVolumeCopy(0);
                        boundingVolume.min = boundingVolume.min.add(newPosition);
                        boundingVolume.max = boundingVolume.max.add(newPosition);
                        GameObject temp = new GameObject(this.playerId);
                        temp.setBoundingVolume(0, boundingVolume);
                        //TODO: alter when implemented for a compound mesh.
                        temp.addChild(this.sceneManager.getPlayer(this.playerId).getMeshes().get(0));

                        String playerCrash = this.sceneManager.checkPlayerPlayerCollision(temp);

                        //Vector3D futurePosition = new Vector3D(position.toArray()).add(newPosition);
                        //sceneManager.getPlayer(playerId).getRotation();
                        if (!this.sceneManager.checkSceneBoundsCollision(boundingVolume)
                                && !this.sceneManager.checkPlayerImmutableObjectCollision(temp)
                                && (playerCrash == null || this.playerId.equals(playerCrash))) {
                            this.sceneManager.getPlayer(this.playerId).updatePosition(newPosition);
                        }
                    }

                    boolean isShooting = this.mouse.isContinuouslyDetecting("shoot")
                            && timeStamp > (this.lastTimeStamp + 500);
                    if (isShooting) {
                        //log.log(INFO, "Shooting!");

                        this.lastTimeStamp = timeStamp;

                        Vector3D v = this.sceneManager.getPlayer(this.playerId).getPosition();
                        Vector3D bulletPosition = new Vector3D(v.getX(), v.getY() + 1.0, v.getZ());

//                               rotation = sceneManager.getPlayer(playerId).getRotation();
//                               if (LeftShoot) {
//                                   log.log(INFO, "LeftShoot");
//                                   rotation = new Vector3D(rotation.getX(), rotation.getY() + 50, rotation.getZ());
//                                   LeftShoot = false;
//                               } else {
//                                   rotation = new Vector3D(rotation.getX(), rotation.getY() + 50, rotation.getZ());
//                                   LeftShoot = true;
//                               }
                        try {
                            //TransformGroup bulletRotate = (TransformGroup) models.get("bullet").clone();
                            TransformGroup bulletRotate = this.models.get("bullet").clone();
                            GameObject bullet = new GameObject(this.playerId + "Bullet" + this.bulletIdSequence++, this.models.get("bullet"));
                            //bulletRotate.setBoundingVolume(0, null);
                            bullet.getBoundingVolume(0).setRenderable(true);
                            bullet.addChild(bulletRotate);
                            bullet.setAttribute("health", 100);
                            bullet.setScaling(new Vector3D(0.5f, 0.5f, 0.5f));
                            //bullet.setPosition(bulletPosition);
                            //bullet.setRotation(new Vector3D(rotation.toArray()));
                            bullet.setVelocity(new Vector3D(0.8f, 2.5f, 0.8f));
                            bullet.updateRotationY((float) rotation.getY());
                            bullet.updatePosition(bulletPosition);
                            this.sceneManager.addMutableObject(bullet);
                        } catch (CloneNotSupportedException ex) {
                            log.log(SEVERE, null, ex);
                        }
                    }
                }
                this.sceneManager.updateCamera(1);
            }
        }
        //}
    }
}
