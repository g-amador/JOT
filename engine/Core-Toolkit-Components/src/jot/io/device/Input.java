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
package jot.io.device;

/**
 * Interface that specifies the core methods to implement for a generic input
 * handler.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public interface Input {

    /**
     * Register a specific inputEvent to be associated with a generic input
     * device event, e.g., associate "shoot" with a key pressed.
     *
     * @param inputEvent to be associated with a generic input device event.
     * @param inputDeviceEvent generic input device event.
     */
    void registerInputEvent(String inputEvent, int inputDeviceEvent);

    /**
     * Test if for a generic input device and specific provided event is
     * occurring, e.g., mouse button or keyboard key is typed/clicked.
     *
     * @param inputEvent specific provided generic input event, e.g., mouse
     * button clicked, keyboard key typed, 3D camera detected movement, etc.
     * @return TRUE if provided input event is occurring, otherwise returns
     * FALSE.
     */
    boolean isDetecting(String inputEvent);

    /**
     * Test if for a generic input device and specific provided event is
     * occurring, e.g., mouse button or keyboard key is pressed.
     *
     * @param inputEvent specific provided generic input event, e.g., mouse
     * button pressed, keyboard key press, 3D camera detected movement, etc.
     * @return TRUE if provided input event is occurring, otherwise returns
     * FALSE.
     */
    boolean isContinuouslyDetecting(String inputEvent);
}
