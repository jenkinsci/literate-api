/*
 * The MIT License
 *
 * Copyright (c) 2014, CloudBees, Inc.
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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This represents a parameter that should be exposed to some {@link AbstractCommands}
 *
 * @since 0.6
 */
public class Parameter implements Serializable {
    /**
     * Ensure consistent serialization.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The name of the parameter. An environment variable with this name will be set in the command context if a value
     * is supplied or if there is a {@link #defaultValue}.
     */
    @NonNull
    private final String name;
    /**
     * A human readable description of the parameter for use if prompting the user for a value.
     */
    @CheckForNull
    private final String description;
    /**
     * An optional default value to use if no value is provided by the user or if no user is present to provide a value.
     */
    @CheckForNull
    private final String defaultValue;
    /**
     * An optional list of valid values that the parameter may take, if {@code null} then there is no
     * a restriction to a finite set.
     */
    @CheckForNull
    private final List<String> validValues;

    /**
     * Constructs a new parameter.
     *
     * @param name         the name of the parameter.
     * @param description  a human readable description of the parameter.
     * @param defaultValue an optional default value.
     */
    public Parameter(@NonNull String name, @CheckForNull String description, @CheckForNull String defaultValue,
                     @CheckForNull Collection<String> validValues) {
        name.getClass(); // throw NPE if null
        this.name = name;
        this.description = description == null || description.trim().length() == 0 ? null : description;
        this.defaultValue = defaultValue;
        if (validValues == null || validValues.isEmpty()
                || (defaultValue != null && validValues.size() == 1
                && defaultValue.equals(validValues.iterator().next()))) {
            this.validValues = null;
        } else {
            this.validValues = new ArrayList<String>(validValues);
        }
    }

    /**
     * Utility method to convert a collection of {@link Parameter} instances into a {@link Map} keyed by
     * {@link org.cloudbees.literate.api.v1.Parameter#getName()}. If the supplied collection has multiple parameters
     * with the same name, the first entry wins.
     *
     * @param parameters the collection of parameters.
     * @return a {@link Map} keyed by {@link org.cloudbees.literate.api.v1.Parameter#getName()}
     */
    @NonNull
    public static Map<String, Parameter> toMap(@CheckForNull Iterable<Parameter> parameters) {
        Map<String, Parameter> result = new LinkedHashMap<String, Parameter>();
        if (parameters != null) {
            for (Parameter p : parameters) {
                if (p == null) {
                    continue;
                }
                if (!result.containsKey(p.getName())) {
                    result.put(p.getName(), p);
                }
            }
        }
        return result;
    }

    /**
     * Utility method to convert a {@link Map} of {@link Parameter}s into a {@link List}.
     *
     * @param parameters the map of parameters.
     * @return a {@link List} of {@link Parameter} instances.
     */
    @NonNull
    public static List<Parameter> toList(@CheckForNull Map<?, Parameter> parameters) {
        return parameters == null ? new ArrayList<Parameter>() : new ArrayList<Parameter>(parameters.values());
    }

    /**
     * Returns the name of the parameter. An environment variable with this name will be set in the command context
     * if a value
     * is supplied or if there is a {@link #defaultValue}.
     *
     * @return the name of the parameter. An environment variable with this name will be set in the command context
     *         if a value
     *         is supplied or if there is a {@link #defaultValue}.
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Returns a human readable description of the parameter for use if prompting the user for a value.
     *
     * @return A human readable description of the parameter for use if prompting the user for a value.
     */
    @CheckForNull
    public String getDescription() {
        return description;
    }

    /**
     * Returns an optional default value to use if no value is provided by the user or if no user is present to
     * provide a value.
     *
     * @return An optional default value to use if no value is provided by the user or if no user is present to
     *         provide a value.
     */
    @CheckForNull
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns an optional list of valid values that the parameter may take, if {@code null} then there is no
     * a restriction to a finite set.
     *
     * @return an optional list of valid values that the parameter may take, if {@code null} then there is no
     *         a restriction to a finite set.
     */
    @CheckForNull
    public List<String> getValidValues() {
        return validValues == null ? null : Collections.unmodifiableList(validValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Parameter{");
        sb.append("name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", defaultValue='").append(defaultValue).append('\'');
        sb.append(", validValues=").append(validValues);
        sb.append('}');
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Parameter parameter = (Parameter) o;

        if (!name.equals(parameter.name)) {
            return false;
        }
        if (defaultValue != null ? !defaultValue.equals(parameter.defaultValue) : parameter.defaultValue != null) {
            return false;
        }
        if (description != null ? !description.equals(parameter.description) : parameter.description != null) {
            return false;
        }
        if (validValues != null ? !validValues.equals(parameter.validValues) : parameter.validValues != null) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
