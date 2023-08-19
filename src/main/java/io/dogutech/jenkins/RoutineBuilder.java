package io.dogutech.jenkins;

import io.dogutech.jenkins.ApiClient;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
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

import java.io.PrintStream;
import java.util.Collections;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class RoutineBuilder extends Builder {

    private final String projectId;
    private final String routineId;
    private final String credentialsId;

    @DataBoundConstructor
    public RoutineBuilder(String projectId, String routineId, String credentialsId) {
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
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        PrintStream logger = listener.getLogger();

        DoguCredential credential = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        DoguCredential.class,
                        Jenkins.getInstance(),
                        ACL.SYSTEM,
                        Collections.<DomainRequirement>emptyList()),
                CredentialsMatchers.withId(credentialsId));


        if (credential == null) {
            logger.println("Failed to find credentials with ID: " + credentialsId);
            return false;
        }

        try {
            ApiClient.runRoutine(projectId, routineId, credentialsId);
        } catch (Exception e) {
            logger.println("ERROR: " + e.getMessage());
            e.printStackTrace(logger);
            return false;
        }

        return true;
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

        public FormValidation doCheckRoutineId(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.error("Please enter RoutineID");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckCredentialsId(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.error("Please select credentials");
            }
            return FormValidation.ok();
        }

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
