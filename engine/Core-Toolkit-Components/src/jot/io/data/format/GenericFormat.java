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
package jot.io.data.format;

import static com.jogamp.opengl.GLProfile.getDefault;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import static com.jogamp.opengl.util.texture.TextureIO.DDS;
import static com.jogamp.opengl.util.texture.TextureIO.GIF;
import static com.jogamp.opengl.util.texture.TextureIO.JPG;
import static com.jogamp.opengl.util.texture.TextureIO.PAM;
import static com.jogamp.opengl.util.texture.TextureIO.PNG;
import static com.jogamp.opengl.util.texture.TextureIO.PPM;
import static com.jogamp.opengl.util.texture.TextureIO.TGA;
import static com.jogamp.opengl.util.texture.TextureIO.TIFF;
import static com.jogamp.opengl.util.texture.TextureIO.newTexture;
import static com.jogamp.opengl.util.texture.TextureIO.newTextureData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Interface that each generic format must implement. Each provided format, is
 * associated to the appropriate format loader/parser. Also it specifies the
 * methods to implement by a generic format data file loader.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public interface GenericFormat {

//    private static final Logger log = getLogger("GenericFormat");
//
//    static {
//        log.setLevel(OFF);
//    }
    /**
     * Load a texture given a valid path to it.
     *
     * @param path to the texture to load.
     * @return the loaded texture otherwise null.
     */
    static Texture loadTexture(String path) {
        Texture texture = null;
        TextureData textureData = loadTextureData(path);
        if (textureData != null) {
            texture = newTexture(textureData);
        }

        return texture;
    }

    /**
     * Load a textureData given a valid path to it.
     *
     * @param path to the textureData to load.
     * @return the loaded textureData otherwise null.
     */
    static TextureData loadTextureData(String path) {
        InputStream image = null;
        TextureData textureData = null;
        try {
            image = GenericFormat.class.getResourceAsStream(
                    path.replace("assets", "")); //Using IDE
            if (image == null) { //Using dist Jar
                image = new FileInputStream(path);
            }

            if (path.toLowerCase().contains(".dds")) {
                textureData = newTextureData(getDefault(), image, true, DDS);
            }
            if (path.toLowerCase().contains(".gif")) {
                textureData = newTextureData(getDefault(), image, true, GIF);
            }
            if (path.toLowerCase().contains(".jpg")
                    || path.toLowerCase().contains(".jpeg")) {
                textureData = newTextureData(getDefault(), image, true, JPG);
            }
            if (path.toLowerCase().contains(".pam")) {
                textureData = newTextureData(getDefault(), image, true, PAM);

            }
            if (path.toLowerCase().contains(".png")) {
                textureData = newTextureData(getDefault(), image, true, PNG);
            }
            if (path.toLowerCase().contains(".ppm")) {
                textureData = newTextureData(getDefault(), image, true, PPM);

            }
            if (path.toLowerCase().contains(".tga")) {
                textureData = newTextureData(getDefault(), image, true, TGA);
            }
            if (path.toLowerCase().contains(".tiff")) {
                textureData = newTextureData(getDefault(), image, true, TIFF);
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex.getMessage());
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            try {
                if (image != null) {
                    image.close();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }

        return textureData;
    }

    /**
     * Load all textures in a given valid path to the folder that contains them.
     *
     * @param path to the textures to load.
     * @return a list with the loaded textures.
     */
    static ArrayList<Texture> loadTextures(String path) {
        ArrayList<Texture> textures = new ArrayList<>();
        File folder = new File(path);
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()
                    && (fileEntry.getPath().toLowerCase().contains(".dds")
                    || fileEntry.getPath().toLowerCase().contains(".gif")
                    || fileEntry.getPath().toLowerCase().contains(".jpg")
                    || fileEntry.getPath().toLowerCase().contains(".jpeg")
                    || fileEntry.getPath().toLowerCase().contains(".pam")
                    || fileEntry.getPath().toLowerCase().contains(".png")
                    || fileEntry.getPath().toLowerCase().contains(".ppm")
                    || fileEntry.getPath().toLowerCase().contains(".tga")
                    || fileEntry.getPath().toLowerCase().contains(".tiff"))) {
                textures.add(loadTexture(path + new File(fileEntry.getPath()).getName()));
            }
        }
        return textures;
    }

    /**
     * Parse a generic format file.
     *
     * @param fileName the path/name of the file to parse.
     * @throws IOException if the file does not exit.
     * @throws LoaderException if the file can't be read.
     */
    void parseFile(String fileName) throws IOException, LoaderException;
}
