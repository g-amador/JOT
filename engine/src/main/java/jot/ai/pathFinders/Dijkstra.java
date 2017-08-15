package jot.ai.pathFinders;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.max;
import static java.lang.System.nanoTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.math.graph.Node;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements Dijkstra's pathfinding algorithm.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Dijkstra extends AbstractPathFinder {

    static final Logger log = getLogger("AbstractPathFinder");

    /**
     * Default constructor.
     */
    public Dijkstra() {
        if (extensionAIOptions.get("usePathFindersDebug")) {
            log.setLevel(INFO);
        }
    }

    /**
     * Dijkstra's algorithm [1].
     *
     * [1] E. W. Dijkstra. (1959). "A note on two problems in connexion with
     * graphs". Numer. Math. 1, 1 (December 1959), 269-271.
     * doi:10.1007/BF01386390.
     *
     * @param startPosition the Cartesian coordinates of the starting node.
     * @param goalPosition the Cartesian coordinates of the destination node.
     * @param timeout the amount of time at most a pathfinder can attempt to
     * find a path.
     * @return if a path is found returns, an array of Vector3D coordinates of
     * the points to go from an start to an target position. Otherwise returns
     * NULL.
     */
    @Override
    public Vector3D[] findPath(Vector3D startPosition, Vector3D goalPosition,
            long timeout) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.start_time = nanoTime();
            this.iterations = 0;
            this.maxTimePerIteration = 0;
            this.minTimePerIteration = MAX_VALUE;
            this.totalMemoryConsumption = 0;
            this.neighborsSize = 0;
            this.GraphNodesSize = 0;
            Node start = new Node(startPosition);
            Node goal = new Node(goalPosition);

            PriorityQueue<Node> open = new PriorityQueue<>();   //The set of unsettled vertices.
            //ArrayList<Node> closed = new ArrayList<>();
            //LinkedList<Node> closed = new LinkedList<>();
            //HashSet<Node> closed = new HashSet<>();   
            HashMap<Vector3D, Node> closed = new HashMap<>();   //The set of settled vertices, the vertices whose shortest distances from the source have been found.            
            this.GraphNodes = new HashMap<>();                  //Each individual node of the graph, i.e., each non repeated line vertex.
            if (extensionAIOptions.get("usePathFindersResetVisited")) {
                this.visited = new ArrayList<>();               //All the (current, neighbor) node pairs for each iteration.
                this.visited_hs = new HashSet<>();
            }
            this.setupGraphNodes();

            start.g = 0.0f;
            start.h = 0.0f;
            start.f = start.g + start.h;
            //open.offer(start);
            open.add(start);
            this.GraphNodes.put(start.position, start);

            if (extensionAIOptions.get("usePathFindersDebug")) {
                this.GraphNodesSize = this.GraphNodes.size();
            }

            while (!open.isEmpty()) {
                if (extensionAIOptions.get("usePathFindersDebug")) {
                    this.iterations++;
                    this.iterationStartTime = nanoTime();
                    long oldUsedMemoryConsumption = (open.size() + closed.size()
                            + this.neighborsSize + this.GraphNodesSize)
                            * (3 * 8 + 3 * 4 + 8) // a node object approximate memory size.
                            + (/*visited.size() * visited_hs.size()
                            +*/closed.size() + this.GraphNodesSize)
                            * (3 * 8);  // the key object memory approximate size.
                    this.totalMemoryConsumption = max(this.totalMemoryConsumption, oldUsedMemoryConsumption);
                }

                if (extensionAIOptions.get("usePathFindersTimeout")
                        && (nanoTime() - this.start_time) > timeout) {
                    log.info("No path available by timeout.");
                    return null;
                }

                //Node current = open.peek();
                Node current = open.poll();
                //open.remove(current);
                //closed.add(current);
                closed.put(current.position, current);

                if (current.equals(goal)) {
                    Vector3D[] solution = this.reconstruct_path(current);
                    this.pathFoundLog("Dijkstra", solution);
                    return solution;
                }

                ArrayList<Node> neighbors = this.getNeighbors(current);
                if (extensionAIOptions.get("usePathFindersDebug")) {
                    this.neighborsSize = neighbors.size();
                }
                neighbors.stream().forEach(neighbor -> {
                    this.visited.add(current.position);
                    this.visited.add(neighbor.position);
                    this.visited_hs.add(current.position);
                    this.visited_hs.add(neighbor.position);

                    //if (!closed.contains(neighbor)) {
                    if (!closed.containsKey(neighbor.position)) { //Ignore the neighbor which is already evaluated.
                        float tentative_g_score = current.g + this.getCost(current, neighbor);

                        //if (!open.contains(neighbor)) {
                        //    open.add(neighbor); //Discover a new node
                        //} else if (tentative_g_score >= neighbor.g) {
                        //    continue; //this is not a better path.
                        //}
                        boolean open_contains_neighbor = open.contains(neighbor);
                        if (!open_contains_neighbor
                                || tentative_g_score < neighbor.g) {
                            neighbor.parent = current;
                            neighbor.g = tentative_g_score;
                            neighbor.h = 0.0f;
                            neighbor.f = neighbor.g + neighbor.h;
                            this.GraphNodes.put(neighbor.position, neighbor);

                            if (!open_contains_neighbor) {
                                //open.offer(neighbor);
                                open.add(neighbor); //Discover a new node
                            }
                        }
                    }
                });
            }
            this.pathNotFoundLog("Dijkstra");
        }

        return null;
    }
}
