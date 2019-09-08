package io.jenkins.plugins.folderauth.roles;

import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class FolderRole extends AbstractRole implements Comparable<FolderRole> {
    @Nonnull
    private final Set<String> folders;

    @DataBoundConstructor
    public FolderRole(String name, Set<PermissionWrapper> permissions, Set<String> folders, Set<String> sids) {
        super(name, permissions, sids);
        this.folders = new HashSet<>(folders);
    }

    public FolderRole(String name, Set<PermissionWrapper> permissions, Set<String> folders) {
        this(name, permissions, folders, Collections.emptySet());
    }

    @Override
    public int compareTo(FolderRole other) {
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
     * Returns sorted folder names as a comma separated string list
     *
     * @return sorted folder names as a comma separated string list
     */
    @Nonnull
    @SuppressWarnings("unused") // used in index.jelly
    public String getFolderNamesCommaSeparated() {
        String csv = new TreeSet<>(folders).toString();
        return csv.substring(1, csv.length() - 1);
    }
}
