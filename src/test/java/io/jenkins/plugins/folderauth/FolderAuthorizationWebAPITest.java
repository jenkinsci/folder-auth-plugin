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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Test for {@link FolderAuthorizationStrategyManagementLink}
 */
public class FolderAuthorizationWebAPITest {

    @Rule
    public final JenkinsRule jenkinsRule = new JenkinsRule();
    private JenkinsRule.WebClient webClient;
    String apiURL;
    private final Logger LOGGER = Logger.getLogger(FolderAuthorizationWebAPITest.class.getName());

    @Before
    public void setup() throws Exception {

        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());
        jenkinsRule.jenkins.setCrumbIssuer(null);
        FolderBasedAuthorizationStrategy strategy = new FolderBasedAuthorizationStrategy.DescriptorImpl()
                .newInstance(null, new JSONObject(true));
        jenkinsRule.jenkins.setAuthorizationStrategy(strategy);

        FolderAuthorizationStrategyManagementLink folderAuthorizationStrategyManagementLink = FolderAuthorizationStrategyManagementLink.all()
                .getInstance(FolderAuthorizationStrategyManagementLink.class);
        apiURL = jenkinsRule.jenkins.getRootUrl() + folderAuthorizationStrategyManagementLink.getUrlName();
        User.getById("adminUser", true);
        FolderAuthorizationStrategyAPI.assignSidToGlobalRole("adminUser", "admin");
        webClient = jenkinsRule.createWebClient();
        webClient.login("adminUser", "adminUser");
        // not ideal, but HTMLUnit web client doesn't accept template literals amongst others
        webClient.getOptions().setThrowExceptionOnScriptError(false);
    }

    @Test
    public void testAddGlobalRole() throws IOException {

        assertFalse(roleExists(RoleType.AGENT, "globalRole"));
        Map<String, Object> map = new HashMap<>();
        map.put("name", "globalRole");
        map.put("permissions", Arrays.asList(Item.CONFIGURE.getId(), Item.BUILD.getId()));
        JSONObject json = JSONObject.fromObject(map);
        String jsonString = json.toString();

        addRole(RoleType.GLOBAL, jsonString);
        assertTrue(roleExists(RoleType.GLOBAL, "globalRole"));
    }

    @Test
    public void testAddFolderRole() throws IOException {

        assertFalse(roleExists(RoleType.FOLDER, "folderRole"));
        Map<String, Object> map = new HashMap<>();
        map.put("name", "folderRole");
        map.put("permissions", Arrays.asList(Item.CONFIGURE.getId(), Item.BUILD.getId()));
        map.put("folderNames", Arrays.asList("folder1", "folder2"));
        JSONObject json = JSONObject.fromObject(map);
        String jsonString = json.toString();

        addRole(RoleType.FOLDER, jsonString);
        assertTrue(roleExists(RoleType.FOLDER, "folderRole"));
    }

    @Test
    public void testAddAgentRole() throws IOException {

        assertFalse(roleExists(RoleType.AGENT, "agentRole"));
        Map<String, Object> map = new HashMap<>();
        map.put("name", "agentRole");
        map.put("permissions", Arrays.asList(Computer.CONFIGURE.getId(), Computer.CREATE.getId()));
        map.put("agentNames", Arrays.asList("agent1", "agent2"));
        JSONObject json = JSONObject.fromObject(map);
        String jsonString = json.toString();

        addRole(RoleType.AGENT, jsonString);
        assertTrue(roleExists(RoleType.AGENT, "agentRole"));
    }

    @Test
    public void testAssignSidToGlobalRole() throws IOException {
        testAddGlobalRole();  // adds a global role named globalRole

        assertFalse(userAssignedToRole(RoleType.GLOBAL, "alice", "globalRole"));
        URL methodURL = new URL(apiURL + "/assignSidToGlobalRole");
        WebRequest request = new WebRequest(methodURL, HttpMethod.POST);

        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "globalRole"),
                new NameValuePair("sid", "alice")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        assertTrue(userAssignedToRole(RoleType.GLOBAL, "alice", "globalRole"));
    }

    @Test
    public void testAssignSidToFolderRole() throws IOException {
        testAddFolderRole();  // adds a folder role named folderRole

        assertFalse(userAssignedToRole(RoleType.FOLDER, "alice", "folderRole"));
        URL methodURL = new URL(apiURL + "/assignSidToFolderRole");
        WebRequest request = new WebRequest(methodURL, HttpMethod.POST);

        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "folderRole"),
                new NameValuePair("sid", "alice")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        assertTrue(userAssignedToRole(RoleType.FOLDER, "alice", "folderRole"));
    }

    @Test
    public void testAssignSidToAgentRole() throws IOException {
        testAddAgentRole();  // adds a agent role named agentRole

        assertFalse(userAssignedToRole(RoleType.AGENT, "alice", "agentRole"));
        URL methodURL = new URL(apiURL + "/assignSidToAgentRole");
        WebRequest request = new WebRequest(methodURL, HttpMethod.POST);

        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "agentRole"),
                new NameValuePair("sid", "alice")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        assertTrue(userAssignedToRole(RoleType.AGENT, "alice", "agentRole"));
    }

    @Test
    public void testDeleteGlobalRole() throws IOException {
        testAddGlobalRole();

        URL methodURL = new URL(apiURL + "/deleteGlobalRole");
        WebRequest request = new WebRequest(methodURL, HttpMethod.POST);

        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "globalRole")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        assertFalse(roleExists(RoleType.GLOBAL, "globalRole"));
    }

    @Test
    public void testDeleteFolderRole() throws IOException {
        testAddFolderRole();

        URL methodURL = new URL(apiURL + "/deleteFolderRole");
        WebRequest request = new WebRequest(methodURL, HttpMethod.POST);

        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "folderRole")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        assertFalse(roleExists(RoleType.FOLDER, "folderRole"));
    }

    @Test
    public void testDeleteAgentRole() throws IOException {
        testAddAgentRole();

        URL methodURL = new URL(apiURL + "/deleteAgentRole");
        WebRequest request = new WebRequest(methodURL, HttpMethod.POST);

        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "agentRole")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        assertFalse(roleExists(RoleType.AGENT, "agentRole"));
    }

    @Test
    public void testGetAgentRole() throws IOException {
        testAssignSidToAgentRole();

        String roleName = "agentRole";
        URL methodURL = new URL(apiURL + "/getAgentRole?name=" + roleName);
        WebRequest request = new WebRequest(methodURL, HttpMethod.GET);

        Page page = webClient.getPage(request);
        // Verifying that web request is successful and that the role is found
        assertEquals("Testing if request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        // Verifying that role contents are correct
        String roleString = page.getWebResponse().getContentAsString();
        LOGGER.info(roleString);
        JSONObject json = JSONObject.fromObject(roleString);
        assertEquals("Verifying that the name is correct", roleName, json.get("name"));
        assertTrue(json.containsKey("sids"));
        JSONArray sids = (JSONArray) json.get("sids");
        assertTrue("User alice should be assigned to role", sids.contains("alice"));
        assertEquals("Verifying that role is extracted correctly",
                new HashSet(Arrays.asList("agent1", "agent2")),
                new HashSet((List) json.get("agents")));

        // Testing getting a role that doesn't exists
        methodURL = new URL(apiURL + "/getAgentRole?name=nonexistent");
        request = new WebRequest(methodURL, HttpMethod.GET);
        page = webClient.getPage(request);
        assertEquals("Testing if request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        roleString = page.getWebResponse().getContentAsString();
        assertEquals("{}", roleString);  // expecting an empty response as the role doesn't exists.
    }

    @Test
    public void testGetFolderRole() throws IOException {
        testAssignSidToFolderRole();  // creates a folder role named "folderRole" and assigns "alice" to the role

        String roleName = "folderRole";
        URL methodURL = new URL(apiURL + "/getFolderRole?name=" + roleName);
        WebRequest request = new WebRequest(methodURL, HttpMethod.GET);

        Page page = webClient.getPage(request);
        // Verifying that web request is successful and that the role is found
        assertEquals("Testing if request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        String roleString = page.getWebResponse().getContentAsString();
        JSONObject json = JSONObject.fromObject(roleString);
        assertEquals(roleName, json.get("name"));
        JSONArray sids = (JSONArray) json.get("sids");
        assertTrue("User alice should be assigned to role", sids.contains("alice"));
        assertEquals("Verifying that role is extracted correctly",
                new HashSet(Arrays.asList("folder1", "folder2")),
                new HashSet((List) json.get("folders")));

        // Testing getting a role that doesn't exists
        methodURL = new URL(apiURL + "/getFolderRole?name=nonexistent");
        request = new WebRequest(methodURL, HttpMethod.GET);
        page = webClient.getPage(request);
        assertEquals("Testing if request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        roleString = page.getWebResponse().getContentAsString();
        assertEquals("{}", roleString);  // expecting an empty response as the role doesn't exists.
    }

    @Test
    public void testGetGlobalRole() throws IOException {
        testAssignSidToGlobalRole();  // creates a folder role named "folderRole" and assigns "alice" to the role

        String roleName = "globalRole";
        URL methodURL = new URL(apiURL + "/getGlobalRole?name=" + roleName);
        WebRequest request = new WebRequest(methodURL, HttpMethod.GET);

        Page page = webClient.getPage(request);
        // Verifying that web request is successful and that the role is found
        assertEquals("Testing if request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        String roleString = page.getWebResponse().getContentAsString();
        JSONObject json = JSONObject.fromObject(roleString);
        assertEquals(roleName, json.get("name"));
        JSONArray sids = (JSONArray) json.get("sids");
        assertTrue("User alice should be assigned to role", sids.contains("alice"));

        // Testing getting a role that doesn't exists
        methodURL = new URL(apiURL + "/getGlobalRole?name=nonexistent");
        request = new WebRequest(methodURL, HttpMethod.GET);
        page = webClient.getPage(request);
        assertEquals("Testing if request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        roleString = page.getWebResponse().getContentAsString();
        assertEquals("{}", roleString);  // expecting an empty response as the role doesn't exists.
    }

    @Test
    public void testGetAssignedRoles() throws IOException {
        testAssignSidToAgentRole();
        testAssignSidToFolderRole();
        testAssignSidToGlobalRole();
        // above creates 3 roles and assigns "alice" to all 3

        URL methodURL = new URL(apiURL + "/getAssignedRoles?sid=alice");
        WebRequest request = new WebRequest(methodURL, HttpMethod.GET);
        Page page = webClient.getPage(request);
        assertEquals("Testing if request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        // Verifying that the method returns all 3 roles
        String response = page.getWebResponse().getContentAsString();
        JSONObject json = JSONObject.fromObject(response);
        assertTrue(json.containsKey("agentRoles"));
        JSONArray agentRoles = (JSONArray) json.get("agentRoles");
        assertTrue(agentRoles.contains("agentRole"));
        assertTrue(json.containsKey("folderRoles"));
        JSONArray folderRoles = (JSONArray) json.get("folderRoles");
        assertTrue(folderRoles.contains("folderRole"));
        assertTrue(json.containsKey("globalRoles"));
        JSONArray globalRoles = (JSONArray) json.get("globalRoles");
        assertTrue(globalRoles.contains("agentRole"));

        // Testing doing a get for a user who is not assigned to any role
        methodURL = new URL(apiURL + "/getAssignedRoles?sid=unknown");
        request = new WebRequest(methodURL, HttpMethod.GET);
        page = webClient.getPage(request);
        assertEquals("Testing if request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        response = page.getWebResponse().getContentAsString();
        json = JSONObject.fromObject(json);
        assertTrue(json.containsKey("agentRoles"));
        agentRoles = (JSONArray) json.get("agentRoles");
        assertEquals(0, agentRoles.size());
        assertTrue(agentRoles.contains("agentRole"));
        assertTrue(json.containsKey("folderRoles"));
        folderRoles = (JSONArray) json.get("folderRoles");
        assertEquals(0, folderRoles.size());
        assertTrue(json.containsKey("globalRoles"));
        globalRoles = (JSONArray) json.get("globalRoles");
        assertEquals(0, globalRoles.size());
    }

    private enum RoleType {
        GLOBAL, FOLDER, AGENT
    }

    /**
     * Util method to add a role and verify that the request succeeded
     *
     * @param roleType   Type of role (either GLOBAL, FOLDER or AGENT
     * @param jsonString String containing the json representation of a role
     * @throws IOException
     */
    private void addRole(RoleType roleType, String jsonString) throws IOException {

        URL methodURL = new URL(apiURL + "/add" + StringUtils.capitalize(roleType.name().toLowerCase()) + "Role");
        WebRequest request = new WebRequest(methodURL, HttpMethod.POST);
        request.setRequestBody(jsonString);
        request.setAdditionalHeader("Content-Type", "application/json");
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
    }

    /**
     * Util method to verify that the role exists
     *
     * @param roleType Type of role
     * @param roleName Map object that represents the role
     * @return true if role exists
     */
    private boolean roleExists(RoleType roleType, String roleName) {

        AuthorizationStrategy a = jenkinsRule.jenkins.getAuthorizationStrategy();
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) a;
        boolean found = false;
        Set<? extends AbstractRole> roles;
        switch (roleType) {
            case AGENT:
                roles = strategy.getAgentRoles();
                break;
            case FOLDER:
                roles = strategy.getFolderRoles();
                break;
            case GLOBAL:
                roles = strategy.getGlobalRoles();
                break;
            default:
                throw new IllegalArgumentException("Expected role type of AGENT, FOLDER or GLOBAL but received: " + roleType);
        }
        for (AbstractRole role : roles) {
            if (role.getName().equals(roleName)) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Util method to verify that a user has been assigned to a particular role
     *
     * @param roleType Type of role
     * @param sid      User ID
     * @param roleName Name of the role
     * @return true if user is assigned to role
     */
    private boolean userAssignedToRole(RoleType roleType, String sid, String roleName) {

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

        for (AbstractRole role : roles) {
            if (role.getName().equals(roleName)) {
                assigned = role.getSids().contains(sid);
                break;
            }
        }
        return assigned;
    }
}
