package io.jenkins.plugins.folderauth.misc;

import io.jenkins.plugins.folderauth.FolderBasedAuthorizationStrategy;
import io.jenkins.plugins.folderauth.roles.AgentRole;
import io.jenkins.plugins.folderauth.roles.FolderRole;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@Restricted(NoExternalUse.class)
@ParametersAreNonnullByDefault
public class FolderBasedAuthorizationStrategyWrapper {
    private final Set<GlobalRole> globalRoles;
    private final Set<FolderRole> folderRoles;
    private final Set<AgentRole> agentRoles;

    public FolderBasedAuthorizationStrategyWrapper(FolderBasedAuthorizationStrategy strategy) {
        this.globalRoles = strategy.getGlobalRoles();
        this.folderRoles = strategy.getFolderRoles();
        this.agentRoles = strategy.getAgentRoles();
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
