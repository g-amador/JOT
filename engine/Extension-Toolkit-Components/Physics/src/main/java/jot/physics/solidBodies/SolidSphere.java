/**
 * Port of Benedikt Bitterli incremental fluids source available at,
 * https://github.com/tunabrain/incremental-fluids
 *
 * Copyright (c) 2013 Benedikt Bitterli
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not claim
 * that you wrote the original software. If you use this software in a product,
 * an acknowledgment in the product documentation would be appreciated but is
 * not required.
 *
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 *
 * 3. This notice may not be removed or altered from any source distribution.
 */
package jot.physics.solidBodies;

import static java.util.logging.Level.ALL;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Represents a sphere (circle) of diameter 1. Can be scaled to the appropriate
 * size
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class SolidSphere extends AbstractSolidBody {

    static final Logger log = getLogger("SolidSphere");

    static {
        log.setLevel(ALL);
    }

    /**
     * Constructor
     *
     * @param x
     * @param y
     * @param s
     * @param t
     * @param vx
     * @param vy
     * @param vt
     */
    public SolidSphere(double x, double y, double s, double t, double vx, double vy, double vt) {
        super(x, y, s, s, t, vx, vy, vt);
    }

    @Override
    public double distance(double x, double y) {
        return length(x - this._posX, y - this._posY) - this._scaleX * 0.5;
    }

    @Override
    public Vector2D closestSurfacePoint(double x, double y) {
        Vector2D result = this.global2Local(x, y);
        x = result.getX();
        y = result.getY();

        double r = length(x, y);
        if (r < 1e-4) {
            x = 0.5;
            y = 0.0;
        } else {
            x /= 2.0 * r;
            y /= 2.0 * r;
        }

        return this.local2Global(x, y);
    }

    @Override
    public Vector2D distanceNormal(double nx, double ny, double x, double y) {
        x -= this._posX;
        y -= this._posY;
        double r = length(x, y);
        if (r < 1e-4) {
            nx = 1.0;
            ny = 0.0;
        } else {
            nx = x / r;
            ny = y / r;
        }

        return new Vector2D(nx, ny);
    }
}
