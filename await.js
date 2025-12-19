(function() {
try {
    console.log("Applying focus-lock script...");

    // 1. Mock document properties to always show "visible"
    Object.defineProperty(document, 'hidden', { value: false, writable: true });
    Object.defineProperty(document, 'visibilityState', { value: 'visible', writable: true });
    Object.defineProperty(document, 'webkitVisibilityState', { value: 'visible', writable: true });

    // 2. Mock document.hasFocus() to always return true
    document.hasFocus = function() { return true; };

    // 3. Block "blur" and "visibilitychange" events
    // We use the 'true' flag to capture the event before it reaches the page's listeners
    const blockEvent = function(e) {
        e.stopImmediatePropagation(); // Stop other listeners from seeing this
        e.preventDefault(); // Prevent default action
        console.log(`Blocked ${e.type} event`);
    };

    const eventsToBlock = [
        'visibilitychange',
        'webkitvisibilitychange',
        'mozvisibilitychange',
        'blur',
        'mozblur',
        'webkitblur'
    ];

    eventsToBlock.forEach(eventName => {
        // Add listener to window and document to catch them everywhere
        window.addEventListener(eventName, blockEvent, true);
        document.addEventListener(eventName, blockEvent, true);
    });

    // 4. Clear existing onblur handlers if they are defined directly
    window.onblur = null;
    document.onblur = null;

    console.log("Focus-lock enabled. The tab now believes it is always active.");

} catch (err) {
    console.error("Error applying focus script:", err);
}
})();
