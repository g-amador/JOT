/*
 * This file is part of the JOT game engine physics extension toolkit component.
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
package jot.physics.particle_system.explosion;

import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import com.jogamp.opengl.GL2;
import static java.lang.Double.NEGATIVE_INFINITY;
import java.util.ArrayList;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.physics.particle_system.Particle;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements a generic simplistic particle.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class ParticleExplosion implements Particle {

    static final Logger log = getLogger("ParticleExplosion");

    static {
        log.setLevel(OFF);
    }

    private float age; //Current age of the particle  
    private float size; //Mass of the particle
    private Vector3D scaling; //Scale of the particle.
    private Vector3D color;
    private Vector3D pos; //Position of the particle
    private Vector3D vel; //Velocity of the particle
    private final Vector3D acc; //Acceleration of the particle 
    private final Vector3D rot; //Rotation of the particle.
    private final float lifeSpan; //Age after which the particle dies
    private ArrayList<Vector3D> vertices;
    private ArrayList<Vector3D> normals;
    private ArrayList<Vector3D> texCoords;

    /**
     * Constructor.
     *
     * @param size particle size or mass.
     * @param lifeSpan particle life span.
     * @param acc0 particle start acceleration.
     * @param vel0 particle start velocity.
     * @param pos0 particle start position.
     * @param rot0 particle start rotation.
     */
    public ParticleExplosion(float size, float lifeSpan,
            Vector3D acc0, Vector3D vel0, Vector3D pos0, Vector3D rot0) {
        this.size = size;
        this.lifeSpan = lifeSpan;

        this.age = 0;

        //a = a0;
        this.acc = new Vector3D(acc0.toArray());

        //v = v0 + a;
        this.vel = vel0.add(this.acc);

        //p = p0 + v;
        this.pos = pos0.add(this.vel);

        this.rot = rot0;

        log.info("Red");
        this.color = new Vector3D(1.0, 0.0, 0.0);
    }

    /**
     * Constructor.
     *
     * @param vertices the vertices associated to the particle.
     * @param normals the normals associated to vertices of the particle.
     * @param texCoords the texture coordinates associated to vertices of the
     * particle.
     * @param scaling particle scale.
     * @param lifeSpan particle life span.
     * @param acc0 particle start acceleration.
     * @param vel0 particle start velocity.
     * @param pos0 particle start position.
     * @param rot0 particle start rotation.
     */
    public ParticleExplosion(
            ArrayList<Vector3D> vertices, ArrayList<Vector3D> normals,
            ArrayList<Vector3D> texCoords, Vector3D scaling, float lifeSpan,
            Vector3D acc0, Vector3D vel0, Vector3D pos0, Vector3D rot0) {
        this.scaling = scaling;
        this.lifeSpan = lifeSpan;

        this.age = 0;

        //a = a0;
        this.acc = new Vector3D(acc0.toArray());

        //v = v0 + a;
        this.vel = vel0.add(this.acc);

//        //p = p0 + v;  
//        for (int i = 0; i < vertices.size(); i++) {
//            vertices.set(i, vertices.get(i).add(vel));
//        }
        //p = p0;
        this.pos = pos0;

        //r = r0;
        this.rot = rot0;

        this.vertices = vertices;
        this.normals = normals;
        this.texCoords = texCoords;

        this.color = new Vector3D(1.0, 1.0, 1.0);
    }

    @Override
    public boolean isDead() {
        return this.age >= this.lifeSpan;
    }

    @Override
    public void render(GL2 gl) {
        if (this.vertices == null) {
            gl.glPointSize(this.size);
            gl.glColor3d(this.color.getX(), this.color.getY(), this.color.getZ());
            gl.glPushMatrix();
            {
                gl.glRotated(this.rot.getX(), 1, 0, 0);
                gl.glRotated(this.rot.getY(), 0, 1, 0);
                gl.glRotated(this.rot.getZ(), 0, 0, 1);
                gl.glBegin(GL_POINTS);
                gl.glVertex3d(this.pos.getX(), this.pos.getY(), this.pos.getZ());
                gl.glEnd();
            }
            gl.glPopMatrix();
        } else {
            gl.glColor3d(this.color.getX(), this.color.getY(), this.color.getZ());
            gl.glPushMatrix();
            {
                gl.glTranslated(this.pos.getX(), this.pos.getY(), this.pos.getZ());
                gl.glRotated(this.rot.getX(), 1, 0, 0);
                gl.glRotated(this.rot.getY(), 0, 1, 0);
                gl.glRotated(this.rot.getZ(), 0, 0, 1);
                gl.glScaled(this.scaling.getX(), this.scaling.getY(), this.scaling.getZ());
//
//                if (extensionPhysicsOptions.get("useBlockCentroid")) {
//                    gl.glDisable(GL_TEXTURE);
//                    gl.glBegin(GL_TRIANGLE_FAN);
//                    {
//                        float x = 0, y = 0, z = 0;
//                        for (Vector3D v : vertices) {
//                            x += v.getX();
//                            y += v.getY();
//                            z += v.getZ();
//                        }
//                        Vector3D centroid = new Vector3D(
//                                x / vertices.size() - signum(x),
//                                y / vertices.size() - signum(y),
//                                z / vertices.size() - signum(z));
//                        gl.glVertex3d(centroid.getX(), centroid.getY(), centroid.getZ());
//                        vertices.stream().forEach(
//                                v -> gl.glVertex3d(v.getX(), v.getY(), v.getZ()));
//
//                        Vector3D v = vertices.get(0);
//                        gl.glVertex3d(v.getX(), v.getY(), v.getZ());
//                    }
//                    gl.glEnd();
//                    gl.glEnable(GL_TEXTURE);
//                }
//
                gl.glBegin(GL_TRIANGLES);
                {
                    for (int i = 0; i < this.vertices.size(); i++) {
                        if (!this.normals.isEmpty()) {
                            Vector3D n = this.normals.get(i);
                            gl.glNormal3d(n.getX(), n.getY(), n.getZ());
                        }
                        if (!this.texCoords.isEmpty()) {
                            Vector3D t = this.texCoords.get(i);
                            if (t.getZ() == NEGATIVE_INFINITY) {
                                gl.glTexCoord2d(t.getX(), t.getY());
                            } else {
                                gl.glTexCoord3d(t.getX(), t.getY(), t.getZ());
                            }
                        }
                        Vector3D v = this.vertices.get(i);
                        if (v.getZ() == NEGATIVE_INFINITY) {
                            gl.glVertex2d(v.getX(), v.getY());
                        } else {
                            gl.glVertex3d(v.getX(), v.getY(), v.getZ());
                        }
                    }
                }
                gl.glEnd();
            }
            gl.glPopMatrix();
        }
    }

    @Override
    public void update(float dt) {
        this.vel = this.vel.add(this.acc.scalarMultiply(dt));

        this.age += dt;

        if (this.vertices == null) {
            this.pos = this.pos.add(this.vel.scalarMultiply(dt));

            this.size -= dt / this.lifeSpan;

            //Fading color
            if (this.color.getY() <= 1 && this.color.getY() != this.color.getX()) {
                log.info("Fading yellow");
                this.color = new Vector3D(1.0, this.color.getY() + 1 / this.lifeSpan, this.color.getZ());
            } else if (this.color.getX() == 1 && this.color.getZ() == 0) {
                log.info("Gray");
                this.color = new Vector3D(0.2, 0.2, 0.2);
            } else if (this.color.getX() < 0.8 && this.color.getY() < 0.8 && this.color.getZ() < 0.8) {
                log.info("Fading gray");
                this.color = new Vector3D(
                        this.color.getX() + 1 / this.lifeSpan,
                        this.color.getY() + 1 / this.lifeSpan,
                        this.color.getZ() + 1 / this.lifeSpan);
            }
        } else {
            for (int i = 0; i < this.vertices.size(); i++) {
                this.vertices.set(i, this.vertices.get(i).add(this.vel.scalarMultiply(dt)));
            }
        }

        //TODO: more realistic fire
    }
}
