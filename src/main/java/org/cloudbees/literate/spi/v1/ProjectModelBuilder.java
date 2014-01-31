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
package org.cloudbees.literate.spi.v1;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.cloudbees.literate.api.v1.ProjectModel;
import org.cloudbees.literate.api.v1.ProjectModelBuildingException;
import org.cloudbees.literate.api.v1.ProjectModelRequest;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 * Service provider interface for {@link org.cloudbees.literate.api.v1.ProjectModelSource}.
 */
public interface ProjectModelBuilder {
    /**
     * Tries to build a {@link ProjectModel} from the supplied {@link ProjectModelRequest}
     *
     * @param request the request.
     * @return the model.
     * @throws IOException                   if things go wrong.
     * @throws ProjectModelBuildingException if the source repository does not yield a valid model.
     */
    ProjectModel build(ProjectModelRequest request) throws IOException, ProjectModelBuildingException;

    /**
     * Returns the marker filename(s) that the model builder supports based on the supplied basename.
     * The presence of a marker file in a project root indicates that the project root is worth attempting
     * to {@link #build(org.cloudbees.literate.api.v1.ProjectModelRequest)} against.
     * @param basename the {@link org.cloudbees.literate.api.v1.ProjectModelRequest#getBaseName()}
     * @return the set of marker filenames.
     */
    @NonNull
    Collection<String> markerFiles(@NonNull String basename);

    /**
     * Annotation to allow defining the sequence amongst competing {@link ProjectModelBuilder} implementations.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Priority {
        /**
         * The priority, higher gets an earlier chance.
         *
         * @return The priority, higher gets an earlier chance.
         */
        int value();
    }

    /**
     * A {@link Comparator} that sorts by decreasing {@link Priority}.
     */
    public static class PriorityComparator implements Comparator<ProjectModelBuilder>, Serializable {
        /**
         * {@inheritDoc}
         */
        //@Override
        public int compare(ProjectModelBuilder o1, ProjectModelBuilder o2) {
            Priority a1 = o1.getClass().getAnnotation(Priority.class);
            Priority a2 = o2.getClass().getAnnotation(Priority.class);
            int p1 = (a1 == null) ? 0 : a1.value();
            int p2 = (a2 == null) ? 0 : a2.value();
            return p1 == p2 ? 0 : (p1 > p2 ? -1 : 1);
        }
    }
}
