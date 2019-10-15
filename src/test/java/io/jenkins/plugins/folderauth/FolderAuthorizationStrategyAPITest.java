package io.jenkins.plugins.folderauth;

import hudson.model.Computer;
import hudson.model.Item;
import hudson.security.AuthorizationStrategy;
import hudson.security.Permission;
import io.jenkins.plugins.folderauth.roles.AgentRole;
import io.jenkins.plugins.folderauth.roles.FolderRole;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static io.jenkins.plugins.folderauth.misc.PermissionWrapper.wrapPermissions;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;


public class FolderAuthorizationStrategyAPITest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        FolderBasedAuthorizationStrategy strategy = new FolderBasedAuthorizationStrategy.DescriptorImpl()
                                                        .newInstance(null, new JSONObject(true));
        // should only create the admin global role
        assertEquals(1, strategy.getGlobalRoles().size());
        assertEquals(0, strategy.getFolderRoles().size());
        assertEquals(0, strategy.getAgentRoles().size());

        j.jenkins.setAuthorizationStrategy(strategy);
    }

    @Test
    public void addGlobalRole() {
        GlobalRole readRole = new GlobalRole("readEverything", wrapPermissions(Jenkins.READ), singleton("user1"));
        FolderAuthorizationStrategyAPI.addGlobalRole(readRole);
        AuthorizationStrategy a = j.jenkins.getAuthorizationStrategy();
        assertTrue(a instanceof FolderBasedAuthorizationStrategy);
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) a;
        assertTrue(strategy.getGlobalRoles().contains(readRole));
    }

    @Test
    public void addFolderRole() {
        FolderRole role = new FolderRole("readEverything", wrapPermissions(Jenkins.READ),
            singleton("folder1"), singleton("user1"));
        FolderAuthorizationStrategyAPI.addFolderRole(role);
        AuthorizationStrategy a = j.jenkins.getAuthorizationStrategy();
        assertTrue(a instanceof FolderBasedAuthorizationStrategy);
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) a;
        assertTrue(strategy.getFolderRoles().contains(role));
    }

    @Test
    public void addAgentRole() {
        AgentRole role = new AgentRole("readEverything", wrapPermissions(Jenkins.READ),
            singleton("agent1"), singleton("user1"));
        FolderAuthorizationStrategyAPI.addAgentRole(role);
        AuthorizationStrategy a = j.jenkins.getAuthorizationStrategy();
        assertTrue(a instanceof FolderBasedAuthorizationStrategy);
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) a;
        assertTrue(strategy.getAgentRoles().contains(role));
    }

    @Test
    public void assignSidToGlobalRole() {
        AuthorizationStrategy a = j.jenkins.getAuthorizationStrategy();
        assertTrue(a instanceof FolderBasedAuthorizationStrategy);
        FolderBasedAuthorizationStrategy oldStrategy = (FolderBasedAuthorizationStrategy) a;
        String adminUserSid = "adminUserSid";
        oldStrategy.getGlobalRoles().forEach(role -> assertFalse(role.getSids().contains(adminUserSid)));
        String adminRoleName = "admin";
        FolderAuthorizationStrategyAPI.assignSidToGlobalRole(adminUserSid, adminRoleName);

        // a new authorization strategy should have been set
        AuthorizationStrategy b = j.jenkins.getAuthorizationStrategy();
        assertTrue(b instanceof FolderBasedAuthorizationStrategy);
        assertNotSame("A new instance of FolderBasedAuthorizationStrategy should have been set.", a, b);
        FolderBasedAuthorizationStrategy newStrategy = (FolderBasedAuthorizationStrategy) b;
        GlobalRole role = newStrategy.getGlobalRoles().stream().filter(r -> r.getName().equals(adminRoleName))
                              .findAny().orElseThrow(() -> new RuntimeException("The admin role should exist"));
        assertTrue(role.getSids().contains(adminUserSid));
    }

    @Test
    public void assignSidToFolderRole() {
        String sid = "user1";
        FolderRole role = new FolderRole("foo", wrapPermissions(Item.READ), singleton("folderFoo"));
        assertEquals(0, role.getSids().size());
        FolderAuthorizationStrategyAPI.addFolderRole(role);
        FolderAuthorizationStrategyAPI.assignSidToFolderRole(sid, "foo");


        AuthorizationStrategy a = j.jenkins.getAuthorizationStrategy();
        assertTrue(a instanceof FolderBasedAuthorizationStrategy);
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) a;
        FolderRole updatedRole = strategy.getFolderRoles().stream().filter(r -> r.getName().equals("foo"))
                                     .findAny().orElseThrow(() -> new RuntimeException("The created role should exist"));
        assertTrue(updatedRole.getSids().contains(sid));
    }

    @Test
    public void assignSidToAgentRole() {
        String sid = "user1";
        AgentRole role = new AgentRole("bar", wrapPermissions(Item.READ), singleton("agentBar"));
        assertEquals(0, role.getSids().size());
        FolderAuthorizationStrategyAPI.addAgentRole(role);
        FolderAuthorizationStrategyAPI.assignSidToAgentRole(sid, "bar");

        AuthorizationStrategy a = j.jenkins.getAuthorizationStrategy();
        assertTrue(a instanceof FolderBasedAuthorizationStrategy);
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) a;
        AgentRole updatedRole = strategy.getAgentRoles().stream().filter(r -> r.getName().equals("bar"))
                                    .findAny().orElseThrow(() -> new RuntimeException("The created role should exist"));
        assertTrue(updatedRole.getSids().contains(sid));
    }

    @Test
    public void removeSidFromGlobalRole() {
        AuthorizationStrategy a = j.jenkins.getAuthorizationStrategy();
        assertTrue(a instanceof FolderBasedAuthorizationStrategy);
        final String adminRoleName = "admin";
        FolderAuthorizationStrategyAPI.assignSidToGlobalRole("user1", adminRoleName);
        FolderAuthorizationStrategyAPI.removeSidFromGlobalRole("user1", adminRoleName);

        // a new authorization strategy should have been set
        AuthorizationStrategy b = j.jenkins.getAuthorizationStrategy();
        assertTrue(b instanceof FolderBasedAuthorizationStrategy);
        assertNotSame("A new instance of FolderBasedAuthorizationStrategy should have been set.", a, b);
        FolderBasedAuthorizationStrategy newStrategy = (FolderBasedAuthorizationStrategy) b;
        GlobalRole role = newStrategy.getGlobalRoles().stream().filter(r -> r.getName().equals(adminRoleName))
                              .findAny().orElseThrow(() -> new RuntimeException("The admin role should exist"));
        assertFalse(role.getSids().contains("admin2"));
    }

    @Test
    public void removeSidFromFolderRole() {
        String sid = "user1";
        FolderRole role = new FolderRole("foo", wrapPermissions(Item.READ), singleton("folderFoo"));
        assertEquals(0, role.getSids().size());
        FolderAuthorizationStrategyAPI.addFolderRole(role);
        FolderAuthorizationStrategyAPI.assignSidToFolderRole(sid, "foo");
        FolderAuthorizationStrategyAPI.removeSidFromFolderRole(sid, "foo");

        AuthorizationStrategy a = j.jenkins.getAuthorizationStrategy();
        assertTrue(a instanceof FolderBasedAuthorizationStrategy);
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) a;
        FolderRole updatedRole = strategy.getFolderRoles().stream().filter(r -> r.getName().equals("foo"))
                                     .findAny().orElseThrow(() -> new RuntimeException("The created role should exist"));
        assertFalse(updatedRole.getSids().contains(sid));
    }

    @Test
    public void removeSidFromAgentRole() {
        String sid = "user1";
        AgentRole role = new AgentRole("bar", wrapPermissions(Item.READ), singleton("agentBar"));
        assertEquals(0, role.getSids().size());
        FolderAuthorizationStrategyAPI.addAgentRole(role);
        FolderAuthorizationStrategyAPI.assignSidToAgentRole(sid, "bar");
        FolderAuthorizationStrategyAPI.removeSidFromAgentRole(sid, "bar");

        AuthorizationStrategy a = j.jenkins.getAuthorizationStrategy();
        assertTrue(a instanceof FolderBasedAuthorizationStrategy);
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) a;
        AgentRole updatedRole = strategy.getAgentRoles().stream().filter(r -> r.getName().equals("bar"))
                                    .findAny().orElseThrow(() -> new RuntimeException("The created role should exist"));
        assertFalse(updatedRole.getSids().contains(sid));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowDuplicateNamesInGlobalRoles() {
        // the "admin" role should already exist
        FolderAuthorizationStrategyAPI.addGlobalRole(new GlobalRole("admin", wrapPermissions(Permission.READ),
            emptySet()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowDuplicateNamesInFolderRoles() {
        FolderAuthorizationStrategyAPI.addFolderRole(new FolderRole("baz", wrapPermissions(Item.READ),
            singleton("folder42")));
        // different permissions shouldn't matter
        FolderAuthorizationStrategyAPI.addFolderRole(new FolderRole("baz", wrapPermissions(Item.CONFIGURE),
            singleton("folder42")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowDuplicateNamesInAgentRoles() {
        FolderAuthorizationStrategyAPI.addAgentRole(new AgentRole("baz", wrapPermissions(Computer.DELETE),
            singleton("agent42")));
        // different agent names shouldn't matter
        FolderAuthorizationStrategyAPI.addAgentRole(new AgentRole("baz", wrapPermissions(Computer.CONFIGURE),
            singleton("agent43")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowBlankSidInGlobalRoles() {
        FolderAuthorizationStrategyAPI.assignSidToGlobalRole("", "admin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowBlankSidInFolderRoles() {
        FolderAuthorizationStrategyAPI.addFolderRole(new FolderRole("qwerty", wrapPermissions(Item.EXTENDED_READ),
            singleton("sampleFolder")));
        FolderAuthorizationStrategyAPI.assignSidToFolderRole(" \t", "qwerty");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowBlankSidInAgentRoles() {
        FolderAuthorizationStrategyAPI.addFolderRole(new FolderRole("foo", wrapPermissions(Item.EXTENDED_READ),
            singleton("sampleAgent")));
        FolderAuthorizationStrategyAPI.assignSidToFolderRole("\t\t \t", "foo");
    }
}
