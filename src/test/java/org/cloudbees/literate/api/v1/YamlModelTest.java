/*
 * The MIT License
 *
 * Copyright (c) 2013-2014, CloudBees, Inc., Amadeus IT Group
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbees.literate.api.v1.vfs.FilesystemRepository;
import org.cloudbees.literate.api.v1.vfs.ProjectRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

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


    @Test
    public void javaAnt() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
        assertThatBuildHasFollowingCommands(model, "ant test");
    }

    @Test
    public void javaGradle() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
        assertThatBuildHasFollowingCommands(model, "gradle assemble", "gradle check");
    }

    @Test
    public void javaMaven() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
        assertThatBuildHasFollowingCommands(model, "mvn test");
    }


    @Test
    public void environment() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
        ExecutionEnvironment environment = new ExecutionEnvironment("java-1.7", "windows");
        assertThatBuildHasFollowingCommands(model, environment, "echo java-1.7 on windows");

        ExecutionEnvironment environment_windows = new ExecutionEnvironment("java-1.6", "windows");
        assertThatBuildHasFollowingCommands(model, environment_windows, "echo java-1.6 on windows");

        ExecutionEnvironment environment_linux = new ExecutionEnvironment("linux");
        assertThatBuildHasFollowingCommands(model, environment_linux, "echo linux");

        assertThatTaskHasFollowingCommands(model, "mycommand", "echo mycommand");
    }

    @Test
    public void parseMapEnvironments() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
        assertThatBuildHasFollowingEnvironment(model, new ExecutionEnvironment("windows", "java 1.7", "maven 3.0.5"), new ExecutionEnvironment("windows",
                "java 1.7", "maven 3.1"), new ExecutionEnvironment("windows", "java 1.6"), new ExecutionEnvironment("linux"));

    }

    @Test
    public void envvarsstring() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
        assertThatEnvvarIsEqualTo(model, ExecutionEnvironment.any(), "FOO", "BAR");
    }

    @Test
    public void envvarsstringmultiple() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
        assertThatEnvvarIsEqualTo(model, ExecutionEnvironment.any(), "FOO", "BAR");
        assertThatEnvvarIsEqualTo(model, ExecutionEnvironment.any(), "ALPHA", "BETA");
    }

    @Test
    public void envvarsglobalmap() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
        assertThatEnvvarIsEqualTo(model, ExecutionEnvironment.any(), "FOO", "BAR");
        assertThatEnvvarIsEqualTo(model, ExecutionEnvironment.any(), "ALPHA", "BETA");
    }

    @Test
    public void envvarsglobalmaplist() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
        assertThatEnvvarIsEqualTo(model, ExecutionEnvironment.any(), "FOO", "BAR");
        assertThatEnvvarIsEqualTo(model, ExecutionEnvironment.any(), "ALPHA", "BETA");
    }
    
    @Test
    public void envvarsmatrix() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
        assertEquals(2, model.getEnvironments().size());
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("FOO", "BAR");
        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("FOO", "BETA");
        assertThatBuildHasFollowingEnvironment(model, ExecutionEnvironment.any().withVariables(map1), ExecutionEnvironment.any().withVariables(map2));
    }
    
    @Test
    public void envvarsmatrixandglobal() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
        assertEquals(2, model.getEnvironments().size());
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("FOO", "BAR");
        map1.put("ALPHA", "a");
        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("FOO", "BETA");
        map2.put("ALPHA", "a");
        assertThatBuildHasFollowingEnvironment(model, ExecutionEnvironment.any().withVariables(map1), ExecutionEnvironment.any().withVariables(map2));
    }

    @Test(expected = ProjectModelBuildingException.class)
    public void noBuildCommand() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
    }

    @Test
    public void multipleBuildIdsSeparatedByComma() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).withBuildId("build, build2").build());
        assertThatBuildHasFollowingCommands(model, "echo Hello world", "echo I'm building too!");
    }

    @Test
    public void multipleBuildIdsSeparatedBySpace() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).withBuildId("build build2").build());
        assertThatBuildHasFollowingCommands(model, "echo Hello world", "echo I'm building too!");
    }

    @Test
    public void multipleBuildIdsMultipleEnvironments() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).withBuildId("build, build2").build());
        assertThatBuildHasFollowingCommands(model, new ExecutionEnvironment("linux"), "echo linux", "echo I'm building too!");
        assertThatBuildHasFollowingCommands(model, new ExecutionEnvironment("windows"), "echo windows", "echo I'm building too!");
    }

    @Test
    public void simpleEnvironment() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
        assertThatBuildHasFollowingEnvironment(model, new ExecutionEnvironment("linux"));
    }

    @Test
    public void simpleList() throws Exception {
        ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
        assertThatBuildHasFollowingEnvironment(model, new ExecutionEnvironment("windows", "java-1.7", "maven-3.0.5"));
    }

    private void assertThatBuildHasFollowingCommands(ProjectModel model, ExecutionEnvironment environment, String... commands) {
        BuildCommands buildCommand = model.getBuild();
        List<String> buildMatchingCommands = buildCommand.getMatchingCommand(environment);
        assertNotNull("No matching commands for " + environment, buildMatchingCommands);
        assertEquals(commands.length, buildMatchingCommands.size());
        for (int i = 0; i < commands.length; i++) {
            assertEquals(commands[i], buildMatchingCommands.get(i));
        }
    }

    private void assertThatBuildHasFollowingCommands(ProjectModel model, String... commands) {
        BuildCommands buildCommand = model.getBuild();
        List<String> buildMatchingCommands = buildCommand.getMatchingCommand(new ExecutionEnvironment());
        assertEquals(commands.length, buildMatchingCommands.size());
        for (int i = 0; i < commands.length; i++) {
            assertEquals(commands[i], buildMatchingCommands.get(i));
        }
    }

    private void assertThatTaskHasFollowingCommands(ProjectModel model, String task, String... commands) {
        TaskCommands taskCommand = model.getTask(task);
        List<String> taskMatchingCommands = taskCommand.getMatchingCommand(new ExecutionEnvironment());
        assertEquals(commands.length, taskMatchingCommands.size());
        for (int i = 0; i < commands.length; i++) {
            assertEquals(commands[i], taskMatchingCommands.get(i));
        }
    }

    private void assertThatBuildHasFollowingEnvironment(ProjectModel model, ExecutionEnvironment... environments) {
      List<ExecutionEnvironment> buildMatchingEnvironment = new ArrayList<ExecutionEnvironment>(model.getEnvironments());
        assertEquals(environments.length, buildMatchingEnvironment.size());
      List<ExecutionEnvironment> input = new ArrayList<ExecutionEnvironment> (Arrays.asList(environments));
      Collections.sort(buildMatchingEnvironment);
      Collections.sort(input);
      assertEquals(input, buildMatchingEnvironment);
    }
    
    private void assertThatEnvvarIsEqualTo(ProjectModel model, ExecutionEnvironment env, String key, String value) {
        Map<String, String> map = model.getEnvironmentVariablesFor(env);
        assertTrue("Key not found : " + key, map.containsKey(key));
        assertEquals("Value incorrect for " + key, value, map.get(key));
    }

}
