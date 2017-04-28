/*
 * This fileName is part of the JOT game engine core toolkit component.
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
package jot.io.sound.handlers;

import com.jogamp.openal.AL;
import static com.jogamp.openal.ALConstants.AL_BUFFER;
import static com.jogamp.openal.ALConstants.AL_FALSE;
import static com.jogamp.openal.ALConstants.AL_GAIN;
import static com.jogamp.openal.ALConstants.AL_LOOPING;
import static com.jogamp.openal.ALConstants.AL_NO_ERROR;
import static com.jogamp.openal.ALConstants.AL_ORIENTATION;
import static com.jogamp.openal.ALConstants.AL_PITCH;
import static com.jogamp.openal.ALConstants.AL_POSITION;
import static com.jogamp.openal.ALConstants.AL_TRUE;
import static com.jogamp.openal.ALConstants.AL_VELOCITY;
import com.jogamp.openal.ALException;
import static com.jogamp.openal.ALFactory.getAL;
import static com.jogamp.openal.util.ALut.alutExit;
import static com.jogamp.openal.util.ALut.alutInit;
import static com.jogamp.openal.util.ALut.alutLoadWAVFile;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static java.lang.String.format;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.io.data.format.UnzipUtility.unzipUtility;
import jot.io.sound.Sound;
import static jot.util.CoreOptions.coreOptions;

/**
 * Class that implements the JOAL sound handler.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Sound3D implements Sound {

    static final Logger log = getLogger("WAV");

    static {
        log.setLevel(OFF);
    }

    // global stores for the sounds
    private final HashMap<String, int[]> buffersMap; // (name, buffer) pairs
    private final HashMap<String, int[]> sourcesMap; // (name, source) pairs
    private final HashMap<String, Float> gainsMap; // (name, gain) pairs
    private final HashMap<String, Float> pitchesMap; // (name, pitch) pairs

    // global listener info
    private float xLis, yLis, zLis; // current position
    private float[] oriLis; // orientation
    private int angleLis = 0; // anti-clockwise rotation anyway from -z axis    

    private AL al; // to access Sound3D

    /**
     * Constructor.
     */
    public Sound3D() {
        if (coreOptions.get("useSoundDebug")) {
            log.setLevel(INFO);
        }

        this.buffersMap = new HashMap<>();
        this.sourcesMap = new HashMap<>();
        this.gainsMap = new HashMap<>();
        this.pitchesMap = new HashMap<>();
        this.initOpenAL();
        this.initListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean load(String filePath, String fileName, boolean toLoop) {
        filePath = unzipUtility(filePath, fileName);

        /**
         * A sound is loaded by first being converted into a JOAL buffer. Then
         * the buffer is linked to a JOAL source, and the source is placed at a
         * particular location in the scene. The buffer and source references
         * are stored in global HashMaps, using the sound’s name as the key.
         */
        if (this.sourcesMap.get(fileName) != null) {
            log.info(format(fileName + " already loaded"));
            return true;
        }
        int[] buffer = this.initBuffer(fileName, filePath);
        if (buffer == null) {
            return false;
        }
        int[] source = this.initSource(fileName, buffer, toLoop);

        if (source == null) {
            this.al.alDeleteBuffers(1, buffer, 0);
            // no need for the buffer anymore
            return false;
        }
        if (toLoop) {
            log.info(format("Looping source created for " + fileName));
        } else {
            log.info(format("Source created for " + fileName));
        }
        this.buffersMap.put(fileName, buffer);
        this.sourcesMap.put(fileName, source);
        this.gainsMap.put(fileName, 1.0f);
        this.pitchesMap.put(fileName, 1.0f);

        return true;
    }

    /**
     * A variant of load(), which employs setPos()
     *
     * @param filePath to the sound to load.
     * @param fileName of the sound to load.
     * @param x Cartesian coordinate of the sound source.
     * @param y Cartesian coordinate of the sound source.
     * @param z Cartesian coordinate of the sound source.
     * @param toLoop TRUE if the sound when finished is to be restarted, FALSE
     * otherwise.
     * @return TRUE if a sound emitter was successfully created in the provided
     * position in Cartesian coordinates, FALSE otherwise.
     */
    public boolean load(String filePath, String fileName, float x, float y, float z, boolean toLoop) {
        if (this.load(filePath, fileName, toLoop)) {
            return this.setPos(fileName, x, y, z);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause(String soundId) {
        int[] source = this.sourcesMap.get(soundId);
        if (source == null) {
            log.info(format("No source found for " + soundId));
        } else {
            log.info(format("Pausing " + soundId));
            this.al.alSourcePause(source[0]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void play(String soundId) {
        int[] source = this.sourcesMap.get(soundId);
        if (source == null) {
            log.info(format("No source found for " + soundId));
        } else {
            log.info(format("Playing " + soundId));
            this.al.alSourcePlay(source[0]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(String soundId) {
        int[] source = this.sourcesMap.get(soundId);
        if (source == null) {
            log.info(format("No source found for " + soundId));
        } else {
            log.info(format("Stopping " + soundId));
            this.al.alSourceStop(source[0]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getGain(String soundId) {
        if (this.gainsMap.containsKey(soundId)) {
            return this.gainsMap.get(soundId);
        }
        return -1.0f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGain(String soundId, float gain) {
        if (gain < 0) {
            gain = 0.0f;
        }
        if (gain > 1) {
            gain = 1.0f;
        }
        if (this.gainsMap.containsKey(soundId)) {
            this.gainsMap.put(soundId, gain);
        }

        int[] source = this.sourcesMap.get(soundId);
        if (source == null) {
            log.info(format("No source found for " + soundId));
        } else {
            log.info(format("Setting volume to " + soundId));
            this.al.alSourcef(source[0], AL_GAIN, gain);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getPitch(String soundId) {
        if (this.pitchesMap.containsKey(soundId)) {
            return this.pitchesMap.get(soundId);
        }
        return -1.0f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPitch(String soundId, float pitch) {
        if (pitch < 0) {
            pitch = 0.0f;
        }
        if (pitch > 1) {
            pitch = 1.0f;
        }
        if (this.pitchesMap.containsKey(soundId)) {
            this.pitchesMap.put(soundId, pitch);
        }

        int[] source = this.sourcesMap.get(soundId);
        if (source == null) {
            log.info(format("No source found for " + soundId));
        } else {
            log.info(format("Setting volume to " + soundId));
            this.al.alSourcef(source[0], AL_PITCH, pitch);
        }
    }

    /**
     * Move the fileName sound to (x,y,z).
     *
     * @param fileName of the file to move.
     * @param x Cartesian coordinate.
     * @param y Cartesian coordinate.
     * @param z Cartesian coordinate.
     * @return TRUE if sound was moved, FALSE otherwise.
     */
    public boolean setPos(String fileName, float x, float y, float z) {
        int[] source = this.sourcesMap.get(fileName);
        if (source == null) {
            log.info(format("No source found for " + fileName));
            return false;
        }
        this.al.alSource3f(source[0], AL_POSITION, x, y, z);
        return true;
    }

    // --------------------- listener methods ---------------------
    /**
     * Get X position of the listener.
     *
     * @return X position of the listener.
     */
    public float getX() {
        return this.xLis;
    }

    /**
     * Get Z position of the listener.
     *
     * @return Z position of the listener.
     */
    public float getZ() {
        return this.zLis;
    }

    /**
     * Get the angle of the listener.
     *
     * @return the angle of the listener.
     */
    public int getAngle() {
        return this.angleLis;
    }

    /**
     * Set the position of the listener at (xNew,zNew).
     *
     * @param xNew Cartesian coordinate.
     * @param zNew Cartesian coordinate.
     */
    public void setListenerPos(float xNew, float zNew) {
        float xOffset = xNew - this.xLis;
        float zOffset = zNew - this.zLis;
        this.xLis = xNew;
        this.zLis = zNew;
        this.al.alListener3f(AL_POSITION, this.xLis, this.yLis, this.zLis);
        /* keep the listener facing the same direction by
         moving the "look at" point by the (x,z) offset */
        this.oriLis[0] += xOffset;
        this.oriLis[2] += zOffset;
        // no change needed to y-coord in oriLis[1]
        this.al.alListenerfv(AL_ORIENTATION, this.oriLis, 0);
    }

    /**
     * Set the listener orientation to be angle degrees in the anticlockwise
     * direction around the y-axis.
     *
     * @param angle in degrees.
     */
    public void setListenerOri(int angle) {
        this.angleLis = angle;
        double angleRadians = toRadians(this.angleLis);
        float xLen = -1.0f * (float) sin(angleRadians);
        float zLen = -1.0f * (float) cos(angleRadians);
        /* face in the (xLen, zLen) direction by adding the
         values to the listener position */
        this.oriLis[0] = this.xLis + xLen;
        this.oriLis[2] = this.zLis + zLen;
        this.al.alListenerfv(AL_ORIENTATION, this.oriLis, 0);
    }

    /**
     * Move the listener by a (x,z) step.
     *
     * @param xStep Cartesian coordinate.
     * @param zStep Cartesian coordinate.
     */
    public void moveListener(float xStep, float zStep) {
        float x = this.xLis + xStep;
        float z = this.zLis + zStep;
        this.setListenerPos(x, z);
    }

    /**
     * Turn the listener anticlockwise by the amount stored in degrees.
     *
     * @param degrees to turn the listener anticlockwise.
     */
    public void turnListener(int degrees) {
        this.setListenerOri(this.angleLis + degrees);
    }

    // -------------------------- finish --------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        Set<String> keys = this.sourcesMap.keySet();
        Iterator<String> iter = keys.iterator();
        String fileName;
        int[] buffer, source;
        while (iter.hasNext()) {
            fileName = iter.next();
            source = this.sourcesMap.get(fileName);
            log.info(format("Stopping " + fileName));
            this.al.alSourceStop(source[0]);
            this.al.alDeleteSources(1, source, 0);
            buffer = this.buffersMap.get(fileName);
            this.al.alDeleteBuffers(1, buffer, 0);
        }
        alutExit();
    }

    /**
     * The Sound3D fileName is loaded into several data arrays, then the buffer
     * is initialized with those arrays.
     *
     * @param fileName
     * @return
     */
    private int[] initBuffer(String fileName, String filePath) {
        // create arrays for holding various Sound3D fileName info
        int[] format = new int[1];
        ByteBuffer[] data = new ByteBuffer[1];
        int[] size = new int[1];
        int[] freq = new int[1];
        int[] loop = new int[1];
        // load Sound3D fileName into the data arrays
        String ffileName = filePath + fileName;
        try {
            alutLoadWAVFile(ffileName, format, data, size, freq, loop);
        } catch (ALException ex) {
            log.info(format("Error loading WAV fileName: " + fileName + "\n"));
            throw new ALException(ex.getMessage());
        }
        log.info(format("Sound size = " + size[0]));
        log.info(format("Sound freq = " + freq[0]));
        // create an empty buffer to hold the sound data
        int[] buffer = new int[1];
        this.al.alGenBuffers(1, buffer, 0);
        if (this.al.alGetError() != AL_NO_ERROR) {
            log.info(format("Could not create a buffer for " + fileName));
            return null;
        }
        // store data in the buffer
        this.al.alBufferData(buffer[0], format[0], data[0], size[0], freq[0]);
        // alutUnloadWAV(format[0], data[0], size[0], freq[0]);
        // not in API anymore
        return buffer;
    }

    /**
     * Place the listener at the origin, looking along the negative z-axis.
     */
    private void initListener() // position and orientate the listener
    {
        this.xLis = 0.0f;
        this.yLis = 0.0f;
        this.zLis = 0.0f;
        this.al.alListener3f(AL_POSITION, this.xLis, this.yLis, this.zLis);
        // position the listener at the origin
        this.al.alListener3i(AL_VELOCITY, 0, 0, 0); // no velocity
        this.oriLis = new float[]{this.xLis, this.yLis, this.zLis - 1.0f, 0.0f, 1.0f, 0.0f};
        /* the first 3 elements are the "look at" point,
         the second 3 are the "up direction" */
        this.al.alListenerfv(AL_ORIENTATION, this.oriLis, 0);
    }

    /**
     * Set up a link to Sound3D via the ALut library (so named to remind OpenGL
     * programmers of GLUT)
     */
    private void initOpenAL() {
        try {
            alutInit(); // creates an Sound3D context
            this.al = getAL(); // used to access Sound3D
            this.al.alGetError(); // clears any error bits
            //log.info(format("Sound3D version: " + Version.getVersion()));
        } catch (ALException ex) {
            throw new ALException(ex.getMessage());
        }
    }

    /**
     * The source is positioned at (0, 0, 0), and linked to the buffer that was
     * just created. The source may play repeatedly, depending on the toLoop
     * argument.
     *
     * @param fileName
     * @param buf
     * @param toLoop
     * @return
     */
    private int[] initSource(String fileName, int[] buf, boolean toLoop) {
        // create a source (a point in space that emits a sound)
        int[] source = new int[1];
        this.al.alGenSources(1, source, 0);
        if (this.al.alGetError() != AL_NO_ERROR) {
            log.info(format("Error creating source for " + fileName));
            return null;
        }
        // configure the source
        this.al.alSourcei(source[0], AL_BUFFER, buf[0]); // bind buffer
        this.al.alSourcef(source[0], AL_PITCH, 1.0f);
        this.al.alSourcef(source[0], AL_GAIN, 1.0f);
        this.al.alSource3f(source[0], AL_POSITION, 0.0f, 0.0f, 0.0f);
        // position the source at the origin
        this.al.alSource3i(source[0], AL_VELOCITY, 0, 0, 0); // no velocity
        if (toLoop) {
            this.al.alSourcei(source[0], AL_LOOPING, AL_TRUE); // looping
        } else {
            this.al.alSourcei(source[0], AL_LOOPING, AL_FALSE); //play once
        }
        if (this.al.alGetError() != AL_NO_ERROR) {
            log.info(format("Error configuring source for " + fileName));
            return null;
        }
        return source;
    }
}
