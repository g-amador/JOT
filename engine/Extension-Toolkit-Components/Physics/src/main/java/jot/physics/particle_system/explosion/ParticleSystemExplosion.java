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

import static com.jogamp.opengl.GL.GL_FRONT_AND_BACK;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.GL2GL3.GL_FILL;
import static com.jogamp.opengl.GL2GL3.GL_LINE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.random;
import static java.lang.Math.sin;
import java.util.ArrayList;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.math.geometry.Mesh;
import jot.physics.particle_system.ParticleSystem;
import static jot.util.CoreOptions.coreOptions;
import static jot.util.ExtensionPhysicsOptions.extensionPhysicsOptions;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements a generic simplistic particle system.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class ParticleSystemExplosion implements ParticleSystem {

    static final Logger log = getLogger("ParticleSystemExplosion");

    static {
        log.setLevel(OFF);
    }

    private final ArrayList<ParticleExplosion> particles;
    private int particlesCount;
    private int listParticles;
    private GameObject gameObject;

    /**
     * Constructor.
     *
     * @param particlesCount the number of particles to create.
     * @param particlesSize0 the initial size of each particle.
     * @param particlesLifeSpan the life a particle has before going bye bye.
     * @param particlesAcc0 the initial acceleration of the particle system.
     * @param particlesPos0 the initial position of the barycentre of the
     * particle system.
     * @param particlesRot0 rotation of each particle.
     */
    public ParticleSystemExplosion(int particlesCount, float particlesSize0,
            float particlesLifeSpan, Vector3D particlesAcc0,
            Vector3D particlesPos0, Vector3D particlesRot0) {
        this.particles = new ArrayList<>();
        this.particlesCount = particlesCount;

        //Create all the particles an setup their initial values.
        for (int i = 0; i < particlesCount; i++) {
            float z = 0;
            if (extensionPhysicsOptions.get("useParticleSystems3D")) {
                z = (float) (random() * 2 - 1);
            }
            float angle = (float) (random() * 2 * PI);
            Vector3D direction = new Vector3D(cos(angle), sin(angle), z);

            if (extensionPhysicsOptions.get("useParticleSystems3D")) {
                z = (float) particlesAcc0.getZ();
            }
            Vector3D acc0 = new Vector3D(
                    particlesAcc0.getX() * direction.getX(),
                    particlesAcc0.getY() * direction.getY(),
                    z * direction.getZ());

            if (extensionPhysicsOptions.get("useParticleSystems3D")) {
                z = (float) (random() * 1 - 0.5);
            }
            Vector3D vel0 = new Vector3D(
                    (random() * 1 - 0.5) * direction.getX(),
                    (random() * 1 - 0.5) * direction.getY(),
                    z * direction.getZ());

            this.particles.add(new ParticleExplosion(particlesSize0, particlesLifeSpan, acc0, vel0, particlesPos0, particlesRot0));
        }
    }

    /**
     * Constructor.
     *
     * @param gameObject the gameObject associated to the particle system.
     * @param particlesLifeSpan the life a particle has before going bye bye.
     * @param particlesAcc0 the initial acceleration of the particle system.
     */
    public ParticleSystemExplosion(GameObject gameObject,
            float particlesLifeSpan, Vector3D particlesAcc0) {
        this.particles = new ArrayList<>();

        this.gameObject = gameObject;

        ArrayList<Mesh> meshes = gameObject.getMeshes();

        System.out.println(meshes.size());
        System.out.println(meshes.get(0).getVertexIndices().capacity());
        System.out.println(meshes.get(0).getVerticesSize());
        System.out.println(meshes.get(0).getVertices().capacity());

        //Create all the particles an setup their initial values.
        meshes.stream().forEach(mesh -> {
            for (int index = 0; index < mesh.getVertexIndices().capacity(); index += mesh.getIndexStride()) {
                ArrayList<Vector3D> normals = new ArrayList<>();
                ArrayList<Vector3D> texCoords = new ArrayList<>();
                ArrayList<Vector3D> vertices = new ArrayList<>();
                for (int i = 0; i < mesh.getIndexStride(); i++) {
                    if (mesh.getNormals() != null) {
                        normals.add(new Vector3D(
                                mesh.getNormals().get(mesh.getNormalsIndices().get(index + i) * mesh.getNormalsSize()),
                                mesh.getNormals().get(mesh.getNormalsIndices().get(index + i) * mesh.getNormalsSize() + 1),
                                mesh.getNormals().get(mesh.getNormalsIndices().get(index + i) * mesh.getNormalsSize() + 2)));
                    }

                    if (mesh.getTextureCoords() != null) {
                        texCoords.add(new Vector3D(
                                mesh.getTextureCoords().get(mesh.getTexCoordIndices().get(index + i) * mesh.getTextureCoordSize()),
                                mesh.getTextureCoords().get(mesh.getTexCoordIndices().get(index + i) * mesh.getTextureCoordSize() + 1),
                                mesh.getTextureCoordSize() == 2
                                        ? NEGATIVE_INFINITY
                                        : mesh.getTextureCoords().get(mesh.getTexCoordIndices().get(index + i) * mesh.getTextureCoordSize() + 2)));
                    }

                    if (mesh.getVerticesSize() == 2) {
                        vertices.add(new Vector3D(
                                mesh.getVertices().get(mesh.getVertexIndices().get(index + i) * mesh.getVerticesSize()),
                                mesh.getVertices().get(mesh.getVertexIndices().get(index + i) * mesh.getVerticesSize() + 1),
                                mesh.getVerticesSize() == 2
                                        ? NEGATIVE_INFINITY
                                        : mesh.getVertices().get(mesh.getVertexIndices().get(index + i) * mesh.getVerticesSize() + 2)));
                    }
                }

                float x = 0, y = 0, z = 0;
                for (Vector3D normal : normals) {
                    x += normal.getX();
                    y += normal.getY();
                    if (extensionPhysicsOptions.get("useParticleSystems3D")) {
                        z += normal.getZ();
                    }
                }
                Vector3D direction = new Vector3D(-x, y, -z);
                log.info(direction.toString());

                if (extensionPhysicsOptions.get("useParticleSystems3D")) {
                    z = (float) particlesAcc0.getZ();
                }
                Vector3D acc0 = new Vector3D(
                        particlesAcc0.getX() * direction.getX(),
                        particlesAcc0.getY() * direction.getY(),
                        z * direction.getZ());

                if (extensionPhysicsOptions.get("useParticleSystems3D")) {
                    z = (float) (random() + 0.1);
                }
                Vector3D vel0 = new Vector3D(
                        (random() + 0.1) * direction.getX(),
                        (random() + 0.1) * direction.getY(),
                        z * direction.getZ());

                this.particles.add(new ParticleExplosion(
                        //vertices, normals, texCoords, gameObject.childScaling(),
                        vertices, normals, texCoords, gameObject.getScaling(),
                        particlesLifeSpan, acc0, vel0,
                        gameObject.getPosition(), gameObject.getRotation()));
            }
        });

        this.particlesCount = this.particles.size();
    }

    @Override
    public boolean isDead() {
        return this.particlesCount <= 0;
    }

    @Override
    public void render(GL2 gl) {
        gl.glPushMatrix();
        {
            gl.glDeleteLists(this.listParticles, 1);
            this.listParticles = gl.glGenLists(1);
            gl.glNewList(this.listParticles, GL_COMPILE);
            {
                gl.glPolygonMode(GL_FRONT_AND_BACK,
                        coreOptions.get("showWireframe") ? GL_LINE : GL_FILL);

                if (coreOptions.get("showTextures")
                        && this.gameObject != null
                        && this.gameObject.getMeshes().get(0).getMaterial(0).getTexture() != null) {
                    this.gameObject.getMeshes().get(0).getMaterial(0).getTexture().enable(gl);
                    this.gameObject.getMeshes().get(0).getMaterial(0).getTexture().bind(gl);
                }

                //Render all existing particles.
                this.particles.stream().forEach(p -> p.render(gl));

                if (coreOptions.get("showTextures")
                        && this.gameObject != null
                        && this.gameObject.getMeshes().get(0).getMaterial(0).getTexture() != null) {
                    this.gameObject.getMeshes().get(0).getMaterial(0).getTexture().disable(gl);
                }
            }
            gl.glEndList();

            gl.glDisable(GL_LIGHTING);
            gl.glCallList(this.listParticles);
            gl.glEnable(GL_LIGHTING);
        }
        gl.glPopMatrix();
    }

    @Override
    public void update(float dt) {
        //ArrayList to store all particles that died, and thus must be removed
        //from the particles ArrayList.
        ArrayList<ParticleExplosion> particles2Remove = new ArrayList<>();

        //For all existing particles:
        this.particles.stream().forEach(p -> {
            //Update particle.
            p.update(dt);

            //If particle died add to particles2Remove.
            if (p.isDead()) {
                particles2Remove.add(p);
            }
        });

        //For each particle to remove:
        particles2Remove.stream().forEach(p -> {
            //Remove it from particles ArrayList.
            this.particles.remove(p);

            //Decreasse particlesCount.
            this.particlesCount--;
        });
    }
}
