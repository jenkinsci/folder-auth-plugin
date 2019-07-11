package io.jenkins.plugins.folderauth.misc;

import hudson.PluginManager;
import hudson.security.Permission;
import io.jenkins.plugins.folderauth.roles.AbstractRole;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A wrapper for efficient serialization of a {@link Permission}
 * when stored as a part of an {@link AbstractRole}.
 */
public final class PermissionWrapper {
    // should've been final but needs to be setup when the
    // object is deserialized from the XML config
    private transient Permission permission;
    private final String id;

    @Restricted(NoExternalUse.class)
    public static final Set<Permission> DANGEROUS_PERMISSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            Jenkins.RUN_SCRIPTS,
            PluginManager.CONFIGURE_UPDATECENTER,
            PluginManager.UPLOAD_PLUGINS
    )));

    /**
     * Constructor.
     *
     * @param id the id of the permission this {@link PermissionWrapper} contains.
     */
    @DataBoundConstructor
    public PermissionWrapper(@Nonnull String id) {
        this.id = id;
        permission = Permission.fromId(id);
        checkPermission();
    }

    /**
     * Used to setup the permission when deserialized
     *
     * @return the {@link PermissionWrapper}
     */
    @SuppressWarnings("unused")
    private Object readResolve() {
        permission = Permission.fromId(id);
        checkPermission();
        return this;
    }

    /**
     * Get the permission corresponding to this {@link PermissionWrapper}
     *
     * @return the permission corresponding to this {@link PermissionWrapper}
     */
    public Permission getPermission() {
        return permission;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionWrapper that = (PermissionWrapper) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Checks if the permission for this {@link PermissionWrapper} is valid.
     *
     * @throws IllegalArgumentException when the permission did not exist, was null or was dangerous.
     */
    private void checkPermission() {
        if (permission == null) {
            throw new IllegalArgumentException("Unable to infer permission from Id: " + id);
        } else if (DANGEROUS_PERMISSIONS.contains(permission)) {
            throw new IllegalArgumentException("Dangerous Permissions are not supported.");
        }
    }

    /**
     * Convenience method to wrap {@link Permission}s into {@link PermissionWrapper}s.
     *
     * @param permissions permissions to be wrapped up
     * @return a set containing a {@link PermissionWrapper} for each permission in {@code permissions}
     */
    public static Set<PermissionWrapper> wrapPermissions(Permission... permissions) {
        return _wrapPermissions(Arrays.stream(permissions));
    }

    /**
     * Convenience method to wrap {@link Permission}s into {@link PermissionWrapper}s.
     *
     * @param permissions permissions to be wrapped up
     * @return a set containing a {@link PermissionWrapper} for each permission in {@code permissions}
     */
    public static Set<PermissionWrapper> wrapPermissions(Collection<Permission> permissions) {
        return _wrapPermissions(permissions.stream());
    }

    private static Set<PermissionWrapper> _wrapPermissions(Stream<Permission> stream) {
        return stream
                .map(Permission::getId)
                .map(PermissionWrapper::new)
                .collect(Collectors.toSet());
    }
}
