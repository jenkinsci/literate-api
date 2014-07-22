package org.cloudbees.literate.impl.yaml.environment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cloudbees.literate.api.v1.ExecutionEnvironment;

/**
 * Decorate the given execution environment with global environment variables
 */
public class GlobalEnvironmentDecorator extends AbstractEnvironmentDecorator implements EnvironmentDecorator {

    @Override
    public Set<ExecutionEnvironment> decorate(ExecutionEnvironment environment, Collection<String> variables) {
        Map<String, String> result = new HashMap<String, String>();
        for (String s : variables) {
            result.putAll(parseProperties(s));
        }
        return Collections.singleton(environment.withVariables(result));
    }

    @Override
    public boolean acceptSection(String sectionName) {
        return "global".equals(sectionName);
    }

}
