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

import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * Class that implements a generic model loader exceptions.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class LoaderException extends Exception {

    static final Logger log = getLogger("LoaderException");

    static {
        log.setLevel(OFF);
    }

    /**
     * Constructor, calls Exception constructor.
     */
    public LoaderException() {
        super();
    }

    /**
     * Constructor, calls Exception(String message) constructor.
     *
     * @param message a string message to show in the exception output.
     */
    public LoaderException(String message) {
        super(message);
    }

    /**
     * /**
     * Constructor, calls Exception(String message, Throwable cause)
     * constructor.
     *
     * @param message a string message to show in the exception output.
     * @param cause the exception cause .
     */
    public LoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor, calls Exception(Throwable cause) constructor.
     *
     * @param cause the exception cause .
     */
    public LoaderException(Throwable cause) {
        super(cause);
    }
}
