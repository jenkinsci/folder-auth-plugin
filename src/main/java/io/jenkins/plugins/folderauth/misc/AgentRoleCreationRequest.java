package io.jenkins.plugins.folderauth.misc;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.folderauth.roles.AgentRole;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class AgentRoleCreationRequest {
    public String name = "";
    public Set<String> agentNames = Collections.emptySet();
    public Set<String> permissions = Collections.emptySet();

    @NonNull
    public AgentRole getAgentRole() {
        Set<PermissionWrapper> perms = permissions.stream().map(PermissionWrapper::new).collect(Collectors.toSet());
        return new AgentRole(name, perms, agentNames);
    }
}
