package com.anicks.googledriveupload;

import com.google.api.client.auth.oauth2.Credential;
import com.google.jenkins.plugins.credentials.domains.DomainRequirementProvider;
import com.google.jenkins.plugins.credentials.domains.RequiresDomain;
import com.google.jenkins.plugins.credentials.oauth.GoogleRobotCredentials;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.google.common.base.Preconditions.checkNotNull;

@RequiresDomain(value = DriveScopeRequirement.class)
public final class GoogleDriveUploader extends Recorder {

    private final String credentialsId;
    private final String driveLocation;
    private final String uploadFolder;

    @DataBoundConstructor
    public GoogleDriveUploader(String credentialsId, String driveLocation, String uploadFolder) {
        this.credentialsId = checkNotNull(credentialsId);
        this.driveLocation = checkNotNull(driveLocation);
        this.uploadFolder = checkNotNull(uploadFolder);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {

        try {
            listener.getLogger().println("Google Drive Uploading Plugin Started.");
            GoogleRobotCredentials credentials = GoogleRobotCredentials.getById(getCredentialsId());
            GoogleDriveManager driveManager = new GoogleDriveManager(authorize(credentials));

            String workspace = build.getWorkspace().getRemote();

            if (uploadFolder.length() > 0) {
                if (uploadFolder.startsWith("$")) {
                    workspace += "/" + build.getEnvironment(listener).get(uploadFolder.replace("$", ""));
                } else {
                    workspace += "/" + uploadFolder;
                }
            }
            listener.getLogger().println("Uploading folder: " + workspace);
            driveManager.uploadFolder(workspace, getDriveLocation(), listener);
        } catch (GeneralSecurityException e) {
            build.setResult(Result.FAILURE);
            return false;
        }
        return true;
    }

    private Credential authorize(GoogleRobotCredentials credentials) throws GeneralSecurityException {
        GoogleRobotCredentials googleRobotCredentials = credentials.forRemote(getRequirement());
        return googleRobotCredentials.getGoogleCredential(getRequirement());
    }

    private DriveScopeRequirement getRequirement() {
        return DomainRequirementProvider.of(getClass(), DriveScopeRequirement.class);
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getDriveLocation() {
        return driveLocation;
    }

    public String getUploadFolder() {
        return uploadFolder;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public String getDisplayName() {
            return "Google Drive Uploader";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }
    }
}
