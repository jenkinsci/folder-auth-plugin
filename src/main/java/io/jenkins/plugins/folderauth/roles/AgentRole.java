package io.jenkins.plugins.folderauth.roles;

import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ParametersAreNonnullByDefault
public class AgentRole extends AbstractRole {
    private final Set<String> agents;

    @DataBoundConstructor
    public AgentRole(String name, Set<PermissionWrapper> permissions, Set<String> sids, Set<String> agents) {
        super(name, permissions);
        this.sids.addAll(sids);
        this.agents = ConcurrentHashMap.newKeySet();
        this.agents.addAll(agents);
    }

    public AgentRole(String name, Set<PermissionWrapper> permissions, Set<String> agents) {
        this(name, permissions, Collections.emptySet(), agents);
    }

    public Set<String> getAgents() {
        return Collections.unmodifiableSet(agents);
    }
}
