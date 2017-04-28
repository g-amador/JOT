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

import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRONT_AND_BACK;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_LINE_LOOP;
import static com.jogamp.opengl.GL.GL_LINE_STRIP;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_TRIANGLE_FAN;
import static com.jogamp.opengl.GL.GL_TRIANGLE_STRIP;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.GL2ES2.GL_INT;
import static com.jogamp.opengl.GL2GL3.GL_FILL;
import static com.jogamp.opengl.GL2GL3.GL_LINE;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_NORMAL_ARRAY;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_TEXTURE_COORD_ARRAY;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;
import static com.jogamp.opengl.util.GLBuffers.newDirectGLBuffer;
import com.jogamp.opengl.util.texture.Texture;
import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.math.geometry.Mesh.PrimitiveType.TRIANGLES;
import jot.math.geometry.shape.Triangle;
import jot.physics.Material;
import static jot.util.CoreOptions.coreOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that represents a 3D geometry, i.e., it contains vertices, normals, and
 * textureCoords values, and the ordered indexes information required to render
 * them.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class PolygonMesh extends Mesh {

    protected static final Logger log = getLogger("Mesh");

    static {
        log.setLevel(OFF);
    }

    protected String Id;

    //TODO: remove unIndexeds when no longer needed
    protected FloatBuffer vertices;
    protected FloatBuffer normals;
    protected FloatBuffer textureCoords;
    //protected FloatBuffer unIndexedVertices;
    //protected FloatBuffer unIndexedNormals;
    //protected FloatBuffer unIndexedTextureCoords;
    protected IntBuffer vertexIndices;
    protected IntBuffer normalsIndices;
    protected IntBuffer texCoordIndices;
    protected int indexStride;
    protected int verticesSize;
    protected int normalsSize;
    protected int textureCoordSize;
    protected ArrayList<Material> materials;
    protected Vector3D min, max;
    protected int primitiveType;
    protected ArrayList<Triangle> triangles;
    protected boolean useDrawElements = true;

    /**
     * On/Off generate new display list, i.e., if already created and not
     * altered it's no longer required to create and data transfer it to the
     * GPU.
     */
    protected int listMesh;

    /**
     * The display list pointer used to render the geometry.
     */
    protected boolean genList = true;

    /**
     * Constructor where the default primitive to render (GL_TRIANGLES) is
     * specified. Also, where the number of dimensions per vertex, normal, and
     * textureCoord.
     */
    public PolygonMesh() {
        this.Id = "Mesh";
        this.indexStride = 3;
        this.verticesSize = 3;
        this.normalsSize = 3;
        this.textureCoordSize = 3;
        this.primitiveType = GL_TRIANGLES;
        this.materials = new ArrayList<>();
        this.materials.add(new Material());
        this.materials.get(0).setRenderable(true);
        this.triangles = new ArrayList<>();
    }

    /**
     * Constructor.
     *
     * @param Id the identifier for this Polygon Mesh.
     */
    public PolygonMesh(String Id) {
        this.Id = Id;
        this.indexStride = 3;
        this.verticesSize = 3;
        this.normalsSize = 3;
        this.textureCoordSize = 3;
        this.primitiveType = GL_TRIANGLES;
        this.materials = new ArrayList<>();
        this.materials.add(new Material(Id));
        this.materials.get(0).setRenderable(true);
        this.triangles = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getMaxVertex() {
        return this.max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getMinVertex() {
        return this.min;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTexCoords2String() {
        float[] coords = new float[0];
        if (this.textureCoords != null) {
            this.textureCoords.rewind();
            coords = new float[this.textureCoords.capacity()];
            this.textureCoords.get(coords);
        }
        StringBuilder buf = new StringBuilder("TexCoords: ");
        for (float coord : coords) {
            buf.append(coord).append(",");
        }
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<Triangle> getTriangles() {
        return this.triangles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTriangles() {
        //Set up all triangles only when first asked for this mesh (if ever)      
        for (int i = 0; i < this.vertexIndices.capacity(); i += this.indexStride) {
            ArrayList<Vector3D> triangleVertices = new ArrayList<>();
            for (int j = 0; j < this.indexStride; j++) {
//                    if (verticesSize == 2) {
//                        triangles.add(new Vector3D(
//                            //TODO: remove unIndexeds when no longer needed
//                            //unIndexedVertices.get(vertexIndices.get(i + j) * verticesSize),
//                            //unIndexedVertices.get(vertexIndices.get(i + j) * verticesSize + 1), 0});
//                            vertices.get(vertexIndices.get(i + j) * verticesSize),
//                            vertices.get(vertexIndices.get(i + j) * verticesSize + 1), 0));
//                    } else {
                triangleVertices.add(new Vector3D(
                        //TODO: remove unIndexeds when no longer needed
                        //unIndexedVertices.get(vertexIndices.get(i + j) * verticesSize),
                        //unIndexedVertices.get(vertexIndices.get(i + j) * verticesSize + 1),
                        //unIndexedVertices.get(vertexIndices.get(i + j) * verticesSize + 2)});
                        this.vertices.get(this.vertexIndices.get(i + j) * this.verticesSize),
                        this.vertices.get(this.vertexIndices.get(i + j) * this.verticesSize + 1),
                        this.vertices.get(this.vertexIndices.get(i + j) * this.verticesSize + 2)));
//                    }
            }

            this.triangles.add(new Triangle(triangleVertices.get(0), triangleVertices.get(1), triangleVertices.get(2),
                    this.materials.get(0)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTrianglesCount() {
        return this.triangles.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUseDrawElements(boolean useDrawElements) {
        this.useDrawElements = useDrawElements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatBuffer getVertices() {
        return this.vertices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVertices(float[] vertices) {
        //TODO: remove unIndexeds when no longer needed
//        this.unIndexedVertices = (FloatBuffer) newDirectGLBuffer(GL_FLOAT, vertices.length);
//        this.unIndexedVertices.put(vertices);
//        vertices = indexVBO(vertexIndices, vertices, getVerticesSize());
        this.vertices = (FloatBuffer) newDirectGLBuffer(GL_FLOAT, vertices.length);
        this.vertices.put(vertices);

        this.vertices.rewind();
        float vertexSet[] = new float[this.vertices.limit()];
        this.vertices.get(vertexSet);
        float max_x, max_y, max_z;
        float min_x, min_y, min_z;
        max_x = max_y = max_z = NEGATIVE_INFINITY;
        min_x = min_y = min_z = POSITIVE_INFINITY;
        for (int i = 0; i < vertexSet.length; i += this.verticesSize) {
            if (vertexSet[i] >= max_x) {
                max_x = vertexSet[i];
            }
            if (vertexSet[i] <= min_x) {
                min_x = vertexSet[i];
            }
            if (vertexSet[i + 1] >= max_y) {
                max_y = vertexSet[i + 1];
            }
            if (vertexSet[i + 1] <= min_y) {
                min_y = vertexSet[i + 1];
            }
            if (vertexSet[i + 2] >= max_z) {
                max_z = vertexSet[i + 2];
            }
            if (vertexSet[i + 2] <= min_z) {
                min_z = vertexSet[i + 2];
            }
        }
        this.max = new Vector3D(max_x, max_y, max_z);
        this.min = new Vector3D(min_x, min_y, min_z);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatBuffer getNormals() {
        return this.normals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNormals(float[] normals) {
        //TODO: remove unIndexeds when no longer needed
//        this.unIndexedNormals = (FloatBuffer) newDirectGLBuffer(GL_FLOAT, normals.length);
//        this.unIndexedNormals.put(normals);
//        normals = indexVBO(normalsIndices, normals, getNormalsSize());
        this.normals = (FloatBuffer) newDirectGLBuffer(GL_FLOAT, normals.length);
        this.normals.put(normals);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatBuffer getTextureCoords() {
        return this.textureCoords;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTextureCoords(float[] textureCoords) {
        //TODO: remove unIndexeds when no longer needed
//        this.unIndexedTextureCoords = (FloatBuffer) newDirectGLBuffer(GL_FLOAT, textureCoords.length);
//        this.unIndexedTextureCoords.put(textureCoords);
//        textureCoords = indexVBO(texCoordIndices, textureCoords, getTextureCoordSize());
        this.textureCoords = (FloatBuffer) newDirectGLBuffer(GL_FLOAT, textureCoords.length);
        this.textureCoords.put(textureCoords);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IntBuffer getVertexIndices() {
        return this.vertexIndices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVertexIndices(int[] vertexIndices) {
        this.vertexIndices = (IntBuffer) newDirectGLBuffer(GL_INT, vertexIndices.length);
        this.vertexIndices.put(vertexIndices);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IntBuffer getNormalsIndices() {
        return this.normalsIndices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNormalsIndices(int[] normalsIndices) {
        this.normalsIndices = (IntBuffer) newDirectGLBuffer(GL_INT, normalsIndices.length);
        this.normalsIndices.put(normalsIndices);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IntBuffer getTexCoordIndices() {
        return this.texCoordIndices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTexCoordIndices(int[] texCoordIndices) {
        this.texCoordIndices = (IntBuffer) newDirectGLBuffer(GL_INT, texCoordIndices.length);
        this.texCoordIndices.put(texCoordIndices);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTextureCoordSize() {
        return this.textureCoordSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTextureCoordSize(int textureCoordSize) {
        this.textureCoordSize = textureCoordSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNormalsSize() {
        return this.normalsSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNormalsSize(int normalsSize) {
        this.normalsSize = normalsSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getVerticesSize() {
        return this.verticesSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVerticesSize(int verticesSize) {
        this.verticesSize = verticesSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndexStride() {
        return this.indexStride;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIndexStride(int indexStride) {
        this.indexStride = indexStride;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPrimitiveType() {
        return this.primitiveType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPrimitiveType(PrimitiveType primitiveType) {
        switch (primitiveType) {
            case POINTS:
                this.primitiveType = GL_POINTS;
                break;
            case LINES:
                this.primitiveType = GL_LINES;
                break;
            case LINE_STRIP:
                this.primitiveType = GL_LINE_STRIP;
                break;
            case LINE_LOOP:
                this.primitiveType = GL_LINE_LOOP;
                break;
            case TRIANGLES:
                this.primitiveType = GL_TRIANGLES;
                break;
            case TRIANGLE_STRIP:
                this.primitiveType = GL_TRIANGLE_STRIP;
                break;
            case TRIANGLE_FAN:
                this.primitiveType = GL_TRIANGLE_FAN;
                break;
            default:
                throw new AssertionError(primitiveType.name());
        }
    }

//    /**
//     *
//     * Get the texture to use/map in the render, to which the textureCoords
//     * refer to.
//     *
//     * @return this mesh texture.
//     */
//    public Texture getTexture() {
//        return materials.get(0).getTexture();
//    }
//
//    /**
//     * Set the texture to use/map in the render, to which the textureCoords
//     * refer to.
//     *
//     * @param texture the texture to use/map.
//     */
//    public void setTexture(Texture texture) {
//        materials.get(0).setTexture(texture);
//    }
//    
    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<Material> getMaterials() {
        return this.materials;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaterials(ArrayList<Material> materials) {
        this.materials = materials;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Material getMaterial(int index) {
        return this.materials.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaterial(Material material) {
        this.materials.add(material);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTextures(ArrayList<Texture> textures) {
        for (int i = 0; i < this.materials.size(); i++) {
            this.materials.get(i).setTexture(textures.get(i));
        }
    }

    //TODO: remove unIndexeds when no longer needed
//    /**
//     * Get the un-indexed vertices of this mesh.
//     *
//     * @return the un-indexed vertices of this mesh.
//     */
//    public FloatBuffer getUnIndexedVertices() {
//        return unIndexedVertices;
//    }
//
//    /**
//     * Get the un-indexed normals of this mesh.
//     *
//     * @return the un-indexed normals of this mesh.
//     */
//    public FloatBuffer getUnIndexedNormals() {
//        return unIndexedNormals;
//    }
//
//    /**
//     * Get the un-indexed texture coordinates of this mesh.
//     *
//     * @return the un-indexed texture coordinates of this mesh.
//     */
//    public FloatBuffer getUnIndexedTextureCoords() {
//        return unIndexedTextureCoords;
//    }
//    
//
    //TODO: remove when no longer needed
//    /**
//     * Index all the values of an provided float array, i.e., depending on the
//     * stride for each vertex, normal, or textureCoord store its values ordered.
//     *
//     * @param indices the indices order to render each value in values.
//     * @param values the values of each vertex, normal, or textureCoord
//     * elements. e.g., for vertices x, y, sometimes z, and sometimes alpha.
//     * @param stride the number of elements per vertex, normal textureCoord.
//     * @return a sorted float array with the indexed values of values.
//     */
//    protected float[] indexVBO(IntBuffer indices, float[] values, int stride) {
//        float[] out_values = new float[indices.capacity() * stride];
//        for (int i = 0; i < indices.capacity(); i++) {
//            out_values[i * stride] = values[indices.get(i) * stride];
//            out_values[i * stride + 1] = values[indices.get(i) * stride + 1];
////            if (stride == 3) {
//            out_values[i * stride + 2] = values[indices.get(i) * stride + 2];
////            }
////            if (stride == 4) {
////                out_values[i * stride + 3] = values[indices.get(i) * stride + 3];
////            }
//        }
//
//        return out_values;
//    }
//    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.Id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(String Id) {
        this.Id = Id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PolygonMesh getNode() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GL2 gl) {
        if (this.materials.get(0).isRenderable()) {
            gl.glPushMatrix();
            {
                this.materials.get(0).applyMaterialProperties(gl);

                //Show wireframe meshes test.
                gl.glPolygonMode(GL_FRONT_AND_BACK,
                        coreOptions.get("showWireframe")
                        ? GL_LINE : GL_FILL);

                //Bind and enable the texture to use associated with this mesh if any, only if showing textures.
                if (coreOptions.get("showTextures")
                        && this.materials.get(0).getTexture() != null) {
                    this.materials.get(0).getTexture().bind(gl);
                    this.materials.get(0).getTexture().enable(gl);
                }
                if (!coreOptions.get("useDisplayLists")) {
                    this.vertexIndices.rewind();
                    this.vertices.rewind();

                    if (this.normals != null) {
                        if (this.normalsIndices != null) {
                            this.normalsIndices.rewind();
                        }
                        this.normals.rewind();
                    }

                    if (this.textureCoords != null) {
                        if (this.texCoordIndices != null) {
                            this.texCoordIndices.rewind();
                        }
                        this.textureCoords.rewind();
                    }

                    gl.glEnableClientState(GL_VERTEX_ARRAY);
                    if (this.normals != null) {
                        gl.glEnableClientState(GL_NORMAL_ARRAY);
                    }
                    if (this.textureCoords != null) {
                        gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
                    }

                    gl.glVertexPointer(3, GL_FLOAT, 0, this.vertices);
                    if (this.normals != null) {
                        gl.glNormalPointer(GL_FLOAT, 0, this.normals);
                    }
                    if (this.textureCoords != null) {
                        gl.glTexCoordPointer(this.textureCoordSize, GL_FLOAT, 0, this.textureCoords);
                    }

                    if (this.useDrawElements) {
                        gl.glDrawElements(this.primitiveType, this.vertexIndices.capacity(), GL_UNSIGNED_INT, this.vertexIndices);
                    } else {
                        gl.glDrawArrays(this.primitiveType, 0, this.vertexIndices.capacity());
                    }

                    gl.glDisableClientState(GL_VERTEX_ARRAY);
                    if (this.normals != null) {
                        gl.glDisableClientState(GL_NORMAL_ARRAY);
                    }
                    if (this.textureCoords != null) {
                        gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
                    }
                } else {
                    if (this.genList) {
                        gl.glDeleteLists(this.listMesh, 1);
                        this.listMesh = gl.glGenLists(1);
                        gl.glNewList(this.listMesh, GL_COMPILE);
                        {
                            //JUST FOR TESTS ALLOWS TO RENDER MODELS NOT INDEXED!!!!!!
                            gl.glBegin(this.primitiveType);
                            for (int i = 0; i < this.vertexIndices.capacity(); i += this.indexStride) {
                                for (int j = 0; j < this.indexStride; j++) {
                                    if (this.normals != null) {
                                        gl.glNormal3f(
                                                this.normals.get(this.normalsIndices.get(i + j) * this.normalsSize),
                                                this.normals.get(this.normalsIndices.get(i + j) * this.normalsSize + 1),
                                                this.normals.get(this.normalsIndices.get(i + j) * this.normalsSize + 2));

                                    }
                                    if (this.textureCoords != null) {
                                        if (this.textureCoordSize == 2) {
                                            gl.glTexCoord2f(
                                                    this.textureCoords.get(this.texCoordIndices.get(i + j) * this.textureCoordSize),
                                                    this.textureCoords.get(this.texCoordIndices.get(i + j) * this.textureCoordSize + 1));
                                        } else {
                                            gl.glTexCoord3f(
                                                    this.textureCoords.get(this.texCoordIndices.get(i + j) * this.textureCoordSize),
                                                    this.textureCoords.get(this.texCoordIndices.get(i + j) * this.textureCoordSize + 1),
                                                    this.textureCoords.get(this.texCoordIndices.get(i + j) * this.textureCoordSize + 2));
                                        }
                                    }
//                                    if (verticesSize == 2) {
//                                        gl.glVertex2f(
//                                                vertices.get(vertexIndices.get(i + j) * verticesSize),
//                                                vertices.get(vertexIndices.get(i + j) * verticesSize + 1));
//                                    } else {
                                    gl.glVertex3f(
                                            this.vertices.get(this.vertexIndices.get(i + j) * this.verticesSize),
                                            this.vertices.get(this.vertexIndices.get(i + j) * this.verticesSize + 1),
                                            this.vertices.get(this.vertexIndices.get(i + j) * this.verticesSize + 2));
//                                    }
//                                    //TODO: remove unIndexeds when no longer needed                        
//                                    if (unIndexedNormals != null) {
//                                        gl.glNormal3f(
//                                                unIndexedNormals.get(normalsIndices.get(i + j) * normalsSize),
//                                                unIndexedNormals.get(normalsIndices.get(i + j) * normalsSize + 1),
//                                                unIndexedNormals.get(normalsIndices.get(i + j) * normalsSize + 2));
//
//                                    }
//                                    if (unIndexedTextureCoords != null) {
//                                        if (textureCoordSize == 2) {
//                                            gl.glTexCoord2f(
//                                                    unIndexedTextureCoords.get(texCoordIndices.get(i + j) * textureCoordSize),
//                                                    unIndexedTextureCoords.get(texCoordIndices.get(i + j) * textureCoordSize + 1));
//                                        } else {
//                                            gl.glTexCoord3f(
//                                                    unIndexedTextureCoords.get(texCoordIndices.get(i + j) * textureCoordSize),
//                                                    unIndexedTextureCoords.get(texCoordIndices.get(i + j) * textureCoordSize + 1),
//                                                    unIndexedTextureCoords.get(texCoordIndices.get(i + j) * textureCoordSize + 2));
//                                        }
//                                    }
//                                    if (verticesSize == 2) {
//                                        gl.glVertex2f(
//                                                unIndexedVertices.get(vertexIndices.get(i + j) * verticesSize),
//                                                unIndexedVertices.get(vertexIndices.get(i + j) * verticesSize + 1));
//                                    } else {
//                                        gl.glVertex3f(
//                                                unIndexedVertices.get(vertexIndices.get(i + j) * verticesSize),
//                                                unIndexedVertices.get(vertexIndices.get(i + j) * verticesSize + 1),
//                                                unIndexedVertices.get(vertexIndices.get(i + j) * verticesSize + 2));
//                                    }
                                }
                            }
                            gl.glEnd();
//                            gl.glBegin(primitiveType);
//                            for (ArrayList<Vector3D> simplex : getSimplexes()) {
//                                for (Vector3D vertex : simplex) {
//                                    gl.glVertex3d(vertex.getX(), vertex.getY(), vertex.getZ());
//                                }
//                            }
//                            gl.glEnd();
                        }
                        gl.glEndList();
                        this.genList = false;
                    }
                    gl.glCallList(this.listMesh);
                }

                //Disable the texture to use associated with this mesh if any, only if showing textures.
                if (coreOptions.get("showTextures")
                        && this.materials.get(0).getTexture() != null) {
                    this.materials.get(0).getTexture().disable(gl);
                }
            }
            gl.glPopMatrix();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(GL2 gl) {
        if (gl != null) {
            log.info("Dispose listMesh.");
            gl.glDeleteLists(this.listMesh, 1);
        }
    }
}
