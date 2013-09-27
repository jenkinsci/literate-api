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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import net.jcip.annotations.Immutable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This represents an entire section of a  project definition - stuff under a heading.
 * NOT an environment.
 */
@Immutable
public abstract class AbstractCommands implements Serializable {

    /**
     * Ensure consistent serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The map of commands keyed by execution environment.
     */
    private final Map<ExecutionEnvironment, List<String>> commands;

    /**
     * Constructor.
     *
     * @param buildCommands a map of environments to build commands, as each environment may have separate build
     *                      commands.
     */
    protected AbstractCommands(Map<ExecutionEnvironment, List<String>> buildCommands) {
        Map<ExecutionEnvironment, List<String>> cmds = new LinkedHashMap<ExecutionEnvironment, List<String>>();
        if (buildCommands != null) {
            for (Map.Entry<ExecutionEnvironment, List<String>> entry : buildCommands.entrySet()) {
                if (entry == null) {
                    continue;
                }
                cmds.put(entry.getKey() == null
                        ? ExecutionEnvironment.any()
                        : entry.getKey(),
                        entry.getValue() == null
                                ? null
                                : new ArrayList<String>(entry.getValue())
                );
            }
        }

        this.commands = Collections.unmodifiableMap(cmds);
    }


    /**
     * Given an environment - return us the whole script for this section, based on a "best" environmental match.
     * I blame stephen and will put passive agressive comments in the git commit for this.
     */
    public List<String> getMatchingCommand(ExecutionEnvironment envs) {
        Map.Entry<ExecutionEnvironment, List<String>> firstBest = null;
        for (Map.Entry<ExecutionEnvironment, List<String>> entry : commands.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }
            if ((envs.isUnspecified() && entry.getKey().isUnspecified()) || envs.isMatchFor(entry.getKey())) {
                // only consider those entries that overlap
                if (firstBest == null || firstBest.getKey().getLabels().size() > entry.getKey().getLabels().size()) {
                    firstBest = entry;
                }
            }
        }
        return firstBest == null ? null : firstBest.getValue();

    }

    public Map<ExecutionEnvironment, List<String>> getCommands() {
        return commands;
    }

    @CheckForNull
    public static List<String> join(@CheckForNull List<String> cmd1, @CheckForNull List<String> cmd2) {
        if (cmd1 == null || cmd1.isEmpty()) {
            return cmd2;
        }
        if (cmd2 == null || cmd2.isEmpty()) {
            return cmd1;
        }
        List<String> result = new ArrayList<String>(cmd1.size() + cmd2.size());
        result.addAll(cmd1);
        result.addAll(cmd2);
        return Collections.unmodifiableList(result);
    }

}
