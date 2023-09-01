package io.jenkins.plugins.dogu.builder;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
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
import io.jenkins.plugins.dogu.DoguOption;
import io.jenkins.plugins.dogu.common.DoguApi;
import io.jenkins.plugins.dogu.common.DoguUtils;
import io.jenkins.plugins.dogu.credential.DoguCredential;
import java.io.PrintStream;
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

        Secret accessTokenSecret = DoguUtils.getAccessToken(credentialsId);
        String apiUrl = DoguUtils.getApiUrl(build, listener, logger);
        DoguOption doguOption = new DoguOption(accessTokenSecret, apiUrl);

        try {
            boolean routineResult = DoguApi.runRoutine(projectId, routineId, doguOption, logger);
            return routineResult;
        } catch (Exception e) {
            e.printStackTrace(logger);
            return false;
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Run routine in Dogu";
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
