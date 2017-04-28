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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.String.format;
import java.util.Enumeration;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Class that implements a zip archive extractor.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class UnzipUtility {

    static final Logger log = getLogger("UnzipUtility");

    /**
     * Size of the buffer to read/write data
     */
    static final int BUFFER_SIZE = 4_096;

    static {
        log.setLevel(OFF);
    }

    /**
     * Utility to deal with a file inside a .zip archive.
     *
     * @param filePath the path to the file to load.
     * @param fileName the name of the file content to load.
     * @return the path to a file depending if it is inside a .zip archive or
     * not.
     */
    public static String unzipUtility(String filePath, String fileName) {
        if (filePath.toLowerCase().contains(".zip")) {
            filePath += "/";
            if (GenericFormat.class
                    .getResource(filePath.replace("assets", "")) != null) { //Using IDE
                filePath = GenericFormat.class.getResource(filePath.replace("assets", "")).getPath();
            }
        } else if (GenericFormat.class
                .getResource(filePath.replace("assets", "") + fileName) != null) { //Using IDE
            filePath = GenericFormat.class.getResource(filePath.replace("assets", "")).getPath();
        }
        return filePath;
    }

    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified
     * by destDirectory (will be created if does not exist).
     *
     * @param zipFilePath extract from.
     * @param destDirectory to extract zip file to.
     */
    public void unzip(String zipFilePath, String destDirectory) {
        try {
            try (ZipFile zipFile = new ZipFile(zipFilePath)) {
                Enumeration<?> enu = zipFile.entries();
                while (enu.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) enu.nextElement();

                    String name = zipEntry.getName();
                    long size = zipEntry.getSize();
                    long compressedSize = zipEntry.getCompressedSize();
                    log.info(format("name: %-20s | size: %6d | compressed size: %6d\n",
                            name, size, compressedSize));

                    File file = new File(destDirectory + name);
                    if (name.endsWith("/")) {
                        file.mkdirs();
                        continue;
                    }

                    File parent = file.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }

                    FileOutputStream fos;
                    try (InputStream is = zipFile.getInputStream(zipEntry)) {
                        fos = new FileOutputStream(file);
                        byte[] bytes = new byte[1_024];
                        int length;
                        while ((length = is.read(bytes)) >= 0) {
                            fos.write(bytes, 0, length);
                        }
                    }
                    fos.close();

                }
            }
        } catch (IOException ex) {
            log.severe(ex.getMessage());
        }
    }
}
