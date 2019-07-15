package io.jenkins.plugins.folderauth.misc;

import jenkins.model.Jenkins;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class PermissionWrapperTest {
    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowDangerousPermissions() {
        new PermissionWrapper(Jenkins.RUN_SCRIPTS.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNullPermissions() {
        new PermissionWrapper("this is not a permission id");
    }
}
