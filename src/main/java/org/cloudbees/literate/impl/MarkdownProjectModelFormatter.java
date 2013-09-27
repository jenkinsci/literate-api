package org.cloudbees.literate.impl;

import org.cloudbees.literate.api.v1.ExecutionEnvironment;
import org.cloudbees.literate.api.v1.ProjectModel;
import org.cloudbees.literate.api.v1.ProjectModelFormatter;
import org.cloudbees.literate.api.v1.TaskCommands;

import java.util.List;
import java.util.Map;

/**
 * @author Stephen Connolly
 */
public class MarkdownProjectModelFormatter extends ProjectModelFormatter {

    /**
     * Default constructor.
     */
    public MarkdownProjectModelFormatter() {
        super(MARKDOWN);
    }

    @Override
    public String format(ProjectModel model) {
        StringBuilder result = new StringBuilder();
        result.append("# Project Name\n");
        result.append("\n");
        result.append("# Environments\n");
        for (ExecutionEnvironment environment : model.getEnvironments()) {
            boolean first = true;
            for (String label : environment.getLabels()) {
                result.append(first ? "\n* " : ", ");
                result.append('`').append(label).append('`');
                first = false;
            }
            if (!first) {
                result.append("\n");
            }
        }
        result.append("\n");
        result.append("# Build\n");
        result.append("\n");
        for (Map.Entry<ExecutionEnvironment, List<String>> entry : model.getBuild().getCommands().entrySet()) {
            boolean first = true;
            for (String env : entry.getKey().getLabels()) {
                result.append(first ? "* On " : ", ");
                result.append('`').append(env).append('`');
                first = false;
            }
            if (!first) {
                result.append("\n\n");
            }
            boolean needBreakingText = false;
            for (String command : entry.getValue()) {
                if (needBreakingText) {
                    result.append(first ? "Then\n\n" : "    Then\n\n");
                } else {
                    needBreakingText = true;
                }
                result.append(first ? "    " : "        ")
                        .append(command.replace("\n", first ? "\n    " : "\n        "))
                        .append("\n\n");
            }
        }
        for (String taskId : model.getTaskIds()) {
            TaskCommands task = model.getTask(taskId);
            if (task == null) {
                continue;
            }
            result.append("\n");
            result.append("# ").append(Character.toUpperCase(taskId.charAt(0)))
                    .append(taskId.substring(1)).append("\n");
            result.append("\n");
            boolean needBreakingText = false;
            for (String command : task.getCommand()) {
                if (needBreakingText) {
                    result.append("Then\n\n");
                } else {
                    needBreakingText = true;
                }
                result.append("    ")
                        .append(command.replace("\n", "\n    "))
                        .append("\n\n");
            }

        }

        return result.toString();
    }
}
