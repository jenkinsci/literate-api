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
package org.cloudbees.literate.api.v1.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Represents a virtual file system that contains a project.
 */
public interface ProjectRepository {

    /**
     * Returns the contents of the specified file
     *
     * @param filePath the file path.
     * @return the contents.
     * @throws org.cloudbees.literate.api.v1.vfs.PathNotFoundException
     *                     if the specified path does not exist.
     * @throws IOException if there was a problem retrieving the contents.
     */
    InputStream get(String filePath) throws PathNotFoundException, IOException;

    /**
     * Returns {@code true} if and only if the specified path corresponds to a file.
     *
     * @param path the path.
     * @return the contents.
     * @throws IOException if there was a problem retrieving the contents.
     */
    boolean isFile(String path) throws IOException;

    /**
     * Returns {@code true} if and only if the specified path corresponds to a directory.
     *
     * @param path the file path.
     * @return the contents.
     * @throws IOException if there was a problem retrieving the contents.
     */
    boolean isDirectory(String path) throws IOException;

    /**
     * Returns the immediate child paths of the specified path.
     *
     * @param path the path to get child paths of. {@code null} is equivalent to the empty string or {@code /}
     * @return the set of immediate child paths, fully qualified relative to the repository root. Paths ending in
     *         {@code /} indicate directories.
     * @throws PathNotFoundException if the specified path does not exist.
     * @throws IOException           if there was a problem retrieving the list of child paths.
     */
    Set<String> getPaths(String path) throws PathNotFoundException, IOException;
}
