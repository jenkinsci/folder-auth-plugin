package io.jenkins.plugins.folderauth.misc;

import io.jenkins.plugins.folderauth.roles.GlobalRole;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Restricted(NoExternalUse.class)
public class GlobalRoleCreationRequest {
    public String name = "";
    public List<String> permissions = Collections.emptyList();

    @Nonnull
    public GlobalRole getGlobalRole() {
        return new GlobalRole(name, permissions.stream().map(PermissionWrapper::new).collect(Collectors.toSet()));
    }
}
