/*
 * This file is part of the JOT game engine managers framework toolkit
 * component. 
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
package jot.manager;

import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2ES3.GL_QUADS;
import static com.jogamp.opengl.GLProfile.getDefault;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureData;
import static com.jogamp.opengl.util.texture.awt.AWTTextureIO.newTextureData;
import java.awt.Color;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.util.Arrays.asList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.gui.Game.defaultCamera;
import static jot.gui.Game.defaultLight;
import jot.gui.camera.Camera;
import jot.io.image.ImageRenderer;
import jot.math.geometry.Node;
import jot.math.geometry.PolygonMesh;
import jot.math.geometry.TransformGroup;
import static jot.math.geometry.Transformations.rotateX;
import static jot.math.geometry.Transformations.rotateY;
import static jot.math.geometry.Transformations.rotateZ;
import static jot.math.geometry.Transformations.scale;
import static jot.math.geometry.Transformations.translate;
import jot.math.geometry.bounding.AABB;
import jot.math.geometry.bounding.AbstractBoundingVolume;
import jot.math.geometry.bounding.BoundingSphere;
import jot.math.geometry.bounding.OBB;
import jot.math.geometry.generators.AbstractGeometryGenerator;
import jot.math.geometry.generators.maze.AbstractMazeGenerator;
import jot.math.geometry.generators.smoother.AbstractSurface;
import jot.math.geometry.generators.terrain.AbstractTerrainGenerator;
import jot.math.geometry.quadrics.CelestialObject;
import jot.math.geometry.quadrics.SkyDome;
import jot.math.geometry.shape.AbstractShape;
import jot.math.geometry.shape.RayTracerPlane;
import jot.math.geometry.shape.RayTracerSphere;
import jot.math.geometry.shape.RayTracerTriangle;
import jot.physics.AbstractRayTracerMaterial;
import jot.physics.CollisionHandler;
import static jot.physics.CollisionHandler.checkSkyBoxCollision;
import static jot.physics.CollisionHandler.checkSkyDomeCollision;
import jot.physics.Diffuse;
import jot.physics.IntersectionResult;
import static jot.physics.IntersectionResult.MISS;
import static jot.physics.Kinematics.translatePolar;
import jot.physics.Light;
import jot.physics.Ray;
import jot.physics.Refractive;
import jot.physics.Specular;
import static jot.util.CoreOptions.coreOptions;
import static jot.util.ExtensionPhysicsOptions.extensionPhysicsOptions;
import static jot.util.FrameworkOptions.frameworkOptions;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements a scene manager.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class SceneManager {

    static final Logger log = getLogger("SceneManager");

    //The scene dimensions where width, height, and length are in the range [-size / 2, size / 2].
    protected static float sceneSize = 100;

    /**
     * The asset manager.
     */
    public static AssetManager localAssetManager = new AssetManager();

    static {
        log.setLevel(OFF);
    }

    /**
     * Get the size of the scene world.
     *
     * @return height, width and length of a SkyBox or radius of a SkyDome.
     */
    public static float getSceneSize() {
        return sceneSize;
    }

    /**
     * Clamp x between 0 and 1.
     *
     * @param x
     * @return the clamped value of x.
     */
    public static double clamp(double x) {
        return min(max(0, x), 1);
    }

    /**
     * Intensify the brightness of a given value x.
     *
     * @param x a given value x.
     * @return the brightness of a given value x intensified, i.e.,
     * clamp(x)^(1/2.2).
     */
    public static double increaseBrightness(double x) {
        return pow(clamp(x), 1 / 2.2);
    }

    /**
     * Convert a given double value to an integer between [0; 255].
     *
     * @param x a given double value.
     * @return a given double value converted to an integer between [0; 255].
     */
    public static int toInt(double x) {
        return (int) (clamp(x) * 255 + .5);
    }

    /**
     * The scene graph tree root node.
     */
    protected GameObject rootNode = new GameObject("root");

    /**
     * The scene lights.
     */
    protected ConcurrentHashMap<String, Light> lights = new ConcurrentHashMap<>();

    /**
     * The scene cameras.
     */
    protected ConcurrentHashMap<String, Camera> cameras = new ConcurrentHashMap<>();

    /**
     * The HashMap with all {@literal <playerId, GameObject>} pairs.
     */
    protected ConcurrentHashMap<String, GameObject> players = new ConcurrentHashMap<>();

    /**
     * The HashMap with all GameObject.
     */
    protected ConcurrentLinkedQueue<GameObject> gameObjects = new ConcurrentLinkedQueue<>();

    /**
     * The HashMap with all {@literal <mutableObjectID, GameObject>} pairs.
     */
    protected ConcurrentLinkedQueue<GameObject> mutableObjects = new ConcurrentLinkedQueue<>();

    /**
     * The HashMap with all {@literal <immutableObjectID, GameObjects>} pairs.
     */
    protected ConcurrentHashMap<String, GameObject> immutableObjects = new ConcurrentHashMap<>();

    protected String localLightId;
    protected String localCameraId;
    protected String localPlayerId;

    protected TransformGroup floor;
    protected TransformGroup sky;

    protected final int ATTACK_DAMAGE = 10;      //TODO: read from file in constructor

    private AbstractRayTracerMaterial DIFFUSE;
    private AbstractRayTracerMaterial SPECULAR;
    private AbstractRayTracerMaterial REFRACTIVE;

    /**
     * RayTracer generated image.
     */
    public Vector3D[][] image;

    /**
     * RayTracer light sources list.
     */
    public ConcurrentLinkedQueue<RayTracerSphere> lightSources = new ConcurrentLinkedQueue<>();

    /**
     * RayTracer shapes list.
     */
    public ConcurrentLinkedQueue<AbstractShape> shapes = new ConcurrentLinkedQueue<>();

    /**
     * Constructor, initializes all data structures to players, mutable and
     * immutable objects. Also, loads all textures to further usage in
     * rendering.
     *
     * @param texturesPaths all textures to further usage in rendering.
     */
    public SceneManager(ConcurrentHashMap<String, String> texturesPaths) {
        localAssetManager.setTextures(texturesPaths);
    }

    /**
     * Constructor, initializes all data structures to players, mutable and
     * immutable objects. Also, loads all textures to further usage in
     * rendering.
     *
     * @param texturesPaths all textures to further usage in rendering.
     * @param width of the image to generate when RayTracing.
     * @param height of the image to generate when RayTracing.
     * @param random
     */
    public SceneManager(ConcurrentHashMap<String, String> texturesPaths,
            int width, int height, ThreadLocal<Random> random) {
        localAssetManager.setTextures(texturesPaths);

        this.DIFFUSE = new Diffuse(random);
        this.SPECULAR = new Specular();
        this.REFRACTIVE = new Refractive(random);

        this.image = new Vector3D[width][];
        for (int y = 0; y < width; y++) {
            this.image[y] = new Vector3D[height];
        }
    }

    /**
     * Get all GameObjects.
     *
     * @return an arrayList with all the gameObjects in the scene.
     */
    public ConcurrentLinkedQueue<GameObject> getAllGameObjects() {
        return this.gameObjects;
    }

    /**
     * Get a specific GameObject given its id.
     *
     * @param id the id for the GameObject to get.
     * @return The gameObject with the given id, otherwise null.
     */
    public GameObject getGameObject(String id) {
        Iterator<Node> iterator = this.rootNode.childIterator();
        while (iterator.hasNext()) {
            GameObject go = (GameObject) iterator.next();
            if (go.getId().toLowerCase().contains(id)) {
                return go;
            }
        }
        return null;
    }

    /**
     * Get all Immutable objects Ids.
     *
     * @return all Immutable objects Ids.
     */
    public Iterator<GameObject> getAllImmutableObjects() {
        return this.immutableObjects.values().iterator();
    }

    /**
     * Get all mutable objects.
     *
     * @return all mutable objects.
     */
    public Iterator<GameObject> getAllMutableObjects() {
        return this.mutableObjects.iterator();
    }

    /**
     * Get the number of players in the scene.
     *
     * @return the number of players in the scene.
     */
    public long getNumberOfPlayers() {
        return this.players.size();
    }

    /**
     * Get all players Ids.
     *
     * @return all players Ids.
     */
    public Iterator<GameObject> getAllPlayers() {
        return this.players.values().iterator();
    }

    /**
     * Get a player (if it exists) whose Id is equal to a provided playerId.
     *
     * @param playerId a provided playerId.
     * @return a player (if it exists) whose playerId differs from both provided
     * playerIds, otherwise NULL.
     */
    public GameObject getPlayer(String playerId) {
        return this.players.get(playerId);
    }

    /**
     * Set which is the local player that perceives the managed scene, e.g., in
     * a multiplayer game this is the player controlled locally.
     *
     * @param playerId the local player identifier.
     */
    public void setLocalPlayerId(String playerId) {
        this.localPlayerId = playerId;
    }

    /**
     * Set the size of the scene world.
     *
     * @param size common height, width and length of a SkyBox or radius of a
     * SkyDome.
     */
    public void setSceneSize(float size) {
        sceneSize = size;
    }

    /**
     * Get light with provided Id.
     *
     * @param lightId a provided light Id
     * @return a provided light Id if it exists, otherwise NULL.
     */
    public Light getLight(String lightId) {
        return this.lights.get(lightId);
    }

    /**
     * Choose from the available lights the one to use.
     *
     * @param lightId the light to use.
     */
    public void setLight2Use(String lightId) {
        this.localLightId = lightId;
        defaultLight = this.lights.get(this.localLightId);
    }

    /**
     * Get camera with provided Id.
     *
     * @param cameraId a provided camera Id
     * @return a provided camera if it exists, otherwise NULL.
     */
    public Camera getCamera(String cameraId) {
        return this.cameras.get(cameraId);
    }

    /**
     * Choose from the available cameras the one to use.
     *
     * @param cameraId the camera to use.
     */
    public void setCamera2Use(String cameraId) {
        this.localCameraId = cameraId;
        defaultCamera = this.cameras.get(this.localCameraId);
        switch (this.cameras.get(this.localCameraId).type) {
            case FIRST_PERSON:
            case THIRD_PERSON:
                if (this.sky != null && !frameworkOptions.get("testMode")) {
                    this.sky.getMeshes().get(0).getMaterial(0).setRenderable(true);
                    if (this.sky.getMeshes().size() > 1) { //Only for skyBox
                        this.sky.getMeshes().get(1).getMaterial(0).setRenderable(true);
                        this.sky.getMeshes().get(2).getMaterial(0).setRenderable(true);
                        this.sky.getMeshes().get(3).getMaterial(0).setRenderable(true);
                        this.sky.getMeshes().get(4).getMaterial(0).setRenderable(true);
                    }
                }
                break;
            case UPPER_VIEW:
            case UPPER_VIEW_FOLLOW:
                if (this.sky != null && !frameworkOptions.get("testMode")) {
                    this.sky.getMeshes().get(0).getMaterial(0).setRenderable(false);
                    if (this.sky.getMeshes().size() > 1) { //Only for skyBox
                        this.sky.getMeshes().get(1).getMaterial(0).setRenderable(true);
                        this.sky.getMeshes().get(2).getMaterial(0).setRenderable(true);
                        this.sky.getMeshes().get(3).getMaterial(0).setRenderable(true);
                        this.sky.getMeshes().get(4).getMaterial(0).setRenderable(true);
                    }
                }
                break;
            case PERSPECTIVE:
            case PERSPECTIVE_FOLLOW:
            case PERSPECTIVE_RAYTRACER:
                if (this.sky != null && !frameworkOptions.get("testMode")) {
                    this.sky.getMeshes().get(0).getMaterial(0).setRenderable(false);
                    if (this.sky.getMeshes().size() > 1) { //Only for skyBox
                        this.sky.getMeshes().get(1).getMaterial(0).setRenderable(false);
                        this.sky.getMeshes().get(2).getMaterial(0).setRenderable(false);
                        this.sky.getMeshes().get(3).getMaterial(0).setRenderable(false);
                        this.sky.getMeshes().get(4).getMaterial(0).setRenderable(false);
                    }
                }
                break;
            default:
                throw new AssertionError(this.cameras.get(this.localCameraId).type.name());
        }
    }

    /**
     * Add SkyBox floor to the scene. Required, since floor might be irregular
     * generated terrain.
     *
     * @param floor SkyBox floor to add to the scene.
     */
    public void setFloor(TransformGroup floor) {
        if (coreOptions.get("Floor")) {
            this.floor = floor;
            //rootNode.removeChild(floor);
            //rootNode.addChild(floor);
        }
    }

    /**
     * Add sky to the scene.
     *
     * @param sky to add to the scene.
     */
    public void setSky(TransformGroup sky) {
        if (frameworkOptions.get("SkyBox") || frameworkOptions.get("SkyDome")) {
            this.sky = sky;
            //rootNode.removeChild(sky);
            //rootNode.addChild(sky)
        }
    }

    /**
     * Toggle On/Off broad phase collision detection.
     */
    public void toggleOnOffShowFPS() {
        coreOptions.put("showFPS", !coreOptions.get("showFPS"));
    }

    /**
     * Toggle On/Off lights.
     */
    public void toggleOnOffShowText() {
        coreOptions.put("showText", !coreOptions.get("showText"));
    }

    /**
     * Toggle On/Off lights.
     */
    public void toggleOnOffLights() {
        frameworkOptions.put("useLights", !frameworkOptions.get("useLights"));
    }

    /**
     * Toggle On/Off broad phase collision detection.
     */
    public void toggleOnOffBroadPhaseCollisionDetection() {
        extensionPhysicsOptions.put("useBroadPhaseCollisionDetection",
                !extensionPhysicsOptions.get("useBroadPhaseCollisionDetection"));
    }

    /**
     * Toggle On/Off narrow phase collision detection.
     */
    public void toggleOnOffNarrowPhaseCollisionDetection() {
        extensionPhysicsOptions.put("useNarrowPhaseCollisionDetection",
                !extensionPhysicsOptions.get("useNarrowPhaseCollisionDetection"));
    }

    /**
     * Toggle On/Off show planar shadows.
     */
    public void toggleOnOffShowPlanarShadows() {
        frameworkOptions.put("showPlanarShadows",
                !frameworkOptions.get("showPlanarShadows"));
        if (frameworkOptions.get("showPlanarShadows")) {
            frameworkOptions.put("showShadowMaps", false);
        }
        if (frameworkOptions.get("showZBuffer")) {
            frameworkOptions.put("showZBuffer", false);
        }
    }

    /**
     * Toggle On/Off show shadow maps.
     */
    public void toggleOnOffShowShadowMaps() {
        frameworkOptions.put("showShadowMaps",
                !frameworkOptions.get("showShadowMaps"));
        if (frameworkOptions.get("showShadowMaps")) {
            frameworkOptions.put("showPlanarShadows", false);
        }
        if (frameworkOptions.get("showZBuffer")) {
            frameworkOptions.put("showZBuffer", false);
        }
    }

    /**
     * Toggle On/Off mesh render textures.
     */
    public void toggleOnOffShowTextures() {
        coreOptions.put("showTextures", !coreOptions.get("showTextures"));
    }

    /**
     * Toggle On/Off mesh render as wireframe.
     */
    public void toggleOnOffShowWireframe() {
        coreOptions.put("showWireframe", !coreOptions.get("showWireframe"));
    }

    /**
     * Toggle On/Off show Z-Buffer.
     */
    public void toggleOnOffShowZBuffer() {
        frameworkOptions.put("showZBuffer",
                !frameworkOptions.get("showZBuffer"));

        if (frameworkOptions.get("showPlanarShadows")) {
            frameworkOptions.put("showPlanarShadows", false);
        }
        if (frameworkOptions.get("showShadowMaps")) {
            frameworkOptions.put("showShadowMaps", false);
        }
    }

    /**
     * Add a new camera to the managed scene.
     *
     * @param camera the new camera to add to the managed scene.
     */
    public void addCamera(Camera camera) {
        this.cameras.put(camera.cameraId, camera);
    }

    /**
     * Add a new light to the managed scene.
     *
     * @param light the new light to add to the managed scene.
     */
    public void addLight(Light light) {
        this.lights.put(light.lightId, light);
    }

    /**
     * Add a new Immutable object to the scene.
     *
     * @param immutableObject the Immutable object to add to the scene.
     */
    public void addImmutableObject(GameObject immutableObject) {
        this.gameObjects.add(immutableObject);
        this.immutableObjects.put(immutableObject.getId(), immutableObject);
        this.rootNode.addChild(immutableObject);
    }

    /**
     * Remove a Immutable object (if it exists) from the scene.
     *
     * @param immutableObject the Immutable object to remove (if it exists) from
     * the scene.
     */
    public void removeImmutableObject(GameObject immutableObject) {
        this.gameObjects.remove(immutableObject);
        this.immutableObjects.remove(immutableObject.getId());
        this.rootNode.removeChild(immutableObject);
    }

    /**
     * Add a new mutable object to the scene.
     *
     * @param mutableObject the mutable object to add to the scene.
     */
    public void addMutableObject(GameObject mutableObject) {
        this.gameObjects.add(mutableObject);
        this.mutableObjects.add(mutableObject);
        this.rootNode.addChild(mutableObject);
    }

    /**
     * Remove a mutable object (if it exists) from the scene.
     *
     * @param mutableObject the mutable object to remove (if it exists) from the
     * scene.
     */
    public void removeMutableObject(GameObject mutableObject) {
        this.gameObjects.remove(mutableObject);
        this.mutableObjects.remove(mutableObject);
        this.rootNode.removeChild(mutableObject);
    }

    /**
     * Add a new player to the scene.
     *
     * @param player the player to add to the scene.
     */
    public void addPlayer(GameObject player) {
        this.gameObjects.add(player);
        this.players.put(player.getId(), player);
        this.rootNode.addChild(player);
    }

    /**
     * Remove a player (if it exists) from the scene.
     *
     * @param player the player to remove (if it exists) from the scene.
     */
    public void removePlayer(GameObject player) {
        this.gameObjects.remove(player);
        this.players.remove(player.getId());
        this.rootNode.removeChild(player);
    }

    /**
     * Create a block obstacle, i.e., set texture, light/material properties,
     * vertex, normals and textureCoords values.
     *
     * @param id the unique identifier of the obstacle.
     * @param texture the texture path to map.
     * @param side the size of the block side.
     * @param boundingVolumeType the type of BoundingVolume that must engulf
     * this model.
     * @return a gameObject which contains a mesh regarding the obstacle data.
     */
    public GameObject buildBlock(String id, String texture, float side,
            AbstractBoundingVolume.BoundingVolumeType boundingVolumeType) {
        GameObject obstacleGroup = new GameObject(id);
        try {
            Texture obstacleTexture = localAssetManager.getTexture(texture);

            PolygonMesh northMesh = new PolygonMesh("northMesh");
            northMesh.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
            northMesh.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
            northMesh.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
            northMesh.getMaterial(0).setE(new float[]{1, 1, 1, 0});
            northMesh.getMaterial(0).setNs(10);
            northMesh.setVertexIndices(new int[]{0, 1, 2, 0, 2, 3});
            northMesh.setNormalsIndices(new int[]{0, 1, 2, 0, 2, 3});
            northMesh.setVertices(new float[]{
                side, -side, side,
                side, side, side,
                -side, side, side,
                -side, -side, side});
            northMesh.setNormals(new float[]{
                0, 0, -1,
                0, 0, -1,
                0, 0, -1,
                0, 0, -1});
            if (localAssetManager.isTexturesNotNull()) {
                northMesh.getMaterial(0).setTexture(obstacleTexture);
                northMesh.setTexCoordIndices(new int[]{0, 1, 2, 0, 2, 3});
                TextureCoords textureCoords = obstacleTexture.getImageTexCoords();
                northMesh.setTextureCoords(new float[]{
                    textureCoords.left(), textureCoords.top(), 0,
                    textureCoords.left(), textureCoords.bottom(), 0,
                    textureCoords.right(), textureCoords.bottom(), 0,
                    textureCoords.right(), textureCoords.top(), 0,});
            }
            obstacleGroup.addChild(northMesh);

            PolygonMesh southMesh = new PolygonMesh("southMesh");
            southMesh.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
            southMesh.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
            southMesh.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
            southMesh.getMaterial(0).setE(new float[]{1, 1, 1, 0});
            southMesh.getMaterial(0).setNs(10);
            southMesh.setVertexIndices(new int[]{0, 1, 2, 0, 2, 3});
            southMesh.setNormalsIndices(new int[]{0, 1, 2, 0, 2, 3});
            southMesh.setVertices(new float[]{
                -side, -side, -side,
                -side, side, -side,
                side, side, -side,
                side, -side, -side});
            southMesh.setNormals(new float[]{
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1});
            if (localAssetManager.isTexturesNotNull()) {
                southMesh.getMaterial(0).setTexture(obstacleTexture);
                southMesh.setTexCoordIndices(new int[]{0, 1, 2, 0, 2, 3});
                TextureCoords textureCoords = obstacleTexture.getImageTexCoords();
                southMesh.setTextureCoords(new float[]{
                    textureCoords.left(), textureCoords.top(), 0,
                    textureCoords.left(), textureCoords.bottom(), 0,
                    textureCoords.right(), textureCoords.bottom(), 0,
                    textureCoords.right(), textureCoords.top(), 0,});
            }
            obstacleGroup.addChild(southMesh);

            PolygonMesh westMesh = new PolygonMesh("westMesh");
            westMesh.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
            westMesh.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
            westMesh.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
            westMesh.getMaterial(0).setE(new float[]{1, 1, 1, 0});
            westMesh.getMaterial(0).setNs(10);
            westMesh.setVertexIndices(new int[]{0, 1, 2, 0, 2, 3});
            westMesh.setNormalsIndices(new int[]{0, 1, 2, 0, 2, 3});
            westMesh.setVertices(new float[]{
                side, -side, -side,
                side, side, -side,
                side, side, side,
                side, -side, side});
            westMesh.setNormals(new float[]{
                1, 0, 0,
                1, 0, 0,
                1, 0, 0,
                1, 0, 0});
            if (localAssetManager.isTexturesNotNull()) {
                westMesh.getMaterial(0).setTexture(obstacleTexture);
                westMesh.setTexCoordIndices(new int[]{0, 1, 2, 0, 2, 3});
                TextureCoords textureCoords = obstacleTexture.getImageTexCoords();
                westMesh.setTextureCoords(new float[]{
                    textureCoords.left(), textureCoords.top(), 0,
                    textureCoords.left(), textureCoords.bottom(), 0,
                    textureCoords.right(), textureCoords.bottom(), 0,
                    textureCoords.right(), textureCoords.top(), 0,});
            }
            obstacleGroup.addChild(westMesh);

            PolygonMesh eastMesh = new PolygonMesh("eastMesh");
            eastMesh.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
            eastMesh.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
            eastMesh.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
            eastMesh.getMaterial(0).setE(new float[]{1, 1, 1, 0});
            eastMesh.getMaterial(0).setNs(10);
            eastMesh.setVertexIndices(new int[]{0, 1, 2, 0, 2, 3});
            eastMesh.setNormalsIndices(new int[]{0, 1, 2, 0, 2, 3});
            eastMesh.setVertices(new float[]{
                -side, -side, -side,
                -side, side, -side,
                -side, side, side,
                -side, -side, side});
            eastMesh.setNormals(new float[]{
                -1, 0, 0,
                -1, 0, 0,
                -1, 0, 0,
                -1, 0, 0});
            if (localAssetManager.isTexturesNotNull()) {
                eastMesh.getMaterial(0).setTexture(obstacleTexture);
                eastMesh.setTexCoordIndices(new int[]{0, 1, 2, 0, 2, 3});
                TextureCoords textureCoords = obstacleTexture.getImageTexCoords();
                eastMesh.setTextureCoords(new float[]{
                    textureCoords.right(), textureCoords.top(), 0,
                    textureCoords.right(), textureCoords.bottom(), 0,
                    textureCoords.left(), textureCoords.bottom(), 0,
                    textureCoords.left(), textureCoords.top(), 0,});
            }
            obstacleGroup.addChild(eastMesh);

            PolygonMesh upMesh = new PolygonMesh("upMesh");
            upMesh.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
            upMesh.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
            upMesh.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
            upMesh.getMaterial(0).setE(new float[]{1, 1, 1, 0});
            upMesh.getMaterial(0).setNs(10);
            upMesh.setVertexIndices(new int[]{0, 1, 2, 0, 2, 3});
            upMesh.setNormalsIndices(new int[]{0, 1, 2, 0, 2, 3});
            upMesh.setVertices(new float[]{
                -side, side, -side,
                -side, side, side,
                side, side, side,
                side, side, -side});
            upMesh.setNormals(new float[]{
                0, -1, 0,
                0, -1, 0,
                0, -1, 0,
                0, -1, 0});
            if (localAssetManager.isTexturesNotNull()) {
                upMesh.getMaterial(0).setTexture(obstacleTexture);
                upMesh.setTexCoordIndices(new int[]{0, 1, 2, 0, 2, 3});
                TextureCoords textureCoords = obstacleTexture.getImageTexCoords();
                upMesh.setTextureCoords(new float[]{
                    textureCoords.right(), textureCoords.bottom(), 0,
                    textureCoords.right(), textureCoords.top(), 0,
                    textureCoords.left(), textureCoords.top(), 0,
                    textureCoords.left(), textureCoords.bottom(), 0,});
            }
            obstacleGroup.addChild(upMesh);

            switch (boundingVolumeType) {
                case SPHERE:
                    obstacleGroup.setBoundingVolume(0, new BoundingSphere(
                            new Vector3D(-side, -side, -side),
                            new Vector3D(side, side, side)));
                    break;
                case AABB:
                    obstacleGroup.setBoundingVolume(0, new AABB(
                            new Vector3D(-side, -side, -side),
                            new Vector3D(side, side, side)));
                    break;
                case OBB:
                    obstacleGroup.setBoundingVolume(0, new OBB(
                            new Vector3D(-side, -side, -side),
                            new Vector3D(side, side, side)));
                    break;
                default:
                    throw new AssertionError(boundingVolumeType.name());
            }
            obstacleGroup.setBoundingRadius(obstacleGroup.getBoundingVolume(0).halfDistance());

//            float[] vertices = new float[]{
//                -side, -side, -side, //0
//                -side, side, -side, //1
//                side, side, -side, //2
//                side, -side, -side, //3            
//                -side, -side, side, //4
//                -side, side, side, //5
//                side, side, side, //6
//                side, -side, side}; //7
//            float[] normals = new float[]{
//                0, 0, 1,
//                0, 0, -1,
//                1, 0, 0,
//                -1, 0, 0,
//                0, 1, 0,};
//            int[] verticesIndices = new int[]{
//                0, 1, 2, 0, 2, 3, //Front face
//                7, 6, 5, 7, 5, 4, //Back face
//                3, 2, 6, 3, 6, 7, //Left face
//                0, 1, 5, 0, 5, 4, //Right face
//                1, 5, 6, 1, 6, 2,}; //Top face
//            int[] normalsIndices = new int[]{
//                0, 0, 0, 0, 0, 0, //Front face
//                1, 1, 1, 1, 1, 1, //Back face
//                2, 2, 2, 2, 2, 2, //Left face
//                3, 3, 3, 3, 3, 3, //Right face 
//                4, 4, 4, 4, 4, 4}; //Top face
//            PolygonMesh obstacleMesh = new PolygonMesh("obstacleMesh");
//            obstacleMesh.getMaterial(0).setKa(new float[]{0.3f, 0.1f, 0.1f, 0});
//            obstacleMesh.getMaterial(0).setKs(new float[]{0.8f, 0.3f, 0.4f, 0});
//            obstacleMesh.getMaterial(0).setKd(new float[]{0.3f, 0.1f, 0.1f, 0});
//            obstacleMesh.getMaterial(0).setE(new float[]{0.3f, 0.1f, 0.1f, 0});
//            obstacleMesh.getMaterial(0).setNs(45);
//            obstacleMesh.setVertexIndices(verticesIndices);
//            obstacleMesh.setNormalsIndices(normalsIndices);
//            obstacleMesh.setVertices(vertices);
//            obstacleMesh.setNormals(normals);
//
//            if (textures != null) {
//                Texture obstacleTexture = textures.get(texture);
//                TextureCoords coords = obstacleTexture.getImageTexCoords();
//                float[] textureCoords = new float[]{
//                    coords.left(), coords.top(), 0,
//                    coords.left(), coords.bottom(), 0,
//                    coords.right(), coords.bottom(), 0,
//                    coords.right(), coords.top(), 0};
//                int[] textureCoordsIndices = new int[]{
//                    0, 1, 2, 0, 2, 3, //Front face
//                    0, 1, 2, 0, 2, 3, //Back face
//                    0, 1, 2, 0, 2, 3, //Left face
//                    3, 2, 1, 3, 1, 0, //Right face
//                    2, 3, 0, 2, 0, 1,}; //Top face
//                obstacleMesh.getMaterial(0).setTexture(obstacleTexture);
//                obstacleMesh.setTexCoordIndices(textureCoordsIndices);
//                obstacleMesh.setTextureCoords(textureCoords);
//            }
//            switch (boundingVolumeType) {
//                case SPHERE:
//                    obstacleGroup.setBoundingVolume(0, new BoundingSphere(
//                            obstacleMesh.getMinVertex(), obstacleMesh.getMaxVertex()));
//                    break;
//                case AABB:
//                    obstacleGroup.setBoundingVolume(0, new AABB(
//                            obstacleMesh.getMinVertex(), obstacleMesh.getMaxVertex()));
//                    break;
//                case OBB:
//                    obstacleGroup.setBoundingVolume(0, new OBB(
//                            obstacleMesh.getMinVertex(), obstacleMesh.getMaxVertex()));
//                    break;
//                default:
//                    throw new AssertionError(boundingVolumeType.name());
//            }
//            obstacleGroup.addChild(obstacleMesh);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        //System.out.println(obstacleGroup.getMeshes().size());

        return obstacleGroup;
    }

    /**
     * Build the astronomical/celestial object, i.e., set texture,
     * light/material properties, vertex, normals and textureCoords values.
     *
     * @param celestialObjectId the unique identifier of the
     * astronomical/celestial object.
     * @param rotationIncrement the rotation to increment at each render.
     * @param radius the value of the astronomical/celestial object radius.
     * @param texture the name and path of the texture for the
     * astronomical/celestial object.
     * @return a gameObject which contains a mesh regarding the
     * astronomical/celestial object data.
     */
    public GameObject buildCelestialObject(String celestialObjectId, float rotationIncrement, float radius, String texture) {
        GameObject celestialObjectGroup = new GameObject(celestialObjectId);

        localAssetManager.setTexture(celestialObjectId, texture);
        Texture celestialObjectTexture = localAssetManager.getTexture(celestialObjectId);

        CelestialObject celestialObjectMesh = new CelestialObject(rotationIncrement, radius, celestialObjectTexture);
        celestialObjectMesh.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
        celestialObjectMesh.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
        celestialObjectMesh.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
        celestialObjectMesh.getMaterial(0).setE(new float[]{1, 1, 1, 0});
        celestialObjectMesh.getMaterial(0).setNs(10);

        celestialObjectGroup.addChild(celestialObjectMesh);

        return celestialObjectGroup;
    }

    /**
     *
     * Build a plane floor, i.e., set texture, light/material properties,
     * vertex, normals and textureCoords values.
     *
     * @param size absolute size value of one of the sides of the square that
     * defines the floor, i.e., floor as size*size area.
     * @param texture the name and path of the texture for the floor.
     * @return a gameObject which contains a mesh regarding the floor data.
     */
    public TransformGroup buildFloor(float size, String texture) {
        if (coreOptions.get("Floor")) {
            TransformGroup floorGroup = new TransformGroup("floor");

            String[] strs = texture.split("/");
            String fileName = strs[strs.length - 1];
            log.info(fileName);
            localAssetManager.setTexture(fileName, texture);
            Texture floorTexture = localAssetManager.getTexture(fileName);
            TextureCoords textureCoords = floorTexture.getImageTexCoords();

            PolygonMesh floorMesh = new PolygonMesh("floorMesh");
            floorMesh.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
            floorMesh.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
            floorMesh.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
            floorMesh.getMaterial(0).setE(new float[]{1, 1, 1, 0});
            floorMesh.getMaterial(0).setNs(10);
            floorMesh.getMaterial(0).setTexture(floorTexture);
            floorMesh.setVertexIndices(new int[]{0, 1, 2, 0, 2, 3});
            floorMesh.setNormalsIndices(new int[]{0, 1, 2, 0, 2, 3});
            floorMesh.setTexCoordIndices(new int[]{0, 1, 2, 0, 2, 3});
            floorMesh.setVertices(new float[]{
                -size / 2, -size / 2, -size / 2,
                -size / 2, size / 2, -size / 2,
                size / 2, size / 2, -size / 2,
                size / 2, -size / 2, -size / 2});
            floorMesh.setNormals(new float[]{
                0, 1, 0,
                0, 1, 0,
                0, 1, 0,
                0, 1, 0});
            floorMesh.setTextureCoords(new float[]{
                textureCoords.left(), textureCoords.top(), 0,
                textureCoords.left(), textureCoords.bottom(), 0,
                textureCoords.right(), textureCoords.bottom(), 0,
                textureCoords.right(), textureCoords.top(), 0});
            floorGroup.setRotation(new Vector3D(90, 180, 0));
            floorGroup.addChild(floorMesh);

            return floorGroup;
        }
        return null;
    }

    /**
     * Build a geometry.
     *
     * @param gg the geometry type.
     */
    private void buildGeometry(AbstractGeometryGenerator gg) {
        gg.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
        gg.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
        gg.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
        gg.getMaterial(0).setE(new float[]{1, 1, 1, 0});
        gg.getMaterial(0).setNs(10);
        gg.generateGeometry();
    }

    /**
     * Build a maze floor.
     *
     * @param mazeGenerator the maze generator to use.
     * @return a gameObject which contains a mesh regarding the floor data.
     */
    public TransformGroup buildMaze(AbstractMazeGenerator mazeGenerator) {
        TransformGroup mazeGroup = new TransformGroup("maze");

        this.buildGeometry(mazeGenerator);

        mazeGroup.addChild(mazeGenerator);
        return mazeGroup;
    }

    /**
     * Build the SkyBox, i.e., set texture, light/material properties, vertex,
     * normals and textureCoords values.
     *
     * @param skySize absolute size value of one of the sides of the square that
     * defines the floor, i.e., floor as size*size area.
     * @param texturePrefix the common string in the name of each of the
     * textures that make up the SkyBox.
     * @param textureExtension the file extension for the given textures in the
     * texture path, assuming all textures have the same extension.
     * @return a gameObject which contains a mesh regarding the SkyBox data.
     */
    public TransformGroup buildSkyBox(
            float skySize, String texturePrefix, String textureExtension) {
        skySize = frameworkOptions.get("useUnreachableSky")
                ? skySize / 2 : skySize;

        TransformGroup skyBoxGroup = new TransformGroup("skybox");

        localAssetManager.setTexture(texturePrefix + "_up", texturePrefix + "_up." + textureExtension);
        Texture up = localAssetManager.getTexture(texturePrefix + "_up");
        localAssetManager.setTexture(texturePrefix + "_north", texturePrefix + "_north." + textureExtension);
        Texture north = localAssetManager.getTexture(texturePrefix + "_north");
        localAssetManager.setTexture(texturePrefix + "_south", texturePrefix + "_south." + textureExtension);
        Texture south = localAssetManager.getTexture(texturePrefix + "_south");
        localAssetManager.setTexture(texturePrefix + "_east", texturePrefix + "_east." + textureExtension);
        Texture east = localAssetManager.getTexture(texturePrefix + "_east");
        localAssetManager.setTexture(texturePrefix + "_west", texturePrefix + "_west." + textureExtension);
        Texture west = localAssetManager.getTexture(texturePrefix + "_west");

        PolygonMesh upMesh = new PolygonMesh("upMesh");
        upMesh.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
        upMesh.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
        upMesh.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
        upMesh.getMaterial(0).setE(new float[]{1, 1, 1, 0});
        upMesh.getMaterial(0).setNs(10);
        upMesh.getMaterial(0).setTexture(up);
        upMesh.setVertexIndices(new int[]{0, 1, 2, 0, 2, 3});
        upMesh.setNormalsIndices(new int[]{0, 1, 2, 0, 2, 3});
        upMesh.setTexCoordIndices(new int[]{0, 1, 2, 0, 2, 3});
        TextureCoords textureCoords = up.getImageTexCoords();
        upMesh.setTextureCoords(new float[]{
            textureCoords.right(), textureCoords.bottom(), 0,
            textureCoords.right(), textureCoords.top(), 0,
            textureCoords.left(), textureCoords.top(), 0,
            textureCoords.left(), textureCoords.bottom(), 0});
        upMesh.setNormals(new float[]{
            0, -1, 0,
            0, -1, 0,
            0, -1, 0,
            0, -1, 0});
        upMesh.setVertices(new float[]{
            -skySize / 2, skySize / 2, -skySize / 2,
            -skySize / 2, skySize / 2, skySize / 2,
            skySize / 2, skySize / 2, skySize / 2,
            skySize / 2, skySize / 2, -skySize / 2});
        skyBoxGroup.addChild(upMesh);

        PolygonMesh northMesh = new PolygonMesh("northMesh");
        northMesh.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
        northMesh.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
        northMesh.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
        northMesh.getMaterial(0).setE(new float[]{1, 1, 1, 0});
        northMesh.getMaterial(0).setNs(10);
        northMesh.getMaterial(0).setTexture(north);
        northMesh.setVertexIndices(new int[]{0, 1, 2, 0, 2, 3});
        northMesh.setNormalsIndices(new int[]{0, 1, 2, 0, 2, 3});
        northMesh.setTexCoordIndices(new int[]{0, 1, 2, 0, 2, 3});
        textureCoords = north.getImageTexCoords();
        northMesh.setTextureCoords(new float[]{
            textureCoords.left(), textureCoords.top(), 0,
            textureCoords.left(), textureCoords.bottom(), 0,
            textureCoords.right(), textureCoords.bottom(), 0,
            textureCoords.right(), textureCoords.top(), 0});
        northMesh.setNormals(new float[]{
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1});
        northMesh.setVertices(new float[]{
            skySize / 2, -skySize / 2, skySize / 2,
            skySize / 2, skySize / 2, skySize / 2,
            -skySize / 2, skySize / 2, skySize / 2,
            -skySize / 2, -skySize / 2, skySize / 2});
        skyBoxGroup.addChild(northMesh);

        PolygonMesh southMesh = new PolygonMesh("southMesh");
        southMesh.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
        southMesh.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
        southMesh.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
        southMesh.getMaterial(0).setE(new float[]{1, 1, 1, 0});
        southMesh.getMaterial(0).setNs(10);
        southMesh.getMaterial(0).setTexture(south);
        southMesh.setVertexIndices(new int[]{0, 1, 2, 0, 2, 3});
        southMesh.setNormalsIndices(new int[]{0, 1, 2, 0, 2, 3});
        southMesh.setTexCoordIndices(new int[]{0, 1, 2, 0, 2, 3});
        textureCoords = south.getImageTexCoords();
        southMesh.setTextureCoords(new float[]{
            textureCoords.left(), textureCoords.top(), 0,
            textureCoords.left(), textureCoords.bottom(), 0,
            textureCoords.right(), textureCoords.bottom(), 0,
            textureCoords.right(), textureCoords.top(), 0});
        southMesh.setNormals(new float[]{
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1});
        southMesh.setVertices(new float[]{
            -skySize / 2, -skySize / 2, -skySize / 2,
            -skySize / 2, skySize / 2, -skySize / 2,
            skySize / 2, skySize / 2, -skySize / 2,
            skySize / 2, -skySize / 2, -skySize / 2});
        skyBoxGroup.addChild(southMesh);

        PolygonMesh eastMesh = new PolygonMesh("eastMesh");
        eastMesh.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
        eastMesh.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
        eastMesh.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
        eastMesh.getMaterial(0).setE(new float[]{1, 1, 1, 0});
        eastMesh.getMaterial(0).setNs(10);
        eastMesh.getMaterial(0).setTexture(east);
        eastMesh.setVertexIndices(new int[]{0, 1, 2, 0, 2, 3});
        eastMesh.setNormalsIndices(new int[]{0, 1, 2, 0, 2, 3});
        eastMesh.setTexCoordIndices(new int[]{0, 1, 2, 0, 2, 3});
        textureCoords = east.getImageTexCoords();
        eastMesh.setTextureCoords(new float[]{
            textureCoords.right(), textureCoords.top(), 0,
            textureCoords.right(), textureCoords.bottom(), 0,
            textureCoords.left(), textureCoords.bottom(), 0,
            textureCoords.left(), textureCoords.top(), 0});
        eastMesh.setNormals(new float[]{
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0});
        eastMesh.setVertices(new float[]{
            -skySize / 2, -skySize / 2, -skySize / 2,
            -skySize / 2, skySize / 2, -skySize / 2,
            -skySize / 2, skySize / 2, skySize / 2,
            -skySize / 2, -skySize / 2, skySize / 2});
        skyBoxGroup.addChild(eastMesh);

        PolygonMesh westMesh = new PolygonMesh("westMesh");
        westMesh.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
        westMesh.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
        westMesh.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
        westMesh.getMaterial(0).setE(new float[]{1, 1, 1, 0});
        westMesh.getMaterial(0).setNs(10);
        westMesh.getMaterial(0).setTexture(west);
        westMesh.setVertexIndices(new int[]{0, 1, 2, 0, 2, 3});
        westMesh.setNormalsIndices(new int[]{0, 1, 2, 0, 2, 3});
        westMesh.setTexCoordIndices(new int[]{0, 1, 2, 0, 2, 3});
        textureCoords = west.getImageTexCoords();
        westMesh.setTextureCoords(new float[]{
            textureCoords.left(), textureCoords.top(), 0,
            textureCoords.left(), textureCoords.bottom(), 0,
            textureCoords.right(), textureCoords.bottom(), 0,
            textureCoords.right(), textureCoords.top(), 0});
        westMesh.setNormals(new float[]{
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,
            1, 0, 0});
        westMesh.setVertices(new float[]{
            skySize / 2, -skySize / 2, -skySize / 2,
            skySize / 2, skySize / 2, -skySize / 2,
            skySize / 2, skySize / 2, skySize / 2,
            skySize / 2, -skySize / 2, skySize / 2});
        skyBoxGroup.addChild(westMesh);

        return skyBoxGroup;
    }

    /**
     * Build the SkyDome, i.e., set texture, light/material properties, vertex,
     * normals and textureCoords values.
     *
     * @param radius of the SkyDome.
     * @param texture the name and path of the texture for the SkyDome.
     * @return a gameObject which contains a mesh regarding the SkyDome data.
     */
    public TransformGroup buildSkyDome(float radius, String texture) {
        radius = frameworkOptions.get("useUnreachableSky")
                ? radius / 2 : radius;

        TransformGroup skyDomeGroup = new TransformGroup("skydome");

        localAssetManager.setTexture("skydome", texture);
        Texture skyDomeTexture = localAssetManager.getTexture("skydome");

        SkyDome skyDomeMesh = new SkyDome(radius, skyDomeTexture);
        skyDomeMesh.getMaterial(0).setKa(new float[]{1, 1, 1, 0});
        skyDomeMesh.getMaterial(0).setKs(new float[]{1, 1, 1, 0});
        skyDomeMesh.getMaterial(0).setKd(new float[]{1, 1, 1, 0});
        skyDomeMesh.getMaterial(0).setE(new float[]{1, 1, 1, 0});
        skyDomeMesh.getMaterial(0).setNs(10);

        skyDomeGroup.addChild(skyDomeMesh);

        return skyDomeGroup;
    }

    /**
     * Build a irregular smooth terrain floor.
     *
     * @param texture the name and path of the texture for the floor.
     * @param smoothFactor relates to the amount of extra points required to
     * generate for the surface smoothing.
     * @param terrainGenerator the terrain generator to use.
     * @param terrainSmoother the terrain generator smooth logic to use.
     * @return a gameObject which contains a mesh regarding the floor data.
     */
    public TransformGroup buildSmoothTerrain(String texture, int smoothFactor,
            AbstractTerrainGenerator terrainGenerator, AbstractSurface terrainSmoother) {
        TransformGroup terrainGroup = new TransformGroup("terrain");

        this.buildGeometry(terrainGenerator);

        terrainGenerator.smoothTerrain(smoothFactor, terrainSmoother);

        if (texture != null && localAssetManager.isTexturesNotNull()) {
            Texture floorTexture = localAssetManager.getTexture(texture);
            terrainGenerator.getMaterial(0).setTexture(floorTexture);
        }

        terrainGroup.addChild(terrainGenerator);
        return terrainGroup;
    }

    /**
     * Build a irregular terrain floor.
     *
     * @param texture the name and path of the texture for the floor.
     * @param terrainGenerator the terrain generator to use.
     * @return a gameObject which contains a mesh regarding the floor data.
     */
    public TransformGroup buildTerrain(String texture,
            AbstractTerrainGenerator terrainGenerator) {
        TransformGroup terrainGroup = new TransformGroup("terrain");

        this.buildGeometry(terrainGenerator);

        if (texture != null && localAssetManager.isTexturesNotNull()) {
            Texture floorTexture = localAssetManager.getTexture(texture);
            terrainGenerator.getMaterial(0).setTexture(floorTexture);
        }

        terrainGroup.addChild(terrainGenerator);
        return terrainGroup;
    }

    /**
     * Render all the gameObjects and their associated meshes in the managed
     * scene graph.
     *
     * @param gl
     */
    //TODO: for culling either change this method or render in GameObject and or mesh.  
    public void render(GL2 gl) {
        if (frameworkOptions.get("useRayTracer")) {
            if (localAssetManager.isTexturesNotNull()
                    && localAssetManager.texturesContains("rayTracerTexture")) {
                gl.glDisable(GL_LIGHTING);

                Texture texture = localAssetManager.getTexture("rayTracerTexture");

                texture.enable(gl);
                texture.bind(gl);
                gl.glPushMatrix();
                gl.glColor3f(1, 1, 1);
                gl.glBegin(GL_QUADS);
                {
                    gl.glTexCoord2f(0f, 0f);
                    gl.glVertex3f(0, 0, 0);
                    gl.glTexCoord2f(0f, 1f);
                    gl.glVertex3f(0, sceneSize, 0);
                    gl.glTexCoord2f(1f, 1f);
                    gl.glVertex3f(-sceneSize, sceneSize, 0);
                    gl.glTexCoord2f(1f, 0f);
                    gl.glVertex3f(-sceneSize, 0, 0);
                }
                gl.glEnd();
                gl.glPopMatrix();
                texture.disable(gl);

                gl.glEnable(GL_LIGHTING);
            }
        } else {
            if ((frameworkOptions.get("SkyBox")
                    || frameworkOptions.get("SkyDome"))
                    && this.sky != null) {
                this.setCamera2Use(this.localCameraId);
                if (frameworkOptions.get("useUnreachableSky")) {
                    Vector3D position = this.cameras.get(this.localCameraId).getPosition();
                    this.sky.setTranslation(new Vector3D(
                            position.getX(),
                            this.sky.getTranslation().getY(),
                            position.getZ()));
                }
                this.sky.render(gl);
            }

            if (coreOptions.get("Floor") && this.floor != null) {
                if (frameworkOptions.get("useUnreachableFloor")) {
                    Vector3D position = this.cameras.get(this.localCameraId).getPosition();
                    this.floor.setTranslation(new Vector3D(
                            position.getX(),
                            this.floor.getTranslation().getY(),
                            position.getZ()));
                }
                this.floor.render(gl);
            }

            if (frameworkOptions.get("showGeometries")) {
                this.rootNode.render(gl);
            }
        }
    }

    /**
     * Dispose all the game content.
     *
     * @param gl
     */
    //TODO: for culling either change this method or render in GameObject and or mesh.    
    public void dispose(GL2 gl) {
        localAssetManager.getTexturesValues().stream()
                .forEach(texture -> texture.destroy(gl));
        localAssetManager.dispose();

        this.gameObjects.stream()
                .forEach(go -> go.dispose(gl));
        this.gameObjects.clear();

        this.mutableObjects.stream()
                .forEach(mo -> mo.dispose(gl));
        this.mutableObjects.clear();

        this.immutableObjects.values().stream()
                .forEach(io -> io.dispose(gl));
        this.immutableObjects.clear();
    }

    /**
     * Update the default camera and the RayTracer camera.
     *
     * @param dt the amount of elapsed game time since the last frame.
     */
    public void updateCamera(float dt) {
        GameObject player = this.players.get(this.localPlayerId);
        Vector3D position = ZERO;
        Vector3D rotation = ZERO;

        if (player != null) {
            position = player.getPosition();
            rotation = player.getRotation();
        }

        //Update default camera.
        this.cameras.get(this.localCameraId).update(position, rotation, dt);

        //TODO: Remove if not necessary
        //Update RayTracer camera.
        //if (cameras.containsKey("PerspectiveRayTracer")) {
        //    cameras.get("PerspectiveRayTracer").update(position, rotation, dt);
        //}
    }

    /**
     * Change a player Id.
     *
     * @param oldId the player Id to change.
     * @param newId the new player Id.
     */
    public void updatePlayerName(String oldId, String newId) {
        GameObject player = this.players.remove(oldId);
        player.setId(newId);
        this.localPlayerId = newId;
        this.players.put(newId, player);
    }

    /**
     * Update all mutable objects position in the managed scene, if the mutable
     * object life span ends or if it hits some obstacle remove it from the
     * managed scene.
     *
     * @param dt the amount of elapsed game time since the last frame.
     */
    //TODO: parallelize, translate the SkyBox/SkyDome
    public void updateMutableObjects(float dt) {
        if (this.mutableObjects != null) {
            log.info(format("Number of mutableObjects %d", this.mutableObjects.size()));

            //Update mutable objects position
            Iterator<GameObject> mutableObjectIterator = this.getAllMutableObjects();
            log.info(Integer.toString(this.mutableObjects.size()));
            while (mutableObjectIterator.hasNext()) {
                GameObject mutableObject = mutableObjectIterator.next();
                if (mutableObject.getAttribute("health") > 80) {
                    mutableObject.setAttribute("health", mutableObject.getAttribute("health") - 1);

                    //Store original position              
                    Vector3D pastPosition = new Vector3D(mutableObject.getPastPosition().toArray());
                    //System.out.println(pastPosition);

                    //Vector3D newUpdate = translate(ZERO, mutableObject.getVelocity(), (float) mutableObject.getRotation().getY(), 0);
                    Vector3D newUpdate = translatePolar(pastPosition,
                            new Vector3D(0, mutableObject.getPosition().getY(), 0),
                            mutableObject.getVelocity(),
                            (float) mutableObject.getRotation().getY(),
                            0, -.98f, (nanoTime() - mutableObject.getT0())
                            * (float) 10e-9);
                    //System.out.println(newUpdate + "\n");

                    //perform position update
                    mutableObject.updatePosition(newUpdate);

                    //keep past position as original position
                    mutableObject.setPastPosition(pastPosition);
                    //mutableObject.updatePosition(new Vector3D(pastPosition.toArray());
                } else {
                    log.info("Mutable object was destroyed by time out!");
                    this.removeMutableObject(mutableObject);
                    mutableObjectIterator.remove();
                }
            }
//
//            //Test for all mutable objects if mutable object hit the scene bounds.
//            mutableObjectIterator = getAllMutableObjects();
//            while (mutableObjectIterator.hasNext()) {
//                GameObject mutableObject = mutableObjectIterator.next();
//                if (checkSceneBoundsCollision(mutableObject.getBoundingVolume(0))) {
//                    removeMutableObject(mutableObject);
//                    mutableObjectIterator.remove();
//                }
//            }
//            
            //Test for all mutable objects if mutable object was hit by mutable objects (such as projectiles).
            mutableObjectIterator = this.getAllMutableObjects();
            while (mutableObjectIterator.hasNext()) {
                GameObject mutableObject = mutableObjectIterator.next();
                int hits = this.checkMutableObjectMutableObjectCollision(mutableObject);
                if (hits > 0) {
                    int newHealth = (int) (mutableObject.getAttribute("health") - hits);
                    log.info(format("%s got hit. Health is now %d", mutableObject.getId(), newHealth));
                    if (newHealth <= 0) {
                        //TODO: Score points here
                        log.info(format("Mutable object %s was destroyed!", mutableObject.getId()));
                        this.removeMutableObject(mutableObject);
                        mutableObjectIterator.remove();
                    }
                }
            }

            //Test for all immutable object if immutable object was hit by mutable objects (such as projectiles).
            Iterator<GameObject> immutableObjectIterator = this.getAllImmutableObjects();
            while (immutableObjectIterator.hasNext()) {
                GameObject immutableObject = immutableObjectIterator.next();
                int hits = this.checkImmutableObjectMutableObjectCollision(immutableObject);
                if (hits > 0) {
                    int newHealth = (int) (immutableObject.getAttribute("health") - hits);
                    log.info(format("%s got hit. Health is now %d", immutableObject.getId(), newHealth));
                    if (newHealth <= 0) {
                        //TODO: Score points here
                        log.info(format("Immutable object %s was destroyed!", immutableObject.getId()));
                        this.removeImmutableObject(immutableObject);
                        immutableObjectIterator.remove();
                    }
                }
            }

            //Test for all players if player was hit by mutable objects (such as projectiles).
            Iterator<GameObject> playerIterator = this.getAllPlayers();
            while (playerIterator.hasNext()) {
                GameObject player = playerIterator.next();
                int hits = this.checkPlayerMutableObjectCollision(player);
                if (hits > 0) {
                    int newHealth = (int) (player.getAttribute("health") - hits);
                    log.info(format("%s got hit. Health is now %d", player.getId(), newHealth));
                    this.getPlayer(player.getId()).setAttribute("health", newHealth);
                    if (newHealth <= 0) {
                        //TODO: Score points here
                        log.info(format("Player %s died!", player.getId()));
                        this.getPlayer(player.getId()).setAttribute("health", 100);
                        //removePlayer(player);
                        //playerIterator.remove();
                    }
                }
            }

//        if (mutableObjects != null) {
//            ConcurrentLinkedQueue<GameObject> aux1 = new ConcurrentLinkedQueue<GameObject>();
//            final ConcurrentLinkedQueue<GameObject> aux2 = new ConcurrentLinkedQueue<GameObject>();
//            aux1.addAll(mutableObjects);
//            aux2.addAll(mutableObjects);
//            mutableObjects.clear();
//            for (final GameObject mutableObject : aux1) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (mutableObject.health < 40) {
//                            mutableObject.health = mutableObject.health + 1);
//                            Vector3D newUpdate = translatePolar(ZERO, 0.8f, (float) mutableObject.getRotation().getY(), 0);
//                            mutableObject.updatePosition(newUpdate);
//                        } else {
//                            //iterator.remove();
//                            aux2.remove(mutableObject);
//                            rootNode.removeChild(mutableObject);
//                            log.info(format("MutableObjects %s aged out.", mutableObject.getId()));
//                        }
//                    }
//                }).start();
//            }
//            mutableObjects.addAll(aux2);
//            aux1.clear();
//            aux2.clear();
//        }
        }
    }

    /**
     * Test for all mutable objects in the managed scene if any collides with a
     * GameObject from a given immutable object in the managed scene.
     *
     * @param immutableObject given immutable or mutable object, or player's
     * GameObject.
     * @return TRUE if collision occurs, FALSE otherwise.
     */
    public int checkImmutableObjectMutableObjectCollision(GameObject immutableObject) {
        return CollisionHandler.checkImmutableObjectMutableObjectCollision(immutableObject, this.rootNode, this.mutableObjects, sceneSize, this.ATTACK_DAMAGE);
    }

    /**
     * Test for all mutable objects in the managed scene if any collides with a
     * GameObject from a given mutable object in the managed scene.
     *
     * @param mutableObject given immutable or mutable object, or player's
     * GameObject.
     * @return TRUE if collision occurs, FALSE otherwise.
     */
    public int checkMutableObjectMutableObjectCollision(GameObject mutableObject) {
        return CollisionHandler.checkMutableObjectMutableObjectCollision(mutableObject, this.rootNode, this.mutableObjects, sceneSize, this.ATTACK_DAMAGE);
    }

    /**
     * Test if a given player collides with any of the immutable objects in the
     * managed scene.
     *
     * @param player given player.
     * @return TRUE if collision occurs, FALSE otherwise.
     */
    public boolean checkPlayerImmutableObjectCollision(GameObject player) {
        return CollisionHandler.checkPlayerImmutableObjectCollision(player, this.immutableObjects);
    }

    /**
     * Test for all mutable objects in the managed scene if any collides with a
     * GameObject from a given player in the managed scene.
     *
     * @param player given player's gameObject. GameObject. it hits an player.
     * @return TRUE if collision occurs, FALSE otherwise.
     */
    public int checkPlayerMutableObjectCollision(GameObject player) {
        return CollisionHandler.checkPlayerMutableObjectCollision(player, this.rootNode, this.mutableObjects, sceneSize, this.ATTACK_DAMAGE);
    }

    /**
     * Test if a given player collides with any of the players in the scene.
     *
     * @param player given player.
     * @return String of player with which collision occurs, NULL otherwise.
     */
    public String checkPlayerPlayerCollision(GameObject player) {
        return CollisionHandler.checkPlayerPlayerCollision(player, this.players);
    }

    /**
     * Test if a given bounding volume collides with the scene SkyBox/SkyDome
     * boundaries.
     *
     * @param boundingVolume of a given player.
     * @return TRUE if collision occurs, FALSE otherwise.
     */
    public boolean checkSceneBoundsCollision(AbstractBoundingVolume boundingVolume) {
        if (frameworkOptions.get("SkyBox")) {
            return frameworkOptions.get("useUnreachableSky")
                    ? checkSkyBoxCollision(boundingVolume, sceneSize / 2)
                    : checkSkyBoxCollision(boundingVolume, sceneSize);
        }
        if (frameworkOptions.get("SkyDome")) {
            return frameworkOptions.get("useUnreachableSky")
                    //? checkSkyBoxCollision(boundingVolume, sceneSize / 2.2f) //Since the floor is a square we could test instead for a boundig box.
                    ? checkSkyDomeCollision(boundingVolume, sceneSize / 2.2f)
                    : checkSkyDomeCollision(boundingVolume, sceneSize / 1.1f);
        }
        return false;
    }

    /**
     * Perform a RayTracer pass to the scene and generate the corresponding
     * texture.
     *
     * @param RAYTRACER_CANVAS_WIDTH
     * @param RAYTRACER_CANVAS_HEIGHT
     * @param samples
     */
    public void RayTrace(int RAYTRACER_CANVAS_WIDTH, int RAYTRACER_CANVAS_HEIGHT, int samples) {
        log.info("Performing ray tracing...");
        long beginTime = currentTimeMillis();

        log.info(format("Options %dx%d with %d samples", RAYTRACER_CANVAS_WIDTH, RAYTRACER_CANVAS_HEIGHT, samples * samples));

        //Image create
        ImageRenderer imageRenderer = new ImageRenderer();
        try {
            imageRenderer.renderImage(this, RAYTRACER_CANVAS_WIDTH, RAYTRACER_CANVAS_HEIGHT, samples);
        } catch (Exception ex) {
            log.severe(ex.getMessage());
        }
//                    //Image Write
//                    File f = new File("image.ppm");
//                    try {
//                        writeImage(scene.image, f);
//                    } catch (IOException ex) {
//                        log.severe(ex.getMessage());
//                    }
        long endTime = currentTimeMillis();
        log.info(format("Finished in %dms", endTime - beginTime));

        BufferedImage bufferedImage = new BufferedImage(RAYTRACER_CANVAS_WIDTH, RAYTRACER_CANVAS_HEIGHT, TYPE_INT_ARGB);
        for (int y = 0; y < this.image.length; y++) {
            for (int x = 0; x < this.image[0].length; x++) {
                int red = toInt(increaseBrightness(this.image[y][x].getX()));
                int green = toInt(increaseBrightness(this.image[y][x].getY()));
                int blue = toInt(increaseBrightness(this.image[y][x].getZ()));
                int rgb = new Color(red, green, blue).getRGB();
                bufferedImage.setRGB(x, this.image.length - y - 1, rgb);
            }
        }

        TextureData textureData = newTextureData(getDefault(), bufferedImage, true);
        if (localAssetManager.isTexturesNotNull()) {
            localAssetManager.setTexture("rayTracerTexture", textureData);
        }
    }

    /**
     * Add all game objects simplexes to the shapes list, apply affine
     * transforms and setup lights.
     */
    public void setupGeometries2Render() {
        //Setup lights
        this.lightSources.add(//new Sphere(1e5, new Vector3D(0, 1e5 + 40.8, 0),new Vector3D(400, 400, 400), new Vector3D(.75, .75, .75), DIFFUSE));
                new RayTracerSphere(1.5, new Vector3D(0, 34.3, 0),
                        new Vector3D(400, 400, 400), ZERO, this.DIFFUSE));
        this.shapes.addAll(this.lightSources);

        //Setup simplexes, i.e., triangles and 4 sided planes.
        this.shapes.addAll(asList(//Scene: radius, position, emission, color, material
                //new Sphere(1e5, new Vector3D(-1e5 - 49, 0, 0), ZERO, new Vector3D(.25, .25, .75), DIFFUSE),//Left
                //new Sphere(1e5, new Vector3D(1e5 + 49, 0, 0), ZERO, new Vector3D(.75, .25, .25), DIFFUSE),//Rght
                new RayTracerSphere(1e5, new Vector3D(-1e5 - 49, 0, 0), ZERO,
                        new Vector3D(.75, .25, .25), this.DIFFUSE),//Left
                new RayTracerSphere(1e5, new Vector3D(1e5 + 49, 0, 0), ZERO,
                        new Vector3D(.25, .25, .75), this.DIFFUSE),//Rght
                //new Sphere(1e5, new Vector3D(0, 0, -1e5 - 81.6), ZERO, new Vector3D(.75, .25, .75), DIFFUSE),//Back
                //new Sphere(1e5, new Vector3D(0, 0, 1e5 + 88.4), ZERO, ZERO, DIFFUSE),//Frnt
                //new Sphere(1e5, new Vector3D(0, -1e5 - 40.8, 0), ZERO, new Vector3D(.75, .75, .75), SPECULAR),//Botm
                //new Sphere(1e5, new Vector3D(0, -1e5 - 40.8, 0), ZERO, new Vector3D(.75, .75, .75), DIFFUSE),//Botm
                //new Sphere(1e5, new Vector3D(0, 1e5 + 40.8, 0), ZERO, new Vector3D(.75, .75, .75), DIFFUSE),//Top
                //new Sphere(16.5, new Vector3D(-23, -24.3, -24.6), ZERO, new Vector3D(.999, .999, .999), REFRACTIVE),//Glas
                //new Sphere(16.5, new Vector3D(-23, -24.3, -34.6), ZERO, new Vector3D(.999, .999, .999), SPECULAR),//Mirr
                //new Sphere(16.5, new Vector3D(-23, -24.3, -24.6), ZERO, new Vector3D(.999, .999, .999), DIFFUSE),//Diffuse
                //new Sphere(16.5, new Vector3D(23, -24.3, -3.6), ZERO, new Vector3D(.999, .999, .999), REFRACTIVE),//Glas
                //new Sphere(16.5, new Vector3D(23, -24.3, -3.6), ZERO, new Vector3D(.999, .999, .999), SPECULAR),//Mirr
                //new Sphere(16.5, new Vector3D(23, -24.3, -3.6), ZERO, new Vector3D(.999, .999, .999), DIFFUSE),//Diffuse
                //new Triangle(new Vector3D(-5, -30, -10), new Vector3D(-15, -18, -20), new Vector3D(-15, -40, -30),
                //        ZERO, new Vector3D(.75, .75, .75), REFRACTIVE),//Glas
                //new Triangle(new Vector3D(-5, -30, -10), new Vector3D(-15, -18, -20), new Vector3D(-15, -40, -30),
                //        ZERO, new Vector3D(.75, .75, .75), SPECULAR),//Mirr
                //new Triangle(new Vector3D(-5, -30, -30), new Vector3D(-15, -18, -20), new Vector3D(-15, -40, -30),
                //        ZERO, new Vector3D(.75, .75, .75), REFRACTIVE),//Glas
                //new Triangle(new Vector3D(-5, -30, -30), new Vector3D(-15, -18, -20), new Vector3D(-15, -40, -30),
                //        ZERO, new Vector3D(.75, .75, .75), SPECULAR),//Mirr
                //new Triangle(new Vector3D(-5, -30, -30), new Vector3D(-15, -18, -20), new Vector3D(-15, -40, -30),
                //        ZERO, new Vector3D(.75, .75, .75), DIFFUSE),//Diffuse
                //new Plane(new Vector3D(1, 0, 0), 50, ZERO, new Vector3D(.75, .25, .25), DIFFUSE),//Left
                //new Plane(new Vector3D(-1, 0, 0), 50, ZERO, new Vector3D(.25, .25, .75), DIFFUSE),//Rght
                new RayTracerPlane(new Vector3D(0, 0, 1), 80, ZERO,
                        new Vector3D(.75, .25, .75), this.DIFFUSE),//Back
                //new Plane(new Vector3D(0, 0,-1), 40, ZERO, ZERO, DIFFUSE),//Frnt
                //new Plane(new Vector3D(0, 1, 0), 40, ZERO, new Vector3D(.75, .75, .75), SPECULAR),//Botm   
                new RayTracerPlane(new Vector3D(0, 1, 0), 40, ZERO,
                        new Vector3D(.75, .75, .75), this.DIFFUSE),//Botm   
                new RayTracerPlane(new Vector3D(0, -1, 0), 40, ZERO,
                        new Vector3D(.75, .75, .75), this.DIFFUSE)//Top  
        ));

        //TODO: properly translate, rotate, scale.
        //Setup all triangles, and quads.        
        this.gameObjects.stream().forEach(go -> {
            Vector3D translation = go.getTranslation();
            Vector3D rotation = go.getRotation();
            Vector3D scaling = go.getScaling();
//                Vector3D translation = go.childTranslation();
//                Vector3D rotation = go.childRotation();
//                Vector3D scaling = go.childScaling();
//            System.out.println("Raytracer");
//            System.out.println(go.getId());
//            System.out.println(translation);
//            System.out.println(rotation);
//            System.out.println(scaling);
//            System.out.println("");
            //TODO: alter whem implemented for a compound mesh.
            go.getMeshes().get(0).getTriangles().stream().forEach(simplex -> {
                Vector3D v0 = this.applyAffineTransformations(simplex.v0, translation, rotation, scaling);
                Vector3D v1 = this.applyAffineTransformations(simplex.v1, translation, rotation, scaling);
                Vector3D v2 = this.applyAffineTransformations(simplex.v2, translation, rotation, scaling);
                this.shapes.add(new RayTracerTriangle(v0, v1, v2,
                        ZERO, new Vector3D(.75, .75, .75), this.DIFFUSE));
            });
        });
    }

    /**
     * For a provided Ray test if any of the shapes in the scene intersects said
     * ray.
     *
     * @param ray a provided Ray.
     * @return the intersection result of ray with all the shapes in the scene.
     */
    public IntersectionResult intersect(Ray ray) {
        IntersectionResult t = MISS;
        for (AbstractShape s : this.shapes) {
            IntersectionResult d = s.intersect(ray);
            if (d.isHit() && d.closerThan(t)) {
                t = d;
            }
        }
        return t;
    }

    /**
     * Apply affine transformations to a given vector.
     *
     * @param v a given vector.
     * @param translation the translation to apply to v.
     * @param rotation the rotation to apply to v.
     * @param scaling the scaling to apply to v.
     * @return v after the translation, rotation, and scale affine
     * transformations.
     */
    private Vector3D applyAffineTransformations(Vector3D v,
            Vector3D translation, Vector3D rotation, Vector3D scaling) {
//        System.out.println(translation);
//        System.out.println(rotation);
//        System.out.println(scaling);
//        translation = new Vector3D(0, 0, 0);
//        scaling = new Vector3D(5, 5, 5);
//
//        Vector3D result = new Vector3D(v.toArray());
        Vector3D result = translate(v, translation);
        if (rotation.getX() != 0) {
            result = rotateX(rotation.getX(), translation, result);
        }
        if (rotation.getY() != 0) {
            result = rotateY(rotation.getY(), translation, result);
        }
        if (rotation.getZ() != 0) {
            result = rotateZ(rotation.getZ(), translation, result);
        }
        return scale(result, scaling);
        //return scale(v, scaling);
    }
}
