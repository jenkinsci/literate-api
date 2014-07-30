package org.cloudbees.literate.impl.yaml.environment;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cloudbees.literate.api.v1.ExecutionEnvironment;

/**
 * Generate matrix environments from given variables
 */
public class MatrixEnvironmentDecorator extends AbstractEnvironmentDecorator implements EnvironmentDecorator {

    @Override
    public Set<ExecutionEnvironment> decorate(ExecutionEnvironment environment, Collection<String> variables) {
        Set<ExecutionEnvironment> envs = new HashSet<ExecutionEnvironment>();
        for (String string : variables) {
            Map<String, String> properties = parseProperties(string);
            envs.add(environment.withVariables(properties));
        }
        return envs;
    }

    @Override
    public boolean acceptSection(String sectionName) {
        return "matrix".equals(sectionName);
    }

}
