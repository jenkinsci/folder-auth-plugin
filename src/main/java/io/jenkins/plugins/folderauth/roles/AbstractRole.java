package io.jenkins.plugins.folderauth.roles;

import hudson.model.User;
import hudson.security.SecurityRealm;
import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import jenkins.model.Jenkins;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.springframework.dao.DataAccessException;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A role as an immutable object
 */
@Restricted(NoExternalUse.class)
public abstract class AbstractRole implements Comparable<AbstractRole> {
    @Override
    public int compareTo(@Nonnull AbstractRole other) {
        return this.name.compareTo(other.name);
    }

    /**
     * The unique name of the role.
     */
    @Nonnull
    protected final String name;

    /**
     * Wrappers of permissions that are assigned to this role. Should not be modified.
     */
    @Nonnull
    private final Set<PermissionWrapper> permissionWrappers;

    /**
     * The sids on which this role is applicable.
     */
    @Nonnull
    protected final Set<String> sids;

    AbstractRole(String name, Set<PermissionWrapper> permissions, Set<String> sids) {
        this.name = name;
        this.permissionWrappers = new HashSet<>(permissions);
        this.sids = new HashSet<>(sids);
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
     * @see AbstractRole#getPermissionsUnsorted() when the permissions are not needed in a sorted order.
     */
    @Nonnull
    public SortedSet<PermissionWrapper> getPermissions() {
        return Collections.unmodifiableSortedSet(new TreeSet<>(permissionWrappers));
    }

    /**
     * The permissions assigned to the role in an unsorted order.
     *
     * @return permissions in an unsorted order.
     * @see AbstractRole#getPermissions() when permissions are needed in a sorted order.
     */
    @Nonnull
    public Set<PermissionWrapper> getPermissionsUnsorted() {
        return Collections.unmodifiableSet(permissionWrappers);
    }

    /**
     * List of sids on which the role is applicable.
     *
     * @return list of sids on which this role is applicable.
     */
    @Nonnull
    public Set<String> getSids() {
        return Collections.unmodifiableSet(sids);
    }

    /**
     * Return a sorted comma separated list of sids assigned to this role
     *
     * @return a sorted comma separated list of sids assigned to this role
     */
    @Nonnull
    @SuppressWarnings("unused") // used by index.jelly
    public String getSidsCommaSeparated() {
        Jenkins jenkinsInstance = Jenkins.get();
        SecurityRealm sr = jenkinsInstance.getSecurityRealm();
        StringBuilder sb = new StringBuilder();
        for (String sid: new TreeSet<>(sids)) {
            try {
                sr.loadUserByUsername(sid);
                User u = User.getById(sid, false);
                String fullName = u.getFullName();
                sb.append(sid).append("(").append(fullName).append("), ");
            } catch (UsernameNotFoundException | DataAccessException | NullPointerException e) {
                // on any exception just add the sid
                // this could happen either because SID lookup error or no FullName set
                sb.append(sid).append(", ");
            }
        }
        return sb.length()==0?"":sb.substring(0,sb.length()-2);
    }
}
