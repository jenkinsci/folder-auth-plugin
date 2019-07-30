package io.jenkins.plugins.folderauth.roles;

import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ParametersAreNonnullByDefault
public class AgentRole extends AbstractRole {
    private final Set<String> agents;

    @DataBoundConstructor
    public AgentRole(String name, Set<PermissionWrapper> permissions, Set<String> agents, Set<String> sids) {
        super(name, permissions, sids);
        this.agents = new HashSet<>(agents);
    }

    public AgentRole(String name, Set<PermissionWrapper> permissions, Set<String> agents) {
        this(name, permissions, agents, Collections.emptySet());
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
