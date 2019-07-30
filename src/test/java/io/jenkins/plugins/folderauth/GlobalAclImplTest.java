package io.jenkins.plugins.folderauth;

import com.google.common.collect.ImmutableSet;
import hudson.model.Item;
import hudson.model.User;
import io.jenkins.plugins.folderauth.acls.GlobalAclImpl;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static io.jenkins.plugins.folderauth.misc.PermissionWrapper.wrapPermissions;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GlobalAclImplTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void hasPermission() {
        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());
        Set<GlobalRole> globalRoles = new HashSet<>();

        GlobalRole role1 = new GlobalRole("role1", wrapPermissions(Item.DISCOVER, Item.READ),
            ImmutableSet.of("foo", "bar", "baz"));
        GlobalRole role2 = new GlobalRole("role2", wrapPermissions(Item.READ, Item.CONFIGURE, Item.BUILD),
            ImmutableSet.of("baz"));
        GlobalRole adminRole = new GlobalRole("adminRole", wrapPermissions(Jenkins.ADMINISTER),
            ImmutableSet.of("admin"));

        globalRoles.add(role1);
        globalRoles.add(role2);
        globalRoles.add(adminRole);

        GlobalAclImpl acl = new GlobalAclImpl(globalRoles);

        Authentication foo = Objects.requireNonNull(User.getById("foo", true)).impersonate();
        Authentication bar = Objects.requireNonNull(User.getById("bar", true)).impersonate();
        Authentication baz = Objects.requireNonNull(User.getById("baz", true)).impersonate();
        Authentication admin = Objects.requireNonNull(User.getById("admin", true)).impersonate();

        assertTrue(acl.hasPermission(foo, Item.READ));
        assertFalse(acl.hasPermission(foo, Item.CONFIGURE));
        assertFalse(acl.hasPermission(foo, Jenkins.ADMINISTER));

        assertTrue(acl.hasPermission(bar, Item.DISCOVER));
        assertTrue(acl.hasPermission(baz, Item.CONFIGURE));
        assertFalse(acl.hasPermission(baz, Jenkins.ADMINISTER));

        assertTrue(acl.hasPermission(admin, Jenkins.ADMINISTER));
    }
}
