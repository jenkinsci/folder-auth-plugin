package io.jenkins.plugins.folderauth.roles;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.ParametersAreNonnullByDefault;
import org.kohsuke.stapler.DataBoundConstructor;

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

    @NonNull
    public Set<String> getAgents() {
        return Collections.unmodifiableSet(agents);
    }

    /**
     * Returns sorted agent names as a comma separated string list
     *
     * @return sorted agent names as a comma separated string list
     */
    @NonNull
    @SuppressWarnings("unused") // used in index.jelly
    public String getAgentNamesCommaSeparated() {
        String csv = new TreeSet<>(agents).toString();
        return csv.substring(1, csv.length() - 1);
    }
}
