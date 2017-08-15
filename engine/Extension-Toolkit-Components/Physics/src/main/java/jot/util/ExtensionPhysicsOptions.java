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
package jot.util;

import static java.lang.String.format;
import java.util.HashMap;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * Class that sets the default physics options.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class ExtensionPhysicsOptions {

    static final Logger log = getLogger("ExtensionPhysicsOptions");

    public static final HashMap<String, Boolean> extensionPhysicsOptions = new HashMap<>();

    static {
        log.setLevel(OFF);

        //Collisions options              
        /**
         * Use collision detection on/off.
         */
        extensionPhysicsOptions.put("useBroadPhaseCollisionDetection", true);

        /**
         * Use narrow phase collision detection on/off.
         */
        extensionPhysicsOptions.put("useNarrowPhaseCollisionDetection", false);

        //Eulerian fluid solvers options   
        /**
         * Use Stable Fluids solver on/off.
         */
        extensionPhysicsOptions.put("useStableFluids", true);

        /**
         * Use Practical Fluids solver on/off.
         */
        extensionPhysicsOptions.put("usePracticalFluids", false);

        /**
         * Use vorticity confinement on/off.
         */
        extensionPhysicsOptions.put("useVorticityConfinement", false);

        //Eulerian fluid linear solvers options        
        /**
         * Use SOR in Jacobi or Gauss-Seidel solvers on/off.
         */
        extensionPhysicsOptions.put("useSOR", false);

        /**
         * Use bounds on/off.
         */
        extensionPhysicsOptions.put("useBounds", false);

        /**
         * Use Mehrstellen stencil on/off.
         */
        extensionPhysicsOptions.put("useMehrstellen", false);

        /**
         * Use full weighted multigrid stencil on/off.
         */
        extensionPhysicsOptions.put("useFullWeightedMG", false);

        /**
         * Use Use conjugate gradient linear solver debug on/off.
         */
        extensionPhysicsOptions.put("useCGdebug", false);

        /**
         * Use multigrid linear solver debug on/off.
         */
        extensionPhysicsOptions.put("useMGdebug", false);

        /**
         * Use Jacobi linear solver for diffusion on/off.
         */
        extensionPhysicsOptions.put("useJacobiDiffusion", false);

        /**
         * Use Gauss Seidel linear solver for diffusion on/off.
         */
        extensionPhysicsOptions.put("useGaussSeidelDiffusion", false);

        /**
         * Use Conjugate Gradient linear solver for diffusion on/off.
         */
        extensionPhysicsOptions.put("useConjugateGradientDiffusion", false);

        /**
         * Use Multi Grid Jacobi linear solver for diffusion on/off.
         */
        extensionPhysicsOptions.put("useMultiGridJacobiDiffusion", false);

        /**
         * Use Multi Grid Gauss Seidel linear solver for diffusion on/off.
         */
        extensionPhysicsOptions.put("useMultiGridGaussSeidelDiffusion", false);

        /**
         * Use Multi Grid Conjugate Gradient linear solver for diffusion on/off.
         */
        extensionPhysicsOptions.put("useMultiGridConjugateGradientDiffusion", false);

        /**
         * Use Jacobi linear solver for projection on/off.
         */
        extensionPhysicsOptions.put("useJacobiProjection", false);

        /**
         * Use Gauss Seidel linear solver for projection on/off.
         */
        extensionPhysicsOptions.put("useGaussSeidelProjection", false);

        /**
         * Use Conjugate Gradient linear solver for projection on/off.
         */
        extensionPhysicsOptions.put("useConjugateGradientProjection", false);

        /**
         * Use Multi Grid Jacobi linear solver for projection on/off.
         */
        extensionPhysicsOptions.put("useMultiGridJacobiProjection", false);

        /**
         * Use Multi Grid Gauss Seidel linear solver for projection on/off.
         */
        extensionPhysicsOptions.put("useMultiGridGaussSeidelProjection", false);

        /**
         * Use Multi Grid Conjugate Gradient linear solver for projection
         * on/off.
         */
        extensionPhysicsOptions.put("useMultiGridConjugateGradientProjection", false);

        //Ripple fluid solvers options   
        /**
         * Use rain effect on/off.
         */
        extensionPhysicsOptions.put("useRain", false);

        /**
         * Use ripple solver on/off.
         */
        extensionPhysicsOptions.put("useRipple", false);

        //Lagrangian simulators options
        /**
         * Do particle simulation in 3D on/off.
         */
        extensionPhysicsOptions.put("useParticleSystems3D", false);

        /**
         * Use centroid to give illusion of mass in block particle explosion
         * effect on/off.
         */
        extensionPhysicsOptions.put("useBlockCentroid", false);
    }

    /**
     * Displays in terminal all the extension physics options available and
     * their values.
     */
    public static void showExtensionPhysicsOptions() {
        log.setLevel(INFO);
        log.info("All available extension physics options values:");

        extensionPhysicsOptions.keySet().stream().forEach(
                key -> log.info(format(key + " = " + extensionPhysicsOptions.get(key))));

        log.info("");
        log.setLevel(OFF);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private ExtensionPhysicsOptions() {
    }
}
