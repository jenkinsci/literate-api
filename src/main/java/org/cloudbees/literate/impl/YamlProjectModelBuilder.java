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
package org.cloudbees.literate.impl;

import org.apache.commons.io.IOUtils;
import org.cloudbees.literate.api.v1.ProjectModel;
import org.cloudbees.literate.api.v1.ProjectModelBuildingException;
import org.cloudbees.literate.api.v1.ProjectModelRequest;
import org.cloudbees.literate.impl.yaml.Language;
import org.cloudbees.literate.spi.v1.ProjectModelBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * A {@link ProjectModelBuilder} that uses a YAML file as the source of its {@link ProjectModel}
 */
@ProjectModelBuilder.Priority(-1000)
public class YamlProjectModelBuilder implements ProjectModelBuilder {
    /**
     * {@inheritDoc}
     */
    //@Override
    public ProjectModel build(ProjectModelRequest request) throws IOException, ProjectModelBuildingException {
        for (String name : Arrays.asList("." + request.getBaseName() + ".yml", ".travis.yml")) {
            if (request.getRepository().isFile(name)) {
                InputStream stream = request.getRepository().get(name);
                try {
                    Yaml yaml = new Yaml();
                    Map<String, Object> model = (Map<String, Object>) yaml.load(stream);
                    String language = (String) model.get("language");
                    for (Language l : ServiceLoader.load(Language.class)) {
                        if (l.supported().contains(language)) {
                            return l.build(model, request.getRepository());
                        }
                    }
                    throw new ProjectModelBuildingException("Unknown / Unsupported language: '" + language + "'");
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            }
        }
        throw new ProjectModelBuildingException("Not a YAML based literate project");
    }

  @Override
  public Set<String> getPossibleMarkerFiles(String baseName) {
    return new HashSet<String>(Arrays.asList("." + baseName + ".yml", ".travis.yml"));
  }


}
