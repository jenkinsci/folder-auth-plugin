'use strict';

/**
 * Assign a sid to a role.
 *
 * @param roleType {('agent' | 'global' | 'folder')} the type of the role
 * @param index index of the role in its parent container
 */
function assignSid(roleType, index) {
    if (!['agent', 'global', 'folder'].includes(roleType)) {
        throw  new Error('Unknown Role Type');
    }

    const formData = new FormData();
    formData.append('sid', document.getElementById(`assign-sid-${roleType}-${index}`).value);
    formData.append('roleName', document.getElementById(`${roleType}RoleContainer`).children[index].getAttribute('data-role-name'));

    const url = `${rootURL}/folder-auth/assignSidTo${roleType[0].toUpperCase()}${roleType.substring(1)}Role`;
    const request = new XMLHttpRequest();
    request.open('POST', url);
    request.onload = () => {
        if (request.status === 200) {
            alert('Sid added successfully.');
            location.reload();
        } else {
            alert('Unable to assign sid to the role.' + request.responseText);
        }

    };

    request.onerror = () => {
        alert('Unable to add the sid to the role: ' + request.responseText);
    };

    // see addRole.js
    request.setRequestHeader('Jenkins-Crumb', crumb.value);
    request.send(formData);
}

/**
 * Removes a sid from a role.
 *
 * @param roleType {('agent' | 'global' | 'folder')} the type of the role
 * @param index index of the role in its parent container
 */
function removeSid(roleType, index) {
    if (!['agent', 'global', 'folder'].includes(roleType)) {
        throw  new Error('Unknown Role Type');
    }

    const formData = new FormData();
    formData.append('sid', document.getElementById(`assign-sid-${roleType}-${index}`).value);
    formData.append('roleName', document.getElementById(`${roleType}RoleContainer`).children[index].getAttribute('data-role-name'));

    const url = `${rootURL}/folder-auth/removeSidFrom${roleType[0].toUpperCase()}${roleType.substring(1)}Role`;
    const request = new XMLHttpRequest();
    request.open('POST', url);
    request.onload = () => {
        if (request.status === 200) {
            alert('Sid removed successfully.');
            location.reload();
        } else {
            alert('Unable to remove the sid.' + request.responseText);
        }
    };

    request.onerror = () => {
        alert('Unable to remove the sid from the role: ' + request.responseText);
    };

    // see addRole.js
    request.setRequestHeader('Jenkins-Crumb', crumb.value);
    request.send(formData);
}
