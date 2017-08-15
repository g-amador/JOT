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

import java.io.FileInputStream;
import java.io.IOException;
import static java.lang.Float.parseFloat;
import static java.lang.String.format;
import static java.lang.System.arraycopy;
import java.math.BigInteger;
import static java.util.Collections.unmodifiableSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.io.data.format.GenericFormat.loadTextures;
import static jot.io.data.format.UnzipUtility.unzipUtility;
import jot.math.geometry.PolygonMesh;
import jot.math.geometry.TransformGroup;
import jot.math.geometry.bounding.AABB;
import jot.math.geometry.bounding.AbstractBoundingVolume;
import jot.math.geometry.bounding.BoundingSphere;
import jot.math.geometry.bounding.OBB;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.xmlbeans.XmlException;
import org.collada.x2005.x11.colladaSchema.COLLADADocument;
import static org.collada.x2005.x11.colladaSchema.COLLADADocument.Factory.parse;
import org.collada.x2005.x11.colladaSchema.CommonNewparamType;
import org.collada.x2005.x11.colladaSchema.EffectDocument;
import org.collada.x2005.x11.colladaSchema.GeometryDocument;
import org.collada.x2005.x11.colladaSchema.ImageDocument;
import org.collada.x2005.x11.colladaSchema.InputLocal;
import org.collada.x2005.x11.colladaSchema.InputLocalOffset;
import org.collada.x2005.x11.colladaSchema.LibraryEffectsDocument;
import org.collada.x2005.x11.colladaSchema.LibraryGeometriesDocument;
import org.collada.x2005.x11.colladaSchema.LibraryImagesDocument;
import org.collada.x2005.x11.colladaSchema.LibraryMaterialsDocument;
import org.collada.x2005.x11.colladaSchema.LibraryVisualScenesDocument;
import org.collada.x2005.x11.colladaSchema.MaterialDocument;
import org.collada.x2005.x11.colladaSchema.NodeDocument;
import org.collada.x2005.x11.colladaSchema.PolylistDocument;
import org.collada.x2005.x11.colladaSchema.ProfileCOMMONDocument;
import org.collada.x2005.x11.colladaSchema.SourceDocument;
import org.collada.x2005.x11.colladaSchema.TrianglesDocument;
import org.collada.x2005.x11.colladaSchema.VerticesDocument;
import org.collada.x2005.x11.colladaSchema.VisualSceneDocument;

/**
 * Class that implements a Collada 1.4.1 specification file parser, i.e., scene
 * loader. Only supports geometries, effects, materials, visualSceneNodes, and
 * images/textures.
 *
 * @author Gonçalo Amador {@literal &} Abel Gomes
 */
public class ColladaScene implements GenericFormat {

    static final Logger log = getLogger("ColladaScene");

    static {
        log.setLevel(OFF);
    }

    /**
     * Load the data, i.e., textures, geometry colors, etc., of a Collada file.
     *
     * @param fileName the path/name of the Collada file content to load.
     * @return the loaded data of a Collada file.
     * @throws LoaderException if the file cannot be read.
     */
    public static ColladaScene load(String fileName) throws LoaderException {
        ColladaScene colladaScene = new ColladaScene();
        try {
            colladaScene.parseFile(fileName);
        } catch (IOException ex) {
            throw new LoaderException("Error reading '" + fileName + "'.", ex);
        }
        return colladaScene;
    }

    /**
     * Load the data, i.e., textures, geometry colors, etc., of a Collada file,
     * and encapsulate it into a transformGroup.
     *
     * @param filePath the path to the file to load.
     * @param fileName the name of the file content to load.
     * @param scale the scale of this model.
     * @param boundingVolumeType the type of BoundingVolume that must engulf
     * this model.
     * @return the loaded data of a Collada file encapsulated into a
     * transformGroup.
     */
    @SuppressWarnings(value = {"unchecked"})
    public static TransformGroup loadFormat(
            String filePath,
            String fileName,
            float scale,
            AbstractBoundingVolume.BoundingVolumeType boundingVolumeType) {
        filePath = unzipUtility(filePath, fileName);

        ColladaScene colladaScene;
        try {
            colladaScene = load(filePath + fileName);
        } catch (LoaderException ex) {
            log.severe(ex.getMessage());
            return null;
        }
        float minX = 0, minY = 0, minZ = 0;
        float maxX = 0, maxY = 0, maxZ = 0;
        TransformGroup model = new TransformGroup(fileName);
        Set<String> geometries = colladaScene.getGeometryNames();
        for (String geometryName : geometries) {
            int[] positionIndices = null;
            int[] normalsIndices = null;
            int[] texCoordIndices = null;
            PolygonMesh geometryMesh = new PolygonMesh(fileName);
            GeometryDocument.Geometry geometry = colladaScene.getGeometry(geometryName);
            VerticesDocument.Vertices verticesInfo = geometry.getMesh().getVertices();
            String positionId = null;
            String normalsId = null;
            String texCoordId = null;
            InputLocal[] inputArray = verticesInfo.getInputArray();
            for (InputLocal inputLocal : inputArray) {
                if ("POSITION".equals(inputLocal.getSemantic())) {
                    positionId = inputLocal.getSource().substring(1); //Remove # from beginning of reference String
                }
                if ("NORMAL".equals(inputLocal.getSemantic())) {
                    normalsId = inputLocal.getSource().substring(1); //Remove # from beginning of reference String
                }
                if ("TEXCOORD".equals(inputLocal.getSemantic())) {
                    texCoordId = inputLocal.getSource().substring(1); //Remove # from beginning of reference String
                }
            }
            //Offset are used for determine the stride needed over the index array.
            int positionOffset = 0;
            int normalsOffset = 0;
            //int vertexOffset = 0;
            int texCoordOffset = 0;
            int colorOffset = 0;
            String materialName = null;
            if (geometry.getMesh().getTrianglesArray() != null
                    && geometry.getMesh().getTrianglesArray().length > 0) {
                log.info(geometry.getMesh().getTrianglesArray(0).toString());
                TrianglesDocument.Triangles triangles
                        = geometry.getMesh().getTrianglesArray(0);
                int triangleCount = triangles.getCount().intValue();
                materialName = triangles.getMaterial();
                InputLocalOffset[] triangleInputArray = triangles.getInputArray();
                for (InputLocalOffset inputLocalOffset : triangleInputArray) {
                    if ("POSITION".equals(inputLocalOffset.getSemantic())) {
                        positionId = inputLocalOffset.getSource().substring(1); //Remove # from beginning of reference String
                        positionOffset = inputLocalOffset.getOffset().intValue();
                    }
                    if ("NORMAL".equals(inputLocalOffset.getSemantic())) {
                        normalsId = inputLocalOffset.getSource().substring(1); //Remove # from beginning of reference String
                        normalsOffset = inputLocalOffset.getOffset().intValue();
                    }
                    if ("VERTEX".equals(inputLocalOffset.getSemantic())) {
                        //For later?                        
                        log.info("Not supported yet.");
                    }
                    if ("TEXCOORD".equals(inputLocalOffset.getSemantic())) {
                        texCoordId = inputLocalOffset.getSource().substring(1);
                        texCoordOffset = inputLocalOffset.getOffset().intValue();
                    }
                    if ("COLOR".equals(inputLocalOffset.getSemantic())) {
                        colorOffset = inputLocalOffset.getOffset().intValue();
                    }
                }
                //Summarize the offsets and use it as indexStride to retrieve the correct index from the list for positions
                int indexStride = positionOffset + normalsOffset + texCoordOffset + colorOffset;
                indexStride = indexStride > 0 ? indexStride : 1;
                List<BigInteger> indices = triangles.getP();
                positionIndices = new int[triangleCount * 3];
                normalsIndices = new int[triangleCount * 3];
                texCoordIndices = new int[triangleCount * 3];
                for (int i = 0; i < positionIndices.length; i++) {
                    positionIndices[i] = indices.get(i * indexStride + positionOffset).intValue();
                    normalsIndices[i] = indices.get(i * indexStride + normalsOffset).intValue();
                    texCoordIndices[i] = indices.get(i * indexStride + texCoordOffset).intValue();
                }
                log.info(format("%d %d %d", positionOffset, normalsOffset, texCoordOffset));
                geometryMesh.setVertexIndices(positionIndices);
                geometryMesh.setNormalsIndices(normalsIndices);
                geometryMesh.setTexCoordIndices(texCoordIndices);
                //geometryMesh.setPrimitiveType(TRIANGLES);
                //geometryMesh.setIndexStride(3);
            } else if (geometry.getMesh().getTrifansArray() != null
                    && geometry.getMesh().getTrifansArray().length > 0) {
                throw new UnsupportedOperationException("Triangle Fans not supported.");
            } else if (geometry.getMesh().getTristripsArray() != null
                    && geometry.getMesh().getTristripsArray().length > 0) {
                throw new UnsupportedOperationException("Triangle strips not supported.");
            } else if (geometry.getMesh().getPolygonsArray() != null
                    && geometry.getMesh().getPolygonsArray().length > 0) {
                throw new UnsupportedOperationException("QUADS Not supported.");
            } else if (geometry.getMesh().getPolylistArray() != null
                    && geometry.getMesh().getPolylistArray().length > 0) {
                log.info(geometry.getMesh().getPolylistArray(0).toString());
                PolylistDocument.Polylist polys = geometry.getMesh().getPolylistArray(0);
                int polysCount = polys.getCount().intValue();
                materialName = polys.getMaterial();
                InputLocalOffset[] polysInputArray = polys.getInputArray();
                for (InputLocalOffset inputLocalOffset : polysInputArray) {

                    if ("POSITION".equals(inputLocalOffset.getSemantic())) {
                        positionId = inputLocalOffset.getSource().substring(1); //Remove # from beginning of reference String
                        positionOffset = inputLocalOffset.getOffset().intValue();
                    }
                    if ("NORMAL".equals(inputLocalOffset.getSemantic())) {
                        normalsId = inputLocalOffset.getSource().substring(1); //Remove # from beginning of reference String
                        normalsOffset = inputLocalOffset.getOffset().intValue();
                    }
                    if ("VERTEX".equals(inputLocalOffset.getSemantic())) {
                        //For later?
                        log.info("Not supported yet.");
                    }
                    if ("TEXCOORD".equals(inputLocalOffset.getSemantic())) {
                        texCoordId = inputLocalOffset.getSource().substring(1);
                        texCoordOffset = inputLocalOffset.getOffset().intValue();
                    }
                    if ("COLOR".equals(inputLocalOffset.getSemantic())) {
                        colorOffset = inputLocalOffset.getOffset().intValue();
                    }
                }

                //Summarize the offsets and use it as indexStride to retrieve the correct index from the list for positions
                int indexStride = 1;
                List<BigInteger> indices = polys.getP();
                if (normalsOffset != 0) {
                    indexStride++;
                }
                if (texCoordOffset != 0) {
                    indexStride++;
                }
                if (colorOffset != 0) {
                    indexStride++;
                }
                int primitiveType = indices.size() / polysCount / indexStride;
                positionIndices = new int[polysCount * primitiveType];
                normalsIndices = new int[polysCount * primitiveType];
                texCoordIndices = new int[polysCount * primitiveType];

                for (int i = 0; i < positionIndices.length; i++) {
                    positionIndices[i] = indices.get(i * indexStride + positionOffset).intValue();
                    normalsIndices[i] = indices.get(i * indexStride + normalsOffset).intValue();
                    texCoordIndices[i] = indices.get(i * indexStride + texCoordOffset).intValue();
                }
                log.info(format("%d %d %d", positionOffset, normalsOffset, texCoordOffset));
                geometryMesh.setVertexIndices(positionIndices);
                geometryMesh.setNormalsIndices(normalsIndices);
                geometryMesh.setTexCoordIndices(texCoordIndices);
                //geometryMesh.setPrimitiveType(TRIANGLES);
                //geometryMesh.setIndexStride(3);
                if (primitiveType > 3) {
                    throw new UnsupportedOperationException("QUADS Not supported.");
                }
            }

            SourceDocument.Source[] sourceArray = geometry.getMesh().getSourceArray();
            for (SourceDocument.Source source : sourceArray) {
                List<Double> floatArrayList = source.getFloatArray1().getListValue();
                BigInteger stride = source.getTechniqueCommon().getAccessor().getStride();
                float[] floatArray = new float[floatArrayList.size()];
                for (int i = 0; i < floatArray.length; i++) {
                    floatArray[i] = floatArrayList.get(i).floatValue();
                }
                if (source.getId().equals(positionId)) {
                    if (stride != null) {
                        geometryMesh.setVerticesSize(stride.intValue());
                        if (positionIndices != null) {
                            geometryMesh.setVertices(indexVBO(positionIndices, copyArray(floatArray), stride.intValue()));
                        } else {
                            geometryMesh.setVertices(copyArray(floatArray));
                        }
                    } else {
                        geometryMesh.setVertices(copyArray(floatArray));
                    }
                } else if (source.getId().equals(normalsId)) {
                    if (stride != null) {
                        geometryMesh.setNormalsSize(stride.intValue());
                        if (normalsIndices != null) {
                            geometryMesh.setNormals(indexVBO(normalsIndices, copyArray(floatArray), stride.intValue()));
                        } else {
                            geometryMesh.setNormals(copyArray(floatArray));
                        }
                    } else {
                        geometryMesh.setNormals(copyArray(floatArray));
                    }
                } else if (source.getId().equals(texCoordId)) {
                    if (stride != null) {
                        geometryMesh.setTextureCoordSize(stride.intValue());
                        if (normalsIndices != null) {
                            geometryMesh.setTextureCoords(indexVBO(texCoordIndices, copyArray(floatArray), stride.intValue()));
                        } else {
                            geometryMesh.setTextureCoords(copyArray(floatArray));
                        }
                    } else {
                        geometryMesh.setTextureCoords(copyArray(floatArray));
                    }
                }
            }

            if (colladaScene.getMaterialNames().size() < 2) {
                materialName = colladaScene.getMaterialNames().iterator().next();
            }
            //FIXME: correct the mistery bug when editing material properties.
            MaterialDocument.Material material = colladaScene.getMaterial(materialName);
            String effectReference = material.getInstanceEffect().getUrl().substring(1);
            EffectDocument.Effect effect = colladaScene.getEffect(effectReference);
            if (((ProfileCOMMONDocument.ProfileCOMMON) effect.getFxProfileAbstractArray(0)).getTechnique().getPhong() != null) {
                ProfileCOMMONDocument.ProfileCOMMON.Technique.Phong materialPhong = ((ProfileCOMMONDocument.ProfileCOMMON) effect.getFxProfileAbstractArray(0)).getTechnique().getPhong();
                if (materialPhong.isSetEmission() && materialPhong.getEmission().isSetColor()) {
                    String emissionValue = materialPhong.getEmission().getColor().getStringValue();
                    log.info(format("emission: " + emissionValue));
                    geometryMesh.getMaterials().get(0).setE(
                            colladaScene.getFloatArray(emissionValue));
                }

                if (materialPhong.isSetAmbient() && materialPhong.getAmbient().isSetColor()) {
                    String ambientValue = materialPhong.getAmbient().getColor().getStringValue();
                    log.info(format("ambient: " + ambientValue));
                    geometryMesh.getMaterials().get(0).setKa(
                            colladaScene.getFloatArray(ambientValue));
                }

                if (materialPhong.isSetDiffuse() && materialPhong.getDiffuse().isSetColor()) {
                    String diffuseValue = materialPhong.getDiffuse().getColor().getStringValue();
                    log.info(format("diffuse: " + diffuseValue));
                    geometryMesh.getMaterials().get(0).setKd(
                            colladaScene.getFloatArray(diffuseValue));
                }

                if (materialPhong.isSetSpecular() && materialPhong.getSpecular().isSetColor()) {
                    String specularValue = materialPhong.getSpecular().getColor().getStringValue();
                    log.info(format("specular: " + specularValue));
                    geometryMesh.getMaterials().get(0).setKs(
                            colladaScene.getFloatArray(specularValue));
                }

                if (materialPhong.isSetShininess() && materialPhong.getShininess().isSetFloat()) {
                    geometryMesh.getMaterials().get(0).setNs(
                            parseFloat(materialPhong.getShininess().getFloat().getStringValue()));
                }
            }
            if (((ProfileCOMMONDocument.ProfileCOMMON) effect.getFxProfileAbstractArray(0)).getTechnique().getBlinn() != null) {
                ProfileCOMMONDocument.ProfileCOMMON.Technique.Blinn materialBlinn
                        = ((ProfileCOMMONDocument.ProfileCOMMON) effect.getFxProfileAbstractArray(0)).getTechnique().getBlinn();

                if (materialBlinn.isSetEmission() && materialBlinn.getEmission().isSetColor()) {
                    String emissionValue = materialBlinn.getEmission().getColor().getStringValue();
                    log.info(format("emission: " + emissionValue));
                    geometryMesh.getMaterials().get(0).setE(
                            colladaScene.getFloatArray(emissionValue));
                }

                if (materialBlinn.isSetAmbient() && materialBlinn.getAmbient().isSetColor()) {
                    String ambientValue = materialBlinn.getAmbient().getColor().getStringValue();
                    log.info(format("ambient: " + ambientValue));
                    geometryMesh.getMaterials().get(0).setKa(
                            colladaScene.getFloatArray(ambientValue));
                }

                if (materialBlinn.isSetDiffuse() && materialBlinn.getDiffuse().isSetColor()) {
                    String diffuseValue = materialBlinn.getDiffuse().getColor().getStringValue();
                    log.info(format("diffuse: " + diffuseValue));
                    geometryMesh.getMaterials().get(0).setKd(
                            colladaScene.getFloatArray(diffuseValue));
                }

                if (materialBlinn.isSetSpecular() && materialBlinn.getSpecular().isSetColor()) {
                    String specularValue = materialBlinn.getSpecular().getColor().getStringValue();
                    log.info(format("specular: " + specularValue));
                    geometryMesh.getMaterials().get(0).setKs(
                            colladaScene.getFloatArray(specularValue));
                }

                if (materialBlinn.isSetShininess() && materialBlinn.getShininess().isSetFloat()) {
                    geometryMesh.getMaterials().get(0).setNs(
                            parseFloat(materialBlinn.getShininess().getFloat().getStringValue()));
                }
            }
            if (((ProfileCOMMONDocument.ProfileCOMMON) effect.getFxProfileAbstractArray(0)).getNewparamArray() != null) {
                CommonNewparamType[] commonNewparamTypes
                        = ((ProfileCOMMONDocument.ProfileCOMMON) effect.getFxProfileAbstractArray(0)).getNewparamArray();

                for (CommonNewparamType commonNewparamType : commonNewparamTypes) {
                    if (commonNewparamType.getSurface() != null) {
                        geometryMesh.setTextures(loadTextures(filePath));
                    }
                }
            }

            Vector3D tmpVertex = geometryMesh.getMinVertex();
            if (tmpVertex.getX() <= minX) {
                minX = (float) (tmpVertex.getX() / scale);
            }
            if (tmpVertex.getY() <= minY) {
                minY = (float) (tmpVertex.getY() / scale);
            }
            if (tmpVertex.getZ() <= minZ) {
                minZ = (float) (tmpVertex.getZ() / scale);
            }

            tmpVertex = geometryMesh.getMaxVertex();
            if (tmpVertex.getX() >= maxX) {
                maxX = (float) (tmpVertex.getX() / scale);
            }
            if (tmpVertex.getY() >= maxY) {
                maxY = (float) (tmpVertex.getY() / scale);
            }
            if (tmpVertex.getZ() >= maxZ) {
                maxZ = (float) (tmpVertex.getZ() / scale);
            }

            geometryMesh.setUseDrawElements(false);
            geometryMesh.setTriangles();
            model.addChild(geometryMesh);
        }

        TransformGroup modelWithTransformations
                = new TransformGroup(fileName + "WithTransformations");
        modelWithTransformations.addChild(model);
        model.setBoundingVolume(0, null);
        switch (boundingVolumeType) {
            case SPHERE:
                modelWithTransformations.setBoundingVolume(0,
                        new BoundingSphere(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ)));
                break;
            case AABB:
                modelWithTransformations.setBoundingVolume(0,
                        new AABB(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ)));
                break;
            case OBB:
                modelWithTransformations.setBoundingVolume(0,
                        new OBB(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ)));
                break;
            default:
                log.info("Not supported BV.");
        }
        scale = 1 / scale;
        modelWithTransformations.setScaling(new Vector3D(scale, scale, scale));
        return modelWithTransformations;
    }

    /**
     * Make a copy of the values of a float array.
     *
     * @param src_array the original array to copy.
     * @return float array that contains a copy of the values src_array.
     */
    private static float[] copyArray(float[] src_array) {
        float[] result = new float[src_array.length];
        arraycopy(src_array, 0, result, 0, src_array.length);

        return result;
    }

    /**
     * Index all the values of an provided float array, i.e., depending on the
     * stride for each vertex, normal, or textureCoord store its values ordered.
     *
     * @param indices the indices order to render each value in values.
     * @param values the values of each vertex, normal, or textureCoord
     * elements. e.g., for vertices x, y, sometimes z, and sometimes alpha.
     * @param stride the number of elements per vertex, normal textureCoord.
     * @return a sorted float array with the indexed values of values.
     */
    static float[] indexVBO(int[] indices, float[] values, int stride) {
        float[] out_values = new float[indices.length * stride];
        for (int i = 0; i < indices.length; i++) {
            out_values[i * stride] = values[indices[i] * stride];
            out_values[i * stride + 1] = values[indices[i] * stride + 1];
            if (stride == 3) {
                out_values[i * stride + 2] = values[indices[i] * stride + 2];
            }
//            if (stride == 4) {
//                out_values[i * stride + 3] = values[indices[i] * stride + 3];
//            }
        }

        return out_values;
    }

    private final ConcurrentHashMap<String, GeometryDocument.Geometry> geometries;
    private final ConcurrentHashMap<String, EffectDocument.Effect> effects;
    private final ConcurrentHashMap<String, MaterialDocument.Material> materials;
    private final ConcurrentHashMap<String, NodeDocument.Node> visualSceneNodes;
    private final ConcurrentHashMap<String, ImageDocument.Image> images;

    /**
     * Constructor.
     */
    public ColladaScene() {
        this.geometries = new ConcurrentHashMap<>();
        this.effects = new ConcurrentHashMap<>();
        this.materials = new ConcurrentHashMap<>();
        this.visualSceneNodes = new ConcurrentHashMap<>();
        this.images = new ConcurrentHashMap<>();
    }

    @Override
    public void parseFile(String fileName) throws IOException, LoaderException {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            COLLADADocument colladaDocument;
            colladaDocument = parse(fis);

            if (colladaDocument == null || colladaDocument.getCOLLADA() == null) {
                throw new LoaderException("Invalid COLLADA document.");
            }
            LibraryImagesDocument.LibraryImages[] libraryImagesArray = colladaDocument.getCOLLADA().getLibraryImagesArray();
            for (LibraryImagesDocument.LibraryImages libraryImages : libraryImagesArray) {
                ImageDocument.Image[] imageArray = libraryImages.getImageArray();
                for (ImageDocument.Image image : imageArray) {
                    this.images.put(image.getId(), image);
                }
            }
            LibraryGeometriesDocument.LibraryGeometries[] libraryGeometries
                    = colladaDocument.getCOLLADA().getLibraryGeometriesArray();
            if (libraryGeometries != null) {
                for (LibraryGeometriesDocument.LibraryGeometries libraryGeometry : libraryGeometries) {
                    GeometryDocument.Geometry[] geometryArray = libraryGeometry.getGeometryArray();
                    if (geometryArray != null) {
                        for (GeometryDocument.Geometry geometry : geometryArray) {
                            this.geometries.put(geometry.getId(), geometry);
                        }
                    }
                }
            }
            LibraryEffectsDocument.LibraryEffects[] libraryEffects = colladaDocument.getCOLLADA().getLibraryEffectsArray();
            if (libraryEffects != null) {
                for (LibraryEffectsDocument.LibraryEffects libraryEffect : libraryEffects) {
                    EffectDocument.Effect[] effectArray = libraryEffect.getEffectArray();
                    if (effectArray != null) {
                        for (EffectDocument.Effect effect : effectArray) {
                            this.effects.put(effect.getId(), effect);
                        }
                    }
                }
            }
            LibraryMaterialsDocument.LibraryMaterials[] libraryMaterials
                    = colladaDocument.getCOLLADA().getLibraryMaterialsArray();
            if (libraryMaterials != null) {
                for (LibraryMaterialsDocument.LibraryMaterials libraryMaterial : libraryMaterials) {
                    MaterialDocument.Material[] materialArray = libraryMaterial.getMaterialArray();
                    if (materialArray != null) {
                        for (MaterialDocument.Material material : materialArray) {
                            this.materials.put(material.getId(), material);
                        }
                    }
                }
            }
            LibraryVisualScenesDocument.LibraryVisualScenes[] libraryVisualScenes
                    = colladaDocument.getCOLLADA().getLibraryVisualScenesArray();
            if (libraryVisualScenes != null) {
                for (LibraryVisualScenesDocument.LibraryVisualScenes libraryVisualScene : libraryVisualScenes) {
                    VisualSceneDocument.VisualScene[] visualScenes = libraryVisualScene.getVisualSceneArray();
                    if (visualScenes != null) {
                        for (VisualSceneDocument.VisualScene visualScene : visualScenes) {
                            NodeDocument.Node[] nodeArray = visualScene.getNodeArray();
                            if (nodeArray != null) {
                                for (NodeDocument.Node node : nodeArray) {
                                    this.visualSceneNodes.put(node.getId(), node);
                                }
                            }
                        }
                    }
                }
            }
        } catch (XmlException ex) {
            throw new LoaderException("Error parsing '" + fileName + "'.", ex);
        }
    }

    /**
     * Get all geometries identifiers.
     *
     * @return all geometries identifiers.
     */
    public Set<String> getGeometryNames() {
        return unmodifiableSet(this.geometries.keySet());
    }

    /**
     * Get the data (if it exists) of a geometry identified by a provided key.
     *
     * @param key a geometry identifier.
     * @return geometry, identified by given key, data.
     */
    public GeometryDocument.Geometry getGeometry(String key) {
        return this.geometries.get(key);
    }

    /**
     * Get all effects identifiers.
     *
     * @return all effects identifiers.
     */
    public Set<String> getEffectNames() {
        return unmodifiableSet(this.effects.keySet());
    }

    /**
     * Get the data (if it exists) of a effect identified by a provided key.
     *
     * @param key a effect identifier.
     * @return effect, identified by given key, data.
     */
    public EffectDocument.Effect getEffect(String key) {
        return this.effects.get(key);
    }

    /**
     * Get all materials identifiers.
     *
     * @return all materials identifiers.
     */
    public Set<String> getMaterialNames() {
        return unmodifiableSet(this.materials.keySet());
    }

    /**
     * Get the data (if it exists) of a material identified by a provided key.
     *
     * @param key a material identifier.
     * @return material, identified by given key, data.
     */
    public MaterialDocument.Material getMaterial(String key) {
        return this.materials.get(key);
    }

    /**
     * Get all nodes identifiers.
     *
     * @return all nodes identifiers.
     */
    public Set<String> getNodeNames() {
        return unmodifiableSet(this.visualSceneNodes.keySet());
    }

    /**
     * Get the data (if it exists) of a node identified by a provided key.
     *
     * @param key a node identifier.
     * @return node, identified by given key, data.
     */
    public NodeDocument.Node getNode(String key) {
        return this.visualSceneNodes.get(key);
    }

    /**
     * Get the data (if it exists) of a image identified by a provided key.
     *
     * @param key a image identifier.
     * @return image, identified by given key, data.
     */
    public ImageDocument.Image getImage(String key) {
        return this.images.get(key);
    }

    /**
     * Convert a string representing (i.e., containing the data of) a float
     * array to a float array.
     *
     * @param floatArrayString a string representing a float array.
     * @return a float array.
     */
    public float[] getFloatArray(String floatArrayString) {
        String[] floatData = floatArrayString.split(" ");
        float[] floatArray = new float[floatData.length];
        for (int i = 0; i < floatData.length; i++) {
            String value = floatData[i];
            floatArray[i] = parseFloat(value);
        }
        return floatArray;
    }

    /**
     * Convert a string representing (i.e., containing the data of) a int array
     * to a int array.
     *
     * @param intArrayString a string representing a int array.
     * @return a int array.
     */
    public int[] getIntArray(String intArrayString) {
        String[] intData = intArrayString.split(" ");
        int[] intArray = new int[intData.length];
        for (int i = 0; i < intArray.length; i++) {
            int value = intArray[i];
            intArray[i] = value;
        }
        return intArray;
    }

    /**
     * Return a string with all the keys for geometries, effects, materials, and
     * nodes. Each key set corresponds is separated by an line break.
     *
     * @return a string that contains all the geometries names, followed by all
     * the effects names, followed by all materials names, follows by all nodes
     * names.
     */
    @Override
    public String toString() {
        StringBuilder objectAsString;
        objectAsString = new StringBuilder();
        Set<String> geometryNames;
        geometryNames = this.getGeometryNames();
        objectAsString.append("\nGeometries: ");
        geometryNames.stream().forEach(
                geometryName -> objectAsString.append(geometryName).append(", "));
        objectAsString.append("\nEffects: ");
        Set<String> effectNames = this.getEffectNames();
        effectNames.stream().forEach(
                effectName -> objectAsString.append(effectName).append(", "));
        objectAsString.append("\nMaterials: ");
        Set<String> materialNames = this.getMaterialNames();
        materialNames.stream().forEach(
                materialName -> objectAsString.append(materialName).append(", "));
        objectAsString.append("\nNodes: ");
        Set<String> nodeNames = this.getNodeNames();
        nodeNames.stream().forEach(
                nodeName -> objectAsString.append(nodeName).append(", "));
        objectAsString.append("\n");
        return objectAsString.toString();
    }
}
