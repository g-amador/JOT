package input.devices;

import javax.vecmath.Point3f;

/**
 * Interface that specifies the core methods to implement for a generic input
 * handler.
 *
 * @author G. Amador & A. Gomes
 */
//TODO: (Projects) implement for other input devices, e.g., Microsoft Kinect, PSEye, Wiimote, PSMove, Joystick.
public interface Input {

    /**
     * Test if for a generic input device and specific provided event is
     * occurring, e.g., mouse button or keyboard key is pressed.
     *
     * @param inputEvent specific provided generic input event, e.g., mouse
     * button pressed, keyboard key press, 3D camera detected movement, etc.
     * @return TRUE if provided input event is occurring, otherwise returns
     * FALSE.
     */
    public boolean isDetecting(String inputEvent);

    /**
     * Get the position in 3D Cartesian space of a generic device cursor, e.g.,
     * for a Wiimote its a X, Y, Z coordinate, for a mouse its the mouse cursor
     * X and Y values, etc.
     *
     * @return X, Y, and Z coordinates of generic cursor.
     */
    public Point3f getPosition();
}