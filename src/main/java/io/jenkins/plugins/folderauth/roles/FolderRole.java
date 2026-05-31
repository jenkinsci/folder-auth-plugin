package io.jenkins.plugins.folderauth.roles;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.kohsuke.stapler.DataBoundConstructor;

public class FolderRole extends AbstractRole {
    @NonNull
    private final Set<String> folders;

    @DataBoundConstructor
    public FolderRole(String name, @CheckForNull Set<PermissionWrapper> permissions, @CheckForNull Set<String> folders, @CheckForNull Set<String> sids) {
        super(name, permissions, sids);
        this.folders = folders != null ? new HashSet<>(folders) : new HashSet<>();
    }

    public FolderRole(String name, Set<PermissionWrapper> permissions, Set<String> folders) {
        this(name, permissions, folders, Collections.emptySet());
    }

    /**
     * Returns the names of the folders managed by this role
     *
     * @return the names of the folders managed by this role
     */
    @NonNull
    public Set<String> getFolderNames() {
        return Collections.unmodifiableSet(folders);
    }

    /**
     * Returns sorted folder names as a comma separated string list
     *
     * @return sorted folder names as a comma separated string list
     */
    @NonNull
    @SuppressWarnings("unused") // used in index.jelly
    public String getFolderNamesCommaSeparated() {
        String csv = new TreeSet<>(folders).toString();
        return csv.substring(1, csv.length() - 1);
    }
}
