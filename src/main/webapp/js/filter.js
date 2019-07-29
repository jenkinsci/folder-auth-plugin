'use strict';

const doFilter = async (type) => {
    let container;
    switch (type) {
        case 'global':
            container = document.getElementById('globalRoleContainer');
            break;
        case 'agent':
            container = document.getElementById('agentRoleContainer');
            break;
        case 'folder':
            container = document.getElementById('folderRoleContainer');
            break;
        default:
            throw new Error('Unknown Role type');
    }

    const str = document.getElementById('globalRoleFilter').value;
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

document.getElementById('globalRoleFilter').addEventListener('input', () => doFilter('global'));
