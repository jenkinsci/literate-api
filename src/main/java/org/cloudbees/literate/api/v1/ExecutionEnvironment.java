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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.jcip.annotations.Immutable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents an environment for execution of a series of commands.
 *
 * @author Stephen Connolly
 */
@Immutable
public class ExecutionEnvironment implements Serializable {

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
     * Default constructor of an empty environment.
     */
    public ExecutionEnvironment() {
        this.labels = Collections.<String>emptySet();
    }

    /**
     * Constructor.
     *
     * @param labels collection of labels that define the environment.
     */
    public ExecutionEnvironment(@CheckForNull Collection<String> labels) {
        this.labels = labels == null
                ? Collections.<String>emptySet()
                : Collections.unmodifiableSet(new TreeSet<String>(removeNulls(labels)));
    }

    /**
     * Constructor.
     *
     * @param labels the labels that define the environment.
     */
    public ExecutionEnvironment(String... labels) {
        this(Arrays.asList(labels));
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
        if (base != null) {
            labels.addAll(base.getLabels());
        }
        if (additionalLabels != null) {
            labels.addAll(removeNulls(additionalLabels));
        }
        this.labels = Collections.unmodifiableSet(labels);
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
     * Returns the labels that define the environment.
     *
     * @return the labels that define the environment.
     */
    @NonNull
    public Set<String> getLabels() {
        return labels;
    }

    /**
     * Returns {@code true} if the environment does not specify any labels.
     *
     * @return {@code true} if the environment does not specify any labels.
     */
    public boolean isUnspecified() {
        return labels.isEmpty();
    }

    /**
     * Tests if this environment satisfies the requirements of the specified environment.
     *
     * @param environment the specified environment.
     * @return {@code true} if and only if all the labels required by the specified environment are provided by this
     *         environment.
     */
    public boolean isMatchFor(ExecutionEnvironment environment) {
        return getLabels().containsAll(environment.getLabels());
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

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return labels.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExecutionEnvironment{");
        sb.append("labels=").append(labels);
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
     * Returns an environment without labels, that is it will match all other environments.
     *
     * @return an environment without labels.
     */
    public static ExecutionEnvironment any() {
        return ANY_EXECUTION_ENVIRONMENT;
    }
}
