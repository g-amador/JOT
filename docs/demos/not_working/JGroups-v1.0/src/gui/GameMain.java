package gui;

import ai.A_AI;
import ai.PathFinders;
import ai.SteeringBehaviours;
import communication.GameUpdate;
import communication.PlayerNode;
import geometry.BoundingVolume;
import geometry.I_GeometryNode;
import geometry.TransformGroup;
import geometry.generators.MazeGenerators;
import geometry.generators.TerrainGenerators;
import input.data.formats.HOG2.HOG2Map.HOG2MapMesh;
import input.devices.KeyBoardHandler;
import input.devices.MouseHandler;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL2;
import javax.vecmath.Point3f;
import managers.SceneManager;
import physics.A_Kinematics;
import static util.A_GameConstants.HOG2Maps;
import static util.A_GameConstants.setGameConstants;
import static util.A_GameConstants.useMazeGenerators;
import static util.A_GameConstants.usePathFinders;
import static util.A_GameConstants.useSteeringBehaviours;
import static util.A_GameConstants.useTerrainGenerators;

/**
 * Class that performs the main game loop.
 *
 * @author G. Amador & A. Gomes
 */
public final class GameMain extends A_Game {

    public String playerId;                 //the id for the player running on this node.
    private final String gridConfig;
    private final static String gameName = "JOTgame";    //the name for the game window/frame.
    public SceneManager sceneManager;
    private static PlayerNode communication;
    private static TerrainGenerators tg;
    private static MazeGenerators mg;
    private final int mazeWidth = 25;
    private final int mazeLength = 25;
    private static A_AI ai;
    private boolean attacking;
    private boolean interacting;
    //private boolean LeftShoot;
    private int bulletIdSequence = 1;
    private final Point3f velocity = new Point3f(0.5f, 0.5f, 0.5f);   //the velocity for any moving avatar or bullet in the sceneManager.
    private final ConcurrentHashMap<String, TransformGroup> models = new ConcurrentHashMap<String, TransformGroup>();
    public float size = SceneManager.getSceneSize();       //absolute size value of one of the sides of the square that defines the floor, i.e., floor as size*size area.    
    private final static int CANVAS_WIDTH = 800;      // width of the game screen
    private final static int CANVAS_HEIGHT = 600;     // heigth of the game screen    
    private static final int UPDATE_RATE = 50;     // number of Frames or Updates Per Second, i.e., FPS or UPS.
    private static final int COMMUNICATION_UPDATE_RATE = 25;    // max number of game updates sent per second.
    private long lastTimeStamp = System.currentTimeMillis();
    private long updateCount = 0;
    //private long communicationUpdateCount = 0;
    private float lowestFPS = Float.POSITIVE_INFINITY;
    private float playersCount = 1;
    private float perceivedPlayersCount = 1;

    /**
     * Constructor, receives a player unique id, mays set up (if a valid AI is
     * chosen) the AI to use, and configures a communication node with a
     * personalized configuration (if provided a valid path to a configuration
     * file).
     *
     * @param playerId player unique id.
     * @param aiLogic the AI logic to use.
     * @param gridConfig a valid path to a configuration file.
     */
    public GameMain(String playerId, String aiLogic, String gridConfig) {
        super(gameName, CANVAS_WIDTH, CANVAS_HEIGHT);

        this.playerId = playerId;     //the players ID, i.e., user name
        this.gridConfig = gridConfig;
        //gameName = "JOTProject";
        //LeftShoot = true;   //the first shoot that this players does should be from its left side

        //activate the input module
        System.out.println("Setting up input controllers (keyboard and mouse).");
        mouseHandler = new MouseHandler();
        keyBoardHandler = new KeyBoardHandler();

        //activate the communication module
        System.out.println("Setting up communication.");
        communication = new PlayerNode(this);

        //activate the AI module for steering behaviours
        if (useSteeringBehaviours || usePathFinders) {
            System.out.println("Setting up AI.");
            if (useSteeringBehaviours) {
                ai = new SteeringBehaviours(size);
            }
            if (usePathFinders) {
                ai = new PathFinders(size);
            }

            //set AI logic to use
            ai.setAILogic(aiLogic);
        }
    }

    @Override
    public void gameInit() {
        System.out.println("Setting up sceneManager...");
        System.out.println("Load models and models textures.");
        ConcurrentHashMap<String, String> texturesPaths = new ConcurrentHashMap<String, String>();
        texturesPaths.put("tie", "/models/dae/tie/tieskin.jpg");
        texturesPaths.put("xwing", "/models/dae/xwing/xwingskin.jpg");
        texturesPaths.put("seymour", "/models/dae/seymour/seymour.png");
        sceneManager = new SceneManager(texturesPaths);

        //Setup cameras
        //NOTE: camera IDs must differ and may not be necessarly the playerId, e.g., camera1, camera2, camera3.
        if ("true".equals(System.getProperty("JOT.scenerotate"))) {
            sceneManager.addCamera(new Camera(size, "THIRD_PERSON", Camera.CameraType.THIRD_PERSON));
        } else {
            sceneManager.addCamera(new Camera(size, "FIRST_PERSON", Camera.CameraType.FIRST_PERSON));
            sceneManager.addCamera(new Camera(size, "THIRD_PERSON", Camera.CameraType.THIRD_PERSON));
            sceneManager.addCamera(new Camera(size, "PERSPECTIVE", Camera.CameraType.PERSPECTIVE));
            sceneManager.addCamera(new Camera(size, "PERSPECTIVE", Camera.CameraType.PERSPECTIVE_FOLLOW));
            sceneManager.addCamera(new Camera(size, "UPPER_VIEW", Camera.CameraType.UPPER_VIEW));
            sceneManager.addCamera(new Camera(size, "UPPER_VIEW", Camera.CameraType.UPPER_VIEW_FOLLOW));
        }
        //sceneManager.setCameraToUse("UPPER_VIEW");
        sceneManager.setCameraToUse("THIRD_PERSON");

        //Setup floor
        TransformGroup floor;
        if (!useTerrainGenerators && !useMazeGenerators) {
            floor = SceneManager.buildFloor(size, "/textures/World_05/SkyBox2_down.jpg");
            //floor = SceneManager.buildFloor(size, "/textures/tiles/tiles9.png");
            //floor = SceneManager.buildFloor(size, "/textures/tiles/tiles9.png");
            //floor = SceneManager.buildFloor(size, "/textures/tiles/tiles16.png");                

            //floor = SceneManager.buildFloor(size, "/textures/tiles/a3.png");
            //floor = SceneManager.buildFloor(size, "/textures/tiles/aura-nimbus.png");
            //floor = SceneManager.buildFloor(size, "/textures/tiles/quadtree.png");
            //floor = SceneManager.buildFloor(size, "/textures/tiles/Voronoi.png");
            //floor = SceneManager.buildFloor(size, "/textures/tiles/delaunay.png");
            //floor = SceneManager.buildFloor(size, "/textures/tiles/delaunay_neighbours_improved.png");        
            //floor = SceneManager.buildFloor(size, "/textures/tiles/hexagons.png");
            //floor = SceneManager.buildFloor(size, "/textures/tiles/hexagons_neighbours_aura_nimbus.png");
            //floor = SceneManager.buildFloor(size, "/textures/tiles/tiles25.png");
            //floor = SceneManager.buildFloor(size, "/textures/tiles/tiles25_neighbours_aura_nimbus.png");               
            ////floor.updateTranslation(new Point3f(0, size / 2, 0));  
            //floor.updateTranslation(new Point3f(0, size / 2 - 2, 0));
        } else if (useTerrainGenerators) {
            floor = SceneManager.buildTerrain(size, null, 5, 50, 3);
            Iterator<I_GeometryNode> childIterator = floor.childIterator();
            tg = ((TerrainGenerators) childIterator.next());
            floor.updateTranslation(new Point3f(-size / 2, 0, -size / 2));
            //floor.updateTranslation(new Point3f(-size / 2, size / 2 - 2, -size / 2));
        } else {
            floor = SceneManager.buildMaze(size - size / 10, mazeWidth, mazeLength);
            Iterator<I_GeometryNode> childIterator = floor.childIterator();
            mg = ((MazeGenerators) childIterator.next());
            floor.updateTranslation(new Point3f(0, -2, 0));
            //floor.updateTranslation(new Point3f(0, size / 2, 0));
            floor.updateTranslation(new Point3f(0, size / 2 - 2, 0));
        }
        sceneManager.setFloor(floor);
        System.out.println("Floor created.");

        //Setup skyBox
        TransformGroup skyBox;
        skyBox = SceneManager.buildSkyBox(size / 5, "/textures/World_05/SkyBox2", "jpg");
        skyBox.setScaling(new Point3f(5, 5, 5));
        //skyBox.setRotation(new Point3f(- 90, 0, 0));
        skyBox.setBoundingVolume(null);
        sceneManager.setSkyBox(skyBox);
        System.out.println("Sky Box added.");

        //Setup models
        //models.put("tie", sceneManager.loadFormat("/models/dae/tie/tiefighter.dae", "tie", 70, BoundingVolume.BoundingVolumeType.AABB));
        //models.put("tie", sceneManager.loadFormat("/models/dae/tie/tiefighter_out.dae", "tie", 70, BoundingVolume.BoundingVolumeType.OBB));        
        models.put("tie", sceneManager.loadFormat("/models/dae/tie/tiefighter.dae", "tie", 70, BoundingVolume.BoundingVolumeType.OBB));
        //models.put("xwing", sceneManager.loadFormat("/models/dae/xwing/xwing_out.dae", "xwing", 3300, BoundingVolume.BoundingVolumeType.SPHERE));
        models.put("xwing", sceneManager.loadFormat("/models/dae/xwing/xwing.dae", "xwing", 3300, BoundingVolume.BoundingVolumeType.SPHERE));
        models.get("xwing").setRotationY(180.0f);
        //models.put("bullet", sceneManager.loadFormat("/models/dae/CoffeeBean2_out.dae", "bullet", 1, BoundingVolume.BoundingVolumeType.AABB));
        //models.put("seymour", sceneManager.loadFormat("/models/dae/seymour/Seymour_Walking_Maya.dae", "seymour", BoundingVolume.BoundingVolumeType.OBB));        
        models.put("bullet", sceneManager.loadFormat("/models/dae/CoffeeBean2_out.dae", "bullet", 1, BoundingVolume.BoundingVolumeType.OBB));

        try {
            TransformGroup playerRotate = models.get("tie").clone();
            //TransformGroup playerRotate = models.get("seymour").clone();
            TransformGroup player = new TransformGroup(playerId, models.get("tie"));
            //TransformGroup player = new TransformGroup(playerId, models.get("seymour"));
            player.getBoundingVolume().setRenderBoundingVolume(true);
            playerRotate.setBoundingVolume(null);
            player.addChild(playerRotate);
            player.setHealth(100);
            player.setVelocity(velocity);
            sceneManager.addPlayer(player);
            sceneManager.setLocalPlayerId(playerId);

            //TODO: initial random pos in less occupied space
            //Avoid placement in occupiwwed by either obstacle or player
            Boolean validPosition = false;
            //Boolean tryOne = true;
            //long count=0;

            if (!A_AI.doAI) {
                do {
                    int randPosX;
                    int randPosZ;

                    if ("true".equals(System.getProperty("JOT.scenerotate"))) {
                        randPosX = 0;
                        randPosZ = 0;
                    } else {
                        randPosX = 45;
                        randPosZ = 45;

                        //randPosX = Math.round((float) (Math.random() * (size - 6)) - ((size - 6) / 2));
                        //randPosZ = Math.round((float) (Math.random() * (size - 6)) - ((size - 6) / 2));
                        if (playerId.toLowerCase().contains("pires")) {
                            randPosX = 0;
                            randPosZ = -45;
                        }

                        if (playerId.toLowerCase().contains("esteves")) {
                            randPosX = 0;
                            randPosZ = -20;
                            sceneManager.getPlayer(playerId).updateRotationY(180.0f);
                        }

                        //if(tryOne == true) {
                        //   randPosX = 12;
                        //   randPosZ = 8;
                        //   tryOne = false;
                        //}
                    }

                    //System.out.println(randPosX+" "+randPosZ);
                    Point3f position = new Point3f(randPosX, 0, randPosZ);

                    BoundingVolume temp_bv = new BoundingVolume(sceneManager.getPlayer(playerId).getBoundingVolume());
                    temp_bv.min.add(position);
                    temp_bv.max.add(position);
                    TransformGroup temp = new TransformGroup(playerId);
                    temp.setBoundingVolume(temp_bv);
                    temp.addChild(sceneManager.getPlayer(playerId).getMesh());

                    String playerCrash = sceneManager.checkPlayerPlayerCollision(temp);
                    sceneManager.getPlayer(playerId).getRotation();

                    //count++;
                    //System.out.println("Try number "+count+".");
                    if (!sceneManager.checkPlayerImmutableObjectCollision(temp) && (playerCrash == null || playerId.equals(playerCrash))) {
                        sceneManager.getPlayer(playerId).updateTranslation(position);
                        validPosition = true;
                        //System.out.println("Valid initial position.");
                    }
                } while (!validPosition);
            }
            System.out.println(playerId + " added to the scene.");
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(GameMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (A_AI.doAI) {
            //generate maze
            //ai.setGraph(mg.getMaze()); //TODO: (MazeGeneration) uncomment only when implemented maze generation.

            //set playerId to manipulate with AI
            sceneManager.getPlayer(playerId).updateTranslation(new Point3f(-44, 0, 16));  //TODO: (MazeGeneration) comment only when implemented maze generation.
            //sceneManager.getPlayer(playerId).updateTranslation(mg.getMazeMin()); //TODO: (MazeGeneration) uncomment only when implemented maze generation.           
            ai.setBot(sceneManager.getPlayer(playerId));

            TransformGroup target = new TransformGroup("target");
            target.setTranslation(new Point3f(25, 0, -25)); //TODO: (MazeGeneration) comment only when implemented maze generation.
            //target.setTranslation(mg.getMazeMax());       //TODO: (MazeGeneration) uncomment only when implemented maze generation.
            target.setRotation(new Point3f()); //zero vector (0, 0, 0)
            target.setVelocity(velocity);
            target.setBoundingVolume(new BoundingVolume(null));
            sceneManager.addPlayer(target);
            ai.setPrimaryTarget(target);

            //find best path
            //ai.setPath(ai.findBestPath());
        }

        if (!"true".equals(System.getProperty("JOT.scenerotate"))) {
            if ("true".equals(System.getProperty("JOT.testMode"))) {
                if (!A_AI.doAI) {
                    try {
                        TransformGroup player2Rotate = models.get("tie").clone();
                        TransformGroup player2 = new TransformGroup("player2", models.get("tie"));
                        //player2.getBoundingVolume().setRenderBoundingVolume(true);
                        player2Rotate.setBoundingVolume(null);
                        player2.addChild(player2Rotate);
                        player2.setHealth(100);
                        player2.setVelocity(velocity);
                        sceneManager.addPlayer(player2);
                        //sceneManager.getPlayer("player2").updateTranslation(new Point3f(2, 0, 2));
                        sceneManager.getPlayer("player2").updateTranslation(new Point3f(25, 0, 12));
                        System.out.println("player2 added to the scene.");

                        TransformGroup player3Rotate = models.get("xwing").clone();
                        TransformGroup player3 = new TransformGroup("player3", models.get("xwing"));
                        //player3.getBoundingVolume().setRenderBoundingVolume(true);
                        player3Rotate.setBoundingVolume(null);
                        player3.addChild(player3Rotate);
                        player3.setHealth(100);
                        player3.setVelocity(velocity);
                        sceneManager.addPlayer(player3);
                        //sceneManager.getPlayer("player3").updateTranslation(new Point3f(-2, 0, -2));
                        sceneManager.getPlayer("player3").updateTranslation(new Point3f(30, 0, -2));
                        sceneManager.getPlayer("player3").updateRotationY(45);
                        System.out.println("player3 added to the scene.");
                    } catch (CloneNotSupportedException ex) {
                        Logger.getLogger(GameMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        System.out.println("Options");
        System.out.println("1 - toggle on/off textures.");
        System.out.println("2 - toggle on/off wireframe.");
        System.out.println("3 - use FIRST_PERSON camera.");
        System.out.println("4 - use THIRD_PERSON camera.");
        System.out.println("5 - use PERSPECTIVE camera.");
        System.out.println("6 - use PERSPECTIVE_FOLLOW camera.");
        System.out.println("7 - use UPPER_VIEW camera.");
        System.out.println("8 - use UPPER_VIEW_FOLLOW camera.");
    }

    @Override
    public void gameRender(GL2 gl) {
        synchronized (GameMain.this) {
            //System.err.println("Render.");
            switch (state) {
                case LOADING:
                    //System.out.println("Loading ...");
                    //TODO: (Extras) loading menu.
                    break;
                case INITIALIZED:
                    //System.out.println("Loaded ...");
                    state = State.PLAYING;
                    break;
                case PLAYING:
                    sceneManager.render(gl);

                    if (useSteeringBehaviours) {
                        ((SteeringBehaviours) ai).renderLogic(gl);
                    }
                    if (usePathFinders) {
                        ((PathFinders) ai).renderGraph(gl);
                        ((PathFinders) ai).renderVisited(gl);
                    }
                    if (PlayerNode.usePlayerNode && fps.getFPS() > 0 && lowestFPS > fps.getFPS()) {
                        lowestFPS = fps.getFPS();
                        playersCount = communication.getPlayerNodesCount();
                        perceivedPlayersCount = communication.getPerceivedPlayerNodesCount();
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

    @Override
    public void gameRun(int UPDATE_RATE) {
        if ("true".equals(System.getProperty("JOT.scenerotate"))) {
            //set communication configuration
            communication.startGridNode(gridConfig);
        }

        super.gameRun(UPDATE_RATE);
    }

    @Override
    public void gameShutdown(GL2 gl) {
        System.out.println("Closing...");
        communication.stopGridNode();

        if (usePathFinders) {
            ((PathFinders) ai).dispose(gl);
        }

        if (useSteeringBehaviours && !usePathFinders) {
            ((SteeringBehaviours) ai).dispose(gl);
        }

        if (useTerrainGenerators) {
            tg.dispose(gl);
        }

        if (HOG2Maps) {
            ((HOG2MapMesh) sceneManager.getTransformGroup("floor").getMesh()).dispose(gl);
        }

        if (useMazeGenerators) {
            mg.dispose(gl);
        }

        //write2Log();
        super.gameShutdown(gl);
    }

    @Override
    public void gameUpdate() {
        if ("true".equals(System.getProperty("JOT.scenerotate"))) {
            synchronized (GameMain.this) {
                if (sceneManager != null) {
                    Point3f cameraRotation = sceneManager.getCameraRotation("THIRD_PERSON");
                    cameraRotation.y += .3f;
                    sceneManager.setCameraRotation("THIRD_PERSON", cameraRotation);

                    if (keyBoardHandler.isDetecting("1")) {
                        sceneManager.setShowTextures();
                    }

                    if (keyBoardHandler.isDetecting("2")) {
                        sceneManager.setShowWireframe();
                    }
                }
            }
        } else {
            if (state == State.GAME_OVER) {
                return;
            }

            if (state == State.PLAYING) {
                //System.err.println("Update.");
                if (updateCount == UPDATE_RATE) {
                    updateCount = 0;

                    //System.err.println("Performed remote updates processing " + communicationUpdateCount + " times.");
                    //communicationUpdateCount = 0;
                }
                updateCount++;

                synchronized (GameMain.this) {
                    //Physics Update 1: Bullets Processing
                    if (sceneManager != null) {
                        sceneManager.updateMutableObjects();
                    }

                    //Input Update
                    if (windowActive && state == State.PLAYING) {
                        Point3f newMouseCursorPosition = mouseHandler.getPosition();

                        long timeStamp = System.currentTimeMillis();

                        if (keyBoardHandler.isDetecting("1")) {
                            sceneManager.setShowTextures();
                        }

                        if (keyBoardHandler.isDetecting("2")) {
                            sceneManager.setShowWireframe();
                        }

                        if (keyBoardHandler.isDetecting("3")) {
                            sceneManager.setCameraToUse("FIRST_PERSON");
                        }

                        if (keyBoardHandler.isDetecting("4")) {
                            sceneManager.setCameraToUse("THIRD_PERSON");
                        }

                        if (keyBoardHandler.isDetecting("5")) {
                            sceneManager.setCameraToUse("PERSPECTIVE");
                        }

                        if (keyBoardHandler.isDetecting("6")) {
                            sceneManager.setCameraToUse("PERSPECTIVE_FOLLOW");
                        }

                        if (keyBoardHandler.isDetecting("7")) {
                            sceneManager.setCameraToUse("UPPER_VIEW");
                        }

                        if (keyBoardHandler.isDetecting("8")) {
                            sceneManager.setCameraToUse("UPPER_VIEW_FOLLOW");
                        }

                        boolean wDown = keyBoardHandler.isContinuouslyDetecting("W") || keyBoardHandler.isContinuouslyDetecting("w");
                        boolean sDown = keyBoardHandler.isContinuouslyDetecting("S") || keyBoardHandler.isContinuouslyDetecting("s");
                        boolean aDown = keyBoardHandler.isContinuouslyDetecting("A") || keyBoardHandler.isContinuouslyDetecting("a");
                        boolean dDown = keyBoardHandler.isContinuouslyDetecting("D") || keyBoardHandler.isContinuouslyDetecting("d");
                        //System.out.println(wDown + " " + sDown + " " + dDown + " " + aDown + " ");

                        boolean qDown = keyBoardHandler.isContinuouslyDetecting("Q") || keyBoardHandler.isContinuouslyDetecting("q");
                        boolean eDown = keyBoardHandler.isContinuouslyDetecting("E") || keyBoardHandler.isContinuouslyDetecting("e");

                        if (wDown && sDown) {
                            wDown = false;
                            sDown = false;
                        }
                        if (aDown && dDown) {
                            aDown = false;
                            dDown = false;
                        }
                        if (qDown && eDown) {
                            qDown = false;
                            eDown = false;
                        }

                        //Moving & rotating
                        float height = 0;
                        float direction = 0;
                        boolean isMoving = false;
                        boolean isMovingUp = false;
                        boolean isMovingDown = false;
                        boolean isRotating = false;

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

                        if (qDown) {
                            height += 1;
                            isMovingUp = true;
                        }
                        if (eDown) {
                            height -= 1;
                            isMovingDown = true;
                        }

                        //Physics Update 2: Player Update
                        if (sceneManager != null && sceneManager.getPlayer(playerId) != null) {
                            Point3f rotation = sceneManager.getPlayer(playerId).getRotation();

                            if (!A_AI.doAI) {
                                if (newMouseCursorPosition.x != 0) {
                                    isRotating = true;
                                    rotation.y += newMouseCursorPosition.x;
                                    sceneManager.getPlayer(playerId).updateRotationY(rotation.y);
                                }

                                Point3f position = sceneManager.getPlayer(playerId).getPresentTranslation();
                                Point3f newPosition = A_Kinematics.translate(new Point3f(), velocity, rotation.y, direction);
                                if (isMovingUp || isMovingDown) {
                                    newPosition.y += height;
                                }

                                BoundingVolume boundingVolume = new BoundingVolume(sceneManager.getPlayer(playerId).getBoundingVolume());
                                boundingVolume.min.add(newPosition);
                                boundingVolume.max.add(newPosition);
                                TransformGroup temp = new TransformGroup(playerId);
                                temp.setBoundingVolume(boundingVolume);
                                temp.addChild(sceneManager.getPlayer(playerId).getMesh());

                                String playerCrash = sceneManager.checkPlayerPlayerCollision(temp);

                                Point3f pastPosition = new Point3f(position);
                                //Point3f futurePosition = new Point3f(position);
                                //futurePosition.add(newPosition);

                                sceneManager.getPlayer(playerId).getRotation();

                                if ((isMoving || isMovingUp || isMovingDown) && !sceneManager.checkSceneBoundsCollision(boundingVolume)
                                        && !sceneManager.checkPlayerImmutableObjectCollision(temp)
                                        && ((playerCrash == null) || (playerId.equals(playerCrash)))) {
                                    if (!isMoving) {
                                        newPosition.x = 0;
                                        newPosition.z = 0;
                                    }

                                    sceneManager.getPlayer(playerId).updateTranslation(newPosition);
                                } else {
                                    isMoving = false;
                                    isMovingUp = false;
                                    isMovingDown = false;
                                }

                                attacking = mouseHandler.isContinuouslyDetecting("SHOOT") && timeStamp > (lastTimeStamp + 500);
                                if (attacking) {
                                    lastTimeStamp = timeStamp;
                                }

                                if (updateCount % Math.round((double) UPDATE_RATE / COMMUNICATION_UPDATE_RATE) == 0) {
                                    if ((attacking || isRotating || isMoving || isMovingUp || isMovingDown) && communication.isGridRunning()) {
                                        //if (communication.isGridRunning()) {
                                        GameUpdate playerGameUpdate = getGameUpdate();
                                        communication.sendUpdate(playerGameUpdate, communication.exitedZone(pastPosition, position));
                                        //System.err.println("Update sent.");
                                    }
                                }

                                //if (communication.exitedZone(pastPosition, position)) {
                                //    System.out.println("exited zone");
                                //} else {
                                //    System.out.println("Same zone");
                                //}
                            } else {
                                ai.update(sceneManager);
                                Point3f pastPosition = sceneManager.getPlayer(playerId).getPastTranslation();
                                Point3f position = sceneManager.getPlayer(playerId).getPresentTranslation();

                                attacking = ai.isAttacking(timeStamp, lastTimeStamp);
                                if (attacking) {
                                    lastTimeStamp = timeStamp;
                                }

                                if (updateCount % Math.round((double) UPDATE_RATE / COMMUNICATION_UPDATE_RATE) == 0) {
                                    if ((attacking || ai.isMoving()) && communication.isGridRunning()) {
                                        //if (communication.isGridRunning()) {
                                        GameUpdate playerGameUpdate = getGameUpdate();
                                        communication.sendUpdate(playerGameUpdate, communication.exitedZone(pastPosition, position));
                                        //System.err.println("A.I. update sent.");
                                    }
                                }

                                //if (communication.exitedZone(pastPosition, position)) {
                                //    System.out.println("exited zone");
                                //} else {
                                //    System.out.println("Same zone");
                                //}
                            }

                            //attacking = false;
                            if (attacking) {
                                //System.out.println("Shooting!");

                                Point3f bulletPosition = new Point3f(sceneManager.getPlayer(playerId).getPresentTranslation());
                                bulletPosition.y += 1.0f;
                                //Point3f rotation = sceneManager.getPlayer(playerId).getRotation();

                                //float newRotation = rotation.y;
                                //if (LeftShoot) {
                                //    //System.out.println("LeftShoot");
                                //    newRotation += 50;
                                //    LeftShoot = false;
                                //} else {
                                //    //System.out.println("RightShoot");
                                //    newRotation -= 50;
                                //    LeftShoot = true;
                                //}
                                try {
                                    TransformGroup bulletRotate = models.get("bullet").clone();
                                    TransformGroup bullet = new TransformGroup(playerId + "Bullet" + bulletIdSequence++, models.get("bullet"));
                                    bulletRotate.setBoundingVolume(null);
                                    bullet.getBoundingVolume().setRenderBoundingVolume(true);
                                    bullet.addChild(bulletRotate);
                                    bullet.setScaling(new Point3f(0.5f, 0.5f, 0.5f));
                                    //bullet.setTranslation(bulletPosition);
                                    //bullet.setRotation(new Point3f(rotation));
                                    bullet.setVelocity(new Point3f(0.8f, 2.5f, 0.8f));
                                    bullet.updateRotationY(rotation.y);
                                    bullet.updateTranslation(bulletPosition);
                                    sceneManager.addMutableObject(bullet);
                                } catch (CloneNotSupportedException ex) {
                                    Logger.getLogger(GameMain.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }

                            if (updateCount % Math.round((double) UPDATE_RATE / COMMUNICATION_UPDATE_RATE) == 0) {
                                //System.err.println("Remote updates processing.");
                                //communicationUpdateCount++;
                                communication.update();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * For a remote gameUpdate update the game sceneManager.
     *
     * @param gameUpdate gameUpdate to process.
     */
    public void gameUpdate(final GameUpdate gameUpdate) {
        if (state == State.PLAYING && (!communication.getLocalNodeId().equals(gameUpdate.nodeId))) {
            synchronized (GameMain.this) {
                //System.err.println("Game Updated");

                try {
                    boolean existed = true;

                    //if remote player is not in sceneManager...                
                    TransformGroup player = sceneManager.getPlayer(gameUpdate.objectId);
                    if (player == null) {
                        TransformGroup playerRotate = models.get("tie").clone();
                        player = new TransformGroup(gameUpdate.objectId, models.get("tie"));
                        //TransformGroup playerRotate = models.get("xwing").clone();
                        //player = new TransformGroup(gameUpdate.objectId, models.get("xwing"));
                        player.setNodeId(gameUpdate.nodeId);
                        //player.getBoundingVolume().setDrawBoundingVolume(true);
                        playerRotate.setBoundingVolume(null);
                        player.addChild(playerRotate);
                        sceneManager.addPlayer(player);
                        existed = false;
                    }

                    //update remote player data
                    player = sceneManager.getPlayer(gameUpdate.objectId);
                    player.setHealth(gameUpdate.health);
                    player.setTranslation(new Point3f(gameUpdate.position));
                    player.setRotation(new Point3f(gameUpdate.rotation));

                    //if remote player not inside local player AOI remove remote player from the hashtable players ...
                    Point3f position = sceneManager.getPlayer(playerId).getPresentTranslation();
                    if (communication.toRemove(position, gameUpdate)) {
                        //... and remove remote player from the scene
                        sceneManager.removePlayer(player);

                        //if broadcast from remote player in the AOI of local player ...
                        if (existed) {
                            //System.out.println("reply remove");
                            //... send remote player reply to remove local player
                            GameUpdate playerGameUpdate = getGameUpdate();
                            communication.sendUpdate(playerGameUpdate, gameUpdate.nodeId);
                        } //else {
                        //    System.out.println("no reply remove");
                        //}
                    } else {
                        //if broadcast from remote player not in the AOI of local player ...
                        if (!existed) {
                            //System.out.println("reply add");
                            //... send remote player reply to add local player
                            GameUpdate playerGameUpdate = getGameUpdate();
                            communication.sendUpdate(playerGameUpdate, gameUpdate.nodeId);
                        } //else {
                        //    System.out.println("no reply add");
                        //}

                        //if remote player is attacking inside local player AOI add bullet to scene
                        if (gameUpdate.isAttacking) {
                            try {
                                TransformGroup bulletRotate = models.get("bullet").clone();
                                TransformGroup bullet = new TransformGroup(gameUpdate.objectId + "Bullet" + bulletIdSequence++, models.get("bullet"));
                                bulletRotate.setBoundingVolume(null);
                                bullet.getBoundingVolume().setRenderBoundingVolume(true);
                                bullet.addChild(bulletRotate);
                                bullet.setScaling(new Point3f(0.5f, 0.5f, 0.5f));
                                //bullet.setTranslation(new Point3f(gameUpdate.position));
                                //bullet.setRotation(new Point3f(gameUpdate.rotation));
                                bullet.updateRotationY(gameUpdate.rotation.y);
                                bullet.updateTranslation(new Point3f(gameUpdate.position));
                                sceneManager.addMutableObject(bullet);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(GameMain.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    }
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(GameMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Get a gameUpdate for the local unit/avatar/player controlled by either
     * input devices or A.I..
     *
     * @return a gameUpdate for the local unit/avatar/player.
     */
    public GameUpdate getGameUpdate() {
        GameUpdate playerGameUpdate = new GameUpdate();
        playerGameUpdate.isAttacking = attacking;
        playerGameUpdate.isInteracting = interacting;
        playerGameUpdate.objectId = sceneManager.getPlayer(playerId).getId();
        playerGameUpdate.nodeId = communication.getLocalNodeId();
        playerGameUpdate.position = sceneManager.getPlayer(playerId).getPresentTranslation();
        playerGameUpdate.rotation = sceneManager.getPlayer(playerId).getRotation();
        playerGameUpdate.health = sceneManager.getPlayer(playerId).getHealth();
        playerGameUpdate.timeSent = System.nanoTime();

        return playerGameUpdate;
    }

    private void write2Log() {
        if (PlayerNode.usePlayerNode && communication.isGridNodeStarted()) {
            File logFile = new File("../../../JOTCore/log/log_" + communication.getLocalNodeId().toString() + ".txt");
            FileWriter out;
            try {
                out = new FileWriter(logFile);
                out.write("LOWEST FPS: " + Float.toString(lowestFPS) + " with "
                        + Float.toString(playersCount) + " total playerNodes " + "and "
                        + Float.toString(perceivedPlayersCount) + " in this playerNode AOI.");
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        setGameConstants();
        Game game = null;
        while (game == null) {
            //if user Id provided and ...
            if (args.length >= 2) {

                usePathFinders = PathFinders.validAILogic(args[1]);
                useSteeringBehaviours = SteeringBehaviours.validAILogic(args[1]);

                //if no valid A.I. logic selected ...
                if (!useSteeringBehaviours && !usePathFinders) {
                    if ("test".equals(args[1].toLowerCase())) {
                        System.setProperty("JOT.testMode", "true");
                        args[1] = "singlePlayer";
                    }
                    game = new GameMain(args[0], "", args[1]);
                } else {
                    if (args.length == 2) {
                        //... otherwhise if proper A.I. logic selected.
                        game = new GameMain(args[0], args[1], "");
                    } else {
                        if ("test".equals(args[2].toLowerCase())) {
                            System.setProperty("JOT.testMode", "true");
                            args[2] = "singlePlayer";
                        }

                        //... otherwhise if proper A.I. logic selected and grid config provided.
                        game = new GameMain(args[0], args[1], args[2]);
                    }
                }
            } else if (args.length == 1) {
                //if only user Id provided
                game = new GameMain(args[0], "", "");
            } else {
                usePathFinders = false;
                useSteeringBehaviours = false;
                useTerrainGenerators = false;
                useMazeGenerators = false;

                System.setProperty("JOT.testMode", "true");
                System.setProperty("JOT.scenerotate", "true");

                //if neither user id, A.I. proper logic, or grid config provided.
                game = new GameMain("player", "scenerotate", "singleplayer");
            }
        }
        game.gameRun(UPDATE_RATE);
    }
}
