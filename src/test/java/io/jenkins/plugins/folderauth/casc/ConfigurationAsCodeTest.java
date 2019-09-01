package io.jenkins.plugins.folderauth.casc;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.Computer;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.slaves.DumbSlave;
import hudson.slaves.JNLPLauncher;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.casc.model.CNode;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Objects;

import static io.jenkins.plugins.casc.misc.Util.getJenkinsRoot;
import static io.jenkins.plugins.casc.misc.Util.toStringFromYamlFile;
import static io.jenkins.plugins.casc.misc.Util.toYamlString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigurationAsCodeTest {
    private Folder folder;

    @Rule
    public JenkinsConfiguredWithCodeRule j = new JenkinsConfiguredWithCodeRule();

    @Before
    public void setUp() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.addNode(new DumbSlave("agent1", "", new JNLPLauncher(true)));
        folder = j.jenkins.createProject(Folder.class, "root");
    }

    @Test
    @ConfiguredWithCode("config.yml")
    public void configurationImportTest() {
        try (ACLContext ignored = ACL.as(User.getOrCreateByIdOrFullName("admin"))) {
            assertTrue(j.jenkins.hasPermission(Jenkins.ADMINISTER));
        }

        try (ACLContext ignored = ACL.as(User.getOrCreateByIdOrFullName("user1"))) {
            assertTrue(folder.hasPermission(Item.READ));
            assertFalse(j.jenkins.hasPermission(Jenkins.ADMINISTER));

            assertTrue(Objects.requireNonNull(j.jenkins.getComputer("agent1")).hasPermission(Computer.CONFIGURE));
            assertFalse(Objects.requireNonNull(j.jenkins.getComputer("agent1")).hasPermission(Computer.DELETE));
        }
    }

    @Test
    @ConfiguredWithCode("config3.yml")
    public void configurationImportWithHumanReadableTest() {
        try (ACLContext ignored = ACL.as(User.getOrCreateByIdOrFullName("admin"))) {
            assertTrue(j.jenkins.hasPermission(Jenkins.ADMINISTER));
        }

        try (ACLContext ignored = ACL.as(User.getOrCreateByIdOrFullName("user1"))) {
            assertTrue(folder.hasPermission(Item.READ));
            assertFalse(j.jenkins.hasPermission(Jenkins.ADMINISTER));

            assertTrue(Objects.requireNonNull(j.jenkins.getComputer("agent1")).hasPermission(Computer.CONFIGURE));
            assertFalse(Objects.requireNonNull(j.jenkins.getComputer("agent1")).hasPermission(Computer.DELETE));
        }
    }

    @Test
    @ConfiguredWithCode("config.yml")
    public void configurationExportTest() throws Exception {
        ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        ConfigurationContext context = new ConfigurationContext(registry);
        CNode yourAttribute = getJenkinsRoot(context).get("authorizationStrategy").asMapping()
            .get("folderBased");

        String exported = toYamlString(yourAttribute);
        String expected = toStringFromYamlFile(this, "expected.yml");

        assertThat(exported, is(expected));
    }

    @Test
    @ConfiguredWithCode("config3.yml")
    public void configurationExportWithHumanReadableTest() throws Exception {
        ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        ConfigurationContext context = new ConfigurationContext(registry);
        CNode yourAttribute = getJenkinsRoot(context).get("authorizationStrategy").asMapping()
            .get("folderBased");

        String exported = toYamlString(yourAttribute);
        String expected = toStringFromYamlFile(this, "expected3.yml");

        assertThat(exported, is(expected));
    }
}
