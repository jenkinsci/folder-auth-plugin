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

### Deleting Role

Just click the big red ❌ on the top right of the role you want to delete and
you're done.

## Reporting issues

Please create a ticket for any bug reports or feature requests on
[Jenkins JIRA](https://issues.jenkins-ci.org/) and add `folder-auth-plugin`
as the 'Component'.
