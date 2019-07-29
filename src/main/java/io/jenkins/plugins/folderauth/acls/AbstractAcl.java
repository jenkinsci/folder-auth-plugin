package io.jenkins.plugins.folderauth.acls;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.security.Permission;
import hudson.security.SidACL;
import io.jenkins.plugins.folderauth.misc.PermissionWrapper;
import jenkins.model.Jenkins;
import org.acegisecurity.acls.sid.Sid;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

abstract class AbstractAcl extends SidACL {

    private static ConcurrentHashMap<Permission, Set<Permission>> implyingPermissionsCache = new ConcurrentHashMap<>();

    static {
        Permission.getAll().forEach(AbstractAcl::cacheImplyingPermissions);
    }

    private static Set<Permission> cacheImplyingPermissions(Permission permission) {
        Set<Permission> implyingPermissions;

        if (PermissionWrapper.DANGEROUS_PERMISSIONS.contains(permission)) {
            // dangerous permissions should be deferred to Jenkins.ADMINISTER
            implyingPermissions = getImplyingPermissions(Jenkins.ADMINISTER);
        } else {
            implyingPermissions = new HashSet<>();

            for (Permission p = permission; p != null; p = p.impliedBy) {
                implyingPermissions.add(p);
            }
        }

        implyingPermissionsCache.put(permission, implyingPermissions);
        return implyingPermissions;
    }

    private static Set<Permission> getImplyingPermissions(Permission p) {
        Set<Permission> permissions = implyingPermissionsCache.get(p);
        if (permissions != null) {
            return permissions;
        } else {
            return cacheImplyingPermissions(p);
        }
    }

    /**
     * Maps each sid to the set of permissions assigned to it.
     * <p>
     * The implementation should ensure that this list contains accurate permissions for each sid.
     */
    protected Map<String, Set<Permission>> permissionList = new ConcurrentHashMap<>();

    @Override
    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL",
            justification = "hudson.security.SidACL requires null when unknown")
    @Nullable
    protected Boolean hasPermission(Sid sid, Permission permission) {
        if (PermissionWrapper.DANGEROUS_PERMISSIONS.contains(permission)) {
            permission = Jenkins.ADMINISTER;
        }

        Set<Permission> permissions = permissionList.get(toString(sid));
        if (permissions != null && CollectionUtils.containsAny(permissions, getImplyingPermissions(permission))) {
            return true;
        }

        return null;
    }
}
