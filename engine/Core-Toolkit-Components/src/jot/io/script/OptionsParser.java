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
package jot.io.script;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import static java.lang.Boolean.valueOf;
import static java.lang.String.format;
import static java.lang.System.out;
import static java.lang.System.setOut;
import java.util.HashMap;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Class that implements a JavaScript file format options parser.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class OptionsParser {

    static final Logger log = getLogger("OptionsParser");

    public static final HashMap<String, Boolean> loadedOptions = new HashMap<>();

    static {
        log.setLevel(OFF);
    }

    /**
     * Parse game constants options values from a JavaScript file.
     *
     * @param file to parse path.
     * @param optionsId the name of the options hashMap, e.g., coreOptions.
     * @param options hashMap of {@literal <}optionKey,optionValue{@literal >}
     * pairs.
     * @return parsed game constants options.
     */
    public static HashMap<String, Boolean> parseFile(
            String file, String optionsId,
            HashMap<String, Boolean> options) {
        log.setLevel(INFO);

        ByteArrayOutputStream pipeOut = new ByteArrayOutputStream();

        //Store the current System.out
        PrintStream old_out = out;

        //Replace redirect output to our stream
        setOut(new PrintStream(pipeOut));

        //create a script engine manager
        ScriptEngineManager factory = new ScriptEngineManager();
        //create a JavaScript engine
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        //open the Java source file and evaluate the code.
        //File is expected to contain a Runnable implementation
        FileReader reader = null;
        try {
            reader = new FileReader(file);

            //evaluate JavaScript code from String
            engine.eval(reader);

            //Revert back to the old System.out
            setOut(old_out);

            //Write the output to a handy string
            String output = new String(pipeOut.toByteArray());

            for (String str : output.split("\n")) {
                String[] args = str.split(" = ");
                if (options.containsKey(args[0])) {
                    options.put(args[0], valueOf(args[1].trim()));
                    loadedOptions.put(args[0], valueOf(args[1].trim()));
                } else {
                    options.put(args[0], valueOf(args[1].trim()));
                    loadedOptions.put(args[0], valueOf(args[1].trim()));
                    log.info(format("%s %s unknown Constant.", optionsId, args[0]));
                }
            }
            log.info("");
        } catch (FileNotFoundException | ScriptException ex) {
            log.severe(ex.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                pipeOut.close();
            } catch (IOException ex) {
                log.severe(ex.getMessage());
            }
            //old_out.close();
        }

        log.setLevel(OFF);

        return options;
    }

    /**
     * Displays in terminal all the loaded options.
     */
    public static void showLoadedOptions() {
        log.setLevel(INFO);
        log.info("All loaded options:");

        loadedOptions.keySet().stream().forEach(
                key -> log.info(format(key + " = " + loadedOptions.get(key))));

        log.info("");
        log.setLevel(OFF);
    }

    /**
     * Displays in terminal all the loaded options with provided value.
     *
     * @param value of all the loaded options to displays in terminal.
     */
    public static void showLoadedOptions(boolean value) {
        log.setLevel(INFO);
        log.info(format("All loaded options with " + value + " value:"));

        loadedOptions.keySet().stream()
                .filter(key -> loadedOptions.get(key) == value)
                .forEach(key -> log.info(format(key + " = " + loadedOptions.get(key))));

        log.info("");
        log.setLevel(OFF);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private OptionsParser() {
    }
}
