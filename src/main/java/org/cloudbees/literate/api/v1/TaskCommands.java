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
import java.util.List;

/**
 * Represents a post-build task.
 */
@Immutable
public class TaskCommands extends AbstractCommands {

    /**
     * Constructor.
     *
     * @param commands the command.
     */
    public TaskCommands(List<String> commands) {
        super(Collections.singletonMap(ExecutionEnvironment.any(), commands));
    }

    /**
     * Returns the command.
     *
     * @return the command.
     */
    public List<String> getCommand() {
        return getCommands().get(ExecutionEnvironment.any());
    }

    /**
     * Merges two commands, the second one will be appended to the first.
     *
     * @param cmd1
     * @param cmd2
     * @return
     */
    public static TaskCommands merge(TaskCommands cmd1, TaskCommands cmd2) {
        return new TaskCommands(join(cmd1.getCommand(), cmd2.getCommand()));
    }
}
