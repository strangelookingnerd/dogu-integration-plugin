package io.jenkins.plugins.dogu.pipeline;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

public class DoguRoutinePipelineStep extends Step {
    private String projectId;
    private String routineId;
    private String credentialsId;

    @DataBoundConstructor
    public DoguRoutinePipelineStep(String projectId, String routineId, String credentialsId) {
        this.projectId = projectId;
        this.routineId = routineId;
        this.credentialsId = credentialsId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getRoutineId() {
        return routineId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new DoguRoutinePipelineStepExecution(this, context);
    }

    @Extension
    public static class StepDescriptorImpl extends StepDescriptor {
        public StepDescriptorImpl() {
            super();
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class, Launcher.class);
        }

        @Override
        public String getFunctionName() {
            return "doguRunRoutine";
        }

        @Override
        public String getDisplayName() {
            return "Dogu pipeline step";
        }
    }
}
