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

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.signum;
import static java.util.logging.Level.ALL;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Represents a box (square) of size 1x1. Can be scaled to the appropriate size
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class SolidBox extends AbstractSolidBody {

    static final Logger log = getLogger("SolidBox");

    static {
        log.setLevel(ALL);
    }

    /**
     * Non-zero signum
     *
     * @param val
     * @return
     */
    static private double nsgn(double val) {
        double result = signum(val);
        return abs(result) == 0 ? 0 : result;
    }

    /**
     * Constructor
     *
     * @param x
     * @param y
     * @param sx
     * @param sy
     * @param t
     * @param vx
     * @param vy
     * @param vt
     */
    public SolidBox(double x, double y, double sx, double sy, double t, double vx, double vy, double vt) {
        super(x, y, sx, sy, t, vx, vy, vt);
    }

    @Override
    public double distance(double x, double y) {
        x -= this._posX;
        y -= this._posY;
        Vector2D result = rotate(x, y, -this._theta);
        double dx = abs(result.getX()) - this._scaleX * 0.5;
        double dy = abs(result.getY()) - this._scaleY * 0.5;

        return dx >= 0.0 || dy >= 0.0
                ? length(max(dx, 0.0), max(dy, 0.0)) : max(dx, dy);
    }

    @Override
    public Vector2D closestSurfacePoint(double x, double y) {
        x -= this._posX;
        y -= this._posY;
        Vector2D result = rotate(x, y, -this._theta);
        x = result.getX();
        y = result.getY();
        double dx = abs(x) - this._scaleX * 0.5;
        double dy = abs(y) - this._scaleY * 0.5;

        if (dx > dy) {
            x = nsgn(x) * 0.5 * this._scaleX;
        } else {
            y = nsgn(y) * 0.5 * this._scaleY;
        }

        result = rotate(x, y, this._theta);
        x = result.getX() + this._posX;
        y = result.getY() + this._posY;

        return new Vector2D(x, y);
    }

    @Override
    public Vector2D distanceNormal(double nx, double ny, double x, double y) {
        x -= this._posX;
        y -= this._posY;
        Vector2D result = rotate(x, y, -this._theta);
        x = result.getX();
        y = result.getY();

        if (abs(x) - this._scaleX * 0.5 > abs(y) - this._scaleY * 0.5) {
            nx = nsgn(x);
            ny = 0.0;
        } else {
            nx = 0.0;
            ny = nsgn(y);
        }

        return rotate(nx, ny, this._theta);
    }
}
