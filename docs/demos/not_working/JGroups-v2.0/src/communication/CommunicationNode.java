package communication;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3f;
import managers.SceneManager;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;

/**
 * Class that implements a generic communication node.
 *
 * @author G. Amador & A. Gomes
 */
public class CommunicationNode extends ReceiverAdapter implements I_Communication {

    //Hash table to store all the available monitors nodeId and the monitors associated zone barycenter
    protected static ConcurrentHashMap<Address, Point3f> monitors;
    //Hash table to store all the available players with their respective nodeId
    protected static ConcurrentHashMap<Address, GameUpdate> players;
    //Hash table to store all remote updates to process.
    protected static ConcurrentHashMap<Address, GameUpdate> remoteGameUpdates;
    //Hash table to store all remote players to remove from the scene.
    protected static ConcurrentHashMap<Address, GameUpdate> remotePlayersToRemove;
    protected static Address localNodeId;    //the default id for the player running on this node
    protected static int zonesWidth = 3;
    protected static int zonesLength = 3;
    protected Point3f zonesCenters[] = null;
    protected float radius = Math.round(SceneManager.getSceneSize() / 12);
    protected float size = SceneManager.getSceneSize();
    protected static boolean nodeInitialized;
    protected static boolean gridInitialized;
    protected JChannel channel;
    protected static RpcDispatcher disp;
    protected static RspList rsp_list;
    protected MethodCall callUnicastAddMonitor;
    protected MethodCall callUnicastAddPlayer;
    protected MethodCall callUnicastRemovePlayer;
    protected static MethodCall callUnicastGameUpdate;
    protected static MethodCall callUnicastGameUpdates;
    protected static RequestOptions opts;
    protected View old_view;

    /**
     * Default constructor.
     */
    public CommunicationNode() {
        nodeInitialized = false;
        gridInitialized = false;

        monitors = new ConcurrentHashMap<Address, Point3f>();
        players = new ConcurrentHashMap<Address, GameUpdate>();
        remoteGameUpdates = new ConcurrentHashMap<Address, GameUpdate>();
        remotePlayersToRemove = new ConcurrentHashMap<Address, GameUpdate>();

        setZonesBaryCenters();
    }

    @Override
    public final void setZonesBaryCenters() {
        System.out.println("Setting up zone centers.");
        if (zonesCenters == null) {
            zonesCenters = new Point3f[zonesLength * zonesWidth];
            for (int i = zonesLength - 1; i >= 0; i--) {
                for (int j = zonesWidth - 1; j >= 0; j--) {
                    float x = -((size / zonesLength) * (i + 1 - 0.5f) - size / 2);
                    float z = -((size / zonesWidth) * (j + 1 - 0.5f) - size / 2);
                    if (x == -0.0) {
                        x = 0.0f;
                    }
                    if (z == -0.0) {
                        z = 0.0f;
                    }

                    zonesCenters[i * zonesLength + j] = new Point3f(x, 0.0f, z);
                }
            }
        }
        System.out.println("Setting up zone centers done.");
    }

    @Override
    public Point3f getClosestZoneBaryCenter(Point3f position) {
        float distance = size;
        Point3f closestZoneBaryCenter = zonesCenters[0];

        for (Point3f p : zonesCenters) {
            float newDistance = position.distance(p);
            if (distance > newDistance) {
                distance = newDistance;
                closestZoneBaryCenter = p;
            }
        }

        return closestZoneBaryCenter;
    }

    @Override
    public boolean exitedZone(Point3f pastPosition, Point3f presentPosition) {
        Point3f cbc1 = getClosestZoneBaryCenter(presentPosition);
        Point3f cbc2 = getClosestZoneBaryCenter(new Point3f(presentPosition.x + radius, presentPosition.y, presentPosition.z));
        Point3f cbc3 = getClosestZoneBaryCenter(new Point3f(presentPosition.x - radius, presentPosition.y, presentPosition.z));
        Point3f cbc4 = getClosestZoneBaryCenter(new Point3f(presentPosition.x, presentPosition.y, presentPosition.z + radius));
        Point3f cbc5 = getClosestZoneBaryCenter(new Point3f(presentPosition.x, presentPosition.y, presentPosition.z - radius));

        if (!cbc1.equals(cbc2)
                || !cbc1.equals(cbc3)
                || !cbc1.equals(cbc4)
                || !cbc1.equals(cbc5)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean toRemove(Point3f position, GameUpdate gameUpdate) {
        if (players.containsKey((Address) gameUpdate.nodeId)) {
            players.remove((Address) gameUpdate.nodeId);
        }
        players.put((Address) gameUpdate.nodeId, gameUpdate);

        float distance = position.distance(gameUpdate.position);
        Point3f cbc1 = getClosestZoneBaryCenter(position);
        Point3f cbc2 = getClosestZoneBaryCenter(gameUpdate.position);

        if (!cbc1.equals(cbc2) && distance > radius) {
            players.remove((Address) gameUpdate.nodeId);
            return true;
        }
        return false;
    }

    @Override
    public void startGridNode(String gridConfig) {
        //System.out.println("Waitting before starting grid node.");
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //TODO: Set up communication encryption here

        if (!"singleplayer".equals(gridConfig.toLowerCase())) {
            try {
                callUnicastAddMonitor = new MethodCall(getClass().getMethod("unicastAddMonitor", Point3f.class, Address.class));
                callUnicastAddPlayer = new MethodCall(getClass().getMethod("unicastAddPlayer", GameUpdate.class));
                callUnicastRemovePlayer = new MethodCall(getClass().getMethod("unicastRemovePlayer", Address.class));
                callUnicastGameUpdate = new MethodCall(getClass().getMethod("unicastGameUpdate", GameUpdate.class));
                callUnicastGameUpdates = new MethodCall(getClass().getMethod("unicastGameUpdates", GameUpdate.class));
                opts = new RequestOptions(ResponseMode.GET_NONE, 5000);

                if (gridConfig.equals("")) {
                    channel = new JChannel(); // use the default config, udp.xml
                } else {
                    channel = new JChannel(gridConfig);
                }
                disp = new RpcDispatcher(channel, this);
                disp.setMembershipListener(this);
                channel.connect("JOTExample");
                old_view = channel.getView();
            } catch (Exception ex) {
                Logger.getLogger(CommunicationNode.class.getName()).log(Level.SEVERE, null, ex);
            }

            gridInitialized = true;
        }

        if (gridInitialized) {
            localNodeId = channel.getAddress();
            nodeInitialized = true;
        }
    }

    @Override
    public void stopGridNode() {
        if (isGridNodeStarted()) {
            channel.close();
            disp.stop();
        }
    }

    @Override
    public void viewAccepted(View new_view) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isOnlyGridNode() {
        return !(channel.getView().size() > 1);
    }

    @Override
    public boolean isGridNodeStarted() {
        return nodeInitialized && gridInitialized;
    }

    @Override
    public boolean isGridRunning() {
        return isGridNodeStarted() && !isOnlyGridNode();
    }

    @Override
    public Object getLocalNodeId() {
        return localNodeId;
    }

    @Override
    public float getPlayerNodesCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getPerceivedPlayerNodesCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getZonesLength() {
        return zonesLength;
    }

    @Override
    public int getZonesWidth() {
        return zonesWidth;
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Show all existing monitors and their respective baryCenters.
     */
    public void showMonitors() {
        System.out.println("Showing all running monitors.");
        ConcurrentHashMap<Address, Point3f> aux = new ConcurrentHashMap<Address, Point3f>();
        aux.putAll(monitors);
        Set st = aux.keySet();
        if (!st.isEmpty()) {
            Iterator itr = st.iterator();
            while (itr.hasNext()) {
                Address monitorNodeId = (Address) itr.next();
                System.out.println("[" + monitorNodeId + "] " + aux.get(monitorNodeId));
            }
        }
        System.out.println("\nThere are " + aux.size() + " running monitors.");
        aux.clear();
        System.out.println();
    }

    /**
     * Show all players in this node hashMap.
     */
    public void showPlayers() {
        System.out.println("Showing all players assigned to this node.");
        ConcurrentHashMap<Address, GameUpdate> aux = new ConcurrentHashMap<Address, GameUpdate>();
        aux.putAll(players);
        Set st = aux.keySet();
        if (!st.isEmpty()) {
            Iterator itr = st.iterator();
            while (itr.hasNext()) {
                Address playerNodeId = (Address) itr.next();
                System.out.println(aux.get(playerNodeId).objectId + " with node id [" + playerNodeId + "]");
                //System.out.println(aux.get(playerNodeId).position);
            }
        }
        System.out.println("\nThere are " + aux.size() + " players assigned to this node.");
        aux.clear();
        System.out.println();
    }

    /**
     * Send a add monitor command to a remote node monitors hash table.
     *
     * @param zoneBaryCenter baryCenter of monitor to remotely add to monitors
     * hash table.
     * @param monitorNodeId node id of monitor to remotely add to monitors hash
     * table.
     * @param destinationNodeId node that will add a remote monitor to its
     * monitors hash table.
     */
    protected void sendAddMonitor(Point3f zoneBaryCenter, Address monitorNodeId, Address destinationNodeId) {
        try {
            //System.out.println(">>> Starting Unicast Add Monitor Update.");
            callUnicastAddMonitor.setArgs(zoneBaryCenter, monitorNodeId);
            rsp_list = disp.callRemoteMethod(destinationNodeId, callUnicastAddMonitor, opts);
        } catch (Exception ex) {
            Logger.getLogger(CommunicationNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Unicast a add monitor to remote node monitors hash table command to a
     * remote node.
     *
     * @param zoneBaryCenter baryCenter of monitor to remotely add to monitors
     * hash table.
     * @param monitorNodeId node id of monitor to remotely add to monitors hash
     * table.
     * @throws Exception If failed.
     */
    public static void unicastAddMonitor(Point3f zoneBaryCenter, Address monitorNodeId) throws Exception {
        //System.out.println(">>> Received Unicast Add Monitor Update.");
        if (localNodeId == null || !(monitorNodeId.equals(localNodeId))) {
            monitors.put(monitorNodeId, zoneBaryCenter);
            System.out.println("Monitor with nodeId [" + monitorNodeId + "] and baricentre " + zoneBaryCenter + " joined the grid.\n");
        }
    }

    /**
     * Send a remove player command to a remote node. The remote node will
     * remove (if he exists), from his players hash table, the player that sent
     * the remote command.
     *
     * @param gameUpdate local player gameUpdate.
     * @param destinationNodeId the remote node that should remove an player (if
     * he exists) from his players hash table.
     */
    protected void sendAddPlayer(GameUpdate gameUpdate, Address destinationNodeId) {
        try {
            //System.out.println(">>> Starting Unicast Add Player Update.");
            callUnicastAddPlayer.setArgs(gameUpdate);
            rsp_list = disp.callRemoteMethod(destinationNodeId, callUnicastAddPlayer, opts);
        } catch (Exception ex) {
            Logger.getLogger(CommunicationNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Unicast a add player from remote node player hash table command to a
     * remote node.
     *
     * @param gameUpdate GameUpdate to Unicast.
     * @throws Exception If failed.
     */
    public static void unicastAddPlayer(GameUpdate gameUpdate) throws Exception {
        //System.out.println(">>> Received Unicast Add Player Update.");
        if ((localNodeId == null || !(((Address) gameUpdate.nodeId).equals(localNodeId)))
                && !players.containsKey((Address) gameUpdate.nodeId)) {
            players.put((Address) gameUpdate.nodeId, gameUpdate);
            //System.out.println("Player with nodeId [" + remoteGameUpdate.nodeId + "] assigned to this monitor.\n");

        }
    }

    /**
     * Send a remove player command to a remote node. The remote node will
     * remove (if he exists), from his players hash table, the player that sent
     * the remote command.
     *
     * @param localPlayerNodeId local player nodeId.
     * @param destinationNodeId the remote node that should remove an player (if
     * he exists) from his players hash table.
     */
    protected void sendRemovePlayer(Address localPlayerNodeId, Address destinationNodeId) {
        try {
            //System.out.println(">>> Starting Unicast Remove Player Update.");
            callUnicastRemovePlayer.setArgs(localPlayerNodeId);
            rsp_list = disp.callRemoteMethod(destinationNodeId, callUnicastRemovePlayer, opts);
        } catch (Exception ex) {
            Logger.getLogger(CommunicationNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Unicast a remove player from remote node player hash table command to a
     * remote node.
     *
     * @param remotePlayerNodeId player nodeId.
     * @throws Exception If failed.
     */
    public static void unicastRemovePlayer(Address remotePlayerNodeId) throws Exception {
        //System.out.println(">>> Received Unicast Remove Player Update.");
        if ((localNodeId == null || !remotePlayerNodeId.equals(localNodeId))
                && players.containsKey(remotePlayerNodeId)) {
            //System.out.println(">>> Did Unicast Remove Player.");
            players.remove(remotePlayerNodeId);
            //System.out.println("Player with nodeId [" + remotePlayerNodeId + "] no longer assigned to this monitor.\n");        
        }
    }

    @Override
    public void sendUpdate(GameUpdate gameUpdate, boolean exitedZone) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendUpdate(GameUpdate gameUpdate, Object destinationNodeId) {
        try {
            //System.out.println(">>> Starting Unicast Game Update.");
            callUnicastGameUpdate.setArgs(gameUpdate);
            rsp_list = disp.callRemoteMethod((Address) destinationNodeId, callUnicastGameUpdate, opts);


        } catch (Exception ex) {
            Logger.getLogger(CommunicationNode.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Unicast a given gameUpdate to a provided destination node.
     *
     * @param gameUpdate GameUpdate to Unicast.
     * @throws Exception If failed.
     */
    public static void unicastGameUpdate(GameUpdate gameUpdate) throws Exception {
        //System.out.println(">>> Received Unicast Game Update.");
        //System.out.println(gameUpdate.nodeId + " " + localNodeId);
        //System.out.println(gameUpdate.nodeId.equals(localNodeId));
        if (localNodeId == null || !(gameUpdate.nodeId.equals(localNodeId))) {
            if (remoteGameUpdates.containsKey((Address) gameUpdate.nodeId)) {
                if (remoteGameUpdates.get((Address) gameUpdate.nodeId).timeSent < gameUpdate.timeSent) {
                    remoteGameUpdates.remove((Address) gameUpdate.nodeId);
                    remoteGameUpdates.put((Address) gameUpdate.nodeId, gameUpdate);
                }
            } else {
                remoteGameUpdates.put((Address) gameUpdate.nodeId, gameUpdate);
            }
        }
    }

    /**
     *
     * Send all game updates from players assigned to monitor with nodeId, to a
     * provided player nodeId.
     *
     * @param gameUpdate all the information regarding a provided player.
     * @param destinationMonitorNodeId nodeID of the monitor that must Unicast
     * its players gameUpdates to provided player.
     */
    public static void sendUpdates(GameUpdate gameUpdate, Address destinationMonitorNodeId) {
        try {
            //System.out.println(">>> Starting Unicast Game Updates.");
            callUnicastGameUpdates.setArgs(gameUpdate);
            rsp_list = disp.callRemoteMethod((Address) destinationMonitorNodeId, callUnicastGameUpdates, opts);


        } catch (Exception ex) {
            Logger.getLogger(CommunicationNode.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

//    /**
//     *
//     * Send all game updates from players assigned to monitor with baryCenter p1
//     * (not for now) and its monitor neighbors, to provided a player nodeId.
//     *
//     * @param p1 baryCenter of the monitor whose neighbor monitors should
//     * Unicast their players gameUpdates to a provided player.
//     * @param gameUpdate all the information regarding a provided player.
//     */
//    protected static void sendUpdates(Point3f p1, GameUpdate gameUpdate) {
//        ConcurrentHashMap<String, Point3f> aux = new ConcurrentHashMap<>();
//        aux.putAll(monitors);
//        Set st = aux.keySet();
//        if (!st.isEmpty()) {
//            Iterator itr = st.iterator();
//            while (itr.hasNext()) {
//                String monitorNodeId = itr.next().toString();
//                float distance = p1.distance(aux.get(monitorNodeId));
//                //if ((distance < ((size / zonesLength) * 2))
//                //        || (distance < ((size / zonesWidth) * 2))) {
//                if ((distance < (size / zonesLength)) || (distance < (size / zonesWidth))) {
//                    try {
//                        unicastGameUpdates(gameUpdate, monitorNodeId);
//                    } catch (GridException ex) {
//                        Logger.getLogger(CommunicationNode.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }
//        }
//        aux.clear();
//    }
    /**
     * Unicast all game updates from players assigned to monitor with
     * destinationMonitorNodeId, to player with destinationPlayerNodeId.
     *
     * @param gameUpdate all the information regarding a provided player.
     * @throws Exception If failed.
     */
    public static void unicastGameUpdates(GameUpdate gameUpdate) throws Exception {
        //System.out.println(">>> Received Unicast Data of Players from MonitorNode.");
        ConcurrentHashMap<Address, GameUpdate> aux = new ConcurrentHashMap<Address, GameUpdate>();
        aux.putAll(players);
        Set st = aux.keySet();
        if (!st.isEmpty()) {
            Iterator itr = st.iterator();
            while (itr.hasNext()) {
                Address playerNodeId = (Address) itr.next();
                if (!(((Address) gameUpdate.nodeId).equals(playerNodeId))) {
                    try {
                        callUnicastGameUpdate.setArgs(aux.get((Address) playerNodeId));
                        rsp_list = disp.callRemoteMethod((Address) gameUpdate.nodeId, callUnicastGameUpdate, opts);
                        callUnicastGameUpdate.setArgs(gameUpdate);
                        rsp_list = disp.callRemoteMethod((Address) playerNodeId, callUnicastGameUpdate, opts);


                    } catch (Exception ex) {
                        Logger.getLogger(CommunicationNode.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        aux.clear();
    }
}