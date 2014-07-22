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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.jcip.annotations.Immutable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Represents an environment for execution of a series of commands.
 *
 * @author Stephen Connolly
 */
@Immutable
public class ExecutionEnvironment implements Serializable, Comparable<ExecutionEnvironment> {

    /**
     * Ensure consistent serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Save allocation of the environment that has no labels.
     */
    private static final ExecutionEnvironment ANY_EXECUTION_ENVIRONMENT = new ExecutionEnvironment();

    /**
     * The labels of this execution environment.
     */
    @NonNull
    private final Set<String> labels;

    /**
     * Environment variables for this particular execution environment
     */
    @NonNull
    private final Map<String, String> variables;

    /**
     * Default constructor of an empty environment.
     */
    public ExecutionEnvironment() {
        this.labels = Collections.<String>emptySet();
        this.variables = Collections.emptyMap();
    }

    /**
     * Constructor.
     *
     * @param labels collection of labels that define the environment.
     * @param variables map of environment variables applicable to this environment
     */
    public ExecutionEnvironment(@CheckForNull Collection<String> labels, @CheckForNull Map<String, String> variables) {
        if (variables != null) { 
            this.labels = labels == null
                ? Collections.<String>emptySet()
                : Collections.unmodifiableSet(new TreeSet<String>(removeNulls(labels)));
            this.variables = Collections.unmodifiableMap(removeNulls(new TreeMap<String, String>(variables)));
        } else {
            Set<String> l = new TreeSet<String>();
            Map<String, String> v = new TreeMap<String, String>();
            for (String string : removeNulls(labels)) {
                String[] split = string.split("=", 2);
                if (split.length > 1) {
                    v.put(split[0], split[1]);
                } else {
                    l.add(string);
                }
            }
            this.labels = Collections.unmodifiableSet(l);
            this.variables = Collections.unmodifiableMap(v);
        }
    }

    /**
     * Constructor.
     *
     * @param labels collection of labels that define the environment.
     */
    public ExecutionEnvironment(@CheckForNull Collection<String> labels){
        this(labels, null);
    }

    /**
     * Constructor.
     *
     * @param labels the labels that define the environment.
     */
    public ExecutionEnvironment(String... labels) {
        this(Arrays.asList(labels), null);
    }

    /**
     * Constructor.
     *
     * @param base             a base {@link ExecutionEnvironment} to be extended by the additional labels
     * @param additionalLabels collection of labels that define the environment.
     */
    public ExecutionEnvironment(@CheckForNull ExecutionEnvironment base,
                                @CheckForNull Collection<String> additionalLabels) {
        Set<String> labels = new TreeSet<String>();
        Map<String, String> variables = new HashMap<String, String>();
        if (base != null) {
            labels.addAll(base.getLabels());
            variables.putAll(base.getVariables());
        }
        if (additionalLabels != null) {
            labels.addAll(removeNulls(additionalLabels));
        }
        this.labels = Collections.unmodifiableSet(labels);
        this.variables = Collections.unmodifiableMap(variables);
    }

    /**
     * Constructor.
     *
     * @param base             a base {@link ExecutionEnvironment} to be extended by the additional labels
     * @param additionalLabels collection of labels that define the environment.
     */
    public ExecutionEnvironment(@CheckForNull ExecutionEnvironment base, String... additionalLabels) {
        this(base, Arrays.asList(additionalLabels));
    }

    /**
     * Returns the components (labels, variables) that define the environment.
     * 
     * @return the components (labels, variables) that define the environment.
     */
    public Set<String> getComponents() {
      return Sets.union(labels, getVariablesAsSet());
    }

    /**
     * Returns the labels that define the environment.
     *
     * @return the labels that define the environment.
     */
    @NonNull
    public Set<String> getLabels() {
        return labels;
    }
    
    @NonNull
    public String getDescription() {
        return join("[",Joiner.on(',').join(getComponents()),"]");
    }

    @NonNull
    private String join(String... str) {
        StringBuilder sb = new StringBuilder();
        for (String string : str) {
            sb.append(string);
        }
        return sb.toString();
    }

    /**
     * Returns the environment variables that define the environment
     *
     * @return the environment variables that are applicable to this environment
     */
    public Map<String, String> getVariables() {
        return variables;
    }
    
    private Set<String> getVariablesAsSet() {
        Set<String> result = Sets.newTreeSet();
        for (Entry<String, String> entry : variables.entrySet()) {
          result.add(entry.getKey() + "=" + entry.getValue());
        }
        return result;
    }

    /**
     * Returns {@code true} if the environment does not specify any labels nor environment variables.
     *
     * @return {@code true} if the environment does not specify any labels nor environment variables.
     */
    public boolean isUnspecified() {
        return labels.isEmpty() && variables.isEmpty();
    }

    /**
     * Tests if this environment satisfies the requirements of the specified environment.
     *
     * @param environment the specified environment.
     * @return {@code true} if and only if all the labels required by the specified environment are provided by this
     *         environment.
     */
    public boolean isMatchFor(ExecutionEnvironment environment) {
        return getLabels().containsAll(environment.getLabels()) &&
                Maps.difference(getVariables(), environment.getVariables()).entriesOnlyOnRight().isEmpty();
    }

    /**
     * Returns a new {@link ExecutionEnvironment} instance complemented with the given environment variables
     * @param variables the environment variables to add to the execution environment. The values will override existing environment variables if applicable
     * @return a new {@link ExecutionEnvironment} instance complemented with the given environment variables
     */
    public ExecutionEnvironment withVariables(Map<String, String> variables) {
        HashMap<String, String> newMap = new HashMap<String, String>(this.variables);
        newMap.putAll(variables);
        return new ExecutionEnvironment(labels, newMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExecutionEnvironment)) {
            return false;
        }

        ExecutionEnvironment that = (ExecutionEnvironment) o;

        if (!labels.equals(that.labels)) {
            return false;
        }

        if (!variables.equals(that.variables)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification="It might be null after a deserialization of an old version")
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + labels.hashCode();
        result = prime * result + (variables == null ? 0 : variables.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExecutionEnvironment{");
        sb.append("labels=").append(labels);
        sb.append(",variables=").append(variables);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Removes any {@code null} values from a collection (returns a copy, original remains unmodified).
     *
     * @param collection the collection.
     * @param <T>        the type of elements in the collection.
     * @return either the original collection if {@code null} was not found or a copy builder all {@code null} values
     *         removed.
     */
    private static <T> Collection<T> removeNulls(Collection<T> collection) {
        try {
            if (!collection.contains(null)) {
                return collection;
            }
        } catch (NullPointerException e) {
            // collection does not permit null, so safe to return unmodified.
            return collection;
        }
        List<T> result = new ArrayList<T>();
        for (T t : collection) {
            if (t != null) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Removes any {@code null} values from a map (returns a copy, original remains unmodified).
     *
     * @param map the map.
     * @param <K>        the type of key in the map.
     * @param <V>        the type of value in the map.
     * @return either the original map if {@code null} was not found or a copy builder all {@code null} values
     *         removed.
     */
    private static <K,V> Map<K,V> removeNulls(Map<K,V> map) {
        try {
        if (!map.containsValue(null)) {
            return map;
        }
        } catch (NullPointerException e) {
         // map does not permit null, so safe to return unmodified.
            return map;
        }
        Map<K,V> result = new HashMap<K,V>();
        for (Entry<K, V> entry : result.entrySet()) {
            if (entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Returns an environment without labels, that is it will match all other environments.
     *
     * @return an environment without labels.
     */
    public static ExecutionEnvironment any() {
        return ANY_EXECUTION_ENVIRONMENT;
    }

    @Override
    public int compareTo(ExecutionEnvironment o) {
        if (o == null) return 1;
        return toString().compareTo(o.toString());
    }

}
