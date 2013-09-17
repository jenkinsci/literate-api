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

import java.util.ServiceLoader;

/**
 * A formatter of {@link ProjectModel} instances.
 *
 * @author Stephen Connolly
 */
public abstract class ProjectModelFormatter {

    /**
     * The Markdown mime type. A {@link ProjectModelFormatter} supporting this mime type must be available.
     */
    public static final String MARKDOWN = "text/markdown";

    /**
     * The mime type implemented by this formatter.
     */
    private final String mimeType;

    /**
     * Constructor.
     *
     * @param mimeType the mime type.
     */
    protected ProjectModelFormatter(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Formats the model as a string.
     *
     * @param model the model.
     * @return the string formatted model.
     */
    public abstract String format(ProjectModel model);

    /**
     * Gets an instance from the current thread's context classloader for the specified mime type.
     *
     * @param mimeType the mime type.
     * @return the instance or {@code null} if no instance available for the specified mime type.
     */
    @CheckForNull
    public static ProjectModelFormatter getInstance(@NonNull String mimeType) {
        return getInstance(Thread.currentThread().getContextClassLoader(), mimeType);
    }

    /**
     * Gets an instance from the specified classloader for the specified mime type.
     *
     * @param classLoader the classloader.
     * @param mimeType    the mime type.
     * @return the instance or {@code null} if no instance available for the specified mime type.
     */
    @CheckForNull
    public static ProjectModelFormatter getInstance(@NonNull ClassLoader classLoader, @NonNull String mimeType) {
        for (ProjectModelFormatter formatter : ServiceLoader.load(ProjectModelFormatter.class, classLoader)) {
            if (mimeType.equals(formatter.mimeType)) {
                return formatter;
            }
        }
        return null;
    }

}
