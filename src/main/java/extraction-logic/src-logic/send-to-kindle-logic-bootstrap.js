/**
 * send-to-kindle-logic-bootstrap.js
 * @author: Bernhard Wolkerstorfer
 * 
 * Description: Bootstrap for background logic.
 * 
 * Copyright (c) 2012 Amazon.com, Inc. All rights reserved.
 */

var $SendToKindle = window.$SendToKindle = {
    // ######## Global Members ########
    "platformInfo": {"name": "s2k-extractor", "platform": "phantomJS", "ref": "ukn"}
};

/**
 * Fetch the current active window.
 * @return Window
 */
$SendToKindle.getActiveWindow = function () {
    return window;
};

/**
 * Fetch the current active document.
 * @param stateInit State Initialization Mode
 * @return Document
 */
$SendToKindle.getActiveDocument = function (stateInit) {
    return window.document;
};

/**
 * Fetch the active state for the current window.
 * @return State
 */
$SendToKindle.getState = function () {
    var state = $SendToKindle.getActiveWindow().stkState;
    if (state === undefined) {
        state = $SendToKindle.getActiveWindow().stkState = new $SendToKindle.State();
    }
    return state;
};