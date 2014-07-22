package org.cloudbees.literate.impl.yaml.environment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.cloudbees.literate.api.v1.ExecutionEnvironment;
import org.junit.Before;
import org.junit.Test;

public class MatrixEnvironmentDecoratorTest {
    MatrixEnvironmentDecorator instance;
    
    ExecutionEnvironment environment;
    
    @Before
    public void setUp() {
        instance=  new MatrixEnvironmentDecorator();
        environment = new ExecutionEnvironment();
    }

    @Test
    public void should_only_accept_section_matrix() {
        assertTrue(instance.acceptSection("matrix"));
        assertFalse(instance.acceptSection(""));
    }
    
    @Test
    public void one_envvar_should_generate_one_env(){
        Set<ExecutionEnvironment> result = instance.decorate(environment, Arrays.asList("A=b"));
        assertEquals(1, result.size());
    }
    
    @Test
    public void two_envvars_should_generate_two_env(){
        Set<ExecutionEnvironment> result = instance.decorate(environment, Arrays.asList("A=b", "A=c"));
        assertEquals(2, result.size());
        boolean bMatched = false;
        boolean cMatched = false;
        for (ExecutionEnvironment env : result) {
            Map<String, String> variables = env.getVariables();
            assertEquals(1, variables.size());
            String aValue = variables.get("A");
            if ("b".equals(aValue)) {
                if (bMatched) {
                    fail("There should be exactly ONE environment with A=b");
                } else {
                    bMatched = true;
                }
            } else if ("c".equals(aValue)) {
                if (cMatched) {
                    fail("There should be exactly ONE environment with A=c");
                } else {
                    cMatched = true;
                }
            } else {
                fail("Unmatched environment");
            }
        }
    }

}
