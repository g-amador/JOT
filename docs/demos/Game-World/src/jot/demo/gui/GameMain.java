package jot.demo.gui;

import static com.jogamp.newt.event.KeyEvent.VK_ESCAPE;
import com.jogamp.opengl.GL2;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.demo.gui.GameMain.Smoother.Default;
import static jot.demo.gui.GameMain.Smoother.NURBS;
import static jot.demo.gui.GameMain.Smoother.Off;
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
import jot.io.data.format.HOG2Map;
import jot.manager.SceneManager;
import jot.math.geometry.Node;
import jot.math.geometry.TransformGroup;
import jot.math.geometry.bounding.AbstractBoundingVolume;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.OBB;
import jot.math.geometry.generators.maze.AbstractMazeGenerator;
import jot.math.geometry.generators.maze.Debug;
import jot.math.geometry.generators.maze.Default;
import jot.math.geometry.generators.maze.Kruskal;
import jot.math.geometry.generators.maze.Prim;
import jot.math.geometry.generators.noise.Improved;
import jot.math.geometry.generators.noise.Marble;
import jot.math.geometry.generators.noise.Ridged;
import jot.math.geometry.generators.noise.Simplex;
import jot.math.geometry.generators.noise.Turbulence;
import jot.math.geometry.generators.noise.Wood;
import jot.math.geometry.generators.smoother.Surface;
import jot.math.geometry.generators.smoother.parametric.NURBSSurface;
import jot.math.geometry.generators.terrain.AbstractTerrainGenerator;
import jot.math.geometry.generators.terrain.DiamondSquare;
import jot.math.geometry.generators.terrain.Noise;
import static jot.physics.Kinematics.translatePolar;
import static jot.util.FrameworkOptions.frameworkOptions;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that performs the main game loop.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class GameMain extends Game {

    private static final Logger log = getLogger("GameMain");

    //private static final int CANVAS_WIDTH = 1_280;
    //private static final int CANVAS_HEIGHT = 900;
    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 600;

    /**
     * Number of Frames or Updates Per Second, i.e., FPS or UPS.
     */
    private static final int UPDATE_RATE = 40;

    static AbstractTerrainGenerator tg;
    static AbstractMazeGenerator mg;

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
    public String playerId;

    private Smoother smoother = Off;

    private final float sceneSize = 100;
    private final int mazeWidth = 25;
    private final int mazeLength = 25;
    private final int lod = 5;
    private final int roughness = (int) (this.sceneSize / 2);
    private final int smoothFactor = 3;
    private final Vector3D velocity = new Vector3D(0.5f, 0.5f, 0.5f);   //the velocity for any moving avatar in the  sceneManager.
    private final ConcurrentHashMap<String, TransformGroup> models = new ConcurrentHashMap<>();

    /**
     * The last time an updated was performed.
     */
    protected long lastTimeStamp = currentTimeMillis();

    /**
     * Auxiliary time counters and update counter.
     */
    protected long beginTime, endTime, updateCount = 0;

    /**
     * Background color
     */
    //private final float[] clearColorWhite = {1.0f, 1.0f, 1.0f, 1.0f};       //White.
    //private final float[] clearColorPurple = {0.3f, 0.3f, 0.7f, 0.0f};      //Purple.
    private final float[] clearColorPitchblack = {0.3f, 0.3f, 0.3f, 0.3f};  //Pitch black.

    private float direction;
    boolean isMoving, isRotating;

    /**
     * Constructor, receives a player unique id, mays set up (if a valid AI is
     * chosen) the AI to use, and configures a communication node with a
     * personalized configuration (if provided a valid path to a configuration
     * file).
     *
     * @param pId player unique Id.
     */
    public GameMain(String pId) {
        super("Game World", CANVAS_WIDTH, CANVAS_HEIGHT);

        this.playerId = pId;     //the players Id, i.e., user name
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
            this.keyBoard.registerInputEvent("0", '0');
            this.keyBoard.registerInputEvent("1", '1');
            this.keyBoard.registerInputEvent("2", '2');
            this.keyBoard.registerInputEvent("3", '3');
            this.keyBoard.registerInputEvent("4", '4');
            this.keyBoard.registerInputEvent("5", '5');
            this.keyBoard.registerInputEvent("6", '6');
            this.keyBoard.registerInputEvent("7", '7');
            this.keyBoard.registerInputEvent("8", '8');
            this.keyBoard.registerInputEvent("9", '9');
            this.keyBoard.registerInputEvent("F", 'F');
            this.keyBoard.registerInputEvent("F", 'f');
            this.keyBoard.registerInputEvent("T", 'T');
            this.keyBoard.registerInputEvent("T", 't');
            this.keyBoard.registerInputEvent("Z", 'Z');
            this.keyBoard.registerInputEvent("Z", 'z');
            this.keyBoard.registerInputEvent("X", 'X');
            this.keyBoard.registerInputEvent("X", 'x');
            this.keyBoard.registerInputEvent("C", 'C');
            this.keyBoard.registerInputEvent("C", 'c');
            this.keyBoard.registerInputEvent("B", 'B');
            this.keyBoard.registerInputEvent("B", 'b');
            this.keyBoard.registerInputEvent("N", 'N');
            this.keyBoard.registerInputEvent("N", 'n');
            this.keyBoard.registerInputEvent("M", 'M');
            this.keyBoard.registerInputEvent("M", 'm');
            this.keyBoard.registerInputEvent("W", 'W');
            this.keyBoard.registerInputEvent("W", 'w');
            this.keyBoard.registerInputEvent("A", 'A');
            this.keyBoard.registerInputEvent("A", 'a');
            this.keyBoard.registerInputEvent("S", 'S');
            this.keyBoard.registerInputEvent("S", 's');
            this.keyBoard.registerInputEvent("D", 'D');
            this.keyBoard.registerInputEvent("D", 'd');
        }
        this.keyBoard.registerInputEvent("Quit", VK_ESCAPE);

        //Set options text to display in main window.
        text.setTextLine("Options:");
        if (null == getProperty("JOT.scenerotate")) {
            if (frameworkOptions.get("useMouse")) {
                text.setTextLine("Mouse move - rotate Duke model.");
            }
            if (frameworkOptions.get("useKeyBoard")) {
                text.setTextLine("W/w,A/a,S/s,D/d keys - to move player.");
                text.setTextLine("Z/z key - use third person view camera.");
                text.setTextLine("X/x key - use perspective follow view camera.");
                text.setTextLine("C/c key - use upper follow view camera.");
                text.setTextLine("F key - toggle on/off show FPS.");
                text.setTextLine("T key - toggle on/off show help.");
                text.setTextLine("1 key - toggle on/off ligths.");
                text.setTextLine("2 key - toggle on/off textures.");
                text.setTextLine("3 key - toggle on/off wireframe.");
                if (frameworkOptions.get("useTerrainGenerators")) {
                    text.setTextLine("0 key - generate new terrain using diamond square.");
                    text.setTextLine("4 to 9 keys - generate new terrain using Improved noises.");
                    text.setTextLine("B/b key - use no terrain smooth.");
                    text.setTextLine("N/n key - use NURBSs terrain smooth.");
                    text.setTextLine("M/m key - use Default terrain smooth.");
                }
                if (frameworkOptions.get("useMazeGenerators")) {
                    text.setTextLine("4 key - generate new maze using default recursive algorithm.");
                    text.setTextLine("5 key - generate new maze using Kruskal's randomized algorithm.");
                    text.setTextLine("6 key - generate new maze using Prim's randomized algorithm.");
                    text.setTextLine("7 key - use default debug maze.");
                }
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

        log.info("Setting up sceneManager...");
        log.info("Load model textures.");
        ConcurrentHashMap<String, String> texturesPaths = new ConcurrentHashMap<>();
        texturesPaths.put("cow", "assets/models/dae/cow/Cow.png");
        texturesPaths.put("brick1", "assets/textures/materials/brick1.jpg");
        texturesPaths.put("floor1", "assets/textures/materials/floor1.png");
        texturesPaths.put("floor2", "assets/textures/materials/floor2.png");
        this.sceneManager = new SceneManager(texturesPaths);
        this.sceneManager.setSceneSize(this.sceneSize);

        //Setup cameras
        float aspectRatio = (float) CANVAS_WIDTH / CANVAS_HEIGHT;
        if ("true".equals(getProperty("JOT.scenerotate"))) {
            this.sceneManager.addCamera(new ThirdPerson(60.0F, aspectRatio, this.sceneSize, this.playerId));
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
            //sceneManager.setCamera2Use("UPPER_VIEW");
            //sceneManager.setCamera2Use("PERSPECTIVE");
        }
        log.info("Cameras setup done.");

        //Setup floor
        if (frameworkOptions.get("Floor")) {
            TransformGroup floor;
            if (frameworkOptions.get("useTerrainGenerators")) {
                switch (this.smoother) {
                    case Default:
                        if (!frameworkOptions.get("showTextures")) {
                            floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                    new DiamondSquare(this.lod, this.roughness, this.sceneSize),
                                    new Surface());
                        } else {
                            floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                    new DiamondSquare(this.lod, this.roughness, this.sceneSize),
                                    new Surface());
                            //floor = sceneManager.buildSmoothTerrain(
                            //        "floor2", smoothFactor, 
                            //        new DiamondSquare(lod, roughness, sceneSize), 
                            //        new Surface());
                        }
                        break;
                    case NURBS:
                        if (!frameworkOptions.get("showTextures")) {
                            floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                    new DiamondSquare(this.lod, this.roughness, this.sceneSize),
                                    new NURBSSurface());
                        } else {
                            floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                    new DiamondSquare(this.lod, this.roughness, this.sceneSize),
                                    new NURBSSurface());
                            //floor = sceneManager.buildSmoothTerrain(
                            //        "floor2", smoothFactor, 
                            //        new DiamondSquare(lod, roughness, sceneSize), 
                            //        new NURBSSurface());
                        }
                        break;
                    case Off:
                        if (!frameworkOptions.get("showTextures")) {
                            floor = this.sceneManager.buildTerrain(
                                    null,
                                    new DiamondSquare(this.lod, this.roughness, this.sceneSize));
                        } else {
                            floor = this.sceneManager.buildTerrain(
                                    "floor1",
                                    new DiamondSquare(this.lod, this.roughness, this.sceneSize));
                            //floor = sceneManager.buildSmoothTerrain(
                            //        "floor2", smoothFactor, 
                            //        new DiamondSquare(lod, roughness, sceneSize));
                        }
                        break;
                    default:
                        throw new AssertionError(this.smoother.name());
                }
                Iterator<Node> childIterator = floor.childIterator();
                tg = ((AbstractTerrainGenerator) childIterator.next());
                //TODO: make the mean position of the terrain always the same   
                floor.setScaling(new Vector3D(1.1f, 1.1f, 1.1f));
                floor.updateTranslation(new Vector3D(
                        -this.sceneSize / 2 - 1,
                        0.0,//-floor.getTranslation().getY(),
                        -this.sceneSize / 2 - 1));
            } else if (frameworkOptions.get("useMazeGenerators")) {
                log.info("Setting up maze generation.");
                //floor = sceneManager.buildMaze(
                //        new Debug(mazeLength, mazeWidth, sceneSize - sceneSize / 10));
                //floor = sceneManager.buildMaze(
                //        new Default(mazeLength, mazeWidth, sceneSize - sceneSize / 10));
                floor = this.sceneManager.buildMaze(
                        new Prim(this.mazeLength, this.mazeWidth, this.sceneSize - this.sceneSize / 10));
                //floor = sceneManager.buildMaze(
                //        new Kruskal(mazeLength, mazeWidth, sceneSize - sceneSize / 10));
                Iterator<Node> childIterator = floor.childIterator();
                mg = ((AbstractMazeGenerator) childIterator.next());
            } else if (frameworkOptions.get("HOG2Maps")) {
                log.info("Loading map file.");
                floor = this.assetManager.loadFormat("assets/HOG2/", "AIbook2.map", 0.25f, OBB);
                //floor = assetManager.loadFormat("assets/HOG2/", "AIbook2.map", 0.25f, OBB);
                //Dragon Age Origins maps
                //floor = assetManager.loadFormat("assets/HOG2/DragonAgeOrigins/map.zip", "arena2.map", 2.0f, OBB);
                //floor = assetManager.loadFormat("assets/HOG2/DragonAgeOrigins/map.zip", "brc202d.map", 4.5f, OBB);
                //floor = assetManager.loadFormat("assets/HOG2/DragonAgeOrigins/map.zip", "brc203d.map", 3.5f, OBB);
                //floor = assetManager.loadFormat("assets/HOG2/DragonAgeOrigins/map.zip", "brc204d.map", 3.8f, OBB);
                //floor = assetManager.loadFormat("assets/HOG2/DragonAgeOrigins/map.zip", "den005d.map", 3.0f, OBB);
                //floor = assetManager.loadFormat("assets/HOG2/DragonAgeOrigins/map.zip", "den011d.map", 1.6f, OBB);
                //floor = assetManager.loadFormat("assets/HOG2/DragonAgeOrigins/map.zip", "den501d.map", 3.0f, OBB);
                //floor = assetManager.loadFormat("assets/HOG2/DragonAgeOrigins/map.zip", "den602d.map", 4.5f, OBB);
                //floor = assetManager.loadFormat("assets/HOG2/DragonAgeOrigins/map.zip", "hrt201n.map", 2.8f, OBB);
                //floor = assetManager.loadFormat("assets/HOG2/DragonAgeOrigins/map.zip", "lak304d.map", 1.8f, OBB);
                //Warcraft III maps
//                floor = assetManager.loadFormat("assets/HOG2/WarcraftIII/map.zip", "battleground.map", 4.8f, OBB);
//                floor = assetManager.loadFormat("assets/HOG2/WarcraftIII/map.zip", "blastedlands.map", 4.8f, OBB);
//                floor = assetManager.loadFormat("assets/HOG2/WarcraftIII/map.zip", "divideandconquer.map", 4.8f, OBB);
//                floor = assetManager.loadFormat("assets/HOG2/WarcraftIII/map.zip", "dragonfire.map", 4.8f, OBB);
//                floor = assetManager.loadFormat("assets/HOG2/WarcraftIII/map.zip", "frostsabre.map", 4.8f, OBB);
                //floor = assetManager.loadFormat("assets/HOG2/WarcraftIII/map.zip", "gardenofwar.map", 4.6f, OBB);
//                floor = assetManager.loadFormat("assets/HOG2/WarcraftIII/map.zip", "harvestmoon.map", 4.8f, OBB);
//                floor = assetManager.loadFormat("assets/HOG2/WarcraftIII/map.zip", "isleofdread.map", 4.8f, OBB);
                //floor = assetManager.loadFormat("assets/HOG2/WarcraftIII/map.zip", "thecrucible.map", 4.6f, OBB);
                //floor = assetManager.loadFormat("assets/HOG2/WarcraftIII/map.zip", "tranquilpaths.map", 4.6f, OBB);

                //TODO: alter when implemented for a compound mesh.
                Vector3D barycentre = ((HOG2Map.HOG2MapMesh) floor.getMeshes().get(0)).getBarycentre();
                barycentre = new Vector3D(
                        barycentre.getX(), //sceneSize / 5, //AIbook1 & AIbook2
                //sceneSize / 2, //Arena
                this.sceneSize, //lak202d & lak304d
                        barycentre.getZ());
                this.sceneManager.setCamera2Use("UPPER_VIEW");
                this.sceneManager.getCamera("UPPER_VIEW").setViewPoint(
                        new Vector3D(barycentre.getX(), 0, barycentre.getZ() + .1));
                this.sceneManager.getCamera("UPPER_VIEW")
                        .setPosition(barycentre);
                //sceneManager.setCamera2Use("PERSPECTIVE");
                //sceneManager.getCamera("PERSPECTIVE").setPosition(new Vector3D(sceneSize * -.1, sceneSize * 0.08, sceneSize * -.1));
            } else {
                floor = this.sceneManager.buildFloor(this.sceneSize,
                        "assets/textures/skybox/World_01/SkyBox2_down.jpg");
                //floor.setRotation(new Vector3D(-90, 0, 0));
                floor.updateTranslation(new Vector3D(0, this.sceneSize / 2, 0));
            }
            this.sceneManager.setFloor(floor);
            log.info("Floor created.");
        }

        //Setup skyBox
        if (frameworkOptions.get("SkyBox")) {
            TransformGroup skyBox = this.sceneManager.buildSkyBox(this.sceneSize,
                    "assets/textures/skybox/World_01/SkyBox2", "jpg");
            //skyBox.setScaling(new Vector3D(5, 5, 5));
            //skyBox.setRotation(new Vector3D(- 90, 0, 0));
            skyBox.setBoundingVolume(0, null);
            this.sceneManager.setSky(skyBox);
            log.info("Sky Box added to the scene.");
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
            log.info("Sky Dome added to the scene.");
        }

        GameObject celestialObject;
        celestialObject = this.sceneManager.buildCelestialObject(
                "earth", 1, 50,
                "assets/textures/celestialObjects/earthmap1k.jpg");
        celestialObject.setBoundingVolume(0, null);
        //sceneManager.addMutableObject(celestialObject);
        //log.info("Celestial Object earth added to the scene.");

        if (!frameworkOptions.get("HOG2Maps")
                && !frameworkOptions.get("useTerrainGenerators")
                && !frameworkOptions.get("useMazeGenerators")) {
            //Setup obstacles
            int boxCount = 6;
            for (int i = 0; i < boxCount; i++) {
                float rotation = i * (360 / boxCount);
                //SPHERE or OBB or AABB            
                GameObject obstacle = this.sceneManager.buildBlock(
                        "obstacle" + (i + 1), "brick1", 1.0f, OBB);
                Vector3D obstaclePostion = translatePolar(ZERO, 15.0f, rotation, 0, 1);
                obstaclePostion = new Vector3D(
                        obstaclePostion.getX(),
                        obstaclePostion.getY() + 1,
                        obstaclePostion.getZ());
                obstacle.setPosition(obstaclePostion);
                //obstacle.getBoundingVolume().setRenderable(true);
                this.sceneManager.addImmutableObject(obstacle);
                //log.info(format("Obstacle %d added", (i + 1)));
            }
        }

        //Setup models   
        this.models.put("cow", this.assetManager.loadFormat(
                "assets/models/dae/cow/", "cow.dae", 1, OBB));
        //models.get("cow").setRotationY(-180);
        //models.get("cow").setRotationZ(90);
        //models.get("cow").setRotationX(90);
        this.models.get("cow").setRotationZ(90.0f);
        this.models.get("cow").setRotationX(-90.0f);

        this.models.put("duke", this.assetManager.loadFormat(
                "assets/models/dae/", "Duke_posed.dae", 1, OBB));
        this.models.get("duke").setRotationY(180);

        try {
            TransformGroup playerRotate;
            GameObject player;
            //playerRotate = models.get("cow").clone();
            //player = new GameObject(playerId, models.get("cow"));
            playerRotate = this.models.get("duke").clone();
            player = new GameObject(this.playerId, this.models.get("duke"));
            player.getBoundingVolume(0).setRenderable(true);
            //playerRotate.setBoundingVolume(0, null);
            player.addChild(playerRotate);
            player.setAttribute("health", 100);
            player.setVelocity(this.velocity);

            this.sceneManager.addPlayer(player);
            this.sceneManager.setLocalPlayerId(this.playerId);
            //sceneManager.getPlayer(playerId).updatePosition(new Vector3D(2, -200, 2));
            if (frameworkOptions.get("useTerrainGenerators")) {
                Vector3D position = this.sceneManager.getPlayer(this.playerId).getPosition();
                float i = (float) (((position.getX() + (this.sceneSize / 2)) / this.sceneSize) * tg.getGeometryLength());
                float j = (float) (((position.getZ() + (this.sceneSize / 2)) / this.sceneSize) * tg.getGeometryWidth());
                float height = tg.getMeanAltitude(i, j);
                this.sceneManager.getPlayer(this.playerId).updatePosition(new Vector3D(0.0d, height, 0.0d));
            }
            log.info(format(this.playerId + " added to the scene."));
        } catch (CloneNotSupportedException ex) {
            log.severe(ex.getMessage());
        }

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
            if (this.keyBoard.isDetecting("Quit")) {
                this.gameShutdown();
            }
            if (this.keyBoard.isDetecting("F")) {
                this.sceneManager.toggleOnOffShowFPS();
            }
            if (this.keyBoard.isDetecting("T")) {
                this.sceneManager.toggleOnOffShowText();
            }

            if (this.keyBoard.isDetecting("1")) {
                this.sceneManager.toggleOnOffLights();
            }
            if (this.keyBoard.isDetecting("2")) {
                this.sceneManager.toggleOnOffShowTextures();
            }
            if (this.keyBoard.isDetecting("3")) {
                this.sceneManager.toggleOnOffShowWireframe();
            }

            if (this.keyBoard.isDetecting("Z")) {
                this.sceneManager.setCamera2Use("THIRD_PERSON");
            }
            if (this.keyBoard.isDetecting("X")) {
                this.sceneManager.setCamera2Use("PERSPECTIVE");
                //sceneManager.setCamera2Use("PERSPECTIVE_FOLLOW");
            }
            if (this.keyBoard.isDetecting("C")) {
                this.sceneManager.setCamera2Use("UPPER_VIEW");
                //sceneManager.setCamera2Use("UPPER_VIEW_FOLLOW");
            }

            if (this.keyBoard.isDetecting("B")) {
                this.smoother = Off;
            }
            if (this.keyBoard.isDetecting("N")) {
                this.smoother = NURBS;
            }
            if (this.keyBoard.isDetecting("M")) {
                this.smoother = Default;
            }

            Vector3D oldPosition = ZERO;
            if (this.sceneManager != null) {
                oldPosition = this.sceneManager.getPlayer(this.playerId).getPosition();
            }
            float oldHeight = (float) oldPosition.getY();
            boolean newTerrainGenerated = false;

            if (this.keyBoard.isDetecting("0")
                    && frameworkOptions.get("useTerrainGenerators")) {
                TransformGroup floor;
                switch (this.smoother) {
                    case Default:
                        if (!frameworkOptions.get("showTextures")) {
                            floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                    new DiamondSquare(this.lod, this.roughness, this.sceneSize),
                                    new Surface());
                        } else {
                            floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                    new DiamondSquare(this.lod, this.roughness, this.sceneSize),
                                    new Surface());
                            //floor = sceneManager.buildSmoothTerrain(
                            //        "floor2", smoothFactor,
                            //        new DiamondSquare(lod, roughness, sceneSize),
                            //        new Surface());
                        }
                        break;
                    case NURBS:
                        if (!frameworkOptions.get("showTextures")) {
                            floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                    new DiamondSquare(this.lod, this.roughness, this.sceneSize),
                                    new NURBSSurface());
                        } else {
                            floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                    new DiamondSquare(this.lod, this.roughness, this.sceneSize),
                                    new NURBSSurface());
                            //floor = sceneManager.buildSmoothTerrain(
                            //        "floor2", smoothFactor,
                            //        new DiamondSquare(lod, roughness, sceneSize),
                            //        new NURBSSurface());
                        }
                        break;
                    case Off:
                        if (!frameworkOptions.get("showTextures")) {
                            floor = this.sceneManager.buildTerrain(
                                    null,
                                    new DiamondSquare(this.lod, this.roughness, this.sceneSize));
                        } else {
                            floor = this.sceneManager.buildTerrain(
                                    "floor1",
                                    new DiamondSquare(this.lod, this.roughness, this.sceneSize));
                            //floor = sceneManager.buildTerrain(
                            //        "floor2",
                            //        new DiamondSquare(lod, roughness, sceneSize));
                        }
                        break;
                    default:
                        throw new AssertionError(this.smoother.name());
                }
                Iterator<Node> childIterator = floor.childIterator();
                tg = ((AbstractTerrainGenerator) childIterator.next());
                floor.setScaling(new Vector3D(1.1f, 1.1f, 1.1f));
                floor.updateTranslation(new Vector3D(
                        -this.sceneSize / 2 - 1,
                        0.0,//-floor.getTranslation().getY(),
                        -this.sceneSize / 2 - 1));
                this.sceneManager.setFloor(floor);
                newTerrainGenerated = true;
            }

            if (this.keyBoard.isDetecting("4")) {
                TransformGroup floor;
                if (frameworkOptions.get("useTerrainGenerators")) {
                    switch (this.smoother) {
                        case Default:
                            if (!frameworkOptions.get("showTextures")) {
                                floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize, new Improved()),
                                        new Surface());
                            } else {
                                floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize, new Improved()),
                                        new Surface());
                                //floor = sceneManager.buildSmoothTerrain(
                                //        "floor2", smoothFactor,
                                //        new Noise(lod, roughness, sceneSize, new Improved()),
                                //        new Surface());
                            }
                            break;
                        case NURBS:
                            if (!frameworkOptions.get("showTextures")) {
                                floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize, new Improved()),
                                        new NURBSSurface());
                            } else {
                                floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize, new Improved()),
                                        new NURBSSurface());
                                //floor = sceneManager.buildSmoothTerrain(
                                //        "floor2", smoothFactor, 
                                //        new Noise(lod, roughness, sceneSize, new Improved()), 
                                //        new NURBSSurface());
                            }
                            break;
                        case Off:
                            if (!frameworkOptions.get("showTextures")) {
                                floor = this.sceneManager.buildTerrain(
                                        null,
                                        new Noise(this.lod, this.roughness, this.sceneSize, new Improved()));
                            } else {
                                floor = this.sceneManager.buildTerrain(
                                        "floor1",
                                        new Noise(this.lod, this.roughness, this.sceneSize, new Improved()));
                                //floor = sceneManager.buildTerrain(
                                //        "floor2", 
                                //        new Noise(lod, roughness, sceneSize, new Improved()));
                            }
                            break;
                        default:
                            throw new AssertionError(this.smoother.name());
                    }
                    Iterator<Node> childIterator = floor.childIterator();
                    tg = ((AbstractTerrainGenerator) childIterator.next());
                    floor.setScaling(new Vector3D(1.1f, 1.1f, 1.1f));
                    floor.updateTranslation(new Vector3D(
                            -this.sceneSize / 2 - 1,
                            0.0,//-floor.getTranslation().getY(),
                            -this.sceneSize / 2 - 1));
                    this.sceneManager.setFloor(floor);
                    newTerrainGenerated = true;
                }

                if (frameworkOptions.get("useMazeGenerators")) {
                    floor = this.sceneManager.buildMaze(
                            new Default(this.mazeLength, this.mazeWidth, this.sceneSize - this.sceneSize / 10));
                    Iterator<Node> childIterator = floor.childIterator();
                    mg = ((AbstractMazeGenerator) childIterator.next());
                    this.sceneManager.setFloor(floor);
                }
            }

            if (this.keyBoard.isDetecting("5")) {
                TransformGroup floor;
                if (frameworkOptions.get("useTerrainGenerators")) {
                    switch (this.smoother) {
                        case Default:
                            if (!frameworkOptions.get("showTextures")) {
                                floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize, new Marble()),
                                        new Surface());
                            } else {
                                floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize, new Marble()),
                                        new Surface());
                                //floor = sceneManager.buildSmoothTerrain(
                                //        "floor2", smoothFactor,
                                //        new Noise(lod, roughness, sceneSize, new Marble()),
                                //        new Surface());
                            }
                            break;
                        case NURBS:
                            if (!frameworkOptions.get("showTextures")) {
                                floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize, new Marble()),
                                        new NURBSSurface());
                            } else {
                                floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize, new Marble()),
                                        new NURBSSurface());
                                //floor = sceneManager.buildSmoothTerrain(
                                //        "floor2", smoothFactor, 
                                //        new Noise(lod, roughness, sceneSize, new Marble()), 
                                //        new NURBSSurface());
                            }
                            break;
                        case Off:
                            if (!frameworkOptions.get("showTextures")) {
                                floor = this.sceneManager.buildTerrain(
                                        null,
                                        new Noise(this.lod, this.roughness, this.sceneSize, new Marble()));
                            } else {
                                floor = this.sceneManager.buildTerrain(
                                        "floor1",
                                        new Noise(this.lod, this.roughness, this.sceneSize, new Marble()));
                                //floor = sceneManager.buildTerrain(
                                //        "floor2", 
                                //        new Noise(lod, roughness, sceneSize, new Marble()));
                            }
                            break;
                        default:
                            throw new AssertionError(this.smoother.name());
                    }
                    Iterator<Node> childIterator = floor.childIterator();
                    tg = ((AbstractTerrainGenerator) childIterator.next());
                    floor.setScaling(new Vector3D(1.1f, 1.1f, 1.1f));
                    floor.updateTranslation(new Vector3D(
                            -this.sceneSize / 2 - 1,
                            0.0,//-floor.getTranslation().getY(),
                            -this.sceneSize / 2 - 1));
                    this.sceneManager.setFloor(floor);
                    newTerrainGenerated = true;
                }

                if (frameworkOptions.get("useMazeGenerators")) {
                    floor = this.sceneManager.buildMaze(
                            new Kruskal(this.mazeLength, this.mazeWidth, this.sceneSize - this.sceneSize / 10));
                    Iterator<Node> childIterator = floor.childIterator();
                    mg = ((AbstractMazeGenerator) childIterator.next());
                    this.sceneManager.setFloor(floor);
                }
            }

            if (this.keyBoard.isDetecting("6")) {
                TransformGroup floor;
                if (frameworkOptions.get("useTerrainGenerators")) {
                    switch (this.smoother) {
                        case Default:
                            if (!frameworkOptions.get("showTextures")) {
                                floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize,
                                                new Ridged()),
                                        new Surface());
                            } else {
                                floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize,
                                                new Ridged()),
                                        new Surface());
                                //floor = sceneManager.buildSmoothTerrain(
                                //        "floor2", smoothFactor,
                                //        new Noise(lod, roughness, sceneSize, new Ridged()),
                                //        new Surface());
                            }
                            break;
                        case NURBS:
                            if (!frameworkOptions.get("showTextures")) {
                                floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize,
                                                new Ridged()),
                                        new NURBSSurface());
                            } else {
                                floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize,
                                                new Ridged()),
                                        new NURBSSurface());
                                //floor = sceneManager.buildSmoothTerrain(
                                //        "floor2", smoothFactor, 
                                //        new Noise(lod, roughness, sceneSize, new Ridged()), 
                                //        new NURBSSurface());
                            }
                            break;
                        case Off:
                            if (!frameworkOptions.get("showTextures")) {
                                floor = this.sceneManager.buildTerrain(
                                        null,
                                        new Noise(this.lod, this.roughness, this.sceneSize,
                                                new Ridged()));
                            } else {
                                floor = this.sceneManager.buildTerrain(
                                        "floor1",
                                        new Noise(this.lod, this.roughness, this.sceneSize,
                                                new Ridged()));
                                //floor = sceneManager.buildTerrain(
                                //        "floor2", 
                                //        new Noise(lod, roughness, sceneSize, new Ridged()));
                            }
                            break;
                        default:
                            throw new AssertionError(this.smoother.name());
                    }
                    Iterator<Node> childIterator = floor.childIterator();
                    tg = ((AbstractTerrainGenerator) childIterator.next());
                    floor.setScaling(new Vector3D(1.1f, 1.1f, 1.1f));
                    floor.updateTranslation(new Vector3D(
                            -this.sceneSize / 2 - 1,
                            0.0,//-floor.getTranslation().getY(),
                            -this.sceneSize / 2 - 1));
                    this.sceneManager.setFloor(floor);
                    newTerrainGenerated = true;
                }

                if (frameworkOptions.get("useMazeGenerators")) {
                    floor = this.sceneManager.buildMaze(
                            new Prim(this.mazeLength, this.mazeWidth, this.sceneSize - this.sceneSize / 10));
                    Iterator<Node> childIterator = floor.childIterator();
                    mg = ((AbstractMazeGenerator) childIterator.next());
                    this.sceneManager.setFloor(floor);
                }
            }

            if (this.keyBoard.isDetecting("7")) {
                TransformGroup floor;
                if (frameworkOptions.get("useTerrainGenerators")) {
                    switch (this.smoother) {
                        case Default:
                            if (!frameworkOptions.get("showTextures")) {
                                floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize,
                                                new Simplex()),
                                        new Surface());
                            } else {
                                floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize,
                                                new Simplex()),
                                        new Surface());
                                //floor = sceneManager.buildSmoothTerrain(
                                //        "floor2", smoothFactor,
                                //        new Noise(lod, roughness, sceneSize, new Simplex()),
                                //        new Surface());
                            }
                            break;
                        case NURBS:
                            if (!frameworkOptions.get("showTextures")) {
                                floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize,
                                                new Simplex()),
                                        new NURBSSurface());
                            } else {
                                floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                        new Noise(this.lod, this.roughness, this.sceneSize,
                                                new Simplex()),
                                        new NURBSSurface());
                                //floor = sceneManager.buildSmoothTerrain(
                                //        "floor2", smoothFactor, 
                                //        new Noise(lod, roughness, sceneSize, new Simplex()), 
                                //        new NURBSSurface());
                            }
                            break;
                        case Off:
                            if (!frameworkOptions.get("showTextures")) {
                                floor = this.sceneManager.buildTerrain(
                                        null,
                                        new Noise(this.lod, this.roughness, this.sceneSize,
                                                new Simplex()));
                            } else {
                                floor = this.sceneManager.buildTerrain(
                                        "floor1",
                                        new Noise(this.lod, this.roughness, this.sceneSize,
                                                new Simplex()));
                                //floor = sceneManager.buildTerrain(
                                //        "floor2", 
                                //        new Noise(lod, roughness, sceneSize, new Simplex()));
                            }
                            break;
                        default:
                            throw new AssertionError(this.smoother.name());
                    }
                    Iterator<Node> childIterator = floor.childIterator();
                    tg = ((AbstractTerrainGenerator) childIterator.next());
                    floor.setScaling(new Vector3D(1.1f, 1.1f, 1.1f));
                    floor.updateTranslation(new Vector3D(
                            -this.sceneSize / 2 - 1,
                            0.0,//-floor.getTranslation().getY(),
                            -this.sceneSize / 2 - 1));
                    this.sceneManager.setFloor(floor);
                    newTerrainGenerated = true;
                }

                if (frameworkOptions.get("useMazeGenerators")) {
                    floor = this.sceneManager.buildMaze(
                            new Debug(this.mazeLength, this.mazeWidth, this.sceneSize - this.sceneSize / 10));
                    Iterator<Node> childIterator = floor.childIterator();
                    mg = ((AbstractMazeGenerator) childIterator.next());
                    this.sceneManager.setFloor(floor);
                }
            }

            if (this.keyBoard.isDetecting("8")
                    && frameworkOptions.get("useTerrainGenerators")) {
                TransformGroup floor;
                switch (this.smoother) {
                    case Default:
                        if (!frameworkOptions.get("showTextures")) {
                            floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                    new Noise(this.lod, this.roughness, this.sceneSize,
                                            new Turbulence()),
                                    new Surface());
                        } else {
                            floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                    new Noise(this.lod, this.roughness, this.sceneSize,
                                            new Turbulence()),
                                    new Surface());
                            //floor = sceneManager.buildSmoothTerrain(
                            //        "floor2", smoothFactor,
                            //        new Noise(lod, roughness, sceneSize, new Turbulence()),
                            //        new Surface());
                        }
                        break;
                    case NURBS:
                        if (!frameworkOptions.get("showTextures")) {
                            floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                    new Noise(this.lod, this.roughness, this.sceneSize,
                                            new Turbulence()),
                                    new NURBSSurface());
                        } else {
                            floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                    new Noise(this.lod, this.roughness, this.sceneSize,
                                            new Turbulence()),
                                    new NURBSSurface());
                            //floor = sceneManager.buildSmoothTerrain(
                            //        "floor2", smoothFactor, 
                            //        new Noise(lod, roughness, sceneSize, new Turbulence()), 
                            //        new NURBSSurface());
                        }
                        break;
                    case Off:
                        if (!frameworkOptions.get("showTextures")) {
                            floor = this.sceneManager.buildTerrain(
                                    null,
                                    new Noise(this.lod, this.roughness, this.sceneSize,
                                            new Turbulence()));
                        } else {
                            floor = this.sceneManager.buildTerrain(
                                    "floor1",
                                    new Noise(this.lod, this.roughness, this.sceneSize,
                                            new Turbulence()));
                            //floor = sceneManager.buildTerrain(
                            //        "floor2", 
                            //        new Noise(lod, roughness, sceneSize, new Turbulence()));
                        }
                        break;
                    default:
                        throw new AssertionError(this.smoother.name());
                }
                Iterator<Node> childIterator = floor.childIterator();
                tg = ((AbstractTerrainGenerator) childIterator.next());
                floor.setScaling(new Vector3D(1.1f, 1.1f, 1.1f));
                floor.updateTranslation(new Vector3D(
                        -this.sceneSize / 2 - 1,
                        0.0,//-floor.getTranslation().getY(),
                        -this.sceneSize / 2 - 1));
                this.sceneManager.setFloor(floor);
                newTerrainGenerated = true;
            }

            if (this.keyBoard.isDetecting("9")
                    && frameworkOptions.get("useTerrainGenerators")) {
                TransformGroup floor;
                switch (this.smoother) {
                    case Default:
                        if (!frameworkOptions.get("showTextures")) {
                            floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                    new Noise(this.lod, this.roughness, this.sceneSize, new Wood()),
                                    new Surface());
                        } else {
                            floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                    new Noise(this.lod, this.roughness, this.sceneSize, new Wood()),
                                    new Surface());
                            //floor = sceneManager.buildSmoothTerrain(
                            //        "floor2", smoothFactor,
                            //        new Noise(lod, roughness, sceneSize, new Wood()),
                            //        new Surface());
                        }
                        break;
                    case NURBS:
                        if (!frameworkOptions.get("showTextures")) {
                            floor = this.sceneManager.buildSmoothTerrain(null, this.smoothFactor,
                                    new Noise(this.lod, this.roughness, this.sceneSize, new Wood()),
                                    new NURBSSurface());
                        } else {
                            floor = this.sceneManager.buildSmoothTerrain("floor1", this.smoothFactor,
                                    new Noise(this.lod, this.roughness, this.sceneSize, new Wood()),
                                    new NURBSSurface());
                            //floor = sceneManager.buildSmoothTerrain(
                            //        "floor2", smoothFactor, 
                            //        new Noise(lod, roughness, sceneSize, new Wood()), 
                            //        new NURBSSurface());
                        }
                        break;
                    case Off:
                        if (!frameworkOptions.get("showTextures")) {
                            floor = this.sceneManager.buildTerrain(
                                    null,
                                    new Noise(this.lod, this.roughness, this.sceneSize,
                                            new Wood()));
                        } else {
                            floor = this.sceneManager.buildTerrain(
                                    "floor1",
                                    new Noise(this.lod, this.roughness, this.sceneSize,
                                            new Wood()));
                            //floor = sceneManager.buildTerrain(
                            //        "floor2", 
                            //        new Noise(lod, roughness, sceneSize, new Wood()));
                        }
                        break;
                    default:
                        throw new AssertionError(this.smoother.name());
                }
                Iterator<Node> childIterator = floor.childIterator();
                tg = ((AbstractTerrainGenerator) childIterator.next());
                floor.setScaling(new Vector3D(1.1f, 1.1f, 1.1f));
                floor.updateTranslation(new Vector3D(
                        -this.sceneSize / 2 - 1,
                        0.0,//-floor.getTranslation().getY(),
                        -this.sceneSize / 2 - 1));
                this.sceneManager.setFloor(floor);
                newTerrainGenerated = true;
            }

            if (frameworkOptions.get("useTerrainGenerators")
                    && newTerrainGenerated) {
                //TODO: fix to less bumpy                       
                Vector3D newPosition = ZERO;
                if (abs(oldPosition.getX()) < (this.sceneSize / 2)
                        && abs(oldPosition.getZ()) < (this.sceneSize / 2)) {
                    float height = tg.getMeanAltitude(
                            (float) (((oldPosition.getX() + (this.sceneSize / 2)) / this.sceneSize) * tg.getGeometryLength()),
                            (float) (((oldPosition.getZ() + (this.sceneSize / 2)) / this.sceneSize) * tg.getGeometryWidth()));
                    newPosition = newPosition.add(new Vector3D(0.0d,
                            height - oldHeight,
                            0.0d));
                }
                this.sceneManager.getPlayer(this.playerId).updatePosition(newPosition);
            }

            boolean wDown = this.keyBoard.isContinuouslyDetecting("W");
            boolean sDown = this.keyBoard.isContinuouslyDetecting("S");
            boolean aDown = this.keyBoard.isContinuouslyDetecting("A");
            boolean dDown = this.keyBoard.isContinuouslyDetecting("D");
            //log.info(format("%b %b %b %b", wDown, sDown, dDown, aDown));

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
        //log.info("Render.");
        switch (state) {
            case INITIALIZED:
                log.info("Initialized ...");
                log.info("Loading ...");
                this.gameLoadContent();
                log.info("Loaded ...\n");
                //TODO: (SceneManagement Extras) loading menu.
                break;
            case PLAYING:
                this.sceneManager.render(gl);
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
     *
     * @param dt
     */
    @Override
    public void gameUpdate(float dt) {
        //synchronized (this) {
        if ("true".equals(getProperty("JOT.scenerotate"))) {
            if (this.sceneManager != null) {
                Vector3D cameraRotation = this.sceneManager.getCamera(this.playerId).getRotation();
                cameraRotation = new Vector3D(
                        cameraRotation.getX(),
                        cameraRotation.getY() + .3,
                        cameraRotation.getZ());
                this.sceneManager.getCamera(this.playerId).setRotation(cameraRotation);
                this.sceneManager.updateCamera(1);
            }
        } else {
            if (state == GAME_OVER) {
                return;
            }

            if (state == PLAYING) {
                //log.info("Update.");
                if (this.updateCount == UPDATE_RATE) {
                    this.updateCount = 0;
                }
                this.updateCount++;

                ///Physics Player Update
                Vector3D newMouseCursorPosition = this.mouse.getPositionShift();

                if (this.sceneManager != null
                        && this.sceneManager.getPlayer(this.playerId) != null) {
                    Vector3D rotation = this.sceneManager.getPlayer(this.playerId).getRotation();

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

                        String playerCrash = null; //sceneManager.checkPlayerPlayerCollision(temp);

                        //Vector3D futurePosition = new Vector3D(position.toArray()).add(newPosition);
                        //sceneManager.getPlayer(playerId).getRotation();
                        if (!this.sceneManager.checkSceneBoundsCollision(boundingVolume)
                                && !this.sceneManager.checkPlayerImmutableObjectCollision(temp)
                                && (playerCrash == null || this.playerId.equals(playerCrash))) {

                            //TODO: fix to less bumpy       
                            if (frameworkOptions.get("useTerrainGenerators")) {
                                Vector3D oldPosition = this.sceneManager.getPlayer(this.playerId).getPosition();
                                Vector3D futurePosition = oldPosition.add(newPosition);
                                if (abs(futurePosition.getX()) < (this.sceneSize / 2) - (this.sceneSize / 20)
                                        && abs(futurePosition.getZ()) < (this.sceneSize / 2) - (this.sceneSize / 20)) {
                                    float height = tg.getMeanAltitude(
                                            (float) (((futurePosition.getX() + (this.sceneSize / 2)) / this.sceneSize) * tg.getGeometryLength()),
                                            (float) (((futurePosition.getZ() + (this.sceneSize / 2)) / this.sceneSize) * tg.getGeometryWidth()));
                                    newPosition = newPosition.add(new Vector3D(
                                            0.0d, height - oldPosition.getY(), 0.0d));
                                }
                            }
                            this.sceneManager.getPlayer(this.playerId).updatePosition(newPosition);
                        }
                    }
                }
                this.sceneManager.updateCamera(1);
            }
            //}
        }
    }

    /**
     * The smoother to use (more may be available).
     */
    public enum Smoother {

        /**
         * Default smoother (Gaussian blur)
         */
        Default,
        /**
         * NURBS smoother
         */
        NURBS,
        /**
         * No smoother
         */
        Off
    }
}
