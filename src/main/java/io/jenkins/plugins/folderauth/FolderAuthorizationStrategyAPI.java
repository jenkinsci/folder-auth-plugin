package io.jenkins.plugins.folderauth;

import hudson.security.AuthorizationStrategy;
import io.jenkins.plugins.folderauth.roles.AgentRole;
import io.jenkins.plugins.folderauth.roles.FolderRole;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Public-facing methods for modifying {@link FolderBasedAuthorizationStrategy}.
 * <p>
 * These methods should only be called when {@link Jenkins#getAuthorizationStrategy()}} is
 * {@link FolderBasedAuthorizationStrategy}. This class does not provide REST API methods.
 *
 * @see FolderAuthorizationStrategyManagementLink for REST API methods.
 */
@ParametersAreNonnullByDefault
@SuppressWarnings("WeakerAccess")
public class FolderAuthorizationStrategyAPI {

    private FolderAuthorizationStrategyAPI() {
    }

    /**
     * Checks the {@link AuthorizationStrategy} and runs the {@link Consumer} when it is an instance of
     * {@link FolderBasedAuthorizationStrategy}.
     * <p>
     * All attempts to access the {@link FolderBasedAuthorizationStrategy} must go through this method
     * for thread-safety.
     *
     * @param runner a function that consumes the current {@link FolderBasedAuthorizationStrategy} and returns a non
     *               null {@link FolderBasedAuthorizationStrategy} object. The object may be the same as the one
     *               consumed if no modification was needed.
     * @throws IllegalStateException when {@link Jenkins#getAuthorizationStrategy()} is not
     *                               {@link FolderBasedAuthorizationStrategy}
     */
    private synchronized static void run(Function<FolderBasedAuthorizationStrategy, FolderBasedAuthorizationStrategy> runner) {
        Jenkins jenkins = Jenkins.get();
        AuthorizationStrategy strategy = jenkins.getAuthorizationStrategy();
        if (strategy instanceof FolderBasedAuthorizationStrategy) {
            FolderBasedAuthorizationStrategy newStrategy = runner.apply((FolderBasedAuthorizationStrategy) strategy);
            jenkins.setAuthorizationStrategy(newStrategy);
        } else {
            throw new IllegalStateException("FolderBasedAuthorizationStrategy is not the" + " current authorization strategy");
        }
    }

    /**
     * Adds a {@link GlobalRole} to the {@link FolderBasedAuthorizationStrategy}.
     *
     * @param role the role to be added.
     * @throws IllegalArgumentException when a role with the given name already exists.
     */
    public static void addGlobalRole(GlobalRole role) {
        run(strategy -> {
            Set<GlobalRole> globalRoles = new HashSet<>(strategy.getGlobalRoles());
            String name = role.getName();
            Optional<GlobalRole> existing = globalRoles.stream().filter(r -> r.getName().equals(name)).findAny();
            if (existing.isPresent()) {
                throw new IllegalArgumentException("A global role with the name \"" + name + "\" already exists.");
            }
            globalRoles.add(role);
            return new FolderBasedAuthorizationStrategy(globalRoles, strategy.getFolderRoles(), strategy.getAgentRoles());
        });
    }

    /**
     * Adds a {@link FolderRole} to the {@link FolderBasedAuthorizationStrategy}.
     *
     * @param role the role to be added.
     * @throws IllegalArgumentException when a role with the given name already exists.
     */
    public static void addFolderRole(FolderRole role) {
        run(strategy -> {
            Set<FolderRole> folderRoles = new HashSet<>(strategy.getFolderRoles());
            String name = role.getName();
            Optional<FolderRole> existing = folderRoles.stream().filter(r -> r.getName().equals(name)).findAny();
            if (existing.isPresent()) {
                throw new IllegalArgumentException("A folder role with the name \"" + name + "\" already exists.");
            }
            folderRoles.add(role);
            return new FolderBasedAuthorizationStrategy(strategy.getGlobalRoles(), folderRoles, strategy.getAgentRoles());
        });
    }

    /**
     * Adds an {@link AgentRole} to the {@link FolderBasedAuthorizationStrategy}.
     *
     * @param role the role to be added.
     * @throws IllegalArgumentException when a role with the given name already exists.
     */
    public static void addAgentRole(AgentRole role) {
        run(strategy -> {
            Set<AgentRole> agentRoles = new HashSet<>(strategy.getAgentRoles());
            String name = role.getName();
            Optional<AgentRole> existing = agentRoles.stream().filter(r -> r.getName().equals(name)).findAny();
            if (existing.isPresent()) {
                throw new IllegalArgumentException("An agent role with the name \"" + name + "\" already exists.");
            }
            agentRoles.add(role);
            return new FolderBasedAuthorizationStrategy(strategy.getGlobalRoles(), strategy.getFolderRoles(), agentRoles);
        });
    }

    /**
     * Assigns the {@code sid} to the {@link GlobalRole} identified by {@code roleName}.
     *
     * @param sid      this sid will be assigned to the global role with the name equal to {@code roleName}.
     * @param roleName the name of the global role
     * @throws IllegalArgumentException when no global role with name equal to {@code roleName} exists
     * @throws IllegalArgumentException when the {@code sid} is empty
     */
    public static void assignSidToGlobalRole(String sid, String roleName) {
        if (StringUtils.isBlank(sid)) {
            throw new IllegalArgumentException("Sid should not be blank.");
        }

        run(strategy -> {
            Set<GlobalRole> globalRoles = new HashSet<>(strategy.getGlobalRoles());
            GlobalRole role = globalRoles.stream().filter(r -> r.getName().equals(roleName)).findAny().orElseThrow(
                () -> new IllegalArgumentException("No global role with name = \"" + roleName + "\" exists"));
            HashSet<String> newSids = new HashSet<>(role.getSids());
            newSids.add(sid);
            globalRoles.remove(role);
            globalRoles.add(new GlobalRole(role.getName(), role.getPermissions(), newSids));
            return new FolderBasedAuthorizationStrategy(globalRoles, strategy.getFolderRoles(), strategy.getAgentRoles());
        });
    }

    /**
     * Assigns the {@code sid} to the {@link AgentRole} identified by {@code roleName}.
     *
     * @param sid      this sid will be assigned to the {@link AgentRole} with the name equal to {@code roleName}.
     * @param roleName the name of the agent role
     * @throws IllegalArgumentException when no agent role with name equal to {@code roleName} exists
     * @throws IllegalArgumentException when the {@code sid} is empty
     */
    public static void assignSidToAgentRole(String sid, String roleName) {
        if (StringUtils.isBlank(sid)) {
            throw new IllegalArgumentException("Sid should not be blank.");
        }

        run(strategy -> {
            Set<AgentRole> agentRoles = new HashSet<>(strategy.getAgentRoles());
            AgentRole role = agentRoles.stream().filter(r -> r.getName().equals(roleName)).findAny().orElseThrow(
                () -> new IllegalArgumentException("No agent role with name = \"" + roleName + "\" exists"));
            HashSet<String> newSids = new HashSet<>(role.getSids());
            newSids.add(sid);
            agentRoles.remove(role);
            agentRoles.add(new AgentRole(role.getName(), role.getPermissions(), role.getAgents(), newSids));
            return new FolderBasedAuthorizationStrategy(strategy.getGlobalRoles(), strategy.getFolderRoles(), agentRoles);
        });
    }

    /**
     * Assigns the {@code sid} to the {@link FolderRole} identified by {@code roleName}.
     *
     * @param sid      this sid will be assigned to the {@link FolderRole} with the name equal to {@code roleName}.
     * @param roleName the name of the folder role
     * @throws IllegalArgumentException when no folder role with name equal to {@code roleName} exists
     * @throws IllegalArgumentException when the {@code sid} is empty
     */
    public static void assignSidToFolderRole(String sid, String roleName) {
        if (StringUtils.isBlank(sid)) {
            throw new IllegalArgumentException("Sid should not be blank.");
        }

        run(strategy -> {
            Set<FolderRole> folderRoles = new HashSet<>(strategy.getFolderRoles());
            FolderRole role = folderRoles.stream().filter(r -> r.getName().equals(roleName)).findAny().orElseThrow(
                () -> new IllegalArgumentException("No folder role with name = \"" + roleName + "\" exists"));
            HashSet<String> newSids = new HashSet<>(role.getSids());
            newSids.add(sid);
            folderRoles.remove(role);
            folderRoles.add(new FolderRole(role.getName(), role.getPermissions(), role.getFolderNames(), newSids));
            return new FolderBasedAuthorizationStrategy(strategy.getGlobalRoles(), folderRoles, strategy.getAgentRoles());
        });
    }

    /**
     * Deletes the {@link GlobalRole} with name equal to {@code roleName}.
     *
     * @param roleName the name of the role to be deleted
     * @throws IllegalArgumentException when no global role with name equal to {@code roleName} exists
     */
    public static void deleteGlobalRole(String roleName) {
        if (roleName.equals("admin")) {
            throw new IllegalArgumentException("Cannot delete the admin role.");
        }

        run(strategy -> {
            Set<GlobalRole> globalRoles = new HashSet<>(strategy.getGlobalRoles());
            GlobalRole role = globalRoles.stream().filter(r -> r.getName().equals(roleName)).findAny().orElseThrow(
                () -> new IllegalArgumentException("No global role with name = \"" + roleName + "\" exists"));
            globalRoles.remove(role);
            return new FolderBasedAuthorizationStrategy(globalRoles, strategy.getFolderRoles(), strategy.getAgentRoles());
        });
    }

    /**
     * Deletes the {@link FolderRole} with name equal to {@code roleName}.
     *
     * @param roleName the name of the role to be deleted
     * @throws IllegalArgumentException when no role with name equal to {@code roleName} exists
     */
    public static void deleteFolderRole(String roleName) {
        run(strategy -> {
            Set<FolderRole> folderRoles = new HashSet<>(strategy.getFolderRoles());
            FolderRole role = folderRoles.stream().filter(r -> r.getName().equals(roleName)).findAny().orElseThrow(
                () -> new IllegalArgumentException("No folder role with name = \"" + roleName + "\" exists"));
            folderRoles.remove(role);
            return new FolderBasedAuthorizationStrategy(strategy.getGlobalRoles(), folderRoles, strategy.getAgentRoles());
        });
    }

    /**
     * Deletes the {@link AgentRole} with name equal to {@code roleName}.
     *
     * @param roleName the name of the role to be deleted
     * @throws IllegalArgumentException when no role with name equal to {@code roleName} exists
     */
    public static void deleteAgentRole(String roleName) {
        run(strategy -> {
            Set<AgentRole> agentRoles = new HashSet<>(strategy.getAgentRoles());
            AgentRole role = agentRoles.stream().filter(r -> r.getName().equals(roleName)).findAny().orElseThrow(
                () -> new IllegalArgumentException("No agent role with name = \"" + roleName + "\" exists"));
            agentRoles.remove(role);
            return new FolderBasedAuthorizationStrategy(strategy.getGlobalRoles(), strategy.getFolderRoles(), agentRoles);
        });
    }

    /**
     * Removes the {@code sid} from the {@link GlobalRole} with name equal to @{code roleName}.
     *
     * @param roleName the name of the role.
     * @param sid      the sid that will be removed.
     * @throws IllegalArgumentException when no {@link GlobalRole} with the given {@code roleName} exists.
     * @since TODO
     */
    public static void removeSidFromGlobalRole(String sid, String roleName) {
        run(strategy -> {
            Set<GlobalRole> globalRoles = new HashSet<>(strategy.getGlobalRoles());
            GlobalRole role = globalRoles.stream().filter(r -> r.getName().equals(roleName)).findAny().orElseThrow(
                () -> new IllegalArgumentException("No global role with name equal to \"" + roleName + "\" exists.")
            );
            Set<String> sids = new HashSet<>(role.getSids());
            sids.remove(sid);
            globalRoles.remove(role);
            globalRoles.add(new GlobalRole(role.getName(), role.getPermissions(), sids));
            return new FolderBasedAuthorizationStrategy(globalRoles, strategy.getFolderRoles(), strategy.getAgentRoles());
        });
    }

    /**
     * Removes the {@code sid} from the {@link FolderRole} with name equal to @{code roleName}.
     *
     * @param roleName the name of the role.
     * @param sid      the sid that will be removed.
     * @throws IllegalArgumentException when no {@link FolderRole} with the given {@code roleName} exists.
     * @since TODO
     */
    public static void removeSidFromFolderRole(String sid, String roleName) {
        run(strategy -> {
            Set<FolderRole> folderRoles = new HashSet<>(strategy.getFolderRoles());
            FolderRole role = folderRoles.stream().filter(r -> r.getName().equals(roleName)).findAny().orElseThrow(
                () -> new IllegalArgumentException("No folder role with name equal to \"" + roleName + "\" exists.")
            );
            Set<String> sids = new HashSet<>(role.getSids());
            sids.remove(sid);
            folderRoles.remove(role);
            folderRoles.add(new FolderRole(role.getName(), role.getPermissions(), role.getFolderNames(), sids));
            return new FolderBasedAuthorizationStrategy(strategy.getGlobalRoles(), folderRoles, strategy.getAgentRoles());
        });
    }

    /**
     * Removes the {@code sid} from the {@link AgentRole} with name equal to @{code roleName}.
     *
     * @param roleName the name of the role.
     * @param sid      the sid that will be removed.
     * @throws IllegalArgumentException when no {@link AgentRole} with the given {@code roleName} exists.
     * @since TODO
     */
    public static void removeSidFromAgentRole(String sid, String roleName) {
        run(strategy -> {
            Set<AgentRole> agentRoles = new HashSet<>(strategy.getAgentRoles());
            AgentRole role = agentRoles.stream().filter(r -> r.getName().equals(roleName)).findAny().orElseThrow(
                () -> new IllegalArgumentException("No agent role with name equal to \"" + roleName + "\" exists.")
            );
            Set<String> sids = new HashSet<>(role.getSids());
            sids.remove(sid);
            agentRoles.remove(role);
            agentRoles.add(new AgentRole(role.getName(), role.getPermissions(), role.getAgents(), sids));
            return new FolderBasedAuthorizationStrategy(strategy.getGlobalRoles(), strategy.getFolderRoles(), agentRoles);
        });
    }
}
