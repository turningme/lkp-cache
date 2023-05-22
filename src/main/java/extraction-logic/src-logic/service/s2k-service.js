/**
 * s2k-service.js
 * @author: Bernhard Wolkerstorfer
 *
 * Description: Service calling object.
 *
 * Copyright (c) 2012 Amazon.com, Inc. All rights reserved.
 */

/**
 * @constructor
 * @param {boolean=} isPreviewMode
 */
$SendToKindle.Service = function (isPreviewMode) {
    // ######## Privileged Members ####
    this.isPreviewMode = isPreviewMode || false;
};

$SendToKindle.Service.prototype = {
    // ######## Constants ########
    SERVICE_URL: "@serviceUrl@",

    // ######## Public Methods ########
    /**
     * Handle an XMLHttpRequest.
     * @param {Object} ajaxRequest  Settings for the AJAX request
     */
    ajax: function (ajaxRequest) {
        // Validate the input data.
        ajaxRequest = ajaxRequest || {};
        ajaxRequest.url = ajaxRequest.url || this.SERVICE_URL;
        ajaxRequest.type = ajaxRequest.type || "GET";
        ajaxRequest.data = ajaxRequest.data || null;
        ajaxRequest.success = ajaxRequest.success || function () {};
        ajaxRequest.error = ajaxRequest.error || function () {};
        ajaxRequest.complete = ajaxRequest.complete || function () {};
        ajaxRequest.timeout = ajaxRequest.timeout || 30000;
        ajaxRequest.async = ajaxRequest.async || true;
        ajaxRequest.cache = ajaxRequest.cache || false;
        ajaxRequest.binary = ajaxRequest.binary || false;
        ajaxRequest.responseType = ajaxRequest.responseType || "";
        ajaxRequest.dataType = ajaxRequest.dataType || "text";

        try {
            // Create and open an XMLHttpRequest.
            var xhr = new XMLHttpRequest();
            xhr.open(ajaxRequest.type, ajaxRequest.url, ajaxRequest.async);

            // Set request settings.
            xhr.timeout = ajaxRequest.timeout;
            xhr.responseType = ajaxRequest.responseType;

            // Disable cache, if requested.
            if (ajaxRequest.cache === false) {
                xhr.setRequestHeader("If-Modified-Since", new Date().toLocaleString());
            }

            // If request data was provided, set the correct content header.
            if (ajaxRequest.data !== null && ajaxRequest.binary === false) {
                xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            }

            // Set response event handler.
            xhr.addEventListener("error", function () {
                ajaxRequest.error(xhr, xhr.statusCode, xhr.statusText);
                ajaxRequest.complete();
            }, false);
            xhr.addEventListener("timeout", function () {
                ajaxRequest.error(xhr, xhr.statusCode, xhr.statusText);
                ajaxRequest.complete();
            }, false);
            xhr.addEventListener("load", function () {
                var response = xhr.responseType === "arraybuffer" ? xhr.response : xhr.responseText;
                var contentType = xhr.getResponseHeader("Content-Type");

                // Invoke the success callback with the data from the response.
                ajaxRequest.success(response, contentType);
                ajaxRequest.complete();
            }, false);

            // Submit the request.
            xhr.send((ajaxRequest.data && ajaxRequest.binary === false) ? $.param(ajaxRequest.data) : ajaxRequest.data);
        }
        catch (e) {
            // Invoke all error completion event listeners in case of an error.
            ajaxRequest.error(null, "exception", "S2K_AJAX_EXCEPTION");
            ajaxRequest.complete();
        }
    },

    /**
     * Download and BASE64-encode an image.
     * @param url       URL
     * @param success   Success Callback
     * @param error     Error Callback
     */
    downloadAndEncodeImage: function (url, success, error) {
        this.downloadAsBinary(url, function (binaryData, contentType) {
            success(this.encodeArrayBufferBase64(binaryData), contentType);
        }.bind(this), error);
    },

    /**
     * Download content in binary format.
     * @param url       URL
     * @param success   Success Callback
     * @param error     Error Callback
     */
    downloadAsBinary: function (url, success, error) {
        this.ajax({
            url: url,
            cache: true,
            responseType: "arraybuffer",
            timeout: 7000,
            success: function (data, contentType) {
                if (success !== undefined) {
                    if (contentType == null) { // try to fix content type if null
                        var ext = url.split('.').pop().toLowerCase();
                        if (ext === "jpg" || ext === "jpeg") {
                            contentType = "image/jpeg";
                        } else if (ext === "png") {
                            contentType = "image/png";
                        } else if (ext === "gif") {
                            contentType = "image/gif";
                        }
                    }
                    success(data, contentType);
                }
            }.bind(this),
            error: function () {
                if (error !== undefined) {
                    error();
                }
            }
        });
    },

    /**
     * Convert data to binary representation.
     * @param data  Data
     */
    convertToBinary: function (data) {
        // Encode UCS-2 as UTF-8.
        var utf8Data = unescape(encodeURIComponent(data));

        // Create a buffer for UTF-8.
        var buffer = new ArrayBuffer(utf8Data.length);

        // Create an array buffer view.
        var udata = new Uint8Array(buffer);

        // Store the data in the binary buffer.
        for (var i = 0, len = utf8Data.length; i < len; i++) {
            udata[i] = utf8Data.charCodeAt(i);
        }

        return udata;
    },

    /**
     * Send a RefTag to the service.
     * @param {string} refTag
     */
    sendRefTag: function (refTag) {
        // Format reftag for browser extension.
        refTag = "stk_" + $SendToKindle.platformInfo.ref + "_ext_" + refTag;

        // Emit a RefTag.
        this.ajax({
            url:  this.SERVICE_URL + "?action=reftag&ref_=" + refTag,
            type: "GET",
            dataType: "json",
            cache: false,
            timeout: 7000
        });
    },

    /**
     * Send preview metrics to remote service.
     * @param metrics Metrics
     * @param callback
     */
    emitMetrics: function (metrics, callback) {
        this.ajax({
            type: "POST",
            dataType: "json",
            cache: false,
            timeout: 10000,
            data: {
                "action": "metrics",
                "extName": $SendToKindle.platformInfo.name,
                "extVersion": $SendToKindle.platformInfo.version,
                "metrics": metrics
            },
            complete: callback
        });
    },

    /**
     * Encode an array buffer as Base64 string.
     * @param buffer            Array Buffer / String
     * @returns Base64-encoded String
     */
    encodeArrayBufferBase64: function (buffer) {
        var base64    = '';
        var encodings = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';

        var bytes         = new Uint8Array(buffer);
        var byteLength    = bytes.byteLength;
        var byteRemainder = byteLength % 3;
        var mainLength    = byteLength - byteRemainder;

        var a, b, c, d;
        var chunk;

        // Main loop deals with bytes in chunks of 3
        for (var i = 0; i < mainLength; i = i + 3) {
            // Combine the three bytes into a single integer
            chunk = (bytes[i] << 16) | (bytes[i + 1] << 8) | bytes[i + 2];

            // Use bitmasks to extract 6-bit segments from the triplet
            a = (chunk & 16515072) >> 18; // 16515072 = (2^6 - 1) << 18
            b = (chunk & 258048)   >> 12; // 258048   = (2^6 - 1) << 12
            c = (chunk & 4032)     >>  6; // 4032     = (2^6 - 1) << 6
            d = chunk & 63;               // 63       = 2^6 - 1

            // Convert the raw binary segments to the appropriate ASCII encoding
            base64 += encodings[a] + encodings[b] + encodings[c] + encodings[d];
        }

        // Deal with the remaining bytes and padding
        if (byteRemainder === 1) {
            chunk = bytes[mainLength];

            a = (chunk & 252) >> 2; // 252 = (2^6 - 1) << 2

            // Set the 4 least significant bits to zero
            b = (chunk & 3) << 4; // 3   = 2^2 - 1

            base64 += encodings[a] + encodings[b] + '==';
        }
        else if (byteRemainder === 2) {
            chunk = (bytes[mainLength] << 8) | bytes[mainLength + 1];

            a = (chunk & 64512) >> 10; // 64512 = (2^6 - 1) << 10
            b = (chunk & 1008)  >>  4; // 1008  = (2^6 - 1) << 4

            // Set the 2 least significant bits to zero
            c = (chunk & 15)    <<  2; // 15    = 2^4 - 1

            base64 += encodings[a] + encodings[b] + encodings[c] + '=';
        }

        return base64;
    }
};