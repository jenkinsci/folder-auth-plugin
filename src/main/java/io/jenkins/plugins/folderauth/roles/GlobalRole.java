package io.jenkins.plugins.folderauth.roles;

import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An {@link AbstractRole} that's applicable everywhere inside Jenkins.
 */
public class GlobalRole extends AbstractRole {
    @DataBoundConstructor
    public GlobalRole(String name, Set<PermissionWrapper> permissions, Set<String> sids) {
        super(name, permissions);
        this.sids.addAll(sids);
    }

    private GlobalRole(String name, HashSet<PermissionWrapper> permissions, HashSet<String> sids) {
        super(name, permissions, sids);
    }

    @SuppressWarnings("unused")
    private GlobalRole writeReplace() {
        return new GlobalRole(name, new HashSet<>(permissionWrappers), new HashSet<>(sids));
    }

    @SuppressWarnings("unused")
    private GlobalRole readResolve() {
        return new GlobalRole(name, permissionWrappers, sids);
    }

    public GlobalRole(String name, Set<PermissionWrapper> permissions) {
        this(name, permissions, Collections.emptySet());
    }
}
