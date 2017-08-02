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

import static com.jogamp.opengl.GL.GL_FRONT_AND_BACK;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.GL2GL3.GL_FILL;
import static com.jogamp.opengl.GL2GL3.GL_LINE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;
import java.util.ArrayList;
import static java.util.Collections.reverse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import static jot.io.data.format.UnzipUtility.unzipUtility;
import static jot.math.Distance.getDistance;
import jot.math.geometry.PolygonMesh;
import jot.math.geometry.TransformGroup;
import jot.math.geometry.bounding.AbstractBoundingVolume;
import static jot.math.graph.Converters.Graph2MST;
import static jot.math.graph.Converters.arrayGraph2HashMapGraph;
import jot.physics.Material;
import static jot.util.CoreOptions.coreOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Class that implements a HOG2 repository HOG2Map file format parser.
 * Repository is available at: http://www.movingai.com/benchmarks/index.html
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class HOG2Map implements GenericFormat {

    static final Logger log = getLogger("HOG2Map");

    private static ArrayList<ArrayList<Character>> Geometry;
    private static float formatScale;

    private static HashMap<Vector3D, ArrayList<Vector3D>> Graph;
    private static HashMap<Vector3D, ArrayList<Vector3D>> GraphMST;
    private static ArrayList<HashSet<Vector3D>> GraphDoorNodesSets;

    private Vector3D leftmostTopmostNode;
    private Vector3D leftmostBottommostNode;
    private Vector3D rightmostTopmostNode;
    private Vector3D rightmostBottommostNode;

    /**
     * The Height of the HOG2 map grid.
     */
    private static int HOG2MapHeight;

    /**
     * The Width of the HOG2 map grid.
     */
    private static int HOG2MapWidth;

    /**
     * The maximum distance between the non diagonal cells.
     */
    private static float nonDiagonalMaxDistance = 0;

    /**
     * The maximum distance between the diagonal cells.
     */
    private static float diagonalMaxDistance = 0;

    static {
        log.setLevel(OFF);
    }

    /**
     * Load the data, i.e., geometry and materials of a HOG2Map file.
     *
     * @param fileName the path/name of the HOG2Map file content to load.
     * @throws LoaderException if the file can't be read.
     */
    public static void load(String fileName) throws LoaderException {
        log.info("load");

        HOG2Map map = new HOG2Map();
        try {
            map.parseFile(fileName);
        } catch (IOException ex) {
            throw new LoaderException("Error reading '" + fileName + "'.", ex);
        }
        //return map;
    }

    /**
     * Load the data, i.e., type of tile terrain, color, of a HOG2Map file, and
     * encapsulate it into a transformGroup.
     *
     * @param filePath the path to the file to load.
     * @param fileName the name of the file content to load.
     * @param scale of the loaded format.
     * @param boundingVolumeType the type of BoundingVolume that must engulf
     * this model.
     * @return the loaded data of a HOG2Map file encapsulated into a
     * transformGroup.
     */
    //TODO: replace color with texture
    @SuppressWarnings({"unchecked"})
    public static TransformGroup loadFormat(
            String filePath,
            String fileName,
            float scale,
            AbstractBoundingVolume.BoundingVolumeType boundingVolumeType) {
        log.info("loadFormat");

        filePath = unzipUtility(filePath, fileName);

        //HOG2Map map;
        try {
            //map = load(fileName);            
            load(filePath + fileName);
        } catch (LoaderException ex) {
            log.severe(ex.getMessage());
            return null;
        }

        Level old_level = log.getLevel();

        scale = 1 / scale;
        formatScale = scale;
        TransformGroup model = new TransformGroup(fileName);
        HOG2MapMesh geometryMesh = new HOG2MapMesh();

        //if a .zip file with all maps exists
        if (filePath.toLowerCase().contains(".zip")) {
            ZipFile zf1 = null;
            ZipFile zf2 = null;
            ObjectInputStream ois1 = null;
            ObjectInputStream ois2 = null;
            FileOutputStream fos1 = null;
            FileOutputStream fos2 = null;
            ZipOutputStream zos1 = null;
            ZipOutputStream zos2 = null;
            ByteArrayOutputStream bos1 = null;
            ByteArrayOutputStream bos2 = null;
            ObjectOutput oo1 = null;
            ObjectOutput oo2 = null;

            try {
                String zipFilePath = filePath.replace(".zip/", ".zip");
                log.info(zipFilePath);

                zf1 = new ZipFile(zipFilePath);
                Enumeration<ZipEntry> entries1 = (Enumeration<ZipEntry>) zf1.entries();
                while (entries1.hasMoreElements()) {
                    ZipEntry ze = entries1.nextElement();
                    log.info(ze.getName());

                    if (scale == 1.0F) {
                        if (ze.getName().contains(
                                coreOptions.get("HOG2MapsWithScells")
                                ? fileName.replace(".map",
                                        coreOptions.get("useDiagonalEdges")
                                        ? ".GraphWithDiagonalEdgesWithScells"
                                        : ".GraphWithScells")
                                : fileName.replace(".map",
                                        coreOptions.get("useDiagonalEdges")
                                        ? ".GraphWithDiagonalEdges"
                                        : ".Graph"))) {
                            ois1 = new ObjectInputStream(zf1.getInputStream(ze));
                            break;
                        }
                    } else if (ze.getName().contains(
                            coreOptions.get("HOG2MapsWithScells")
                            ? fileName.replace(".map",
                                    coreOptions.get("useDiagonalEdges")
                                    ? ".GraphWithDiagonalEdgesScaledWithScells"
                                    : ".GraphScaledWithScells")
                            : fileName.replace(".map",
                                    coreOptions.get("useDiagonalEdges")
                                    ? ".GraphWithDiagonalEdgesScaled"
                                    : ".GraphScaled"))) {
                        ois1 = new ObjectInputStream(zf1.getInputStream(ze));
                        break;
                    }
                }

                zf2 = new ZipFile(zipFilePath);
                Enumeration<ZipEntry> entries2 = (Enumeration<ZipEntry>) zf2.entries();
                while (entries2.hasMoreElements()) {
                    ZipEntry ze = entries2.nextElement();
                    log.info(ze.getName());

                    if (scale == 1.0F) {
                        if (ze.getName().contains(
                                coreOptions.get("HOG2MapsWithScells")
                                ? fileName.replace(".map",
                                        coreOptions.get("useDiagonalEdges")
                                        ? ".GraphMSTwithDiagonalEdgesWithScells"
                                        : ".GraphMSTwithScells")
                                : fileName.replace(".map",
                                        coreOptions.get("useDiagonalEdges")
                                        ? ".GraphMSTwithDiagonalEdges"
                                        : ".GraphMST"))) {
                            ois2 = new ObjectInputStream(zf2.getInputStream(ze));
                            break;
                        }
                    } else if (ze.getName().contains(
                            coreOptions.get("HOG2MapsWithScells")
                            ? fileName.replace(".map",
                                    coreOptions.get("useDiagonalEdges")
                                    ? ".GraphMSTwithDiagonalEdgesScaledWithScells"
                                    : ".GraphMSTScaledWithScells")
                            : fileName.replace(".map",
                                    coreOptions.get("useDiagonalEdges")
                                    ? ".GraphMSTwithDiagonalEdgesScaled"
                                    : ".GraphMSTScaled"))) {
                        ois2 = new ObjectInputStream(zf2.getInputStream(ze));
                        break;
                    }
                }

                //if the file exists in the .zip file load hash map grap
                if (ois1 != null) {
                    if (coreOptions.get("HOG2MapsDebug")) {
                        log.setLevel(INFO);
                        log.info("Reading Graph from .zip file.");
                        log.setLevel(old_level);
                    }
                    Graph = (HashMap) ois1.readObject();
                } else { //otherwise write to file 
                    if (coreOptions.get("HOG2MapsDebug")) {
                        log.setLevel(INFO);
                        log.info("Writting Graph to .zip file.");
                        log.setLevel(old_level);
                    }
                    Graph = arrayGraph2HashMapGraph(geometryMesh.getArrayGraph());

                    File fTemp = new File(zipFilePath.replace(".zip", "Temp.zip"));
                    fos1 = new FileOutputStream(fTemp);
                    zos1 = new ZipOutputStream(fos1);

                    //Copy all previous entries to temporary file
                    entries1 = (Enumeration<ZipEntry>) zf1.entries();
                    while (entries1.hasMoreElements()) {
                        ZipEntry ze = entries1.nextElement();
                        log.info(ze.getName());

                        zos1.putNextEntry(new ZipEntry(ze.getName()));

                        try (BufferedInputStream bis = new BufferedInputStream(
                                zf1.getInputStream(ze))) {
                            while (bis.available() > 0) {
                                zos1.write(bis.read());
                            }

                            zos1.closeEntry();
                        }
                    }

                    //Write to new entry to temporary file
                    String[] spliterator = filePath.split("/");
                    String path = spliterator[spliterator.length - 1].replace(".zip", "/");
                    ZipEntry ze;
                    ze = scale == 1.0F
                            ? new ZipEntry(path + (coreOptions.get("HOG2MapsWithScells")
                                    ? fileName.replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphWithDiagonalEdgesWithScells"
                                            : ".GraphWithScells")
                                    : fileName.replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphWithDiagonalEdges"
                                            : ".Graph")))
                            : new ZipEntry(path + (coreOptions.get("HOG2MapsWithScells")
                                    ? fileName.replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphWithDiagonalEdgesScaledWithScells"
                                            : ".GraphScaledWithScells")
                                    : fileName.replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphWithDiagonalEdgesScaled"
                                            : ".GraphScaled")));
                    zos1.putNextEntry(ze);
                    bos1 = new ByteArrayOutputStream();
                    oo1 = new ObjectOutputStream(bos1);
                    oo1.writeObject(Graph);
                    zos1.write(bos1.toByteArray());
                    zos1.closeEntry();

                    zf1.close();
                    zos1.close();
                    fos1.close();

                    //Replace original zip file with temporary one
                    zf1 = new ZipFile(zipFilePath.replace(".zip", "Temp.zip"));
                    fos1 = new FileOutputStream(zipFilePath);
                    zos1 = new ZipOutputStream(fos1);

                    entries1 = (Enumeration<ZipEntry>) zf1.entries();
                    while (entries1.hasMoreElements()) {
                        ze = entries1.nextElement();
                        log.info(ze.getName());
                        zos1.putNextEntry(ze);

                        try (BufferedInputStream bis
                                = new BufferedInputStream(
                                        zf1.getInputStream(ze))) {
                            while (bis.available() > 0) {
                                zos1.write(bis.read());
                            }
                            zos1.closeEntry();
                        }
                    }

                    //Delete temporary file
                    fTemp.delete();
                }

                //if the file exists in the .zip file load hash map grap
                if (ois2 != null) {
                    if (coreOptions.get("HOG2MapsDebug")) {
                        log.setLevel(INFO);
                        log.info("Reading MST Graph from .zip file.");
                        log.setLevel(old_level);
                    }
                    GraphMST = (HashMap) ois2.readObject();
                } else { //otherwise write to file 
                    if (coreOptions.get("HOG2MapsDebug")) {
                        log.setLevel(INFO);
                        log.info("Writting MST Graph to .zip file.");
                        log.setLevel(old_level);
                    }
                    GraphMST = arrayGraph2HashMapGraph(Graph2MST(Graph));

                    File fTemp = new File(zipFilePath.replace(".zip", "Temp.zip"));
                    fos2 = new FileOutputStream(fTemp);
                    zos2 = new ZipOutputStream(fos2);

                    //Copy all previous entries to temporary file
                    entries2 = (Enumeration<ZipEntry>) zf2.entries();
                    while (entries2.hasMoreElements()) {
                        ZipEntry ze = entries2.nextElement();
                        log.info(ze.getName());

                        zos2.putNextEntry(new ZipEntry(ze.getName()));

                        try (BufferedInputStream bis = new BufferedInputStream(
                                zf2.getInputStream(ze))) {
                            while (bis.available() > 0) {
                                zos2.write(bis.read());
                            }

                            zos2.closeEntry();
                        }
                    }

                    //Write to new entry to temporary file
                    String[] spliterator = filePath.split("/");
                    String path = spliterator[spliterator.length - 1].replace(".zip", "/");
                    ZipEntry ze;
                    ze = scale == 1.0F
                            ? new ZipEntry(path + (coreOptions.get("HOG2MapsWithScells")
                                    ? fileName.replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphMSTwithDiagonalEdgesWithScells"
                                            : ".GraphMSTwithScells")
                                    : fileName.replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphMSTwithDiagonalEdges"
                                            : ".GraphMST")))
                            : new ZipEntry(path + (coreOptions.get("HOG2MapsWithScells")
                                    ? fileName.replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphMSTwithDiagonalEdgesScaledWithScells"
                                            : ".GraphMSTScaledWithScells")
                                    : fileName.replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphMSTwithDiagonalEdgesScaled"
                                            : ".GraphMSTScaled")));
                    zos2.putNextEntry(ze);
                    bos2 = new ByteArrayOutputStream();
                    oo2 = new ObjectOutputStream(bos2);
                    oo2.writeObject(GraphMST);
                    zos2.write(bos2.toByteArray());
                    zos2.closeEntry();

                    zf2.close();
                    zos2.close();
                    fos2.close();

                    //Replace original zip file with temporary one
                    zf2 = new ZipFile(zipFilePath.replace(".zip", "Temp.zip"));
                    fos2 = new FileOutputStream(zipFilePath);
                    zos2 = new ZipOutputStream(fos2);

                    entries2 = (Enumeration<ZipEntry>) zf2.entries();
                    while (entries2.hasMoreElements()) {
                        ze = entries2.nextElement();
                        log.info(ze.getName());
                        zos2.putNextEntry(ze);

                        try (BufferedInputStream bis
                                = new BufferedInputStream(
                                        zf2.getInputStream(ze))) {
                            while (bis.available() > 0) {
                                zos2.write(bis.read());
                            }
                            zos2.closeEntry();
                        }
                    }

                    //Delete temporary file
                    fTemp.delete();
                }
            } catch (IOException | ClassNotFoundException ex) {
                log.severe(ex.getMessage());
            } finally {
                try {
                    if (zf1 != null) {
                        zf1.close();
                    }
                    if (zf2 != null) {
                        zf2.close();
                    }
                    if (ois1 != null) {
                        ois1.close();
                    }
                    if (ois2 != null) {
                        ois2.close();
                    }
                    if (zos1 != null) {
                        zos1.close();
                    }
                    if (zos2 != null) {
                        zos2.close();
                    }
                    if (fos1 != null) {
                        fos1.close();
                    }
                    if (fos2 != null) {
                        fos2.close();
                    }
                    if (oo1 != null) {
                        oo1.close();
                    }
                    if (oo2 != null) {
                        oo2.close();
                    }
                    if (bos1 != null) {
                        bos1.close();
                    }
                    if (bos2 != null) {
                        bos2.close();
                    }
                } catch (IOException ex) {
                    log.severe(ex.getMessage());
                }
            }
        } else { //otherwise if a .zip file with all maps does not exist
            //if the file exists load hash map grap
            File f1;
            f1 = scale == 1.0F
                    ? (coreOptions.get("HOG2MapsWithScells")
                    ? new File((filePath + fileName)
                            .replace(".map", coreOptions.get("useDiagonalEdges")
                                    ? ".GraphWithDiagonalEdgesWithScells"
                                    : ".GraphWithScells"))
                    : new File((filePath + fileName)
                            .replace(".map", coreOptions.get("useDiagonalEdges")
                                    ? ".GraphWithDiagonalEdges"
                                    : ".Graph")))
                    : (coreOptions.get("HOG2MapsWithScells")
                    ? new File((filePath + fileName)
                            .replace(".map", coreOptions.get("useDiagonalEdges")
                                    ? ".GraphWithDiagonalEdgesScaledWithScells"
                                    : ".GraphScaledWithScells"))
                    : new File((filePath + fileName)
                            .replace(".map", coreOptions.get("useDiagonalEdges")
                                    ? ".GraphWithDiagonalEdgesScaled"
                                    : ".GraphScaled")));

            if (f1.exists()) {
                if (coreOptions.get("HOG2MapsDebug")) {
                    log.setLevel(INFO);
                    log.info("Reading Graph from file.");
                    log.setLevel(old_level);
                }

                FileInputStream fis = null;
                ObjectInputStream ois = null;
                try {
                    fis = scale == 1.0F
                            ? (coreOptions.get("HOG2MapsWithScells")
                            ? new FileInputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphWithDiagonalEdgesWithScells"
                                            : ".GraphWithScells"))
                            : new FileInputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphWithDiagonalEdges"
                                            : ".Graph")))
                            : (coreOptions.get("HOG2MapsWithScells")
                            ? new FileInputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphWithDiagonalEdgesScaledWithScells"
                                            : ".GraphScaledWithScells"))
                            : new FileInputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphWithDiagonalEdgesScaled"
                                            : ".GraphScaled")));
                    ois = new ObjectInputStream(fis);

                    Graph = (HashMap) ois.readObject();
                } catch (FileNotFoundException ex) {
                    log.severe(ex.getMessage());
                } catch (IOException | ClassNotFoundException ex) {
                    log.severe(ex.getMessage());
                } finally {
                    try {
                        if (ois != null) {
                            ois.close();
                        }
                        if (fis != null) {
                            fis.close();
                        }
                    } catch (IOException ex) {
                        log.severe(ex.getMessage());
                    }
                }
            } else { //otherwise write to file 
                if (coreOptions.get("HOG2MapsDebug")) {
                    log.setLevel(INFO);
                    log.info("Writting Graph to file.");
                    log.setLevel(old_level);
                }
                Graph = arrayGraph2HashMapGraph(geometryMesh.getArrayGraph());

                FileOutputStream fos = null;
                ObjectOutputStream oos = null;
                try {
                    fos = scale == 1.0F
                            ? (coreOptions.get("HOG2MapsWithScells")
                            ? new FileOutputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphWithDiagonalEdgesWithScells"
                                            : ".GraphWithScells"))
                            : new FileOutputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphWithDiagonalEdges"
                                            : ".Graph")))
                            : (coreOptions.get("HOG2MapsWithScells")
                            ? new FileOutputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphWithDiagonalEdgesScaledWithScells"
                                            : ".GraphScaledWithScells"))
                            : new FileOutputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphWithDiagonalEdgesScaled"
                                            : ".GraphScaled")));
                    oos = new ObjectOutputStream(fos);
                    oos.writeObject(Graph);
                } catch (FileNotFoundException ex) {
                    log.severe(ex.getMessage());
                } catch (IOException ex) {
                    log.severe(ex.getMessage());
                } finally {
                    try {
                        if (oos != null) {
                            oos.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException ex) {
                        log.severe(ex.getMessage());
                    }
                }
            }

            //if the file exists load hash map grap
            File f2;
            f2 = scale == 1.0F
                    ? (coreOptions.get("HOG2MapsWithScells")
                    ? new File((filePath + fileName)
                            .replace(".map", coreOptions.get("useDiagonalEdges")
                                    ? ".GraphMSTwithDiagonalEdgesWithScells"
                                    : ".GraphMSTwithScells"))
                    : new File((filePath + fileName)
                            .replace(".map", coreOptions.get("useDiagonalEdges")
                                    ? ".GraphMSTwithDiagonalEdges"
                                    : ".GraphMST")))
                    : (coreOptions.get("HOG2MapsWithScells")
                    ? new File((filePath + fileName)
                            .replace(".map", coreOptions.get("useDiagonalEdges")
                                    ? ".GraphMSTwithDiagonalEdgesScaledWithScells"
                                    : ".GraphMSTScaledWithScells"))
                    : new File((filePath + fileName)
                            .replace(".map", coreOptions.get("useDiagonalEdges")
                                    ? ".GraphMSTwithDiagonalEdgesScaled"
                                    : ".GraphMSTScaled")));

            if (f2.exists()) {
                if (coreOptions.get("HOG2MapsDebug")) {
                    log.setLevel(INFO);
                    log.info("Reading MST Graph from file.");
                    log.setLevel(old_level);
                }

                FileInputStream fis = null;
                ObjectInputStream ois = null;
                try {
                    fis = scale == 1.0F
                            ? (coreOptions.get("HOG2MapsWithScells")
                            ? new FileInputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphMSTwithDiagonalEdgesWithScells"
                                            : ".GraphMSTwithScells"))
                            : new FileInputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphMSTwithDiagonalEdges"
                                            : ".GraphMST")))
                            : (coreOptions.get("HOG2MapsWithScells")
                            ? new FileInputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphMSTwithDiagonalEdgesScaledWithScells"
                                            : ".GraphMSTScaledWithScells"))
                            : new FileInputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphMSTwithDiagonalEdgesScaled"
                                            : ".GraphMSTScaled")));
                    ois = new ObjectInputStream(fis);

                    GraphMST = (HashMap) ois.readObject();
                } catch (FileNotFoundException ex) {
                    log.severe(ex.getMessage());
                } catch (IOException | ClassNotFoundException ex) {
                    log.severe(ex.getMessage());
                } finally {
                    try {
                        if (ois != null) {
                            ois.close();
                        }
                        if (fis != null) {
                            fis.close();
                        }
                    } catch (IOException ex) {
                        log.severe(ex.getMessage());
                    }
                }
            } else { //otherwise write to file 
                if (coreOptions.get("HOG2MapsDebug")) {
                    log.setLevel(INFO);
                    log.info("Writting MST Graph to file.");
                    log.setLevel(old_level);
                }
                GraphMST = arrayGraph2HashMapGraph(Graph2MST(Graph));

                FileOutputStream fos = null;
                ObjectOutputStream oos = null;
                try {
                    fos = scale == 1.0F
                            ? (coreOptions.get("HOG2MapsWithScells")
                            ? new FileOutputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphMSTwithDiagonalEdgesWithScells"
                                            : ".GraphMSTwithScells"))
                            : new FileOutputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphMSTwithDiagonalEdges"
                                            : ".GraphMST")))
                            : (coreOptions.get("HOG2MapsWithScells")
                            ? new FileOutputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphMSTwithDiagonalEdgesScaledWithScells"
                                            : ".GraphMSTScaledWithScells"))
                            : new FileOutputStream((filePath + fileName)
                                    .replace(".map",
                                            coreOptions.get("useDiagonalEdges")
                                            ? ".GraphMSTwithDiagonalEdgesScaled"
                                            : ".GraphMSTScaled")));
                    oos = new ObjectOutputStream(fos);
                    oos.writeObject(GraphMST);
                } catch (FileNotFoundException ex) {
                    log.severe(ex.getMessage());
                } catch (IOException ex) {
                    log.severe(ex.getMessage());
                } finally {
                    try {
                        if (oos != null) {
                            oos.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException ex) {
                        log.severe(ex.getMessage());
                    }
                }
            }
        }
        //Generate the region door nodes hash set.
        GraphDoorNodesSets = geometryMesh.generateRegionDoorNodesSets();

        model.addChild(geometryMesh);

        TransformGroup modelWithTransformations
                = new TransformGroup(fileName + "WithTransformations");
        modelWithTransformations.addChild(model);
        model.setBoundingVolume(0, null);
        //modelWithTransformations.setBoundingVolume(0, new BoundingVolume(min, max, boundingVolumeType));
        modelWithTransformations.setScaling(new Vector3D(scale, scale, scale));

        return modelWithTransformations;
    }

    @Override
    public void parseFile(String fileName) throws IOException, LoaderException {
        log.info("parseFile");
        ZipFile zf = null;
        BufferedReader br = null;
        try {
            if (fileName.toLowerCase().contains(".zip")) {
                String[] fileNameArgs = fileName.split("/");
                String zipFilePath = fileName.replace("/" + fileNameArgs[fileNameArgs.length - 1], "");
                log.info(zipFilePath);
                zf = new ZipFile(zipFilePath);
                Enumeration<?> entries = zf.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry ze = (ZipEntry) entries.nextElement();
                    if (ze.getName().contains(fileNameArgs[fileNameArgs.length - 1])) {
                        log.info(ze.getName());
                        br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
                        break;
                    }
                }
                if (br == null) {
                    return;
                }
            } else {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            }
            String line;
            int lines = 0;
            HOG2MapHeight = MAX_VALUE;
            HOG2MapWidth = 0;
            Geometry = new ArrayList<>();
            while ((line = br.readLine()) != null && lines < HOG2MapHeight) {
                if (line.startsWith("type octile")) {
                    log.info(line);
                } else if (line.startsWith("height")) {
                    log.info(line.split(" ")[1]);
                    HOG2MapHeight = new Integer(line.split(" ")[1]);
                } else if (line.startsWith("width")) {
                    log.info(line.split(" ")[1]);
                    HOG2MapWidth = new Integer(line.split(" ")[1]);
                } else if (line.startsWith("map")) {
                    log.info(line);
                } else if (line.startsWith(".") || line.startsWith("D")
                        || line.startsWith("G") || line.startsWith("@")
                        || line.startsWith("O") || line.startsWith("T")
                        || line.startsWith("S") || line.startsWith("W")) {
                    lines++;
                    ArrayList<Character> values_line = new ArrayList<>();
                    char[] lineCharArray = line.toCharArray();
                    for (int i = 0; i < HOG2MapWidth; i++) {
                        values_line.add(i, lineCharArray[i]);
                        //log.info(Character.toString(lineCharArray[i]));
                    }
                    log.info(line);
                    //log.info("");
                    reverse(values_line);
                    Geometry.add(0, values_line);
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
            if (zf != null) {
                zf.close();
            }
        }

        //log.info(format("%d %d", Geometry.size(), HOG2MapHeight));
        //log.info(format("%d %d", Geometry.get(0).size(), HOG2MapWidth));
    }

    /**
     * Class that extends mesh and implements a HOG2Map mesh type.
     */
    //TODO: store Graphs in fileFormat 
    public static class HOG2MapMesh extends PolygonMesh {

        private Vector3D[] graph;
        private HashSet<Vector3D> GraphDoorNodes;
        private HashMap<Vector3D, Vector2D> GraphNodesGridCoords;
        private HashMap<Vector2D, Vector3D> GridCoordsGraphNodes;

        private Vector3D leftmostTopmostNode;
        private Vector3D leftmostBottommostNode;
        private Vector3D rightmostTopmostNode;
        private Vector3D rightmostBottommostNode;

        /**
         * Constructor
         */
        public HOG2MapMesh() {
            this.generateGraph();
            this.materials = new ArrayList<>();
            this.materials.add(new Material());
            this.materials.get(0).setRenderable(true);
        }

        /**
         * Get the barycentre of this format.
         *
         * @return the barycentre x, y, z values of this format.
         */
        public Vector3D getBarycentre() {
            int j = Geometry.size() - 1;
            int i = Geometry.get(j).size() - 1;

            Vector3D geometryMin = new Vector3D(.5f, 0, -.5f);
            Vector3D geometryMax = new Vector3D(i + .5f, 0, j - .5f);
            Vector3D barycentre = new Vector3D(
                    (geometryMax.getX() + geometryMin.getX()) / 2,
                    0.0f,
                    (geometryMax.getZ() + geometryMin.getZ()) / 2);
            return barycentre.scalarMultiply(formatScale);
        }

        /**
         * Get the color that corresponds to a specific type of terrain
         * identified by a given character.
         *
         * @param c a given character
         * @return a Vector3D with the color corresponding to a specific type of
         * terrain identified by a given character.
         */
        private Vector3D getColor(char c) {
            switch (c) {
                case 'D'://D - door
                case 'd':
                    return coreOptions.get("showGraphDoorNodes")
                            ? new Vector3D(1, 0, 1) : new Vector3D(1, 1, 1);
                case '.'://. - passable terrain                
                case 'G'://G - passable terrain
                case 'g':
                    return new Vector3D(1, 1, 1);
                case '@'://@ - out of bounds
                case 'O'://O - out of bounds
                case 'o':
                    return new Vector3D(0, 0, 0);
                case 'T'://T - trees (unpassable)
                case 't':
                    return new Vector3D(0, .8f, .2f);
                case 'S'://S - swamp (passable from regular terrain)
                case 's':
                    return new Vector3D(0, .5f, .5f);
                case 'W'://W - water (traversable, but not passable from terrain)
                case 'w':
                    return new Vector3D(0, 0, 1);
                default:
                    return null; //Deal with erroroneous character or a novel unsuported character.
            }
        }

        /**
         * Get the coordinates of a Vector3D given its char and color that
         * corresponds to a specific type of terrain identified by a given
         * character.
         *
         * @param c a given character
         * @return a Vector3D of coordinates, with the color corresponding to a
         * specific type of terrain identified by a given character.
         */
        private Vector3D getVector3D(float i, float j, char c) {
            switch (c) {
                case 'D'://D - door
                case 'd':
                case '.'://. - passable terrain
                case 'G'://G - passable terrain
                case 'g':
                    return new Vector3D(i, 0.0f, j);
                case 'S'://S - swamp (passable from regular terrain)
                case 's':
                    return coreOptions.get("HOG2MapsWithScells")
                            ? new Vector3D(i, -0.5f, j) : null;
                default:
                    return null; //Deal with erroroneous character or a novel unsuported character.
            }
        }

        /**
         * This method returns the available paths Graph.
         *
         * @return Graph, whose keys correspond to each individual node, and for
         * each node an list of connected neighbor nodes.
         */
        public HashMap<Vector3D, ArrayList<Vector3D>> getGraph() {
            return Graph;
        }

        /**
         * This method returns the available paths Graph.
         *
         * @return Graph, which corresponds to all lines formed by each pair of
         * nodes.
         */
        private Vector3D[] getArrayGraph() {
            return this.graph;
        }

        /**
         * This method returns the available paths Graph minimal spanning tree.
         *
         * @return Graph minimal spanning tree, whose keys correspond to each
         * individual node, and for each node an list of connected neighbor
         * nodes.
         */
        public HashMap<Vector3D, ArrayList<Vector3D>> getGraphMST() {
            return GraphMST;
        }

        /**
         * This method returns the available Graph door nodes.
         *
         * @return an HashSet with all Graph nodes that are region door nodes.
         */
        private HashSet<Vector3D> getGraphDoorNodes() {
            return this.GraphDoorNodes;
        }

        /**
         * This method returns the available Graph region door nodes.
         *
         * @return an ArrayList with the HashSets of Graph region door nodes.
         */
        public ArrayList<HashSet<Vector3D>> getGraphDoorNodesSets() {
            return GraphDoorNodesSets;
        }

        /**
         * This method returns an hash map where each Graph node position maps
         * to its 2D grid coordinates.
         *
         * @return hashMap, which contains for each Graph node position its 2D
         * grid coordinates.
         */
        public HashMap<Vector3D, Vector2D> getGraphNodesGridCoordinates() {
            return this.GraphNodesGridCoords;
        }

        /**
         * This method returns an hash map where each Graph node 2D grid
         * coordinates maps to its position.
         *
         * @return hashMap, which contains for each Graph node 2D grid
         * coordinates its position.
         */
        public HashMap<Vector2D, Vector3D> getGridCoordinatesGraphNodes() {
            return this.GridCoordsGraphNodes;
        }

        /**
         * Get the HOG2 map file height.
         *
         * @return the HOG2 map file height.
         */
        public int getHOG2MapHeight() {
            return HOG2MapHeight;
        }

        /**
         * Get the HOG2 map file width.
         *
         * @return the HOG2 map file width.
         */
        public int getHOG2MapWidth() {
            return HOG2MapWidth;
        }

        /**
         * Get the HOG2 map maximum diagonal distance between two nodes.
         *
         * @return the HOG2 map maximum diagonal distance between two nodes.
         */
        public float getDiagonalMaxDistance() {
            return diagonalMaxDistance;
        }

        /**
         * Get the HOG2 map maximum non diagonal distance between two nodes.
         *
         * @return the HOG2 map maximum non diagonal distance between two nodes.
         */
        public float getNonDiagonalMaxDistance() {
            return nonDiagonalMaxDistance;
        }

        /**
         * Get the leftmost bottommost graph node.
         *
         * @return the leftmost bottommost graph node.
         */
        public Vector3D getLeftmostBottommostNode() {
            return this.leftmostBottommostNode;
        }

        /**
         * Get the leftmost topmost graph node.
         *
         * @return the leftmost topmost graph node.
         */
        public Vector3D getLeftmostTopmostNode() {
            return this.leftmostTopmostNode;
        }

        /**
         * Get the rightmost bottommost graph node.
         *
         * @return the rightmost bottommost graph node.
         */
        public Vector3D getRightmostBottommostNode() {
            return this.rightmostBottommostNode;
        }

        /**
         * Get the rightmost topmost graph node.
         *
         * @return the rightmost topmost graph node.
         */
        public Vector3D getRightmostTopmostNode() {
            return this.rightmostTopmostNode;
        }

        /**
         * Generated the Graph for the Map Geometry.
         */
        private void generateGraph() {
            log.info("generateGraph");

            ArrayList<Vector3D> GraphList = new ArrayList<>();
            this.GraphDoorNodes = new HashSet<>();
            this.GraphNodesGridCoords = new HashMap<>();
            this.GridCoordsGraphNodes = new HashMap<>();

            for (int j = 0; j < Geometry.size(); j++) {
                log.info("");
                for (int i = 0; i < Geometry.get(j).size(); i++) {
                    Vector3D center = this.getVector3D(i + 0.5f, j - 0.5f, Geometry.get(j).get(i));
                    if (center != null) {
                        center = center.scalarMultiply(formatScale);
                        this.GraphNodesGridCoords.put(center, new Vector2D(i, j));
                        this.GridCoordsGraphNodes.put(new Vector2D(i, j), center);
                        if (this.max == null && this.isPassableTerrain(Geometry.get(j).get(i))) {
                            this.max = new Vector3D(center.toArray());
                        }
                        if (this.isDoor(Geometry.get(j).get(i))) {
                            this.GraphDoorNodes.add(center);
                        }
                        if (i != Geometry.get(j).size() - 1) {
                            Vector3D east = this.getVector3D(i + 1.5f, j - .5f, Geometry.get(j).get(i + 1));
                            if (east != null) {
                                east = east.scalarMultiply(formatScale);
                                GraphList.add(center);
                                GraphList.add(east);
                                log.info(format(center + " " + east));
                                float distance = (float) getDistance(center, east);
                                if (nonDiagonalMaxDistance < distance) {
                                    nonDiagonalMaxDistance = distance;
                                }
                            }

                            if (coreOptions.get("useDiagonalEdges") && j != 0) {
                                Vector3D southeast = this.getVector3D(i + 1.5f, j - 1.5f, Geometry.get(j - 1).get(i + 1));
                                if (southeast != null) {
                                    southeast = southeast.scalarMultiply(formatScale);
                                    GraphList.add(center);
                                    GraphList.add(southeast);
                                    log.info(format(center + " " + southeast));
                                    float distance = (float) getDistance(center, southeast);
                                    if (diagonalMaxDistance < distance) {
                                        diagonalMaxDistance = distance;
                                    }
                                }
                            }

                            if (coreOptions.get("useDiagonalEdges") && j != Geometry.size() - 1) {
                                Vector3D norhtheast = this.getVector3D(i + 1.5f, j + .5f, Geometry.get(j + 1).get(i + 1));
                                if (norhtheast != null) {
                                    norhtheast = norhtheast.scalarMultiply(formatScale);
                                    GraphList.add(center);
                                    GraphList.add(norhtheast);
                                    log.info(format(center + " " + norhtheast));
                                    float distance = (float) getDistance(center, norhtheast);
                                    if (diagonalMaxDistance < distance) {
                                        diagonalMaxDistance = distance;
                                    }
                                }
                            }
                        }

                        if (j != 0) {
                            Vector3D south = this.getVector3D(i + .5f, j - 1.5f, Geometry.get(j - 1).get(i));
                            if (south != null) {
                                south = south.scalarMultiply(formatScale);
                                GraphList.add(center);
                                GraphList.add(south);
                                log.info(format(center + " " + south));
                                float distance = (float) getDistance(center, south);
                                if (nonDiagonalMaxDistance < distance) {
                                    nonDiagonalMaxDistance = distance;
                                }
                            }
                        }
                    }
                }
            }
            //Set the minimum vertex, i.e., the lowest x, y, z values of all vertexes from this mesh.
            for (int j = Geometry.size() - 1; j >= 0; j--) {
                for (int i = Geometry.get(j).size() - 1; i >= 0; i--) {
                    Vector3D center = this.getVector3D(i + 0.5f, j - 0.5f, Geometry.get(j).get(i));
                    if (center != null) {
                        center = center.scalarMultiply(formatScale);
                        if (this.min == null && this.isPassableTerrain(Geometry.get(j).get(i))) {
                            this.min = new Vector3D(center.toArray());
                        }
                    }
                }
            }
            
            //Get the four nodes closest to each of the four corners of the square that inside contains the map.
            this.leftmostBottommostNode = new Vector3D(
                    Double.POSITIVE_INFINITY, 0, Double.POSITIVE_INFINITY);
            this.leftmostTopmostNode = new Vector3D(
                    Double.POSITIVE_INFINITY, 0, 0);            
            this.rightmostBottommostNode = new Vector3D(0, 0, Double.POSITIVE_INFINITY);
            this.rightmostTopmostNode = new Vector3D(
                    0, 0, 0);
            for (int j = 0; j < Geometry.size(); j++) {
                for (int i = 0; i < Geometry.get(j).size(); i++) {
                    Vector3D current = this.getVector3D(i + 0.5f, j - 0.5f, Geometry.get(j).get(i));
                    if (this.isPassableTerrain(Geometry.get(j).get(i))) {
                        if (this.leftmostBottommostNode.getX() >= current.getX()
                                && this.leftmostBottommostNode.getZ() >= current.getZ()) {
                            this.leftmostBottommostNode = new Vector3D(current.toArray());
                        }
                        if (this.leftmostTopmostNode.getX() >= current.getX()
                                && this.leftmostTopmostNode.getZ() <= current.getZ()) {
                            this.leftmostTopmostNode = new Vector3D(current.toArray());
                        }
                        if (this.rightmostBottommostNode.getX() <= current.getX()
                                && this.rightmostBottommostNode.getZ() >= current.getZ()) {
                            this.rightmostBottommostNode = new Vector3D(current.toArray());
                        }
                        if (this.rightmostTopmostNode.getX() <= current.getX()
                                && this.rightmostTopmostNode.getZ() <= current.getZ()) {
                            this.rightmostTopmostNode = new Vector3D(current.toArray());
                        }
                    }
                }
            }

            this.graph = GraphList.toArray(new Vector3D[GraphList.size()]);
        }

        private ArrayList<HashSet<Vector3D>> generateRegionDoorNodesSets() {
            //HashSet<Vector3D> regionDoorNodes = new HashSet<>();
            ArrayList<HashSet<Vector3D>> GraphDoorNodesSets = new ArrayList<>();
            this.getGraphDoorNodes().stream()
                    .forEach(doorNode -> {
                        boolean regionDoorNodesSet = false;
                        for (HashSet<Vector3D> GraphDoorNodesSet : GraphDoorNodesSets) {
                            if (GraphDoorNodesSet.contains(doorNode)) {
                                regionDoorNodesSet = true;
                            }
                        }
                        if (!regionDoorNodesSet) {
                            GraphDoorNodesSets.add(this.getRegionDoorNodesSet(doorNode));
                        }
                    });

            return GraphDoorNodesSets;
        }

        private HashSet<Vector3D> getRegionDoorNodesSet(Vector3D node) {
            HashSet<Vector3D> regionDoorNodesSet = new HashSet<>();
            this.regionDoorNodes(node, regionDoorNodesSet);

            return regionDoorNodesSet;
        }

        private void regionDoorNodes(Vector3D node, HashSet<Vector3D> regionDoorNodesSet) {
            regionDoorNodesSet.add(node);

            Graph.get(node).stream()
                    .filter(neighbor -> !regionDoorNodesSet.contains(neighbor))
                    .forEach(neighbor -> {
                        Vector2D graphNodeGridCoords = this.GraphNodesGridCoords.get(neighbor);
                        if (this.isDoor(Geometry.get((int) graphNodeGridCoords.getY())
                                .get((int) graphNodeGridCoords.getX()))) {
                            regionDoorNodesSet.add(neighbor);
                            this.regionDoorNodes(neighbor, regionDoorNodesSet);
                        }
                    });
        }

        /**
         * Test if a given Vector3D is a passable terrain.
         *
         * @param c a given character.
         * @return TRUE if c corresponds to a passable terrain, FALSE otherwise.
         */
        private boolean isPassableTerrain(char c) {
            switch (c) {
                case 'D'://D - door
                case 'd':
                case '.'://. - passable terrain
                case 'G'://G - passable terrain
                case 'g':
                    return true;
                //case 'W'://W - water (traversable, but not passable from terrain)
                //case 'w':
                case 'S'://S - swamp (passable from regular terrain)
                case 's':
                    return coreOptions.get("HOG2MapsWithScells");
                default:
                    return false; //Deal with erroroneous character or a novel unsuported character.
            }
        }

        /**
         * Test if a given Vector3D is a door.
         *
         * @param c a given character.
         * @return TRUE if c corresponds to a door, FALSE otherwise.
         */
        private boolean isDoor(char c) {
            switch (c) {
                case 'D'://D - door
                case 'd':
                    return true;
                default:
                    return false;
            }
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

                    gl.glPolygonMode(GL_FRONT_AND_BACK,
                            coreOptions.get("showWireframe")
                            ? GL_LINE : GL_FILL);
                    if (this.genList) {
                        Vector3D color;
                        gl.glDeleteLists(this.listMesh, 1);
                        this.listMesh = gl.glGenLists(1);
                        gl.glNewList(this.listMesh, GL_COMPILE);
                        {
                            gl.glBegin(GL_TRIANGLES);
                            {
                                for (int j = 0; j < Geometry.size(); j++) {
                                    log.info("");
                                    for (int i = 0; i < Geometry.get(j).size(); i++) {
                                        log.info(Character.toString(Geometry.get(j).get(i)));
                                        color = this.getColor(Geometry.get(j).get(i));
                                        gl.glColor3d(color.getX(), color.getY(), color.getZ());

                                        //Render doors with some height
                                        if (coreOptions.get("showGraphDoorNodes")
                                                && coreOptions.get("showGraphDoorNodesElevated")) {
                                            if (color.equals(new Vector3D(1, 0, 1))) {
                                                //TRIANGLE 1
                                                gl.glVertex3f(i, 1.0f, j);
                                                gl.glVertex3f(i, 1.0f, j - 1);
                                                gl.glVertex3f(i + 1, 1.0f, j - 1);

                                                //TRIANGLE 2
                                                gl.glVertex3f(i, 1.0f, j);
                                                gl.glVertex3f(i + 1, 1.0f, j - 1);
                                                gl.glVertex3f(i + 1, 1.0f, j);

                                                gl.glColor3f(1.0f, 1.0f, 1.0f);
                                            }
                                        }

                                        //TRIANGLE 1
                                        gl.glVertex3d(i, 0.0d, j);
                                        gl.glVertex3d(i, 0.0d, j - 1);
                                        gl.glVertex3d(i + 1, 0.0d, j - 1);

                                        //TRIANGLE 2
                                        gl.glVertex3d(i, 0.0d, j);
                                        gl.glVertex3d(i + 1, 0.0d, j - 1);
                                        gl.glVertex3d(i + 1, 0.0d, j);

                                    }
                                }
                                log.info("");
                            }
                            gl.glEnd();
                        }
                        gl.glEndList();
                        this.genList = false;
                    }

                    gl.glDisable(GL_LIGHTING);
                    gl.glCallList(this.listMesh);
                    gl.glEnable(GL_LIGHTING);
                }
                gl.glPopMatrix();
            }
        }

//        /**
//         * {@inheritDoc}
//         */
//        @Override
//        public void dispose(GL2 gl) {
//            if (gl != null) {
//                log.info("Dispose listMesh.");
//                gl.glDeleteLists(listMesh, 1);
//            }
//        }
    }
}
