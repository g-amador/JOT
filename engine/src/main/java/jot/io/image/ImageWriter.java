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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.manager.SceneManager;
import static jot.manager.SceneManager.increaseBrightness;
import static jot.manager.SceneManager.toInt;

/**
 * Class that implements a image writer, i.e., that writes to a file the
 * RayTracer image generated.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class ImageWriter {

    static final Logger log = getLogger("ImageWriter");

    static {
        log.setLevel(OFF);
    }

    /**
     * Method to write a image to a given file name and path.
     *
     * @param sceneManager
     * @param destination to write the file to.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void writeImage(SceneManager sceneManager, File destination) throws IOException, FileNotFoundException {
        //write ppm header
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(destination))) {
            //write ppm header
            bw.write("P3\n" + sceneManager.image.length + " " + sceneManager.image[0].length + "\n" + 255 + "\n");

            for (int y = sceneManager.image.length - 1; y >= 0; y--) {
                for (int x = 0; x < sceneManager.image[0].length; x++) {
                    int red = toInt(increaseBrightness(sceneManager.image[y][x].getX()));
                    int green = toInt(increaseBrightness(sceneManager.image[y][x].getY()));
                    int blue = toInt(increaseBrightness(sceneManager.image[y][x].getZ()));
                    bw.write(red + " " + green + " " + blue + " ");
                }
                bw.write("\n");
            }
        }
    }

    /**
     * Default constructor.
     */
    ImageWriter() {
    }
}
