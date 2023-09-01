package io.jenkins.plugins.dogu.credential;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

public class DoguCredential extends BaseStandardCredentials {

    private final Secret secret;

    @DataBoundConstructor
    public DoguCredential(CredentialsScope scope, String id, String description, Secret secret) {
        super(scope, id, description);

        this.secret = secret;
    }

    public Secret getSecret() {
        return secret;
    }

    @Extension
    public static final class DescriptorImpl extends CredentialsDescriptor {

        @Override
        public String getDisplayName() {
            return "Dogu access token";
        }
    }
}
