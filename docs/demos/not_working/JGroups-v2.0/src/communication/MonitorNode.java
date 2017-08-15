package communication;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.vecmath.Point3f;
import org.jgroups.Address;
import org.jgroups.View;

/**
 * Class that specializes a communication node of type monitor.
 *
 * @author Gonçalo Paiva & Abel Gomes
 */
//TODO: concurrency problem when two monitors simultaneously start.
public final class MonitorNode extends CommunicationNode {

    private static int localZoneId;

    /**
     * Constructor, sets this monitor zoneId (if a valid one is provided) and
     * sets the grid configuration.
     *
     * @param zoneId a provided valid monitor zoneId.
     * @param gridConfig location of .xml with the grid configuration to use.
     */
    public MonitorNode(String zoneId, String gridConfig) {
        super();

        try {
            localZoneId = Integer.valueOf(zoneId);
        } catch (NumberFormatException e) {
            System.out.println("First argument i (zone index) must be a number, i, i = 0, 1, 2, 3 ... ");
            throw new RuntimeException(e);
        }

        if (localZoneId >= zonesCenters.length) {
            System.out.println("Invalid zone index (try a lower number) ... ");
            System.exit(0);
        }

        startGridNode(gridConfig);
        monitorStart(gridConfig);
    }

    public static void main(String[] args) {
        MonitorNode m;

        if (args.length == 0) {
            System.out.println("Monitor needs to be called with at least one argument (zone index) i, i = 0, 1, 2, 3 ... \n");
            System.exit(0);
        } else if (args.length == 1) {
            m = new MonitorNode(args[0], "");
        } else {
            m = new MonitorNode(args[0], args[1]);
        }
    }

    /**
     * Start a monitor thread process.
     *
     * @param gridConfig location of .xml with the grid configuration to use.
     */
    private void monitorStart(final String gridConfig) {
        // Create a new thread
        Thread monitorThread = new Thread() {
            // Override run() to provide the running behavior of this thread.
            @Override
            public void run() {
                update(gridConfig);
            }
        };

        // Start the thread. start() calls run(), which in turn calls gameLoop().
        monitorThread.start();
    }

    @Override
    public void startGridNode(String gridConfig) {
        super.startGridNode(gridConfig);

        if (gridInitialized) {
            if (monitors.get(localNodeId) == null) {
                monitors.put(localNodeId, zonesCenters[localZoneId]);
            }

            System.out.println("Monitor with nodeId [" + localNodeId + "] and bary center " + monitors.get(localNodeId) + " joined the grid.\n");

            //TODO: for all available monitors if this one is assigned to the same zone as another monitor already running test for cheating

            //if more than one node in the grid ...
            if (!isOnlyGridNode()) {
                //... after giving some time for other monitors to send their data...
                //try {
                //    Thread.sleep(5000);
                //} catch (Exception e) {
                //    throw new RuntimeException(e);
                //}

                //... if joining monitor send this node bary center and nodeId to all available monitors
                ConcurrentHashMap<Address, Point3f> aux = new ConcurrentHashMap<Address, Point3f>();
                aux.putAll(monitors);
                Set st = aux.keySet();
                if (!st.isEmpty()) {
                    Iterator itr = st.iterator();
                    while (itr.hasNext()) {
                        Address monitorNodeId = (Address) itr.next();
                        if (localNodeId != null && !(monitorNodeId.equals(localNodeId))) {
                            sendAddMonitor(aux.get(localNodeId), localNodeId, monitorNodeId);
                        }
                    }
                }
                aux.clear();
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
                Set<Address> st = new LinkedHashSet<Address>(old_view_ids);
                st.removeAll(new_view_ids);

                //... get remote node Id...
                if (!st.isEmpty()) {
                    Iterator itr = st.iterator();
                    while (itr.hasNext()) {
                        Address remoteNodeId = (Address) itr.next();
                        //System.out.println("Node [" + remoteNodeId + "] left/failed");

                        //... remove remote player (if it exists) from the hashtable players
                        if (players.containsKey(remoteNodeId)) {
                            players.remove(remoteNodeId);
                        }

                        //... remove remote monitor (if it exists) from the hashtable monitors
                        if (monitors.containsKey(remoteNodeId)) {
                            monitors.remove(remoteNodeId);
                        }
                    }
                }
            } else { //... if remote node node joins...
                //... after giving some time for other node to start ...
                //System.out.println("Waitting before send data to new grid node.");
                //try {
                //    Thread.sleep(5000);
                //} catch (Exception e) {
                //    throw new RuntimeException(e);
                //}

                Set<Address> st1 = new LinkedHashSet<Address>(new_view_ids);
                st1.removeAll(old_view_ids);

                //... get remote node Id...
                if (!st1.isEmpty()) {
                    Iterator itr1 = st1.iterator();
                    while (itr1.hasNext()) {
                        Address remoteNodeId = (Address) itr1.next();
                        //System.out.println("Node [" + remoteNodeId + "] joined");

                        //... each monitor should unicast to the joining node ...
                        sendAddMonitor(monitors.get(localNodeId), localNodeId, remoteNodeId);

                        //... wait for reply (if joining node is player) from joining playerNode ...
                        //try {
                        //    Thread.sleep(7500);
                        //} catch (Exception e) {
                        //    throw new RuntimeException(e);
                        //}

                        //... if joining node is also a monitor send to local monitor players the new remote monitor data.
                        if (monitors.containsKey(remoteNodeId)) {
                            ConcurrentHashMap<Address, Point3f> aux1 = new ConcurrentHashMap<Address, Point3f>();
                            ConcurrentHashMap<Address, GameUpdate> aux2 = new ConcurrentHashMap<Address, GameUpdate>();
                            aux1.putAll(monitors);
                            aux2.putAll(players);
                            Set st2 = aux2.keySet();
                            if (!st2.isEmpty()) {
                                Iterator itr2 = st2.iterator();
                                while (itr2.hasNext()) {
                                    Address playerNodeId = (Address) itr2.next();
                                    sendAddMonitor(aux1.get(remoteNodeId), remoteNodeId, playerNodeId);
                                }
                            }
                            aux1.clear();
                            aux2.clear();
                        }
                    }
                }
            }

            old_view = new_view;
        }
    }

    /**
     * Reboot this monitor grid node.
     *
     * @param gridConfig location of .xml with the grid configuration to use.
     */
    private void rebootNode(String gridConfig) {
        System.out.println("Rebooting this monitor grid node.");
        monitors.remove(localNodeId);
        stopGridNode();
        startGridNode(gridConfig);
        System.out.println();
    }

    /**
     * Show all existing baryCenters
     */
    private void showAllZonesBaryCenters() {
        for (int j = 0; j < zonesWidth; j++) {
            for (int i = 0; i < zonesLength; i++) {
                System.out.print(zonesCenters[i * zonesLength + j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Given a point p1 show all monitors whose baryCenters are less than two
     * zones of distance, i.e., the monitor zone neighbors.
     *
     * @param p1 a point that either is a player position or a monitor zone
     * baryCenter.
     */
    private void showNeighbors(Point3f p1) {
        System.out.println("Showing all neighbor running monitors.");
        int count = 0;
        ConcurrentHashMap<Address, Point3f> aux = new ConcurrentHashMap<Address, Point3f>();
        aux.putAll(monitors);
        Set st = aux.keySet();
        if (!st.isEmpty()) {
            Iterator itr = st.iterator();
            while (itr.hasNext()) {
                Address monitorNodeId = (Address) itr.next();
                float distance = p1.distance(aux.get(monitorNodeId));
                if ((distance < ((size / zonesLength) * 2)
                        || distance < ((size / zonesWidth) * 2))
                        && !monitorNodeId.equals(localNodeId)) {
                    System.out.println("[" + monitorNodeId + "] " + aux.get(monitorNodeId));
                    count++;
                }
            }
        }
        System.out.println("\nThere are " + count + " neighbor running monitors.");
        aux.clear();
        System.out.println();
    }

    /**
     * Output to terminal all the available terminal commands.
     */
    private void showCommands() {
        System.out.println("Runtime options:\n"
                + "A - show all zones bary centers\n"
                + "N - show neighbour monitors of this monitor\n"
                + "M - show all monitors\n"
                + "P - show all players assigned to this monitor\n"
                + "R - reboot this monitor grid node\n"
                + "X - shutdown this monitor\n");
        System.out.println();
    }

    /**
     * Extended version of update
     *
     * @param gridConfig location of .xml with the grid configuration to use.
     */
    private void update(String gridConfig) {
        if (isGridNodeStarted()) {
            Scanner keyboard = new Scanner(System.in);
            String option = "";
            showCommands();

            do {
                if (keyboard.hasNext()) {
                    option = keyboard.nextLine().toUpperCase();
                    if (option.equalsIgnoreCase("A")) {
                        System.out.flush();
                        showAllZonesBaryCenters();
                        showCommands();
                    } else if (option.equalsIgnoreCase("N")) {
                        System.out.flush();
                        showNeighbors(monitors.get(localNodeId));
                        showCommands();
                    } else if (option.equalsIgnoreCase("M")) {
                        System.out.flush();
                        showMonitors();
                        showCommands();
                    } else if (option.equalsIgnoreCase("P")) {
                        System.out.flush();
                        showPlayers();
                        showCommands();
                    } else if (option.equalsIgnoreCase("R")) {
                        System.out.flush();
                        rebootNode(gridConfig);
                        showCommands();
                    } else if (option.equalsIgnoreCase("X")) {
                        System.out.flush();
                    } else {
                        System.out.flush();
                        System.out.println("Ups ... command is either rong or you need to learn to read.\n");
                        showCommands();
                    }
                }
                update();
            } while (!option.equals("X"));
            stopGridNode();
        }
    }

    @Override
    public void update() {
        if (isGridRunning()) {
            ConcurrentHashMap<Address, GameUpdate> aux = new ConcurrentHashMap<Address, GameUpdate>();
            aux.putAll(remoteGameUpdates);
            remoteGameUpdates.clear();
            Set st = aux.keySet();
            if (!st.isEmpty()) {
                Iterator itr = st.iterator();
                while (itr.hasNext()) {
                    final Address playerNodeId = (Address) itr.next();
                    final GameUpdate gameUpdate = aux.get(playerNodeId);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (players.containsKey(playerNodeId)) {
                                players.remove(playerNodeId);
                                players.put(playerNodeId, gameUpdate);
                            }
                        }
                    }).start();
                }
            }
            aux.clear();
        }
    }
}