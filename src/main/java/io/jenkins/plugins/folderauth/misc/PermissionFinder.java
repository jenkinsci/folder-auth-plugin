package io.jenkins.plugins.folderauth.misc;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Implements lookup for {@link Permission}s.
 */
// Imported from https://github.com/jenkinsci/configuration-as-code-plugin/blob/727c976d137461f146b301f302d1552ca81de75e/plugin/src/main/java/io/jenkins/plugins/casc/util/PermissionFinder.java
@Restricted(NoExternalUse.class)
public class PermissionFinder {

    /** For Matrix Auth - Title/Permission **/
    private static final Pattern PERMISSION_PATTERN = Pattern.compile("^([^\\/]+)\\/(.+)$");

    /**
     * Attempt to match a given permission to what is defined in the UI.
     * @param id String of the form "Title/Permission" (Look in the UI) for a particular permission
     * @return a matched permission
     */
    @CheckForNull
    public static Permission findPermission(String id) {
        if (id.contains("/")) {
            final String resolvedId = findPermissionId(id);
            return resolvedId != null ? Permission.fromId(resolvedId) : null;
        } else {
            return Permission.fromId(id);
        }
    }

    /**
     * Attempt to match a given permission to what is defined in the UI.
     * @param id String of the form "Title/Permission" (Look in the UI) for a particular permission
     * @return a matched permission ID
     */
    @CheckForNull
    public static String findPermissionId(String id) {
        List<PermissionGroup> pgs = PermissionGroup.getAll();
        Matcher m = PERMISSION_PATTERN.matcher(id);
        if(m.matches()) {
            String owner = m.group(1);
            String name = m.group(2);
            for(PermissionGroup pg : pgs) {
                if(pg.owner.equals(Permission.class)) {
                    continue;
                }
                if(pg.getId().equals(owner)) {
                    return pg.owner.getName() + "." + name;
                }
            }
        }
        return null;
    }
}
