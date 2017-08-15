/*
 * This file is part of the PathFinder program. This is a simple program that
 * serves as a testbed for steering behaviors, pathFinding, and maze generation
 * algorithms. The program features a JogAmp-based graphical component, to
 * visualize the graph to traverse, the found (if one exists) path and the
 * traversed nodes.
 *
 * The program also includes a loader for Collada 1.4 models and HOG2
 * Pathfinding Benchmarks, available at <http://www.movingai.com/benchmarks/>.
 * Copyright (C) 2014 Gonçalo Amador & Abel Gomes
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * E-mail Contacts: G. Amador (g.n.p.amador@gmail.com) & 
 *                  A. Gomes (agomes@it.ubi.pt)
 */
package jot.demo.gui;

import static com.jogamp.newt.event.KeyEvent.VK_ESCAPE;
import com.jogamp.opengl.GL2;
import static java.lang.Math.random;
import static java.lang.Math.round;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import jot.io.data.format.HOG2Map.HOG2MapMesh;
import jot.manager.SceneManager;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.A_STAR;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.BEST_FIRST_SEARCH;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.DIJKSTRA;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.FRINGE_SEARCH;
import jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.ALIGNMENT;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.ARRIVE;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.COHESION;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.EVADE;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.FLEE;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.HIDE;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.INTERPOSE;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.OFFSET_PURSUIT;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.PATH_FOLLOW;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.PURSUIT;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.SEEK;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.SEPARATION;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.WANDER;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.WANDER_AREA;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.WANDER_CURVE;
import jot.manager.ai.PathFindersManager;
import jot.manager.ai.SteeringBehaviorsManager;
import jot.math.geometry.Node;
import jot.math.geometry.TransformGroup;
import jot.math.geometry.bounding.AbstractBoundingVolume;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.OBB;
import jot.math.geometry.bounding.BoundingSphere;
import jot.math.geometry.generators.maze.AbstractMazeGenerator;
import jot.math.geometry.generators.maze.Prim;
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

    private static final Logger log = getLogger("Steering Behaviors & PathFinders");

    //Number of Frames or Updates Per Second, i.e., FPS or UPS.
    private static final int UPDATE_RATE = 25;  //number of Frames or Updates Per Second, i.e., FPS or UPS.
    //private static final int CANVAS_WIDTH = 1_280;
    //private static final int CANVAS_HEIGHT = 900;
    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 600;

    private static HashMap<String, SteeringBehaviorsManager> sbms; //each A.I. agent has an steering behavior manager.
    private static PathFindersManager pfm;
    private static AbstractMazeGenerator mg;

    /**
     * The main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        setGameConstants();

        //Default Logger level is INFO
        log.setLevel(INFO);
        //log.setLevel(OFF);
        //log.setLevel(WARNING);

        Game game = null;
        while (game == null) {
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
    private int mazeWidth = 25;
    private int mazeLength = 25;
    private int bulletIdSequence = 1;
    //private boolean LeftShoot = false;
    private final float sceneSize = 100;
    private final int groupSize = 20; //at most we will have 20 players.
    private final Vector3D playerVelocity = new Vector3D(10.5F, 10.5F, 10.5F);   //the velocity for any moving avatar or bullet in the sceneManager.
    private final Vector3D agentsVelocity = new Vector3D(10.5F, 10.5F, 10.5F);   //the velocity for any moving avatar or bullet in the sceneManager.
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
    //private final float[] color = {1.0f, 1.0f, 1.0f, 1.0f};       //White.
    private final float[] color = {0.3f, 0.3f, 0.7f, 0.0f};      //Purple.
    //private final float[] color = {0.3f, 0.3f, 0.3f, 0.3f};  //Pitch black.

    private float direction;
    private boolean isMoving, isRotating;

    //AbstractBoundingVolume.BoundingVolumeType modelsBoundingBoxType = AABB;
    AbstractBoundingVolume.BoundingVolumeType modelsBoundingBoxType = OBB;

    /**
     * Constructor, receives a player unique id, mays set up (if a valid A.I. is
     * chosen) the A.I. to use, and configures a communication node with a
     * personalized configuration (if provided a valid path to a configuration
     * file).
     *
     * @param pId player unique Id.
     */
    public GameMain(String pId) {
        super("Pathfinders & Steering behaviors", CANVAS_WIDTH, CANVAS_HEIGHT);

        this.playerId = pId;    //the players Id, i.e., user name
        //LeftShoot = true;       //the first shoot that this players does should be from its left side

        //setting up A.I.
        log.info("Setting up A.I..");

        //activate the A.I. module for steering behaviors        
        if (frameworkOptions.get("useSteeringBehaviors")) {
            sbms = new HashMap<>();
            sbms.put(this.playerId, new SteeringBehaviorsManager(this.sceneSize));

            ArrayList<SteeringBehavior> steeringBehaviors = new ArrayList<>();
            //Steering behaviors to use.    
            //steeringBehaviors.add(WANDER); //FIXME: not working 
            //steeringBehaviors.add(WANDER_AREA); //FIXME: not working 
            //steeringBehaviors.add(WANDER_CURVE); //FIXME: not working  
            //OBSTACLE_AVOIDANCE, WALL_AVOIDANCE, //TODO: Need these? I think not! If true remove.
            steeringBehaviors.add(PATH_FOLLOW);
            //steeringBehaviors.add(OFFSET_PURSUIT); //FIXME: not working 
            //steeringBehaviors.add(SEPARATION); //FIXME: not working 
            //steeringBehaviors.add(ALIGNMENT); //FIXME: not working 
            //steeringBehaviors.add(COHESION); //FIXME: not working 
            this.setAgentSteeringBehaviors(this.playerId, steeringBehaviors);

            sbms.put("player2", new SteeringBehaviorsManager(this.sceneSize));
            steeringBehaviors = new ArrayList<>();
            //Steering behaviors to use.    
            //steeringBehaviors.add(SEEK);
            //steeringBehaviors.add(FLEE);
            //steeringBehaviors.add(ARRIVE);
            //steeringBehaviors.add(PURSUIT);
            //steeringBehaviors.add(EVADE);
            //steeringBehaviors.add(WANDER); //FIXME: not working 
            //steeringBehaviors.add(WANDER_AREA); //FIXME: not working 
            //steeringBehaviors.add(WANDER_CURVE); //FIXME: not working 
            //OBSTACLE_AVOIDANCE, WALL_AVOIDANCE, //TODO: Need these? I think not! If true remove.
            //steeringBehaviors.add(INTERPOSE);
            //steeringBehaviors.add(HIDE);
            //steeringBehaviors.add(PATH_FOLLOW);
            //steeringBehaviors.add(OFFSET_PURSUIT); //FIXME: not working 
            //steeringBehaviors.add(SEPARATION); //FIXME: not working 
            //steeringBehaviors.add(ALIGNMENT); //FIXME: not working 
            //steeringBehaviors.add(COHESION); //FIXME: not working 
            this.setAgentSteeringBehaviors("player2", steeringBehaviors);

            sbms.put("player3", new SteeringBehaviorsManager(this.sceneSize));
            steeringBehaviors = new ArrayList<>();
            //Steering behaviors to use.    
            //steeringBehaviors.add(SEEK);
            //steeringBehaviors.add(FLEE);
            //steeringBehaviors.add(ARRIVE);
            //steeringBehaviors.add(PURSUIT);
            //steeringBehaviors.add(EVADE);
            //steeringBehaviors.add(WANDER); //FIXME: not working 
            //steeringBehaviors.add(WANDER_AREA); //FIXME: not working 
            //steeringBehaviors.add(WANDER_CURVE); //FIXME: not working 
            //OBSTACLE_AVOIDANCE, WALL_AVOIDANCE, //TODO: Need these? I think not! If true remove.
            //steeringBehaviors.add(HIDE);
            //steeringBehaviors.add(PATH_FOLLOW);
            //steeringBehaviors.add(OFFSET_PURSUIT); //FIXME: not working 
            //steeringBehaviors.add(SEPARATION); //FIXME: not working 
            //steeringBehaviors.add(ALIGNMENT); //FIXME: not working 
            //steeringBehaviors.add(COHESION); //FIXME: not working 
            this.setAgentSteeringBehaviors("player3", steeringBehaviors);

            for (int i = 3; i < this.groupSize; i++) {
                sbms.put("player" + (i + 1), new SteeringBehaviorsManager(this.sceneSize));
                steeringBehaviors = new ArrayList<>();
                //Steering behaviors to use.    
                //steeringBehaviors.add(WANDER); //FIXME: not working 
                //steeringBehaviors.add(WANDER_AREA); //FIXME: not working 
                //steeringBehaviors.add(WANDER_CURVE); //FIXME: not working 
                //OBSTACLE_AVOIDANCE, WALL_AVOIDANCE, //TODO: Need these? I think not! If true remove.
                //steeringBehaviors.add(OFFSET_PURSUIT); //FIXME: not working 
                //steeringBehaviors.add(SEPARATION); //FIXME: not working 
                //steeringBehaviors.add(ALIGNMENT); //FIXME: not working 
                //steeringBehaviors.add(COHESION); //FIXME: not working 
                this.setAgentSteeringBehaviors("player" + (i + 1), steeringBehaviors);
            }
        }

        //activate the A.I. module for pathfinders
        if (frameworkOptions.get("usePathFinders")) {
            sbms = new HashMap<>();
            sbms.put(this.playerId, new SteeringBehaviorsManager(this.sceneSize));

            //set steering behavior to use
            sbms.get(this.playerId).SteeringBehaviorOn(PATH_FOLLOW);

            pfm = new PathFindersManager();
            //pfm = new PathFindersManager(60);

            //set pathfinder to use (for the first time because I don't like 
            //warnigs or inspect and transform warnings)!
            pfm.setPathfinder2use(DIJKSTRA);
            pfm.setPathfinder2use(A_STAR);
            pfm.setPathfinder2use(BEST_FIRST_SEARCH);
            pfm.setPathfinder2use(FRINGE_SEARCH);

            //set pathfinder to use (for real this time ... because I don't like 
            //warnigs or inspect and transform warnings)!
            //pfm.setPathfinder2use(DIJKSTRA);
            pfm.setPathfinder2use(A_STAR);
            //pfm.setPathfinder2use(BEST_FIRST_SEARCH);
            //pfm.setPathfinder2use(FRINGE_SEARCH);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameInit() {
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
            this.keyBoard.registerInputEvent("9", '9');
            this.keyBoard.registerInputEvent("F", 'F');
            this.keyBoard.registerInputEvent("F", 'f');
            this.keyBoard.registerInputEvent("T", 'T');
            this.keyBoard.registerInputEvent("T", 't');
            this.keyBoard.registerInputEvent("W", 'W');
            this.keyBoard.registerInputEvent("W", 'w');
            this.keyBoard.registerInputEvent("A", 'A');
            this.keyBoard.registerInputEvent("A", 'a');
            this.keyBoard.registerInputEvent("S", 'S');
            this.keyBoard.registerInputEvent("S", 's');
            this.keyBoard.registerInputEvent("D", 'D');
            this.keyBoard.registerInputEvent("D", 'd');
            this.keyBoard.registerInputEvent("Z", 'Z');
            this.keyBoard.registerInputEvent("Z", 'z');
            this.keyBoard.registerInputEvent("X", 'X');
            this.keyBoard.registerInputEvent("X", 'x');
            this.keyBoard.registerInputEvent("C", 'C');
            this.keyBoard.registerInputEvent("C", 'c');
        }
        this.keyBoard.registerInputEvent("Quit", VK_ESCAPE);

        text.setTextLine("Options:");
        if (null == getProperty("JOT.scenerotate")) {
            if (frameworkOptions.get("useMouse")) {
                text.setTextLine("Mouse move - rotate Duke model.");
                text.setTextLine("Mouse left button - shoot.");
            }
            if (frameworkOptions.get("useKeyBoard")) {
                text.setTextLine("1 key - toggle on/off lights.");
                text.setTextLine("2 key - toggle on/off textures.");
                text.setTextLine("3 key - toggle on/off wireframe.");
                text.setTextLine("4 key - toggle on/off broad phase collision detection.");
                text.setTextLine("5 key - toggle on/off show floor.");
                text.setTextLine("6 key - toggle on/off show path.");
                text.setTextLine("7 key - toggle on/off show graph.");
                text.setTextLine("8 key - toggle on/off show visited.");
                text.setTextLine("Z/z key - use perspective view camera.");
                text.setTextLine("X/x key - use perspective view camera.");
                text.setTextLine("C/c key - use upper view camera.");
                text.setTextLine("W/w,A/a,S/s,D/d keys - to move player.");
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
        texturesPaths.put("duck", "assets/models/dae/duck/duckCM.jpg");
        texturesPaths.put("brick1", "assets/textures/materials/brick1.jpg");
        sceneManager = new SceneManager(texturesPaths);
        sceneManager.setSceneSize(this.sceneSize);

        //Setup cameras
        //NOTE: camera Ids must differ and may not be necessarly the this.playerId, e.g., camera1, camera2, camera3.
        float aspectRatio = (float) CANVAS_WIDTH / CANVAS_HEIGHT;
        if ("true".equals(getProperty("JOT.scenerotate"))) {
            sceneManager.addCamera(new ThirdPerson(60, aspectRatio, this.sceneSize, this.playerId));
            sceneManager.setCamera2Use(this.playerId);
        } else {
            sceneManager.addCamera(new FirstPerson(60, aspectRatio, this.sceneSize, "FIRST_PERSON"));
            sceneManager.addCamera(new ThirdPerson(60, aspectRatio, this.sceneSize, "THIRD_PERSON"));
            sceneManager.addCamera(new Perspective(60, aspectRatio, this.sceneSize, "PERSPECTIVE"));
            sceneManager.addCamera(new PerspectiveFollow(60, aspectRatio, this.sceneSize, "PERSPECTIVE_FOLLOW"));
            sceneManager.addCamera(new UpperView(60, aspectRatio, this.sceneSize, "UPPER_VIEW"));
            sceneManager.addCamera(new UpperViewFollow(60, aspectRatio, this.sceneSize, "UPPER_VIEW_FOLLOW"));

            if (!frameworkOptions.get("useSteeringBehaviors")
                    && !frameworkOptions.get("usePathFinders")) {
                //sceneManager.setCamera2Use("FIRST_PERSON");
                sceneManager.setCamera2Use("THIRD_PERSON");
            } else {
                //sceneManager.setCamera2Use("PERSPECTIVE");
                //sceneManager.setCamera2Use("PERSPECTIVE_FOLLOW");
                sceneManager.setCamera2Use("UPPER_VIEW");
                //sceneManager.setCamera2Use("UPPER_VIEW_FOLLOW");   
                //sceneManager.setCamera2Use("THIRD_PERSON");
            }
        }
        log.info("Cameras setup done.");

        //Setup skyBox
        if (!frameworkOptions.get("HOG2Maps")
                && !frameworkOptions.get("useMazeGenerators")
                && frameworkOptions.get("SkyBox")) {
            TransformGroup skyBox;
            skyBox = sceneManager.buildSkyBox(this.sceneSize,
                    "assets/textures/skybox/World_01/SkyBox2", "jpg");
            //skyBox.setScaling(new Vector3D(5, 5, 5));
            //skyBox.setRotation(new Vector3D(- 90, 0, 0));
            skyBox.setBoundingVolume(0, null);
            sceneManager.setSky(skyBox);
            log.info("Sky Box added to the scene.");
        }

        //Setup skyDome
        if (!frameworkOptions.get("HOG2Maps")
                && !frameworkOptions.get("useMazeGenerators")
                && frameworkOptions.get("SkyDome")) {
            TransformGroup skyDome;
            skyDome = sceneManager.buildSkyDome(
                    this.sceneSize / 2,
                    "assets/textures/skydome/sky_povray1.jpg");
            //skyDome.setScaling(new Vector3D(5, 5, 5));
            //skyDome.setRotation(new Vector3D(- 90, 0, 0));
            skyDome.setBoundingVolume(0, null);
            sceneManager.setSky(skyDome);
            log.info("Sky Dome added to the scene.");
        }

        //Setup floor
        if (frameworkOptions.get("Floor") && !frameworkOptions.get("HOG2Maps")
                && !frameworkOptions.get("useMazeGenerators")) {
            TransformGroup floor = sceneManager.buildFloor(this.sceneSize,
                    "assets/textures/skybox/World_01/SkyBox2_down.jpg");

            //floor.setRotation(new Vector3D(-90, 0, 0));
            floor.updateTranslation(new Vector3D(0, this.sceneSize / 2, 0));

            sceneManager.setFloor(floor);
            log.info("Floor created.");
        }

        //Setup obstacles 
        //TODO: load from file or script
        if (!frameworkOptions.get("HOG2Maps")
                && !frameworkOptions.get("useMazeGenerators")) {
            if (frameworkOptions.get("useObstaclesConfig1")) {
                int boxCount = 6;
                for (int i = 0; i < boxCount; i++) {
                    float rotation = i * (360 / boxCount);
                    //SPHERE or OBB or AABB
                    GameObject obstacle = sceneManager.buildBlock("obstacle"
                            + (i + 1),
                            "brick1", 1.0F, this.modelsBoundingBoxType);
                    Vector3D obstaclePostion = translatePolar(ZERO, 15.0F, rotation, 0, 1);
                    obstaclePostion = new Vector3D(
                            obstaclePostion.getX(),
                            obstaclePostion.getY() + 1,
                            obstaclePostion.getZ());
                    obstacle.setPosition(obstaclePostion);
                    obstacle.getBoundingVolume(0).setRenderable(true);
                    sceneManager.addImmutableObject(obstacle);
                    log.info(format("Obstacle "
                            + (i + 1)
                            + " added to the scene."));
                }
            }

            if (frameworkOptions.get("useObstaclesConfig2")) {
                //TOP GATES
                int boxCount = 60;
                for (int z = -50; z < -20; z++) {
                    //SPHERE or OBB or AABB
                    GameObject obstacle = sceneManager.buildBlock("obstacle"
                            + (z + boxCount),
                            "brick1", 1.0F, this.modelsBoundingBoxType);
                    Vector3D obstaclePostion = translatePolar(ZERO, z + 3, 0, -90, 1);
                    obstaclePostion = new Vector3D(
                            obstaclePostion.getX(),
                            obstaclePostion.getY() + 1,
                            obstaclePostion.getZ());
                    obstacle.setPosition(obstaclePostion);
                    sceneManager.addImmutableObject(obstacle);
                    log.info(format("Obstacle "
                            + (z + boxCount)
                            + " added to the scene."));
                }

                for (int z = 5; z < 30; z++) {
                    //SPHERE or OBB or AABB
                    GameObject obstacle = sceneManager.buildBlock("obstacle"
                            + (z + boxCount),
                            "brick1", 1.0F, this.modelsBoundingBoxType);
                    Vector3D obstaclePostion = translatePolar(ZERO, z, 0, -90, 1);
                    obstaclePostion = new Vector3D(
                            obstaclePostion.getX(),
                            obstaclePostion.getY() + 1,
                            obstaclePostion.getZ());
                    obstacle.setPosition(obstaclePostion);
                    sceneManager.addImmutableObject(obstacle);
                    log.info(format("Obstacle "
                            + (z + boxCount)
                            + " added to the scene."));
                }

                //BARRIERS
                for (int i = -50; i < 50; i++) {
                    //SPHERE or OBB or AABB
                    GameObject obstacle = sceneManager.buildBlock("obstacle"
                            + (i + 150 + boxCount),
                            "brick1", 1.0F, this.modelsBoundingBoxType);
                    Vector3D obstaclePostion = translatePolar(ZERO, 47, i, i, 1);
                    obstaclePostion = new Vector3D(
                            obstaclePostion.getX(),
                            obstaclePostion.getY() + 1,
                            obstaclePostion.getZ());
                    obstacle.setPosition(obstaclePostion);
                    sceneManager.addImmutableObject(obstacle);
                    log.info(format("Obstacle "
                            + (i + 150 + boxCount)
                            + " added to the scene."));
                }

                for (int i = -50; i < 50; i++) {
                    //SPHERE or OBB or AABB
                    GameObject obstacle = sceneManager.buildBlock("obstacle"
                            + (i + 250 + boxCount),
                            "brick1", 1.0F, this.modelsBoundingBoxType);
                    Vector3D obstaclePostion = translatePolar(ZERO, -47, -i, -i, 1);
                    obstaclePostion = new Vector3D(
                            obstaclePostion.getX(),
                            obstaclePostion.getY() + 1,
                            obstaclePostion.getZ());
                    obstacle.setPosition(obstaclePostion);
                    sceneManager.addImmutableObject(obstacle);
                    log.info(format("Obstacle "
                            + (i + 250 + boxCount)
                            + " added to the scene."));
                }

                //INTERNAL OBSTACLES
                for (int z = 0; z < 100; z++) {
                    int x = round((float) random() * 47);
                    int y = round((float) random() * 47);
                    int val1 = round((float) random());
                    int val2 = round((float) random());
                    if (val1 != 1) {
                        x *= -1;
                    }
                    if (val2 != 1) {
                        y *= -1;
                    }

                    if ((x > 10 || x < -10) && (y > 10 || y < -10)) {
                        //SPHERE or OBB or AABB
                        GameObject obstacle = sceneManager.buildBlock("obstacle"
                                + (z + 450),
                                "brick1", 1.0F, this.modelsBoundingBoxType);
                        Vector3D obstaclePostion = translatePolar(ZERO, x, 0, y, 1);
                        obstaclePostion = new Vector3D(
                                obstaclePostion.getX(),
                                obstaclePostion.getY() + 1,
                                obstaclePostion.getZ());
                        obstacle.setPosition(obstaclePostion);
                        sceneManager.addImmutableObject(obstacle);
                        log.info(format("Obstacle "
                                + (z + 450)
                                + " added to the scene."));
                    } else {
                        z--;
                    }
                }
            }

            if (frameworkOptions.get("useObstaclesConfig3")) {
                float dist1 = 15.0F;
                float dist2 = 28.0F;
                float dist3 = 35.0F;
                float dist4 = 22.0F;
                float dist5 = 8.0F;
                int boxCount1 = 15;
                int boxCount2 = 40;
                int boxCount3 = 45;
                int boxCount4 = 27;
                int boxCount5 = 9;
                for (int i = 0; i < boxCount1; i++) {
                    float rotation = i * (360 / boxCount1);
                    //SPHERE or OBB or AABB
                    GameObject obstacle = sceneManager.buildBlock("obstacle"
                            + (i + 1),
                            "brick1", 1.0F, this.modelsBoundingBoxType);
                    Vector3D obstaclePostion = translatePolar(ZERO, dist1, rotation, 0, 1);
                    obstaclePostion = new Vector3D(
                            obstaclePostion.getX(),
                            obstaclePostion.getY() + 1,
                            obstaclePostion.getZ());
                    obstacle.setPosition(obstaclePostion);
                    //obstacle.getBoundingVolume().setRenderable(true);
                    sceneManager.addImmutableObject(obstacle);
                    log.info(format("Obstacle "
                            + (i + 1)
                            + " added"));
                }

                for (int z = 0; z < boxCount2; z++) {
                    float rotation = z * (360 / boxCount2);
                    //SPHERE or OBB or AABB
                    GameObject obstacle = sceneManager.buildBlock("obstacle"
                            + (z + 1 + boxCount1),
                            "brick1", 1.0F, this.modelsBoundingBoxType);
                    Vector3D obstaclePostion = translatePolar(ZERO, dist2, rotation, 0, 1);
                    obstaclePostion = new Vector3D(
                            obstaclePostion.getX(),
                            obstaclePostion.getY() + 1,
                            obstaclePostion.getZ());
                    obstacle.setPosition(obstaclePostion);
                    //obstacle.getBoundingVolume().setRenderable(true);
                    sceneManager.addImmutableObject(obstacle);
                    log.info(format("Obstacle "
                            + (z + 1 + boxCount1)
                            + " added"));
                }

                for (int z = 0; z < boxCount3; z++) {
                    float rotation = z * (360 / boxCount3);
                    //SPHERE or OBB or AABB
                    GameObject obstacle = sceneManager.buildBlock("obstacle"
                            + (z + 1 + boxCount1 + boxCount2),
                            "brick1", 1.0F, this.modelsBoundingBoxType);
                    Vector3D obstaclePostion = translatePolar(ZERO, dist3, rotation, 0, 1);
                    obstaclePostion = new Vector3D(
                            obstaclePostion.getX(),
                            obstaclePostion.getY() + 1,
                            obstaclePostion.getZ());
                    obstacle.setPosition(obstaclePostion);
                    //obstacle.getBoundingVolume().setRenderable(true);
                    sceneManager.addImmutableObject(obstacle);
                    log.info(format("Obstacle "
                            + (z + 1 + boxCount1 + boxCount2)
                            + " added"));
                }

                for (int z = 0; z < boxCount4; z++) {
                    float rotation = z * (360 / boxCount4);
                    //SPHERE or OBB or AABB
                    GameObject obstacle = sceneManager.buildBlock("obstacle"
                            + (z + 1 + boxCount1 + boxCount2 + boxCount3),
                            "brick1", 1.0F, this.modelsBoundingBoxType);
                    Vector3D obstaclePostion = translatePolar(ZERO, dist4, rotation, 0, 1);
                    obstaclePostion = new Vector3D(
                            obstaclePostion.getX(),
                            obstaclePostion.getY() + 1,
                            obstaclePostion.getZ());
                    obstacle.setPosition(obstaclePostion);
                    //obstacle.getBoundingVolume().setRenderable(true);
                    sceneManager.addImmutableObject(obstacle);
                    log.info(format("Obstacle "
                            + (z + 1 + boxCount1 + boxCount2 + boxCount3)
                            + " added"));
                }

                for (int z = 0; z < boxCount5; z++) {
                    float rotation = z * (360 / boxCount5);
                    //SPHERE or OBB or AABB
                    GameObject obstacle = sceneManager.buildBlock("obstacle"
                            + (z + 1 + boxCount1 + boxCount2 + boxCount3 + boxCount4),
                            "brick1", 1.0F, this.modelsBoundingBoxType);
                    Vector3D obstaclePostion = translatePolar(ZERO, dist5, rotation, 0, 1);
                    obstaclePostion = new Vector3D(
                            obstaclePostion.getX(),
                            obstaclePostion.getY() + 1,
                            obstaclePostion.getZ());
                    obstacle.setPosition(obstaclePostion);
                    //obstacle.getBoundingVolume().setRenderable(true);
                    sceneManager.addImmutableObject(obstacle);
                    log.info(format("Obstacle "
                            + (z + 1 + boxCount1 + boxCount2 + boxCount3 + boxCount4)
                            + " added"));
                }
            }
        }

        //Setup models        
        if (!frameworkOptions.get("HOG2Maps")
                && !frameworkOptions.get("useMazeGenerators")) {
            this.models.put("cow", assetManager.loadFormat("assets/models/dae/cow/", "cow.dae", 1, this.modelsBoundingBoxType));
            //models.get("cow").setRotationY(-180);
            //models.get("cow").setRotationZ(90);
            //models.get("cow").setRotationX(90);
            this.models.get("cow").setRotationZ(90.0f);
            this.models.get("cow").setRotationX(-90.0f);
        }
        this.models.put("duke", assetManager.loadFormat("assets/models/dae/", "Duke_posed.dae", 1, this.modelsBoundingBoxType));
        this.models.get("duke").setRotationY(180);
        this.models.put("duck", assetManager.loadFormat("assets/models/dae/duck/", "duck_triangulate.dae", 250, this.modelsBoundingBoxType));
        this.models.get("duck").setRotationY(-90);
        this.models.put("bullet", assetManager.loadFormat("assets/models/dae/", "CoffeeBean2_out.dae", 1, this.modelsBoundingBoxType));

        try {
            //create a flock of ducks to simulate flocking or offset pursuit.
            if (frameworkOptions.get("useSteeringBehaviorsGroups")) {
                for (int i = 0; i < this.groupSize; i++) {
                    TransformGroup playeriRotate = this.models.get("duck").clone();
                    GameObject playeri = new GameObject("player" + (i + 1), this.models.get("duck"));
                    //TransformGroup playeriRotate = models.get("cow").clone();
                    //GameObject playeri = new GameObject("player" + (i + 1), models.get("cow"));
                    playeri.getBoundingVolume(0).setRenderable(true);
                    //playeriRotate.setBoundingVolume(0, null);
                    playeri.addChild(playeriRotate);
                    playeri.setAttribute("health", 100);
                    playeri.setVelocity(this.agentsVelocity);
                    playeri.setMaxSpeed((float) this.agentsVelocity.getNorm());
                    playeri.setBoundingRadius(playeri.getBoundingRadius() * 10);
                    if (i < 1) {
                        //playeri.setBoundingRadius(playeri.getBoundingRadius() * 10);
                        sbms.put("player1", sbms.get(this.playerId));
                        sbms.remove(this.playerId);
                        this.playerId = "player1";
                        sceneManager.setLocalPlayerId(this.playerId);
                    }
                    sceneManager.addPlayer(playeri);

                    float area = ((this.sceneSize - this.sceneSize / 10)
                            - (this.sceneSize - this.sceneSize / 10) / 2);
                    sceneManager.getPlayer("player" + (i + 1)).updatePosition(
                            new Vector3D(
                                    round((float) (random() * area * 2 - area)),
                                    playeri.getPosition().getY(),
                                    round((float) (random() * area * 2 - area))));
                    sceneManager.getPlayer("player" + (i + 1)).updateRotationY(45);
                    log.info(format("player" + (i + 1) + " added to the scene."));
                }
            } else {
                TransformGroup playerRotate = this.models.get("duke").clone();
                GameObject player = new GameObject(this.playerId, this.models.get("duke"));
                if (!frameworkOptions.get("useSteeringBehaviors")
                        && !frameworkOptions.get("usePathFinders")) {
                    player.getBoundingVolume(0).setRenderable(true);
                }
                //playerRotate.setBoundingVolume(0, null);
                player.addChild(playerRotate);
                player.setAttribute("health", 100);
                player.setVelocity(ZERO);
                //player.setVelocity(playerVelocity);
                player.setMaxSpeed((float) this.playerVelocity.getNorm());
                //TODO: alter when implemented for a compound mesh.
                player.getMeshes().get(0).getMaterial(0).setRenderable(false);
                sceneManager.addPlayer(player);
                sceneManager.setLocalPlayerId(this.playerId);
                //sceneManager.getPlayer(this.playerId).updatePosition(new Vector3D(2, 0, 2));
                log.info(format(this.playerId + " added to the scene."));

                if (null == getProperty("JOT.scenerotate")
                        && !frameworkOptions.get("HOG2Maps")
                        && !frameworkOptions.get("useMazeGenerators")
                        && !frameworkOptions.get("usePathFinders")
                        && sbms.get("player2").isSteeringBehaviorOn()) {
                    TransformGroup player2Rotate = this.models.get("cow").clone();
                    GameObject player2 = new GameObject("player2", this.models.get("cow"));
                    player2.getBoundingVolume(0).setRenderable(true);
                    //player2Rotate.setBoundingVolume(0, null);
                    player2.addChild(player2Rotate);
                    player2.setAttribute("health", 100);
                    player2.setVelocity(this.agentsVelocity);
                    player2.setMaxSpeed((float) this.agentsVelocity.getNorm());
                    sceneManager.addPlayer(player2);
                    //sceneManager.getPlayer("player2").updatePosition(new Vector3D(2, 0, 2));
                    sceneManager.getPlayer("player2").updatePosition(new Vector3D(25, 0, 12));
                    log.info("player2 added to the scene.");

                    if (sbms.get("player2").isSteeringBehaviorOn(INTERPOSE)) {
                        TransformGroup player3Rotate = this.models.get("duck").clone();
                        GameObject player3 = new GameObject("player3", this.models.get("duck"));
                        player3.getBoundingVolume(0).setRenderable(true);
                        //player3Rotate.setBoundingVolume(0, null);
                        player3.addChild(player3Rotate);
                        player3.setAttribute("health", 100);
                        player3.setVelocity(this.agentsVelocity);
                        player3.setMaxSpeed((float) this.agentsVelocity.getNorm());
                        sceneManager.addPlayer(player3);
                        //sceneManager.getPlayer("player3").updatePosition(new Vector3D(-2, 0, -2));
                        sceneManager.getPlayer("player3").updatePosition(new Vector3D(20, 0, -2));
                        //sceneManager.getPlayer("player3").updateRotationY(45);
                        log.info("player3 added to the scene.");
                    }
                }
            }
        } catch (CloneNotSupportedException ex) {
            log.log(SEVERE, null, ex);
        }

        if (null == getProperty("JOT.scenerotate")) {
            //Setup HOG2 map or Maze floor and use A.I. (if A.I. activated).
            log.info("Parallel thread running to setup HOG2 map or Maze floor and use A.I. (if A.I. activated).");
            Thread thread = new Thread("Setup HOG2 map or Maze floor and use A.I. (if A.I. activated).") {
                @Override
                public void run() {
                    boolean old_useAI = frameworkOptions.get("useAI");
                    if (frameworkOptions.get("useAI")) {
                        frameworkOptions.put("useAI", !frameworkOptions.get("useAI"));
                    }

                    //Setup floor        
                    TransformGroup floor = null;
                    float scale = 1;
                    if (frameworkOptions.get("HOG2Maps")) {
                        log.info("Loading map file.");
                        String map = "";
                        if (frameworkOptions.get("_21x21_withObstacles")) {
                            scale = 0.25F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/", "21x21_withObstacles.map", scale, OBB);
                            map = "_21x21_withObstacles";
                        } else if (frameworkOptions.get("_31x31")) {
                            scale = 0.33F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/", "31x31.map", scale, OBB);
                            //floor = assetManager.loadFormat(
                            //        "assets/HOG2/", "31x31squares.map", scale, OBB);
                            map = "_31x31";
                        } //A.I. Programming Game A.I. by example book maps.  
                        else if (frameworkOptions.get("_52x52")) {
                            scale = 0.5F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/", "52x52.map", scale, OBB);
                            //floor = assetManager.loadFormat(
                            //        "assets/HOG2/", "52x52squares.map", scale, OBB);
                            map = "_52x52";
                        } //A.I. Programming Game A.I. by example book maps.  
                        else if (frameworkOptions.get("AIbook1")) {
                            scale = 0.25F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/", "AIbook1.map", scale, OBB);
                            map = "AIbook1";
                        } else if (frameworkOptions.get("AIbook2")) {
                            scale = 0.25F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/", "AIbook2.map", scale, OBB);
                            map = "AIbook2";
                        } //Dragon Age Origins maps 
                        else if (frameworkOptions.get("useArena2")) {
                            scale = 2.2F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/DragonAgeOrigins/map.zip", "arena2.map", scale, OBB);
                            map = "arena2";
                        } else if (frameworkOptions.get("useBrc202d")) {
                            scale = 4.5F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/DragonAgeOrigins/map.zip", "brc202d.map", scale, OBB);
                            map = "brc202d";
                        } else if (frameworkOptions.get("useBrc203d")) {
                            scale = 3.5F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/DragonAgeOrigins/map.zip", "brc203d.map", scale, OBB);
                            map = "brc203d";
                        } else if (frameworkOptions.get("useBrc204d")) {
                            scale = 3.8F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/DragonAgeOrigins/map.zip", "brc204d.map", scale, OBB);
                            map = "brc204d";
                        } else if (frameworkOptions.get("useDen011d")) {
                            scale = 1.6F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/DragonAgeOrigins/map.zip", "den011d.map", scale, OBB);
                            map = "den011d";
                        } else if (frameworkOptions.get("useDen500d")) {
                            scale = 3.8F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/DragonAgeOrigins/map.zip", "den500d.map", scale, OBB);
                            map = "den500d";
                        } else if (frameworkOptions.get("useDen501d")) {
                            scale = 3.0F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/DragonAgeOrigins/map.zip", "den501d.map", scale, OBB);
                            map = "den501d";
                        } else if (frameworkOptions.get("useDen602d")) {
                            scale = 4.5F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/DragonAgeOrigins/map.zip", "den602d.map", scale, OBB);
                            map = "den602d";
                        } else if (frameworkOptions.get("useHrt201n")) {
                            scale = 2.8F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/DragonAgeOrigins/map.zip", "hrt201n.map", scale, OBB);
                            map = "hrt201n";
                        } else if (frameworkOptions.get("useLak304d")) {
                            scale = 1.8F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/DragonAgeOrigins/map.zip", "lak304d.map", scale, OBB);
                            map = "lak304d";
                        } //Warcraft III maps
                        else if (frameworkOptions.get("useBattleground")) {
                            scale = 4.8F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/WarcraftIII/map.zip", "battleground.map", scale, OBB);
                            map = "battleground";
                        } else if (frameworkOptions.get("useBlastedlands")) {
                            scale = 4.8F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/WarcraftIII/map.zip", "blastedlands.map", scale, OBB);
                            map = "blastedlands";
                        } else if (frameworkOptions.get("useDivideandconquer")) {
                            scale = 4.8F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/WarcraftIII/map.zip", "divideandconquer.map", scale, OBB);
                            map = "divideandconquer";
                        } else if (frameworkOptions.get("useDragonfire")) {
                            scale = 4.8F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/WarcraftIII/map.zip", "dragonfire.map", scale, OBB);
                            map = "dragonfire";
                        } else if (frameworkOptions.get("useFrostsabre")) {
                            scale = 4.8F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/WarcraftIII/map.zip", "frostsabre.map", scale, OBB);
                            map = "frostsabre";
                        } else if (frameworkOptions.get("useGardenofwar")) {
                            scale = 4.6F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/WarcraftIII/map.zip", "gardenofwar.map", scale, OBB);
                            map = "gardenofwar";
                        } else if (frameworkOptions.get("useHarvestmoon")) {
                            scale = 4.8F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/WarcraftIII/map.zip", "harvestmoon.map", scale, OBB);
                            map = "harvestmoon";
                        } else if (frameworkOptions.get("useIsleofdread")) {
                            scale = 4.8F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/WarcraftIII/map.zip", "isleofdread.map", scale, OBB);
                            map = "isleofdread";
                        } else if (frameworkOptions.get("useThecrucible")) {
                            scale = 4.6F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/WarcraftIII/map.zip", "thecrucible.map", scale, OBB);
                            map = "thecrucible";
                        } else if (frameworkOptions.get("useTranquilpaths")) {
                            scale = 4.6F;
                            floor = assetManager.loadFormat(
                                    "assets/HOG2/WarcraftIII/map.zip", "tranquilpaths.map", scale, OBB);
                            map = "tranquilpaths";
                        }

                        if (floor != null) {
                            //TODO: alter when implemented for a compound mesh.
                            Vector3D barycentre = ((HOG2MapMesh) floor.getMeshes().get(0)).getBarycentre();
                            barycentre = new Vector3D(barycentre.getX(), 100, barycentre.getZ());
                            //sceneManager.setCamera2Use("UPPER_VIEW");
                            sceneManager.getCamera("UPPER_VIEW").setViewPoint(new Vector3D(
                                    barycentre.getX(), 0, barycentre.getZ() + .1));
                            sceneManager.getCamera("UPPER_VIEW").setPosition(barycentre);
                            //sceneManager.setCamera2Use("PERSPECTIVE");
                            //sceneManager.getCamera("PERSPECTIVE").setCameraPosition(new Vector3D(
                            //        size * -.1, size * 0.08, size * -.1));

                            int w = ((HOG2MapMesh) floor.getMeshes().get(0)).getHOG2MapWidth();
                            int h = ((HOG2MapMesh) floor.getMeshes().get(0)).getHOG2MapHeight();
                            pfm.setupPathLog(map, w, h);
                        }
                    }
                    if (frameworkOptions.get("useMazeGenerators")) {
                        log.info("Setting up maze generation.");
                        if (frameworkOptions.get("Maze_50x50")) {
                            mazeLength = mazeWidth = 50;
                        }
                        if (frameworkOptions.get("Maze_100x100")) {
                            mazeLength = mazeWidth = 100;
                        }
                        if (frameworkOptions.get("Maze_250x250")) {
                            mazeLength = mazeWidth = 250;
                        }

                        floor = sceneManager.buildMaze(new Prim(
                                mazeLength, mazeWidth, sceneSize - sceneSize / 10));
                        Iterator<Node> childIterator = floor.childIterator();
                        mg = ((AbstractMazeGenerator) childIterator.next());
                        mg.setMazeColor(new Vector3D(.5, .5, .5));
                        pfm.setupPathLog("Maze", mazeWidth, mazeLength);
                    }

                    if (frameworkOptions.get("Floor") && floor != null) {
                        sceneManager.setFloor(floor);
                        log.info("Floor created.");
                    }

                    if (old_useAI) {
                        if (frameworkOptions.get("usePathFinders")) {
                            /**
                             * Generate graph for maze and update position of
                             * agent to manipulate with A.I.
                             */
                            if (frameworkOptions.get("useMazeGenerators")) {
                                pfm.setGraph(mg.getMaze());
                                //pfm.setGraphColor(new Vector3D(0.8, 0.2, 0.5));

                                sceneManager.getPlayer(playerId).updatePosition(mg.getMazeMax());
                            }

                            /**
                             * Generate graph for HOG2 map and update position
                             * of agent to manipulate with A.I.
                             */
                            if (frameworkOptions.get("HOG2Maps")
                                    && frameworkOptions.get("Floor")
                                    && null != floor) {
                                pfm.setGridCoordsGraphNodes(
                                        //TODO: alter when implemented for a compound mesh.
                                        ((HOG2MapMesh) floor.getMeshes().get(0))
                                                .getGridCoordinatesGraphNodes());
                                pfm.setGraphNodesGridCoords(
                                        //TODO: alter when implemented for a compound mesh.
                                        ((HOG2MapMesh) floor.getMeshes().get(0))
                                                .getGraphNodesGridCoordinates());
                                pfm.setGraph(
                                        //TODO: alter when implemented for a compound mesh.
                                        ((HOG2MapMesh) floor.getMeshes().get(0))
                                                .getGraph());
                                if (frameworkOptions.get("usePathFindersLowLevelGraph")) {
                                    //&& !frameworkOptions.get("_52x52")) {
                                    HashMap<Vector3D, ArrayList<Vector3D>> lowLevelGraph
                                            = ((HOG2MapMesh) floor.getMeshes().get(0))
                                                    .getGraphMST();
                                    pfm.setLowLevelGraph(lowLevelGraph);
                                }
                                pfm.setGraphColor(new Vector3D(0.8, 0.2, 0.5));

                                sceneManager.getPlayer(playerId).updatePosition(
                                        floor.getMeshes().get(0).getMaxVertex());
                            }

                            //Update position of agent to manipulate with A.I. 
                            if (!frameworkOptions.get("HOG2Maps")
                                    && !frameworkOptions.get("useMazeGenerators")) {
                                pfm.setGraphColor(new Vector3D(0.8, 0.2, 0.5));

                                sceneManager.getPlayer(playerId).updatePosition(
                                        new Vector3D(-44, 0, 16));
                            }

                            //change path color to red, default blue
                            sbms.get(playerId).setPathColor(new Vector3D(1.0f, 0.0f, 0.0f));
                            if (frameworkOptions.get("_52x52")) {
                                //change path color to gray, default blue
                                sbms.get(playerId).setPathColor(new Vector3D(0.3f, 0.3f, 0.3f));
                                //change path line width value, default 1.5
                                sbms.get(playerId).setPathLineWitdh(5f);
                            }
                            //Set agent to manipulate with A.I.     
                            sbms.get(playerId).setPrimaryAgent(sceneManager.getPlayer(playerId));
                            pfm.setPrimaryAgent(sceneManager.getPlayer(playerId));

                            //Set target agent
                            GameObject target = new GameObject("target");
                            if (!frameworkOptions.get("HOG2Maps")
                                    && !frameworkOptions.get("useMazeGenerators")) {
                                pfm.setGraph(pfm.getGraph());
                                target.setPosition(new Vector3D(25, 0, -25));
                            }
                            if (frameworkOptions.get("useMazeGenerators")) {
                                target.setPosition(mg.getMazeMin());
                            }
                            if (frameworkOptions.get("HOG2Maps")
                                    && frameworkOptions.get("Floor")
                                    && floor != null) {
                                //TODO: alter when implemented for a compound mesh.
                                target.setPosition(floor.getMeshes().get(0).getMinVertex());
                            }
                            target.setRotation(ZERO);
                            target.setVelocity(agentsVelocity);
                            target.setMaxSpeed((float) agentsVelocity.getNorm());
                            target.setBoundingVolume(0, new BoundingSphere(null));
                            //sceneManager.addPlayer(target);
                            sbms.get(playerId).setPrimaryGoal(target);
                            pfm.setPrimaryGoal(target);

                            sbms.get(playerId).setPath(null);
                            if (!frameworkOptions.get("_31x31")) {
                                log.info("Searching for a path.");
                                pfm.pathFindersDebugCSVLog();
                                sbms.get(playerId).setPath(pfm.findPath());
                            }
                        } else if (frameworkOptions.get("useSteeringBehaviors")) {
                            if (frameworkOptions.get("useSteeringBehaviorsGroups")) {
                                for (int i = 0; i < groupSize; i++) {
                                    sbms.get("player" + (i + 1)).setPrimaryAgent(sceneManager.getPlayer("player" + (i + 1)));
                                    sbms.get("player" + (i + 1)).setPrimaryGoal(sceneManager.getPlayer(playerId));
                                }
                                sbms.get("player2").setEntityOffset(new Vector3D(-5, 0, -5));
                                sbms.get("player3").setEntityOffset(new Vector3D(5, 0, -5));
                                sbms.get("player4").setEntityOffset(new Vector3D(-10, 0, -10));
                                sbms.get("player5").setEntityOffset(new Vector3D(0, 0, -10));
                                sbms.get("player6").setEntityOffset(new Vector3D(10, 0, -10));
                            } else {
                                sbms.get(playerId).setPrimaryAgent(sceneManager.getPlayer(playerId));
                                if (sbms.get("player2").isSteeringBehaviorOn()) {
                                    sbms.get("player2").setPrimaryAgent(sceneManager.getPlayer("player2"));
                                    sbms.get("player2").setPrimaryGoal(sceneManager.getPlayer(playerId));
                                    if (sbms.get("player2").isSteeringBehaviorOn(INTERPOSE)) {
                                        sbms.get("player2").setSecondaryGoal(sceneManager.getPlayer("player3"));
                                        sbms.get("player3").setPrimaryAgent(sceneManager.getPlayer("player3"));
                                        sbms.get("player3").setPrimaryGoal(sceneManager.getPlayer(playerId));
                                    }
                                }
                            }
                        }
                    }
                    if (old_useAI) {
                        frameworkOptions.put("useAI", !frameworkOptions.get("useAI"));
                    }
                }
            };
            thread.start();
        }

        state = PLAYING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameProcessInput() {
        //log.info( "Process Input.");
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
                sceneManager.toggleOnOffLights();
            }
            if (this.keyBoard.isDetecting("2")) {
                sceneManager.toggleOnOffShowTextures();
            }
            if (this.keyBoard.isDetecting("3")) {
                sceneManager.toggleOnOffShowWireframe();
            }
            if (this.keyBoard.isDetecting("4")) {
                sceneManager.toggleOnOffBroadPhaseCollisionDetection();
            }
            if (this.keyBoard.isDetecting("5")) {
                frameworkOptions.put("Floor", !frameworkOptions.get("Floor"));
                //useAI = !useAI;
            }
            if (this.keyBoard.isDetecting("6")) {
                frameworkOptions.put("showPath", !frameworkOptions.get("showPath"));
            }
            if (this.keyBoard.isDetecting("7")) {
                frameworkOptions.put("showGraph", !frameworkOptions.get("showGraph"));
            }
            if (this.keyBoard.isDetecting("8")) {
                frameworkOptions.put("showVisited", !frameworkOptions.get("showVisited"));
            }         

            if (this.keyBoard.isDetecting("Z")) {
                sceneManager.setCamera2Use("PERSPECTIVE");
            }
            if (this.keyBoard.isDetecting("X")) {
                sceneManager.setCamera2Use("THIRD_PERSON");
            }
            if (this.keyBoard.isDetecting("C")) {
                sceneManager.setCamera2Use("UPPER_VIEW");
            }

            boolean wDown = this.keyBoard.isContinuouslyDetecting("W");
            boolean sDown = this.keyBoard.isContinuouslyDetecting("S");
            boolean aDown = this.keyBoard.isContinuouslyDetecting("A");
            boolean dDown = this.keyBoard.isContinuouslyDetecting("D");
            //log.info( format("%b %b %b %b", wDown, sDown, dDown, aDown));

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gameRender(GL2 gl) {
        //log.info( "Render.");
        switch (state) {
            case INITIALIZED:
                log.info("Initialized ...");
                log.info("Loading ...");
                this.gameLoadContent();
                log.info("Loaded ...\n");
                //TODO: (SceneManagement Extras) loading menu.
                break;
            case PLAYING:
                this.clearColor = this.color;

                sceneManager.render(gl);
                if (frameworkOptions.get("useAI")) {
                    if (frameworkOptions.get("useSteeringBehaviors")) {
                        sbms.get(this.playerId).renderAI(gl);
                        if (frameworkOptions.get("useSteeringBehaviorsGroups")) {
                            for (int i = 1; i < this.groupSize; i++) {
                                sbms.get("player" + (i + 1)).renderAI(gl);
                            }
                        }
                    }
                    if (frameworkOptions.get("usePathFinders")) {
                        pfm.renderAI(gl);
                    }
                }
                break;
            case PAUSED:
                //......
                break;
            case GAME_OVER:
                //......
                break;
        }
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
        if ("true".equals(getProperty("JOT.scenerotate"))) {
            if (sceneManager != null) {
                Vector3D cameraRotation = sceneManager.getCamera(this.playerId).getRotation();
                cameraRotation = new Vector3D(
                        cameraRotation.getX(),
                        cameraRotation.getY() + .3,
                        cameraRotation.getZ());
                sceneManager.getCamera(this.playerId).setRotation(cameraRotation);
                sceneManager.updateCamera(1);
            }
        } else {
            if (state == GAME_OVER) {
                return;
            }

            if (state == PLAYING) {
                //log.info( "Update.");
                if (this.updateCount == UPDATE_RATE) {
                    this.updateCount = 0;
                }
                this.updateCount++;

                //Physics Update 1: Bullets Processing
                if (sceneManager != null) {
                    sceneManager.updateMutableObjects(1);
                }

                long timeStamp = currentTimeMillis();

                //Physics Update 2: Player Update
                Vector3D newMouseCursorPosition = this.mouse.getPositionShift();

                if (sceneManager != null && sceneManager.getPlayer(this.playerId) != null) {
                    Vector3D rotation = sceneManager.getPlayer(this.playerId).getRotation();

                    if (!frameworkOptions.get("usePathFinders")) {
                        if (newMouseCursorPosition.getX() != 0) {
                            this.isRotating = true;
                            rotation = new Vector3D(
                                    rotation.getX(),
                                    rotation.getY() + newMouseCursorPosition.getX() + .3,
                                    rotation.getZ());
                            sceneManager.getPlayer(this.playerId).updateRotationY((float) rotation.getY());
                        }

                        if (this.isMoving) {
                            //Vector3D position = sceneManager.getPlayer(this.playerId).getPosition();
                            Vector3D newPosition = translatePolar(ZERO,
                                    //sceneManager.getPlayer(this.playerId).getVelocity(),
                                    this.playerVelocity,
                                    //(float) rotation.getY(), direction, 1);
                                    (float) rotation.getY(), this.direction, dt);

                            AbstractBoundingVolume boundingVolume
                                    = sceneManager.getPlayer(this.playerId).getBoundingVolumeCopy(0);
                            boundingVolume.min = boundingVolume.min.add(newPosition);
                            boundingVolume.max = boundingVolume.max.add(newPosition);
                            GameObject temp = new GameObject(this.playerId);
                            temp.setBoundingVolume(0, boundingVolume);
                            //TODO: alter when implemented for a compound mesh.
                            temp.addChild(sceneManager.getPlayer(this.playerId).getMeshes().get(0));

                            String playerCrash = sceneManager.checkPlayerPlayerCollision(temp);

                            //Vector3D futurePosition = new Vector3D(position.toArray()).add(newPosition);
                            //sceneManager.getPlayer(this.playerId).getRotation();
                            if (!sceneManager.checkSceneBoundsCollision(boundingVolume)
                                    && !sceneManager.checkPlayerImmutableObjectCollision(temp)
                                    && (playerCrash == null || this.playerId.equals(playerCrash))) {
                                sceneManager.getPlayer(this.playerId).updatePosition(newPosition);

                                //Set the oriented velocity for steering behaviors.                                 
                                sceneManager.getPlayer(this.playerId).setVelocity(new Vector3D(
                                        this.playerVelocity.getX() * (newPosition.getX() == 0.0
                                        ? 0.0 : signum(newPosition.getX())),
                                        this.playerVelocity.getY() * (newPosition.getY() == 0.0
                                        ? 0.0 : signum(newPosition.getY())),
                                        this.playerVelocity.getZ() * (newPosition.getZ() == 0.0
                                        ? 0.0 : signum(newPosition.getZ()))));
                            } else {
                                // If not able to move because of collision
                                // set main player velocity to zero.
                                sceneManager.getPlayer(this.playerId).setVelocity(ZERO);
                            }
                        } else {
                            // If not able to move set main player velocity to zero.
                            sceneManager.getPlayer(this.playerId).setVelocity(ZERO);
                        }
                    }

                    if (frameworkOptions.get("useAI")
                            && (frameworkOptions.get("useSteeringBehaviors")
                            || frameworkOptions.get("usePathFinders"))) {
                        //sbms.get(this.playerId).update(sceneManager, 1);  
                        sbms.get(this.playerId).update(sceneManager, dt);
                        if (frameworkOptions.get("useSteeringBehaviorsGroups")) {
                            for (int i = 1; i < this.groupSize; i++) {
                                //sbms.get("player" + (i + 1)).update(sceneManager, 1);
                                sbms.get("player" + (i + 1)).update(sceneManager, dt);
                            }
                        }

                        boolean shooting = false;
//                        if (useSteeringBehaviors) {
//                            shooting = sbm.isAttacking(timeStamp, lastTimeStamp);
//                            if (shooting) {
//                                lastTimeStamp = timeStamp;
//                            }
//                        }
                        if (shooting) {
                            log.info("Shooting!");

                            Vector3D v = sceneManager.getPlayer("player2").getPosition();
                            Vector3D bulletPosition = new Vector3D(
                                    v.getX(), v.getY() + 1, v.getZ());
//                               rotation = sceneManager.getPlayer(this.playerId).getRotation();
//                               if (LeftShoot) {
//                                   log.warning("LeftShoot");
//                                   rotation = new Vector3D(rotation.getX(), rotation.getY() + 50, rotation.getZ());
//                                   LeftShoot = false;
//                               } else {
//                                   rotation = new Vector3D(rotation.getX(), rotation.getY() + 50, rotation.getZ());
//                                   LeftShoot = true;
//                               }
                            try {
                                TransformGroup bulletRotate
                                        = this.models.get("bullet").clone();
                                GameObject bullet = new GameObject(
                                        "player2Bullet" + this.bulletIdSequence++,
                                        this.models.get("bullet"));
                                //bulletRotate.setBoundingVolume(0, null);
                                bullet.getBoundingVolume(0).setRenderable(true);
                                bullet.addChild(bulletRotate);
                                bullet.setScaling(new Vector3D(0.5F, 0.5F, 0.5F));
                                //bullet.setPosition(bulletPosition);
                                //bullet.setRotation(new Vector3D(rotation.toArray()));
                                bullet.setVelocity(new Vector3D(0.8F, 2.5F, 0.8F));
                                bullet.updateRotationY((float) rotation.getY());
                                bullet.updatePosition(bulletPosition);
                                sceneManager.addMutableObject(bullet);
                            } catch (CloneNotSupportedException ex) {
                                log.severe(ex.getMessage());
                            }
                        }
                    }
                }
                sceneManager.updateCamera(1);
            }
        }
    }

    private void setAgentSteeringBehaviors(String AgentId,
            ArrayList<SteeringBehavior> steeringBehaviors) {
        //Deactivate all steering behaviors.
        sbms.get(AgentId).SteeringBehaviorOff(SEEK);
        sbms.get(AgentId).SteeringBehaviorOff(FLEE);
        sbms.get(AgentId).SteeringBehaviorOff(ARRIVE);
        sbms.get(AgentId).SteeringBehaviorOff(PURSUIT);
        sbms.get(AgentId).SteeringBehaviorOff(EVADE);
        sbms.get(AgentId).SteeringBehaviorOff(WANDER);
        sbms.get(AgentId).SteeringBehaviorOff(WANDER_AREA);
        sbms.get(AgentId).SteeringBehaviorOff(WANDER_CURVE);
        //OBSTACLE_AVOIDANCE, WALL_AVOIDANCE, //Need these? I think not!
        sbms.get(AgentId).SteeringBehaviorOff(INTERPOSE);
        sbms.get(AgentId).SteeringBehaviorOff(HIDE);
        sbms.get(AgentId).SteeringBehaviorOff(PATH_FOLLOW);
        sbms.get(AgentId).SteeringBehaviorOff(OFFSET_PURSUIT);
        sbms.get(AgentId).SteeringBehaviorOff(SEPARATION);
        sbms.get(AgentId).SteeringBehaviorOff(ALIGNMENT);
        sbms.get(AgentId).SteeringBehaviorOff(COHESION);

        //Activate steering behaviors to use.
        steeringBehaviors.stream().forEach(sb -> {
            sbms.get(AgentId).SteeringBehaviorOn(sb);
            log.info(format(sb + " activated for agent " + AgentId));
        });
        steeringBehaviors.clear();
    }
}
