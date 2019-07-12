package io.jenkins.plugins.folderauth;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.ManagementLink;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.security.AuthorizationStrategy;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import io.jenkins.plugins.folderauth.misc.FolderRoleCreationRequest;
import io.jenkins.plugins.folderauth.misc.GlobalRoleCreationRequest;
import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import io.jenkins.plugins.folderauth.roles.FolderRole;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.json.JsonBody;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class FolderAuthorizationStrategyManagementLink extends ManagementLink {
    private static final Logger LOGGER = Logger.getLogger(FolderAuthorizationStrategyManagementLink.class.getName());

    @CheckForNull
    @Override
    public String getIconFileName() {
        return Jenkins.get().getAuthorizationStrategy() instanceof FolderBasedAuthorizationStrategy ?
                "lock.png" : null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "folder-auth";
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return Messages.FolderBasedAuthorizationStrategy_DisplayName();
    }

    @Nonnull
    @Restricted(NoExternalUse.class)
    public Set<Permission> getGlobalPermissions() {
        HashSet<PermissionGroup> groups = new HashSet<>(PermissionGroup.getAll());
        groups.remove(PermissionGroup.get(Permission.class));
        return getSafePermissions(groups);
    }

    @Nonnull
    @Restricted(NoExternalUse.class)
    public Set<Permission> getFolderPermissions() {
        HashSet<PermissionGroup> groups = new HashSet<>(PermissionGroup.getAll());
        groups.remove(PermissionGroup.get(Computer.class));
        groups.remove(PermissionGroup.get(Permission.class));
        return getSafePermissions(groups);
    }

    /**
     * Adds a {@link GlobalRole} to {@link FolderBasedAuthorizationStrategy}.
     *
     * @param request the request to create the {@link GlobalRole}
     * @throws IOException           when unable to add Global role
     * @throws IllegalStateException when {@link Jenkins#getAuthorizationStrategy()} is
     *                               not {@link FolderBasedAuthorizationStrategy}
     */
    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void doAddGlobalRole(@JsonBody GlobalRoleCreationRequest request) throws IOException {
        Jenkins jenkins = Jenkins.get();
        jenkins.checkPermission(Jenkins.ADMINISTER);
        AuthorizationStrategy strategy = jenkins.getAuthorizationStrategy();
        if (strategy instanceof FolderBasedAuthorizationStrategy) {
            ((FolderBasedAuthorizationStrategy) strategy).addGlobalRole(request.getGlobalRole());
        } else {
            throw new IllegalStateException(Messages.FolderBasedAuthorizationStrategy_NotCurrentStrategy());
        }
    }

    /**
     * Assigns {@code sid} to the global role identified by {@code roleName}.
     * <p>
     * Does not do anything if a role corresponding to the {@code roleName} does not exist.
     *
     * @param roleName the name of the global to which {@code sid} will be assigned to.
     * @param sid      the sid of the user/group to be assigned.
     * @throws IOException                      when unable to assign the Sid to the role
     * @throws IllegalStateException            when {@link Jenkins#getAuthorizationStrategy()} is
     *                                          not {@link FolderBasedAuthorizationStrategy}
     * @throws java.util.NoSuchElementException when no role with name equal to {@code roleName} exists.
     */
    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void doAssignSidToGlobalRole(@QueryParameter(required = true) String roleName,
                                        @QueryParameter(required = true) String sid) throws IOException {
        Jenkins jenkins = Jenkins.get();
        jenkins.checkPermission(Jenkins.ADMINISTER);
        AuthorizationStrategy strategy = jenkins.getAuthorizationStrategy();
        if (strategy instanceof FolderBasedAuthorizationStrategy) {
            ((FolderBasedAuthorizationStrategy) strategy).assignSidToGlobalRole(roleName, sid);
            redirect();
        } else {
            throw new IllegalStateException(Messages.FolderBasedAuthorizationStrategy_NotCurrentStrategy());
        }
    }

    /**
     * Adds a {@link FolderRole} to {@link FolderBasedAuthorizationStrategy}.
     *
     * @param request the request to create the role
     * @throws IOException           when unable to add the role
     * @throws IllegalStateException when {@link Jenkins#getAuthorizationStrategy()} is
     *                               not {@link FolderBasedAuthorizationStrategy}
     */
    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void doAddFolderRole(@JsonBody FolderRoleCreationRequest request) throws IOException {
        Jenkins jenkins = Jenkins.get();
        jenkins.checkPermission(Jenkins.ADMINISTER);
        AuthorizationStrategy strategy = jenkins.getAuthorizationStrategy();
        if (strategy instanceof FolderBasedAuthorizationStrategy) {
            ((FolderBasedAuthorizationStrategy) strategy).addFolderRole(request.getFolderRole());
        }
    }

    /**
     * Assigns {@code sid} to the folder role identified by {@code roleName}.
     * <p>
     * Does not do anything if a role corresponding to the {@code roleName} does not exist.
     *
     * @param roleName the name of the global to which {@code sid} will be assigned to.
     * @param sid      the sid of the user/group to be assigned.
     * @throws IOException                      when unable to assign the Sid to the role
     * @throws IllegalStateException            when {@link Jenkins#getAuthorizationStrategy()} is
     *                                          not {@link FolderBasedAuthorizationStrategy}
     * @throws java.util.NoSuchElementException when no role with name equal to {@code roleName} exists.
     */
    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void doAssignSidToFolderRole(@QueryParameter(required = true) String roleName,
                                        @QueryParameter(required = true) String sid) throws IOException {
        Jenkins jenkins = Jenkins.get();
        jenkins.checkPermission(Jenkins.ADMINISTER);
        AuthorizationStrategy strategy = jenkins.getAuthorizationStrategy();
        if (strategy instanceof FolderBasedAuthorizationStrategy) {
            ((FolderBasedAuthorizationStrategy) strategy).assignSidToFolderRole(roleName, sid);
            redirect();
        } else {
            throw new IllegalStateException(Messages.FolderBasedAuthorizationStrategy_NotCurrentStrategy());
        }
    }

    /**
     * Redirects to the same page that initiated the request.
     */
    private void redirect() {
        try {
            Stapler.getCurrentResponse().forwardToPreviousPage(Stapler.getCurrentRequest());
        } catch (ServletException | IOException e) {
            LOGGER.log(Level.WARNING, "Unable to redirect to previous page.");
        }
    }

    @Nonnull
    @Restricted(NoExternalUse.class)
    public Set<GlobalRole> getGlobalRoles() {
        AuthorizationStrategy strategy = Jenkins.get().getAuthorizationStrategy();
        if (strategy instanceof FolderBasedAuthorizationStrategy) {
            return ((FolderBasedAuthorizationStrategy) strategy).getGlobalRoles();
        } else {
            throw new IllegalStateException(Messages.FolderBasedAuthorizationStrategy_NotCurrentStrategy());
        }
    }

    /**
     * Get all {@link AbstractFolder}s in the system
     *
     * @return folders in the system
     */
    @Nonnull
    @Restricted(NoExternalUse.class)
    public List<AbstractFolder> getAllFolders() {
        Jenkins jenkins = Jenkins.get();
        jenkins.checkPermission(Jenkins.ADMINISTER);
        List<AbstractFolder> folders;

        try (ACLContext ignored = ACL.as(ACL.SYSTEM)) {
            folders = jenkins.getAllItems(AbstractFolder.class);
        }

        return folders;
    }

    /**
     * Returns the {@link FolderRole}s used by the {@link FolderBasedAuthorizationStrategy}.
     *
     * @return the {@link FolderRole}s used by the {@link FolderBasedAuthorizationStrategy}
     * @throws IllegalStateException when {@link Jenkins#getAuthorizationStrategy()} is
     *                               not {@link FolderBasedAuthorizationStrategy}
     */
    @Nonnull
    @Restricted(NoExternalUse.class)
    public Set<FolderRole> getFolderRoles() {
        AuthorizationStrategy strategy = Jenkins.get().getAuthorizationStrategy();
        if (strategy instanceof FolderBasedAuthorizationStrategy) {
            return ((FolderBasedAuthorizationStrategy) strategy).getFolderRoles();
        } else {
            throw new IllegalStateException(Messages.FolderBasedAuthorizationStrategy_NotCurrentStrategy());
        }
    }

    /**
     * Deletes a global role.
     *
     * @param roleName the name of the role to be deleted
     * @throws IOException                      when unable to delete the role
     * @throws IllegalStateException            when {@link Jenkins#getAuthorizationStrategy()} is
     *                                          not {@link FolderBasedAuthorizationStrategy}
     * @throws IllegalArgumentException         when trying to delete the admin role
     * @throws java.util.NoSuchElementException when no role with name equal to {@code roleName} exists.
     */
    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void doDeleteGlobalRole(@QueryParameter(required = true) String roleName)
            throws IOException {
        Jenkins jenkins = Jenkins.get();
        jenkins.checkPermission(Jenkins.ADMINISTER);
        AuthorizationStrategy strategy = jenkins.getAuthorizationStrategy();
        if (strategy instanceof FolderBasedAuthorizationStrategy) {
            ((FolderBasedAuthorizationStrategy) strategy).deleteGlobalRole(roleName);
            redirect();
        } else {
            throw new IllegalStateException(Messages.FolderBasedAuthorizationStrategy_NotCurrentStrategy());
        }
    }

    /**
     * Deletes a folder role.
     *
     * @param roleName the name of the role to be deleted
     * @throws IOException                      when unable to delete the role
     * @throws IllegalStateException            when {@link Jenkins#getAuthorizationStrategy()} is
     *                                          not {@link FolderBasedAuthorizationStrategy}
     * @throws java.util.NoSuchElementException when no role with name equal to {@code roleName} exists.
     */
    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void doDeleteFolderRole(@QueryParameter(required = true) String roleName)
            throws IOException {
        Jenkins jenkins = Jenkins.get();
        jenkins.checkPermission(Jenkins.ADMINISTER);
        AuthorizationStrategy strategy = jenkins.getAuthorizationStrategy();
        if (strategy instanceof FolderBasedAuthorizationStrategy) {
            ((FolderBasedAuthorizationStrategy) strategy).deleteFolderRole(roleName);
            redirect();
        } else {
            throw new IllegalStateException(Messages.FolderBasedAuthorizationStrategy_NotCurrentStrategy());
        }
    }

    static Set<Permission> getSafePermissions(Set<PermissionGroup> groups) {
        HashSet<Permission> safePermissions = new HashSet<>();
        groups.stream().map(PermissionGroup::getPermissions).forEach(safePermissions::addAll);
        safePermissions.removeAll(PermissionWrapper.DANGEROUS_PERMISSIONS);
        return safePermissions;
    }
}
