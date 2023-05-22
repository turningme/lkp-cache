/**
 * ncrt-formatter.js
 * @author: Bernhard Wolkerstorfer
 *
 * Description: Formatter for Send-to-Kindle documents.
 *
 * Copyright (c) 2012 Amazon.com, Inc. All rights reserved.
 */

/**
 * @constructor
 */
$SendToKindle.NcrtExtractor.Formatter = function (extractor, relaxedMode, feedMode) {
    // ######## Privileged Members ########
    /**
     * Extractor
     */
    this.extractor = extractor;

    /**
     * Flag for row alteration.
     */
    this.rowFlag = false;

    /**
     * Formatting mode flags,
     */
    this.relaxedMode = relaxedMode;
    this.feedMode = feedMode;

    /**
     * Extraction Utils
     */
    this.extractorUtils = new $SendToKindle.ExtractorUtils();

    /**
     * Formatting Rules
     */
    this.RULES = {
        ":header": {
            "rules": [ this.preserveFontStyle ],
            "attributes": /^(style)$/i
        },
        "a": {
            "rules" : [ this.makeLinkAbsolute ],
            "attributes": /^(href|title|name)$/i
        },
        "img": {
            "rules": [ this.formatImage ],
            "attributes": /^(width|height|src|alt|style)$/i
        },
        "table": {
            "rules": [ this.formatTable ],
            "attributes": /^(cellspacing|cellpadding)$/i
        },
        "tr,th,td": {
            "rules": [ this.formatTableElement ],
            "attributes": /^(colspan|rowspan|style)$/i
        },
        "font": {
            "attributes": /^(color|face|size)$/i
        },
        "li": {
            "rules": [ this.formatListItem ],
            "attributes": /^(style)$/i
        },
        "p,span": {
            "rules": [ this.preserveFontStyle ],
            "attributes": /^(style)$/i
        },
        "*": {
            "rules": [ this.cleanAttributes ]
        }
    };
};

$SendToKindle.NcrtExtractor.Formatter.prototype = {
    // ######## Constants ########
    /**
     * Special tags for exclusion.
     */
    SPECIAL_TAGS: /^br|img|td|th|h[1-6]|code|sub|sup|font|mbp:pageBreak$/i,

    /**
     * Text used in advertisement.
     */
    AD_TEXT: /^\s*(advertisement|werbung|anzeige)\s*$/i,

    // ######## Methods ########

    // -------- Node Information Methods --------
    /**
     * Get the tag name for a node.
     * @param $node Node
     */
    getTagName: function ($node) {
        // Sanity test the node.
        if ($node === null || $node === undefined || $node.length === 0) {
            return "#invalid";
        }

        // Read node properties.
        var nodeType = $node[0].nodeType;
        var nodeName = $node[0].tagName;

        // Analyze node.
        if (nodeType === 3) {
            return "#text";
        }
        else if (nodeType === 1 && nodeName && nodeName > '') {
            return nodeName.toLowerCase();
        }
        else {
            return "#invalid";
        }
    },

    // -------- Methods for the DOM Clean-Up --------
    /**
     * Test, if a link is local.
     * @param metrics Node Metrics
     * @return Local Flag
     */
    isLocalLink: function (metrics) {
        var href = metrics.node.attr("href");
        var path = $SendToKindle.getState().stateDocument.location.pathname;
        return (path !== "/") && (href === undefined || href[0] === "#" || href.search(path) !== -1);
    },

    /**
     * Test for removable node.
     * @param $node     Nodes
     * @param metrics   Metrics
     */
    isRemovable: function (metrics) {
        // Test, if node is empty.
        var isSpecialTag = metrics.tag_name.match(this.SPECIAL_TAGS) !== null;

        // Global removable constraints.
        var isRemovable = (!metrics.is_visible_node && !isSpecialTag) || metrics.is_skip_image || metrics.is_skip_node ||
            metrics.is_pagination || metrics.is_ad_link || metrics.is_ad_image || metrics.is_ad_node;

        // Strict content formatting constraints.
        if (!isRemovable && !this.relaxedMode) {
            // Constraint for removal on high link density.
            var isHighLinkDensity = metrics.subtree_link_text_density > 0.5 &&
                metrics.subtree_links > 1 &&
                metrics.subtree_valid_containers === 0 &&
                metrics.node.parent().is(":header") === false &&
                (metrics.subtree_links !== metrics.subtree_images || !metrics.is_link ||
                !metrics.is_metadata_node || !metrics.is_inside_metadata) &&
                metrics.subtree_large_images === 0 && !isSpecialTag;

            // Unify image flag.
            var isImage = metrics.is_small_image || metrics.is_medium_image || metrics.is_large_image;

            // Test, if the node is a DropCap.
            var isDropCap = (metrics.tag_name === "span" &&
            metrics.node.parent()[0].nodeName.match(/^p$/i) !== null &&
            metrics.node.text().length === 1);

            // Restriction for strict mode.
            isRemovable = isRemovable || isHighLinkDensity ||
                metrics.is_comment_node || (metrics.is_metadata_node && !this.feedMode) ||
                (metrics.is_link && this.isLocalLink(metrics)) ||
                (metrics.subtree_length_text < 1 && metrics.subtree_images === 0 && !isImage && !isSpecialTag && !isDropCap);
        }

        return isRemovable;
    },

    /**
     * Test, if a node is empty.
     * @param $node     Node
     */
    isEmpty: function ($node) {
        if ($node === undefined || $node === null || $node.length === 0 ||
            ($node.attr("class") !== undefined && $node.attr("class").match(/s2k-/i) !== null)) {
            return false;
        }
        else {
            // Gather node information.
            var text = $node.text(),
                textLength = $node.text().length,
                images = $node.find("img").length,
                isAdText = text.match(this.AD_TEXT) !== null;

            // Handle whitespace length.
            var whitespace = $node.text().match(/(\s)+/gi);
            var whitespaceLength = (whitespace !== null ? whitespace.join("").length : 0);

            // Test node for removal constraints.
            return (($node[0].nodeType === 1 && $node[0].nodeName.match(this.SPECIAL_TAGS) === null &&
            (whitespaceLength / (textLength || 1) > 0.8 || textLength === 0) && images === 0) || isAdText);
        }
    },

    /**
     * Remove links from images.
     * @param $node         Node
     */
    unlinkImages: function ($node) {
        var self = this;
        var images = $node.find("img");

        $.each(images, function () {
            var $n = $(this);
            var metrics = $n.data("s2k");

            if (metrics === undefined || metrics.is_ad_image === true || metrics.is_visible_node === false ||
                (metrics.node.width() < 50 || metrics.node.height() < 50) ||
                (metrics.node[0].naturalWidth !== undefined && metrics.node[0].naturalWidth < 50) ||
                (metrics.node[0].naturalHeight !== undefined && metrics.node[0].naturalHeight < 50) ||
                (metrics.is_skip_image && !self.relaxedMode)) {

                $n.remove();
            }
            else if (metrics.is_inside_link === true) {
                // Find parent node that is the link for this image.
                var m = metrics, parent = $n;
                while (m !== undefined && m.is_inside_link === true && m.is_link === false) {
                    parent = parent.parent();
                    m = parent.data("s2k");
                }

                if (m !== undefined && m.is_link === true) {
                    // Update metrics.
                    metrics.is_inside_link = false;
                    $n.data("s2k", metrics);

                    // Replace link with image.
                    parent.replaceWith($n);

                    // Push image to higher containers, if container only contains image to
                    // reduce DOM complexity and allow text captions.
                    parent = $n.parent();
                    while (parent.children().length === 1 && $.trim(parent.text()).length === 0 && !parent.is("td")) {
                        // Replace link with image.
                        parent.replaceWith($n);

                        // Update to new parent.
                        parent = $n.parent();
                    }
                }
            }
        });
    },

    // -------- Methods to format nodes for Kindle --------
    /**
     * Clean attributes on node.
     * @param tagName   Tag NAme
     * @param $node     Node
     * @param metrics   Metrics
     * @param $baseNode Base Node
     */
    cleanAttributes: function (tagName, $node, metrics, $baseNode) {
        if ($node.attr("class") === undefined || $node.attr("class").match(/s2k-/i) === null) {
            // Load attribute exclusion.
            var exclude = null;
            if ($node.is(":header")) {
                exclude = this.RULES[":header"].attributes;
            }
            else if ($node.is("tr,th,td")) {
                exclude = this.RULES["tr,th,td"].attributes;
            }
            else if ($node.is("p,span")) {
                exclude = this.RULES["p,span"].attributes;
            }
            else {
                exclude = this.RULES[tagName] ? this.RULES[tagName].attributes : null;
            }

            // Remove attributes.
            var element = $node[0],
                length = element.attributes.length,
                current = 0;

            while (length > 0 && current < length) {
                var an = element.attributes[current].nodeName;
                if (exclude === null || an.match(exclude) === null) {
                    element.removeAttribute(an);
                    length--;
                }
                else {
                    current++;
                }
            }
        }
    },

    /**
     * Format images.
     * @param tagName       Tag Name
     * @param $node         Node
     * @param metrics       Node Metrics
     * @param $baseNode     Base Node
     */
    formatImage: function (tagName, $node, metrics, $baseNode) {
        // Ensure image is not isolated from its content.
        var parent = $node.parent();
        while (parent.children().length === 1 && $.trim(parent.text()).length === 0 && !parent.is("td")) {
            parent.replaceWith($node);
            parent = $node.parent();
        }

        // Load additional parameters for image formatting.
        var parentTag = (parent.length === 1 ? parent[0].nodeName.toLowerCase() : undefined);
        var styleName = "s2k-default-image";
        var imageContainer = parent;
        var isFigure = false;

        // Try to read image width.
        var width = (metrics ? metrics.node[0].naturalWidth || metrics.node.width() : 0);
        width = Math.min(width || $node[0].naturalWidth || $node.width() || 500, 500);

        // Update lazy loading images.
        if ($node.attr("src") !== undefined && $node.attr("src").match(/(blank\w*|x)\.(bmp|jpg|jpeg|gif|png)/i) !== null) {
            $node.attr("src", $node.attr("data-src") || $node.attr("data-original") || $node.attr("src"));
            width = Math.min(Math.max(width, metrics.node.width()), 500);
        }

        // Clean node attributes for S2K attributes.
        $node.removeAttr("style").removeAttr("class").removeAttr("height");
        if (parent[0] !== $baseNode[0]) {
            parent.removeAttr("style").removeAttr("class").removeAttr("height");
        }

        // Wrap nodes into image wrapper, if required.
        if (parentTag === "div" && parent.length > 0 && parent[0] !== $baseNode[0] && parent.children().length <= 3 && this.feedMode === false) {
            parent.addClass(styleName);
        }
        else if (parentTag === "figure") {
            var content = $("<div>", {"class": styleName}).append(parent.contents().clone(true));
            parent.replaceWith(content);

            imageContainer = content;
            $node = content.find("img").first();
            isFigure = true;
        }
        else if (parentTag !== "td") {
            $node.wrap($("<div>", {"class": styleName}));
            imageContainer = $node.parent();
        }

        // Set width, if it is specified.
        if (width > 0) {
            $node.css({"width": width, "max-width": "100%"}).attr("width", width);
            imageContainer.css({"width": width});

            if (imageContainer.children().length > 1) {
                $node.css("margin-bottom", "1.0em");
            }
        }
    },

    /**
     * Preserve the font style of the element.
     * @param tagName       Tag Name
     * @param $node         Node
     * @param metrics       Node Metrics
     * @param $baseNode     Base Node
     */
    preserveFontStyle: function (tagName, $node, metrics, $baseNode) {
        if (metrics !== undefined && $node !== undefined) {
            // Save preserve-able font styles.
            var fontWeight = metrics.node.css("font-weight");
            var fontStyle = metrics.node.css("font-style");
            var textDeco = metrics.node.css("text-decoration");

            // Set preserved font style.
            $node.removeAttr("style").css({
                "font-style": fontStyle,
                "font-weight": fontWeight,
                "text-decoration": textDeco
            });
        }
    },

    /**
     * Format a list item.
     * @param tagName       Tag Name
     * @param $node         Node
     * @param metrics       Node Metrics
     * @param $baseNode     Base Node
     */
    formatListItem: function (tagName, $node, metrics, $baseNode) {
        // Load node information.
        var listStyle = metrics ? metrics.node.css("list-style-type") : "";
        var imageBullet = metrics ? metrics.node.css("background-image") : "";

        // Handle image bullets.
        if (listStyle === "none" && imageBullet !== "") {
            listStyle = "disc";
        }

        // Set list style type.
        $node.removeAttr("style").css("list-style-type", listStyle);
    },

    /**
     * Format a table.
     * @param tagName       Tag Name
     * @param $node         Node
     * @param metrics       Node Metrics
     * @param $baseNode     Base Node
     */
    formatTable: function (tagName, $node, metrics, $baseNode) {
        // Reset row color flag.
        this.rowFlag = false;

        // Add style class for table.
        $node.removeAttr("class").removeAttr("style").addClass("s2k-table");
    },

    /**
     * Format a table item.
     * @param tagName       Tag Name
     * @param $node         Node
     * @param metrics       Node Metrics
     * @param $baseNode     Base Node
     */
    formatTableElement: function (tagName, $node, metrics, $baseNode) {
        if (tagName === "tr") {
            this.rowFlag = !this.rowFlag;
            $node.css({"width": "", "height": "", "background": "", "background-color": ""})
                .attr("class", this.rowFlag ? "s2k-darkrow" : "");
        }
        else if (tagName === "th" || tagName === "td") {
            $node.css({"width": "", "height": "", "background": "", "background-color": ""});
        }
    },

    /**
     * Make relative URL absolute.
     * @param tagName       Tag Name
     * @param $node         Node
     * @param metrics       Node Metrics
     * @param $baseNode     Base Node
     */
    makeLinkAbsolute: function (tagName, $node, metrics, $baseNode) {
        var location = $SendToKindle.getActiveDocument(false).location;
        var href = $node.attr("href");

        if (href === undefined || href === null || href === "") {
            // Replace a non-link anchor with its text.
            $node.replaceWith("<span class='s2k-no-link'>" + $node.text() + "</span>");
        }
        else if (href !== undefined) {
            var isRelativeLink = (href.match(/^(http|https|mailto:|#.+)/i) === null);
            var isScriptLink = (href.match(/^(#|javascript:.*)$/i) !== null);

            // Update link.
            if (isRelativeLink === true && isScriptLink === false) {
                $node.attr("href", this.extractorUtils.makeUrlAbsolute(href));
            }
            else if (isScriptLink === true) {
                if ($node.parent().is(":header") === true) {
                    // Replace link with a content wrapper.
                    $node.replaceWith($("<div>").append($node.contents().clone(true)));
                }
                else if ($node.parent().is("p") === true) {
                    // Replace the script link with its text.
                    $node.replaceWith($node.text());
                }
                else {
                    // Fetch parent.
                    var parent = $node.parent();

                    // Remove node.
                    $node.remove();

                    // Remove parent, if it is empty.
                    $node = parent;
                    while (this.isEmpty($node)) {
                        parent = $node.parent();
                        $node.remove();
                        $node = parent;
                    }
                }
            }
        }
    },

    /**
     * Remove an removable node.
     */
    removeNode: function ($node, metrics, globalMetrics) {
        // Check, if node has a header as parent.
        if ($node.prev().is(":header") && !$node.is("p")) {
            $node.prev().remove();
        }

        // Fetch the nodes parent for further testing.
        var parent = $node.parent();

        // Remove the node.
        $node.remove();

        // Test, if the parent just contains an image after removal and deal with it.
        if (parent.children("img").length > 0 && parent.children("img").length === parent.children().length &&
            (parent.attr("class") !== undefined && parent.attr("class").match(/s2k-/i) === null)) {

            // Update recursion step with replacement.
            $node = parent.children("img");
            metrics = $node.data("s2k");

            if (metrics !== undefined && globalMetrics !== undefined) {
                metrics.score(globalMetrics);
            }

            // Replace the node.
            parent.replaceWith($node);
        }
    },

    /**
     * Reformat a table result as standard HTML result.
     *
     * @param node  Node
     */
    reformatTable: function ($node) {
        // Load table body.
        var table = $node.children().first();

        // Reformat table rows.
        table.children().each(function () {
            var $row = $('<div class="s2k-row-block"></div>');
            $row.data("s2k", $(this).data("s2k"));

            // Reformat table columns.
            $(this).children().each(function () {
                var $col = $('<div class="s2k-col-block"></div>')
                    .data("s2k", $(this).data("s2k"))
                    .append($(this).contents().clone(true));

                $row.append($col);
            });

            $node.append($row);
        });

        // Remove the original table.
        table.remove();
    },

    /**
     * Format and clean content.
     * @param $node         Node
     */
    format: function ($node) {
        // Unlink images.
        this.unlinkImages($node);

        // Remove possible title.
        if ($node.children().first().is(":header")) {
            $node.children().first().remove();
        }

        // Build a div structure from tables.
        if (this.getTagName($node.children().first()) === "tbody") {
            this.reformatTable($node);
        }

        // Apply styling rules.
        var formatter = this;
        $.each(this.RULES, function (selector, config) {
            // Continue to next set of rules, if no rules where defined.
            if (config.rules === undefined) {
                return;
            }

            // Filter nodes for filter.
            $node.find(selector).each(function () {
                // Load current node.
                var globalMetrics;
                var $n = $(this);
                var tagName = formatter.getTagName($n);
                var metrics = $n.data("s2k");
                var $baseNode = $n;

                // Find base node.
                while ($baseNode.is(".s2k-page") === false && $baseNode.parent().length > 0) {
                    $baseNode = $baseNode.parent();
                }

                // Rebalance node metrics.
                if ($baseNode.is(".s2k-page") && metrics !== undefined) {
                    globalMetrics = $baseNode.data("s2k");
                    metrics.score(globalMetrics);
                }

                // Test, if node can be removed and remove it.
                if (tagName === "#invalid" || $n.attr("id") === "s2k-status-message" ||
                    formatter.isEmpty($n) || (metrics !== undefined && formatter.isRemovable(metrics))) {
                    formatter.removeNode($n, metrics, globalMetrics);
                }

                // Apply rules to node.
                for (var i = 0, len = config.rules.length; i < len; i++) {
                    var applyRule = config.rules[i].bind(formatter);
                    applyRule(tagName, $n, metrics, $baseNode);
                }

                // Check if node is empty and remove it.
                if ($n !== undefined && $n.length > 0 && formatter.isEmpty($n)) {
                    formatter.removeNode($n, metrics, globalMetrics);
                }
            });
        });

        // Remove empty nodes from final markup.
        $node.find("*").filter(function () {
            return formatter.isEmpty($(this));
        });
    }
};