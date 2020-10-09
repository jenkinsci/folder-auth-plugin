package io.jenkins.plugins.folderauth;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import hudson.model.Computer;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.AuthorizationStrategy;
import io.jenkins.plugins.folderauth.roles.AbstractRole;
import io.jenkins.plugins.folderauth.roles.AgentRole;
import io.jenkins.plugins.folderauth.roles.FolderRole;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test for {@link FolderAuthorizationStrategyManagementLink}
 */
public class FolderAuthorizationWebAPITest {

    @Rule
    public final JenkinsRule jenkinsRule = new JenkinsRule();
    private JenkinsRule.WebClient webClient;

    @Before
    public void setup() throws Exception {

        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());
        jenkinsRule.jenkins.setCrumbIssuer(null);
        // Setting authorization strategy
        FolderBasedAuthorizationStrategy strategy = new FolderBasedAuthorizationStrategy.DescriptorImpl()
            .newInstance(null, new JSONObject(true));
        jenkinsRule.jenkins.setAuthorizationStrategy(strategy);

        // Adding an admin user
        User.getById("adminUser", true);
        // Assigning web client to admin
        FolderAuthorizationStrategyAPI.assignSidToGlobalRole("adminUser", "admin");
        webClient = jenkinsRule.createWebClient();
        webClient.login("adminUser", "adminUser");
        // not ideal, but HTMLUnit web client doesn't accept template literals amongst others
        webClient.getOptions().setThrowExceptionOnScriptError(false);
    }

    @Test
    public void testAddGlobalRole()
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "globalRole");
        map.put("permissions", Arrays.asList(Item.CONFIGURE.getId(), Item.BUILD.getId()));
        JSONObject json = JSONObject.fromObject(map);
        String jsonString = json.toString();

        addRole(RoleType.GLOBAL, jsonString);
        verifyThatRoleExists(RoleType.GLOBAL, map);
    }

    private enum RoleType {
        GLOBAL, FOLDER, AGENT
    }

    @Test
    public void testAddFolderRole()
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "folderRole");
        map.put("permissions", Arrays.asList(Item.CONFIGURE.getId(), Item.BUILD.getId()));
        map.put("folderNames", Arrays.asList("folder1", "folder2"));
        JSONObject json = JSONObject.fromObject(map);
        String jsonString = json.toString();

        addRole(RoleType.FOLDER, jsonString);
        verifyThatRoleExists(RoleType.FOLDER, map);
    }

    @Test
    public void testAddAgentRole() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "agentRole");
        map.put("permissions", Arrays.asList(Computer.CONFIGURE.getId(), Computer.CREATE.getId()));
        map.put("agentNames", Arrays.asList("agent1", "agent2"));
        JSONObject json = JSONObject.fromObject(map);
        String jsonString = json.toString();

        addRole(RoleType.AGENT, jsonString);
        verifyThatRoleExists(RoleType.AGENT, map);
    }

    @Test
    public void testAssignSidToGlobalRole()
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        testAddGlobalRole();  // adds a global role named globalRole

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/assignSidToGlobalRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);

        request.setRequestParameters(Arrays.asList(
            new NameValuePair("roleName", "globalRole"),
            new NameValuePair("sid", "alice")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        verifyUserAssignedToRole(RoleType.GLOBAL, "alice", "globalRole");
    }

    @Test
    public void testAssignSidToFolderRole()
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        testAddFolderRole();  // adds a folder role named folderRole

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/assignSidToFolderRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);

        request.setRequestParameters(Arrays.asList(
            new NameValuePair("roleName", "folderRole"),
            new NameValuePair("sid", "alice")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        verifyUserAssignedToRole(RoleType.FOLDER, "alice", "folderRole");
    }

    @Test
    public void testAssignSidToAgentRole()
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        testAddAgentRole();  // adds a agent role named agentRole

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/assignSidToAgentRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);

        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "agentRole"),
                new NameValuePair("sid", "alice")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        verifyUserAssignedToRole(RoleType.AGENT, "alice", "agentRole");
    }

    @Test
    public void testDeleteGlobalRole()
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        testAddGlobalRole();

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/deleteGlobalRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);

        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "globalRole")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) jenkinsRule.jenkins.getAuthorizationStrategy();
        for (GlobalRole globalRole : strategy.getGlobalRoles()) {
            assertNotEquals("globalRole", globalRole.getName());
        }
    }

    @Test
    public void testDeleteFolderRole()
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        testAddFolderRole();

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/deleteFolderRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);

        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "folderRole")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) jenkinsRule.jenkins.getAuthorizationStrategy();
        for (FolderRole folderRole : strategy.getFolderRoles()) {
            assertNotEquals("globalRole", folderRole.getName());
        }
    }

    @Test
    public void testDeleteAgentRole() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        testAddAgentRole();

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/deleteAgentRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);

        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "agentRole")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) jenkinsRule.jenkins.getAuthorizationStrategy();
        for (AgentRole agentRole : strategy.getAgentRoles()) {
            assertNotEquals("globalRole", agentRole.getName());
        }
    }

    /**
     * Util method to add a role and verify that the request succeeded
     * @param roleType Type of role (either GLOBAL, FOLDER or AGENT
     * @param jsonString String containing the json representation of a role
     * @throws IOException
     */
    private void addRole(RoleType roleType, String jsonString) throws IOException {
        String apiPath;
        switch (roleType) {
            case GLOBAL:
                apiPath = "folder-auth/addGlobalRole";
                break;
            case FOLDER:
                apiPath = "folder-auth/addFolderRole";
                break;
            case AGENT:
                apiPath = "folder-auth/addAgentRole";
                break;
            default:
                throw new IllegalArgumentException("Unexpected role type");
        }
        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + apiPath);
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);
        request.setRequestBody(jsonString);
        request.setAdditionalHeader("Content-Type", "application/json");
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
    }

    /**
     * Util method to verify that the role exists
     * @param roleType Type of role
     * @param roleMap Map object that represents the role
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void verifyThatRoleExists(RoleType roleType, Map<String, Object> roleMap)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        AuthorizationStrategy a = jenkinsRule.jenkins.getAuthorizationStrategy();
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) a;
        boolean found = false;
        // Using reflection to get method
        String getRoleMethodString = "get" + StringUtils.capitalize(roleType.name().toLowerCase()  + "Roles");
        Method getRoleMethod = strategy.getClass().getMethod(getRoleMethodString);
        Set<AbstractRole> roleSet = (Set) getRoleMethod.invoke(strategy);
        for (AbstractRole role : roleSet) {
            if (role.getName().equals(roleMap.get("name"))) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    /**
     * Util method to verify that a user has been assigned to a particular role
     * @param roleType Type of role
     * @param sid User ID
     * @param roleName Name of the role
     */
    private void verifyUserAssignedToRole(RoleType roleType, String sid, String roleName) {

        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) jenkinsRule.jenkins.getAuthorizationStrategy();
        boolean assigned = false;
        Set<? extends AbstractRole> roles;
        switch (roleType) {
            case GLOBAL:
                roles = strategy.getGlobalRoles();
                break;
            case FOLDER:
                roles = strategy.getFolderRoles();
                break;
            case AGENT:
                roles = strategy.getAgentRoles();
                break;
            default:
                throw new IllegalArgumentException("Unexpected role. Expecting GLOBAL, FOLDER or AGENT");
        }

        for (AbstractRole folderRole : roles) {
            if (folderRole.getName().equals(roleName)) {
                assigned = folderRole.getSids().contains(sid);
                break;
            }
        }
        assertTrue(assigned);
    }
}
