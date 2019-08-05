package io.jenkins.plugins.folderauth.roles;

import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FolderRole extends AbstractRole implements Comparable<FolderRole> {
    @Nonnull
    private final Set<String> folders;

    @DataBoundConstructor
    public FolderRole(String name, Set<PermissionWrapper> permissions, Set<String> folders, Set<String> sids) {
        super(name, permissions);
        this.sids.addAll(sids);
        this.folders = ConcurrentHashMap.newKeySet();
        this.folders.addAll(folders);
    }

    public FolderRole(String name, Set<PermissionWrapper> permissions, Set<String> folders) {
        this(name, permissions, folders, Collections.emptySet());
    }

    private FolderRole(String name, Set<PermissionWrapper> permissions, HashSet<String> folders, HashSet<String> sids) {
        super(name, permissions, sids);
        this.folders = folders;
    }

    /**
     * Used by XStream when serializing this object.
     * <p>
     * Simplifies the configuration produced by the object.
     *
     * @return a new {@link FolderRole} which does not use thread-safe constructs
     */
    @SuppressWarnings("unused")
    private FolderRole writeReplace() {
        return new FolderRole(name, permissionWrappers, new HashSet<>(folders), new HashSet<>(sids));
    }

    /**
     * Used by XSteam when deserializing.
     * <p>
     * Replaces the collections into thread-safe collections.
     *
     * @return a new {@link FolderRole} that uses thread safe collections.
     */
    @SuppressWarnings("unused")
    private FolderRole readResolve() {
        return new FolderRole(name, permissionWrappers, folders, sids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sids, permissionWrappers);
    }

    @Override
    public boolean equals(@CheckForNull Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FolderRole that = (FolderRole) o;
        return name.equals(that.name) &&
                sids.equals(that.sids) &&
                permissionWrappers.equals(that.permissionWrappers);
    }

    @Override
    public int compareTo(@Nonnull FolderRole other) {
        return name.compareTo(other.name);
    }

    /**
     * Returns the names of the folders managed by this role
     *
     * @return the names of the folders managed by this role
     */
    @Nonnull
    public Set<String> getFolderNames() {
        return Collections.unmodifiableSet(folders);
    }

    /**
     * Returns the folder names as a comma separated string list
     *
     * @return the folder names as a comma separated string list
     */
    @Nonnull
    @SuppressWarnings("unused") // used in index.jelly
    public String getFolderNamesCommaSeparated() {
        String csv = folders.toString();
        return csv.substring(1, csv.length() - 1);
    }
}
