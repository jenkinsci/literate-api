/*
 * The MIT License
 *
 * Copyright (c) 2013, CloudBees, Inc.
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
package org.cloudbees.literate.api.v1;

import net.jcip.annotations.Immutable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This represents build commands.
 */
@Immutable
public class BuildCommands extends AbstractCommands {

    /**
     * Ensure consistent serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param command the build command.
     */
    public BuildCommands(String command) {
        this(null, command);
    }

    /**
     * Constructor.
     *
     * @param environment the environment that the command required.
     * @param command     the build command.
     */
    public BuildCommands(ExecutionEnvironment environment, String command) {
        this(Collections.singletonMap(environment == null ? ExecutionEnvironment.any() : environment, command));
    }

    /**
     * Constructor.
     *
     * @param buildCommands a map of environments to build commands, as each environment may have separate build
     *                      commands.
     */
    public BuildCommands(Map<ExecutionEnvironment, String> buildCommands) {
        super(buildCommands);
    }

    /**
     * Merges two build commands. Matching sections from the second will be appended to the first.
     *
     * @param cmd1 the first.
     * @param cmd2 the second.
     * @return the merged commands.
     */
    public static BuildCommands merge(BuildCommands cmd1, BuildCommands cmd2) {
        Map<ExecutionEnvironment, String> result = new LinkedHashMap<ExecutionEnvironment, String>(cmd1.getCommands());
        for (Map.Entry<ExecutionEnvironment, String> entry : cmd2.getCommands().entrySet()) {
            result.put(entry.getKey(), join(result.get(entry.getKey()), entry.getValue()));
        }
        return new BuildCommands(result);
    }

}
