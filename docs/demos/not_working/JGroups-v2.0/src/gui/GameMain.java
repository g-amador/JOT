package gui;

//import ai.PathFinders;
import ai.A_AI;
import ai.PathFinders;
import ai.SteeringBehaviours;
import communication.GameUpdate;
import communication.PlayerNode;
//import geometry.I_GeometryNode;
import geometry.BoundingVolume;
import geometry.TransformGroup;
import geometry.generators.MazeGenerators;
import geometry.generators.TerrainGenerators;
import input.devices.MouseHandler;
import input.devices.KeyBoardHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL2;
import javax.vecmath.Point3f;
import managers.SceneManager;
import physics.A_Kinematics;
import static ui.Game.mouseHandler;
//import physics.A_Collision;
//import java.util.Iterator;

/**
 *
 * @author G. Amador & A. Gomes
 */
public class GameMain extends A_Game {

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
    private static final boolean usePathFinders = false;
    private static final boolean useSteeringBehaviours = false;
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
    private final float lowestFPS = Float.POSITIVE_INFINITY;
    private final float playersCount = 1;
    private final float perceivedPlayersCount = 1;
//    public GL2 gl;
//    public SceneManager sceneManager;
//    public String playerId;     //the id for the player running on this node.
//    protected String gameName;  //the name for the game window/frame. 
//    protected MouseHandler mouseHandler;
//    protected KeyBoardHandler keyBoardHandler;
//    //private static TerrainGenerators tg;
//    private static A_AI ai;
//        private static boolean usePathFinders = false;
//    private static boolean useSteeringBehaviours = false;
//    //private static MazeGenerators mg;     //TODO: (MazeGeneration) uncomment only when implemented maze generation.
//    //private int mazeWidth = 25;
//    //private int mazeLength = 25;
//    protected PlayerNode communication;
//    private boolean attacking;
//    private boolean interacting;
//    //private boolean LeftShoot;
//    private int bulletIdSequence = 1;
//    private Point3f velocity = new Point3f(0.5f, 0.5f, 0.5f);   //the velocity for any moving avatar or bullet in the sceneManager.
//    protected ConcurrentHashMap<String, TransformGroup> models = new ConcurrentHashMap<String, TransformGroup>();
//    protected float size = SceneManager.getSceneSize();       //absolute size value of one of the sides of the square that defines the floor, i.e., floor as size*size area.    
    //private static boolean navi3D = false;    //TODO: implement navi3D
    private static final boolean spaceGame = false;
    //private static final boolean spaceGame = true;
    private static final boolean stupidGame = false;
    //private static final boolean stupidGame = true;
    //private static final boolean dukeBeanEm = false;
    private static final boolean dukeBeanEm = true;

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
        System.out.println("Load model textures.");
        ConcurrentHashMap<String, String> texturesPaths = new ConcurrentHashMap<String, String>();
        if (spaceGame) {
            texturesPaths.put("tie", "/models/dae/tie/tieskin.jpg");
            texturesPaths.put("xwing", "/models/dae/xwing/xwingskin.jpg");
        } else if (stupidGame) {
            texturesPaths.put("cow", "/models/dae/cow/Cow.png");
            texturesPaths.put("duck", "/models/dae/duck/duckCM.jpg");
            texturesPaths.put("brick1", "/textures/materials/brick1.jpg");
        }
        sceneManager = new SceneManager(texturesPaths);

        //Setup cameras
        //NOTE: camera IDs must differ and may not be necessarly the playerId, e.g., camera1, camera2, camera3.
        if ("true".equals(System.getProperty("JOT.scenerotate"))) {
            sceneManager.addCamera(new Camera(size, playerId, Camera.CameraType.THIRD_PERSON));
        } else {
            sceneManager.addCamera(new Camera(size, playerId, Camera.CameraType.PERSPECTIVE));
            //sceneManager.addCamera(new Camera(size, playerId, Camera.CameraType.FIRST_PERSON));
            sceneManager.addCamera(new Camera(size, playerId, Camera.CameraType.THIRD_PERSON));
            //sceneManager.addCamera(new Camera(size, playerId, Camera.CameraType.UPPER_VIEW));
        }
        sceneManager.setCameraToUse(playerId);

        //Setup floor
        TransformGroup floor;
        if (spaceGame) {
            //floor = SceneManager.buildFloor(size, "/textures/World_05/SkyBox2_down.jpg");
            floor = SceneManager.buildFloor(size, "/textures/zones/zones9.png");

        } else if (stupidGame) {
            //floor = SceneManager.buildFloor(size, "/textures/World_03/SkyBox2_down.jpg");
            floor = SceneManager.buildFloor(size, "/textures/zones/zones9.png");
        } else {
            floor = SceneManager.buildFloor(size, "/textures/World_01/SkyBox2_down.jpg");
            //floor = SceneManager.buildFloor(size, "/textures/zones/zones9.png");
        }
        //floor = SceneManager.buildFloor(size, "/textures/zones/a3.png");
        //floor = SceneManager.buildFloor(size, "/textures/zones/aura-nimbus.png");
        //floor = SceneManager.buildFloor(size, "/textures/zones/delaunay.png");
        //floor = SceneManager.buildFloor(size, "/textures/zones/delaunay_neighbours.png");
        //floor = SceneManager.buildFloor(size, "/textures/zones/hexagons.png");
        //floor = SceneManager.buildFloor(size, "/textures/zones/hexagons_neighbours.png");
        //floor = SceneManager.buildFloor(size, "/textures/zones/hexagons_neighbours_aura_nimbus.png");
        //floor = SceneManager.buildFloor(size, "/textures/zones/zones25.png");
        //floor = SceneManager.buildFloor(size, "/textures/zones/zones25_neighbours.png");
        //floor = SceneManager.buildFloor(size, "/textures/zones/zones25_neighbours_aura_nimbus.png");
        //floor = SceneManager.buildFloor(size, "/textures/zones/delaunay_neighbours_improved.png");        
        floor.setRotation(new Point3f(90, 180, 0));
        if (spaceGame) {
            floor.updateTranslation(new Point3f(0, size / 2 - 2, 0));
        } else {
            floor.updateTranslation(new Point3f(0, size / 2, 0));
        }

        //floor = SceneManager.buildTerrain(size, null, 5, 50, 3);
        //Iterator<IGeometryNode> childIterator = floor.childIterator();
        //tg = ((TerrainGenerators) childIterator.next());
        //if (spaceGame) {
        //    floor.updateTranslation(new Point3f(-size / 2, size / 2 - 2, -size / 2));
        //} else {
        //    floor.updateTranslation(new Point3f(-size / 2, size / 2, -size / 2));
        //}
        //floor = SceneManager.buildMaze(size - size / 10, mazeWidth, mazeLength);
        //Iterator<IGeometryNode> childIterator = floor.childIterator();
        //mg = ((MazeGenerators) childIterator.next());        
        //if (spaceGame) {
        //    floor.updateTranslation(new Point3f(0, size / 2 - 2, 0));
        //} else {
        //    floor.updateTranslation(new Point3f(0, size / 2, 0));
        //}        
        sceneManager.setFloor(floor);
        System.out.println("Floor created.");

        //Setup skyBox
        TransformGroup skyBox;
        if (spaceGame) {
            skyBox = SceneManager.buildSkyBox(size / 5, "/textures/World_05/SkyBox2", "jpg");
        } else if (stupidGame) {
            skyBox = SceneManager.buildSkyBox(size / 5, "/textures/World_03/SkyBox2", "jpg");
        } else {
            skyBox = SceneManager.buildSkyBox(size / 5, "/textures/World_01/SkyBox2", "jpg");
        }

        skyBox.setScaling(new Point3f(5, 5, 5));
        //skyBox.setRotation(new Point3f(- 90, 0, 0));
        skyBox.setBoundingVolume(null);
        sceneManager.setSkyBox(skyBox);
        System.out.println("Sky Box added.");

        //Setup immutableObjects
        if (!"true".equals(System.getProperty("JOT.scenerotate"))
                && !spaceGame) {
            int boxCount = 6;

            for (int i = 0; i < boxCount; i++) {
                float rotation = i * (360 / boxCount);
                TransformGroup immutableObject;
                if (dukeBeanEm) {
                    immutableObject = SceneManager.buildBrickWall("immutableObject" + (i + 1), "brick1", 0, BoundingVolume.BoundingVolumeType.AABB);
                } else {
                    immutableObject = SceneManager.buildBrickWall("immutableObject" + (i + 1), "brick1", 0, BoundingVolume.BoundingVolumeType.OBB);
                }
                Point3f immutableObjectPostion = A_Kinematics.translate(new Point3f(), 15, rotation, 0);
                immutableObject.setTranslation(immutableObjectPostion);
                sceneManager.addImmutableObject(immutableObject);
                System.out.println("Obstacle " + (i + 1) + " added");
            }
        }
        //Setup models
        if (spaceGame) {
            //models.put("tie", sceneManager.loadFormat("/models/dae/tie/tiefighter_out.dae", "tie", BoundingVolume.BoundingVolumeType.OBB));
            models.put("tie", sceneManager.loadFormat("/models/dae/tie/tiefighter.dae", "tie", 70, BoundingVolume.BoundingVolumeType.OBB));

            //models.put("xwing", sceneManager.loadFormat("/models/dae/xwing/xwing_out.dae", "xwing", 3300, BoundingVolume.BoundingVolumeType.SPHERE));
            models.put("xwing", sceneManager.loadFormat("/models/dae/xwing/xwing.dae", "xwing", 3300, BoundingVolume.BoundingVolumeType.SPHERE));
            models.get("xwing").setRotationY(180.0f);

            models.put("bullet", sceneManager.loadFormat("/models/dae/CoffeeBean2_out.dae", "bullet", 1, BoundingVolume.BoundingVolumeType.OBB));
        } else if (stupidGame) {
            //models.put("cow", sceneManager.loadFormat("/models/dae/cow/cow_out.dae", "cow", 1, BoundingVolume.BoundingVolumeType.OBB));
            models.put("cow", sceneManager.loadFormat("/models/dae/cow/cow.dae", "cow", 1, BoundingVolume.BoundingVolumeType.OBB));
            models.get("cow").setRotationZ(90.0f);
            models.get("cow").setRotationX(-90.0f);

            //models.put("duck", sceneManager.loadFormat("/models/dae/duck/duck_triangulate_out.dae", "duck", 250, BoundingVolume.BoundingVolumeType.SPHERE));
            models.put("duck", sceneManager.loadFormat("/models/dae/duck/duck_triangulate.dae", "duck", 250, BoundingVolume.BoundingVolumeType.SPHERE));
            models.get("duck").setRotationY(-90.0f);

            models.put("bullet", sceneManager.loadFormat("/models/dae/CoffeeBean2_out.dae", "bullet", 1, BoundingVolume.BoundingVolumeType.OBB));
        } else {
            //models.put("duke", sceneManager.loadFormat("/models/dae/Duke_posed_out.dae", "duke", 1, BoundingVolume.BoundingVolumeType.AABB));
            models.put("duke", sceneManager.loadFormat("/models/dae/Duke_posed.dae", "duke", 1, BoundingVolume.BoundingVolumeType.AABB));
            models.get("duke").setRotationY(180);
            models.put("bullet", sceneManager.loadFormat("/models/dae/CoffeeBean2_out.dae", "bullet", 1, BoundingVolume.BoundingVolumeType.AABB));
        }

        try {
            TransformGroup playerRotate;
            TransformGroup player;
            if (spaceGame) {
                playerRotate = models.get("tie").clone();
                player = new TransformGroup(playerId, models.get("tie"));
                //playerRotate = models.get("xwing").clone();
                //player = new TransformGroup(playerId, models.get("xwing"));
            } else if (stupidGame) {
                playerRotate = models.get("cow").clone();
                player = new TransformGroup(playerId, models.get("cow"));
                //playerRotate = models.get("duck").clone();
                //player = new TransformGroup(playerId, models.get("duck"));
            } else {
                playerRotate = models.get("duke").clone();
                player = new TransformGroup(playerId, models.get("duke"));
            }
            player.getBoundingVolume().setRenderBoundingVolume(true);
            playerRotate.setBoundingVolume(null);
            player.addChild(playerRotate);
            player.setHealth(100);
            player.setVelocity(velocity);
            sceneManager.addPlayer(player);
            sceneManager.setLocalPlayerId(playerId);

            //TODO: initial random pos in less occupied space
            //Avoid placement in occupied by either immutableObject or player
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
                        if (playerId.toLowerCase().contains("pires")) {
                            randPosX = 0;
                            randPosZ = -45;
                        } else if (playerId.toLowerCase().contains("esteves")) {
                            randPosX = 0;
                            randPosZ = -20;
                            sceneManager.getPlayer(playerId).updateRotationY(180.0f);
                        } else {
                            randPosX = 45;
                            randPosZ = 45;
                            //randPosX = Math.round((float) (Math.random() * (size - 6)) - ((size - 6) / 2));
                            //randPosZ = Math.round((float) (Math.random() * (size - 6)) - ((size - 6) / 2));
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
            System.out.println(playerId + " added to the sceneManager.");
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
            //sceneManager.addPlayer(target);
            ai.setPrimaryTarget(target);

            //find best path
            //ai.setPath(ai.findBestPath());
        }

        if (!"true".equals(System.getProperty("JOT.scenerotate"))
                && "true".equals(System.getProperty("JOT.testMode"))) {
            try {
                TransformGroup player2Rotate;
                TransformGroup player2;
                if (spaceGame) {
                    player2Rotate = models.get("tie").clone();
                    player2 = new TransformGroup("player2", models.get("tie"));
                } else if (stupidGame) {
                    player2Rotate = models.get("cow").clone();
                    player2 = new TransformGroup("player2", models.get("cow"));
                } else {
                    player2Rotate = models.get("duke").clone();
                    player2 = new TransformGroup("player2", models.get("duke"));
                }
                player2.getBoundingVolume().setRenderBoundingVolume(true);
                player2Rotate.setBoundingVolume(null);
                player2.addChild(player2Rotate);
                player2.setHealth(100);
                player2.setVelocity(velocity);
                sceneManager.addPlayer(player2);
                sceneManager.getPlayer("player2").updateTranslation(new Point3f(25, 0, 12));
                //sceneManager.getPlayer("player2").updateTranslation(new Point3f(2, 0, 2));
                System.out.println("player2 added to the sceneManager.");

                TransformGroup player3Rotate;
                TransformGroup player3;
                if (spaceGame) {
                    player3Rotate = models.get("xwing").clone();
                    player3 = new TransformGroup("player3", models.get("xwing"));
                } else if (stupidGame) {
                    player3Rotate = models.get("duck").clone();
                    player3 = new TransformGroup("player3", models.get("duck"));
                } else {
                    player3Rotate = models.get("duke").clone();
                    player3 = new TransformGroup("player3", models.get("duke"));
                }
                player3.getBoundingVolume().setRenderBoundingVolume(true);
                player3Rotate.setBoundingVolume(null);
                player3.addChild(player3Rotate);
                player3.setHealth(100);
                player3.setVelocity(velocity);
                sceneManager.addPlayer(player3);
                sceneManager.getPlayer("player3").updateTranslation(new Point3f(30, 0, -2));
                //sceneManager.getPlayer("player3").updateTranslation(new Point3f(-2, 0, -2));
                sceneManager.getPlayer("player3").updateRotationY(45);
                System.out.println("player3 added to the sceneManager.");
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(GameMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        System.out.println("Options:");
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
                    //ai.renderLogic(gl);
                    //ai.renderGraph(gl);
                    //ai.renderVisited(gl);
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
        //set communication configuration
        communication.startGridNode(gridConfig);

        super.gameRun(UPDATE_RATE);
    }

    @Override
    public void gameShutdown(GL2 gl) {
        System.out.println("Closing...");
        communication.stopGridNode();

        ai.dispose(gl);
        //tg.dispose(gl);
        //mg.dispose(gl);

        super.gameShutdown(gl);
    }

    @Override
    public void gameUpdate() {
        if ("true".equals(System.getProperty("JOT.scenerotate"))) {
            synchronized (GameMain.this) {
                if (sceneManager != null) {
                    if (keyBoardHandler.isDetecting("1")) {
                        sceneManager.setShowTextures();
                    }

                    if (keyBoardHandler.isDetecting("2")) {
                        sceneManager.setShowWireframe();
                    }

                    Point3f cameraRotation = sceneManager.getCameraRotation(playerId);
                    cameraRotation.y += .3f;
                    sceneManager.setCameraRotation(playerId, cameraRotation);
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
                        //
                        //    Iterator<TransformGroup> playerIterator = sceneManager.getAllPlayers();
                        //    //TODO: parallelize
                        //    while (playerIterator.hasNext()) {
                        //        final TransformGroup player = playerIterator.next();
                        //        int hits = sceneManager.checkBulletHit(player);
                        //        if (hits > 0) {
                        //            int newHealth = player.getHealth() - hits;
                        //            System.out.println(player.getId() + " got hit. Health is now " + newHealth);
                        //            sceneManager.getPlayer(player.getId()).setHealth(newHealth);
                        //            if (newHealth <= 0) {
                        //                //TODO: Score points here
                        //                System.out.println("Player " + player.getId() + " died!");
                        //                sceneManager.getPlayer(player.getId()).setHealth(100);
                        //            }
                        //        }
                        //    }
                    }

                    //Input Update
                    if (windowActive && state == State.PLAYING) {
                        long timeStamp = System.currentTimeMillis();

                        if (keyBoardHandler.isDetecting("1")) {
                            sceneManager.setShowTextures();
                        }

                        if (keyBoardHandler.isDetecting("2")) {
                            sceneManager.setShowWireframe();
                        }

                        if (keyBoardHandler.isDetecting("P") || keyBoardHandler.isDetecting("p")) {
                            communication.showPlayers();
                        }
                        if (keyBoardHandler.isDetecting("M") || keyBoardHandler.isDetecting("m")) {
                            communication.showMonitors();
                        }

                        Point3f newMouseCursorPosition = mouseHandler.getPosition();

                        //TODO: implement for navi 3D
                        //if (navi3D) {
                        //    sceneManager.addCameraTilt(newMouseCursorPosition.y / 24);
                        //}
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

                                //TODO: implement for navi 3D
                                //if (newY != 0) {
                                //isRotating = true;
                                //
                                ////????????
                                //rotation.x = newMouseCursorPosition.y;
                                ////rotation.z = newMouseCursorPosition.y;
                                //sceneManager.getPlayer(playerId).updateRotationX(update);
                                ////sceneManager.getPlayer(playerId).updateRotationZ(update);
                                //}
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

                                //if ((isMoving || isMovingUp || isMovingDown) && !sceneManager.checkSceneBoundsCollision(futurePosition, size)
                                if ((isMoving || isMovingUp || isMovingDown) && !sceneManager.checkSceneBoundsCollision(boundingVolume)
                                        && !sceneManager.checkPlayerImmutableObjectCollision(temp)
                                        && (playerCrash == null || playerId.equals(playerCrash))) {
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

                                if (updateCount % Math.round((double) UPDATE_RATE / COMMUNICATION_UPDATE_RATE) == 0
                                        && (attacking || isRotating || isMoving || isMovingUp || isMovingDown)
                                        && communication.isGridRunning()) {
                                    //if (communication.isGridRunning()) {
                                    GameUpdate playerGameUpdate = getGameUpdate();
                                    communication.sendUpdate(playerGameUpdate, communication.exitedZone(pastPosition, position));
                                    //System.err.println("Update sent.");
                                }

                                //if (communication.exitedZone(pastPosition, position)) {
                                //    System.out.println("Exited zone");
                                //} else {
                                //    System.out.println("Same zone");
                                //}
                            } else {
                                ai.update(sceneManager);
                                Point3f pastPosition = sceneManager.getPlayer(playerId).getPastTranslation();
                                Point3f position = sceneManager.getPlayer(playerId).getPresentTranslation();

                                //attacking = ai.isShooting(timeStamp, lastTimeStamp);
                                if (attacking) {
                                    lastTimeStamp = timeStamp;
                                }

                                if (updateCount % Math.round((double) UPDATE_RATE / COMMUNICATION_UPDATE_RATE) == 0
                                        && (attacking || ai.isMoving())
                                        && communication.isGridRunning()) {
                                    //if (communication.isGridRunning()) {
                                    GameUpdate playerGameUpdate = getGameUpdate();
                                    communication.sendUpdate(playerGameUpdate, communication.exitedZone(pastPosition, position));
                                    //System.err.println("AI update sent.");
                                }

                                //if (communication.exitedZone(pastPosition, position)) {
                                //    System.out.println("exited zone");
                                //} else {
                                //    System.out.println("Same zone");
                                //}
                            }

                            if (attacking) {
                                //System.out.println("Shooting!");
                                Point3f bulletPosition = new Point3f(sceneManager.getPlayer(playerId).getPresentTranslation());
                                //Point3f rotation = sceneManager.getPlayer(playerId).getRotation();

                                if (!spaceGame) {
                                    bulletPosition.y += 1.0f;
                                }
                                float newRotation = rotation.y;

                                //if (!stupidGame) {
                                //    if (LeftShoot) {
                                //        //System.out.println("LeftShoot");
                                //        newRotation += 50;
                                //        LeftShoot = false;
                                //    } else {
                                //        //System.out.println("RightShoot");
                                //        newRotation -= 50;
                                //        LeftShoot = true;
                                //    }
                                //}
                                try {
                                    TransformGroup bulletRotate = models.get("bullet").clone();
                                    TransformGroup bullet = new TransformGroup(playerId + "Bullet" + bulletIdSequence++, models.get("bullet"));
                                    bulletRotate.setBoundingVolume(null);
                                    bullet.getBoundingVolume().setRenderBoundingVolume(true);
                                    bullet.addChild(bulletRotate);
                                    if (spaceGame) {
                                        bullet.setScaling(new Point3f(0.5f, 0.5f, 0.5f));
                                    }
                                    //bullet.setTranslation(bulletPosition);
                                    //bullet.setRotation(new Point3f(rotation));
                                    bullet.setVelocity(new Point3f(0.8f, 2.5f, 0.8f));
                                    bullet.updateRotationY(rotation.y);
                                    bullet.updateTranslation(bulletPosition);
                                    sceneManager.addMutableObject(bullet);

                                } catch (CloneNotSupportedException ex) {
                                    Logger.getLogger(GameMain.class
                                            .getName()).log(Level.SEVERE, null, ex);
                                }
                            }

                            if (updateCount % Math.round((double) UPDATE_RATE / COMMUNICATION_UPDATE_RATE) == 0) {
                                //System.err.prteztetintln("Remote updates processing.");
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
    public void gameUpdate(GameUpdate gameUpdate) {
        if (state == State.PLAYING && !communication.getLocalNodeId().equals(gameUpdate.nodeId)) {
            synchronized (GameMain.this) {
                //System.out.println("Game Updated");

                try {
                    //if remote player is not in sceneManager... 
                    TransformGroup player = sceneManager.getPlayer(gameUpdate.objectId);
                    if (player == null) {
                        TransformGroup playerRotate;
                        if (spaceGame) {
                            playerRotate = models.get("tie").clone();
                            player = new TransformGroup(gameUpdate.objectId, models.get("tie"));
                            //playerRotate = models.get("xwing").clone();
                            //player = new TransformGroup(gameUpdate.objectId, models.get("xwing"));
                        } else if (stupidGame) {
                            playerRotate = models.get("cow").clone();
                            player = new TransformGroup(gameUpdate.objectId, models.get("cow"));
                            //playerRotate = models.get("duck").clone();
                            //player = new TransformGroup(gameUpdate.objectId, models.get("duck"));
                        } else {
                            playerRotate = models.get("duke").clone();
                            player = new TransformGroup(gameUpdate.objectId, models.get("duke"));
                        }
                        player.setNodeId(gameUpdate.nodeId);
                        //player.getBoundingVolume().setRenderBoundingVolume(true);
                        playerRotate.setBoundingVolume(null);
                        player.addChild(playerRotate);
                        sceneManager.addPlayer(player);
                    }

                    //update remote player data
                    player = sceneManager.getPlayer(gameUpdate.objectId);
                    player.setHealth(new Integer(gameUpdate.health));
                    player.setTranslation(new Point3f(gameUpdate.position));
                    player.setRotation(new Point3f(gameUpdate.rotation));

                    //if remote player is more than two zones of distance ...
                    Point3f position = sceneManager.getPlayer(playerId).getPresentTranslation();
                    if (communication.toRemove(position, gameUpdate)) {
                        //... remove remote player from the scene
                        sceneManager.removePlayer(player);
                    } else {
                        //if remote player is attacking inside local player AOI add bullet to scene
                        if (gameUpdate.isAttacking) {
                            try {
                                TransformGroup bulletRotate = models.get("bullet").clone();
                                TransformGroup bullet = new TransformGroup(gameUpdate.objectId + "Bullet" + bulletIdSequence++, models.get("bullet"));
                                bulletRotate.setBoundingVolume(null);
                                bullet.getBoundingVolume().setRenderBoundingVolume(true);
                                bullet.addChild(bulletRotate);
                                if (spaceGame) {
                                    bullet.setScaling(new Point3f(0.5f, 0.5f, 0.5f));
                                }
                                //bullet.setTranslation(new Point3f(gameUpdate.position));
                                //bullet.setRotation(new Point3f(gameUpdate.rotation));
                                bullet.updateRotationY(gameUpdate.rotation.y);
                                bullet.updateTranslation(new Point3f(gameUpdate.position));
                                sceneManager.addMutableObject(bullet);

                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(GameMain.class
                                        .getName()).log(Level.SEVERE, null, ex);
                            }

//                            TransformGroup bullet;
//
//                            if (dukeBeanEm) {
//                                bullet = sceneManager.getBullet(BoundingVolume.BoundingVolumeType.AABB);
//                            } else {
//                                bullet = sceneManager.getBullet(BoundingVolume.BoundingVolumeType.OBB);
//                            }
//                            if (bullet != null) {
//                                if (spaceGame) {
//                                    bullet.setScaling(new Point3f(0.5f, 0.5f, 0.5f));
//                                }
//                                bullet.setTranslation(new Point3f(gameUpdate.position));
//                                bullet.setVelocity(new Point3f(gameUpdate.velocity));
//                                bullet.setRotation(new Point3f(gameUpdate.rotation));
//
//                                //... add to scene
//                                sceneManager.addBullet(bullet);
//                            }
                        }
                    }
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(GameMain.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Get a gameUpdate for the local unit/avatar/player controlled by either
     * input devices or AI.
     *
     * @return a gameUpdate for the local unit/avatar/player.
     */
    public GameUpdate getGameUpdate() {
        GameUpdate playerGameUpdate = new GameUpdate();
        playerGameUpdate.isAttacking = attacking;
        playerGameUpdate.isInteracting = interacting;
        playerGameUpdate.health = sceneManager.getPlayer(playerId).getHealth();
        playerGameUpdate.objectId = sceneManager.getPlayer(playerId).getId();
        playerGameUpdate.nodeId = communication.getLocalNodeId();
        playerGameUpdate.position = sceneManager.getPlayer(playerId).getPresentTranslation();
        playerGameUpdate.rotation = sceneManager.getPlayer(playerId).getRotation();
        playerGameUpdate.timeSent = System.nanoTime();

        return playerGameUpdate;
    }
}
