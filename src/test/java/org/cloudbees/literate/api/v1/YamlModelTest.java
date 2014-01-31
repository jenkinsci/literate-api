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

import org.cloudbees.literate.api.v1.vfs.FilesystemRepository;
import org.cloudbees.literate.api.v1.vfs.PathNotFoundException;
import org.cloudbees.literate.api.v1.vfs.ProjectRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assume.assumeThat;

public class YamlModelTest {

    @Rule
    public TestName name = new TestName();

    public File projectRootDir;

    public ProjectRepository repository;

    @Before
    public void findProjectRoot() throws Exception {
        URL url = getClass().getResource(getClass().getSimpleName() + "/" + name.getMethodName());
        assumeThat("The test resource for " + name.getMethodName() + " exist", url, notNullValue());
        try {
            projectRootDir = new File(url.toURI());
        } catch (URISyntaxException e) {
            projectRootDir = new File(url.getPath());
        }
        assumeThat("The test resource for " + name.getMethodName() + " exist", projectRootDir.isDirectory(), is(true));
        repository = new FilesystemRepository(projectRootDir);
    }

    @Test
    public void smokes() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
    }

    @Test
    public void advanced() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
    }

    @Test(expected = ProjectModelBuildingException.class)
    public void empty() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
    }


}
