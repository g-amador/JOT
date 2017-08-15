/*
 * This file is part of the JOT game engine physics extension toolkit component.
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
package jot.physics;

import static java.lang.String.format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.math.Distance.getDistance;
import static jot.math.GJK.BodiesIntersect;
import jot.math.geometry.bounding.AbstractBoundingVolume;
import static jot.util.ExtensionPhysicsOptions.extensionPhysicsOptions;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements broad/narrow phase collision detection among each pair
 * of available bounding volumes, and among multiple objects in the scene.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
//TODO: (Collisions Extras) implement other kinds of bounding volumes (i.e., ellipsoids, BVHs, CH, etc.)
//      implement other techniques for triangle/polygon based meshes.
public class CollisionHandler {

    static final Logger log = getLogger("CollisionHandler");

    /**
     * The last position in which a collision with another object in the scene
     * occurred.
     */
    public static Vector3D lastCollisionPosition;

    static {
        log.setLevel(OFF);
    }

    /**
     * Test for all mutable objects in the scene if any collides with a
     * transform group from a given immutable object in the scene.
     *
     * @param immutableObject given immutable or mutable object, or player's
     * GameObject.
     * @param rootNode the scene transformGroup.
     * @param mutableObjects a linked queue with all the mutable objects in the
     * scene.
     * @param sceneSize length of one sides of the SkyBox square or SkyDome
     * radius.
     * @param ATTACK_DAMAGE the value of damage each mutable object causes when
     * it hits an player.
     * @return TRUE if collision occurs, FALSE otherwise.
     */
    public static int checkImmutableObjectMutableObjectCollision(
            GameObject immutableObject,
            GameObject rootNode,
            ConcurrentLinkedQueue<GameObject> mutableObjects,
            float sceneSize, int ATTACK_DAMAGE) {
        if (extensionPhysicsOptions.get("useBroadPhaseCollisionDetection")) {
            Iterator<GameObject> it = mutableObjects.iterator();
            int mutableObjectHits = 0;
            while (it.hasNext()) {
                boolean didHit = false;
                GameObject mutableObject = it.next();
                if (immutableObject.getBoundingVolume(0).isCollide(
                        mutableObject.getBoundingVolume(0))) {
                    if (narrowPhaseCollisionDetection(
                            immutableObject, mutableObject)) {
                        log.info("Mutable object hit immutable object");
                        didHit = true;
                        mutableObjectHits += ATTACK_DAMAGE;
                    }
                }
                if (didHit) {
                    mutableObjects.remove(mutableObject);
                    rootNode.removeChild(mutableObject);
                    it.remove();
                }
            }
            return mutableObjectHits;
        }

        return 0;
    }

    /**
     * Test for all mutable objects in the scene if any collides with a
     * transform group from a given mutable object in the scene.
     *
     * @param mutableObject1 given immutable or mutable object, or player's
     * GameObject.
     * @param rootNode the scene transformGroup.
     * @param mutableObjects a linked queue with all the mutable objects in the
     * scene.
     * @param sceneSize length of one sides of the SkyBox square or SkyDome
     * radius.
     * @param ATTACK_DAMAGE the value of damage each mutable object causes when
     * it hits an player.
     * @return TRUE if collision occurs, FALSE otherwise.
     */
    public static int checkMutableObjectMutableObjectCollision(
            GameObject mutableObject1, GameObject rootNode,
            ConcurrentLinkedQueue<GameObject> mutableObjects,
            float sceneSize, int ATTACK_DAMAGE) {
        if (extensionPhysicsOptions.get("useBroadPhaseCollisionDetection")) {
            int mutableObjectHits = 0;
            Iterator<GameObject> it = mutableObjects.iterator();
            while (it.hasNext()) {
                boolean didHit = false;
                GameObject mutableObject2 = it.next();
                if (mutableObject1.getBoundingVolume(0).isCollide(
                        mutableObject2.getBoundingVolume(0))
                        && !mutableObject1.getId().equals(
                                mutableObject2.getId())) {
                    if (narrowPhaseCollisionDetection(
                            mutableObject1, mutableObject2)) {
                        log.info("Mutable object hit mutable object");
                        didHit = true;
                        mutableObjectHits += ATTACK_DAMAGE;
                    }
                }
                if (didHit) {
                    //mutableObjects.remove(mutableObject1);
                    //rootNode.removeChild(mutableObject1);
                    mutableObjects.remove(mutableObject2);
                    rootNode.removeChild(mutableObject2);
                    it.remove();
                }
            }
            return mutableObjectHits;
        }

        return 0;
    }

    /**
     * Test if a player collides with any of the immutable objects in the scene.
     *
     * @param player a given player.
     * @param immutableObjects an arrayList with all obstacles in the scene.
     * @return TRUE if collision occurs, FALSE otherwise.
     */
    public static boolean checkPlayerImmutableObjectCollision(
            GameObject player,
            ConcurrentHashMap<String, GameObject> immutableObjects) {
        if (extensionPhysicsOptions.get("useBroadPhaseCollisionDetection")) {
            Collection<GameObject> activeImmutableObjects = immutableObjects.values();
            for (GameObject activeImmutableObject : activeImmutableObjects) {
                //activeImmutableObject.getBoundingVolume(0).setRenderBoundingVolume(true);
                if (player.getBoundingVolume(0).isCollide(
                        activeImmutableObject.getBoundingVolume(0))) {
                    if (narrowPhaseCollisionDetection(
                            player, activeImmutableObject)) {
                        //TODO: get position in which narrowPhase collision 
                        //happens. Following only works for broadphase
                        lastCollisionPosition = new Vector3D(
                                activeImmutableObject.getPosition().toArray());
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    /**
     * Test for all mutable objects in the scene if any collides with a
     * transform group from a given player in the scene.
     *
     * @param player given player's transformGroup. GameObject.
     * @param rootNode the scene transformGroup.
     * @param mutableObjects a linked queue with all the mutable objects in the
     * scene.
     * @param sceneSize length of one sides of the SkyBox square or SkyDome
     * radius.
     * @param ATTACK_DAMAGE the value of damage each mutable object causes when
     * it hits an player.
     * @return TRUE if collision occurs, FALSE otherwise.
     */
    public static int checkPlayerMutableObjectCollision(
            GameObject player,
            GameObject rootNode,
            ConcurrentLinkedQueue<GameObject> mutableObjects,
            float sceneSize, int ATTACK_DAMAGE) {
        if (extensionPhysicsOptions.get("useBroadPhaseCollisionDetection")) {
            Iterator<GameObject> it = mutableObjects.iterator();
            int mutableObjectHits = 0;
            while (it.hasNext()) {
                boolean didHit = false;
                GameObject mutableObject = it.next();
                if (player.getBoundingVolume(0).isCollide(
                        mutableObject.getBoundingVolume(0))
                        && !mutableObject.getId().startsWith(player.getId())) {
                    if (narrowPhaseCollisionDetection(player, mutableObject)) {
                        log.info(format("Mutable object hit player %s", player.getId()));
                        didHit = true;
                        mutableObjectHits += ATTACK_DAMAGE;
                    }
                }
                if (didHit) {
                    mutableObjects.remove(mutableObject);
                    rootNode.removeChild(mutableObject);
                    it.remove();
                }
            }
            return mutableObjectHits;
        }

        return 0;
    }

    /**
     * Test if a given player collides with any of the players in the scene.
     *
     * @param player given player.
     * @param players an hash map of with all the players in the scene.
     * @return String of player with which collision occurs, NULL otherwise.
     */
    public static String checkPlayerPlayerCollision(GameObject player,
            ConcurrentHashMap<String, GameObject> players) {
        if (extensionPhysicsOptions.get("useBroadPhaseCollisionDetection")) {
            Collection<GameObject> activePlayers = players.values();
            for (GameObject activePlayer : activePlayers) {
                if (activePlayer.getId().equals(player.getId())) {
                    continue;
                }
                //activePlayer.getBoundingVolume(0).setRenderBoundingVolume(true);
                if (player.getBoundingVolume(0).isCollide(
                        activePlayer.getBoundingVolume(0))) {
                    return narrowPhaseCollisionDetection(player, activePlayer)
                            ? activePlayer.getId() : null;
                }
            }
            return null;
        }
        return null;
    }

    /**
     * Test if a given bounding volume collides with the SkyBox boundaries.
     *
     * @param boundingVolume of a given player.
     * @param sceneSize length of one sides of the SkyBox.
     * @return TRUE if collision occurs, FALSE otherwise.
     */
    public static boolean checkSkyBoxCollision(
            AbstractBoundingVolume boundingVolume, float sceneSize) {
        if (extensionPhysicsOptions.get("useBroadPhaseCollisionDetection")) {
            Vector3D baryCenter = boundingVolume.barycentre();
            return baryCenter.getX() - 2 <= -sceneSize / 2
                    || baryCenter.getX() + 2 >= sceneSize / 2
                    || baryCenter.getY() - 2 <= -sceneSize / 2
                    || baryCenter.getY() + 2 >= sceneSize / 2
                    || baryCenter.getZ() - 2 <= -sceneSize / 2
                    || baryCenter.getZ() + 2 >= sceneSize / 2;
        }
        return false;
    }

    /**
     * Test if a given bounding volume collides with the SkyDome boundaries.
     *
     * @param boundingVolume of a given player.
     * @param sceneSize diameter of the SkyDome.
     * @return TRUE if collision occurs, FALSE otherwise.
     */
    public static boolean checkSkyDomeCollision(
            AbstractBoundingVolume boundingVolume, float sceneSize) {
        if (extensionPhysicsOptions.get("useBroadPhaseCollisionDetection")) {
            Vector3D baryCenter = boundingVolume.barycentre();
            return getDistance(baryCenter, ZERO) >= sceneSize / 2;
        }
        return false;
    }

    /**
     * Method to test narrow phase collision between two Meshes using
     * Gilbert–Johnson–Keerthi, as explained at the link:
     * http://www.codezealot.org/archives/88
     * http://lewisresearchgroup.wikidot.com/gjk-algorithm
     *
     * @param t1 the first GameObject that contains the mesh to test narrow
     * phase collision with m2.
     * @param t2 the second GameObject that contains the mesh to test narrow
     * phase collision with m1.
     * @return TRUE if broad phase collision occurs, FALSE otherwise.
     */
    private static boolean narrowPhaseCollisionDetection(
            GameObject t1, GameObject t2) {
        if (extensionPhysicsOptions.get("useNarrowPhaseCollisionDetection")) {
            //FIXME: missing aphine transformations!!!!!

//        ArrayList<ArrayList<Vector3D>> simplexes1 = t1.getMesh().getSimplexes();
//        ArrayList<ArrayList<Vector3D>> simplexes2 = t2.getMesh().getSimplexes();
//
//        System.out.println("testing: " + simplexes1.size());
//        System.out.println("against: " + simplexes2.size());
//        System.out.println("");
//
//        for (ArrayList<Vector3D> simplex1 : simplexes1) {
//            for (ArrayList<Vector3D> simplex2 : simplexes2) {
            ArrayList<Vector3D> simplex1 = new ArrayList<>();
            simplex1.add(new Vector3D(0, 0, 0));
            simplex1.add(new Vector3D(1, 0, 0));
            simplex1.add(new Vector3D(1, 1, 0));
            simplex1.add(new Vector3D(0, 1, 0));
            simplex1.add(new Vector3D(0, 0, -1));
            simplex1.add(new Vector3D(1, 0, -1));
            simplex1.add(new Vector3D(1, 1, -1));
            simplex1.add(new Vector3D(0, 1, -1));

            ArrayList<Vector3D> simplex2 = new ArrayList<>();
            simplex2.add(new Vector3D(0.5, 0, 0));
            simplex2.add(new Vector3D(1.5, 0, 0));
            simplex2.add(new Vector3D(1.5, 1, 0));
            simplex2.add(new Vector3D(0.5, 1, 0));
            simplex2.add(new Vector3D(0.5, 0, -1));
            simplex2.add(new Vector3D(1.5, 0, -1));
            simplex2.add(new Vector3D(1.5, 1, -1));
            simplex2.add(new Vector3D(0.5, 1, -1));
            if (BodiesIntersect(simplex1, simplex2)) {
                //System.out.println("Narrow phase collision detected!!!");
                log.info("Narrow phase collision detected!!!");
                return true;
            }
//            }
//        }
            return false;
        }
        return true;
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private CollisionHandler() {
    }
}
