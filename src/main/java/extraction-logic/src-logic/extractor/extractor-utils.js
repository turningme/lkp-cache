/**
 *
 * extractor-utils.js
 *
 * @author: Bernhard Wolkerstorfer
 *
 * Description: Utilities for extractors.
 *
 * Copyright (c) 2012 Amazon.com, Inc. All rights reserved.
 *
 */

/**
 * @constructor
 */
$SendToKindle.ExtractorUtils = function () {
    // ######## Members ########
    /**
     * Service Instance
     */
    this.service = new $SendToKindle.Service();
};

$SendToKindle.ExtractorUtils.prototype = {
    // ######## Constants ########
    /**
     * Supported image types.
     */
    SUPPORTED_IMAGE_TYPES: /^(image\/(png|jpeg|jpg|gif|bmp|x-(bmp|bitmap|xbitmap|win-bitmap|windows-bitmap|ms-bmp)|ms-bmp))|(application\/(x-bmp|x-win-bitmap))/i,

    /**
     * Supported pagination types.
     */
    SUPPORTED_PAGE_FIELDS: /[\?&]((page\w*|pg|pgno|first|start)=(\d+,)?\d+)|(\/\d+\/$)|(\/\d+$)|(_\d+)(\.\w+)$/i,

    // ######## Methods ########
    /**
     * Ensure that a given URL is absolute.
     * @param {string} url      URL
     */
    makeUrlAbsolute: function (url) {
        // Ensure URL is absolute for cross-browser compatibility.
        if (url.match(/^\/\//i) !== null) {
            url = location.protocol + url;
        }
        else if (url.match(/^(http|https|mailto:|#.+)/i) === null) {
            // Set absolute path, if needed.
            if (url.substr(0, 1) !== "/") {
                // Separate dir-path from file-path
                var pathname = location.pathname.substring(0, location.pathname.lastIndexOf("/")) + "/";
                url = pathname + url;
            }

            // Append the origin.
            url = (location.origin || (location.protocol + "//" + location.host)) + url;
        }

        return url;
    },

    /**
     * Download and embed all images for offline use.
     * @param images        Images
     * @param index         Image Index
     * @param success       Callback
     * @param task          Active task
     */
    embedImages: function (images, index, success, task) {
        var self = this;
        // Handle all images in the result document.
        if (index < images.length) {
            var img = $(images[index]);

            // Handle ASP.NET MVC lazy loading logic.
            var source = img.attr("src");
            if (source === undefined) {
                // Download next image.
                this.embedImages(images, index + 1, success, task);
                return;
            }

            // Ensure a valid URL for the image.
            source = this.makeUrlAbsolute(source);

            if (task && task.isTimedOut()) {
                img.remove();
                this.embedImages(images, index + 1, success, task);
                return;
            }
            // Download image and encode as Base64 string.
            this.service.downloadAndEncodeImage(source, function (encodedData, dataType) {
                if (dataType.match(self.SUPPORTED_IMAGE_TYPES) !== null) {
                    // Replace image with offline version.
                    img.attr("src", "data:" + dataType + ";base64," + encodedData);
                }
                else {
                    // Remove unsupported image.
                    img.remove();
                }

                // Download next image.
                self.embedImages(images, index + 1, success, task);
            }, function () {
                // Remove image from DOM.
                img.remove();
                // Download next image.
                self.embedImages(images, index + 1, success, task);
            });
        }
        else {
            success();
        }
    },

    /**
     *
     * Analyze page links for multi-page articles.
     * @param l1    Link for first page.
     * @param l2    Link for second page.
     *
     * [Find a solution that is faster than O(n), if possible in JS.]
     *
     */
    analyzePageLinks: function (l1, l2) {
        var pageLink = null;

        if (l2 === undefined || l1 === l2) {
            // Find a matching page field.
            var matches = l1.match(this.SUPPORTED_PAGE_FIELDS);

            // Match the link.
            if (matches !== null && matches[1] !== undefined) {
                // Match was page query field.
                pageLink = l1.replace(matches[1], matches[2] + "=" + (matches[3] || "") + "_PN_");
            }
            else if (matches !== null && (matches[4] !== undefined || matches[5] !== undefined || matches[6] !== undefined)) {
                // Match was a permlink URL.
                var placeHolder = null;

                // Load the correct placeholder.
                if (matches[4] !== undefined) {
                    placeHolder = "/_PN_/";
                }
                else if (matches[5] !== undefined) {
                    placeHolder = "/_PN_";
                }
                else {
                    placeHolder = "__PN_" + matches[7];
                }

                pageLink = l1.replace(this.SUPPORTED_PAGE_FIELDS, placeHolder);
            }
        }
        else {
            // Find the length of the smallest string.
            var length = Math.min(l1.length, l2.length);

            // Analyze string an replace the page numbers by _PN_.
            var pageNumber = false;
            pageLink = "";

            for (var i = 0; i < length; i++) {
                if ((l1.charCodeAt(i) ^ l2.charCodeAt(i)) === 0 && pageNumber === false) {
                    pageLink += l1.charAt(i);
                }
                else if (pageNumber === true) {
                    pageNumber = (pageNumber === true && l1[i].match(/\d/i) !== null);
                    if (pageNumber === false) {
                        pageLink += l1.charAt(i);
                    }
                }
                else {
                    pageLink += "_PN_";
                    pageNumber = true;
                }
            }
        }

        return pageLink;
    },

    /**
     * Test a link against the page template link.
     * @param link          Link
     * @param pageLink      Template
     * @param pageParam     Parameter for the URL indicating the page.
     */
    testPageLink: function (link, pageLink, pageParam) {
        var pageNumber = null;

        // Fetch page number from original link.
        if (pageParam[0] === "/") {
            pageNumber = link.match(/\/(\d+)\/?$/i);
        }
        else if (pageParam[0] === "_") {
            pageNumber = link.match(/_(\d+)\.\w+$/i);
        }
        else {
            pageNumber = link.match(new RegExp(pageParam.replace("?", "\\?") + "(\\d+)", "i"));
        }

        // Test for page number.
        pageNumber = (pageNumber !== null ? pageNumber = pageNumber[1] : "_PN_");

        // Create a page link from the template and compare to link.
        // Do a sanity test to make sure that the link at least contain
        // each other.
        return link.indexOf(pageLink.replace(/_PN_/g, pageNumber)) !== -1;
    },

    /**
     * Get the path for a node to the body tag.
     * @param $node         Node
     * @return XPath
     */
    getPathForNode: function ($node) {
        var path = "";
        var $n = $node;
        var metrics = $n.data("s2k");

        // Traverse to the body node.
        while (metrics.tag_name !== "body") {
            path = "/" + metrics.node_name + path;
            $n = $n.parent();
            metrics = $n.data("s2k");
        }

        return path;
    },

    /**
     * Get a node from a path or ID.
     * @param $baseNode
     * @param path  Path or ID
     * @return Node
     */
    getNodeFromPath: function ($baseNode, path) {
        if (path[0] === "#") {
            $baseNode = $baseNode.find(path);
        }
        else {
            // Split path for traversal.
            path = path.substring(1).split("/");
            var $n = $baseNode;

            // Traverse the DOM to the content node(s).
            for (var i = 0, length = path.length; i < length - 1; i++) {
                if (path[i] !== null) {
                    $n = $n.filter(path[i]);
                    if ($n === undefined || $n.length === 0) {
                        break;
                    }
                    $n = $($n).children();
                }
            }

            // Load the matching nodes, if traversal was successful.
            // If there was a problem in the traversal, try to do a
            // greedy load of result nodes.
            if ($n === undefined) {
                $baseNode = $baseNode.find(path[length - 1]);
            }
            else {
                $baseNode = $n.filter(path[length - 1]);
            }
        }
        return $baseNode;
    }
};