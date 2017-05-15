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
package jot.math.geometry;

/**
 * Interface to specify the default methods to implement for each renderable
 * geometry.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public interface Renderable {

    /**
     * See if render for this geometry object, is activated or deactivated.
     *
     * @return TRUE if this geometry object is renderable, FALSE otherwise.
     */
    boolean isRenderable();

    /**
     * Activate or deactivate render for this geometric object, by default
     * render is set to false.
     *
     * @param renderable TRUE if this geometric object should be render, FALSE
     * otherwise.
     */
    void setRenderable(boolean renderable);
}
