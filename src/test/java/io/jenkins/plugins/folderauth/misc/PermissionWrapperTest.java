package io.jenkins.plugins.folderauth.misc;

import jenkins.model.Jenkins;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class PermissionWrapperTest {
    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowDangerousPermissions() {
        new PermissionWrapper(Jenkins.RUN_SCRIPTS.getId());
    }

    /**
     * Unknown permission IDs (e.g. from an uninstalled plugin) no longer throw.
     * Instead the wrapper is created with a null internal permission and {@link PermissionWrapper#isValid()}
     * returns {@code false}.  Jenkins will not grant any access for such wrappers.
     */
    @Test
    public void unknownPermissionsAreAcceptedWithWarning() {
        PermissionWrapper wrapper = new PermissionWrapper("this is not a permission id");
        assertNull("permission should be null for an unknown id", wrapper.getPermission());
        assertFalse("wrapper with unknown permission must not be valid", wrapper.isValid());
    }
}
