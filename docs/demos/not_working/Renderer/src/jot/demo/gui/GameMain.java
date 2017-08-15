package jot.demo.gui;

import jot.geometry.bounding.A_BoundingVolume;
import jot.geometry.I_GeometryNode;
import jot.geometry.TransformGroup;
import jot.geometry.generators.terrain.TerrainGenerators;
import jot.gui.Camera.CameraType;
import jot.io.image.ImageRenderer;
import static jot.io.image.ImageWriter.writeImage;
import jot.io.device.KeyBoardHandler;
import jot.io.device.MouseHandler;
import java.io.File;
import java.io.IOException;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.logging.Logger.getLogger;
import com.jogamp.opengl.GL2;
import jot.lighting.Light;
import jot.manager.SceneManager;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;
import static jot.physics.Kinematics.translate;
import static jot.util.FrameworkOptions.Floor;
import static jot.util.FrameworkOptions.SkyBox;
import static jot.util.FrameworkOptions.SkyDome;
import static jot.util.FrameworkOptions.useRayTracer;
import static jot.demo.util.GameConstants.setGameConstants;
import jot.util.GameObject;
import static jot.util.ExtensionGeometryOptions.useTerrainGenerators;

// 
// 
/**
 * Adaptation of smallpt, a Path Tracer originally written by Kevin Beason,
 * 2008. Ported to Java and refactored by Ronald Chen.
 *
 * @author G. Amador & A. Gomes
 */
public class GameMain extends A_Game {

    private static final Logger log = getLogger("GameMainRenderer");

    private static final int UPDATE_RATE = 25; //Number of Frames or Updates Per Second, i.e., FPS or UPS.
    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 600;

    private static final int RAYTRACER_CANVAS_WIDTH = 400;
    private static final int RAYTRACER_CANVAS_HEIGHT = 400;

    static {
        log.setLevel(OFF);
    }

    /**
     * The main method.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        setGameConstants();

        log.setLevel(INFO);

        A_Game game = null;
        while (game == null) {
            //if user ID provided and ...
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
    public String playerID;

    private final int samples = 4;
    private final CameraType cameraToUseType = CameraType.PERSPECTIVE;

    private final float size = 100;
    private final Vector3D velocity = new Vector3D(0.5f, 0.5f, 0.5f);   //the velocity for any moving avatar or bullet in the  sceneManager.
    private final ConcurrentHashMap<String, TransformGroup> models = new ConcurrentHashMap<String, TransformGroup>();

    /**
     * The last time an updated was performed.
     */
    protected long lastTimeStamp = currentTimeMillis();

    /**
     * Auxiliary time counters and update counter.
     */
    protected long beginTime, endTime, updateCount = 0;

    private float direction;
    private boolean isMoving, isRotating;

    private TerrainGenerators tg;

    private SceneManager sceneManager;

    private boolean rayTracing = true;

    /**
     * Constructor, receives a player unique id, mays set up (if a valid AI is
     * chosen) the AI to use, and configures a communication node with a
     * personalized configuration (if provided a valid path to a configuration
     * file).
     *
     * @param pID player unique ID.
     */
    public GameMain(String pID) {
        super("Renderer", CANVAS_WIDTH, CANVAS_HEIGHT);

        this.playerID = pID;     //the players ID, i.e., user name

        //activate the input module
        log.info("Setting up input controllers (keyboard and mouse).");
        mouseHandler = new MouseHandler();
        //getLogger(MouseHandler.class.getName()).setLevel(ALL);
        keyBoardHandler = new KeyBoardHandler();
        //getLogger(KeyBoardHandler.class.getName()).setLevel(ALL);

        getLogger(ImageRenderer.class.getName()).setLevel(ALL);

        log.info("Done!");
    }

    @Override
    public void gameInit() {
        log.info("Setting up sceneManager...");
        log.info("Load model textures.");
        ConcurrentHashMap<String, String> texturesPaths = new ConcurrentHashMap<String, String>();
        texturesPaths.put("cow", "/models/dae/cow/Cow.png");
        texturesPaths.put("duck", "/models/dae/duck/duckCM.jpg");
        texturesPaths.put("tie", "/models/dae/tie/tieskin.jpg");
        texturesPaths.put("xwing", "/models/dae/xwing/xwingskin.jpg");
        texturesPaths.put("brick1", "/textures/materials/brick1.jpg");
        texturesPaths.put("floor1", "/textures/materials/floor1.png");
        texturesPaths.put("floor2", "/textures/materials/floor2.png");

        ThreadLocal<Random> random = new ThreadLocal<Random>() {
            @Override
            protected Random initialValue() {
                return new Random(1337);
            }
        };
        sceneManager = new SceneManager(texturesPaths, RAYTRACER_CANVAS_WIDTH, RAYTRACER_CANVAS_HEIGHT, random);
        sceneManager.setSceneSize(size);

        //Setup cameras
        //NOTE: camera IDs must differ and may not be necessarly the playerID, e.g., camera1, camera2, camera3.
        if ("true".equals(getProperty("JOT.scenerotate"))) {
            sceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, playerID, Camera.CameraType.THIRD_PERSON));
            sceneManager.setCameraToUse(playerID);
        } else {
            sceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "FIRST_PERSON", CameraType.FIRST_PERSON));
            sceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "THIRD_PERSON", CameraType.THIRD_PERSON));
            sceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "PERSPECTIVE", CameraType.PERSPECTIVE));
            sceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "PERSPECTIVE_FOLLOW", CameraType.PERSPECTIVE_FOLLOW));
            sceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "PERSPECTIVE_RAYTRACER", CameraType.PERSPECTIVE_RAYTRACER)); //Raytracer viewPort camera
            sceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "UPPER_VIEW", CameraType.UPPER_VIEW));
            sceneManager.addCamera(new Camera(60, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, "UPPER_VIEW_FOLLOW", CameraType.UPPER_VIEW_FOLLOW));
            sceneManager.addCamera(new Camera(
                    28.8f, (float) CANVAS_WIDTH / CANVAS_HEIGHT, size, cameraToUseType,
                    //new Vector3D(size * .22f, size * 0.08f, size * .22f), //Position
                    //new Vector3D(0.0, -0.01, -0.01));                     //LookAT
                    new Vector3D(0, 11.2, 214), new Vector3D(0, -0.042612, -1))); //Raytracer camera

            sceneManager.setCameraToUse(cameraToUseType.toString());
        }
        log.info("Cameras setup done.");

        //Setup lights
        sceneManager.addLight(new Light("light0", GL2.GL_LIGHT0, null));
        sceneManager.setLightToUse("light0");
        log.info("Lights setup done.");

        //Setup floor
        if (Floor) {
            TransformGroup floor;
            if (useTerrainGenerators) {
                //floor = buildTerrain(size, null, 5, 50, 3, "diamondSquare");
                //floor = buildTerrain(size, "floor1", 5, 50, 3, "diamondSquare");
                //floor = buildTerrain(size, "floor2", 5, 50, 3, "diamondSquare");
                //floor = buildSmoothTerrain(size, null, 5, 50, 3, "diamondSquare", "nurbs");
                floor = buildSmoothTerrain(size, "floor1", 5, 50, 3, "diamondSquare", "nurbs");
                //floor = buildSmoothTerrain(size, "floor2", 5, 50, 3, "diamondSquare", "nurbs");
                Iterator<I_GeometryNode> childIterator = floor.childIterator();
                tg = ((TerrainGenerators) childIterator.next());
                floor.updateTranslation(new Vector3D(-size / 2, 0, -size / 2));
                //floor.updateTranslation(new Vector3D(-size / 2, size / 2 - 2, -size / 2));
            } else {
                floor = buildFloor(size, "/textures/skybox/World_01/SkyBox2_down.jpg");
                //floor.setRotation(new Vector3D(-90, 0, 0));
                floor.updateTranslation(new Vector3D(0, size / 2, 0));
            }
            sceneManager.setFloor(floor);
            log.info("Floor created.");
        }

        //Setup skyBox
        if (SkyBox) {
            TransformGroup skyBox;
            skyBox = buildSkyBox(size / 5, "/textures/skybox/World_01/SkyBox2", "jpg");

            skyBox.setScaling(new Vector3D(5, 5, 5));
            //skyBox.setRotation(new Vector3D(- 90, 0, 0));
            skyBox.setBoundingVolume(0, null);
            sceneManager.setSky(skyBox);
            log.info("Sky Box added.");
        }

        //Setup skyDome
        if (SkyDome) {
            TransformGroup skyDome;
            skyDome = buildSkyDome(50, "/textures/skydome/sky_povray1.jpg");
            //skyDome.setScaling(new Vector3D(5, 5, 5));
            //skyDome.setRotation(new Vector3D(- 90, 0, 0));
            skyDome.setBoundingVolume(0, null);
            sceneManager.setSky(skyDome);
            log.info("Sky Dome added.");
        }

        //Setup obstacles
        if (!useTerrainGenerators) {
            int boxCount = 6;
            for (int i = 0; i < boxCount; i++) {
                float rotation = i * (360 / boxCount);
                //SPHERE or OBB or AABB            
                GameObject obstacle = buildBlock("obstacle" + (i + 1), "brick1", 1.0f, BoundingVolume.BoundingVolumeType.OBB);
                Vector3D obstaclePostion = translate(ZERO, 15.0f, rotation, 0, 1);
                obstaclePostion = new Vector3D(obstaclePostion.getX(), obstaclePostion.getY() + 1, obstaclePostion.getZ());
                obstacle.setTranslation(obstaclePostion);
                obstacle.getBoundingVolume(0).setRenderBoundingVolume(true);
                sceneManager.addImmutableObject(obstacle);
                log.log(INFO, format("Obstacle %d added", i + 1));
            }
        }

        //Setup models   
        //models.put("tie", sceneManager.loadFormat("/models/dae/tie/tiefighter.dae", "tie", 70, BoundingVolume.BoundingVolumeType.AABB));
        //models.put("tie", sceneManager.loadFormat("/models/dae/tie/tiefighter_out.dae", "tie", 70, BoundingVolume.BoundingVolumeType.OBB));        
        models.put("tie", sceneManager.loadFormat("/models/dae/tie/tiefighter.dae", "tie", 70, BoundingVolume.BoundingVolumeType.OBB));

        //models.put("xwing", sceneManager.loadFormat("/models/dae/xwing/xwing_out.dae", "xwing", 3300, BoundingVolume.BoundingVolumeType.SPHERE));
        models.put("xwing", sceneManager.loadFormat("/models/dae/xwing/xwing.dae", "xwing", 3300, BoundingVolume.BoundingVolumeType.SPHERE));
        models.get("xwing").setRotationY(180.0f);

        //models.put("cow", sceneManager.loadFormat("/models/dae/cow/cow_out.dae", "cow", 1, BoundingVolume.BoundingVolumeType.OBB));
        models.put("cow", sceneManager.loadFormat("/models/dae/cow/cow.dae", "cow", 1, BoundingVolume.BoundingVolumeType.OBB));
        models.get("cow").setRotationZ(90.0f);
        models.get("cow").setRotationX(-90.0f);

        models.put("duck", sceneManager.loadFormat("/models/dae/duck/duck_triangulate.dae", "duck", 250, BoundingVolume.BoundingVolumeType.OBB));
        models.get("duck").setRotationY(-90.0f);

        models.put("duke", sceneManager.loadFormat("/models/dae/Duke_posed.dae", "duke", 1, BoundingVolume.BoundingVolumeType.OBB));
        models.get("duke").setRotationY(180);

        try {
            TransformGroup playerRotate;
            GameObject player;
            //playerRotate = models.get("duke").clone();
            //player = new GameObject(playerID, models.get("duke"));
            playerRotate = models.get("tie").clone();
            player = new GameObject(playerID, models.get("tie"));
            player.getBoundingVolume(0).setRenderBoundingVolume(true);
            //playerRotate.setBoundingVolume(0, null);
            player.addChild(playerRotate);
            player.setHealth(100);
            player.setVelocity(velocity);
            sceneManager.addPlayer(player);
            sceneManager.setLocalPlayerID(playerID);
            sceneManager.getPlayer(playerID).updateTranslation(new Vector3D(2, 1, 2));
            log.log(INFO, format("%s added to the sceneManager.", playerID));

            TransformGroup player1Rotate;
            GameObject player1;
            //player1Rotate = models.get("duke").clone();
            //player1 = new GameObject(playerID, models.get("duke"));            
            player1Rotate = models.get("tie").clone();
            player1 = new GameObject("player1", models.get("tie"));
            player1.getBoundingVolume(0).setRenderBoundingVolume(true);
            //player1Rotate.setBoundingVolume(0, null);
            player1.addChild(player1Rotate);
            player1.setHealth(100);
            player1.setVelocity(velocity);
            sceneManager.addPlayer(player1);
            sceneManager.getPlayer("player1").updateTranslation(new Vector3D(10, 1, 10));
            log.info("player1 added to the sceneManager.");

            if (!"true".equals(getProperty("JOT.scenerotate"))) {
                TransformGroup player2Rotate = models.get("duck").clone();
                GameObject player2 = new GameObject("player2", models.get("duck"));
                player2.getBoundingVolume(0).setRenderBoundingVolume(true);
                //player2Rotate.setBoundingVolume(0, null);
                player2.addChild(player2Rotate);
                player2.setHealth(100);
                player2.setVelocity(velocity);
                sceneManager.addPlayer(player2);
                //sceneManager.setLocalPlayerID("player2");
                //sceneManager.getPlayer("player2").updateTranslation(new Vector3D(2, 0, 2));
                sceneManager.getPlayer("player2").updateTranslation(new Vector3D(25, 0, 12));
                log.info("player2 added to the sceneManager.");

                TransformGroup player3Rotate = models.get("cow").clone();
                GameObject player3 = new GameObject("player3", models.get("cow"));
                player3.getBoundingVolume(0).setRenderBoundingVolume(true);
                //player3Rotate.setBoundingVolume(0, null);
                player3.addChild(player3Rotate);
                player3.setHealth(100);
                player3.setVelocity(velocity);
                sceneManager.addPlayer(player3);
                //sceneManager.setLocalPlayerID("player3");
                //sceneManager.getPlayer("player3").updateTranslation(new Vector3D(-2, 0, -2));
                sceneManager.getPlayer("player3").updateTranslation(new Vector3D(30, 0, -2));
                sceneManager.getPlayer("player3").updateRotationY(45);
                log.info("player3 added to the sceneManager.");
            }
        } catch (CloneNotSupportedException ex) {
            log.log(SEVERE, null, ex);
        }
        sceneManager.setupGeometriesToRender(); //TODO: replace with add when object added/removed from the scene.

        if (!"true".equals(getProperty("JOT.scenerotate"))) {
            log.info("");
            log.info("Options");
            log.info("1 - toggle on/off lights.");
            log.info("2 - toggle on/off textures.");
            log.info("3 - toggle on/off wireframe.");
            log.info("4 - toggle on/off planar shadows.");
            log.info("5 - toggle on/off shadow maps.");
            log.info("6 - toggle on/off zBuffer.");
            log.info("7 - to raytrace.");
            log.info("8 - toggle on/off raytracer.");
            log.info("9 - to render the last raytraced scene to a file.");
            log.info("T/t - generate new terrain");
            log.info("w,a,s,d - to move player.");
            log.info("u,h,j,k - to move light.");
            log.info("y,i - torotate the light around the center of the scene.");
            log.info("-/+ - to translate the light down/up.");
            log.info("Q/q quit.");
        }
    }

    @Override
    public void gameProcessInput() {
        //log.info("Process Input.");
        synchronized (this) {
            if (isDetecting("Q") || isDetecting("q")) {
                gameShutdown(gl);
            }

            //General options
            if (isDetecting("1")) {
                sceneManager.setLights();
            }
            if (isDetecting("2")) {
                sceneManager.setShowTextures();
            }
            if (isDetecting("3")) {
                sceneManager.setShowWireframe();
            }
            if (isDetecting("4")) {
                sceneManager.setShowPlanarShadows();
            }
            if (isDetecting("5")) {
                sceneManager.setShowShadowMaps();
            }
            if (isDetecting("6")) {
                sceneManager.setShowZBuffer();
            }
            if (isDetecting("7")) {
                rayTracing = true;
            }
            if (isDetecting("8")) {
                useRayTracer = !useRayTracer;
            }
            if (sceneManager != null && (isDetecting("9"))) {
                log.info("Writting Image!");
                beginTime = currentTimeMillis();
                File f = new File("image.ppm");
                try {
                    writeImage(sceneManager, f);
                } catch (IOException ex) {
                    loglog(SEVERE, null, ex);
                }
                endTime = currentTimeMillis();
                log.info(format("Finished in %dms", endTime - beginTime));
            }

            //Light moving
            if (isContinuouslyDetecting("Y") || isContinuouslyDetecting("y")) {
                sceneManager.getLight("light0").updateLightRotation(gl, -1);
            }
            if (isContinuouslyDetecting("I") || isContinuouslyDetecting("i")) {
                sceneManager.getLight("light0").updateLightRotation(gl, 1);
            }
            if (isContinuouslyDetecting("+")) {
                sceneManager.getLight("light0").updateLightTranslation(gl, 0, 1, 0);
            }
            if (isContinuouslyDetecting("-")) {
                sceneManager.getLight("light0").updateLightTranslation(gl, 0, -1, 0);
            }
            if (isContinuouslyDetecting("U") || isContinuouslyDetecting("u")) {
                sceneManager.getLight("light0").updateLightTranslation(gl, 0, 0, -1);
            }
            if (isContinuouslyDetecting("J") || isContinuouslyDetecting("j")) {
                sceneManager.getLight("light0").updateLightTranslation(gl, 0, 0, 1);
            }
            if (isContinuouslyDetecting("H") || isContinuouslyDetecting("h")) {
                sceneManager.getLight("light0").updateLightTranslation(gl, -1, 0, 0);
            }
            if (isContinuouslyDetecting("K") || isContinuouslyDetecting("k")) {
                sceneManager.getLight("light0").updateLightTranslation(gl, 1, 0, 0);
            }
            if ((isDetecting("T") || isDetecting("t")) && useTerrainGenerators) {
                TransformGroup floor;
                //floor = buildTerrain(size, null, 5, 50, 3, "diamondSquare");
                floor = buildTerrain(size, "floor1", 5, 50, 3, "diamondSquare");
                //floor = buildTerrain(size, "floor2", 5, 50, 3, "diamondSquare");
                //floor = buildSmoothTerrain(size, null, 5, 50, 3, "diamondSquare", "nurbs");
                //floor = buildSmoothTerrain(size, "floor1", 5, 50, 3, "diamondSquare", "nurbs");
                //floor = buildSmoothTerrain(size, "floor2", 5, 50, 3, "diamondSquare", "nurbs");
                Iterator<I_GeometryNode> childIterator = floor.childIterator();
                tg = ((TerrainGenerators) childIterator.next());
                floor.updateTranslation(new Vector3D(-size / 2, 0, -size / 2));
                //floor.updateTranslation(new Vector3D(-size / 2, size / 2 - 2, -size / 2));
                sceneManager.setFloor(floor);
            }

            boolean wDown = isContinuouslyDetecting("W") || isContinuouslyDetecting("w");
            boolean sDown = isContinuouslyDetecting("S") || isContinuouslyDetecting("s");
            boolean aDown = isContinuouslyDetecting("A") || isContinuouslyDetecting("a");
            boolean dDown = isContinuouslyDetecting("D") || isContinuouslyDetecting("d");
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
    }

    @Override
    public void gameRender(GL2 gl) {
        synchronized (this) {
            //log.warning("Render.");
            switch (state) {
                case LOADING:
                    //log.warning("Loading ...");
                    //TODO: (SceneManagement Extras) loading menu.
                    break;
                case INITIALIZED:
                    log.warning("Loaded ...");
                    state = A_Game.State.PLAYING;
                    break;
                case PLAYING:
                    //TODO: make sceneRender equal RayTrace and rasterized scenes.
                    if (!rayTracing && useRayTracer) {
                        sceneManager.setCameraToUse("PERSPECTIVE_RAYTRACER");
                    } else {
                        sceneManager.setCameraToUse(cameraToUseType.toString());
                    }
                    sceneManager.render(gl);
                    break;
                case PAUSED:
                    //......
                    break;
                case GAME_OVER:
                    //......
                    break;
            }
        }
    }

    @Override
    public void gameShutdown(GL2 gl) {
        log.info("Closing...");
        super.gameShutdown(gl);
    }

    @Override
    public void gameUpdate() {
        synchronized (this) {
            if ("true".equals(getProperty("JOT.scenerotate"))) {
                if (sceneManager != null) {
                    Vector3D cameraRotation = sceneManager.getCamera(playerID).getCameraRotation();
                    cameraRotation = new Vector3D(cameraRotation.getX(), cameraRotation.getY() + .3, cameraRotation.getZ());
                    sceneManager.getCamera(playerID).setCameraRotation(cameraRotation);
                    sceneManager.updateCamera();
                }
            } else {
                if (state == State.GAME_OVER) {
                    return;
                }

                if (state == State.PLAYING) {
                    //log.info("Update.");
                    if (updateCount == UPDATE_RATE) {
                        updateCount = 0;
                    }
                    updateCount++;

                    if (sceneManager != null) {
                        if (rayTracing && useRayTracer) {
                            sceneManager.RayTrace(RAYTRACER_CANVAS_WIDTH, RAYTRACER_CANVAS_HEIGHT, samples);
                            rayTracing = false;
                        } else {
                            Vector3D newMouseCursorPosition = mouseHandler.getPositionShift();

                            if (sceneManager.getPlayer(playerID) != null) {
                                Vector3D rotation = sceneManager.getPlayer(playerID).getRotation();

                                if (newMouseCursorPosition.getX() != 0) {
                                    //isRotating = true;
                                    rotation = new Vector3D(rotation.getX(), rotation.getY() + newMouseCursorPosition.getX() + .3, rotation.getZ());
                                    sceneManager.getPlayer(playerID).updateRotationY((float) rotation.getY());
                                }

                                //Vector3D position = sceneManager.getPlayer(playerID).getPresentTranslation();
                                Vector3D newPosition = translate(ZERO, velocity, (float) rotation.getY(), direction, 1);

                                BoundingVolume boundingVolume = new BoundingVolume(sceneManager.getPlayer(playerID).getBoundingVolume(0));
                                boundingVolume.min = boundingVolume.min.add(newPosition);
                                boundingVolume.max = boundingVolume.max.add(newPosition);
                                GameObject temp = new GameObject(playerID);
                                temp.setBoundingVolume(0, boundingVolume);
                                temp.addChild(sceneManager.getPlayer(playerID).getMesh());

                                String playerCrash = sceneManager.checkPlayerPlayerCollision(temp);

                                //Vector3D futurePosition = new Vector3D(position.toArray()).add(newPosition);
                                //sceneManager.getPlayer(playerID).getRotation();
                                if (isMoving && !sceneManager.checkSceneBoundsCollision(boundingVolume)
                                        && !sceneManager.checkPlayerImmutableObjectCollision(temp)
                                        && (playerCrash == null || playerID.equals(playerCrash))) {
                                    sceneManager.getPlayer(playerID).updateTranslation(newPosition);
                                }

//                                //TODO: fix to proper height
//                                Vector3D position = sceneManager.getPlayer(playerID).getPresentTranslation();
//                                float y = tg.getAltitude(
//                                        (int) ((position.getX() + size / 2) / size * tg.getGeometry().size()),
//                                        (int) ((position.getZ() + size / 2) / size * tg.getGeometry().get(0).size()));
//                                if (position.getY() != y) {
//                                    sceneManager.getPlayer(playerID).setTranslation(new Vector3D(position.getX(), y, position.getZ()));
//                                }
                            }
                        }
                        sceneManager.updateCamera();
                    }
                }
            }
        }
    }
}
