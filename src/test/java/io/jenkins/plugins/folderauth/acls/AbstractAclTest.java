package io.jenkins.plugins.folderauth.acls;

import hudson.model.User;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;

import static io.jenkins.plugins.folderauth.misc.PermissionWrapper.wrapPermissions;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class AbstractAclTest {

    private static final PermissionGroup PERMISSIONS = new PermissionGroup(
            AbstractAclTest.class, hudson.security.Messages._Permission_Permissions_Title());
    public static final Permission OPTIONAL = new Permission(
            PERMISSIONS, "Optional", null, Jenkins.ADMINISTER, PermissionScope.JENKINS);
    public static final Permission IMPLIED = new Permission(
            PERMISSIONS, "Implied", null, OPTIONAL, PermissionScope.JENKINS);

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> aclTypes() {
        return Arrays.asList(new Object[][] {{"global"}, {"scoped"}});
    }

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private final String aclType;

    public AbstractAclTest(String aclType) {
        this.aclType = aclType;
    }

    private AbstractAcl acl(Permission permission) {
        if ("global".equals(aclType)) {
            return new GlobalAclImpl(Collections.singleton(new GlobalRole(
                    "role", wrapPermissions(permission), Collections.singleton("user"))));
        }
        GenericAclImpl acl = new GenericAclImpl();
        acl.assignPermissions(Collections.singleton("user"), Collections.singleton(permission));
        return acl;
    }

    private Authentication user() {
        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());
        return Objects.requireNonNull(User.getById("user", true)).impersonate();
    }

    @Test
    public void disabledManageGrantIsIgnoredWithoutRebuildingAcl() {
        boolean enabled = Jenkins.MANAGE.getEnabled();
        try {
            Jenkins.MANAGE.setEnabled(true);
            AbstractAcl manageAcl = acl(Jenkins.MANAGE);
            AbstractAcl adminAcl = acl(Jenkins.ADMINISTER);
            Authentication user = user();
            assertTrue(manageAcl.hasPermission(user, Jenkins.MANAGE));

            Jenkins.MANAGE.setEnabled(false);
            assertFalse(manageAcl.hasPermission(user, Jenkins.MANAGE));
            assertTrue(adminAcl.hasPermission(user, Jenkins.MANAGE));

            Jenkins.MANAGE.setEnabled(true);
            assertTrue(manageAcl.hasPermission(user, Jenkins.MANAGE));
        } finally {
            Jenkins.MANAGE.setEnabled(enabled);
        }
    }

    @Test
    public void onlyEnabledGrantsMayImplyPermissions() {
        boolean optionalEnabled = OPTIONAL.getEnabled();
        boolean impliedEnabled = IMPLIED.getEnabled();
        try {
            OPTIONAL.setEnabled(true);
            IMPLIED.setEnabled(true);
            AbstractAcl optionalAcl = acl(OPTIONAL);
            AbstractAcl adminAcl = acl(Jenkins.ADMINISTER);
            Authentication user = user();
            assertTrue(optionalAcl.hasPermission(user, IMPLIED));

            OPTIONAL.setEnabled(false);
            assertFalse(optionalAcl.hasPermission(user, IMPLIED));
            assertTrue(adminAcl.hasPermission(user, IMPLIED));

            // A disabled intermediate permission must not interrupt the implication chain.
            IMPLIED.setEnabled(false);
            assertTrue(adminAcl.hasPermission(user, IMPLIED));
            OPTIONAL.setEnabled(true);
            assertTrue(optionalAcl.hasPermission(user, IMPLIED));
        } finally {
            OPTIONAL.setEnabled(optionalEnabled);
            IMPLIED.setEnabled(impliedEnabled);
        }
    }
}
