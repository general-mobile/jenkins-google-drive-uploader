package com.generalmobile.googledriveupload;

import com.google.api.services.drive.DriveScopes;
import com.google.common.collect.ImmutableList;
import com.google.jenkins.plugins.credentials.oauth.GoogleOAuth2ScopeRequirement;

import java.util.Collection;

/**
 * The required OAuth2 scopes for managing Google Drive files.
 */
public class DriveScopeRequirement extends GoogleOAuth2ScopeRequirement {
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getScopes() {
        return ImmutableList.of(DriveScopes.DRIVE_FILE);
    }
}
