package io.jenkins.plugins.folderauth;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import hudson.model.Computer;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.AuthorizationStrategy;
import io.jenkins.plugins.folderauth.roles.AgentRole;
import io.jenkins.plugins.folderauth.roles.FolderRole;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    }

    @Test
    public void nullTest() {
        assertTrue(true);
    }

    @Test
    public void testAddGlobalRole() throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "globalRole");
        jsonMap.put("permissions", Arrays.asList(Item.CONFIGURE.getId(), Item.BUILD.getId()));
        JSONObject json = JSONObject.fromObject(jsonMap);
        String jsonString = json.toString();

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/addGlobalRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);
        request.setRequestBody(jsonString);
        request.setAdditionalHeader("Content-Type", "application/json");
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        // Verifying that the global role exists
        AuthorizationStrategy a = jenkinsRule.jenkins.getAuthorizationStrategy();
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) a;
        boolean found = false;
        for (GlobalRole globalRole : strategy.getGlobalRoles()) {
            if (globalRole.getName().equals("globalRole")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testAddFolderRole() throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "folderRole");
        jsonMap.put("permissions", Arrays.asList(Item.CONFIGURE.getId(), Item.BUILD.getId()));
        jsonMap.put("folderNames", Arrays.asList("folder1", "folder2"));
        JSONObject json = JSONObject.fromObject(jsonMap);
        String jsonString = json.toString();

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/addFolderRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);
        request.setRequestBody(jsonString);
        request.setAdditionalHeader("Content-Type", "application/json");
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        // Verifying that the global role exists
        AuthorizationStrategy a = jenkinsRule.jenkins.getAuthorizationStrategy();
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) a;
        boolean found = false;
        for (FolderRole folderRole : strategy.getFolderRoles()) {
            if (folderRole.getName().equals("folderRole")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testAddAgentRole() throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "agentRole");
        map.put("permissions", Arrays.asList(Computer.CONFIGURE.getId(), Computer.CREATE.getId()));
        map.put("agentNames", Arrays.asList("agent1", "agent2"));
        JSONObject json = JSONObject.fromObject(map);
        String jsonString = json.toString();

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/addAgentRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);
        request.setRequestBody(jsonString);
        request.setAdditionalHeader("Content-Type", "application/json");
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        // Verifying that the global role exists
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) jenkinsRule.jenkins.getAuthorizationStrategy();
        boolean found = false;
        for (AgentRole agentRole : strategy.getAgentRoles()) {
            if (agentRole.getName().equals("agentRole")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testAssignSidToGlobalRole() throws IOException {
        testAddGlobalRole();  // adds a global role named global

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/assignSidToGlobalRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);
//        request.setRequestBody("roleName=globalRole&sid=alice");

        // Setting parameters
        request.setRequestParameters(Arrays.asList(
            new NameValuePair("roleName", "globalRole"),
            new NameValuePair("sid", "alice")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        // Verifying that alice has been assigned
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) jenkinsRule.jenkins
                .getAuthorizationStrategy();
        boolean assigned = false;
        for (GlobalRole globalRole : strategy.getGlobalRoles()) {
            if (globalRole.getName().equals("globalRole")) {
                assigned = globalRole.getSids().contains("alice");
                break;
            }
        }
        assertTrue(assigned);
    }

    @Test
    public void testAssignSidToFolderRole() throws IOException {
        testAddFolderRole();  // adds a global role named global

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/assignSidToFolderRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);

        // Setting parameters
        request.setRequestParameters(Arrays.asList(
            new NameValuePair("roleName", "folderRole"),
            new NameValuePair("sid", "alice")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        // Verifying that alice has been assigned
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) jenkinsRule.jenkins.getAuthorizationStrategy();
        boolean assigned = false;
        for (FolderRole folderRole : strategy.getFolderRoles()) {
            if (folderRole.getName().equals("folderRole")) {
                assigned = folderRole.getSids().contains("alice");
                break;
            }
        }
        assertTrue(assigned);
    }

    @Test
    public void testAssignSidToAgentRole() throws IOException {
        testAddAgentRole();  // adds a global role named global

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/assignSidToAgentRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);

        // Setting parameters
        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "agentRole"),
                new NameValuePair("sid", "alice")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        // Verifying that alice has been assigned
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) jenkinsRule.jenkins.getAuthorizationStrategy();
        boolean assigned = false;
        for (AgentRole agentRole : strategy.getAgentRoles()) {
            if (agentRole.getName().equals("agentRole")) {
                assigned = agentRole.getSids().contains("alice");
                break;
            }
        }
        assertTrue(assigned);
    }

    @Test
    public void testDeleteGlobalRole() throws IOException {
        testAddGlobalRole();

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/deleteGlobalRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);

        // Setting parameters
        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "globalRole")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        // Verifying that role is actually deleted
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) jenkinsRule.jenkins.getAuthorizationStrategy();
        for (GlobalRole globalRole : strategy.getGlobalRoles()) {
            assertNotEquals("globalRole", globalRole.getName());
        }
    }

    @Test
    public void testDeleteFolderRole() throws IOException {
        testAddFolderRole();

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/deleteFolderRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);

        // Setting parameters
        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "folderRole")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        // Verifying that role is actually deleted
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) jenkinsRule.jenkins.getAuthorizationStrategy();
        for (FolderRole folderRole : strategy.getFolderRoles()) {
            assertNotEquals("globalRole", folderRole.getName());
        }
    }

    @Test
    public void testDeleteAgentRole() throws IOException {
        testAddAgentRole();

        URL apiURL = new URL(jenkinsRule.jenkins.getRootUrl() + "folder-auth/deleteAgentRole");
        WebRequest request = new WebRequest(apiURL, HttpMethod.POST);

        // Setting parameters
        request.setRequestParameters(Arrays.asList(
                new NameValuePair("roleName", "agentRole")
        ));
        Page page = webClient.getPage(request);
        assertEquals("Verifying that request is successful", HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        // Verifying that role is actually deleted
        FolderBasedAuthorizationStrategy strategy = (FolderBasedAuthorizationStrategy) jenkinsRule.jenkins.getAuthorizationStrategy();
        for (AgentRole agentRole : strategy.getAgentRoles()) {
            assertNotEquals("globalRole", agentRole.getName());
        }
    }
}
