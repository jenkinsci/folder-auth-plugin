package io.jenkins.plugins.folderauth.roles;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collections;
import java.util.Set;

/**
 * An {@link AbstractRole} that's applicable everywhere inside Jenkins.
 */
public class GlobalRole extends AbstractRole {
    @DataBoundConstructor
    public GlobalRole(String name, @CheckForNull Set<PermissionWrapper> permissions, @CheckForNull Set<String> sids) {
        super(name, permissions, sids);
    }

    public GlobalRole(String name, Set<PermissionWrapper> permissions) {
        this(name, permissions, Collections.emptySet());
    }
}
