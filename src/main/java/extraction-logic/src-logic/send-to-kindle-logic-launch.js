/**
 * send-to-kindle-launch.js
 * @author: Bernhard Wolkerstorfer
 * 
 * Description: Launch logic for Send-to-Kindle.
 * 
 * Copyright (c) 2012 Amazon.com, Inc. All rights reserved.
 */

// #################################################################### 
// ## Extraction Logic                                               ##
// ####################################################################
/**
 * Called once extraction is complete. Returns extraction result to service or
 * null on failure.
 * 
 * @param {Object}
 *            status Status
 */
$SendToKindle.launchProgress = function(status) {

    var state = $SendToKindle.getState();

    // Handle the extraction result.
    if (status.success === true && state.abortWorkflow === false) {
        state.metrics.stopTimer(state.metrics.NAMES.t_extraction);
        state.content = status.data;

        // Emit source metrics.
        state.metrics.countWithPrefix(state.metrics.NAMES.c_source,
                state.content.source);

        callback(state.content.asStorageJson());
    } else if (status.error === true && state.abortWorkflow === false) {

        // Emit metrics.
        state.metrics.countWithPrefix(state.metrics.NAMES.c_source,
                state.stateDocument.location.hostname);
        state.metrics.count(state.metrics.NAMES.c_extraction_error);
        state.metrics.terminate();

        callback({ });
    }
};

/**
 * Entry point for extraction engine.
 */
$SendToKindle.launch = function() {
    var state = $SendToKindle.getState();
    state.metrics.init();

    if (state.stateDocument.body !== null) {
        state.metrics.startTimer(state.metrics.NAMES.t_extraction);

        var extractor = $SendToKindle.createExtractor(state);
        extractor.extract($SendToKindle.launchProgress, callback);
    }
};