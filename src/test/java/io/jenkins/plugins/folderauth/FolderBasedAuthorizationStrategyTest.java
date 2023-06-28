package io.jenkins.plugins.folderauth;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.google.common.collect.ImmutableSet;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.security.AuthorizationStrategy;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import io.jenkins.plugins.folderauth.roles.FolderRole;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;
import java.util.HashSet;

import static io.jenkins.plugins.folderauth.misc.PermissionWrapper.wrapPermissions;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class FolderBasedAuthorizationStrategyTest {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private Folder root;
    private Folder child1;
    private Folder child2;
    private Folder child3;

    private FreeStyleProject job1;
    private FreeStyleProject job2;

    private User admin;
    private User user1;
    private User user2;

    @Before
    public void setUp() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());

        FolderBasedAuthorizationStrategy strategy = new FolderBasedAuthorizationStrategy(Collections.emptySet(),
                Collections.emptySet(), Collections.emptySet());
        jenkins.setAuthorizationStrategy(strategy);

        final String adminRoleName = "adminRole";
        final String overallReadRoleName = "overallRead";

        FolderAuthorizationStrategyAPI.addGlobalRole(new GlobalRole(adminRoleName,
                wrapPermissions(FolderAuthorizationStrategyManagementLink.getSafePermissions(
                        new HashSet<>(PermissionGroup.getAll())))));

        FolderAuthorizationStrategyAPI.assignSidToGlobalRole("admin", adminRoleName);

        FolderAuthorizationStrategyAPI.addGlobalRole(new GlobalRole(overallReadRoleName, wrapPermissions(Permission.READ)));
        FolderAuthorizationStrategyAPI.assignSidToGlobalRole("authenticated", overallReadRoleName);

        FolderAuthorizationStrategyAPI.addFolderRole(new FolderRole("folderRole1", wrapPermissions(Item.READ),
                ImmutableSet.of("root")));
        FolderAuthorizationStrategyAPI.assignSidToFolderRole("user1", "folderRole1");
        FolderAuthorizationStrategyAPI.assignSidToFolderRole("user2", "folderRole1");
        FolderAuthorizationStrategyAPI.assignSidToFolderRole("anonymous", "folderRole1");

        FolderAuthorizationStrategyAPI.addFolderRole(new FolderRole("folderRole2", wrapPermissions(Item.CONFIGURE, Item.DELETE),
                ImmutableSet.of("root/child1")));
        FolderAuthorizationStrategyAPI.assignSidToFolderRole("user2", "folderRole2");

        /*
         * Folder hierarchy for the test
         *
         *             root
         *             /  \
         *        child1   child2
         *          /        \
         *        child3     job1
         *         /
         *        job2
         */

        root = jenkins.createProject(Folder.class, "root");
        child1 = root.createProject(Folder.class, "child1");
        child2 = root.createProject(Folder.class, "child2");
        child3 = child1.createProject(Folder.class, "child3");

        job1 = child2.createProject(FreeStyleProject.class, "job1");
        job2 = child3.createProject(FreeStyleProject.class, "job2");

        admin = User.getById("admin", true);
        user1 = User.getById("user1", true);
        user2 = User.getById("user2", true);
        user2.setFullName("Test User2");
    }

    @Test
    public void permissionTest() {
        Jenkins jenkins = jenkinsRule.jenkins;

        try (ACLContext ignored = ACL.as(admin)) {
            assertTrue(jenkins.hasPermission(Jenkins.ADMINISTER));
            assertTrue(child3.hasPermission(Item.CONFIGURE));
            assertTrue(job1.hasPermission(Item.READ));
            assertTrue(job2.hasPermission(Item.CREATE));
        }

        try (ACLContext ignored = ACL.as(user1)) {
            assertTrue(jenkins.hasPermission(Permission.READ));
            assertTrue(root.hasPermission(Item.READ));
            assertTrue(job1.hasPermission(Item.READ));
            assertTrue(job2.hasPermission(Item.READ));

            assertFalse(job1.hasPermission(Item.CREATE));
            assertFalse(job1.hasPermission(Item.DELETE));
            assertFalse(job1.hasPermission(Item.CONFIGURE));
            assertFalse(job2.hasPermission(Item.CREATE));
            assertFalse(job2.hasPermission(Item.CONFIGURE));
        }

        try (ACLContext ignored = ACL.as(user2)) {
            assertTrue(jenkins.hasPermission(Permission.READ));
            assertTrue(child2.hasPermission(Item.READ));
            assertTrue(child1.hasPermission(Item.READ));
            assertTrue(job2.hasPermission(Item.CONFIGURE));
            assertFalse(job1.hasPermission(Item.CONFIGURE));
        }
    }

    @Test
    public void SIDToFullNameLookupTest() {
        Jenkins jenkins = jenkinsRule.jenkins;

        AuthorizationStrategy strategy = jenkins.getAuthorizationStrategy();
        if (strategy instanceof FolderBasedAuthorizationStrategy) {
            FolderBasedAuthorizationStrategy actualStrategy = (FolderBasedAuthorizationStrategy) strategy;
            for (FolderRole role: actualStrategy.getFolderRoles()) {
              if (role.getName().equals("folderRole1")) {
                  assertEquals("anonymous, user1(user1), user2(Test User2)",role.getSidsCommaSeparated());
              }
            }
        }
    }
}
