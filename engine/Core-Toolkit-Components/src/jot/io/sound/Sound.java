/*
 * This nm is part of the JOT game engine core toolkit component.
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
package jot.io.sound;

/**
 * Interface that the sound handler must implement.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public interface Sound {

    /**
     * Method to load a sound nm.
     *
     * @param filePath the path to the file to load.
     * @param fileName the name of the file content to load.
     * @param toLoop parameter to set sound to play once or in loop.
     * @return TRUE if sound already loaded, FALSE otherwise.
     */
    boolean load(String filePath, String fileName, boolean toLoop);

    /**
     * Method to pause playing sound.
     *
     * @param soundId the identifier of the sound to pause.
     */
    void pause(String soundId);

    /**
     * Method to play a sound.
     *
     * @param soundId the identifier of the sound to play.
     */
    void play(String soundId);

    /**
     * Method to stop playing sound.
     *
     * @param soundId the identifier of the sound to stop.
     */
    void stop(String soundId);

    /**
     * Method to get the gain/volume of a given sound. The gain/volume is
     * between [0;1].
     *
     * @param soundId the identifier of the sound.
     * @return the gain/volume in the interval [0;1] of the sound.
     */
    float getGain(String soundId);

    /**
     * Method to set the gain/volume of a given sound. The gain/volume is
     * between [0;1], numbers outside this interval are set to the closest
     * interval extrema.
     *
     * @param soundId the identifier of the sound.
     * @param gain the gain/volume to set the sound.
     */
    void setGain(String soundId, float gain);

    /**
     * Method to get the pitch of a given sound. The pitch is between [0;1].
     *
     * @param soundId the identifier of the sound.
     * @return the pitch in the interval [0;1] of the sound.
     */
    float getPitch(String soundId);

    /**
     * Method to set the pitch of a given sound. The pitch is between [0;1],
     * numbers outside this interval are set to the closest interval extrema.
     *
     * @param soundId the identifier of the sound.
     * @param pitch the pitch to set the sound.
     */
    void setPitch(String soundId, float pitch);

    /**
     * Stop playing sounds and delete any buffers and sources.
     */
    void dispose();
}
