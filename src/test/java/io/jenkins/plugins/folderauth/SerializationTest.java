package io.jenkins.plugins.folderauth;

import com.google.common.collect.ImmutableSet;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.XStream2;
import io.jenkins.plugins.folderauth.roles.FolderRole;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.HashSet;
import java.util.Set;

import static io.jenkins.plugins.folderauth.misc.PermissionWrapper.wrapPermissions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SerializationTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        Set<GlobalRole> globalRoles = new HashSet<>();
        globalRoles.add(new GlobalRole("admin", wrapPermissions(Jenkins.ADMINISTER),
                ImmutableSet.of("admin")));

        Set<FolderRole> folderRoles = new HashSet<>();
        folderRoles.add(new FolderRole("read", wrapPermissions(Jenkins.READ), ImmutableSet.of("user1")));

        j.jenkins.setAuthorizationStrategy(new FolderBasedAuthorizationStrategy(globalRoles, folderRoles));
    }

    @Test
    @Issue("JENKINS-58485")
    public void shouldNotUseConcurrentHashMap$KeySetView() {
        XStream2 xStream = new XStream2();
        String xml = xStream.toXML(j.jenkins.getAuthorizationStrategy());
        assertFalse(xml.contains("ConcurrentHashMap$KeySetView"));
    }

    @Test
    @Issue("JENKINS-58485")
    public void shouldDeserializeConfigWithoutConcurrentHashMap$KeySetView() {
        XStream2 xStream = new XStream2();

        String xml = "<io.jenkins.plugins.folderauth.FolderBasedAuthorizationStrategy>" +
                             "  <globalRoles>" +
                             "    <io.jenkins.plugins.folderauth.roles.GlobalRole>" +
                             "      <name>admin</name>" +
                             "      <permissionWrappers>" +
                             "        <io.jenkins.plugins.folderauth.misc.PermissionWrapper>" +
                             "          <id>hudson.model.Hudson.Administer</id>" +
                             "        </io.jenkins.plugins.folderauth.misc.PermissionWrapper>" +
                             "      </permissionWrappers>" +
                             "      <sids>" +
                             "        <string>admin</string>" +
                             "      </sids>" +
                             "    </io.jenkins.plugins.folderauth.roles.GlobalRole>" +
                             "  </globalRoles>" +
                             "  <folderRoles>" +
                             "    <io.jenkins.plugins.folderauth.roles.FolderRole>" +
                             "      <name>read</name>" +
                             "      <permissionWrappers>" +
                             "        <io.jenkins.plugins.folderauth.misc.PermissionWrapper>" +
                             "          <id>hudson.model.Hudson.Read</id>\n" +
                             "        </io.jenkins.plugins.folderauth.misc.PermissionWrapper>" +
                             "      </permissionWrappers>" +
                             "      <sids/>" +
                             "      <folders>" +
                             "        <string>user1</string>" +
                             "      </folders>" +
                             "    </io.jenkins.plugins.folderauth.roles.FolderRole>" +
                             "  </folderRoles>" +
                             "</io.jenkins.plugins.folderauth.FolderBasedAuthorizationStrategy>";

        Object obj = xStream.fromXML(xml);
        assertTrue(obj instanceof FolderBasedAuthorizationStrategy);
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) obj;
        assertEquals(1, strategy.getGlobalRoles().size());
        assertEquals(1, strategy.getFolderRoles().size());

        // check if the ACLs have been properly initialized
        try (ACLContext ignored = ACL.as(User.getById("admin", true).impersonate())) {
            assertTrue(j.jenkins.hasPermission(Jenkins.ADMINISTER));
        }

        try (ACLContext ignored = ACL.as(User.getById("user1", true).impersonate())) {
            assertFalse(j.jenkins.hasPermission(Jenkins.ADMINISTER));
        }
    }

}
