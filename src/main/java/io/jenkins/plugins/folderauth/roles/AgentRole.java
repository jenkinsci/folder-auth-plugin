package io.jenkins.plugins.folderauth.roles;

import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ParametersAreNonnullByDefault
public class AgentRole extends AbstractRole {
    private final Set<String> agents;

    @DataBoundConstructor
    public AgentRole(String name, Set<PermissionWrapper> permissions, Set<String> agents, Set<String> sids) {
        super(name, permissions);
        this.sids.addAll(sids);
        this.agents = ConcurrentHashMap.newKeySet();
        this.agents.addAll(agents);
    }

    public AgentRole(String name, Set<PermissionWrapper> permissions, Set<String> agents) {
        this(name, permissions, Collections.emptySet(), agents);
    }

    private AgentRole(String name, HashSet<PermissionWrapper> permissions, HashSet<String> agents, HashSet<String> sids) {
        super(name, permissions, sids);
        this.agents = agents;
    }

    @SuppressWarnings("unused")
    private AgentRole writeReplace() {
        return new AgentRole(name, new HashSet<>(permissionWrappers), new HashSet<>(agents), new HashSet<>(sids));
    }

    @SuppressWarnings("unused")
    private AgentRole readResolve() {
        return new AgentRole(name, permissionWrappers, agents, sids);
    }

    @Nonnull
    public Set<String> getAgents() {
        return Collections.unmodifiableSet(agents);
    }

    /**
     * Returns the agent names as a comma separated string list
     *
     * @return the agent names as a comma separated string list
     */
    @Nonnull
    @SuppressWarnings("unused") // used in index.jelly
    public String getAgentNamesCommaSeparated() {
        String csv = agents.toString();
        return csv.substring(1, csv.length() - 1);
    }
}
