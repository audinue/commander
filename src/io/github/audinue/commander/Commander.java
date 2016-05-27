/*
 * The MIT License
 *
 * Copyright 2016 Audi Nugraha &lt;audinue@gmail.com&gt;.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.audinue.commander;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A command line front controller.
 *
 * @author Audi Nugraha &lt;audinue@gmail.com&gt;
 */
public class Commander {

    private CommandModel commandModel;
    private final HashMap<String, OptionModel> optionModels = new LinkedHashMap<>();

    private void parse(Object controller) {
        for (Method m : controller.getClass().getDeclaredMethods()) {
            Command c = m.getAnnotation(Command.class);
            if (c != null) {
                m.setAccessible(true);
                commandModel = new CommandModel(controller, m, c);
            }
            Option o = m.getAnnotation(Option.class);
            if (o != null) {
                m.setAccessible(true);
                OptionModel om = new OptionModel(controller, m, o);
                optionModels.put(om.getName(), om);
                if (!om.getAlias().isEmpty()) {
                    optionModels.put(om.getAlias(), om);
                }
            }
        }
    }

    /**
     * Constructs a new Commander object.
     *
     * @param controller The application main controller.
     */
    public Commander(Object controller) {
        parse(controller);
    }

    /**
     * Executes the controller.
     *
     * @param args The command line arguments.
     */
    public void execute(String... args) {
        ArrayList<String> as = new ArrayList<>(Arrays.asList(args));
        ArrayList<String> cmas = new ArrayList<>(); // Command Model ArgumentS
        while (!as.isEmpty()) {
            String a = as.remove(0);
            if (optionModels.containsKey(a)) {
                OptionModel om = optionModels.get(a);
                ArrayList<String> omas = new ArrayList<>(); // Option Model ArgumentS
                for (int i = 0; i < om.getParameterCount(); i++) {
                    try {
                        omas.add(as.remove(0));
                    } catch (IndexOutOfBoundsException ex) {
                        throw new RuntimeException("Missing argument(s) for option " + om.getName());
                    }
                }
                om.invoke(omas.toArray());
            } else {
                cmas.add(a);
            }
        }
        if (commandModel != null) {
            commandModel.invoke(new Object[]{cmas.toArray(new String[]{})});
        }
    }

    private void print(PrintStream out) {
        if (commandModel != null) {
            out.printf("Usage: %s\n", commandModel.getUsage());
        }
        Set<OptionModel> oms = new LinkedHashSet<>(optionModels.values());
        if (oms.size() > 0) {
            out.printf("\nOptions:\n");
            int p = 0;
            for (OptionModel om : oms) {
                p = Math.max(p, om.getFullName().length());
            }
            for (OptionModel om : oms) {
                out.printf("\n%1$-" + p + "s", om.getFullName());
                out.printf("%4s", " ");
                out.printf("%s", om.getDescription());
            }
        }
        out.printf("\n");
    }

    /**
     * Supposed to print the exception thrown by execute().
     *
     * @param e The exception to be printed.
     */
    public void printException(Exception e) {
        if (e.getMessage() == null) {
            print(System.out);
        } else {
            System.err.printf("Error: %s\n\n", e.getMessage());
            print(System.err);
        }
    }
}
