/*
 * This file is part of the JOT game engine i/o framework toolkit component.
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
package jot.io.image;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.logging.Level.ALL;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.manager.SceneManager;
import static jot.manager.SceneManager.clamp;
import jot.physics.Ray;
import jot.physics.Sampler;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements a image renderer, i.e., the RayTracer image generator.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class ImageRenderer {

    static final Logger log = getLogger("ImageRenderer");

    static {
        log.setLevel(ALL);
    }

    private static Vector3D combineRadiances(List<Vector3D> radiances) {
        Vector3D combinedradiance = ZERO;
        for (Vector3D radiance : radiances) {
            combinedradiance = combinedradiance.add(radiance);
        }
        combinedradiance = combinedradiance.scalarMultiply(1d / radiances.size());
        return combinedradiance;
    }

    private static void samplePixel(Sampler sampler, SceneManager sceneManager, int w, int h, int samples, int x, int y) {
        List<Vector3D> radiances = new ArrayList<>(samples);
        for (int sy = 0; sy < samples; sy++) {
            double dy = (double) sy / samples;
            for (int sx = 0; sx < samples; sx++) {
                double dx = (double) sx / samples;
                Ray sampleRay = sceneManager.getCamera("PerspectiveRayTracer").getSampleRay((dx + x) / w, (dy + y) / h);
                Vector3D radiance = sampler.radiance(sceneManager, sampleRay, 0);
                radiances.add(new Vector3D(clamp(radiance.getX()), clamp(radiance.getY()), clamp(radiance.getZ())));
            }
        }
        sceneManager.image[y][x] = combineRadiances(radiances);
    }

    /**
     * Generate the rayTraced image.
     *
     * @param sceneManager
     * @param w
     * @param h
     * @param samples
     * @throws Exception
     */
    public void renderImage(SceneManager sceneManager, int w, int h, int samples) throws Exception {
        ExecutorService executor = newFixedThreadPool(getRuntime().availableProcessors());
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                executor.submit(this.createRowJob(new Sampler(), sceneManager, w, h, samples, x, y));
            }
        }
        executor.shutdown();
        executor.awaitTermination(MAX_VALUE, DAYS);
    }

    private Runnable createRowJob(final Sampler sampler,
            final SceneManager sceneManager,
            final int w, final int h,
            final int samples, final int x, final int y) {
        return () -> {
            log.info(format("\rRendering (%d spp) %5.4f%%", samples * samples, 100. * (x + h * y) / (w * h)));
            samplePixel(sampler, sceneManager, w, h, samples, x, y);
        };
    }
}
