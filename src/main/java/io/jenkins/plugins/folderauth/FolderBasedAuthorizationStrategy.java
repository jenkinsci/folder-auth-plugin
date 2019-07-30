package io.jenkins.plugins.folderauth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.security.ACL;
import hudson.security.AuthorizationStrategy;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.SidACL;
import io.jenkins.plugins.folderauth.acls.GenericAclImpl;
import io.jenkins.plugins.folderauth.acls.GlobalAclImpl;
import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import io.jenkins.plugins.folderauth.roles.AbstractRole;
import io.jenkins.plugins.folderauth.roles.AgentRole;
import io.jenkins.plugins.folderauth.roles.FolderRole;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.acegisecurity.acls.sid.PrincipalSid;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * An {@link AuthorizationStrategy} that controls access to {@link com.cloudbees.hudson.plugins.folder.AbstractFolder}s
 * through {@link FolderRole}s, to {@link Computer}s through {@link AgentRole}s. Also provides global permissions
 * through {@link GlobalRole}s.
 * <p></p>
 * All objects of this class are immutable. To modify the data for this strategy,
 * please use the {@link FolderAuthorizationStrategyAPI}.
 *
 * @see FolderAuthorizationStrategyAPI for modifying the roles
 * @see FolderAuthorizationStrategyManagementLink for REST API methods
 */
@ParametersAreNonnullByDefault
public class FolderBasedAuthorizationStrategy extends AuthorizationStrategy {
    private static final String ADMIN_ROLE_NAME = "admin";
    private static final String FOLDER_SEPARATOR = "/";

    private final Set<AgentRole> agentRoles;
    private final Set<GlobalRole> globalRoles;
    private final Set<FolderRole> folderRoles;

    /**
     * An {@link ACL} that works only on {@link #globalRoles}.
     * <p>
     * All other {@link ACL} should inherit from this {@link ACL}.
     */
    private transient GlobalAclImpl globalAcl;
    /**
     * Maps full name of jobs to their respective {@link ACL}s. The {@link ACL}s here do not
     * get inheritance from their parents.
     */
    private transient ConcurrentHashMap<String, GenericAclImpl> jobAcls;
    /**
     * Maps full name of the Agents to their respective {@link ACL}s. Inheritance is not needed here
     * because Agents are not nestable.
     */
    private transient ConcurrentHashMap<String, GenericAclImpl> agentAcls;
    /**
     * Contains the ACLs for projects that do not need any further inheritance.
     * <p>
     * Invalidate this cache whenever folder roles are updated.
     */
    private transient Cache<String, SidACL> jobAclCache;

    @DataBoundConstructor
    public FolderBasedAuthorizationStrategy(Set<GlobalRole> globalRoles, Set<FolderRole> folderRoles,
                                            Set<AgentRole> agentRoles) {
        this.agentRoles = new HashSet<>(agentRoles);
        this.globalRoles = new HashSet<>(globalRoles);
        this.folderRoles = new HashSet<>(folderRoles);

        // the sets above should NOT be modified. They are not Collections.unmodifiableSet()
        // because that complicates the serialized XML and add unnecessary nesting.

        init();
    }

    /**
     * Clears and recalculates {@code jobAcls}.
     */
    private void updateJobAcls() {
        jobAcls.clear();

        for (FolderRole role : folderRoles) {
            updateAclForFolderRole(role);
        }
    }

    private synchronized void updateAgentAcls() {
        agentAcls.clear();

        for (AgentRole role : agentRoles) {
            updateAclForAgentRole(role);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return an {@link ACL} formed using just globalRoles
     */
    @Nonnull
    @Override
    public GlobalAclImpl getRootACL() {
        return globalAcl;
    }

    /**
     * Used to initialize transient fields when loaded from disk
     *
     * @return {@code this}
     */
    @Nonnull
    @SuppressWarnings("unused")
    private FolderBasedAuthorizationStrategy readResolve() {
        init();
        return this;
    }

    /**
     * Gets the {@link ACL} for a {@link Job}
     *
     * @return the {@link ACL} for the {@link Job}
     */
    @Nonnull
    @Override
    public SidACL getACL(Job<?, ?> project) {
        return getACL((AbstractItem) project);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public SidACL getACL(AbstractItem item) {
        String fullName = item.getFullName();
        SidACL acl = jobAclCache.getIfPresent(fullName);

        if (acl != null) {
            return acl;
        }

        String[] splits = fullName.split(FOLDER_SEPARATOR);
        StringBuilder sb = new StringBuilder(fullName.length());
        acl = globalAcl;

        // Roles on a folder are applicable to all children
        for (String str : splits) {
            sb.append(str);
            SidACL newAcl = jobAcls.get(sb.toString());
            if (newAcl != null) {
                acl = acl.newInheritingACL(newAcl);
            }
            sb.append(FOLDER_SEPARATOR);
        }

        jobAclCache.put(fullName, acl);
        return acl;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public SidACL getACL(@Nonnull Computer computer) {
        String name = computer.getName();
        SidACL acl = agentAcls.get(name);
        if (acl == null) {
            return globalAcl;
        } else {
            // TODO: cache these ACLs
            return globalAcl.newInheritingACL(acl);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Collection<String> getGroups() {
        Set<String> groups = ConcurrentHashMap.newKeySet();
        agentRoles.stream().parallel().map(AbstractRole::getSids).forEach(groups::addAll);
        globalRoles.stream().parallel().map(AbstractRole::getSids).forEach(groups::addAll);
        folderRoles.stream().parallel().map(AbstractRole::getSids).forEach(groups::addAll);
        return Collections.unmodifiableCollection(groups);
    }

    /**
     * Returns the {@link GlobalRole}s on which this {@link AuthorizationStrategy} works.
     *
     * @return set of {@link GlobalRole}s on which this {@link AuthorizationStrategy} works.
     */
    @Nonnull
    public Set<GlobalRole> getGlobalRoles() {
        return Collections.unmodifiableSet(globalRoles);
    }

    /**
     * Returns the {@link AgentRole}s on which this {@link AuthorizationStrategy} works.
     *
     * @return set of {@link AgentRole}s on which this {@link AuthorizationStrategy} works.
     */
    @Nonnull
    public Set<AgentRole> getAgentRoles() {
        return Collections.unmodifiableSet(agentRoles);
    }

    /**
     * Returns the {@link FolderRole}s on which this {@link AuthorizationStrategy} works.
     *
     * @return {@link FolderRole}s on which this {@link AuthorizationStrategy} works
     */
    @Nonnull
    public Set<FolderRole> getFolderRoles() {
        return Collections.unmodifiableSet(folderRoles);
    }

    /**
     * Updates the ACL for the folder role
     * <p>
     * <b>Note: does not invalidate the cache</b>
     * <p>
     * Should be called when a folderRole has been updated.
     *
     * @param role the role to be updated
     */
    private void updateAclForFolderRole(FolderRole role) {
        for (String name : role.getFolderNames()) {
            updateGenericAcl(name, jobAcls, role);
        }
    }

    /**
     * Updates the ACL for the agent role
     * <p>
     * <b>Note: does not invalidate the cache</b>
     * <p>
     * Should be called when an agentRole has been updated.
     *
     * @param role the role to be updated
     */
    private void updateAclForAgentRole(AgentRole role) {
        for (String agent : role.getAgents()) {
            updateGenericAcl(agent, agentAcls, role);
        }
    }

    private void updateGenericAcl(String fullName, ConcurrentHashMap<String, GenericAclImpl> acls, AbstractRole role) {
        GenericAclImpl acl = acls.get(fullName);
        if (acl == null) {
            acl = new GenericAclImpl();
        }
        acl.assignPermissions(role.getSids(),
            role.getPermissions().stream().map(PermissionWrapper::getPermission).collect(Collectors.toSet()));
        acls.put(fullName, acl);
    }

    /**
     * Initializes the cache, generates ACLs and makes the {@link FolderBasedAuthorizationStrategy}
     * ready to work.
     */
    private void init() {
        jobAcls = new ConcurrentHashMap<>();
        agentAcls = new ConcurrentHashMap<>();

        jobAclCache = CacheBuilder.newBuilder()
                          .expireAfterWrite(1, TimeUnit.HOURS)
                          .maximumSize(2048)
                          .build();

        globalAcl = new GlobalAclImpl(globalRoles);
        updateJobAcls();
        updateAgentAcls();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<AuthorizationStrategy> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.FolderBasedAuthorizationStrategy_DisplayName();
        }

        @Nonnull
        @Override
        public FolderBasedAuthorizationStrategy newInstance(@Nullable StaplerRequest req, @Nonnull JSONObject formData) {
            AuthorizationStrategy strategy = Jenkins.get().getAuthorizationStrategy();
            if (strategy instanceof FolderBasedAuthorizationStrategy) {
                // this action was invoked from the 'Configure Global Security' page when the
                // old strategy was FolderBasedAuthorizationStrategy; return it back as formData would be empty
                return (FolderBasedAuthorizationStrategy) strategy;
            } else {
                // when this AuthorizationStrategy is selected for the first time, this makes the current
                // user admin (give all permissions) and prevents him/her from getting access denied.
                // The same thing happens in Role Strategy plugin. See RoleBasedStrategy.DESCRIPTOR.newInstance()

                HashSet<PermissionGroup> groups = new HashSet<>(PermissionGroup.getAll());
                groups.remove(PermissionGroup.get(Permission.class));
                Set<PermissionWrapper> adminPermissions = PermissionWrapper.wrapPermissions(
                    FolderAuthorizationStrategyManagementLink.getSafePermissions(groups));

                GlobalRole adminRole = new GlobalRole(ADMIN_ROLE_NAME, adminPermissions,
                    Collections.singleton(new PrincipalSid(Jenkins.getAuthentication()).getPrincipal()));

                return new FolderBasedAuthorizationStrategy(Collections.singleton(adminRole), Collections.emptySet(),
                    Collections.emptySet());
            }
        }
    }
}
