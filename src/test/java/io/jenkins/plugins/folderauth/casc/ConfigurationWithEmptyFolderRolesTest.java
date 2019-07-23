package io.jenkins.plugins.folderauth.casc;

import hudson.security.AuthorizationStrategy;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.folderauth.FolderBasedAuthorizationStrategy;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigurationWithEmptyFolderRolesTest {
    @Rule
    public JenkinsConfiguredWithCodeRule j = new JenkinsConfiguredWithCodeRule();

    @Test
    @ConfiguredWithCode("config2.yml")
    public void shouldNotThrowErrorWithEmptyFolderRoles() {
        AuthorizationStrategy authorizationStrategy = j.jenkins.getAuthorizationStrategy();
        assertTrue(authorizationStrategy instanceof FolderBasedAuthorizationStrategy);
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) authorizationStrategy;
        assertEquals(0, strategy.getFolderRoles().size());
        assertEquals(2, strategy.getGlobalRoles().size());
    }
}
