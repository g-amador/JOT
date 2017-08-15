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
package jot.physics.particle_system.rain;

import com.jogamp.opengl.GL2;
import static java.lang.Math.random;
import java.util.ArrayList;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.physics.particle_system.ParticleSystem;

/**
 * Class that implement a rain effect.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Rain implements ParticleSystem {

    static final Logger log = getLogger("Rain");

    static {
        log.setLevel(OFF);
    }

    /**
     * Intensity of the rain drops;
     */
    public int intensity;

    /**
     * Thickness of the rain drops;
     */
    public int rainThikness;

    /**
     * The amount of ripples in the system.
     */
    public int ripplesCount;

    private final ArrayList<Ripple> ripples;

    private final int n;
    private final int size;

    /**
     * Constructor.
     *
     * @param n cell count in x an y.
     * @param size absolute size value of one of the sides of the square that
     * defines the floor, i.e., floor as size*size area.
     */
    public Rain(int n, int size) {
        this.n = n;
        this.size = size;

        this.intensity = 2;
        this.rainThikness = 8;
        this.ripplesCount = 0;
        this.ripples = new ArrayList<>();

        this.generateRipples();
    }

    @Override
    public boolean isDead() {
        return this.ripplesCount <= 0;
    }

    @Override
    public void render(GL2 gl) {
        //For all existing particles:
        this.ripples.stream().forEach(ripple -> ripple.render(gl));
    }

    @Override
    public void update(float dt) {
        //ArrayList to store all ripples that died, and thus must be removed
        //from the ripples ArrayList.
        ArrayList<Ripple> ripples2Remove = new ArrayList<>();

        //For all existing ripples:           
        this.ripples.stream().forEach(ripple -> {
            //Update Ripple.
            ripple.update(dt);

            //If Ripple died add to ripples2Remove.
            if (ripple.isDead()) {
                ripples2Remove.add(ripple);
            }
        });

        //For each Ripple to remove:
        ripples2Remove.stream().forEach(ripple -> {
            //Remove it from ripples ArrayList.
            this.ripples.remove(ripple);

            //Decreasse ripplesCount.
            this.ripplesCount--;
        });

        //Create more ripples
        this.generateRipples();
    }

    private void generateRipples() {
        for (int i = 0; i < this.intensity; i++) {
            this.ripples.add(new Ripple(this.n, this.size, this.rainThikness));
            this.ripples.get(this.ripples.size() - 1).disturb((int) (1 + random() * (this.n - 2)), (int) (1 + random() * (this.n - 2)));
            this.ripplesCount++;
        }
    }
}
