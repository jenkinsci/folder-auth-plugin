package io.jenkins.plugins.folderauth.roles;

import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A role as an object
 */
@Restricted(NoExternalUse.class)
public abstract class AbstractRole {
    /**
     * The unique name of the role.
     */
    @Nonnull
    protected final String name;

    /**
     * Wrappers of permissions that are assigned to this role.
     */
    @Nonnull
    protected final Set<PermissionWrapper> permissionWrappers;

    /**
     * The sids on which this role is applicable.
     */
    @Nonnull
    protected final Set<String> sids;

    @ParametersAreNonnullByDefault
    public AbstractRole(String name, Set<PermissionWrapper> permissionWrappers) {
        this.name = name;
        this.sids = ConcurrentHashMap.newKeySet();
        this.permissionWrappers = Collections.unmodifiableSet(permissionWrappers);
    }

    /**
     * Constructor for an {@link AbstractRole} that does not use thread-safe constructs.
     * <p>
     * Use only when serializing the object. The set of Sids and the unmodifiable set of
     * permissions is changed to a {@link HashSet} for simpler serialization
     *
     * @param name        the names of role
     * @param permissions the permissions granted by this role
     * @param sids        the sids to be assigned to this role
     */
    @ParametersAreNonnullByDefault
    AbstractRole(String name, Set<PermissionWrapper> permissions, Set<String> sids) {
        this.name = name;
        this.sids = new HashSet<>(sids);

        // Permissions are stored in an unmodifiable set. If an unmodifiable set was already
        // provided, the set would be wrapped up again resulting in unnecessary nesting in
        // the XML produced during serialization. This simplifies it.
        this.permissionWrappers = new HashSet<>(permissions);
    }

    /**
     * This {@link AbstractRole} will become applicable on the given {@code sids}.
     *
     * @param sids the user sids on which this role will become applicable
     */
    public void assignSids(String... sids) {
        assignSids(Arrays.asList(sids));
    }

    /**
     * This {@link AbstractRole} will become applicable on the given {@code sids}.
     *
     * @param sids the sids on which this role will become applicable
     */
    public void assignSids(@Nonnull List<String> sids) {
        this.sids.addAll(sids);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRole role = (AbstractRole) o;
        return name.equals(role.name) &&
                permissionWrappers.equals(role.permissionWrappers) &&
                sids.equals(role.sids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, permissionWrappers, sids);
    }

    /**
     * The name of the Role
     *
     * @return the name of the role
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * The permissions assigned to the role.
     * <p>
     * This method, however, does not return all permissions implied by this {@link AbstractRole}
     *
     * @return the permissions assigned to the role.
     */
    @Nonnull
    public Set<PermissionWrapper> getPermissions() {
        return Collections.unmodifiableSet(permissionWrappers);
    }

    /**
     * List of sids on which the role is applicable.
     *
     * @return list of sids on which this role is applicable.
     */
    @Nonnull
    public Set<String> getSids() {
        return sids;
    }

    /**
     * Return a comma separated list of sids assigned to this role
     *
     * @return a comma separated list of sids assigned to this role
     */
    @Nonnull
    @SuppressWarnings("unused") // used by index.jelly
    public String getSidsCommaSeparated() {
        String string = sids.toString();
        return string.substring(1, string.length() - 1);
    }

    public void unassignSids(String... sids) {
        this.sids.removeAll(Arrays.asList(sids));
    }

    public void unassignSids(@Nonnull List<String> sids) {
        this.sids.removeAll(sids);
    }
}
