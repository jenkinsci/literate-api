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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;

/**
 * A {@link ProjectRepository} hosted on the local file system.
 */
public class FilesystemRepository implements ProjectRepository {
    /**
     * The root of the {@link ProjectRepository}.
     */
    private final File root;

    /**
     * Constructor.
     *
     * @param root The root of the {@link ProjectRepository}.
     */
    public FilesystemRepository(File root) {
        this.root = root;
    }

    /**
     * Resolves the path to a file.
     *
     * @param path the path.
     * @return the {@link File} corresponding to the path.
     * @throws PathNotFoundException if the path does not exist.
     */
    private File resolve(String path) throws PathNotFoundException {
        File dir;
        if (path == null || path.trim().length() == 0 || path.equals("/")) {
            return root;
        } else {
            dir = new File(root, path);
            String p1 = root.getAbsolutePath().replace('\\', '/');
            if (!p1.endsWith("/")) {
                p1 = p1 + "/";
            }
            String p2 = dir.getAbsolutePath().replace('\\', '/');
            if (!p2.endsWith("/")) {
                p2 = p2 + "/";
            }
            if (p2.startsWith(p1)) {
                return dir;
            } else {
                throw new PathNotFoundException("Path is outside of repository");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream get(String filePath) throws PathNotFoundException, IOException {
        try {
            return new FileInputStream(resolve(filePath));
        } catch (FileNotFoundException e) {
            throw new PathNotFoundException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirectory(String path) throws IOException {
        return resolve(path).isDirectory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFile(String path) throws IOException {
        return resolve(path).isFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getPaths(String path) throws PathNotFoundException, IOException {
        File dir;
        String prefix;
        if (path == null || path.trim().length() == 0 || path.equals("/")) {
            dir = root;
            prefix = "/";
        } else {
            dir = new File(root, path);
            String p1 = root.getAbsolutePath().replace('\\', '/');
            if (!p1.endsWith("/")) {
                p1 = p1 + "/";
            }
            String p2 = dir.getAbsolutePath().replace('\\', '/');
            if (!p2.endsWith("/")) {
                p2 = p2 + "/";
            }
            if (p2.startsWith(p1)) {
                prefix = "/" + p2.substring(p1.length());
            } else {
                throw new PathNotFoundException("Path is outside of repository");
            }
        }
        if (!dir.isDirectory()) {
            throw new PathNotFoundException("Path does not exist or is not a directory");
        }
        Set<String> result = new TreeSet<String>();
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                result.add(prefix + f.getName() + "/");
            } else {
                result.add(prefix + f.getName());
            }
        }
        return result;
    }
}
