package io.jenkins.plugins.folderauth.misc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hudson.PluginManager;
import hudson.security.Permission;
import io.jenkins.plugins.folderauth.Messages;
import io.jenkins.plugins.folderauth.roles.AbstractRole;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
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
@ParametersAreNonnullByDefault
public final class PermissionWrapper implements Comparable<PermissionWrapper> {
    // should've been final but needs to be setup when the
    // object is deserialized from the XML config
    private transient Permission permission;
    private final String id;

    @SuppressWarnings("deprecation")
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
    public PermissionWrapper(String id) {
        this.id = id;
        permission = PermissionFinder.findPermission(id);
        checkPermission();
    }

    public String getId() {
        return String.format("%s/%s", permission.group.getId(), permission.name);
    }

    @Restricted(NoExternalUse.class) // Restricted so JCasC does not pick it up
    public String getPermissionId() {
        return id;
    }

    /**
     * Used to setup the permission when deserialized
     *
     * @return the {@link PermissionWrapper}
     */
    @Nonnull
    @SuppressWarnings("unused")
    private Object readResolve() {
        permission = PermissionFinder.findPermission(id);
        checkPermission();
        return this;
    }

    /**
     * Get the permission corresponding to this {@link PermissionWrapper}
     *
     * @return the permission corresponding to this {@link PermissionWrapper}
     */
    @Nonnull
    @JsonIgnore
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
            throw new IllegalArgumentException(Messages.PermissionWrapper_UnknownPermission(id));
        } else if (DANGEROUS_PERMISSIONS.contains(permission)) {
            throw new IllegalArgumentException(Messages.PermissionWrapper_NoDangerousPermissions());
        }
    }

    /**
     * Convenience method to wrap {@link Permission}s into {@link PermissionWrapper}s.
     *
     * @param permissions permissions to be wrapped up
     * @return a set containing a {@link PermissionWrapper} for each permission in {@code permissions}
     */
    @Nonnull
    public static Set<PermissionWrapper> wrapPermissions(Permission... permissions) {
        return _wrapPermissions(Arrays.stream(permissions));
    }

    /**
     * Convenience method to wrap {@link Permission}s into {@link PermissionWrapper}s.
     *
     * @param permissions permissions to be wrapped up
     * @return a set containing a {@link PermissionWrapper} for each permission in {@code permissions}
     */
    @Nonnull
    public static Set<PermissionWrapper> wrapPermissions(Collection<Permission> permissions) {
        return _wrapPermissions(permissions.stream());
    }

    @Nonnull
    private static Set<PermissionWrapper> _wrapPermissions(Stream<Permission> stream) {
        return stream
                .map(Permission::getId)
                .map(PermissionWrapper::new)
                .collect(Collectors.toSet());
    }

    @Override
    public int compareTo(@Nonnull PermissionWrapper other) {
        return Permission.ID_COMPARATOR.compare(this.permission, other.permission);
    }
}
