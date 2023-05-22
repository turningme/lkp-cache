/**
 * state.js
 * @author: Bernhard Wolkerstorfer
 *
 * Description: Application State
 *
 * Copyright (c) 2012 Amazon.com, Inc. All rights reserved.
 *
 */

/**
 * @constructor
 */
$SendToKindle.State = function () {
    // ######## Privileged Members ########
    this.stateWindow = $SendToKindle.getActiveWindow();
    this.stateDocument = $SendToKindle.getActiveDocument(true);
    this.service = new $SendToKindle.Service();
    this.metrics = new $SendToKindle.Metrics(this.service);
    this.content = null;
    this.abortWorkflow = false;
};
