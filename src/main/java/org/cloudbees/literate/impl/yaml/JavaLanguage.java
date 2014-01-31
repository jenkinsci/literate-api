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
package org.cloudbees.literate.impl.yaml;

import org.cloudbees.literate.api.v1.ProjectModel;
import org.cloudbees.literate.api.v1.ProjectModelBuildingException;
import org.cloudbees.literate.api.v1.ProjectModelValidationException;
import org.cloudbees.literate.api.v1.vfs.ProjectRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author stephenc
 * @since 17/05/2013 13:29
 */
public class JavaLanguage implements Language {
    //@Override
    public Set<String> supported() {
        return Collections.singleton("java");
    }

    //@Override
    public ProjectModel build(Map<String, Object> model, ProjectRepository repository)
            throws IOException, ProjectModelBuildingException {
        if (repository.isFile("build.gradle")) {
            return ProjectModel.builder().addBuild("gradle check").build();
        }
        if (repository.isFile("pom.xml")) {
            return ProjectModel.builder().addBuild("mvn test").build();
        }
        return ProjectModel.builder().addBuild("ant test").build();
    }
}
