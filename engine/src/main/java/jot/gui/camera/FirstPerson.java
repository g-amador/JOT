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
import static jot.gui.camera.Camera.Type.FIRST_PERSON;
import static jot.physics.Kinematics.translatePolar;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements a first person camera.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class FirstPerson extends Camera {

    static final Logger log = getLogger("Camera");

    static {
        log.setLevel(OFF);
    }

    /**
     * {@inheritDoc}
     */
    public FirstPerson(float fov, float aspectRatio, float size, String cameraId) {
        super(fov, aspectRatio, size, cameraId);
        this.type = FIRST_PERSON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(Vector3D position, Vector3D rotation, float dt) {
        this.position = translatePolar(position, 0.25f, (float) this.rotation.getY(), (float) rotation.getY() + 180, dt);
        this.position = new Vector3D(this.position.getX(), position.getY() + 1.3, this.position.getZ());

        this.viewPoint = translatePolar(position, 1.0f, (float) this.rotation.getY(), (float) rotation.getY(), dt);
        this.viewPoint = new Vector3D(this.viewPoint.getX(), position.getY() + 1, this.viewPoint.getZ());
    }
}
