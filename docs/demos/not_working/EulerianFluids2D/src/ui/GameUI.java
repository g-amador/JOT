package ui;

import com.jogamp.opengl.GL2;

/**
 * Abstract class that specifies the methods that the gameMain class (the game
 * logic class) must implement. Based on source available at (last consulted on
 * 29 - 07 - 2012):
 * http://www3.ntu.edu.sg/home/ehchua/programming/java/J8d_Game_Framework.html
 *
 * @author G. Amador & A. Gomes
 */
public abstract class GameUI {

    /**
     * Enumeration for the states of the game.
     */
    public static enum State {

        LOADING, INITIALIZED, PLAYING, PAUSED, GAME_OVER
    }
    static State state;   // current state of the game

    /**
     * Initialize all default game content, i.e., player, obstacles, SkyBox, and
     * floor.
     */
    public abstract void gameInit();

    /**
     * Shutdown the game, clean up code that runs only once.
     *
     * @param gl
     */
    public abstract void gameShutdown(GL2 gl);

    /**
     * Update the state and position of all the game objects, detect collisions
     * and provide responses.
     */
    public abstract void gameUpdate();

    /**
     * Refresh the display.
     *
     * @param gl
     */
    public abstract void gameRender(GL2 gl);
}
