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
import org.cloudbees.literate.api.v1.vfs.ProjectRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A request for a {@link ProjectModel}
 *
 * @author Stephen Connolly
 */
@Immutable
public class ProjectModelRequest {

    /**
     * Ensure consistent serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The base name that the project model builder will use to detect the model. The base name should typically be used
     * to detect a "marker" file, the presence of which implies that the project model can be built by the builder and
     * the absence implies that the builder should not try and build the project model.
     */
    @NonNull
    private final String baseName;

    /**
     * The repository to build the project model from.
     */
    @NonNull
    private final ProjectRepository repository;

    /**
     * The id of the environments within the source model. The builder may ignore this part of the request if
     * the source format does not permit customization of the id of a list of target environments.
     */
    @NonNull
    private final String environmentsId;

    /**
     * The id of the build instructions within the source model. The builder may ignore this part of the request if
     * the source format does not permit customization of the id of build instructions.
     */
    @NonNull
    private final String buildId;

    /**
     * The ids of post-build tasks to be collected from the source model. The builder may ignore this part of the
     * request if the source format does not permit specification of post-build tasks.
     */
    @NonNull
    private final Set<String> taskIds;

    /**
     * Use {@link #builder(org.cloudbees.literate.api.v1.vfs.ProjectRepository)}.
     *
     * @param baseName       the base name.
     * @param repository     the repository.
     * @param environmentsId the environment id.
     * @param buildId        the build id.
     * @param taskIds        the task ids.
     */
    private ProjectModelRequest(@CheckForNull String baseName,
                                @NonNull ProjectRepository repository,
                                @CheckForNull String environmentsId,
                                @CheckForNull String buildId,
                                @NonNull List<String> taskIds) {
        repository.getClass();
        this.baseName = baseName == null ? "cloudbees" : baseName;
        this.repository = repository;
        this.environmentsId = environmentsId == null ? "environments" : environmentsId;
        this.buildId = buildId == null ? "build" : buildId;
        this.taskIds = taskIds.isEmpty()
                ? Collections.singleton("deploy")
                : Collections.unmodifiableSet(new TreeSet<String>(taskIds));
    }

    /**
     * Gets the base name that the project model builder will use to detect the model.
     *
     * @return the base name.
     */
    @NonNull
    public String getBaseName() {
        return baseName;
    }

    /**
     * Gets the id of the build instructions within the source model.
     *
     * @return the id of the build instructions within the source model.
     */
    @NonNull
    public String getBuildId() {
        return buildId;
    }

    /**
     * Gets the id of the environments within the source model.
     *
     * @return the id of the environments within the source model
     */
    @NonNull
    public String getEnvironmentsId() {
        return environmentsId;
    }

    /**
     * Returns the project repository to construct the project model from.
     *
     * @return the project repository to construct the project model from.
     */
    @NonNull
    public ProjectRepository getRepository() {
        return repository;
    }

    /**
     * Returns the task ids to additionally populate in the project model.
     *
     * @return the task ids to additionally populate in the project model.
     */
    @NonNull
    public Set<String> getTaskIds() {
        return taskIds;
    }

    /**
     * Instantiates a new {@link Builder}.
     *
     * @param repository the repository to construct the project model from.
     * @return the {@link Builder}.
     */
    @NonNull
    public static Builder builder(ProjectRepository repository) {
        return new Builder(repository);
    }

    /**
     * A builder of {@link ProjectModelRequest} instances.
     */
    @NotThreadSafe
    public static class Builder {
        /**
         * The base name that the project model builder will use to detect the model. The base name should typically
         * be used
         * to detect a "marker" file, the presence of which implies that the project model can be built by the
         * builder and
         * the absence implies that the builder should not try and build the project model.
         */
        @CheckForNull
        private String baseName;

        /**
         * The repository to build the project model from.
         */
        @NonNull
        private ProjectRepository repository;

        /**
         * The id of the environments within the source model. The builder may ignore this part of the request if
         * the source format does not permit customization of the id of a list of target environments.
         */
        @CheckForNull
        private String environmentsId;

        /**
         * The id of the build instructions within the source model. The builder may ignore this part of the request if
         * the source format does not permit customization of the id of build instructions.
         */
        @CheckForNull
        private String buildId;

        /**
         * The ids of post-build tasks to be collected from the source model. The builder may ignore this part of the
         * request if the source format does not permit specification of post-build tasks.
         */
        @NonNull
        private final List<String> taskIds = new ArrayList<String>();

        /**
         * Use {@link ProjectModelRequest#builder(org.cloudbees.literate.api.v1.vfs.ProjectRepository)}.
         *
         * @param repository the repository.
         */
        private Builder(@NonNull ProjectRepository repository) {
            repository.getClass();
            this.repository = repository;
        }

        /**
         * Configure the base name to use for the request.
         *
         * @param baseName the base name to use for the request.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder withBaseName(@CheckForNull String baseName) {
            this.baseName = baseName;
            return this;
        }

        /**
         * Configure the environments id to use for the request.
         *
         * @param environmentsId the environments id to use for the request.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder withEnvironmentsId(@CheckForNull String environmentsId) {
            this.environmentsId = environmentsId;
            return this;
        }

        /**
         * Configure the build id to use for the request.
         *
         * @param buildId the build id to use for the request.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder withBuildId(@CheckForNull String buildId) {
            this.buildId = buildId;
            return this;
        }

        /**
         * Adds a task id to the request.
         *
         * @param taskId the task id to add to the request.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addTaskId(@NonNull String taskId) {
            taskId.getClass();
            if (!this.taskIds.contains(taskId.toLowerCase())) {
                this.taskIds.add(taskId.toLowerCase());
            }
            return this;
        }

        /**
         * Adds task ids to the request.
         *
         * @param taskIds the task ids to add to the request.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addTaskIds(String... taskIds) {
            for (String taskId : taskIds) {
                addTaskId(taskId);
            }
            return this;
        }

        /**
         * Adds task ids to the request.
         *
         * @param taskIds the task ids to add to the request.
         * @return {@code this} for method chaining.
         */
        @NonNull
        public Builder addTaskIds(Collection<String> taskIds) {
            for (String taskId : taskIds) {
                addTaskId(taskId);
            }
            return this;
        }

        /**
         * Builds the {@link ProjectModelRequest}.
         *
         * @return the {@link ProjectModelRequest} instance.
         */
        @NonNull
        public ProjectModelRequest build() {
            return new ProjectModelRequest(baseName, repository, environmentsId, buildId, taskIds);
        }
    }
}
