package io.jenkins.plugins.folderauth.misc;

import io.jenkins.plugins.folderauth.roles.AgentRole;
import io.jenkins.plugins.folderauth.roles.FolderRole;
import io.jenkins.plugins.folderauth.roles.GlobalRole;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@ParametersAreNonnullByDefault
public class FolderBasedAuthorizationStrategyWrapper {
    private final Set<GlobalRole> globalRoles;
    private final Set<FolderRole> folderRoles;
    private final Set<AgentRole> agentRoles;

    public FolderBasedAuthorizationStrategyWrapper(Set<GlobalRole> globalRoles,
                                                   Set<FolderRole> folderRoles,
                                                   Set<AgentRole> agentRoles) {
        this.globalRoles = globalRoles;
        this.folderRoles = folderRoles;
        this.agentRoles = agentRoles;
    }

    public Set<GlobalRole> getGlobalRoles() {
        return globalRoles;
    }

    public Set<FolderRole> getFolderRoles() {
        return folderRoles;
    }

    public Set<AgentRole> getAgentRoles() {
        return agentRoles;
    }
}
