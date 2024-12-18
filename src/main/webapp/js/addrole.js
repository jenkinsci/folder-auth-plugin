'use strict';

// noinspection JSUnusedGlobalSymbols
/**
 * Adds a global role
 */
function addGlobalRole() {
    const roleName = document.getElementById('globalRoleName').value;
    if (!roleName || roleName.length < 3) {
        alert('Please enter a valid name for the role to be added.');
        return;
    }

    const response = {
        name: roleName,
        permissions: Array.from(document.getElementById('global-permission-select').options)
            .filter(option => option.selected)
            .map(option => option.value)
    };

    if (response.permissions.length <= 0) {
        alert('Please select at least one permission');
        return;
    }

    sendPostRequest(`${rootURL}/folder-auth/addGlobalRole`, response);
};

// noinspection JSUnusedGlobalSymbols
/**
 * Adds a Folder Role
 */
function addFolderRole() {
    const roleName = document.getElementById('folderRoleName').value;
    if (!roleName || roleName.length < 3) {
        alert('Please enter a valid name for the role to be added');
        return;
    }

    const response = {
        name: roleName,
        permissions: Array.from(document.getElementById('folder-permission-select').options)
            .filter(option => option.selected)
            .map(option => option.value),
        folderNames: Array.from(document.getElementById('folder-select').options)
            .filter(option => option.selected)
            .map(option => option.value)
    };

    if (!response.permissions || response.permissions.length <= 0) {
        alert('Please select at least one permission');
        return;
    }

    if (!response.folderNames || response.folderNames.length <= 0) {
        alert('Please select at least one folder on which this role will be applicable');
        return;
    }

    sendPostRequest(`${rootURL}/folder-auth/addFolderRole`, response);
};

// noinspection JSUnusedGlobalSymbols
/**
 * Adds an agent Role
 */
function addAgentRole() {
    const roleName = document.getElementById('agentRoleName').value;
    if (!roleName || roleName.length < 3) {
        alert('Please enter a valid name for the role to be added');
        return;
    }

    const response = {
        name: roleName,
        agentNames: Array.from(document.getElementById('agent-select').options)
            .filter(option => option.selected)
            .map(option => option.value),
        permissions: Array.from(document.getElementById('agent-permission-select').options)
            .filter(option => option.selected)
            .map(option => option.value)
    };

    if (!response.permissions || response.permissions.length <= 0) {
        alert('Please select at least one permission');
        return;
    }

    if (!response.agentNames || response.agentNames.length <= 0) {
        alert('Please select at least one agent on which this role will be applicable');
        return;
    }

    sendPostRequest(`${rootURL}/folder-auth/addAgentRole`, response);
};

/**
 * Sends a POST request to {@code postUrl}
 * @param postUrl the URL
 * @param json JSON data to be sent
 */
const sendPostRequest = (postUrl, json) => {
    const xhr = new XMLHttpRequest();
    xhr.open('POST', postUrl, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    // Jelly file sets up the crumb value for CSRF protection
    if (crumb.value) {
        xhr.setRequestHeader('Jenkins-Crumb', crumb.value);
    }

    xhr.onload = () => {
        if (xhr.status === 200) {
            alert('The role was added successfully');
            location.reload(); // refresh the page
        } else {
            alert('Unable to add the role\n' + xhr.responseText);
        }
    };

    xhr.send(JSON.stringify(json));
};

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".add-role-button").forEach((button) => {
        button.addEventListener("click", (event) => {
            const { action } = event.target.dataset;

            window[action]();
        });
    });

    document.querySelectorAll(".delete-role-form").forEach((form) => {
        form.addEventListener("submit", (event) => {
            if (!confirm(event.target.dataset.confirmDeleteText)) {
                event.preventDefault();
            }
        });
    })
});
