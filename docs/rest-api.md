# REST API methods

The plugin provides REST APIs for modifying the roles. All of these methods
require the user invoking them to have the `Jenkins.ADMINISTER` permission.

## API methods for adding a role

### `addGlobalRole`

Adds a global role with no sids assigned to it. Requires POST to `${JENKINS_URL}/folder-auth/addGlobalRole`.
The request body should specify the role to be added as a JSON object. For
example, to add a role with name `foo` and providing the `Item | Delete` and the `Item | Configure` permissions, the request body should look like this:

```json
{
    "name": "foo",
    "permissions": [
        "hudson.model.Item.Delete",
        "hudson.model.Item.Configure"
    ]
}
```

### `addFolderRole`

Adds a folder role with no sids assigned to it. Requires POST to `${JENKINS_URL}/folder-auth/addFolderRole`.
The request body should specify the role to be added as a JSON object. For
example, to add a role with name `foo` and providing the`Item | Delete` and
the `Item | Configure` permissions on folders `bar`and `foo/baz`, the
request body should look like this:

```json
{
    "name": "foo",
    "permissions": [
        "hudson.model.Item.Delete",
        "hudson.model.Item.Configure"
    ],
    "folderNames": [
        "foo/baz",
        "bar"
    ]
}
```

### `addAgentRole`

Adds an agent role with no sids assigned to it. Requires POST to `${JENKINS_URL}/folder-auth/addAgentRole`.
The request body should specify the role to be added as a JSON object. For
example, to add a role with the equal to `foo` and providing the
`Agent | Configure` permissions on agents `bar` and `baz`, the request body
should look like this:

```json
{
    "name": "foo",
    "permissions": [
        "hudson.model.Computer.Configure",
    ],
    "agentNames": [
        "bar",
        "baz"
    ]
}
```

## API methods for assigning a sid to a role

### `assignSidToGlobalRole`

Assigns a sid to the role identified by its name. Requires POST to
`${JENKINS_URL}/folder-auth/assignSidToGlobalRole`. The following query
parameters are required:

* `roleName`: The sid will be assigned to the global role with the name equal
to this parameter.
* `sid`: The sid to be assigned to the role with the name equal to the value of
`roleName`.

Using `curl`, for example, a sid "foo" can be assigned to the role "bar"

```bash
curl -X POST -d 'roleName=bar&sid=foo' \
    http://localhost:8080/folder-auth/assignSidToGlobalRole
```