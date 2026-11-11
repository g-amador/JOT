package jot.demo.util;

import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.util.CoreOptions.showTextures;
import static jot.util.CoreOptions.showWireframe;
import static jot.util.CoreOptions.useKeyBoard;
import static jot.util.CoreOptions.useMouse;
import static jot.util.CoreOptions.useMouseCursor;
import static jot.util.ExtensionGeometryOptions.useTerrainGenerators;
import static jot.util.ExtensionPhysicsOptions.useCollisionDetection;
import static jot.util.ExtensionPhysicsOptions.useNarrowPhaseCollisionDetection;
import static jot.util.FrameworkOptions.Floor;
import static jot.util.FrameworkOptions.SkyBox;
import static jot.util.FrameworkOptions.SkyDome;
import static jot.util.FrameworkOptions.addSkyBoxCeil;
import static jot.util.FrameworkOptions.addSkyBoxWalls;
import static jot.util.FrameworkOptions.showGeometries;
import static jot.util.FrameworkOptions.showPlanarShadows;
import static jot.util.FrameworkOptions.showShadowMaps;
import static jot.util.FrameworkOptions.showZBuffer;
import static jot.util.FrameworkOptions.useLights;
import static jot.util.FrameworkOptions.useRayTracer;

/**
 * Class to set up the game constants.
 *
 * @author G. Amador & A. Gomes
 */
public class GameConstants {

    static final Logger log = getLogger("GameConstants");

    /**
     * Method to set up the defaults for the game constants options.
     */
    public static void setGameConstants() {
        //Input devices options
        useMouse = true;
        useMouseCursor = true;
        useKeyBoard = true;

        //Geometry options
        showTextures = true;
        showWireframe = false;

        //Geometry generators options
        useTerrainGenerators = false;

        //Physics options
        useCollisionDetection = true;
        useNarrowPhaseCollisionDetection = false;

        //Lights options
        useLights = true;

        //Render options
        useRayTracer = false;

        //Shadows options        
        showPlanarShadows = true;
        showShadowMaps = false;

        //zbuffer options
        showZBuffer = false;

        //Scene options
        showGeometries = true;
        Floor = true;
        SkyDome = false;
        SkyBox = false;
        addSkyBoxCeil = true;
        addSkyBoxWalls = true;

        //TODO: read from file        
    }

    /**
     * Default constructor.
     */
    GameConstants() {
    }
}
