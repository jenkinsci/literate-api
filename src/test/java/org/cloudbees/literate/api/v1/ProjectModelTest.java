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

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ProjectModelTest {

    @Test
    public void smokes() throws ProjectModelBuildingException {
        assertThat(ProjectModel.builder().addBuild("mvn test").build().getBuildFor(
                ExecutionEnvironment.any()), is(Collections.singletonList("mvn test")));
    }

    @Test
    public void builderSmokes() throws ProjectModelBuildingException {
        assertThat(ProjectModel.builder().addBuild("mvn test").build().getBuildFor(), is(Collections.singletonList("mvn test")));
    }

    @Test(expected = IllegalStateException.class)
    public void buildCommandIsEnvSpecificOrGlobal() throws ProjectModelBuildingException {
        ProjectModel.builder()
                .addBuild(Collections.singleton("linux"), "mvn test")
                .addBuild("mvn verify");
    }

    @Test(expected = IllegalStateException.class)
    public void buildCommandIsEnvSpecificOrGlobal2() throws ProjectModelBuildingException {
        ProjectModel.builder()
                .addBuild("mvn verify")
                .addBuild(Collections.singleton("linux"), "mvn test");
    }

    @Test
    public void envSpecific() throws ProjectModelBuildingException {
        ProjectModel model = ProjectModel.builder()
                .addEnvironment("linux")
                .addEnvironment("windows")
                .addBuild(Collections.singleton("linux"), "mvn test")
                .addBuild(Collections.singleton("windows"), "mvn.bat test").build();
        assertThat(model.getBuildFor(), nullValue());
        assertThat(model.getBuildFor("linux"), is(Collections.singletonList("mvn test")));
        assertThat(model.getBuildFor("windows"), is(Collections.singletonList("mvn.bat test")));
    }


}
