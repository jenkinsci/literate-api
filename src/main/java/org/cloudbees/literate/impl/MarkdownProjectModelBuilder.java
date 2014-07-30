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

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.IOUtils;
import org.cloudbees.literate.api.v1.ExecutionEnvironment;
import org.cloudbees.literate.api.v1.Parameter;
import org.cloudbees.literate.api.v1.ProjectModel;
import org.cloudbees.literate.api.v1.ProjectModelBuildingException;
import org.cloudbees.literate.api.v1.ProjectModelRequest;
import org.cloudbees.literate.api.v1.ProjectModelValidationException;
import org.cloudbees.literate.api.v1.vfs.ProjectRepository;
import org.cloudbees.literate.spi.v1.ProjectModelBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.core.SubstringMatcher;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.BulletListNode;
import org.pegdown.ast.CodeNode;
import org.pegdown.ast.DefinitionListNode;
import org.pegdown.ast.DefinitionNode;
import org.pegdown.ast.DefinitionTermNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.ListItemNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import static org.cloudbees.literate.impl.MarkdownProjectModelBuilder.StringContainsIgnoreCase.containsStringIgnoreCase;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;

/**
 * A {@link ProjectModelBuilder} that uses a Markdown file as the source of its {@link ProjectModel}
 *
 * @todo finish documenting this hairy code.
 */
@ProjectModelBuilder.Priority(Integer.MAX_VALUE)
public class MarkdownProjectModelBuilder implements ProjectModelBuilder {
    /**
     * The {@link PegDownProcessor} extension flags to match GitHub's Markdown rules.
     */
    private static final int GITHUB = Extensions.AUTOLINKS + Extensions.FENCED_CODE_BLOCKS + Extensions.HARDWRAPS
            + Extensions.DEFINITIONS;

    public static String getText(Node node) {
        return getTextUntil(node, null);
    }

    public static String getTextUntil(Node node, Node until) {
        StringBuilder builder = new StringBuilder();
        if (node == until) {
            // no-op
        } else if (node instanceof TextNode) {
            builder.append(TextNode.class.cast(node).getText());
        } else {
            for (Node n : node.getChildren()) {
                if (n == until) {
                    break;
                }
                if (n instanceof TextNode) {
                    builder.append(TextNode.class.cast(n).getText());
                } else if (n instanceof SuperNode) {
                    builder.append(getText(n));
                }
            }
        }
        return builder.toString();
    }

    public static HeaderNode asHeaderNode(Node node) {
        return HeaderNode.class.cast(node);
    }

    /**
     * {@inheritDoc}
     */
    //@Override
    public ProjectModel build(ProjectModelRequest request) throws IOException, ProjectModelBuildingException {
        for (String name : markerFiles(request.getBaseName())) {
            if (request.getRepository().isFile(name)) {
                return new Parser(request).parseProjectModel(request.getRepository(), name);
            }
        }
        throw new ProjectModelBuildingException("Not a Markdown based literate project");
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    public Collection<String> markerFiles(@NonNull String basename) {
        return Collections.singleton("." + basename + ".md");
    }

    /**
     * The source parser.
     */
    private static class Parser {

        private static final String FALLBACK_FILE = "README.md";
        /**
         * Matches header nodes.
         */
        private static final Matcher<Node> isHeader = instanceOf(HeaderNode.class);
        /**
         * Matches the root node.
         */
        private static final Matcher<Node> isRoot = instanceOf(RootNode.class);
        /**
         * Matches paragraphs of text.
         */
        private static final Matcher<Node> isPara = instanceOf(ParaNode.class);
        /**
         * Matches super nodes.
         */
        private static final Matcher<Node> isSuper = instanceOf(SuperNode.class);
        /**
         * Matches code blocks.
         */
        private static final Matcher<Node> isCode = instanceOf(CodeNode.class);
        /**
         * Matches a node that has code blocks.
         */
        private static final Matcher<Node> hasCode = new WithDescendant(isCode);
        /**
         * Matches list items.
         */
        private static final Matcher<Node> isItem = allOf(instanceOf(ListItemNode.class), new WithChild(isRoot));
        /**
         * Matches bullet points.
         */
        private static final Matcher<Node> isBullet = allOf(instanceOf(BulletListNode.class), new WithChild(isItem));
        /**
         * Matches a node that has a bullet point descendant.
         */
        private static final Matcher<Node> hasBullet = new WithDescendant(isBullet);
        /**
         * Matches a verbatim block.
         */
        private static final Matcher<Node> isVerbatim = instanceOf(VerbatimNode.class);
        /**
         * Matches a node that has a verbatim descendant.
         */
        private static final Matcher<Node> hasVerbatim = new WithDescendant(isVerbatim);
        /**
         * Matches a node that is the term in a definition list.
         */
        private static final Matcher<Node> isDefinitionTerm = instanceOf(DefinitionTermNode.class);
        /**
         * Matches a node that is the definition in a definition list.
         */
        private static final Matcher<Node> isDefinition = instanceOf(DefinitionNode.class);
        /**
         * Matches a node that is a definition list.
         */
        private static final Matcher<Node> isDefinitionList = allOf(instanceOf(DefinitionListNode.class),
                new WithChild(isDefinitionTerm), new WithChild(isDefinition));
        /**
         * Matches the environments section header.
         */
        private final Matcher<Node> isEnvHeader;
        /**
         * Matches the build section header.
         */
        private final Matcher<Node> isBuildHeader;
        /**
         * Matchers for the task section headers.
         */
        private final Map<String, Matcher<Node>> isTaskHeader;
        private final int minLength;

        /**
         * Makes the parser.
         *
         * @param request the request to parse.
         */
        private Parser(ProjectModelRequest request) {
            minLength = "#".length() + request.getBuildId().length() + "\n    a".length();
            isEnvHeader = allOf(isHeader, new WithText(containsStringIgnoreCase(request.getEnvironmentsId())));
            isBuildHeader = allOf(isHeader, new WithText(containsStringIgnoreCase(request.getBuildId())));
            Map<String, Matcher<Node>> isTaskHeader = new LinkedHashMap<String, Matcher<Node>>();
            for (String taskId : request.getTaskIds()) {
                isTaskHeader.put(taskId, CoreMatchers.<Node>allOf(
                        isHeader, new WithText(containsStringIgnoreCase(taskId)))
                );
            }
            this.isTaskHeader = isTaskHeader;
        }

        /**
         * Parses the model.
         *
         * @param repository the repository.
         * @param filePath   the file to parse.
         * @return the model.
         * @throws IOException when things go wrong.
         */
        private ProjectModel parseProjectModel(ProjectRepository repository, String filePath)
                throws IOException, ProjectModelValidationException {
            InputStream stream = repository.get(filePath);
            try {
                char[] chars = IOUtils.toCharArray(stream);
                RootNode document = chars.length < minLength ? null : new PegDownProcessor(GITHUB).parseMarkdown(chars);
                ProjectModel.Builder builder = ProjectModel.builder();
                if (document != null && !document.getChildren().isEmpty()) {
                    Iterator<Node> iterator = document.getChildren().iterator();

                    consumeEnvironmentSection(iterator, builder);

                    iterator = document.getChildren().iterator();
                    if (discardTo(iterator, isBuildHeader)) {
                        consumeBuild(iterator, builder);
                    }

                    for (Map.Entry<String, Matcher<Node>> entry : isTaskHeader.entrySet()) {
                        iterator = document.getChildren().iterator();
                        if (discardTo(iterator, entry.getValue())) {
                            consumeTask(iterator, builder, entry.getKey());
                        }
                    }
                }
                ProjectModel model;
                boolean isFallbackFile = FALLBACK_FILE.equals(filePath);
                try {
                    model = builder.build();
                } catch (ProjectModelBuildingException e) {
                    if (!isFallbackFile) {
                        model = null;
                    } else {
                        throw new ProjectModelValidationException("Unable to turn " + filePath + " into a valid model", e);
                    }
                }
                if (model == null || model.getBuild().getCommands().isEmpty() && model.getTaskIds().isEmpty()) {
                    if (!isFallbackFile && repository.isFile(FALLBACK_FILE)) {
                        // try the fall-back
                        return parseProjectModel(repository, FALLBACK_FILE);
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unable to turn " + filePath + " into a valid model. Please check that it contains a valid build section.\n");
                    sb.append("Valid build sections include :\n");
                    sb.append("- verbatim (starts by 4 spaces or tab)\n");
                    sb.append("- bullet list (starts by *, +, -, or a number)\n");
                    sb.append("- definition list");
                    throw new ProjectModelValidationException(sb.toString());
                }
                return model;
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }

        private void consumeBuild(Iterator<Node> iterator, ProjectModel.Builder builder) {
            while (iterator.hasNext()) {
                Node node = iterator.next();
                if (isHeader.matches(node)) {
                    break;
                }
                if (isVerbatim.matches(node)) {
                    builder.addBuild(getText(node));
                }
                if (isBullet.matches(node)) {
                    builder.addBuild(parseBuild(node.getChildren()));
                }
                if (isDefinitionList.matches(node)) {
                    builder.addBuildParameters(parseDefinitions(node.getChildren()));
                }
            }
        }

        private List<Parameter> parseDefinitions(List<Node> children) {
            ArrayList<Parameter> result = new ArrayList<Parameter>();
            DefinitionTermNode term = null;
            for (Node node : children) {
                if (isDefinitionTerm.matches(node)) {
                    term = (DefinitionTermNode) node;
                }
                if (isDefinition.matches(node) && term != null) {
                    String name = getText(term);
                    String defaultValue = null;
                    Set<String> validValues = null;
                    if (hasCode.matches(node)) {
                        Stack<Iterator<Node>> stack = new Stack<Iterator<Node>>();
                        stack.push(node.getChildren().iterator());
                        while (!stack.isEmpty()) {
                            Iterator<Node> i = stack.pop();
                            while (i.hasNext()) {
                                Node c = i.next();
                                if (isCode.matches(c)) {
                                    String text = getText(c);
                                    if (defaultValue == null) {
                                        defaultValue = text;
                                    } else if (validValues == null) {
                                        validValues = new LinkedHashSet<String>();
                                        validValues.add(defaultValue);
                                        validValues.add(text);
                                    } else {
                                        validValues.add(text);
                                    }
                                } else if (!c.getChildren().isEmpty()) {
                                    stack.push(i);
                                    i = c.getChildren().iterator();
                                }
                            }
                        }
                    }
                    if (!name.isEmpty()) {
                        result.add(new Parameter(name, getText(node), defaultValue, validValues));
                    }
                    term = null;
                }
            }
            return result;
        }

        private void consumeTask(Iterator<Node> iterator, ProjectModel.Builder builder, String taskId) {
            while (iterator.hasNext()) {
                Node node = iterator.next();
                if (isHeader.matches(node)) {
                    break;
                }
                if (isVerbatim.matches(node)) {
                    builder.addTask(taskId.toLowerCase(), getText(node));
                }
                if (isBullet.matches(node)) {
                    // discard
                }
                if (isDefinitionList.matches(node)) {
                    builder.addTaskParameters(taskId.toLowerCase(), parseDefinitions(node.getChildren()));
                }

            }
        }

        /**
         * eat up all the stuff until we reach the matcher's match
         */
        private boolean discardTo(Iterator<Node> iterator, Matcher<Node> matcher) {
            while (iterator.hasNext()) {
                if (matcher.matches(iterator.next())) {
                    return true;
                }
            }
            return false;
        }

        private void consumeEnvironmentSection(Iterator<Node> iterator, ProjectModel.Builder builder) {
            if (discardTo(iterator, isEnvHeader)) {
                while (iterator.hasNext()) {
                    Node node = iterator.next();
                    if (isHeader.matches(node)) {
                        break;
                    }
                    if (isBullet.matches(node)) {
                        builder.addEnvironments(parseEnvironments(node.getChildren()));
                    }
                }
            }
        }

        private Map<ExecutionEnvironment, List<String>> parseBuild(List<Node> children) {
            Map<ExecutionEnvironment, List<String>> result = new LinkedHashMap<ExecutionEnvironment, List<String>>();
            for (Node node : children) {
                if (isItem.matches(node)) {
                    result.putAll(parseBuild(node));
                }
            }
            return result;
        }

        private Map<ExecutionEnvironment, List<String>> parseBuild(Node listItem) {
            if (hasVerbatim.matches(listItem)) {
                Set<String> labels = new TreeSet<String>();
                List<String> cmd = new ArrayList<String>();
                for (Node root : listItem.getChildren()) {
                    if (isVerbatim.matches(root)) {
                        cmd.add(getText(root));
                    }
                    if (isRoot.matches(root) || isPara.matches(root)) {
                        for (Node child : root.getChildren()) {
                            if (isVerbatim.matches(child)) {
                                cmd.add(getText(child));
                            } else if (isPara.matches(child)) {
                                for (Node node : child.getChildren()) {
                                    if (isSuper.matches(node)) {
                                        for (Node n : node.getChildren()) {
                                            if (isVerbatim.matches(n)) {
                                                cmd.add(getText(child));
                                            } else if (isCode.matches(n)) {
                                                labels.add(getText(n));
                                            }
                                        }
                                    }
                                }
                            } else if (isSuper.matches(child)) {
                                for (Node node : child.getChildren()) {
                                    if (isVerbatim.matches(child)) {
                                        cmd.add(getText(node));
                                    } else if (isCode.matches(node)) {
                                        labels.add(getText(node));
                                    }
                                }
                            }
                        }
                    }
                }
                return Collections.singletonMap(new ExecutionEnvironment(labels), cmd);
            }
            return Collections.emptyMap();
        }

        private List<ExecutionEnvironment> parseEnvironments(Node listItem) {
            Set<String> toAll = new TreeSet<String>();
            List<ExecutionEnvironment> environments = new ArrayList<ExecutionEnvironment>();
            for (Node root : listItem.getChildren()) {
                if (isRoot.matches(root) || isPara.matches(root)) {
                    for (Node child : root.getChildren()) {
                        if (isBullet.matches(child)) {
                            environments.addAll(parseEnvironments(child.getChildren()));
                        } else if (isPara.matches(child)) {
                            for (Node node : child.getChildren()) {
                                if (isSuper.matches(node)) {
                                    for (Node n : node.getChildren()) {
                                        if (isCode.matches(n)) {
                                            toAll.add(getText(n));
                                        }
                                    }
                                }
                            }
                        } else if (isSuper.matches(child)) {
                            for (Node node : child.getChildren()) {
                                if (isCode.matches(node)) {
                                    toAll.add(getText(node));
                                }
                            }
                        }
                    }
                }
            }
            List<ExecutionEnvironment> result = new ArrayList<ExecutionEnvironment>(Math.max(1, environments.size()));
            if (environments.isEmpty()) {
                result.add(new ExecutionEnvironment(toAll));
            } else {
                for (ExecutionEnvironment environment : environments) {
                    result.add(new ExecutionEnvironment(environment, toAll));
                }
            }
            return result;
        }

        private List<ExecutionEnvironment> parseEnvironments(List<Node> listItems) {
            List<ExecutionEnvironment> result = new ArrayList<ExecutionEnvironment>();
            for (Node node : listItems) {
                if (isItem.matches(node)) {
                    result.addAll(parseEnvironments(node));
                }
            }
            return result;
        }
    }

    private static class WithChild extends BaseMatcher<Node> {
        private final Matcher<? super Node> childMatcher;

        private WithChild(Matcher<? super Node> childMatcher) {
            this.childMatcher = childMatcher;
        }

        //@Override
        public boolean matches(Object item) {
            if (!(item instanceof Node)) {
                return false;
            }
            for (Node child : ((Node) item).getChildren()) {
                if (childMatcher.matches(child)) {
                    return true;
                }
            }
            return false;
        }

        //@Override
        public void describeTo(Description description) {
            description.appendText("with a child node matching ").appendDescriptionOf(childMatcher);
        }
    }

    private static class WithDescendant extends BaseMatcher<Node> {
        private final Matcher<? super Node> childMatcher;

        private WithDescendant(Matcher<? super Node> childMatcher) {
            this.childMatcher = childMatcher;
        }

        //@Override
        public boolean matches(Object item) {
            if (!(item instanceof Node)) {
                return false;
            }
            for (Node child : ((Node) item).getChildren()) {
                if (childMatcher.matches(child) || this.matches(child)) {
                    return true;
                }
            }
            return false;
        }

        //@Override
        public void describeTo(Description description) {
            description.appendText("with a descendant node matching ").appendDescriptionOf(childMatcher);
        }
    }

    private static class WithText extends BaseMatcher<Node> {
        private final Matcher<? super String> textMatcher;

        private WithText(Matcher<? super String> matcher) {
            textMatcher = matcher;
        }

        //@Override
        public boolean matches(Object item) {
            if (!(item instanceof Node)) {
                return false;
            }
            StringBuilder builder = new StringBuilder();
            Node node = (Node) item;
            if (node instanceof TextNode) {
                builder.append(TextNode.class.cast(node).getText());
            } else {
                for (Node n : node.getChildren()) {
                    if (n instanceof TextNode) {
                        builder.append(TextNode.class.cast(n).getText());
                    } else if (n instanceof SuperNode) {
                        builder.append(getText(SuperNode.class.cast(n)));
                    }
                }
            }
            return textMatcher.matches(builder.toString());
        }

        //@Override
        public void describeTo(Description description) {
            description.appendText("with text matching ").appendDescriptionOf(textMatcher);
        }
    }

    public static class StringContainsIgnoreCase extends SubstringMatcher {
        public StringContainsIgnoreCase(String substring) {
            super(substring.toLowerCase());
        }

        /**
         * Creates a matcher that matches if the examined {@link String} contains the specified
         * {@link String} anywhere.
         * <p/>
         * For example:
         * <pre>assertThat("myStringOfNote", containsString("ring"))</pre>
         *
         * @param substring the substring that the returned matcher will expect to find within any examined string
         */
        @Factory
        public static Matcher<String> containsStringIgnoreCase(String substring) {
            return new StringContainsIgnoreCase(substring);
        }

        @SuppressWarnings("IndexOfReplaceableByContains")
        @Override
        protected boolean evalSubstringOf(String s) {
            return s.toLowerCase().indexOf(substring) >= 0;
        }

        @Override
        protected String relationship() {
            return "containing (ignore case)";
        }

    }
}
