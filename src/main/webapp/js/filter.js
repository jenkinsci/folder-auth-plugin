'use strict';

(() => {
    /**
     * Asynchronously filters out roles whose name matches the string entered in the text box.
     * @param {('agent'|'global'|'folder')} type
     * @returns {Promise<void>} a promise that completes when search completes
     * @throws Error when unknown type is provided.
     */
    const doFilter = async (type) => {
        let container;
        switch (type) {
            case 'global':
            case 'agent':
            case 'folder':
                container = document.getElementById(`${type}RoleContainer`);
                break;
            default:
                throw new Error('Unknown Role type');
        }

        const str = document.getElementById(`${type}RoleFilter`).value;
        let matching = 0;

        for (let i = 0; i < container.childNodes.length; i++) {
            const element = container.childNodes[i];
            if (element.getAttribute('roleName').includes(str)) {
                element.style.display = 'flex';
                matching++;
            } else {
                element.style.display = 'none';
            }
        }

        const labelId = `no-matching-${type}-role-label`;
        const label = document.getElementById(labelId);

        if (!matching) {
            if (!label) {
                const newLabel = document.createElement('p');
                newLabel.innerText = 'No matching roles found.';
                newLabel.id = labelId;
                container.parentElement.appendChild(newLabel);
            }
        } else if (label) {
            container.parentElement.removeChild(label);
        }
    };

    // if no agent role is present, agentRoleFilter input will not be present, similarly for others
    const agentRoleFilter = document.getElementById('agentRoleFilter');
    if (agentRoleFilter) {
        addEventListener('input', () => doFilter('agent'));
    }

    const folderRoleFilter = document.getElementById('folderRoleFilter');
    if (folderRoleFilter) {
        folderRoleFilter.addEventListener('input', () => doFilter('folder'));
    }

    const globalRoleFilter = document.getElementById('globalRoleFilter');
    if (globalRoleFilter) {
        globalRoleFilter.addEventListener('input', () => doFilter('global'));
    }
})();
