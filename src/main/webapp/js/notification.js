function showNotification(message, options, reload = false) {
    notificationBar.show(message, options);
    if (reload) {
        window.setTimeout(function () {
            location.reload();
        }, notificationBar.DELAY);
    }
}

function showNotificationOK(message) {
    showNotification(message, notificationBar.OK, true);
}

function showNotificationERROR(message) {
    showNotification(message, notificationBar.ERROR);
}
