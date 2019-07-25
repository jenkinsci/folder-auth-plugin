'use strict';

/**
 * Sends a GET request and renders the result on the web page.
 */
const getFolders = () => {
    const xhr = new XMLHttpRequest();
    xhr.open('GET', `${rootURL}/folder-auth/getAllFolders`, true);
    xhr.send(null);

    xhr.onload = () => {
        renderFoldersAsOptions(JSON.parse(xhr.responseText));
    };

    xhr.onerror = () => {
        alert(`Unable to get the list of folders: ${xhr.responseText}`);
    };
};

/**
 * Renders all folders on the web UI.
 * @param folders list of folder names
 */
const renderFoldersAsOptions = (folders) => {
    const select = document.getElementById('folder-select');
    const loadingLabel = document.getElementById('loading-folders');

    if (!(Array.isArray(folders) && folders.length)) {
        loadingLabel.innerText = 'Please create a folder before adding a folder role.';
        return;
    }

    folders.forEach(folder => {
        const option = document.createElement('option');
        option.value = folder;
        option.innerHTML = folder;
        select.appendChild(option);
    });

    // make the <select> element visible
    select.style.display = 'block';

    // remove the 'Loading folders' label
    loadingLabel.parentElement.removeChild(loadingLabel);

    // enable submitting the form
    document.getElementById('add-folder-role-button').removeAttribute('disabled');
};
