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

//import com.jogamp.opengl.util.texture.Texture;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.io.data.format.UnzipUtility.unzipUtility;
import jot.math.geometry.PolygonMesh;
import jot.math.geometry.TransformGroup;
import jot.math.geometry.bounding.AABB;
import jot.math.geometry.bounding.AbstractBoundingVolume;
import jot.math.geometry.bounding.BoundingSphere;
import jot.math.geometry.bounding.OBB;
import jot.physics.Material;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements a WavefrontOBJ file parser. Load the WavefrontOBJ
 * model, centering and scaling it. The scale comes from the sz argument in the
 * constructor, and is implemented by changing the vertices of the loaded model.
 *
 * The model can have vertices, normals and tex coordinates, and refer to
 * materials in a MTL file.
 *
 * Information about the model is printed to stdout.
 *
 * @author Gonçalo Amador {@literal &} Abel Gomes
 */
public class WavefrontOBJ implements GenericFormat {

    static final Logger log = getLogger("WavefrontOBJ");

    private static final float DUMMY_Z_TC = -5.0f;
    //private String fileName;    // with path and ".WavefrontOBJ" extension
    private static String modelDir;

    static {
        log.setLevel(OFF);
    }

    /**
     * Load the data, i.e., geometry and materials of a WavefrontOBJ file.
     *
     * @param fileName the path/name of the WavefrontOBJ file content to load.
     * @return the loaded data of a WavefrontOBJ file.
     * @throws LoaderException
     */
    public static WavefrontOBJ load(String fileName) throws LoaderException {
        WavefrontOBJ obj = new WavefrontOBJ();
        try {
            obj.parseFile(fileName);
        } catch (IOException ex) {
            throw new LoaderException("Error reading '" + fileName + "'.", ex);
        }
        return obj;
    }

    /**
     * Load the data, i.e., geometry and materials of a WavefrontOBJ file, and
     * encapsulate it into a transformGroup.
     *
     * @param filePath the path to the file to load.
     * @param fileName the name of the file content to load.
     * @param scale the scale of this model.
     * @param boundingVolumeType the type of BoundingVolume that must engulf
     * this model.
     * @return the loaded data of a WavefrontOBJ file encapsulated into a
     * transformGroup.
     */
    //@SuppressWarnings({"unchecked"})
    public static TransformGroup loadFormat(
            String filePath,
            String fileName,
            float scale,
            AbstractBoundingVolume.BoundingVolumeType boundingVolumeType) {
        filePath = unzipUtility(filePath, fileName);

        WavefrontOBJ obj;
        try {
            obj = load(filePath + fileName);
        } catch (LoaderException ex) {
            log.severe(ex.getMessage());
            return null;
        }

        float minX = 0, minY = 0, minZ = 0;
        float maxX = 0, maxY = 0, maxZ = 0;
        TransformGroup objModel = new TransformGroup(fileName);
        //Vector3D min = ZERO;
        //Vector3D max = ZERO;

        int[] positionIndices = new int[obj.faces.getNumFaces() * obj.faces.facesVertIdxs.get(0).length];
        int[] normalsIndices = new int[obj.faces.getNumFaces() * obj.faces.facesNormIdxs.get(0).length];
        int[] textureIndices = new int[obj.faces.getNumFaces() * obj.faces.facesTexIdxs.get(0).length];
        float[] positions = new float[obj.faces.getNumFaces() * obj.faces.facesVertIdxs.get(0).length * 3];
        float[] normals = new float[obj.faces.getNumFaces() * obj.faces.facesNormIdxs.get(0).length * 3];
        int textureCoordSize
                = !obj.textureCoords.isEmpty()
                && obj.textureCoords.get(0).getZ() != DUMMY_Z_TC
                ? 3 // using 3D tex coords
                : 2;// using 2D tex coords
        float[] textureCoords = new float[obj.faces.getNumFaces() * obj.faces.facesTexIdxs.get(0).length * textureCoordSize];

        PolygonMesh geometryMesh = new PolygonMesh(fileName);

//        int polytype;
//        if (obj.faces.facesVertIdxs.get(0).length == 3) {
//            polytype = GL2.GL_TRIANGLES;
//        } else if (obj.faces.facesVertIdxs.get(0).length == 4) {
//            polytype = GL2.GL_QUADS;
//        } else {
//            polytype = GL2.GL_POLYGON;
//        }
        int posIndex = 0;
        for (Vector3D v : obj.vertices) {
            positions[posIndex + 0] = (float) v.getX();
            positions[posIndex + 1] = (float) v.getY();
            positions[posIndex + 2] = (float) v.getZ();
            posIndex += 3;
        }
        if (obj.faces.facesNormIdxs.get(0)[0] != 0) {
            int norIndex = 0;
            for (Vector3D n : obj.normals) {
                normals[norIndex + 0] = (float) n.getX();
                normals[norIndex + 1] = (float) n.getY();
                normals[norIndex + 2] = (float) n.getZ();
                norIndex += 3;
            }
        }
        if (obj.faces.facesTexIdxs.get(0)[0] != 0) {
            int texIndex = 0;
            for (Vector3D tc : obj.textureCoords) {
                textureCoords[texIndex + 0] = (float) tc.getX();
                textureCoords[texIndex + 1] = (float) tc.getY();
                if (tc.getZ() == DUMMY_Z_TC) // using 2D tex coords
                {
                    texIndex += 2;
                } else // 3D tex coords 
                {
                    textureCoords[texIndex + 2] = (float) tc.getZ();
                    texIndex += 3;
                }
            }
        }

        int posIndices = 0;
        for (int[] vIndeces : obj.faces.facesVertIdxs) {
            for (int vIndex : vIndeces) {
                positionIndices[posIndices] = vIndex - 1;
                posIndices++;
            }
        }
        if (obj.faces.facesNormIdxs.get(0)[0] != 0) {
            int norIndices = 0;
            for (int[] nIndeces : obj.faces.facesNormIdxs) {
                for (int nIndex : nIndeces) {
                    normalsIndices[norIndices] = nIndex - 1;
                    norIndices++;
                }
            }
        }
        if (obj.faces.facesTexIdxs.get(0)[0] != 0) {
            int texIndices = 0;
            for (int[] tIndeces : obj.faces.facesTexIdxs) {
                for (int tIndex : tIndeces) {
                    textureIndices[texIndices] = tIndex - 1;
                    texIndices++;
                }
            }
        }

        //No indexes alternatives
        //for (int[] vIndeces : obj.faces.facesVertIdxs) {
        //    for (int vIndex : vIndeces) {
        //        positions[posIndex + 0] = obj.vertices.get(vIndex - 1).getX();
        //        positions[posIndex + 1] = obj.vertices.get(vIndex - 1).getY();
        //        positions[posIndex + 2] = obj.vertices.get(vIndex - 1).getZ();
        //        posIndex += 3;
        //    }
        //}
        // Assume all OBJ models loaded are triangle based!!!!
        geometryMesh.setVertexIndices(positionIndices);
        geometryMesh.setVertices(positions);
        geometryMesh.setNormalsIndices(normalsIndices);
        geometryMesh.setNormals(normals);
        geometryMesh.setTextureCoordSize(textureCoordSize);
        geometryMesh.setTexCoordIndices(textureIndices);
        geometryMesh.setTextureCoords(textureCoords);
        //geometryMesh.setTextures(loadTextures(filePath));

        Vector3D tmpVertex = geometryMesh.getMinVertex();
        if (tmpVertex.getX() < minX) {
            minX = (float) (tmpVertex.getX() / scale);
        }
        if (tmpVertex.getY() < minY) {
            minY = (float) (tmpVertex.getY() / scale);
        }
        if (tmpVertex.getZ() < minZ) {
            minZ = (float) (tmpVertex.getZ() / scale);
        }

        tmpVertex = geometryMesh.getMaxVertex();
        if (tmpVertex.getX() > maxX) {
            maxX = (float) (tmpVertex.getX() / scale);
        }
        if (tmpVertex.getY() > maxY) {
            maxY = (float) (tmpVertex.getY() / scale);
        }
        if (tmpVertex.getZ() > maxZ) {
            maxZ = (float) (tmpVertex.getZ() / scale);
        }

        geometryMesh.setTriangles();
        objModel.addChild(geometryMesh);

        TransformGroup modelWithTransformations = new TransformGroup(fileName + "WithTransformations");
        modelWithTransformations.addChild(objModel);
        objModel.setBoundingVolume(0, null);
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

    // collection of vertices, normals and texture coords for the model
    private final ArrayList<Vector3D> vertices;
    private final ArrayList<Vector3D> normals;
    private final ArrayList<Vector3D> textureCoords;
    private boolean hasTCs3D = true;
    // whether the model uses 3D or 2D tex coords
    private final Faces faces;              // model faces
    private final FaceMaterials faceMats;   // materials used by faces
    private Materials materials;      // materials defined in MTL file
    private final ModelDimensions modelDims;  // model dimensions
    // for scaling the model    
    private float maxSize;
    //private int modelDispList;  // the model's display list

    //public WavefrontOBJ(GL2 gl, String nm, float sz, boolean showDetails) {
    /**
     * Constructor.
     */
    public WavefrontOBJ() {
//        fileName = nm;
//        maxSize = sz;
        this.vertices = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.textureCoords = new ArrayList<>();

        this.faces = new Faces(this.vertices, this.normals, this.textureCoords);
        this.faceMats = new FaceMaterials();
        this.modelDims = new ModelDimensions();

//        loadModel(gl, fileName);
//        centerScale();
//        draw2List(gl);
//
//        if (showDetails) {
//            reportOnModel();
//        }
    }

    @Override
    public void parseFile(String fileName) throws FileNotFoundException, IOException, LoaderException {
        boolean isLoaded = true;   // hope things will go okay
        String[] strs = fileName.split("/");
        modelDir = fileName.replace(strs[strs.length - 1], "");
        log.info(modelDir);

        log.info(format("Loading model from " + fileName + " ..."));
        BufferedReader br;
        br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));

        int lineNum = 0;
        String line;
        boolean isFirstCoord = true;
        boolean isFirstTC = true;
        int numFaces = 0;

        while (((line = br.readLine()) != null) && isLoaded) {
            lineNum++;
            if (line.length() > 0) {
                line = line.trim(); //Returns a copy of the string, with leading and trailing whitespace omitted

                if (line.startsWith("v ")) {   // vertex
                    isLoaded = this.addVert(line, isFirstCoord);
                    if (isFirstCoord) {
                        isFirstCoord = false;
                    }
                } else if (line.startsWith("vt")) {   // tex coord
                    isLoaded = this.addTexCoord(line, isFirstTC);
                    if (isFirstTC) {
                        isFirstTC = false;
                    }
                } else if (line.startsWith("vn")) // normal
                {
                    isLoaded = this.addNormal(line);
                } else if (line.startsWith("f ")) {  // face
                    isLoaded = this.faces.addFace(line);
                    numFaces++;
                } else if (line.startsWith("mtllib ")) // load material
                {
                    this.materials = new Materials(line.substring(7));
                } else if (line.startsWith("usemtl ")) // use material
                {
                    this.faceMats.addUse(numFaces, line.substring(7));
                } else if (line.charAt(0) == 'g') {  // group name
                    // not implemented
                    log.info("Group name not implemented");
                } else if (line.charAt(0) == 's') {  // smoothing group
                    // not implemented
                    log.info("Smoothing group not implemented");
                } else if (line.charAt(0) == '#') // comment line
                {
                    log.info(format("Ignoring line " + lineNum + " : " + line));
                } else {
                    log.info(format("Ignoring line " + lineNum + " : " + line));
                }
            }
        }
        br.close();
    }

    /**
     * Add vertex from line "v x y z" to vert ArrayList, and update the model
     * dimension's info.
     *
     * @param line
     * @param isFirstCoord
     * @return
     */
    private boolean addVert(String line, boolean isFirstCoord) {
        Vector3D vert = this.readVector3D(line);
        if (vert != null) {
            this.vertices.add(vert);
            if (isFirstCoord) {
                this.modelDims.set(vert);
            } else {
                this.modelDims.update(vert);
            }
            return true;
        }
        return false;
    }

    /**
     * The line starts with an WavefrontOBJ word ("v" or "vn"), followed by
     * three floats (x, y, z) separated by spaces
     *
     * @param line
     * @return
     */
    private Vector3D readVector3D(String line) {
        StringTokenizer tokens = new StringTokenizer(line, " ");
        tokens.nextToken();    // skip the WavefrontOBJ word

        try {
            float x = parseFloat(tokens.nextToken());
            float y = parseFloat(tokens.nextToken());
            float z = parseFloat(tokens.nextToken());

            return new Vector3D(x, y, z);
        } catch (NumberFormatException ex) {
            log.severe(ex.getMessage());
        }

        return null;   // means an error occurred
    }

    /**
     * Add the texture coordinate from the line "vt x y z" to the textureCoords
     * ArrayList. There may only be two tex coords on the line, which is
     * determined by looking at the first tex coord line.
     *
     * @param line
     * @param isFirstTC
     * @return
     */
    private boolean addTexCoord(String line, boolean isFirstTC) {
        if (isFirstTC) {
            this.hasTCs3D = this.checkTC3D(line);
            log.info(format("Using 3D tex coords: " + this.hasTCs3D));
        }

        Vector3D texCoord = this.readTCTuple(line);
        if (texCoord != null) {
            this.textureCoords.add(texCoord);
            return true;
        }
        return false;
    }

    /**
     * Check if the line has 4 tokens, which will be the "vt" token and 3 tex
     * coords in this case.
     *
     * @param line
     * @return
     */
    private boolean checkTC3D(String line) {
        String[] tokens = line.split("\\s+");
        return (tokens.length == 4);
    }

    /**
     * The line starts with a "vt" WavefrontOBJ word and two or three floats (x,
     * y, z) for the tex coords separated by spaces. If there are only two
     * coords, then the z-value is assigned a dummy value, DUMMY_Z_TC.
     *
     * @param line
     * @return
     */
    private Vector3D readTCTuple(String line) {
        StringTokenizer tokens = new StringTokenizer(line, " ");
        tokens.nextToken();    // skip "vt" WavefrontOBJ word

        try {
            float x = parseFloat(tokens.nextToken());
            float y = parseFloat(tokens.nextToken());

            float z = DUMMY_Z_TC;
            if (this.hasTCs3D) {
                z = parseFloat(tokens.nextToken());
            }

            return new Vector3D(x, y, z);
        } catch (NumberFormatException ex) {
            log.severe(ex.getMessage());
        }

        return null;   // means an error occurred
    }

    /**
     * Add normal from line "vn x y z" to the normals ArrayList.
     *
     * @param line
     * @return
     */
    private boolean addNormal(String line) {
        Vector3D normCoord = this.readVector3D(line);
        if (normCoord != null) {
            this.normals.add(normCoord);
            return true;
        }
        return false;
    }

    //TODO: copy for other models    
    /**
     * Position the model so it's center is at the origin, and scale it so its
     * longest dimension is no bigger than maxSize.
     */
    private void centerScale() {
        // get the model's center point
        Vector3D center = this.modelDims.getCenter();

        // calculate a scale factor
        float scaleFactor = 1.0f;
        float largest = this.modelDims.getLargest();
        // log.info(format("Largest dimension: " + largest);
        if (largest != 0.0f) {
            scaleFactor = (this.maxSize / largest);
        }
        log.info(format("Scale factor: " + scaleFactor));

        // modify the model's vertices
        Vector3D vert;
        float x, y, z;
        for (int i = 0; i < this.vertices.size(); i++) {
            vert = this.vertices.get(i);
            x = (float) ((vert.getX() - center.getX()) * scaleFactor);
            y = (float) ((vert.getY() - center.getY()) * scaleFactor);
            z = (float) ((vert.getZ() - center.getZ()) * scaleFactor);
            this.vertices.set(i, new Vector3D(x, y, z));
        }
    }

//    private void draw2List(GL2 gl) /* render the model to a display list, so it can be drawn quicker later */ {
//        modelDispList = gl.glGenLists(1);
//        gl.glNewList(modelDispList, GL2.GL_COMPILE);
//
//        gl.glPushMatrix();
//        // render the model face-by-face
//        String faceMat;
//        for (int i = 0; i < faces.getNumFaces(); i++) {
//            faceMat = faceMats.findMaterial(i);             // get material used by face i
//            if (faceMat != null) {
//                materials.renderWithMaterial(gl, faceMat);  // render using that material
//            }
//            faces.renderFace(gl, i);                        // draw face i
//        }
//        materials.switchOffTex(gl);
//        gl.glPopMatrix();
//
//        gl.glEndList();
//    } // end of draw2List()
//
//    public void draw(GL2 gl) {
//        gl.glCallList(modelDispList);
//    }
    //TODO: copy for other models
    /**
     * Show overall model properties.
     */
    public void reportOnModel() {
        log.info(format("No. of vertices: " + this.vertices.size()));
        log.info(format("No. of normal coords: " + this.normals.size()));
        log.info(format("No. of tex coords: " + this.textureCoords.size()));
        log.info(format("No. of faces: " + this.faces.getNumFaces()));

        this.modelDims.reportDimensions();
        // dimensions of model (before centering and scaling)

        if (this.materials != null) {
            this.materials.showMaterials();   // list defined materials
        }
        this.faceMats.showUsedMaterials();  // show what materials have been used by faces
    }

    //----------------  inner Class  --------------------

    /* This class calculates the 'edge' coordinates for the model
     along its three dimensions.

     The edge coords are used to calculate the model's:
     * width, height, depth
     * its largest dimension (width, height, or depth)
     * (x, y, z) center point
     */
    /**
     * Inner class ModelDimensions
     */
    public class ModelDimensions {
        // edge coordinates

        private float leftPt, rightPt;   // on x-axis
        private float topPt, bottomPt;   // on y-axis
        private float farPt, nearPt;     // on z-axis
        // for reporting
        private final DecimalFormat df = new DecimalFormat("0.##");  // 2 dp

        /**
         * Constructor.
         */
        public ModelDimensions() {
            this.leftPt = 0.0f;
            this.rightPt = 0.0f;
            this.topPt = 0.0f;
            this.bottomPt = 0.0f;
            this.farPt = 0.0f;
            this.nearPt = 0.0f;
        }

        /**
         * Initialize the model's edge coordinates.
         *
         * @param vert
         */
        public void set(Vector3D vert) {
            this.rightPt = (float) vert.getX();
            this.leftPt = (float) vert.getX();

            this.topPt = (float) vert.getY();
            this.bottomPt = (float) vert.getY();

            this.nearPt = (float) vert.getZ();
            this.farPt = (float) vert.getZ();
        }

        /**
         * Update the edge coordinates using vert.
         *
         * @param vert
         */
        public void update(Vector3D vert) {
            if (vert.getX() > this.rightPt) {
                this.rightPt = (float) vert.getX();
            }
            if (vert.getX() < this.leftPt) {
                this.leftPt = (float) vert.getX();
            }

            if (vert.getY() > this.topPt) {
                this.topPt = (float) vert.getY();
            }
            if (vert.getY() < this.bottomPt) {
                this.bottomPt = (float) vert.getY();
            }

            if (vert.getZ() > this.nearPt) {
                this.nearPt = (float) vert.getZ();
            }
            if (vert.getZ() < this.farPt) {
                this.farPt = (float) vert.getZ();
            }
        }

        // ------------- use the edge coordinates ----------------------------
        /**
         *
         * @return
         */
        public float getWidth() {
            return (this.rightPt - this.leftPt);
        }

        /**
         *
         * @return
         */
        public float getHeight() {
            return (this.topPt - this.bottomPt);
        }

        /**
         *
         * @return
         */
        public float getDepth() {
            return (this.nearPt - this.farPt);
        }

        /**
         *
         * @return
         */
        public float getLargest() {
            float height = this.getHeight();
            float depth = this.getDepth();

            float largest = this.getWidth();
            if (height > largest) {
                largest = height;
            }
            if (depth > largest) {
                largest = depth;
            }

            return largest;
        }

        /**
         *
         * @return
         */
        public Vector3D getCenter() {
            float xc = (this.rightPt + this.leftPt) / 2.0f;
            float yc = (this.topPt + this.bottomPt) / 2.0f;
            float zc = (this.nearPt + this.farPt) / 2.0f;
            return new Vector3D(xc, yc, zc);
        }

        /**
         *
         */
        public void reportDimensions() {
            Vector3D center = this.getCenter();

            log.info(format("x Coords: " + this.df.format(this.leftPt)
                    + " to " + this.df.format(this.rightPt)));
            log.info(format("  Mid: " + this.df.format(center.getX())
                    + "; Width: " + this.df.format(this.getWidth())));

            log.info(format("y Coords: " + this.df.format(this.bottomPt)
                    + " to " + this.df.format(this.topPt)));
            log.info(format("  Mid: " + this.df.format(center.getY())
                    + "; Height: " + this.df.format(this.getHeight())));

            log.info(format("z Coords: " + this.df.format(this.nearPt)
                    + " to " + this.df.format(this.farPt)));
            log.info(format("  Mid: " + this.df.format(center.getZ())
                    + "; Depth: " + this.df.format(this.getDepth())));
        }
    }

    //-------------------- inner Class ---------------------------
    /**
     * Faces stores the information for each face of a model.
     *
     *
     * A face is represented by three arrays of indicies for the vertices,
     * normals, and tex coords used in that face.
     *
     * facesVertIdxs, facesTexIdxs, and facesNormIdxs are ArrayLists of those
     * arrays; one entry for each face.
     *
     * renderFace() is supplied with a face index, looks up the associated
     * vertices, normals, and tex coords indicies arrays, and uses those arrays
     * to access the actual vertices, normals, and tex coords data for rendering
     * the face.
     *
     */
    public class Faces {

        private static final float DUMMY_Z_TC = -5.0f;

        /* indicies for vertices, tex coords, and normals used
         by each face */
        private ArrayList<int[]> facesVertIdxs;
        private ArrayList<int[]> facesTexIdxs;
        private ArrayList<int[]> facesNormIdxs;
        // references to the model's vertices, normals, and tex coords
        private final ArrayList<Vector3D> vertices;
        private final ArrayList<Vector3D> normals;
        private final ArrayList<Vector3D> textureCoords;

        /**
         *
         * @param vs
         * @param ns
         * @param ts
         */
        public Faces(ArrayList<Vector3D> vs, ArrayList<Vector3D> ns,
                ArrayList<Vector3D> ts) {
            this.vertices = vs;
            this.normals = ns;
            this.textureCoords = ts;

            this.facesVertIdxs = new ArrayList<>();
            this.facesTexIdxs = new ArrayList<>();
            this.facesNormIdxs = new ArrayList<>();
        }

        /**
         * Get this face's indicies from line "f v/vt/vn ..." with vt or vn
         * index values perhaps being absent.
         *
         * @param line
         * @return
         */
        public boolean addFace(String line) {
            try {
                line = line.substring(2);   // skip the "f "
                StringTokenizer st = new StringTokenizer(line, " ");
                int numTokens = st.countTokens();   // number of v/vt/vn tokens
                // create arrays to hold the v, vt, vn indicies
                int v[] = new int[numTokens];
                int vt[] = new int[numTokens];
                int vn[] = new int[numTokens];

                for (int i = 0; i < numTokens; i++) {
                    String faceToken = this.addFaceVals(st.nextToken());  // get a v/vt/vn token
                    // log.info(format(faceToken);

                    StringTokenizer st2 = new StringTokenizer(faceToken, "/");
                    int numSeps = st2.countTokens();  // how many '/'s are there in the token

                    v[i] = parseInt(st2.nextToken());
                    vt[i] = (numSeps > 1) ? parseInt(st2.nextToken()) : 0;
                    vn[i] = (numSeps > 2) ? parseInt(st2.nextToken()) : 0;
                    // add 0's if the vt or vn index values are missing;
                    // 0 is a good choice since real indicies start at 1
                }
                // store the indicies for this face
                this.facesVertIdxs.add(v);
                this.facesTexIdxs.add(vt);
                this.facesNormIdxs.add(vn);
            } catch (NumberFormatException ex) {
                log.severe(format("Incorrect face index!\n " + ex.getMessage()));
                return false;
            }
            return true;
        }

        /**
         * A face token (v/vt/vn) may be missing vt or vn index values; add 0's
         * in those cases.
         *
         * @param faceStr
         * @return
         */
        private String addFaceVals(String faceStr) {
            char chars[] = faceStr.toCharArray();
            StringBuilder sb = new StringBuilder();
            char prevCh = 'x';   // dummy value

            for (int k = 0; k < chars.length; k++) {
                if (chars[k] == '/' && prevCh == '/') // if no char between /'s
                {
                    sb.append('0');   // add a '0'
                }
                prevCh = chars[k];
                sb.append(prevCh);
            }
            return sb.toString();
        }

//        public void renderFace(GL2 gl, int i) /* Render the ith face by getting the vertex, normal, and tex
//         coord indicies for face i. Use those indicies to access the
//         actual vertex, normal, and tex coord data, and render the face.
//
//         Each face uses 3 array of indicies; one for the vertex
//         indicies, one for the normal indicies, and one for the tex
//         coord indicies.
//
//         If the model doesn't use normals or tex coords then the indicies
//         arrays will contain 0's.
//         */ {
//            if (i >= facesVertIdxs.size()) // i out of bounds?
//            {
//                return;
//            }
//
//            int[] vertIdxs = (int[]) (facesVertIdxs.get(i));
//            // get the vertex indicies for face i
//
//            int polytype;
//            if (vertIdxs.length == 3) {
//                polytype = GL2.GL_TRIANGLES;
//            } else if (vertIdxs.length == 4) {
//                polytype = GL2.GL_QUADS;
//            } else {
//                polytype = GL2.GL_POLYGON;
//            }
//
//
//            gl.glBegin(polytype);
//
//            // get the normal and tex coords indicies for face i
//            int[] normIdxs = (int[]) (facesNormIdxs.get(i));
//            int[] texIdxs = (int[]) (facesTexIdxs.get(i));
//
//            /* render the normals, tex coords, and vertices for face i
//             by accessing them using their indicies */
//            Vector3D vert, norm, texCoord;
//            for (int f = 0; f < vertIdxs.length; f++) {
//
//                if (normIdxs[f] != 0) {  // if there are normals, render them
//                    norm = (Vector3D) normals.get(normIdxs[f] - 1);
//                    gl.glNormal3f(norm.getX(), norm.getY(), norm.getZ());
//                }
//
//                if (texIdxs[f] != 0) {   // if there are tex coords, render them
//                    texCoord = (Vector3D) textureCoords.get(texIdxs[f] - 1);
//                    if (texCoord.getZ() == DUMMY_Z_TC) // using 2D tex coords
//                    {
//                        gl.glTexCoord2f(texCoord.getX(), texCoord.getY());
//                    } else // 3D tex coords
//                    {
//                        gl.glTexCoord3f(texCoord.getX(), texCoord.getY(), texCoord.getZ());
//                    }
//                }
//
//                vert = (Vector3D) vertices.get(vertIdxs[f] - 1);  // render the vertices
//                gl.glVertex3f(vert.getX(), vert.getY(), vert.getZ());
//            }
//
//            gl.glEnd();
//        }
        /**
         *
         * @return
         */
        public int getNumFaces() {
            return this.facesVertIdxs.size();
        }
    }

    //-------------------------  Inner Class  --------------------------------------
    /**
     * FaceMaterials stores the face indicies where a material is first used. At
     * render time, this information is utilized to change the rendering
     * material when a given face needs to be drawn.
     */
    public class FaceMaterials {

        /**
         * The face index (integer) where a material is first used for
         * reporting.
         */
        private final HashMap<Integer, String> faceMats;

        /**
         * How many times a material (string) is used
         */
        private final HashMap<String, Integer> matCount;

        /**
         * Constructor.
         */
        public FaceMaterials() {
            this.faceMats = new HashMap<>();
            this.matCount = new HashMap<>();
        }

        /**
         *
         * @param faceIdx
         * @param matName
         */
        public void addUse(int faceIdx, String matName) {
            // store the face index and the material it uses
            if (this.faceMats.containsKey(faceIdx)) // face index already present
            {
                log.info(format("Face index " + faceIdx
                        + " changed to use material " + matName));
            }
            this.faceMats.put(faceIdx, matName);

            // store how many times matName has been used by faces
            if (this.matCount.containsKey(matName)) {
                int i = this.matCount.get(matName) + 1;
                this.matCount.put(matName, i);
            } else {
                this.matCount.put(matName, 1);
            }
        }

        /**
         *
         * @param faceIdx
         * @return
         */
        public String findMaterial(int faceIdx) {
            return this.faceMats.get(faceIdx);
        }

        /**
         * List all the materials used by faces, and the number of faces that
         * have used them.
         */
        public void showUsedMaterials() {
            log.info(format("No. of materials used: " + this.matCount.size()));

            // build an iterator of material names
            Set<String> keys = this.matCount.keySet();
            Iterator<String> iter = keys.iterator();

            // cycle through the hashmap showing the count for each material
            String matName;
            int count;
            while (iter.hasNext()) {
                matName = iter.next();
                count = this.matCount.get(matName);

                log.info(format(matName + ": " + count));
            }
        }
    }

    //------------------ inner class Materials -----------------------------------
    /**
     * This class does two main tasks: it loads the material details from the
     * MTL file, storing them as Material objects in the materials ArrayList.
     *
     * it sets up a specified material's colours or textures to be used when
     * rendering -- see renderWithMaterial()
     */
    public class Materials {

        String MODEL_DIR = modelDir;
        ArrayList<Material> materials;
        // stores the Material objects built from the MTL file data
        // for storing the material currently being used for rendering
        String renderMatName = null;
        boolean usingTexture = false;

        /**
         * Constructor.
         *
         * @param mtlFnm
         */
        public Materials(String mtlFnm) {
            this.materials = new ArrayList<>();
            //MODEL_DIR = ;

            String mfnm = this.MODEL_DIR + mtlFnm;

            log.info(format("Loading material from " + mfnm));
            try (BufferedReader br = new BufferedReader(new FileReader(mfnm))) {
                this.readMaterials(br);
            } catch (FileNotFoundException ex) {
                log.severe(ex.getMessage());
            } catch (IOException ex) {
                log.severe(ex.getMessage());
            }
        }

        /**
         * Parse the MTL file line-by-line, building Material objects which are
         * collected in the materials ArrayList.
         *
         * @param br
         * @throws java.io.FileNotFoundException
         * @throws java.io.IOException
         */
        private void readMaterials(BufferedReader br) throws FileNotFoundException, IOException {
            String line;
            Material currMaterial = null;  // current material

            while (((line = br.readLine()) != null)) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }

                if (line.startsWith("newmtl ")) {  // new material
                    if (currMaterial != null) // save previous material
                    {
                        this.materials.add(currMaterial);
                    }

                    // start collecting info for new material
                    currMaterial = new Material(line.substring(7));
                    //} else if (line.startsWith("map_Kd ")) {  // texture filename
                    //    String fileName = MODEL_DIR + line.substring(7);
                    //if (currMaterial != null) {
                    //    currMaterial.loadTexture(fileName);
                    //}
                } else if (line.startsWith("Ka ")) // ambient colour
                {
                    if (currMaterial != null) {
                        currMaterial.setKa(this.readVector3D(line));
                    }
                } else if (line.startsWith("Kd ")) // diffuse colour
                {
                    if (currMaterial != null) {
                        currMaterial.setKd(this.readVector3D(line));
                    }
                } else if (line.startsWith("Ks ")) // specular colour
                {
                    if (currMaterial != null) {
                        currMaterial.setKs(this.readVector3D(line));
                    }
                } else if (line.startsWith("Ns ")) {  // shininess
                    float val = parseFloat(line.substring(3));
                    if (currMaterial != null) {
                        currMaterial.setNs(val);
                    }
                } else if (line.charAt(0) == 'd') {    // alpha
                    float val = parseFloat(line.substring(2));
                    if (currMaterial != null) {
                        currMaterial.setD(val);
                    }
                } else if (line.startsWith("illum ")) { // illumination model
                    // not implemented
                    log.info("Illumination model not implemented");
                } else if (line.charAt(0) == '#') // comment line
                {
                    log.info(format("Ignoring MTL line: {0}", line));
                    //log.info(format("Ignoring MTL line: " + line);
                } else {
                    log.info(format("Ignoring MTL line: {0}", line));
                    //log.info(format("Ignoring MTL line: " + line);
                }
            }
            this.materials.add(currMaterial);

            br.close();
        }

        /**
         * The line starts with an MTL word such as Ka, Kd, Ks, and the three
         * floats (x, y, z) separated by spaces
         *
         * @param line
         * @return
         */
        private float[] readVector3D(String line) {
            StringTokenizer tokens = new StringTokenizer(line, " ");
            tokens.nextToken();    // skip MTL word

            try {
                float x = parseFloat(tokens.nextToken());
                float y = parseFloat(tokens.nextToken());
                float z = parseFloat(tokens.nextToken());

                return new float[]{x, y, z};
            } catch (NumberFormatException ex) {
                log.severe(ex.getMessage());
            }

            return null;   // means an error occurred
        }

        /**
         * List all the Material objects
         */
        public void showMaterials() {
            log.info(format("No. of materials: " + this.materials.size()));
            Material m;
            for (Material material : this.materials) {
                m = material;
                m.showMaterial();
                // log.info(format();
            }
        }

//        // ----------------- using a material at render time -----------------
//        public void renderWithMaterial(GL2 gl, String faceMat) /* Render using the texture or colours associated with the
//         material, faceMat. But only change things if faceMat is
//         different from the current rendering material, whose name
//         is stored in renderMatName.
//         */ {
//            if (!faceMat.equals(renderMatName)) {   // is faceMat is a new material?
//                renderMatName = faceMat;
//                switchOffTex(gl);   // switch off any previous texturing
//
//                // set up new rendering material
//                Texture tex = getTexture(renderMatName);
//                if (tex != null) {   // use the material's texture
//                    // log.info(format("Using texture with " + renderMatName);
//                    switchOnTex(gl, tex);
//                } else // use the material's colours
//                {
//                    setMaterialColors(gl, renderMatName);
//                }
//            }
//        }
//        
//        public void switchOffTex(GL2 gl) // switch texturing off and put the lights on;
//        // also called from ObjModel.draw2List()
//        {
//            if (usingTexture) {
//                gl.glDisable(GL2.GL_TEXTURE_2D);
//                usingTexture = false;
//                gl.glEnable(GL2.GL_LIGHTING);
//            }
//        }
//
//        private void switchOnTex(GL2 gl, Texture tex) // switch the lights off, and texturing on
//        {
//            gl.glDisable(GL2.GL_LIGHTING);
//            gl.glEnable(GL.GL_TEXTURE_2D);
//            usingTexture = true;
//            tex.bind(gl);
//        }
//        private Texture getTexture(String matName) // return the texture associated with the material name
//        {
//            Material m;
//            for (int i = 0; i < materials.size(); i++) {
//                m = (Material) materials.get(i);
//                if (m.hasName(matName)) {
//                    return m.getTexture();
//                }
//            }
//            return null;
//        } // end of getTexture()
//        /**
//         * Start rendering using the colours specifies by the named material.
//         *
//         * @param gl
//         * @param matName
//         */
//        private void setMaterialColors(GL2 gl, String matName) {
//            Material m;
//            for (int i = 0; i < materials.size(); i++) {
//                m = (Material) materials.get(i);
//                if (m.hasName(matName)) {
//                    m.setMaterialColors(gl);
//                }
//            }
//        }
    }
}
