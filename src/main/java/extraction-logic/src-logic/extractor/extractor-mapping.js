/**
 * extractor-mapping.js
 * 
 * @author: Bernhard Wolkerstorfer
 * 
 * Description: Mapping for extractor types.
 * 
 * Copyright (c) 2012 Amazon.com, Inc. All rights reserved.
 */

$SendToKindle.EXTRACTORS = [
    {"schema": /(maps\.google\.\w+)|(\w\.bing\.\w+\/maps)/i, "type": $SendToKindle.MapExtractor, "metrics": "c_extraction_maps"}
];

/**
 * Create an extractor for this type.
 * @param state State
 */
$SendToKindle.createExtractor = function (state) {
    var href = state.stateDocument.location.href;

    for (var i = 0, len = $SendToKindle.EXTRACTORS.length; i < len; i++) {
        if ($SendToKindle.EXTRACTORS[i].schema.test(href) === true) {
            state.metrics.count(state.metrics.NAMES[$SendToKindle.EXTRACTORS[i].metrics]);
            return new $SendToKindle.EXTRACTORS[i].type();
        }
    }

    // Fallback for general cases.
    state.metrics.count(state.metrics.NAMES.c_extraction_ncrt);
    return new $SendToKindle.NcrtExtractor();
};