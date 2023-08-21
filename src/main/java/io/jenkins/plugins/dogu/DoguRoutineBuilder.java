package io.jenkins.plugins.dogu;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.dogu.api.ApiClient;
import io.jenkins.plugins.dogu.api.ApiClient.RunRoutineResponse;
import io.jenkins.plugins.dogu.api.DoguWebSocketClient;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class DoguRoutineBuilder extends Builder {

    private final String projectId;
    private final String routineId;
    private final String credentialsId;

    @DataBoundConstructor
    public DoguRoutineBuilder(String projectId, String routineId, String credentialsId) {
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
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        PrintStream logger = listener.getLogger();

        DoguCredential credential = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        DoguCredential.class, Jenkins.get(), ACL.SYSTEM, Collections.<DomainRequirement>emptyList()),
                CredentialsMatchers.withId(credentialsId));

        Secret accessTokenSecret;
        if (credential == null) {
            logger.println("Failed to find credentials with ID: " + credentialsId);
            return false;
        } else {
            accessTokenSecret = credential.getSecret();
        }

        String apiUrl = "https://api.dogutech.io";
        try {
            EnvVars envVars = build.getEnvironment(listener);
            envVars.overrideAll(build.getBuildVariables());

            String customApiUrl = envVars.get("DOGU_API_URL");

            if (customApiUrl != null) {
                apiUrl = customApiUrl;
            }

            logger.println("API URL: " + apiUrl);
        } catch (IOException | InterruptedException e) {
        }

        DoguOption doguOption = new DoguOption(accessTokenSecret, apiUrl);

        RunRoutineResponse routine;
        try {
            routine = ApiClient.runRoutine(projectId, routineId, doguOption);
            logger.println("Spawn pipeline, project-id: " + projectId + ", routine-id: " + routineId
                    + " routine-pipeline-id: " + routine.routinePipelineId);
        } catch (Exception e) {
            logger.println("Error: " + e.getMessage());
            e.printStackTrace(logger);
            return false;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
                DoguWebSocketClient client;

                try {
                    client = ApiClient.connectRoutine(
                            logger, projectId, routineId, routine.routinePipelineId, doguOption);
                } catch (Exception e) {
                    logger.println("Error: " + e.getMessage());
                    e.printStackTrace(logger);
                    return 1;
                }

                while (true) {
                    if (Thread.currentThread().isInterrupted()) {
                        return 1;
                    }

                    try {
                        switch (client.state) {
                            case NONE:
                                break;
                            case SUCCESS:
                                logger.println("Success");
                                return 0;
                            case FAILURE:
                                logger.println("Failure");
                                return 1;
                        }

                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        client.close(1006);
                        return 1;
                    }
                }
            }
        });

        int result;
        try {
            result = future.get();
        } catch (InterruptedException e) {
            logger.println("Cancelled");
            future.cancel(true);
            return false;
        } catch (ExecutionException e) {
            logger.println("Error " + e.getCause());
            return false;
        } finally {
            executorService.shutdown();
        }

        return result == 0 ? true : false;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Run Dogu Routine";
        }

        @RequirePOST
        public FormValidation doCheckRoutineId(@QueryParameter String value) {
            try {
                Jenkins.get().checkPermission(Jenkins.READ);
            } catch (Exception e) {
                return FormValidation.error("Please login");
            }

            if (value.isEmpty()) {
                return FormValidation.error("Please enter RoutineID");
            }

            return FormValidation.ok();
        }

        @RequirePOST
        public FormValidation doCheckCredentialsId(@QueryParameter String value) {
            try {
                Jenkins.get().checkPermission(Jenkins.READ);
            } catch (Exception e) {
                return FormValidation.error("Please login");
            }

            if (value.isEmpty()) {
                return FormValidation.error("Please select credentials");
            }

            return FormValidation.ok();
        }

        @RequirePOST
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null || !item.hasPermission(Item.CONFIGURE)) {
                return result.includeCurrentValue(credentialsId);
            }

            return result.includeEmptyValue()
                    .includeAs(ACL.SYSTEM, item, DoguCredential.class)
                    .includeCurrentValue(credentialsId);
        }
    }
}