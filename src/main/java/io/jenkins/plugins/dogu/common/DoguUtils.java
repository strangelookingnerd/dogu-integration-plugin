package io.jenkins.plugins.dogu.common;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.Secret;
import io.jenkins.plugins.dogu.credential.DoguCredential;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import jenkins.model.Jenkins;

public class DoguUtils {
    public static Secret getAccessToken(String credentialsId) {
        DoguCredential credential = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        DoguCredential.class, Jenkins.get(), ACL.SYSTEM, Collections.<DomainRequirement>emptyList()),
                CredentialsMatchers.withId(credentialsId));

        Secret accessTokenSecret;
        if (credential == null) {
            accessTokenSecret = Secret.fromString("");
        } else {
            accessTokenSecret = credential.getSecret();
        }

        return accessTokenSecret;
    }

    public static String getApiUrl(Run<?, ?> build, TaskListener listener, PrintStream logger) {
        String apiUrl = "https://api.dogutech.io";

        try {
            EnvVars envVars = build.getEnvironment(listener);
            String customApiUrl = envVars.get("DOGU_API_URL");

            if (customApiUrl != null) {
                apiUrl = customApiUrl;
            }
        } catch (IOException | InterruptedException e) {
        }

        logger.println("API URL: " + apiUrl);

        return apiUrl;
    }
}
