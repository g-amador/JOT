package jot.demo.util;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import static java.lang.Boolean.valueOf;
import static java.lang.String.format;
import static java.lang.System.out;
import static java.lang.System.setOut;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import static jot.util.CoreOptions.Floor;
import static jot.util.CoreOptions.showTextures;
import static jot.util.CoreOptions.showWireframe;
import static jot.util.CoreOptions.useKeyBoard;
import static jot.util.CoreOptions.useMouse;
import static jot.util.CoreOptions.useMouseCursor;
import static jot.util.ExtensionPhysicsOptions.useBroadPhaseCollisionDetection;
import static jot.util.ExtensionPhysicsOptions.useNarrowPhaseCollisionDetection;
import static jot.util.ExtensionPhysicsOptions.useRain;
import static jot.util.ExtensionPhysicsOptions.useRipple;
import static jot.util.FrameworkOptions.SkyBox;
import static jot.util.FrameworkOptions.SkyDome;
import static jot.util.FrameworkOptions.addSkyBoxCeil;
import static jot.util.FrameworkOptions.addSkyBoxWalls;
import static jot.util.FrameworkOptions.showGeometries;
import static jot.util.FrameworkOptions.showPlanarShadows;
import static jot.util.FrameworkOptions.showShadowMaps;
import static jot.util.FrameworkOptions.showZBuffer;
import static jot.util.FrameworkOptions.useLights;

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
        loadGameConstants("assets/scripts/", "GameConstants.js");
    }

    /**
     * Load game constants values from provided file in provided path.
     *
     * @param filePath the path to the file containing the game constants
     * values.
     * @param fileName the file from which to read the game constants values.
     */
    public static void loadGameConstants(String filePath, String fileName) {
        if (GameConstants.class
                .getResource(filePath.replace("assets", "") + fileName) != null) { //Using IDE
            filePath = GameConstants.class.getResource(filePath.replace("assets", "")).getPath();
        }

        parseFile(filePath + fileName);
    }

    /**
     * Parse a game constants values file.
     *
     * @param file the path of the file to parse.
     */
    protected static void parseFile(String file) {
        ByteArrayOutputStream pipeOut = new ByteArrayOutputStream();

        // Store the current System.out
        PrintStream old_out = out;

        // Replace redirect output to our stream
        setOut(new PrintStream(pipeOut));

        // create a script engine manager
        ScriptEngineManager factory = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        // open the Java source file and evaluate the code.
        // File is expected to contain a Runnable implementation
        FileReader reader = null;
        try {
            reader = new FileReader(file);

            // evaluate JavaScript code from String
            engine.eval(reader);

            // Revert back to the old System.out
            setOut(old_out);

            // Write the output to a handy string
            String output = new String(pipeOut.toByteArray());

            for (String str : output.split("\n")) {
                //System.out.println(str);
                String[] args = str.split(" = ");
                switch (args[0]) {
                    //Input devices options
                    case "useMouse":
                        useMouse = valueOf(args[1].trim());
                        break;
                    case "useMouseCursor":
                        useMouseCursor = valueOf(args[1].trim());
                        break;
                    case "useKeyBoard":
                        useKeyBoard = valueOf(args[1].trim());
                        break;
                    //Geometry options
                    case "showTextures":
                        showTextures = valueOf(args[1].trim());
                        break;
                    case "showWireframe":
                        showWireframe = valueOf(args[1].trim());
                        break;
                    //Collisions options
                    case "useBroadPhaseCollisionDetection":
                        useBroadPhaseCollisionDetection = valueOf(args[1].trim());
                        break;
                    case "useNarrowPhaseCollisionDetection":
                        useNarrowPhaseCollisionDetection = valueOf(args[1].trim());
                        break;
                    //Ripple options  
                    case "useRain":
                        useRain = valueOf(args[1].trim());
                        break;
                    case "useRipple":
                        useRipple = valueOf(args[1].trim());
                        break;
                    //Lagrangian simulators options
                    //case "useParticleSystems3D":
                    //    useParticleSystems3D = valueOf(args[1].trim());
                    //    break;
                    //Lights options
                    case "useLights":
                        useLights = valueOf(args[1].trim());
                        break;
                    //Z-Buffer
                    case "showZBuffer":
                        showZBuffer = valueOf(args[1].trim());
                        break;
                    //Shadows options  
                    case "showPlanarShadows":
                        showPlanarShadows = valueOf(args[1].trim());
                        break;
                    case "showShadowMaps":
                        showShadowMaps = valueOf(args[1].trim());
                        break;
                    //Scene options
                    case "showGeometries":
                        showGeometries = valueOf(args[1].trim());
                        break;
                    case "Floor":
                        Floor = valueOf(args[1].trim());
                        break;
                    case "SkyDome":
                        SkyDome = valueOf(args[1].trim());
                        break;
                    case "SkyBox":
                        SkyBox = valueOf(args[1].trim());
                        break;
                    case "addSkyBoxCeil":
                        addSkyBoxCeil = valueOf(args[1].trim());
                        break;
                    case "addSkyBoxWalls":
                        addSkyBoxWalls = valueOf(args[1].trim());
                        break;
                    default:
                        log.log(INFO, format("%s unknown Constant.", args[0]));
                }
            }
        } catch (FileNotFoundException | ScriptException ex) {
            log.log(SEVERE, null, ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                pipeOut.close();
            } catch (IOException ex) {
                log.log(SEVERE, null, ex);
            }
            //old_out.close();
        }
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private GameConstants() {
    }
}
