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
package jot.gui.camera;

import static com.jogamp.opengl.GL.GL_VIEWPORT;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import com.jogamp.opengl.glu.GLU;
import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.physics.Ray;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.PLUS_I;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.PLUS_J;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Abstract class that implements a camera.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class Camera {

    protected static final Logger log = getLogger("Camera");

    static {
        log.setLevel(OFF);
    }

    protected final float size;
    protected float fov; //radians
    protected float aspectRatio;
    protected Vector3D rightVector;
    protected Vector3D upVector;
    protected Vector3D viewPoint;
    protected Vector3D rotation;
    protected Vector3D position;

    protected double xminRequested = -5, xmaxRequested = 5;
    protected double yminRequested = -5, ymaxRequested = 5;
    protected double zmin = -10, zmax = 10;
    protected boolean orthographic = true;
    protected boolean preserveAspect = true;

    protected double xminActual, xmaxActual, yminActual, ymaxActual;
    protected final GLU glu;

    /**
     * The type of this camera.
     */
    public Type type;

    /**
     * This camera id.
     */
    public String cameraId;

    /**
     * Constructor.
     *
     * @param fov camera field of view.
     * @param aspectRatio camera perspective aspect ratio.
     * @param size the size of the game world.
     * @param cameraId the unique identifier for this camera.
     */
    protected Camera(float fov, float aspectRatio, float size, String cameraId) {

        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.size = size;
        this.cameraId = cameraId;
        this.glu = new GLU();
        this.viewPoint = ZERO;
        this.rotation = ZERO;
        this.position = ZERO;
        this.upVector = PLUS_J;
    }

    /**
     * Constructor.
     *
     * @param fov camera field of view.
     * @param aspectRatio camera perspective aspect ratio.
     * @param size the size of the game world.
     * @param cameraId the unique identifier for this camera.
     * @param position
     * @param viewPoint
     */
    protected Camera(float fov, float aspectRatio, float size, String cameraId,
            Vector3D position, Vector3D viewPoint) {
        this.fov = (float) toRadians(fov);
        this.aspectRatio = aspectRatio;
        this.size = size;
        this.cameraId = cameraId;
        this.viewPoint = viewPoint;//.normalize();
        this.position = position;
        this.glu = new GLU();
        this.rotation = ZERO;
        this.rightVector = PLUS_I.scalarMultiply(aspectRatio);
        this.upVector = PLUS_J;
    }

    /**
     * Get whether the current projection is orthographic or perspective. The
     * default is perspective.
     *
     * @return TRUE if current projection is orthographic, FALSE if current
     * projection is perspective.
     */
    public boolean getOrthographic() {
        return this.orthographic;
    }

    /**
     * Determine whether the projection is orthographic or perspective. The
     * default is perspective.
     *
     * @param orthographic set to true for orthographic projection and to false
     * for perspective projection.
     */
    public void setOrthographic(boolean orthographic) {
        this.orthographic = orthographic;
    }

    /**
     * Get whether the xy-limits should be adjusted to match the aspect ratio of
     * the display area. The default is true.
     *
     * @return TRUE if the xy-limits are adjusted to match the aspect ratio of
     * the display area, FALSE if are not adjusted to match the aspect ratio of
     * the display area.
     */
    public boolean getPreserveAspect() {
        return this.preserveAspect;
    }

    /**
     * Determine whether the xy-limits should be adjusted to match the aspect
     * ratio of the display area. The default is true.
     *
     * @param preserveAspect
     */
    public void setPreserveAspect(boolean preserveAspect) {
        this.preserveAspect = preserveAspect;
    }

    /**
     * Set the limits of the view volume. The limits are set with respect to the
     * viewing coordinates. That is, the view center is assumed to be at the
     * point (0,0) in the plane of the screen. The view up vector (more
     * precisely, its projection onto the screen) points upwards on the screen.
     * The z-axis is perpendicular to the screen, with the positive direction of
     * the z-axis pointing out of the screen. In this coordinate system, xmin
     * and xmax give the horizontal limits on the screen, ymin and ymax give the
     * vertical limits on the screen, and zmin and zmax give the limits of the
     * view volume along the z-axis. (Note that this is NOT exactly the same as
     * the parameters in either glOrtho or glFrustum! Most important to note is
     * that zmin and zmax are given with reference to the view center, not the
     * eye.) Note that xmin/xmax or ymin/ymax might be adjusted to match the
     * aspect ratio of the display area.
     *
     * @param xmin
     * @param xmax
     * @param ymin
     * @param ymax
     * @param zmin
     * @param zmax
     */
    public void setLimits(double xmin, double xmax, double ymin, double ymax, double zmin, double zmax) {
        this.xminRequested = this.xminActual = xmin;
        this.xmaxRequested = this.xmaxActual = xmax;
        this.yminRequested = this.yminActual = ymin;
        this.ymaxRequested = this.ymaxActual = ymax;
        this.zmin = zmin;
        this.zmax = zmax;
    }

    /**
     * Get camera rotation.
     *
     * @return value of camera rotation.
     */
    public Vector3D getRotation() {
        return this.rotation;
    }

    /**
     * Set camera rotation.
     *
     * @param rotation value of camera rotation.
     */
    public void setRotation(Vector3D rotation) {
        this.rotation = rotation;
    }

    /**
     * Get camera position.
     *
     * @return value of camera position.
     */
    public Vector3D getPosition() {
        return this.position;
    }

    /**
     * Set camera position.
     *
     * @param position of the camera.
     */
    public void setPosition(Vector3D position) {
        this.position = position;
    }

    /**
     * Get camera up vector.
     *
     * @return value of camera up vector.
     */
    public Vector3D getUpVector() {
        return this.upVector;
    }

    /**
     * Set camera up vector.
     *
     * @param upVector value of camera up vector.
     */
    public void setUpVector(Vector3D upVector) {
        this.upVector = upVector;
    }

    /**
     * Get camera view point.
     *
     * @return value of camera view point.
     */
    public Vector3D getViewPoint() {
        return this.viewPoint;
    }

    /**
     * Set camera view point.
     *
     * @param viewPoint value of camera view point.
     */
    public void setViewPoint(Vector3D viewPoint) {
        this.viewPoint = viewPoint;
    }

    /**
     * Get camera field of view.
     *
     * @return value of camera field of view.
     */
    public float getFieldOfView() {
        return this.fov;
    }

    /**
     * Set camera field of view.
     *
     * @param fov value of camera field of view.
     */
    public void setFieldOfView(float fov) {
        this.fov = fov;
    }

    /**
     * Get camera aspect ratio.
     *
     * @return value of camera aspect ratio.
     */
    public float getAspectRatio() {
        return this.aspectRatio;
    }

    /**
     * Set camera aspect ratio.
     *
     * @param aspectRatio value of camera aspect ratio.
     */
    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    /**
     * Configure glutLookAt with camera position, viewpoint and up vector or
     * with a given position, view point (0,0,0) and up vector (0, 1, 0).
     *
     * @param position
     */
    public void lookAt(float[] position) {
        if (position == null) {
            this.glu.gluLookAt(
                    this.position.getX(), this.position.getY(), this.position.getZ(),
                    this.viewPoint.getX(), this.viewPoint.getY(), this.viewPoint.getZ(),
                    this.upVector.getX(), this.upVector.getY(), this.upVector.getZ());
        } else {
            this.glu.gluLookAt(
                    position[0], position[1], position[2],
                    0, 0, 0,
                    0, 1, 0);
        }
    }

    /**
     * Apply the camera to an OpenGL context. This method completely replaces
     * the projection and the modelview transformation in the context. It sets
     * these transformations to the identity and then applies the view and
     * projection represented by the camera. This method is meant to be called
     * at the begining of the display method and should replace any other means
     * of setting the projection and view.
     *
     * @param gl
     * @param position
     * @param setupLookAt
     */
    public void apply(GL2 gl, float[] position, boolean setupLookAt) {
        int[] viewport = new int[4];
        gl.glGetIntegerv(GL_VIEWPORT, viewport, 0);
        this.xminActual = this.xminRequested;
        this.xmaxActual = this.xmaxRequested;
        this.yminActual = this.yminRequested;
        this.ymaxActual = this.ymaxRequested;
        if (this.preserveAspect) {
            double viewWidth = viewport[2];
            double viewHeight = viewport[3];
            double windowWidth = this.xmaxActual - this.xminActual;
            double windowHeight = this.ymaxActual - this.yminActual;
            double aspect = viewHeight / viewWidth;
            double desired = windowHeight / windowWidth;
            if (desired > aspect) { //expand width
                double extra = (desired / aspect - 1.0) * (this.xmaxActual - this.xminActual) / 2.0;
                this.xminActual -= extra;
                this.xmaxActual += extra;
            } else if (aspect > desired) {
                double extra = (aspect / desired - 1.0) * (this.ymaxActual - this.yminActual) / 2.0;
                this.yminActual -= extra;
                this.ymaxActual += extra;
            }
        }
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        double viewDistance = this.norm(new double[]{
            this.viewPoint.getX() - this.position.getX(),
            this.viewPoint.getY() - this.position.getY(),
            this.viewPoint.getZ() - this.position.getZ()});
        if (this.orthographic) {
            gl.glOrtho(this.xminActual, this.xmaxActual, this.yminActual, this.ymaxActual,
                    viewDistance - this.zmax, viewDistance - this.zmin);
        } else {
            double near = viewDistance - this.zmax;
            if (near < 0.1) {
                near = 0.1;
            }
            double centerx = (this.xminActual + this.xmaxActual) / 2;
            double centery = (this.yminActual + this.ymaxActual) / 2;
            double newwidth = (near / viewDistance) * (this.xmaxActual - this.xminActual);
            double newheight = (near / viewDistance) * (this.ymaxActual - this.yminActual);
            double x1 = centerx - newwidth / 2;
            double x2 = centerx + newwidth / 2;
            double y1 = centery - newheight / 2;
            double y2 = centery + newheight / 2;
            gl.glFrustum(x1, x2, y1, y2, near, viewDistance - this.zmin);
        }
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        if (setupLookAt) {
            this.lookAt(position);
        }
    }

    protected double norm(double[] v) {
        double norm2 = v[0] * v[0] + v[1] * v[1] + v[2] * v[2];
        if (isNaN(norm2) || isInfinite(norm2) || norm2 == 0) {
            throw new NumberFormatException("Vector length zero, undefined, or infinite.");
        }
        return sqrt(norm2);
    }

    /**
     * Set camera position and rotation, depending on the type of camera.
     *
     * @param position value of the controlled player position.
     * @param rotation value of the controlled player rotation.
     * @param dt the amount of elapsed game time since the last frame.
     */
    public abstract void update(Vector3D position, Vector3D rotation, float dt);

    /**
     * Sample a ray for a given direction.
     *
     * @param x
     * @param y
     * @return the sampled ray.
     */
    public Ray getSampleRay(double x, double y) {
        return new Ray(this.position, this.getSampleDirection(x, y));
    }

    protected double getImagePlaneBoxSideLength() {
        // viewPoint_length * 2 * tan(fov / 2) = side_length
        // where viewPoint_length == 1
        return 2 * tan(this.fov / 2);
    }

    protected Vector3D getSampleDirection(double x, double y) {
        Vector3D base = this.rightVector.scalarMultiply(x).add(this.upVector.scalarMultiply(y));
        Vector3D centered = base.subtract(new Vector3D(this.rightVector.getX() / 2, this.upVector.getY() / 2, 0));
        Vector3D scaled = centered.scalarMultiply(this.getImagePlaneBoxSideLength());
        return scaled.add(this.viewPoint).normalize();
    }

    /**
     * Types of cameras: FIRST_PERSON, THIRD_PERSON, PERSPECTIVE,
     * PERSPECTIVE_FOLLOW, PERSPECTIVE_RAYTRACER, UPPER_VIEW, and
     * UPPER_VIEW_FOLLOW.
     */
    public enum Type {

        FIRST_PERSON, THIRD_PERSON,
        PERSPECTIVE, PERSPECTIVE_FOLLOW, PERSPECTIVE_RAYTRACER,
        UPPER_VIEW, UPPER_VIEW_FOLLOW;
    }
}
