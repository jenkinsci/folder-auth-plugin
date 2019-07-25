package io.jenkins.plugins.folderauth.listeners;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import hudson.security.AuthorizationStrategy;
import io.jenkins.plugins.folderauth.FolderBasedAuthorizationStrategy;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Listens for changes to {@link AbstractFolder}s in Jenkins and makes sure that the configuration of
 * {@link FolderBasedAuthorizationStrategy} remains the same.
 * <p>
 * Does not do anything if {@link Jenkins#getAuthorizationStrategy} is not {@link FolderBasedAuthorizationStrategy}.
 */
@Extension
@Restricted(NoExternalUse.class)
public class FolderListener extends ItemListener {
    @Override
    public void onDeleted(Item item) {
        AuthorizationStrategy a = Jenkins.get().getAuthorizationStrategy();
        if (item instanceof AbstractFolder && a instanceof FolderBasedAuthorizationStrategy) {
            FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) a;
            strategy.onFolderDeleted(item.getFullName());
        }
    }

    @Override
    public void onLocationChanged(Item item, String oldFullName, String newFullName) {
        AuthorizationStrategy a = Jenkins.get().getAuthorizationStrategy();
        if (item instanceof AbstractFolder && a instanceof FolderBasedAuthorizationStrategy) {
            FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) a;
            strategy.onFolderRenamed(oldFullName, newFullName);
        }
    }
}
