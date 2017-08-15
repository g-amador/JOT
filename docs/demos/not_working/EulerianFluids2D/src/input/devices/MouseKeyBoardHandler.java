package input.devices;

import java.awt.event.*;
import javax.vecmath.Point3f;

/**
 * Class that implements a mouse and keyboard input handler.
 *
 * @author G. Amador & A. Gomes
 */
public class MouseKeyBoardHandler implements Input, KeyListener,
        MouseListener, MouseMotionListener {

    private boolean movedX;
    private boolean movedY;
    private boolean[] keys;
    //private boolean keyTyped;
    private KeyEvent lastKeyTyped;
    private MouseEvent mouse;
    private MouseEvent oldMouse;
    private MouseEvent newMouse;

    /**
     * Default constructor.
     */
    public MouseKeyBoardHandler() {
        this.keys = new boolean[256];
        this.movedX = true;
        this.movedY = true;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        lastKeyTyped = e;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouse = e;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouse = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouse = null;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouse = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        oldMouse = newMouse;
        newMouse = e;
        movedX = true;
        movedY = true;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        oldMouse = newMouse;
        newMouse = e;
        movedX = true;
        movedY = true;
    }

    @Override
    public boolean isDetecting(String inputEvent) {
        if ((lastKeyTyped != null) && (inputEvent.equals(lastKeyTyped.getKeyChar() + ""))) {
            //keyTyped = false;
            lastKeyTyped = null;
            return true;
        }

        if (inputEvent.equals("button1")) {
            if (mouse != null) {
                return (mouse.getButton() == MouseEvent.BUTTON1);
            }
        }

        if (inputEvent.equals("button3")) {
            if (mouse != null) {
                return (mouse.getButton() == MouseEvent.BUTTON3 && newMouse.getID() == MouseEvent.MOUSE_DRAGGED);
            }
        }

        return false;
    }

    @Override
    public Point3f getPosition() {
        if ((oldMouse != null) && (movedX || movedY)) {
//            movedX = false;
//            movedY = false;
//            float x = (oldMouse.getX() - newMouse.getX());
//            float y = (oldMouse.getY() - newMouse.getY());
//
//            //TODO: (SceneManagement Extras) infer z value in some way.
//            float z = 0.0f;
//            return new Point3f(x, y, z);
            return new Point3f(newMouse.getX(), newMouse.getY(), 0);
        }

        return new Point3f();
    }
}
