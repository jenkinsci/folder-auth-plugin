package io.jenkins.plugins.folderauth.roles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class RoleNullabilityTest {

    @Test
    public void folderRoleShouldHandleNullCollections() {
        FolderRole role = new FolderRole("test-folder-role", null, null, null);

        assertNotNull(role.getPermissionsUnsorted());
        assertEquals(0, role.getPermissionsUnsorted().size());

        assertNotNull(role.getFolderNames());
        assertEquals(0, role.getFolderNames().size());

        assertNotNull(role.getSids());
        assertEquals(0, role.getSids().size());
    }

    @Test
    public void globalRoleShouldHandleNullCollections() {
        GlobalRole role = new GlobalRole("test-global-role", null, null);

        assertNotNull(role.getPermissionsUnsorted());
        assertEquals(0, role.getPermissionsUnsorted().size());

        assertNotNull(role.getSids());
        assertEquals(0, role.getSids().size());
    }

    @Test
    public void agentRoleShouldHandleNullCollections() {
        AgentRole role = new AgentRole("test-agent-role", null, null, null);

        assertNotNull(role.getPermissionsUnsorted());
        assertEquals(0, role.getPermissionsUnsorted().size());

        assertNotNull(role.getAgents());
        assertEquals(0, role.getAgents().size());

        assertNotNull(role.getSids());
        assertEquals(0, role.getSids().size());
    }
}
