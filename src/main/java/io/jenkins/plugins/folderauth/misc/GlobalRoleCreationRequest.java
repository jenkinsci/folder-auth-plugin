package io.jenkins.plugins.folderauth.misc;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Restricted(NoExternalUse.class)
public class GlobalRoleCreationRequest {
    public String name = "";
    public List<String> permissions = Collections.emptyList();

    @NonNull
    public GlobalRole getGlobalRole() {
        return new GlobalRole(name, permissions.stream().map(PermissionWrapper::new).collect(Collectors.toSet()));
    }
}
