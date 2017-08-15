package communication;

import geometry.TransformGroup;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3f;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import gui.GameMain;

/**
 * Class that implements the communication grid that specifies the network
 * topology of the game.
 *
 * @author G. Amador & A. Gomes
 */
public final class PlayerNode extends ReceiverAdapter implements I_Communication {

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
    protected float radius;
    protected float size;
    private final String playerId;            //the default id for the player running on this node
    private final GameMain callBack;
    private boolean nodeInitialized;
    private boolean gridInitialized;
    private JChannel channel;
    private View old_view;
    public static boolean usePlayerNode = false;

    /**
     * Constructor, sets this node as not initialized or working, resets the
     * game updates to process cue values to NULL, and sets a callback to the
     * main (GameRenderer) game class methods.
     *
     * @param callBack a callback to the main (GameRenderer) game class methods.
     */
    public PlayerNode(GameMain callBack) {
        this.nodeInitialized = false;
        this.gridInitialized = false;
        this.callBack = callBack;
        this.playerId = callBack.playerId;

        radius = Math.round(callBack.size / 12);
        size = callBack.size;

        players = new ConcurrentHashMap<Address, GameUpdate>();
        remoteGameUpdates = new ConcurrentHashMap<Address, GameUpdate>();
        remotePlayersToRemove = new ConcurrentHashMap<Address, GameUpdate>();

        setZonesBaryCenters();

        usePlayerNode = true;
    }

    @Override
    public void setZonesBaryCenters() {
        if (usePlayerNode) {
            System.out.println("Setting up zone centers.");
            if (zonesCenters == null) {
                zonesCenters = new Point3f[zonesLength * zonesWidth];
                for (int i = zonesLength - 1; i >= 0; i--) {
                    for (int j = zonesWidth - 1; j >= 0; j--) {
                        float x = -((size / zonesLength) * (i + 1 - 0.5f) - (size / 2));
                        float z = -((size / zonesWidth) * (j + 1 - 0.5f) - (size / 2));
                        if (x == -0.0) {
                            x = 0.0f;
                        }
                        if (z == -0.0) {
                            z = 0.0f;
                        }

                        zonesCenters[(i * zonesLength) + j] = new Point3f(x, 0.0f, z);
                    }
                }
            }
            System.out.println("Setting up zone centers done.");
        }
    }

    @Override
    public Point3f getClosestZoneBaryCenter(Point3f position) {
        if (usePlayerNode) {
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
        return new Point3f();
    }

    @Override
    public int getZonesLength() {
        if (usePlayerNode) {
            return zonesLength;
        }
        return 0;
    }

    @Override
    public int getZonesWidth() {
        if (usePlayerNode) {
            return zonesWidth;
        }
        return 0;
    }

    @Override
    public boolean exitedZone(Point3f pastPosition, Point3f presentPosition) {
        if (usePlayerNode) {
            Point3f cbc1 = getClosestZoneBaryCenter(presentPosition);
            Point3f cbc2 = getClosestZoneBaryCenter(new Point3f(presentPosition.x + radius, presentPosition.y, presentPosition.z));
            Point3f cbc3 = getClosestZoneBaryCenter(new Point3f(presentPosition.x - radius, presentPosition.y, presentPosition.z));
            Point3f cbc4 = getClosestZoneBaryCenter(new Point3f(presentPosition.x, presentPosition.y, presentPosition.z + radius));
            Point3f cbc5 = getClosestZoneBaryCenter(new Point3f(presentPosition.x, presentPosition.y, presentPosition.z - radius));

            if ((!cbc1.equals(cbc2))
                    || (!cbc1.equals(cbc3))
                    || (!cbc1.equals(cbc4))
                    || (!cbc1.equals(cbc5))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean toRemove(Point3f position, GameUpdate gameUpdate) {
        if (usePlayerNode) {
            if (players.containsKey((Address) gameUpdate.nodeId)) {
                players.remove((Address) gameUpdate.nodeId);
            }
            players.put((Address) gameUpdate.nodeId, gameUpdate);

            float distance = position.distance(gameUpdate.position);
            Point3f cbc1 = getClosestZoneBaryCenter(position);
            Point3f cbc2 = getClosestZoneBaryCenter(gameUpdate.position);

            if ((!cbc1.equals(cbc2)) && (distance > radius)) {
                players.remove((Address) gameUpdate.nodeId);
                return true;
            }
        }
        return false;
    }

    @Override
    public void startGridNode(String gridConfig) {
        if (usePlayerNode) {
            //System.out.println("Waitting before starting grid node.");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //TODO: Set up communication encryption here
            if (!"singleplayer".equals(gridConfig.toLowerCase())) {
                try {
                    if (gridConfig.equals("")) {
                        channel = new JChannel(); // use the default config, udp.xml
                    } else {
                        channel = new JChannel(gridConfig);
                    }
                    channel.setReceiver(this);
                    channel.connect("JOTExample");
                    old_view = channel.getView();
                } catch (Exception ex) {
                    Logger.getLogger(PlayerNode.class.getName()).log(Level.SEVERE, null, ex);
                }
                gridInitialized = true;
            }

            if (gridInitialized) {
                localNodeId = channel.getAddress();
                System.out.println(playerId + " with nodeId [" + localNodeId + "] joined the grid.");

                //if more than one node in the grid ...
                if (!isOnlyGridNode()) {
                    //... wait a litle to make sure other nodes are listening and everything was loaded ...
                    //System.out.println("Waitting before send data to other grid nodes.");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    //to confirm to which player's unicast should be done
                    //... broadcast local player that just entered data
                    GameUpdate playerGameUpdate = callBack.getGameUpdate();
                    sendUpdate(playerGameUpdate, null);
                    //System.out.println("Data sent.");
                }

                nodeInitialized = true;
            }
        }
    }

    @Override
    public void stopGridNode() {
        if (usePlayerNode && isGridNodeStarted()) {
            channel.close();
        }
    }

    /**
     * Setup behavior for when a grid node leaves/fails.
     *
     * @param new_view
     */
    @Override
    public void viewAccepted(View new_view) {
        if (usePlayerNode && nodeInitialized) {
            //System.out.println("** old view: " + old_view);
            //System.out.println("** new view: " + new_view);

            List<Address> old_view_ids = old_view.getMembers();
            List<Address> new_view_ids = new_view.getMembers();
            //System.out.println(old_view_ids);
            //System.out.println(new_view_ids);

            //if remote node leaves/fails...
            if (old_view.size() >= new_view.size()) {
                Set<Address> st = new LinkedHashSet<Address>(old_view_ids);
                st.removeAll(new_view_ids);

                //... get remote node Id...
                if (!st.isEmpty()) {
                    for (Address remoteNodeId : st) {
                        //System.out.println("Node [" + remoteNodeId + "] left/failed");

                        //... remove remote player (if it exists) from the hashtable players and set remote player (if it exists) to be removed from the scene
                        if (players.containsKey(remoteNodeId)) {
                            remotePlayersToRemove.put(remoteNodeId, players.get(remoteNodeId));
                            players.remove(remoteNodeId);
                        }
                    }
                }
            }
            //else node joins
            //System.out.println(new_view.size());

            old_view = new_view;
        }
    }

    @Override
    public boolean isOnlyGridNode() {
        if (usePlayerNode) {
            return !(channel.getView().size() > 1);
        }
        return false;
    }

    @Override
    public boolean isGridNodeStarted() {
        if (usePlayerNode) {
            return (nodeInitialized && gridInitialized);
        }
        return false;
    }

    @Override
    public boolean isGridRunning() {
        if (usePlayerNode) {
            return (isGridNodeStarted() && !isOnlyGridNode());
        } else {
            return false;
        }
    }

    @Override
    public Object getLocalNodeId() {
        if (usePlayerNode) {
            return localNodeId;
        }
        return null;
    }

    @Override
    public float getPlayerNodesCount() {
        if (usePlayerNode && channel != null) {
            return channel.getView().size();
        }
        return 1;
    }

    @Override
    public float getPerceivedPlayerNodesCount() {
        if (usePlayerNode) {
            return players.size() + 1;
        }
        return 1;
    }

    @Override
    public void sendUpdate(GameUpdate gameUpdate, Object destinationNodeId) {
        if (usePlayerNode) {
            try {
                if (destinationNodeId == null) {
                    channel.send(null, gameUpdate);
                } else {
                    channel.send((Address) destinationNodeId, gameUpdate);
                }
            } catch (Exception ex) {
                Logger.getLogger(PlayerNode.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void sendUpdate(GameUpdate gameUpdate, boolean exitedZone) {
        if (usePlayerNode) {
            if (!exitedZone) {
                ConcurrentHashMap<Address, GameUpdate> aux = new ConcurrentHashMap<Address, GameUpdate>();
                aux.putAll(players);
                Set st = aux.keySet();
                if (!st.isEmpty()) {
                    Iterator itr = st.iterator();
                    while (itr.hasNext()) {
                        Address destinationNodeId = (Address) itr.next();
                        //System.out.println("Unicast game update to " + destinationNodeId);
                        sendUpdate(gameUpdate, destinationNodeId);
                    }
                }
                aux.clear();
                //System.out.println("unicasts");
            } else {
                //remotePlayersToRemove.putAll(players);
                //players.clear();
                sendUpdate(gameUpdate, null);
                //System.out.println("broadcasts");
            }
        }
    }

    @Override
    public void receive(Message msg) {
        if (usePlayerNode) {
            GameUpdate gameUpdate = (GameUpdate) msg.getObject();
            //System.out.println(">>> Received Remote Game Update");
            //System.out.println("[" + gameUpdate.nodeId + "] [" + localNodeId + "]");
            //System.out.println(gameUpdate.nodeId.equals(localNodeId));
            if ((localNodeId != null) && (!(gameUpdate.nodeId.equals(localNodeId)))) {
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
    }

    @Override
    public void update() {
        if (usePlayerNode && isGridNodeStarted()) {
            ConcurrentHashMap<Address, GameUpdate> aux = new ConcurrentHashMap<Address, GameUpdate>();
            aux.putAll(remotePlayersToRemove);
            remotePlayersToRemove.clear();
            Set st = aux.keySet();
            if (!st.isEmpty()) {
                Iterator itr = st.iterator();
                while (itr.hasNext()) {
                    Address playerNodeId = (Address) itr.next();
                    //final String playerObjectId = aux.get(playerNodeId).objectId;
                    //new Thread(new Runnable() {
                    //@Override
                    //public void run() {
                    //final TransformGroup player = callBack.scene.getPlayer(playerObjectId);
                    TransformGroup player = callBack.sceneManager.getPlayer(aux.get((Address) playerNodeId).objectId);
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
}
