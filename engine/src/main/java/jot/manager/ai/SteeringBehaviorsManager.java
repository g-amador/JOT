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
package jot.manager.ai;

import com.jogamp.opengl.GL2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.ai.Agent;
import jot.ai.steeringBehaviours.AbstractSteeringBehavior;
import jot.ai.steeringBehaviours.Alignment;
import jot.ai.steeringBehaviours.Arrive;
import jot.ai.steeringBehaviours.Cohesion;
import jot.ai.steeringBehaviours.Evade;
import jot.ai.steeringBehaviours.Flee;
import jot.ai.steeringBehaviours.Hide;
import jot.ai.steeringBehaviours.Interpose;
import jot.ai.steeringBehaviours.OffsetPursuit;
import jot.ai.steeringBehaviours.PathFollow;
import jot.ai.steeringBehaviours.Pursuit;
import jot.ai.steeringBehaviours.Seek;
import jot.ai.steeringBehaviours.Separation;
import jot.ai.steeringBehaviours.Wander;
import jot.ai.steeringBehaviours.WanderArea;
import jot.ai.steeringBehaviours.WanderCurve;
import jot.manager.SceneManager;
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.ALIGNMENT;
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
import static jot.manager.ai.AbstractSteeringBehaviorsManager.SteeringBehavior.values;
import jot.math.geometry.bounding.AbstractBoundingVolume;
import static jot.physics.Kinematics.translate;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import static jot.util.FrameworkOptions.frameworkOptions;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements Craig Reynolds steering behaviors.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class SteeringBehaviorsManager extends AbstractSteeringBehaviorsManager {

    private static final Logger log = getLogger("SteeringBehaviors");

    static {
        log.setLevel(OFF);
    }

    /**
     * The Steering behaviors allowed to use and states.
     */
    protected HashMap<SteeringBehavior, Boolean> steeringBehaviorsStates;
    protected HashMap<SteeringBehavior, AbstractSteeringBehavior> steeringBehaviors;

    /**
     * Tagged neighbor entities, i.e., within the radius of some entity.
     */
    protected ArrayList<Agent> taggetNeighborEntities;

    /**
     * A callback to the scene manager.
     */
    protected SceneManager callback;

    /**
     * Neighbor entities.
     */
    protected Iterator<GameObject> entities;

    /**
     * Entity in formation corresponding offset.
     */
    protected Vector3D entityOffset;

    /**
     * Obstacles.
     */
    protected Iterator<GameObject> obstacles;

    private float targetInAttackRangeThreshold;   //Minimal distance after which the player/agent starts shooting the target     

    /**
     * Constructor that sets the speed at which a A.I. controlled agent moves,
     * the threshold for a agent to attack when near another target agent in
     * some behaviors, and the absolute size value of one of the sides of the
     * square that defines the floor, i.e., floor as size*size area length of
     * the game
     *
     * @param sceneSize absolute size value of one of the sides of the square
     * that defines the floor, i.e., floor as size*size area.
     */
    public SteeringBehaviorsManager(float sceneSize) {
        //The default setup to use
        extensionAIOptions.put("useSteeringBehaviors", true);
        //frameworkOptions.put("useAI", false);
        frameworkOptions.put("useAI", true);

        this.targetInAttackRangeThreshold = sceneSize / 6;

        this.entities = null;
        this.taggetNeighborEntities = new ArrayList<>();
        this.steeringBehaviorsStates = new HashMap<>();
        this.steeringBehaviors = new HashMap<>();
        for (SteeringBehavior logic : values()) {
            switch (logic) {
                case SEEK:
                    log.info("Seek A.I. logic activated!");
                    Seek seekSteeringBehavior = new Seek();
                    seekSteeringBehavior.setDistance2TargetThreshold(sceneSize / 10);
                    this.steeringBehaviors.put(logic, seekSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case FLEE:
                    log.info("Flee A.I. logic activated!");
                    Flee fleeSteeringBehavior = new Flee();
                    fleeSteeringBehavior.setDistance2TargetThreshold(sceneSize / 5);
                    this.steeringBehaviors.put(logic, fleeSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case ARRIVE:
                    log.info("Arrive A.I. logic activated!");
                    Arrive arriveSteeringBehavior = new Arrive();
                    arriveSteeringBehavior.setDistance2TargetThreshold(sceneSize / 10);
                    arriveSteeringBehavior.setDeceleration(5);
                    this.steeringBehaviors.put(logic, arriveSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case PURSUIT:
                    log.info("Pursuit A.I. logic activated!");
                    Pursuit pursuitSteeringBehavior = new Pursuit();
                    pursuitSteeringBehavior.setDistance2TargetThreshold(sceneSize / 10);
                    this.steeringBehaviors.put(logic, pursuitSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case EVADE:
                    log.info("Evade A.I. logic activated!");
                    Evade evadeSteeringBehavior = new Evade();
                    evadeSteeringBehavior.setDistance2TargetThreshold(sceneSize / 5);
                    this.steeringBehaviors.put(logic, evadeSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case WANDER:
                    log.info("Wander A.I. logic activated!");
                    Wander wanderSteeringBehavior = new Wander();
                    wanderSteeringBehavior.setDistance2TargetThreshold(1);
                    wanderSteeringBehavior.setSceneSize(sceneSize);
                    wanderSteeringBehavior.setWanderTime(25);
                    wanderSteeringBehavior.setWanderDistance(30);
                    wanderSteeringBehavior.setWanderJitter(8);
                    wanderSteeringBehavior.setWanderRadius(10);
                    this.steeringBehaviors.put(logic, wanderSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case WANDER_AREA:
                    log.info("Wander Area A.I. logic activated!");
                    WanderArea wanderAreaSteeringBehavior = new WanderArea();
                    wanderAreaSteeringBehavior.setDistance2TargetThreshold(1);
                    wanderAreaSteeringBehavior.setSceneSize(sceneSize);
                    wanderAreaSteeringBehavior.setWanderTime(240);
                    this.steeringBehaviors.put(logic, wanderAreaSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case WANDER_CURVE:
                    log.info("Wander Curve A.I. logic activated!");
                    WanderCurve wanderCurveSteeringBehavior = new WanderCurve();
                    wanderCurveSteeringBehavior.setDistance2TargetThreshold(1);
                    wanderCurveSteeringBehavior.setSceneSize(sceneSize);
                    wanderCurveSteeringBehavior.setWanderTime(25);
                    wanderCurveSteeringBehavior.setWanderJitter(90);
                    this.steeringBehaviors.put(logic, wanderCurveSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case OBSTACLE_AVOIDANCE:
                    //TODO: OBSTACLE_AVOIDANCE
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case WALL_AVOIDANCE:
                    //TODO: WALL_AVOIDANCE
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case INTERPOSE:
                    log.info("Interpose A.I. logic activated!");
                    Interpose interposeSteeringBehavior = new Interpose();
                    interposeSteeringBehavior.setDistance2TargetThreshold(sceneSize / 10);
                    interposeSteeringBehavior.setDeceleration(1);
                    this.steeringBehaviors.put(logic, interposeSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case HIDE:
                    log.info("Hide A.I. logic activated!");
                    Hide hideSteeringBehavior = new Hide();
                    hideSteeringBehavior.setSceneSize(sceneSize);
                    this.steeringBehaviors.put(logic, hideSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case PATH_FOLLOW:
                    log.info("Path Follow A.I. logic activated!");
                    PathFollow pathFollowSteeringBehavior = new PathFollow();
                    pathFollowSteeringBehavior.setDistance2TargetThreshold(1);
                    pathFollowSteeringBehavior.setDeceleration(5);
                    this.steeringBehaviors.put(logic, pathFollowSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case OFFSET_PURSUIT:
                    log.info("Offset pursuit A.I. logic activated!");
                    OffsetPursuit offsetPursuitSteeringBehavior = new OffsetPursuit();
                    offsetPursuitSteeringBehavior.setDistance2TargetThreshold(1);
                    this.steeringBehaviors.put(logic, offsetPursuitSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case SEPARATION:
                    log.info("Separation A.I. logic activated!");
                    Separation separationSteeringBehavior = new Separation();
                    this.steeringBehaviors.put(logic, separationSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case ALIGNMENT:
                    log.info("Alignment A.I. logic activated!");
                    Alignment alignmentSteeringBehavior = new Alignment();
                    this.steeringBehaviors.put(logic, alignmentSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
                case COHESION:
                    log.info("Cohesion A.I. logic activated!");
                    Cohesion cohesionSteeringBehavior = new Cohesion();
                    //cohesionSteeringBehavior.setDistance2TargetThreshold(sceneSize / 10);
                    this.steeringBehaviors.put(logic, cohesionSteeringBehavior);
                    this.steeringBehaviorsStates.put(logic, false);
                    break;
            }
        }

        if (extensionAIOptions.get("useSteeringBehaviorsDebug")) {
            log.setLevel(INFO);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTargetInAttackRangeThreshold(float threshold) {
        this.targetInAttackRangeThreshold = threshold;
    }

    /**
     * Set the wander distance for Craig Reynolds wander steering behavior.
     *
     * @param wanderDistance
     */
    public void setWanderDistance(float wanderDistance) {
        if (this.steeringBehaviors.containsKey(WANDER)) {
            ((Wander) this.steeringBehaviors.get(WANDER)).setWanderDistance(wanderDistance);
        }
    }

    /**
     * Set the wander jitter for Craig Reynolds wander steering behavior.
     *
     * @param wanderJitter
     */
    public void setWanderJitter(float wanderJitter) {
        if (this.steeringBehaviors.containsKey(WANDER)) {
            ((Wander) this.steeringBehaviors.get(WANDER)).setWanderJitter(wanderJitter);
        }
        if (this.steeringBehaviors.containsKey(WANDER_CURVE)) {
            ((WanderCurve) this.steeringBehaviors.get(WANDER_CURVE)).setWanderJitter(wanderJitter);
        }
    }

    /**
     * Set the wander radius for Craig Reynolds wander steering behavior.
     *
     * @param wanderRadius
     */
    public void setWanderRadius(float wanderRadius) {
        if (this.steeringBehaviors.containsKey(WANDER)) {
            ((Wander) this.steeringBehaviors.get(WANDER)).setWanderRadius(wanderRadius);
        }
    }

    /**
     * Set the time until choosing the next wander direction.
     *
     * @param wanderTime until choosing the next wander direction.
     */
    public void setWanderTime(long wanderTime) {
        if (this.steeringBehaviors.containsKey(WANDER_AREA)) {
            ((WanderArea) this.steeringBehaviors.get(WANDER_AREA)).setWanderTime(wanderTime);
        }
        if (this.steeringBehaviors.containsKey(WANDER_CURVE)) {
            ((WanderCurve) this.steeringBehaviors.get(WANDER_CURVE)).setWanderTime(wanderTime);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPrimaryAgent(GameObject primaryAgent) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            super.setPrimaryAgent(primaryAgent);
            for (SteeringBehavior logic : values()) {
                if (this.steeringBehaviorsStates.get(logic)) { //TODO: remove when all implemented
                    this.steeringBehaviors.get(logic).setPrimaryAgent(this.primaryAgent);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPrimaryGoal(GameObject primaryGoal) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            super.setPrimaryGoal(primaryGoal);
            for (SteeringBehavior logic : values()) {
                if (!logic.toString().startsWith("WANDER")
                        && !logic.toString().startsWith("PATH_FOLLOW")) {
                    if (this.steeringBehaviorsStates.get(logic)) { //TODO: remove when all implemented                        
                        this.steeringBehaviors.get(logic).setPrimaryGoal(this.primaryGoal);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSecondaryGoal(GameObject secondaryGoal) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.secondaryGoal = new Agent(
                    secondaryGoal.getId(),
                    secondaryGoal.getMaxSpeed(),
                    secondaryGoal.getBoundingRadius(),
                    new Vector3D(secondaryGoal.getPosition().toArray()),
                    new Vector3D(secondaryGoal.getRotation().toArray()),
                    new Vector3D(secondaryGoal.getVelocity().toArray()));
            this.setAgentLog(this.secondaryGoal);
            ((Interpose) this.steeringBehaviors.get(INTERPOSE)).setSecondaryGoal(this.secondaryGoal);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEntities(Iterator<GameObject> entities) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.entities = entities;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEntityOffset(Vector3D offset) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.entityOffset = offset;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObstacles(Iterator<GameObject> obstacles) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.obstacles = obstacles;
            ((Hide) this.steeringBehaviors.get(HIDE)).setObstacles(this.obstacles);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D[] getPath() {
        return ((PathFollow) this.steeringBehaviors.get(PATH_FOLLOW)).getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPath(Vector3D[] path) {
        ((PathFollow) this.steeringBehaviors.get(PATH_FOLLOW)).setPath(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPathColor(Vector3D color) {
        ((PathFollow) this.steeringBehaviors.get(PATH_FOLLOW)).setPathColor(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPathLineWitdh(float lineWitdh) {
        ((PathFollow) this.steeringBehaviors.get(PATH_FOLLOW)).setPathLineWitdh(lineWitdh);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttacking(long timeStamp, long lastTimeStamp) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            //boolean attack = false;
            //
            ////only attack, if target in attack range, for seek, flee, arrive, pursuit, and evade logics
            //if (isSteeringBehaviorOn(SEEK)
            //        || isSteeringBehaviorOn(FLEE)
            //        || isSteeringBehaviorOn(PURSUIT)
            //        || isSteeringBehaviorOn(EVADE)) {
            //    attack = targetInAttackRange() && timeStamp > (lastTimeStamp + 500);
            //}            
            //return attack;

            return this.targetInAttackRange() && timeStamp > (lastTimeStamp + 500); //attack;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSteeringBehaviorOn() {
        return this.steeringBehaviorsStates.values().stream()
                .anyMatch(on -> on == true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSteeringBehaviorOn(SteeringBehavior logic) {
        return this.steeringBehaviorsStates.get(logic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void SteeringBehaviorOn(SteeringBehavior logic) {
        this.steeringBehaviorsStates.put(logic, true);
        //frameworkOptions.put("useAI", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void SteeringBehaviorOff(SteeringBehavior logic) {
        this.steeringBehaviorsStates.put(logic, false);
        //for (boolean on : steeringBehaviorsStates.values()) {
        //    if (on) {
        //        return;
        //    }
        //}
        //frameworkOptions.put("useAI", false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renderAI(GL2 gl) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            for (SteeringBehavior logic : values()) {
                if (this.isSteeringBehaviorOn(logic) && this.primaryAgent != null) {
                    if (!(this.isSteeringBehaviorOn(OFFSET_PURSUIT)
                            || this.isSteeringBehaviorOn(SEPARATION)
                            || this.isSteeringBehaviorOn(ALIGNMENT)
                            || this.isSteeringBehaviorOn(COHESION))) {
                        this.steeringBehaviors.get(logic).render(gl);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(GL2 gl) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            for (SteeringBehavior logic : values()) {
                if (this.isSteeringBehaviorOn(logic)) {
                    this.steeringBehaviors.get(logic).dispose(gl);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(Object sceneManager, float dt) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.callback = (SceneManager) sceneManager;
            this.setEntities(this.callback.getAllPlayers());
            this.setObstacles(this.callback.getAllImmutableObjects());

            if (this.isSteeringBehaviorOn() && null != this.primaryAgent) {
                GameObject primaryAgentPlayer = this.callback.getPlayer(this.primaryAgent.getId());
                GameObject primaryGoalPlayer = null;
                if (this.primaryGoal != null) {
                    primaryGoalPlayer = this.callback.getPlayer(this.primaryGoal.getId());
                }
                GameObject secondaryGoalPlayer = null;
                if (this.isSteeringBehaviorOn(INTERPOSE)) {
                    secondaryGoalPlayer = this.callback.getPlayer(this.secondaryGoal.getId());
                }

                //update the primaryAgent, primaryGoal, and secondaryGoal.
                this.setPrimaryAgent(primaryAgentPlayer);
                if (null != primaryGoalPlayer) {
                    this.setPrimaryGoal(primaryGoalPlayer);
                }
                if (null != secondaryGoalPlayer) {
                    this.setSecondaryGoal(secondaryGoalPlayer);
                }

                /*
                System.out.println(this.primaryAgent.getId());
                //System.out.println(primaryAgentPlayer.getPosition());
                System.out.println(primaryAgentPlayer.getRotation());
                //System.out.println(primaryAgentPlayer.getVelocity());
                 */
                //use the activated A.I. logics to update the primary agent
                Vector3D velocity = ZERO;
                Vector3D rotation = ZERO;
                Vector3D position = ZERO;

//            if (!(isSteeringBehaviorOn(OFFSET_PURSUIT) || isSteeringBehaviorOn(SEPARATION)
//                    || isSteeringBehaviorOn(ALIGNMENT) || isSteeringBehaviorOn(COHESION))) {            
                for (SteeringBehavior logic : values()) {
                    if (this.isSteeringBehaviorOn(logic)) {
                        //System.out.println(primaryAgent.getId());
                        //System.out.println(logic);
                        //System.out.println(steeringBehaviors.get(logic).getPrimaryAgent().getPosition());
                        //System.out.println(steeringBehaviors.get(logic).getPrimaryAgent().getVelocity());
                        //System.out.println(steeringBehaviors.get(logic).getPrimaryAgent().getRotation());
                        velocity = this.steeringBehaviors.get(logic).steer();
                        velocity = new Vector3D(velocity.getX(), 0, velocity.getZ());
                        rotation = this.steeringBehaviors.get(logic).getPrimaryAgent().getRotation();
                        //System.out.println(steeringBehaviors.get(logic).getPrimaryAgent().getPosition());
                        //System.out.println(steeringBehaviors.get(logic).getPrimaryAgent().getVelocity());
                        //System.out.println(steeringBehaviors.get(logic).getPrimaryAgent().getRotation());
                        //System.out.println(velocity);
                        //System.out.println(rotation);
                    }
                }

                //make sure vehicle does not exceed maximum velocity
                if (velocity.getNorm() > this.primaryAgent.getMaxSpeed()) {
                    velocity = velocity.equals(ZERO)
                            ? velocity
                            : velocity.normalize().scalarMultiply(this.primaryAgent.getMaxSpeed());
                }

                //FIXME: a wierd fickering effect, something not right probably with the timestep.
                //Using Cartesian Coordinates.
                position = translate(position, velocity, dt);
                //Using Polar Coordinates.
                //Vector3D velocitySignum = new Vector3D(signum(velocity.getX()), 0, signum(velocity.getZ()));
                //velocity = new Vector3D(abs(velocity.getX()), 0, abs(velocity.getZ()));
                //position = translatePolar(position, velocity, (float) rotation.getY(), 180, dt);
                //velocity = new Vector3D(
                //        velocitySignum.getX() * velocity.getX(),
                //        0,
                //        velocitySignum.getZ() * velocity.getZ());

//                    GameObject primaryAgentPlayer = callback.getPlayer(primaryAgent.getId());
//                    GameObject primaryGoalPlayer = null;
//                    if (primaryGoal != null) {
//                        primaryGoalPlayer = callback.getPlayer(primaryGoal.getId());
//                    }
//                    GameObject secondaryGoalPlayer = null;
//                    if (isSteeringBehaviorOn(INTERPOSE)) {
//                        secondaryGoalPlayer = callback.getPlayer(secondaryGoal.getId());
//                    }
                //System.out.println(primaryAgentPlayer.getPosition() + " " + primaryAgentPlayer.getVelocity() + " " + primaryAgentPlayer.getRotation());
                //System.out.println(primaryGoalPlayer.getPosition() + " " + primaryGoalPlayer.getVelocity() + " " + primaryGoalPlayer.getRotation());
                //if (null != secondaryGoalPlayer) {
                //System.out.println(secondaryGoalPlayer.getPosition() + " " + secondaryGoalPlayer.getVelocity() + " " + secondaryGoalPlayer.getRotation());
                //}
                //Translate a copy of the bounding volume of the primaryAgentPlayer, 
                //as determined by the stearing behavior, into its future position.            
                AbstractBoundingVolume boundingVolume = primaryAgentPlayer.getBoundingVolumeCopy(0);
                boundingVolume.min = boundingVolume.min.add(position);
                boundingVolume.max = boundingVolume.max.add(position);

                //Create a temporary game object and set its bounding volume to the prior copy.
                GameObject temp = new GameObject(this.primaryAgent.getId());
                temp.setBoundingVolume(0, boundingVolume);

                //Check if the temporary game object collides with another player in the scene.
                String primaryAgentPlayerCrash = this.callback.checkPlayerPlayerCollision(temp);

                //If the temporary game object/bounding volume does not collide 
                //with anything in the scene update its position            
                if (!this.callback.checkSceneBoundsCollision(boundingVolume)
                        && !this.callback.checkPlayerImmutableObjectCollision(temp)
                        && (primaryAgentPlayerCrash == null
                        || this.primaryAgent.getId().equals(primaryAgentPlayerCrash))) {
                    primaryAgentPlayer.updatePosition(position);
                    primaryAgentPlayer.updateRotationY((float) rotation.getY());
                    primaryAgentPlayer.setVelocity(velocity);
                }

                /*
                //System.out.println(primaryAgentPlayer.getPosition());
                System.out.println(primaryAgentPlayer.getRotation());
                //System.out.println(primaryAgentPlayer.getVelocity());
                System.out.println("");
                 */
                //update the primaryAgent, primaryGoal, and secondaryGoal.
//                    setPrimaryAgent(primaryAgentPlayer);
//                    if (null != primaryGoalPlayer) {
//                        setPrimaryGoal(primaryGoalPlayer);
//                    }
//                    if (null != secondaryGoalPlayer) {
//                        setSecondaryGoal(secondaryGoalPlayer);
//                    }
            }
//            } else {
//                //if (isSteeringBehaviorOn(OFFSET_PURSUIT) || isSteeringBehaviorOn(SEPARATION)
//                //        || isSteeringBehaviorOn(ALIGNMENT) || isSteeringBehaviorOn(COHESION)) {
//                //update all other entities for flocking or offset_pursuit.                               
//                while (entities.hasNext()) {
//                    GameObject entity = entities.next();
//                    //System.out.println(entity.getId());
//
//                    GameObject primaryAgentPlayer = callback.getPlayer(entity.getId());
//                    GameObject primaryGoalPlayer = callback.getPlayer(primaryGoal.getId());
//
//                    setPrimaryAgent(primaryAgentPlayer);
//                    setPrimaryGoal(primaryGoalPlayer);
//
//                    velocity = rotation = ZERO;
//
//                    if (!entity.getId().equals(primaryGoalPlayer.getId())) {
//                        if (isSteeringBehaviorOn(OFFSET_PURSUIT)) {
//                            ((OffsetPursuit) steeringBehaviors.get(OFFSET_PURSUIT))
//                                    .setOffset(entitiesOffsets.get(entity.getId()));
//                            velocity = steeringBehaviors.get(OFFSET_PURSUIT).steer();
//                            rotation = primaryGoalPlayer.getRotation();
//                        }
//
//                        //FIXME: SOMETHING STILL WRONG WITH THE FLOCKING BEHAVIOR!
//                        if (isSteeringBehaviorOn(SEPARATION)
//                                || isSteeringBehaviorOn(ALIGNMENT)
//                                || isSteeringBehaviorOn(COHESION)) {
//                            tagNeighbors(entity, callback.getAllPlayers(),
//                                    entity.getBoundingRadius());
//                            //System.out.println(taggetNeighborEntities.size());
//                        }
//
//                        if (!taggetNeighborEntities.isEmpty()) {
//                            if (isSteeringBehaviorOn(SEPARATION)) {
//                                //System.out.println("Separation");
//                                ((Separation) steeringBehaviors.get(SEPARATION))
//                                        .setTaggetNeighborEntities(taggetNeighborEntities);
//                                velocity = steeringBehaviors.get(SEPARATION).steer();
//                                velocity = new Vector3D(velocity.getX(), 0, velocity.getZ());
//                            }
//
//                            if (isSteeringBehaviorOn(ALIGNMENT)) {
//                                //System.out.println("Alignment");
//                                ((Alignment) steeringBehaviors.get(ALIGNMENT))
//                                        .setTaggetNeighborEntities(taggetNeighborEntities);
//                                rotation = steeringBehaviors.get(ALIGNMENT).steer();
//                            }
//
//                            if (isSteeringBehaviorOn(COHESION)) {
//                                //System.out.println("Cohesion");
//                                ((Cohesion) steeringBehaviors.get(COHESION))
//                                        .setTaggetNeighborEntities(taggetNeighborEntities);
//                                ((Cohesion) steeringBehaviors.get(COHESION))
//                                        .setDistance2TargetThreshold(entity.getBoundingRadius() / 2);
//                                velocity = velocity.add(steeringBehaviors.get(COHESION).steer());
//                                velocity = new Vector3D(velocity.getX(), 0, velocity.getZ());
//                            }
//                        } else {
//                            for (SteeringBehavior logic : values()) {
//                                if (isSteeringBehaviorOn(logic)) {
//                                    if (logic != OFFSET_PURSUIT && logic != SEPARATION
//                                            && logic != ALIGNMENT && logic != COHESION) {
//                                        //System.out.println(logic);
//                                        velocity = steeringBehaviors.get(logic).steer();
//                                        velocity = new Vector3D(velocity.getX(), 0, velocity.getZ());
//                                        rotation = steeringBehaviors.get(logic).getPrimaryAgent().getRotation();
//                                    }
//                                }
//                            }
//                        }
//                    } else { //If leader of the group
////                        velocity = primaryGoalPlayer.getVelocity();
////                        rotation = primaryGoalPlayer.getRotation();
////
////                        for (SteeringBehavior logic : values()) {
////                            if (isSteeringBehaviorOn(logic)) {
////                                if (logic != OFFSET_PURSUIT && logic != SEPARATION
////                                        && logic != ALIGNMENT && logic != COHESION) {
////                                    //System.out.println(primaryGoal.getPosition() + " " + primaryAgent.getPosition());                    
////                                    velocity = steeringBehaviors.get(logic).steer();
////                                    velocity = new Vector3D(velocity.getX(), 0, velocity.getZ());
////                                    rotation = steeringBehaviors.get(logic).getPrimaryAgent().getRotation();
////                                    //System.out.println(velocity);
////                                    //System.out.println(rotation);
////                                }
////                            }
////                        }
//                    }
//
//                    //make sure vehicle does not exceed maximum velocity
//                    if (velocity.getNorm() > primaryAgent.getMaxSpeed()) {
//                        velocity = velocity.equals(ZERO)
//                                ? velocity
//                                : velocity.normalize().scalarMultiply(primaryAgent.getMaxSpeed());
//                    }
//
//                    //FIXME: a wierd fickering effect, something not right probably with the timestep.
//                    //Using Cartesian Coordinates.
//                    position = translate(ZERO, velocity, dt);
//
//                    AbstractBoundingVolume boundingVolume = entity.getBoundingVolumeCopy(0);
//                    boundingVolume.min = boundingVolume.min.add(position);
//                    boundingVolume.max = boundingVolume.max.add(position);
//
//                    //Create a temporary game object and set its bounding volume to the prior copy.
//                    GameObject temp = new GameObject(entity.getId());
//                    temp.setBoundingVolume(0, boundingVolume);
//
//                    //Check if the temporary game object collides with another player in the scene.
//                    String entityCrash = callback.checkPlayerPlayerCollision(temp);
//
//                    //If the temporary game object/bounding volume does not collide 
//                    //with anything in the scene update its position   
//                    boolean isLeaderSteering = false;
//                    if (!callback.checkSceneBoundsCollision(boundingVolume)
//                            && !callback.checkPlayerImmutableObjectCollision(temp)
//                            && (entityCrash == null
//                            || entity.getId().equals(entityCrash))) {
//                        if (!entity.getId().equals(primaryGoalPlayer.getId())) {
//                            entity.updatePosition(position);
//                            if (!rotation.equals(ZERO)) {
//                                System.out.println(entity.getId());
//                                System.out.println(entity.getRotation());
//                                System.out.println(rotation);
//                                entity.updateRotationY(
//                                        //(float) entity.getRotation().add(rotation).getY());
//                                        //(float) rotation.add(entity.getRotation().negate()).getY());
//                                        (float) entity.getRotation().negate().getY());
//                                entity.updateRotationY((float) rotation.getY());
//                                System.out.println(entity.getRotation());
//                                System.out.println("");
//                            }
//                            entity.setVelocity(velocity);
//                        } else //Update leader only when moving over steering behaviour effect.
//                        {
////                            for (SteeringBehavior logic : values()) {
////                                isLeaderSteering = isLeaderSteering && isSteeringBehaviorOn(logic);
////                            }
////                            if (isLeaderSteering) {
////                                primaryAgentPlayer.updatePosition(position);
////                                primaryAgentPlayer.updateRotationY((float) rotation.getY());
////                                primaryAgentPlayer.setVelocity(velocity);
////                                setPrimaryAgent(primaryAgentPlayer);
////                            }
//                        }
//                    }
//                }            
        }
    }

    //*****************
    //AUXILIAR METHODS
    //*****************
    /**
     * Test if target is in attack range.
     *
     * @return TRUE if agent facing target and target is in attack range, FALSE
     * otherwise.
     */
    protected boolean targetInAttackRange() {
        for (SteeringBehavior logic : values()) {
            if (this.isSteeringBehaviorOn(logic)) {
                float rotation = this.steeringBehaviors.get(logic).
                        getDirection2Target(this.primaryGoal.getPosition());

                //if target in attack range and ...
                if (this.steeringBehaviors.get(logic)
                        .getDistance2Target(this.primaryGoal.getPosition())
                        <= this.targetInAttackRangeThreshold) {
                    return (this.primaryAgent.getRotation().getY() - 12) <= rotation
                            && (this.primaryAgent.getRotation().getY() + 12) >= rotation;
                }
            }
        }
        return false;
    }

    /**
     * Before a steering force can be calculated, a entity neighbors must be
     * determined and either stored in a container or tagged ready for
     * processing.
     *
     * @param entity whose neighbors are to tag.
     * @param entities all nearby entities.
     * @param radius of entity within which a entity is considered its neighbor.
     */
    protected void tagNeighbors(GameObject entity,
            Iterator<GameObject> entities, double radius) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            //first clear any current tag
            this.taggetNeighborEntities.clear();

            //iterate through all entities checking for range
            while (entities.hasNext()) {
                Agent currentEntity = new Agent(entities.next());

                Vector3D to = currentEntity.getPosition().subtract(entity.getPosition());

                //the bounding radius of the other is taken into account by adding it
                //to the range
                double range = radius + currentEntity.getBoundingRadius();

                //if entity within range, tag for further consideration. (working in
                //distance-squared space to avoid sqrts)
                if (!currentEntity.getId().equals(entity.getId())
                        && (to.getNormSq() < range * range)) {
                    this.taggetNeighborEntities.add(currentEntity);
                }
            } //next entity
        }
    }

//    /**
//     * Steer the boot being controlled by the AI, in order to make turn away
//     * from an obstacle.
//     */
//    protected void CollisionAvoidance() {
//        if(extensionAIOptions.get("useSteeringBehaviors")) {
//
//            //checkObstacleCollision(callback.getPlayer(playerId).getBoundingVolume(1));
//            //TODO: old code check against Tiago code.
////            if (steerTime == 0) {
////                steerTime = round((float) random() * 10);
////
////                if (round(random()) == 1) {
////                    steerRot += 45;
////                } else {
////                    steerRot -= 45;
////
////                }
////            }
//        }
//    }
//
//    /**
//     * Steer the boot being controlled by the AI, in order to make turn away
//     * from an obstacle.
//     */
//    protected void Steer() {
//        if(extensionAIOptions.get("useSteeringBehaviors")) {
//            //TODO: Tiago code check for bugs.
//            if (steerTime == 0) {
//                steerTime = (int) round(random() * 10);
//                if (round(random()) == 1) {
//                    steerRot += 25;
//                } else {
//                    steerRot -= 25;
////                    steerRot += 45;
////                } else {
////                    steerRot -= 45;       
////                    steerRot += 90;
////                } else {
////                    steerRot -= 90;
//                }
//            }
//        }
//    }
//    public boolean checkcolliding() {
//    log.info(" "+ callback.checkPlayerImmutableObjectCollision(callback.getPlayer(playerId).getBoundingVolume(0)));
//    boolean lol = scene.checkObstacleCollision(scene.getPlayer(playerId).getBoundingVolume(1));
//    log.info(lol); 
//    //return lol;
//    }    
}
