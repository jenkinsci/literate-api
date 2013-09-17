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

import edu.umd.cs.findbugs.annotations.NonNull;
import net.jcip.annotations.Immutable;
import org.cloudbees.literate.spi.v1.ProjectModelBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * A source of {@link ProjectModel} instances. The source depends on what SPI implementations are available on the
 * classpath, hence you supply a class loader (or use the context class loader) so that the {@link ServiceLoader}
 * can look up the implementations that are within scope.
 */
@Immutable
public class ProjectModelSource {

    /**
     * The classloader.
     */
    private final ClassLoader classLoader;

    /**
     * Constructs an instance from a specific classloader.
     *
     * @param classLoader the classloader.
     */
    public ProjectModelSource(ClassLoader classLoader) {
        classLoader.getClass(); // throw NPE if null
        this.classLoader = classLoader;
    }

    /**
     * Constructs an instance from the current thread's context classloader.
     */
    public ProjectModelSource() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Submits a request and returns the resulting model
     *
     * @param request the request.
     * @return the {@link ProjectModel}.
     * @throws IOException                   if there were IO problems connecting to the
     *                                       {@link ProjectModelRequest#getRepository()}.
     * @throws ProjectModelBuildingException if the {@link ProjectModelRequest#getRepository()} did not contain a valid
     *                                       model definition.
     */
    @NonNull
    public ProjectModel submit(@NonNull ProjectModelRequest request) throws IOException, ProjectModelBuildingException {
        request.getClass(); // throw NPE if null
        IOException ioe = null;
        ProjectModelBuildingException pmbe = null;
        List<ProjectModelBuilder> builders = new ArrayList<ProjectModelBuilder>();
        for (ProjectModelBuilder builder : ServiceLoader.load(ProjectModelBuilder.class, classLoader)) {
            builders.add(builder);
        }
        Collections.sort(builders, new ProjectModelBuilder.PriorityComparator());
        for (ProjectModelBuilder builder : builders) {
            try {
                return builder.build(request);
            } catch (IOException e) {
                if (ioe == null) {
                    ioe = e;
                }
            } catch (ProjectModelBuildingException e) {
                if (pmbe == null) {
                    pmbe = e;
                }
            } catch (NullPointerException e) {
                // ignore
            }
        }
        if (ioe != null) {
            throw ioe;
        }
        if (pmbe != null) {
            throw pmbe;
        }
        throw new ProjectModelBuildingException("Could not find a builder to instantiate a model");
    }

}
