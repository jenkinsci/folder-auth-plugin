# Using the plugin

This plugin allows administrator users to allow or restrict users permissions.

This plugin follows a 'role'-based model in where, depending on the context,
a 'role' is applicable on multiple objects, can be assigned to multiple
users and can grant a number of permissions. All users assigned to the role
get all the permissions granted by that role for every object the role is
valid on.

This plugin provides multiple types of roles for managing permissions which
can all be used simultaneously to provide a flexible way to manage permissions:

* **Global Roles**: these roles give users permissions which are applicable
 everywhere in Jenkins.
* **Folder Roles**: these roles allow managing permissions for individual
 projects organized in 'folders' from Cloudbees'
 [Folders plugin](https://plugins.jenkins.io/cloudbees-folder).
* **Agent Roles**: these roles allow managing permissions for agents connected
 to Jenkins.

## Setting up

To use this plugin, you need to set it as the authorization strategy from the
'Configure Global Security' page. To do that:

1. Log in as a user with administrator permissions.
2. Go to the 'Manage Jenkins' page which should visible on the sidebar from
Jenkins' home page.
3. Go to the 'Configure Global Security' page.
4. Ensure that security for your Jenkins instance is enabled.
5. Choose 'Folder Authorization Strategy' as the authorization and save the
configuration.

Folder based authorization is now active 🎉. Now go back to the 'Manage
Jenkins' page and scroll down. You should see a linkfor the 'Folder
Authorization Strategy'. Click on the link and you can now
start configuring permissions for users.

### Adding Roles

The process to add any type of role is similar. Here we show how to add a
'Folder Role'.

![Adding a folder role](/docs/images/add-folder-role.png)

1. Choose a name for the role. The name of the role uniquely identifies it and
is useful for invoking the REST API methods.
2. Choose the permissions that would be granted through this role. You can
select multiple permissions by holding down the control key on your keyboard.
3. Choose the folders on which this role will be applicable.
4. Click the 'Add role' button and you're done!

### Assigning Roles to users

It is really easy to assign roles to users and groups. Just follow the steps
below.

![Assigning a role](/docs/images/assign-role.png)

1. Find the role, type the user's ID in the text field.
2. Click the submit button.
3. The role was assigned to the user!

**Note**: Apart from the user IDs provided by your security realm, Jenkins
provides two groups, `authenticated` and `anonymous`, which as their names
suggest, apply to authenticated and non-authenticated users, respectively.
Therefore, if you assign a role to the `authenticated` sid, permissions would
be granted to all logged-in users.

### Deleting a Role

Just click the big red ❌ on the top right of the role you want to delete and
you're done.

## Inheritance of Folder Roles

When you add a folder role that is applicable on a *parent* folder, the
permissions granted by that role are applicable to all of its children.
Therefore, all permissions granted to a folder for a user are applicable to
all folders and jobs nested under it.

## Using Agent Roles

Agent roles can be used to configure users' permissions for agents connected
to Jenkins. Multiple agents can be selected when creating an agent role, just
like with Folder roles. All users assigned to this role will get all
permissions granted by an agent role for all agents on which this role is
applicable.

Unlike Folder Roles, there is no support for inheritance of Agent Roles
because Jenkins does not support nesting of agents.

## Jenkins Configuration-as-Code Support

This plugin supports configuration as code for hands-free setup of your
Jenkins instance. The easiest way to get a configuration for your uses-case is
to configure the plugin through the Web UI and then export the configuration
as YAML and store it for later use.

You can write the configuration manually too. As an example, a YAML
configuration for this plugin typically looks like this:

```yaml
jenkins:
  authorizationStrategy:
    folderBased:
      agentRoles:
        - agents:
            - "agent1"
          name: "agentRole1"
          permissions:
            - id: "hudson.model.Computer.Configure"
            - id: "hudson.model.Computer.Disconnect"
          sids:
            - "user1"
      folderRoles:
        - folders:
            - "root"
          name: "viewRoot"
          permissions:
            - id: "hudson.model.Item.Read"
          sids:
            - "user1"
      globalRoles:
        - name: "admin"
          permissions:
            - id: "hudson.model.Hudson.Administer"
          sids:
            - "admin"
        - name: "read"
          permissions:
            - id: "hudson.model.Hudson.Read"
          sids:
            - "user1"
```

**Note**: You need to have [Configuration-as-Code](https://plugins.jenkins.io/configuration-as-code)
plugin installed for using Jenkins Configuration-as-Code. The CasC plugin will
**not** be installed when you install the Folder Auth plugin.
