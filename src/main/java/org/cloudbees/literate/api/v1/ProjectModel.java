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
import edu.umd.cs.findbugs.annotations.NonNull;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a literate project.
 */
@Immutable
public class ProjectModel implements Serializable {

    /**
     * Ensure consistent serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The environments that this project should be built on.
     */
    @NonNull
    private final List<ExecutionEnvironment> environments;

    /**
     * The commands used to build the project.
     */
    @NonNull
    private final BuildCommands build;

    /**
     * The post-build tasks available for the project.
     */
    @NonNull
    private final Map<String, TaskCommands> tasks;

    /**
     * Instantiates a {@link Builder} for creating {@link ProjectModel} instances.
     *
     * @return a new {@link Builder} for creating {@link ProjectModel} instances.
     */
    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Do not invoke directly, use {@link #builder()}.
     *
     * @param environments the environments.
     * @param build        the build commands.
     * @param tasks        the tasks and their commands.
     */
    private ProjectModel(@CheckForNull List<ExecutionEnvironment> environments,
                         @CheckForNull BuildCommands build,
                         @CheckForNull Map<String, TaskCommands> tasks) {
        if (environments != null) {
            this.environments = Collections.unmodifiableList(new ArrayList<ExecutionEnvironment>(environments));
        } else {
            this.environments = Collections.singletonList(ExecutionEnvironment.any());
        }
        this.build = build == null ? new BuildCommands(Collections.singletonList("")) : build;
        this.tasks = tasks == null ? Collections.<String, TaskCommands>emptyMap() : Collections.unmodifiableMap(tasks);
    }

    /**
     * Helper version of {@link #getBuildFor(ExecutionEnvironment)}.
     *
     * @param labels the labels.
     * @return the build command or {@code null} if no build command matches the supplied labels.
     */
    @CheckForNull
    public List<String> getBuildFor(String... labels) {
        return getBuildFor(new ExecutionEnvironment(labels));
    }

    /**
     * Gets the matching build command.
     *
     * @param environment the environment.
     * @return the build command or {@code null} if no build command matches the supplied environment.
     */
    @CheckForNull
    public List<String> getBuildFor(@CheckForNull ExecutionEnvironment environment) {
        return build.getMatchingCommand(environment == null ? ExecutionEnvironment.any() : environment);
    }

    /**
     * Returns the build commands.
     *
     * @return the build commands.
     */
    @NonNull
    public BuildCommands getBuild() {
        return build;
    }

    /**
     * Returns the task ids of all the tasks defined in the model.
     *
     * @return the task ids of all the tasks defined in the model.
     */
    @NonNull
    public Set<String> getTaskIds() {
        return tasks.keySet();
    }

    /**
     * Returns the task command for the specified task id.
     *
     * @param taskId the task id.
     * @return the task command or {@code null}.
     */
    @CheckForNull
    public TaskCommands getTask(@CheckForNull String taskId) {
        return taskId == null ? null : tasks.get(taskId);
    }

    /**
     * Returns the (possibly empty) list of build environments.
     *
     * @return the (possibly empty) list of build environments.
     */
    @NonNull
    public List<ExecutionEnvironment> getEnvironments() {
        return environments;
    }

    /**
     * The builder of {@link ProjectModel} instances.
     */
    @NotThreadSafe
    public static class Builder {
        /**
         * The environments.
         */
        @NonNull
        private final List<ExecutionEnvironment> environments = new ArrayList<ExecutionEnvironment>();

        /**
         * The build commands.
         */
        @NonNull
        private final Map<ExecutionEnvironment, List<String>> build =
                new LinkedHashMap<ExecutionEnvironment, List<String>>();

        /**
         * The post-build tasks.
         */
        @NonNull
        private final Map<String, TaskCommands> tasks = new LinkedHashMap<String, TaskCommands>();

        /**
         * Use {@link org.cloudbees.literate.api.v1.ProjectModel#builder()}.
         */
        private Builder() {
        }

        /**
         * Adds the supplied environments.
         *
         * @param environments the environments to add.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addEnvironments(@NonNull Collection<ExecutionEnvironment> environments) {
            this.environments.addAll(environments);
            return this;
        }

        /**
         * Adds the supplied environment.
         *
         * @param environment the environment to add.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addEnvironment(@NonNull ExecutionEnvironment environment) {
            environment.getClass();
            environments.add(environment);
            return this;
        }

        /**
         * Adds the supplied environment.
         *
         * @param labels the labels of the environment to add.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addEnvironment(String... labels) {
            return addEnvironment(new ExecutionEnvironment(labels));
        }

        /**
         * Adds the supplied build commands.
         *
         * @param build the build commands.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addBuild(Map<ExecutionEnvironment, List<String>> build) {
            for (Map.Entry<ExecutionEnvironment, List<String>> entry : build.entrySet()) {
                addBuild(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * Adds the supplied build command.
         *
         * @param command the build command.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addBuild(String command) {
            return addBuild(Collections.<String>emptySet(), command);
        }

        /**
         * Adds the supplied build commands.
         *
         * @param commands the build commands.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addBuild(List<String> commands) {
            return addBuild(Collections.<String>emptySet(), commands);
        }

        /**
         * Adds the supplied build command.
         *
         * @param labels  the environment labels.
         * @param commands the build command.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addBuild(Set<String> labels, List<String> commands) {
            return addBuild(new ExecutionEnvironment(labels), commands);
        }

        /**
         * Adds the supplied build command.
         *
         * @param labels  the environment labels.
         * @param command the build command.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addBuild(Set<String> labels, String command) {
            return addBuild(new ExecutionEnvironment(labels), command);
        }

        /**
         * Adds the supplied build command.
         *
         * @param environment the environment.
         * @param command     the build command.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addBuild(ExecutionEnvironment environment, String command) {
            return addBuild(environment, Collections.singletonList(command));
        }

        /**
         * Adds the supplied build command.
         *
         * @param environment the environment.
         * @param commands     the build command.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addBuild(ExecutionEnvironment environment, List<String> commands) {
            if (environment == null) {
                environment = ExecutionEnvironment.any();
            }
            if (environment.isUnspecified()) {
                if (build.size() > 1 || (build.size() == 1 && !build.containsKey(ExecutionEnvironment.any()))) {
                    throw new IllegalStateException("Cannot have a global command and environment specific commands");
                }
            } else if (build.size() == 1 && build.containsKey(ExecutionEnvironment.any())) {
                throw new IllegalStateException("Cannot have a global command and environment specific commands");
            }
            List<String> existing = build.get(environment);
            if (existing == null) {
                build.put(environment, commands);
            } else {
                build.put(environment, AbstractCommands.join(existing, commands));
            }
            return this;
        }

        /**
         * Adds the supplied task command.
         *
         * @param taskId  the task id.
         * @param command the task command.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addTask(String taskId, String command) {
            return addTask(taskId, Collections.singletonList(command));
        }

        /**
         * Adds the supplied task command.
         *
         * @param taskId  the task id.
         * @param commands the task command.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addTask(String taskId, List<String> commands) {
            return addTask(taskId, new TaskCommands(commands));
        }

        /**
         * Adds the supplied task command.
         *
         * @param taskId   the task id.
         * @param commands the task commands.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addTask(String taskId, TaskCommands commands) {
            if (this.tasks.containsKey(taskId)) {
                this.tasks.put(taskId, TaskCommands.merge(this.tasks.get(taskId), commands));
            } else {
                this.tasks.put(taskId, commands);
            }
            return this;
        }

        /**
         * Builds the {@link ProjectModel} instance.
         *
         * @return the {@link ProjectModel} instance.
         */
        @NonNull
        public ProjectModel build() {
            return new ProjectModel(environments.isEmpty()
                    ? Collections.singletonList(ExecutionEnvironment.any())
                    : environments,
                    new BuildCommands(build),
                    tasks);
        }

    }

}
