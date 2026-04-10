package io.jenkins.plugins.folderauth.misc;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.PluginManager;
import hudson.security.Permission;
import io.jenkins.plugins.folderauth.Messages;
import io.jenkins.plugins.folderauth.roles.AbstractRole;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A wrapper for efficient serialization of a {@link Permission}
 * when stored as a part of an {@link AbstractRole}.
 */
@ParametersAreNonnullByDefault
public final class PermissionWrapper implements Comparable<PermissionWrapper> {
    private static final Logger LOGGER = Logger.getLogger(PermissionWrapper.class.getName());
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
    public PermissionWrapper(String id) {
        this.id = id;
        permission = PermissionFinder.findPermission(id);
        checkPermission();
    }

    public String getId() {
        // If the permission could not be resolved (unknown plugin/disabled), fall back to the raw stored id.
        if (permission == null) {
            return id;
        }
        return String.format("%s/%s", permission.group.getId(), permission.name);
    }

    /**
     * Used to setup the permission when deserialized
     *
     * @return the {@link PermissionWrapper}
     */
    @NonNull
    @SuppressWarnings("unused")
    private Object readResolve() {
        permission = PermissionFinder.findPermission(id);
        checkPermission();
        return this;
    }

    /**
     * Get the permission corresponding to this {@link PermissionWrapper}.
     * May return {@code null} when the permission ID references a plugin that is not installed
     * or a permission that could not be resolved.  Callers must handle the {@code null} case.
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * Returns {@code true} if this wrapper holds a valid, enabled permission.
     * Wrappers for which this returns {@code false} should not be used for access-control decisions.
     */
    public boolean isValid() {
        return permission != null && permission.enabled;
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
     * <p>Behaviour by case:
     * <ul>
     *   <li><b>Dangerous permissions</b> ({@link #DANGEROUS_PERMISSIONS}) – always rejected with an
     *       {@link IllegalArgumentException} regardless of context.</li>
     *   <li><b>Unknown permissions</b> ({@code permission == null}) – a WARNING is logged and the
     *       wrapper is kept with a {@code null} internal permission.  This allows Jenkins to start up
     *       even when a referenced permission belongs to a plugin that is not currently installed.</li>
     *   <li><b>Disabled permissions</b> ({@code !permission.enabled}) – a WARNING is logged.
     *       SECURITY-3062 compliance is preserved via two mechanisms: the UI filters disabled
     *       permissions out ({@code getSafePermissions}) and Jenkins core refuses to honour them
     *       at access-check time ({@code AbstractACL.hasPermission}).</li>
     * </ul>
     */
    private void checkPermission() {
        if (permission == null) {
            // Permission from an uninstalled/unavailable plugin – log and continue rather than crashing.
            LOGGER.log(Level.WARNING,
                    "Permission ''{0}'' is unknown in this Jenkins installation (plugin may not be installed) "
                    + "and will have no effect.", id);
        } else if (DANGEROUS_PERMISSIONS.contains(permission)) {
            throw new IllegalArgumentException(Messages.PermissionWrapper_NoDangerousPermissions());
        } else if (!permission.enabled) {
            // SECURITY-3062: disabled permissions cannot be granted via the UI (getSafePermissions filters
            // them out) and Jenkins core itself refuses to honour them at access-check time.  We log a
            // WARNING here instead of throwing so that existing configurations that reference a permission
            // which is disabled in the current environment (e.g. Credentials/UseItem, Job/WipeOut) do not
            // prevent Jenkins from starting up.
            LOGGER.log(Level.WARNING,
                    "Permission ''{0}'' is disabled in this Jenkins installation and will have no effect. "
                    + "Consider removing it from the folder-auth configuration.", id);
        }
    }

    /**
     * Convenience method to wrap {@link Permission}s into {@link PermissionWrapper}s.
     *
     * @param permissions permissions to be wrapped up
     * @return a set containing a {@link PermissionWrapper} for each permission in {@code permissions}
     */
    @NonNull
    public static Set<PermissionWrapper> wrapPermissions(Permission... permissions) {
        return _wrapPermissions(Arrays.stream(permissions));
    }

    /**
     * Convenience method to wrap {@link Permission}s into {@link PermissionWrapper}s.
     *
     * @param permissions permissions to be wrapped up
     * @return a set containing a {@link PermissionWrapper} for each permission in {@code permissions}
     */
    @NonNull
    public static Set<PermissionWrapper> wrapPermissions(Collection<Permission> permissions) {
        return _wrapPermissions(permissions.stream());
    }

    @NonNull
    private static Set<PermissionWrapper> _wrapPermissions(Stream<Permission> stream) {
        return stream
                .map(Permission::getId)
                .map(PermissionWrapper::new)
                .collect(Collectors.toSet());
    }

    @Override
    public int compareTo(@NonNull PermissionWrapper other) {
        // Fall back to string comparison when either permission could not be resolved.
        if (this.permission == null || other.permission == null) {
            return this.id.compareTo(other.id);
        }
        return Permission.ID_COMPARATOR.compare(this.permission, other.permission);
    }
}
