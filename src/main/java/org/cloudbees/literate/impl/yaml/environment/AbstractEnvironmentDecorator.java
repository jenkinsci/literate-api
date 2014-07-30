package org.cloudbees.literate.impl.yaml.environment;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEnvironmentDecorator {

    public AbstractEnvironmentDecorator() {
        super();
    }

    /**
     * Parses a string that looks like KEY=VALUE KEY2=VALUE into a proper map
     * 
     * @param s
     *            the input string
     * @return a map containing the properties in structured mode
     */
    protected Map<String, String> parseProperties(String s) {
        Map<String, String> result = new HashMap<String, String>();
        String[] split = s.split(" ");
        for (String string : split) {
            String[] split2 = string.split("=", 2);
            assert split2.length == 2 : "Properties must have format KEY=VALUE";
            result.put(split2[0], split2[1]);
        }
        return result;
    }

}