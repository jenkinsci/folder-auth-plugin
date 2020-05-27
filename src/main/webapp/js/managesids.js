'use strict';

/**
 * Assign a sid to a role.
 *
 * @param roleType {('agent' | 'global' | 'folder')} the type of the role
 * @param roleName the name of the role
 * @param sidInputBoxId id of the <input type="text"> that contains the sid
 */
function assignSid(roleType, roleName, sidInputBoxId) {
    const formData = new FormData();
    formData.append('sid', document.getElementById(sidInputBoxId).value);
    formData.append('roleName', roleName);

    if (!(roleType === 'agent' || roleType === 'global' || roleType === 'folder')) {
        throw  new Error('Unknown Role Type');
    }

    const url = `${rootURL}/folder-auth/assignSidTo${roleType[0].toUpperCase()}${roleType.substring(1)}Role`;
    const request = new XMLHttpRequest();
    request.open('POST', url);
    request.onload = () => {
        if (request.status === 200) {
            showNotificationOK('Sid added successfully.');
        } else {
            showNotificationERROR('Unable to remove the sid.' + request.responseText);
        }

    };

    request.onerror = () => {
        showNotificationERROR('Unable to add the sid to the role: ' + request.responseText);
    };

    // see addRole.js
    request.setRequestHeader('Jenkins-Crumb', crumb.value);
    request.send(formData);
}

/**
 * Removes a sid from a role.
 *
 * @param roleType {('agent' | 'global' | 'folder')} the type of the role
 * @param roleName the name of the role
 * @param sidInputBoxId id of the <input type="text"> that contains the sid
 */
function removeSid(roleType, roleName, sidInputBoxId) {
    const formData = new FormData();
    formData.append('sid', document.getElementById(sidInputBoxId).value);
    formData.append('roleName', roleName);

    if (!(roleType === 'agent' || roleType === 'global' || roleType === 'folder')) {
        throw  new Error('Unknown Role Type');
    }

    const url = `${rootURL}/folder-auth/removeSidFrom${roleType[0].toUpperCase()}${roleType.substring(1)}Role`;
    const request = new XMLHttpRequest();
    request.open('POST', url);
    request.onload = () => {
        if (request.status === 200) {
            showNotificationOK('Sid removed successfully.');
        } else {s
            showNotificationERROR('Unable to remove the sid.' + request.responseText);
        }
    };

    request.onerror = () => {
        showNotificationERROR('Unable to remove the sid from the role: ' + request.responseText);
    };

    // see addRole.js
    request.setRequestHeader('Jenkins-Crumb', crumb.value);
    request.send(formData);
}
