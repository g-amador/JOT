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

import static com.jogamp.newt.event.InputEvent.AUTOREPEAT_MASK;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import static java.lang.String.format;
import java.util.ArrayList;
import java.util.HashMap;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.io.device.Input;
import static jot.util.CoreOptions.coreOptions;

/**
 * Class that implements a keyboard input handler.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class KeyBoard implements Input, KeyListener {

    static final Logger log = getLogger("KeyBoardHandler");

    static {
        log.setLevel(OFF);
    }

    private final boolean[] keys;
    private final HashMap<String, ArrayList<Integer>> inputEvents = new HashMap<>();

    /**
     * Constructor.
     */
    public KeyBoard() {
        if (coreOptions.get("useKeyBoardDebug")) {
            log.setLevel(INFO);
        }

        this.keys = new boolean[256];
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (coreOptions.get("useKeyBoard")
                && (0 == (AUTOREPEAT_MASK & e.getModifiers()))) { //This if disables auto-repeat.
            log.info(format("Key {0} pressed", e.getKeyChar()));
            this.keys[e.getKeyChar()] = true;
            //keys[e.getKeyCode()] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (coreOptions.get("useKeyBoard")
                && (0 == (AUTOREPEAT_MASK & e.getModifiers()))) { //This if disables auto-repeat.
            log.info(format("Key {0} released", e.getKeyChar()));
            this.keys[e.getKeyChar()] = false;
            //keys[e.getKeyCode()] = false;        
        }
    }

    @Override
    public void registerInputEvent(String inputEvent, int inputDeviceEvent) {
        if (coreOptions.get("useKeyBoard")) {
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
        if (coreOptions.get("useKeyBoard")
                && this.inputEvents.get(inputEvent) != null) {
            boolean result = false;
            for (Integer inputDeviceEvent : this.inputEvents.get(inputEvent)) {
                if (this.keys[inputDeviceEvent]) {
                    log.info(inputEvent);
                    log.info(Boolean.toString(this.keys[inputDeviceEvent]));
                    this.keys[inputDeviceEvent] = false;
                    result = true;
                }
            }
            return result;
        }
        return false;
    }

    @Override
    public boolean isContinuouslyDetecting(String inputEvent) {
        if (coreOptions.get("useKeyBoard") && this.inputEvents.get(inputEvent) != null) {
            for (Integer inputDeviceEvent : this.inputEvents.get(inputEvent)) {
                if (this.keys[inputDeviceEvent]) {
                    log.info(inputEvent);
                    log.info(Boolean.toString(this.keys[inputDeviceEvent]));
                    return true;
                }
            }
            return false;
        }
        return false;
    }
}
