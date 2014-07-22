package org.cloudbees.literate.impl.yaml.environment;

import java.util.Collection;
import java.util.Set;

import org.cloudbees.literate.api.v1.ExecutionEnvironment;

/**
 * @author vlatombe
 * 
 * @since XXX
 */
public interface EnvironmentDecorator {
    /**
     * Decorate the given environment with the additional requests.
     * Several environments can be created from an initial one.
     * 
     * @param environment The environment to decorate
     * @param variables The collection of variables in the accepted section
     * @return a set of ExecutionEnvironment obtained from the given environment
     */
    public Set<ExecutionEnvironment> decorate(ExecutionEnvironment environment, Collection<String> variables);
    
    /**
     * Specifies the section name that is picked up by the decorator
     * 
     * @param sectionName The section name for which the decorator applies
     * @return
     */
    public boolean acceptSection(String sectionName);
}
