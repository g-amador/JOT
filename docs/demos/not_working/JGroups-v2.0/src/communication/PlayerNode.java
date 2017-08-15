package communication;

import static communication.CommunicationNode.monitors;
import geometry.TransformGroup;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.vecmath.Point3f;
import org.jgroups.Address;
import org.jgroups.View;
import gui.GameMain;

/**
 * Class that specializes a communication node of type player.
 *
 * @author Gonçalo Paiva & Abel Gomes
 */
//TODO: make client server or p2p with the monitor nodes option
public final class PlayerNode extends CommunicationNode {

    //the default monitorNodeId associated to this player/monitor
    private static Address localMonitorNodeId;
    //the default id for the player running on this node
    private String playerId;
    private GameMain callBack;

    /**
     * Constructor for a communication node of type player. Sets this node as
     * not initialized/working, resets the game updates to process cue values to
     * NULL, and sets a callback to the main (GameRenderer) game class methods.
     *
     * @param callBack a callback to the main (GameRenderer) game class methods.
     */
    public PlayerNode(GameMain callBack) {
        super();

        this.callBack = callBack;
        this.playerId = callBack.playerId;
    }

    @Override
    public void startGridNode(String gridConfig) {
        super.startGridNode(gridConfig);

        if (gridInitialized) {
            //wait a litle to make sure the data from the monitor in charge of the zone of this joining node was received ...
            //System.out.println("Waitting before send data to other grid nodes.");
            //try {
            //    Thread.sleep(7500);
            //} catch (Exception e) {
            //    throw new RuntimeException(e);
            //}

            //and send a reply back to him and request all the gameUpdates from players in this monitors assigned zone.
            GameUpdate playerGameUpdate = callBack.getGameUpdate();
            localMonitorNodeId = getClosestMonitorNodeId(playerGameUpdate.position);
            if (localMonitorNodeId != null) {
                sendUpdates(playerGameUpdate, localMonitorNodeId);
                sendAddPlayer(playerGameUpdate, localMonitorNodeId);
                System.out.println(playerId + " with nodeId [" + localNodeId + "] joined the grid.\n");
            }

            nodeInitialized = true;
        }
    }

    /**
     * Setup behavior for when a grid node joins/leaves/fails.
     *
     * @param new_view
     */
    @Override
    public void viewAccepted(View new_view) {
        if (nodeInitialized) {
            //System.out.println("** old view: " + old_view);
            //System.out.println("** new view: " + new_view);

            List<Address> old_view_ids = old_view.getMembers();
            List<Address> new_view_ids = new_view.getMembers();
            //System.out.println(old_view_ids);
            //System.out.println(new_view_ids);

            //if remote node leaves/fails...
            if (old_view.size() > new_view.size()) {
                Set<Address> st1 = new LinkedHashSet<Address>(old_view_ids);
                st1.removeAll(new_view_ids);

                //... get remote node Id...
                if (!st1.isEmpty()) {
                    for (Address remoteNodeId : st1) {
                        if (players.containsKey(remoteNodeId)) {
                            remotePlayersToRemove.put(remoteNodeId, players.get(remoteNodeId));
                            players.remove(remoteNodeId);
                        }

                        //... remove remote monitor (if it exists) from the hashtable monitors
                        if (monitors.containsKey(remoteNodeId)) {
                            monitors.remove(remoteNodeId);
                            //... and if it was this player's monitor get (if available) a new monitor
                            if (localMonitorNodeId.equals(remoteNodeId)) {
                                ConcurrentHashMap<Address, Point3f> aux = new ConcurrentHashMap<Address, Point3f>();
                                aux.putAll(monitors);
                                Set st = aux.keySet();
                                if (!st.isEmpty()) {
                                    GameUpdate playerGameUpdate = callBack.getGameUpdate();
                                    localMonitorNodeId = getClosestMonitorNodeId(playerGameUpdate.position);
                                    //sendUpdates(playerGameUpdate, aux.get(localMonitorNodeId));
                                    sendUpdates(playerGameUpdate, localMonitorNodeId);
                                    //sendAddPlayer(playerGameUpdate, localMonitorNodeId);
                                    sendUpdate(playerGameUpdate, localMonitorNodeId);
                                }
                                aux.clear();
                            }
                        }
                    }
                }
            } //else { //... if remote node node joins...
            //}

            old_view = new_view;
        }
    }

    @Override
    public boolean exitedZone(Point3f pastPosition, Point3f presentPosition) {
        if (!pastPosition.equals(presentPosition)) {
            if (localMonitorNodeId == null) {
                return false;
            } else {
                return !localMonitorNodeId.equals(getClosestMonitorNodeId(presentPosition));
            }
        }
        return false;
    }

    /**
     * Given a point p1 obtain the closest monitorNodeId from all running
     * monitors.
     *
     * @param p1 position of an player or a remote monitor baryCenter
     * @return the monitor node id whose baryCenter is closer to p1
     */
    private Address getClosestMonitorNodeId(Point3f p1) {
        float distance = size;
        Address clostesMonitorNodeId = localMonitorNodeId;

        ConcurrentHashMap<Address, Point3f> aux = new ConcurrentHashMap<Address, Point3f>();
        aux.putAll(monitors);
        Set st = aux.keySet();
        if (!st.isEmpty()) {
            Iterator itr = st.iterator();
            while (itr.hasNext()) {
                Address monitorNodeId = (Address) itr.next();
                float newDistance = p1.distance(aux.get(monitorNodeId));
                if (distance > newDistance) {
                    distance = newDistance;
                    clostesMonitorNodeId = monitorNodeId;
                }
            }
        }
        aux.clear();

        return clostesMonitorNodeId;
    }

    @Override
    public Point3f getClosestZoneBaryCenter(Point3f p1) {
        float distance = size;
        Point3f closestZoneBaryCenter = monitors.get(localMonitorNodeId);

        ConcurrentHashMap<Address, Point3f> aux = new ConcurrentHashMap<Address, Point3f>();
        aux.putAll(monitors);
        Set st = aux.keySet();
        if (!st.isEmpty()) {
            Iterator itr = st.iterator();
            while (itr.hasNext()) {
                Address monitorNodeId = (Address) itr.next();
                float newDistance = p1.distance(aux.get(monitorNodeId));
                if (distance > newDistance) {
                    distance = newDistance;
                    closestZoneBaryCenter = aux.get(monitorNodeId);
                }
            }
        }
        aux.clear();

        return closestZoneBaryCenter;
    }

    @Override
    public float getPlayerNodesCount() {
        if (channel != null) {
            if (monitors == null) {
                return channel.getView().size();
            } else {
                return channel.getView().size() - monitors.size();
            }
        }
        return 1;
    }

    @Override
    public float getPerceivedPlayerNodesCount() {
        return players.size() + 1;
    }

    @Override
    public void sendUpdate(GameUpdate gameUpdate, boolean exited) {
        ConcurrentHashMap<Address, GameUpdate> aux = new ConcurrentHashMap<Address, GameUpdate>();
        aux.putAll(players);
        Set st = aux.keySet();
        if (!st.isEmpty()) {
            Iterator itr = st.iterator();
            while (itr.hasNext()) {
                Address destinationNodeId = (Address) itr.next();
                sendUpdate(gameUpdate, destinationNodeId);
            }
        }
        aux.clear();

        if (exited) {
            //TODO: if it changes zone update its hash table and Unicast to its still remaning nodes and to monitor of new zones
            remotePlayersToRemove.putAll(players);
            players.clear();
            sendRemovePlayer((Address) gameUpdate.nodeId, localMonitorNodeId);
            localMonitorNodeId = getClosestMonitorNodeId(gameUpdate.position);
            //sendUpdates(gameUpdate, monitors.get(localMonitorNodeId));
            sendUpdates(gameUpdate, localMonitorNodeId);
            sendAddPlayer(gameUpdate, localMonitorNodeId);
        }

        //System.out.println(gameUpdate.position);
        sendUpdate(gameUpdate, localMonitorNodeId);
    }

    @Override
    public void update() {
        if (isGridNodeStarted()) {
            //System.out.println("Performing update.");
            //If no monitors running crash this player node.
            Set st = monitors.keySet();
            if (st.isEmpty()) {
                System.out.println("There are zero running monitors.");
                callBack.gameShutdown(GameMain.gl);
            }

            ConcurrentHashMap<Address, GameUpdate> aux = new ConcurrentHashMap<Address, GameUpdate>();
            aux.putAll(remotePlayersToRemove);
            remotePlayersToRemove.clear();
            st = aux.keySet();
            if (!st.isEmpty()) {
                Iterator itr = st.iterator();
                while (itr.hasNext()) {
                    Address playerNodeId = (Address) itr.next();
                    //final String playerObjectId = aux.get(playerNodeId).objectId;
                    //new Thread(new Runnable() {
                    //@Override
                    //public void run() {
                    //final TransformGroup player = callBack.scene.getPlayer(playerObjectId);
                    TransformGroup player = callBack.sceneManager.getPlayer(aux.get(playerNodeId).objectId);
                    if (player != null) {
                        callBack.sceneManager.removePlayer(player);
                    }
                    //}
                    //}).start();
                }
            }
            aux.clear();

            aux.putAll(remoteGameUpdates);
            remoteGameUpdates.clear();
            st = aux.keySet();
            if (!st.isEmpty()) {
                Iterator itr = st.iterator();
                while (itr.hasNext()) {
                    Address playerNodeId = (Address) itr.next();
                    final GameUpdate gameUpdate = aux.get(playerNodeId);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            callBack.gameUpdate(gameUpdate);
                        }
                    }).start();
                }
            }
            aux.clear();
        }
    }

    @Override
    public void showMonitors() {
        super.showMonitors();

        if (playerId != null && localMonitorNodeId != null) {
            System.out.println(playerId + " monitor nodeId [" + localMonitorNodeId + "].");
        }
    }
}