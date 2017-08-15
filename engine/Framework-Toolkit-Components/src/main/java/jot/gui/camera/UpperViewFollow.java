/*
 * This file is part of the JOT game engine framework toolkit component. 
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
package jot.gui.camera;

import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.gui.camera.Camera.Type.UPPER_VIEW_FOLLOW;
import static jot.physics.Kinematics.translatePolar;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements a upper view follow camera.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class UpperViewFollow extends Camera {

    static final Logger log = getLogger("Camera");

    static {
        log.setLevel(OFF);
    }

    /**
     * {@inheritDoc}
     */
    public UpperViewFollow(float fov, float aspectRatio, float size, String cameraId) {
        super(fov, aspectRatio, size, cameraId);
        this.type = UPPER_VIEW_FOLLOW;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(Vector3D position, Vector3D rotation, float dt) {
        float height = this.size / 4;
        if (height < 100) {
            height = 100;
        } else {
            height = this.size / 4;
        }
        this.position = new Vector3D(position.getX(), height, position.getZ());

        this.viewPoint = translatePolar(position, 1.0f, (float) this.rotation.getY(), 0, dt);
    }
}
