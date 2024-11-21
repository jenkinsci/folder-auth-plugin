package io.jenkins.plugins.folderauth.misc;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.folderauth.roles.FolderRole;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@SuppressWarnings("WeakerAccess")
@Restricted(NoExternalUse.class)
public class FolderRoleCreationRequest {
    public String name = "";
    public Set<String> folderNames = Collections.emptySet();
    public Set<String> permissions = Collections.emptySet();

    @NonNull
    public FolderRole getFolderRole() {
        Set<PermissionWrapper> perms = permissions.stream().map(PermissionWrapper::new).collect(Collectors.toSet());
        return new FolderRole(name, perms, folderNames);
    }
}
