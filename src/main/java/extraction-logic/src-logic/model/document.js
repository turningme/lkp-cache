/**
 * document.js
 * @author: Bernhard Wolkerstorfer
 *
 * Description: Send To Kindle Document
 *
 * Copyright (c) 2011 Amazon.com, Inc. All rights reserved.
 */

/**
 * @constructor
 * @param {Object} dataType
 * @param {jQuery} $c
 */
$SendToKindle.Document = function ($c) {
    // ######## Privileged Members ########
    this.title           = null;
    this.author          = null;
    this.publicationDate = null;
    this.source          = null;
    this.url             = null;
    this.metadata        = null;
    this.contentNode     = null;
    this.confidence      = 1.0;

    // --------- Content ---------
    if ($c !== null) {
        this.contentNode = $c;
    }

    // -------- Conversion Format --------
    this.$kindleContent = $(
        "<div>" +
        "   <div class='s2k-article'>" +
        "       <div class='s2k-article-header'>" +
        "           <div class='s2k-title'></div>" +
        "           <hr style='background: transparent; color: transparent; border-left: none; border-right: none; border-top: none; border-bottom: 1px dashed #000;' />" +
        "           <div class='s2k-byline'>" +
        "               <span class='s2k-author'></span>" +
        "               <a class='s2k-source-short'></a>" +
        "               <span class='s2k-timestamp'></span>" +
        "           </div>" +
        "       </div>" +
        "       <div class='s2k-article-body'></div>" +
        "       <div class='s2k-article-footer'>" +
        "            <hr style='background: transparent; color: transparent; border-left: none; border-right: none; border-top: none; border-bottom: 1px dashed #000;' />" +
        "            Source: <a class='s2k-source-long'></a>" +
        "       </div>" +
        "   <div>" +
        "</div>");
};

$SendToKindle.Document.prototype = {
    // ######## Constants ########
    BYLINE_SEPARATOR: "<span class='byline-separator'>&nbsp;&#149;&nbsp;</span>",

    // ######## Methods ########
    /**
     * Set extraction result.
     * @param $c    Content
     */
    setContent: function ($c) {
        this.contentNode = $c.clone(true);
    },

    /**
     * Escape HTML for plain insertion.
     * @param {string} textToEscape Text that should be escaped.
     * @returns {string} Escaped Text
     */
    escapeHTML: function (textToEscape) {
        return textToEscape.replace(/[&"<>]/g, function (ch) {
            return { "&": "&amp;", '"': "&quot", "<": "&lt;", ">": "&gt;" }[ch];
        });
    },

    /**
     * Format a content template.
     * @param {jQuery} $ct   Content Template
     * @param {Object} pdoc  Kindle Personal Document
     */
    formatContentTemplate: function ($ct, pdoc) {
        if (this.contentNode !== null) {
            // Set the article content.
            var entry = $ct.find(".s2k-article-body");
            entry.empty().append(this.contentNode.clone(true));

            $.each(entry.find(".s2k-default-image"), function () {
                $(this).wrap("<div class='s2k-image-wrap'></div>");
            });

            $.each(entry.find("blockquote"), function () {
                var content = $(this).contents();
                var $n = $("<div class='s2k-blockquote'></div>").append(content);
                $(this).replaceWith($n);
            });

            entry.find(":header").addClass(".s2k-text-header");

            // Set metadata.
            $ct.find(".s2k-title").text(pdoc.title);

            // Set author
            if (pdoc.author !== null) {
                $ct.find(".s2k-author")
                    .append("By ")
                    .append($("<span class='fn'></span").text(pdoc.author))
                    .append(this.BYLINE_SEPARATOR);
            }

            // Set publication date.
            if (pdoc.publicationDate !== null) {
                $ct.find(".s2k-timestamp")
                    .append(this.BYLINE_SEPARATOR)
                    .append(pdoc.publicationDate);
            }

            // If the HTML tag has the original HTML property set by the client, overwrite the
            // URL and source of the pdoc with it.
            var originalArticleURL = document.documentElement.getAttribute("data-s2k-original-url");
            if (originalArticleURL !== null) {
                var anchor = document.createElement("a");
                anchor.href = originalArticleURL;

                pdoc.source = anchor.hostname;
                pdoc.url = originalArticleURL;
            }

            // Set source header short link.
            $ct.find(".s2k-source-short").attr("href", pdoc.url);
            $ct.find(".s2k-source-short").text(pdoc.source);

            // Generate the footer URL text.
            var footerUrl = pdoc.url;
            if (footerUrl.length > 140) {
                footerUrl = footerUrl.substring(0, 137) + "...";
            }

            // Set the footer URL text.
            $ct.find(".s2k-source-long").attr("href", pdoc.url);
            $ct.find(".s2k-source-long").text(footerUrl);

            return $ct;
        }
    },

    /**
     * Fetch Kindle formatted content.
     *
     * @return {string} Kindle-formatted Content
     */
    getKindleContent: function () {
        // Format the content template
        this.formatContentTemplate(this.$kindleContent, this, true);

        //@preserve Create HTML document for KindleGen, this string will not be displayed parsed into a DOM.
        return "<ht" + "ml>" +
            "<he" + "ad>" +
            "  <meta http-equiv='Content-Type' content='text/html;charset=utf-8' />" +
            "  <title>" + this.escapeHTML(this.title) + "</title>" +
            "  <style type='text/css'>" +
            "    .s2k-article-footer { font-size: 0.8em; text-align: left }" +
            "    .s2k-title { font-size: 1.5em; font-weight: bold; color: black; }" +
            "    .s2k-byline { font-size: 0.8em; font-style: italic; color: black; margin-bottom: 1em; }" +
            "    .s2k-author .fn { font-weight: bold }" +
            "    .s2k-image-wrap { width: 100%; text-align: center; padding-top: 1.25em; padding-bottom: 1.25em;}" +
            "    .s2k-default-image { border: #000 hidden 1px; line-height: 1em; margin: 0px auto; padding: 0.25em; text-align: center; font-size: 50%; }" +
            "    .s2k-blockquote { margin: 0em 2em 0em 2em; font-style: italic; }" +
            "    .s2k-text-header { margin: 1em }" +
            "    .s2k-table table { width: 100%; padding-top: 1.0em; padding-bottom: 1.0em;}" +
            "    .s2k-table table, .s2k-table tbody, .s2k-table tr, .s2k-table td, .s2k-table th { margin: 20px 0px; border: none; border-collapse: collapse; border-spacing: 2px; font-size: inherit; }" +
            "    .s2k-table td, .s2k-table th { border-top: none; border-left: none; border-right: none; border-bottom: 1px solid #111; }" +
            "    .s2k-maps-table { width: 100%; border-collapse: collapse; font-size: inherit; }" +
            "    .s2k-maps-table td { vertical-align: middle; padding-bottom: 5px; padding-top: 10px; } " +
            "    .s2k-waypoint-header .s2k-maps-text { font-weight: bold; }" +
            "    .s2k-maps-waypoints { margin-top: 20px; }" +
            "    .s2k-waypoint-header { border-top: 1px solid #000; border-bottom: 1px solid #000; }" +
            "    .s2k-waypoint-info { font-size: 70%; text-align: right;  border-top: 1px solid #000;}" +
            "    .s2k-maps-distance { width: 10%; font-size: 80%; text-align: right; }" +
            "    .s2k-maps-num { width: 5%; font-size: 80%; text-align: center; padding: 0px 10px; }" +
            "    .s2k-maps-icon { width: 5%; padding: 0px 10px; text-align: center; }" +
            "    .s2k-maps-text { padding-left: 20px; }" +
            "    .s2k-maps-step-duration { font-size: 80%; }" +
            "    .s2k-maps-step-notice { color: #800000; font-size: 80%; }" +
            "    .s2k-maps-copyright, .s2k-maps-disclosure { margin-top: 20px; line-height: 1.5em; text-align: center; font-size: 70%; }" +
            "    .s2k-maps-disclosure { margin-top: 50px; }" +
            "    tr.s2k-darkrow { background-color: #e8e7e3; }" +
            "    p { margin-bottom: 1em; }" +
            "  </style>" +
            "</head>" +
            "<bo" + "dy>" +
            "  " + this.$kindleContent.html() +
            " </bo" + "dy>" +
            "</ht" + "ml>";
    },

    /**
     * Create JSON-formatted document.
     *
     * @param {boolean} kindleFormat Flag for conversion into Kindle format.
     * @return {Object} JSON-formatted content.
     */
    asStorageJson: function () {
        // Load content in correct format.
        var kindleContent = null;
        if (this.contentNode) {
            // @preserve Generate a HTML document for sending.
            kindleContent = this.getKindleContent();
        }

        // Set empty content to null.
        if ($.trim(kindleContent) === "<div></div>") {
            kindleContent = null;
        }

        // Return JSON transfer object.
        return {
            "title": this.title,
            "author": this.author,
            "url": this.url,
            "source": this.source,
            "publicationDate": this.publicationDate,
            "kindleContent": kindleContent,
        };
    }
};