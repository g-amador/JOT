package util;

import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * Class to set up the game constants.
 *
 * @author G. Amador & A. Gomes
 */
public class GameConstants {

    private static final Logger log = getLogger(GameConstants.class.getName());

    /**
     * Method to set up the defaults for the game constants options.
     */
    public static void setGameConstants() {
        //Input devices options
        InputDevicesOptions.useMouse = true;
        InputDevicesOptions.useMouseCursor = false;
        InputDevicesOptions.useKeyBoard = true;

        //Geometry options
        GeometryOptions.showTextures = true;
        GeometryOptions.showWireframe = false;

        //Geometry Scene options
        GeometryOptions.Floor = false;
        GeometryOptions.SkyDome = false;
        GeometryOptions.SkyBox = false;
        GeometryOptions.addSkyBoxCeil = true;
        GeometryOptions.addSkyBoxWalls = true;

        //Geometry generators options
        GeometryOptions.useMazeGenerators = false;

        //Input HOG2Map files options
        GeometryOptions.HOG2Maps = false;

        //Physics options
        PhysicsOptions.doCollisionDetection = false;
        PhysicsOptions.doNarrowPhaseCollisionDetection = false;

        //AI options
        AIOptions.useSteeringBehaviors = true;
        AIOptions.usePathFinders = false;
        AIOptions.usePathFindersDebug = false;
        AIOptions.usePathFindersTimeout = false;
        AIOptions.usePathFindersGraphHashMap = false;
        AIOptions.usePathFindersPotentialFields = false;

        //Extras options
        ExtrasOptions.useLights = true;

        //TODO: read from file        
    }

    /**
     * Default constructor.
     */
    GameConstants() {

    }
}
