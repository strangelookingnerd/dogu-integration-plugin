package io.jenkins.plugins.dogu.pipeline;

import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.Secret;
import io.jenkins.plugins.dogu.DoguOption;
import io.jenkins.plugins.dogu.common.DoguApi;
import io.jenkins.plugins.dogu.common.DoguUtils;
import jakarta.annotation.Nonnull;
import java.io.PrintStream;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

public class DoguRoutinePipelineStepExecution extends StepExecution {
    private static final long serialVersionUID = 2L;
    private transient DoguRoutinePipelineStep step;

    @DataBoundConstructor
    public DoguRoutinePipelineStepExecution(DoguRoutinePipelineStep step, StepContext context) {
        super(context);
        this.step = step;
    }

    @Override
    public boolean start() throws Exception {
        String projectId = step.getProjectId();
        String routineId = step.getRoutineId();
        String credentialsId = step.getCredentialsId();

        Run<?, ?> build = getContext().get(Run.class);
        TaskListener listener = getContext().get(TaskListener.class);
        PrintStream logger = listener.getLogger();
        Secret accessTokenSecret = DoguUtils.getAccessToken(credentialsId);

        String apiUrl = DoguUtils.getApiUrl(build, listener, logger);
        DoguOption doguOption = new DoguOption(accessTokenSecret, apiUrl);

        try {
            boolean routineResult = DoguApi.runRoutine(projectId, routineId, doguOption, logger);
            getContext().onSuccess("Success");
            return routineResult;
        } catch (Exception e) {
            e.printStackTrace(logger);
            getContext().onFailure(e);
            return false;
        }
    }

    @Override
    public void stop(@Nonnull Throwable cause) throws Exception {}
}
