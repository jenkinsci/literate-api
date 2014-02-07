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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
     * @param commands the build commands.
     */
    public BuildCommands(List<String> commands) {
        this(null, commands);
    }

    /**
     * Constructor.
     *
     * @param environment the environment that the command required.
     * @param commands    the build command.
     */
    public BuildCommands(ExecutionEnvironment environment, List<String> commands) {
        this(Collections.singletonMap(environment == null ? ExecutionEnvironment.any() : environment, commands));
    }

    /**
     * Constructor.
     *
     * @param commands   the build commands.
     * @param parameters the parameters.
     * @since 0.6
     */
    public BuildCommands(List<String> commands, List<Parameter> parameters) {
        this(null, commands, parameters);
    }

    /**
     * Constructor.
     *
     * @param environment the environment that the command required.
     * @param commands    the build command.
     * @param parameters  the parameters.
     * @since 0.6
     */
    public BuildCommands(ExecutionEnvironment environment, List<String> commands, List<Parameter> parameters) {
        this(Collections.singletonMap(environment == null ? ExecutionEnvironment.any() : environment, commands),
                parameters);
    }

    /**
     * Constructor.
     *
     * @param buildCommands a map of environments to build commands, as each environment may have separate build
     *                      commands.
     */
    public BuildCommands(Map<ExecutionEnvironment, List<String>> buildCommands) {
        this(buildCommands, null);
    }

    /**
     * Constructor.
     *
     * @param buildCommands a map of environments to build commands, as each environment may have separate build
     *                      commands.
     * @param parameters    the parameters.
     * @since 0.6
     */
    public BuildCommands(Map<ExecutionEnvironment, List<String>> buildCommands, List<Parameter> parameters) {
        super(buildCommands, parameters);
    }

    /**
     * Merges two build commands. Matching sections from the second will be appended to the first.
     *
     * @param cmd1 the first.
     * @param cmd2 the second.
     * @return the merged commands.
     */
    public static BuildCommands merge(BuildCommands cmd1, BuildCommands cmd2) {
        Map<ExecutionEnvironment, List<String>> result =
                new LinkedHashMap<ExecutionEnvironment, List<String>>(cmd1.getCommands());
        for (Map.Entry<ExecutionEnvironment, List<String>> entry : cmd2.getCommands().entrySet()) {
            result.put(entry.getKey(), join(result.get(entry.getKey()), entry.getValue()));
        }
        List<Parameter> parameters = new ArrayList<Parameter>(Parameter.toList(cmd1.getParameters()));
        parameters.addAll(Parameter.toList(cmd2.getParameters()));
        return new BuildCommands(result, parameters);
    }

}
