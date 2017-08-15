/*
 * This file is part of the JOT game engine core toolkit component.
 * Copyright (C) 2014 Gonçalo Amador & Abel Gomes
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * E-mail Contacts: G. Amador (g.n.p.amador@gmail.com) & 
 *                  A. Gomes (agomes@it.ubi.pt)
 */
package jot.io.device.handlers;

import com.jogamp.newt.event.MouseEvent;
import static com.jogamp.newt.event.MouseEvent.BUTTON_COUNT;
import com.jogamp.newt.event.MouseListener;
import static java.lang.String.format;
import java.util.ArrayList;
import java.util.HashMap;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.io.device.TrackableInput;
import static jot.util.CoreOptions.coreOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements a mouse input handler.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Mouse implements TrackableInput, MouseListener {

    static final Logger log = getLogger("MouseHandler");

    static {
        log.setLevel(OFF);
    }

    private boolean movedX;
    private boolean movedY;
    private boolean mouseOnScreen;
    private MouseEvent mouse;
    private MouseEvent oldMouse;
    private MouseEvent newMouse;
    private final boolean[] buttons;
    private final HashMap<String, ArrayList<Integer>> inputEvents = new HashMap<>();

    /**
     * Constructor.
     */
    public Mouse() {
        if (coreOptions.get("useMouseDebug")) {
            log.setLevel(INFO);
        }

        this.buttons = new boolean[BUTTON_COUNT];
        this.movedX = true;
        this.movedY = true;
        this.mouseOnScreen = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (coreOptions.get("useMouse")) {
            this.mouse = e;
            this.buttons[e.getButton() - 1] = true;
            log.info(format("Button {0} pressed", e.getButton()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (coreOptions.get("useMouse")) {
            this.mouse = null;
            this.buttons[e.getButton() - 1] = false;
            log.info(format("Button {0} released", e.getButton()));
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (coreOptions.get("useMouse")) {
            this.mouse = e;
            this.mouseOnScreen = true;
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (coreOptions.get("useMouse")) {
            this.mouse = null;
            this.mouseOnScreen = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (coreOptions.get("useMouse")) {
            this.oldMouse = this.newMouse;
            this.newMouse = e;
            this.movedX = true;
            this.movedY = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        if (coreOptions.get("useMouse")) {
            this.oldMouse = this.newMouse;
            this.newMouse = e;
            this.movedX = true;
            this.movedY = true;
        }
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        if (coreOptions.get("useMouse")) {
            this.mouse = e;
        }
    }

    @Override
    public void registerInputEvent(String inputEvent, int inputDeviceEvent) {
        if (coreOptions.get("useMouse")) {
            ArrayList<Integer> inputDeviceEvents = new ArrayList<>();
            if (this.inputEvents.get(inputEvent) != null) {
                inputDeviceEvents.addAll(this.inputEvents.get(inputEvent));
            }
            inputDeviceEvents.add(inputDeviceEvent);
            this.inputEvents.put(inputEvent, inputDeviceEvents);
        }
    }

    @Override
    public boolean isDetecting(String inputEvent) {
        if (coreOptions.get("useMouse")
                && this.mouse != null
                && this.inputEvents.get(inputEvent) != null) {
            boolean result = false;
            for (Integer inputDeviceEvent : this.inputEvents.get(inputEvent)) {
                if (this.buttons[inputDeviceEvent - 1]) {
                    log.info(inputEvent);
                    log.info(Boolean.toString(this.buttons[inputDeviceEvent - 1]));
                    this.buttons[inputDeviceEvent - 1] = false;
                    result = true;
                }
            }
            return result;
        }
        return false;
    }

    @Override
    public boolean isContinuouslyDetecting(String inputEvent) {
        if (coreOptions.get("useMouse")
                && this.mouse != null
                && this.inputEvents.get(inputEvent) != null) {
            for (Integer inputDeviceEvent : this.inputEvents.get(inputEvent)) {
                if (this.buttons[inputDeviceEvent - 1]) {
                    log.info(inputEvent);
                    log.info(Boolean.toString(this.buttons[inputDeviceEvent - 1]));
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public Vector3D getAcceleration() {
        //TODO: calculate accelaration of movement and return it.
        return ZERO;
    }

    @Override
    public Vector3D getPosition() {
        if (coreOptions.get("useMouse") && this.oldMouse != null) {
            return new Vector3D(this.newMouse.getX(), this.newMouse.getY(), 0);
        }
        return ZERO;
    }

    @Override
    public Vector3D getPositionShift() {
        if (coreOptions.get("useMouse")) {
            if (this.oldMouse != null) {
                if (this.mouseOnScreen) {
                    if (this.movedX || this.movedY) {
                        return this.positionShift();
                    }
                } else {
                    return coreOptions.get("useMouseOffScreenPositionShift")
                            ? this.positionShift() : ZERO;
                }
            }
        }
        return ZERO;
    }

    private Vector3D positionShift() {
        this.movedX = false;
        this.movedY = false;
        float x = this.oldMouse.getX() - this.newMouse.getX();
        float y = this.oldMouse.getY() - this.newMouse.getY();

        //TODO: infer z value in some way.
        float z = 0.0f;
        return new Vector3D(x, y, z);
    }
}
