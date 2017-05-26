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
package jot.math.geometry;

import com.jogamp.opengl.util.texture.Texture;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import jot.math.geometry.shape.Triangle;
import jot.physics.Material;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Abstract class that a mesh (3D geometry representation) class must extend.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class Mesh implements Node {

    /**
     * Get the largest x, y, z values of all vertexes from this mesh.
     *
     * @return the maximum x, y, z values of all vertexes from this mesh.
     */
    public abstract Vector3D getMaxVertex();

    /**
     * Get the lowest x, y, z values of all vertexes from this mesh.
     *
     * @return the minimum x, y, z values of all vertexes from this mesh.
     */
    public abstract Vector3D getMinVertex();

    /**
     * Get all textureCoords values as a string, i.e., convert the texture data
     * buffer into a string.
     *
     * @return a string with all the textureCoords values.
     */
    public abstract String getTexCoords2String();

    /**
     * Get the triangles count for this mesh.
     *
     * @return the triangles count for this mesh.
     */
    public abstract int getTrianglesCount();

    /**
     * Set use DrawElements or use DrawArrays.
     *
     * @param useDrawElements FALSE to use DrawArrays, TRUE to use DrawElements,
     * default use DrawElements.
     */
    public abstract void setUseDrawElements(boolean useDrawElements);

    /**
     * Set the textures list of the materials list of this mesh.
     *
     * @param textures the textures list of the materials list to set to this
     * mesh.
     */
    public abstract void setTextures(ArrayList<Texture> textures);

    /**
     * Gets this mesh vertices values.
     *
     * @return this mesh vertices.
     */
    public abstract FloatBuffer getVertices();

    /**
     * Converts a float array, with vertices values, into a float buffer.
     *
     * @param vertices float array, with vertices values.
     */
    public abstract void setVertices(float[] vertices);

    /**
     * Gets this mesh normals values.
     *
     * @return this mesh normals.
     */
    public abstract FloatBuffer getNormals();

    /**
     * Converts a float array, with normals values, into a float buffer.
     *
     * @param normals float array, with normals values.
     */
    public abstract void setNormals(float[] normals);

    /**
     * Gets this mesh texture coordinates values.
     *
     * @return this mesh texture coordinates.
     */
    public abstract FloatBuffer getTextureCoords();

    /**
     * Converts a float array, with textureCoords values, into a float buffer.
     *
     * @param textureCoords array, with textureCoords values.
     */
    public abstract void setTextureCoords(float[] textureCoords);

    /**
     * Gets this mesh vertex indices values.
     *
     * @return this mesh vertex indices.
     */
    public abstract IntBuffer getVertexIndices();

    /**
     * Converts a int array, with vertexIndices values, into a int buffer.
     *
     * @param vertexIndices int array, with verticesIndices values.
     */
    public abstract void setVertexIndices(int[] vertexIndices);

    /**
     * Gets this mesh normals indices values.
     *
     * @return this mesh normals indices.
     */
    public abstract IntBuffer getNormalsIndices();

    /**
     * Converts a int array, with normalsIndices values, into a int buffer.
     *
     * @param normalsIndices int array, with normalsIndices values.
     */
    public abstract void setNormalsIndices(int[] normalsIndices);

    /**
     * Gets this mesh texture coordinates indices values.
     *
     * @return this mesh texture coordinates indices.
     */
    public abstract IntBuffer getTexCoordIndices();

    /**
     * Converts a int array, with textureCoordsIndices values, into a int
     * buffer.
     *
     * @param texCoordIndices int array, with normalsIndices values.
     */
    public abstract void setTexCoordIndices(int[] texCoordIndices);

    /**
     * Get the size of each textureCcoord, i.e., the number of terms required
     * for each textureCoord, e.g., if using S,T values it would be 2.
     *
     * @return the size of each textureCcoord.
     */
    public abstract int getTextureCoordSize();

    /**
     * Set the size of each textureCcoord, i.e., the number of terms required
     * for each textureCoord, e.g., if using S,T values it would be 2.
     *
     * @param textureCoordSize the size of each textureCcoord.
     */
    public abstract void setTextureCoordSize(int textureCoordSize);

    /**
     * Get the size of each normal, i.e., the number of terms required for each
     * normal.
     *
     * @return the size of each normal.
     */
    public abstract int getNormalsSize();

    /**
     * Set the size of each normal, i.e., the number of terms required for each
     * normal.
     *
     * @param normalsSize the size of each normal.
     */
    public abstract void setNormalsSize(int normalsSize);

    /**
     * Get the size of each vertex, i.e., the number of terms required for each
     * vertex.
     *
     * @return the size of each vertex.
     */
    public abstract int getVerticesSize();

    /**
     * Set the size of each vertex, i.e., the number of terms required for each
     * vertex.
     *
     * @param verticesSize the size of each normal.
     */
    public abstract void setVerticesSize(int verticesSize);

    /**
     * Get the stride between indexes, e.g., a vertex, normals, and
     * textureCoords are 3.
     *
     * @return the stride between indexes.
     */
    public abstract int getIndexStride();

    /**
     * Set the stride between indexes, e.g., a vertex, normals, and
     * textureCoords are 3. This is required in the render process.
     *
     * @param indexStride the integer value of the primitive to use, e.g.,
     * QUADS.
     */
    public abstract void setIndexStride(int indexStride);

    /**
     * Get the primitive type to use when rendering if the setPrimitiveType
     * method is never called default is TRIANGLES.
     *
     * @return the primitive type to use when rendering.
     */
    public abstract int getPrimitiveType();

    /**
     * Set the primitive type to use when rendering if this method is never
     * called default is TRIANGLES.
     *
     * @param primitiveType the primitiveType value of the primitive to use,
     * e.g., TRIANGLES.
     */
    public abstract void setPrimitiveType(PrimitiveType primitiveType);

    /**
     *
     * Get the material list of this mesh.
     *
     * @return this mesh material list.
     */
    public abstract ArrayList<Material> getMaterials();

    /**
     * Set the material list of this mesh.
     *
     * @param materials the material list to set to this mesh.
     */
    public abstract void setMaterials(ArrayList<Material> materials);

    /**
     *
     * Get a specific material with a given index from the materials list of
     * this mesh.
     *
     * @param index of a specific material from the materials list of this mesh.
     * @return the material at a given index from the materials list of this
     * mesh.
     */
    public abstract Material getMaterial(int index);

    /**
     * Get all the triangles of this mesh.
     *
     * @return all the triangles of this mesh.
     */
    public abstract ArrayList<Triangle> getTriangles();

    /**
     * Set all the triangles of this mesh.
     */
    public abstract void setTriangles();

    /**
     * Set the last material from the materials list of this mesh.
     *
     * @param material the material to add materials list to this mesh.
     */
    public abstract void setMaterial(Material material);

    public enum PrimitiveType {

        POINTS, //Points primitive
        LINES, LINE_STRIP, LINE_LOOP, //Lines primitives
        TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN //Triangles primitives
    }
}
