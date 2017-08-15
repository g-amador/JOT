/*
 * This file is part of the JOT game engine managers framework toolkit
 * component. 
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
package jot.manager;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import static com.jogamp.opengl.util.texture.TextureIO.newTexture;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.io.data.format.ColladaScene;
import jot.io.data.format.GenericFormat;
import jot.io.data.format.HOG2Map;
import static jot.io.data.format.UnzipUtility.unzipUtility;
import jot.io.data.format.WavefrontOBJ;
import jot.math.geometry.TransformGroup;
import jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType;

/**
 * Class that implements a asset manager.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class AssetManager {

    static final Logger log = getLogger("AssetManager");

    static {
        log.setLevel(OFF);
    }

    private final ConcurrentHashMap<String, Texture> textures = new ConcurrentHashMap<>();
    //private ConcurrentHashMap<String, TextureData> texturesData = new ConcurrentHashMap<>();

    /**
     * Constructor.
     */
    public AssetManager() {
    }

    /**
     * Get textures hashMap.
     *
     * @return textures hashMap.
     */
    public boolean isTexturesNotNull() {
        return this.textures != null;
    }

    /**
     * Load the data, i.e., geometry and materials of a generic format file, and
     * encapsulate it into a transformGroup.
     *
     * @param filePath the path to the file to load.
     * @param fileName the name of the file content to load.
     * @param scale the scale of this format.
     * @param boundingVolumeType the type of BoundingVolume that must engulf
     * this format.
     * @return the loaded data of a generic format file encapsulated into a
     * transformGroup.
     */
    public TransformGroup loadFormat(
            String filePath,
            String fileName,
            float scale,
            BoundingVolumeType boundingVolumeType) {
        filePath = unzipUtility(filePath, fileName);

        if (fileName.toLowerCase().contains(".dae")) {
            return ColladaScene.loadFormat(filePath, fileName, scale, boundingVolumeType);
        } else if (fileName.toLowerCase().contains(".map")) {
            return HOG2Map.loadFormat(filePath, fileName, scale, boundingVolumeType);
        } else if (fileName.toLowerCase().contains(".obj")) {
            return WavefrontOBJ.loadFormat(filePath, fileName, scale, boundingVolumeType);
        } else {
            return null;
        }
    }

    /**
     * Load a texture given a valid path to it.
     *
     * @param path to the texture to load.
     * @return the loaded texture otherwise null.
     */
    private Texture loadTexture(String path) {
        return GenericFormat.loadTexture(path);
    }
//
//    /**
//     * Load a textureData given a valid path to it.
//     *
//     * @param path to the textureData to load.
//     * @return the loaded textureData otherwise null.
//     */
//    private TextureData loadTextureData(String path) {
//        return AbstractGenericFormat.loadTextureData(path);
//    }
//
//    /**
//     * Load all textures in a given valid path to the folder that contains them.
//     *
//     * @param path to the textures to load.
//     * @return a list with the loaded textures.
//     */
//    private ArrayList<Texture> loadTextures(String path) {
//        return AbstractGenericFormat.loadTextures(path);
//    }
//

    /**
     * Get all texture values.
     *
     * @return all texture values.
     */
    public Collection<Texture> getTexturesValues() {
        return this.textures.values();
    }

    /**
     * Get a texture given its identifier key.
     *
     * @param key the identifier of the texture.
     * @return the texture with provided identifier key.
     */
    public Texture getTexture(String key) {
        return this.textures.get(key);
    }

    /**
     * Set a texture given its path and identifier key.
     *
     * @param key the identifier of the texture.
     * @param path to the texture file.
     */
    public void setTexture(String key, String path) {
        this.textures.put(key, this.loadTexture(path));
    }

    /**
     * Set a texture given its path and identifier key.
     *
     * @param key the identifier of the texture.
     * @param textureData of the texture to create.
     */
    public void setTexture(String key, TextureData textureData) {
        this.textures.put(key, newTexture(textureData));
    }

    public void setTextures(ConcurrentHashMap<String, String> texturesPaths) {
        if (null != texturesPaths) {
            Set<?> st = texturesPaths.keySet();
            if (!st.isEmpty()) {
                Iterator<?> itr = st.iterator();
                while (itr.hasNext()) {
                    String key = itr.next().toString();
                    String path = texturesPaths.get(key);
                    this.setTexture(key, path);
                }
            }
        }
//        } else {
//            showTextures = false;
//        }
    }

//    /**
//     * Get a texture data given its identifier key.
//     *
//     * @param key key the identifier of the texture.
//     * @return the texture data with provided identifier key.
//     */
//    private TextureData getTextureData(String key) {
//        return texturesData.get(key);
//    }
//    /**
//     * Set a texture data given its identifier key.
//     *
//     * @param key the identifier of the texture.
//     * @param path to the texture file.
//     */
//    private void setTextureData(String key, String path) {
//        texturesData.put(key, loadTextureData(path));
//    }
    public boolean texturesContains(String key) {
        return this.textures.containsKey(key);
    }

    public void dispose() {
        this.textures.clear();
        //texturesData.clear();
    }
}
