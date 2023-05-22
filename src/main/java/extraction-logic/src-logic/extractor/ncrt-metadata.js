/**
 * ncrt-metadata.js
 * @author: Bernhard Wolkerstorfer
 *
 * Description: Formatter for Send-to-Kindle documents.
 *
 * Copyright (c) 2012 Amazon.com, Inc. All rights reserved.
 */

/**
 * @constructor
 */
$SendToKindle.NcrtExtractor.Metadata = function (relaxedMode) {
    // ######## Privileged Members ########
    this.relaxedMode = relaxedMode || false;
    this.extractorUtils = new $SendToKindle.ExtractorUtils();
};

$SendToKindle.NcrtExtractor.Metadata.prototype = {
    // ######## Constants ########
    /**
     * Title Meta Tags
     */
    META_TITLE: "meta[itemprop='headline'],meta[name='title'],meta[property='og:title'],meta[name='og:title']",

    /**
     * Author Meta Tags
     */
    META_AUTHOR: "meta[name='author'],meta[property='og:author'],meta[name='og:author']",

    /**
     * Date Meta Tags
     */
    META_DATE: "meta[name='pubdate'],meta[name='DATE'],meta[name='dat']",

    /**
     * Whitespace Test
     */
    WHITESPACE: /^\s*$/i,


    // ######## Methods ########
    /**
     * Analyze a possible title candidate.
     * @param candidate         Candidate
     * @param documentTitle     Title Node
     * @param metadata          Metadata
     */
    analyzeTitle: function (candidate, documentTitle, metadata) {
        // Test, if title is above the content node or inside the content node.
        var contentOffset = metadata.baseMetrics.node.offset();
        var contentHeight = metadata.baseMetrics.node.height();
        var contentWidth = metadata.baseMetrics.node.width();
        var candidateOffset = candidate.node.offset();

        if (candidate.node.width() > 0 && candidate.node.height() > 0 &&
            (candidateOffset.top < (contentOffset.top + contentHeight)) &&
            (candidateOffset.left >= contentOffset.left) &&
            (candidateOffset.left <= contentOffset.left + contentWidth)) {

            // Test, if candidate is in the content block.
            var isInBlock = $.contains(metadata.baseMetrics.node[0], candidate.node[0]);

            // Test, if candidate and content block share the same parent.
            var isInParent = $.contains(metadata.baseMetrics.node.parent()[0], candidate.node[0]);

            // Test, if candidate and content block share the skip level parent.
            var isInSkipParent = $.contains(metadata.baseMetrics.node.parent().parent()[0], candidate.node[0]);

            // Test, if candidate and document title share the same text.
            var isSimilar = candidate.node.text() !== "" && documentTitle.substring(0, candidate.node.text().length) === candidate.node.text();

            // Parse the header importance.
            var header = 6 - parseInt(candidate.tag_name[1], 10);

            // Generate a score for the title node.
            var score = ((isSimilar ? 5 : 0) + (isInBlock ? 5 : 0) + (isInParent ? 3 : 0) + (isInSkipParent ? 2 : 0) + header) / 20;

            // Compare with current highest scoring item.
            if (score > 0 && metadata.titleScore < score || metadata.title === null) {
                metadata.title = candidate.node.contents().filter(this.filterInvisible).text();
                metadata.subtitle = $("<div class='s2k-subtitle'></div>");

                // Test for whitespace title.
                if (metadata.title.match(this.WHITESPACE) === null) {
                    if (isInBlock === false && metadata.baseMetrics.node.parent()[0] !== candidate.node.parent()[0]) {
                        try {
                            // Get the visual position of the candidate.
                            var contentBottom = contentOffset.top + contentHeight;

                            // Find all headers in the parent without the current header.
                            var subtitle = candidate.node.parent().find(":header").filter(function () {
                                var metrics = $(this).data("s2k");
                                var offset = $(this).offset();

                                return (this !== candidate.node[0] && metrics !== undefined &&
                                metrics.is_metadata_node === false && metrics.is_inside_metadata === false &&
                                offset.top > candidateOffset.top && offset.top <= contentBottom);
                            });

                            // Take all visible content and append it as a subtitle.
                            metadata.subtitle.append($("<p>").text(subtitle.contents().filter(this.filterInvisible).text()));

                            // Test, if the title is followed by an introduction paragraph.
                            var follower = candidate.node.next();
                            var followerData = follower.data("s2k");
                            if (follower.is(":header") === false && followerData !== undefined && ($.contains(follower[0], metadata.baseMetrics.node[0]) === false) &&
                                ((followerData.is_metadata_node === false && followerData.subtree_ratio_length_plain_text > 0.8 && followerData.subtree_lines >= 1) ||
                                followerData.subtree_large_images > 0)) {

                                // Load intro.
                                metadata.subtitle.append(follower.contents().filter(this.filterInvisible).clone(true));
                            }
                        }
                        catch (e) {
                            metadata.subtitle = null;
                        }
                    }
                    else if (candidate.node.next().is(":header")) {
                        // Look at next node for a sub-title.
                        metadata.subtitle.append($("<p>").text(candidate.node.next().contents().filter(this.filterInvisible).text()));
                    }
                    else {
                        metadata.subtitle = null;
                    }

                    metadata.titleScore = score;
                }
                else {
                    metadata.title = null;
                }
            }
        }
    },

    /**
     * Analyze a possible author candidate.
     * @param candidate         Candidate
     * @param metadata          Metadata
     */
    analyzeAuthor: function (candidate, metadata) {
        // Test, if candidate is in the content block.
        var isInBlock = $.contains(metadata.baseMetrics.node[0], candidate.node[0]);

        // Test, if candidate and content block share the same parent.
        var isInParent = $.contains(metadata.baseMetrics.node.parent()[0], candidate.node[0]);

        // Test, if candidate and content block share the skip level parent.
        var isInSkipParent = $.contains(metadata.baseMetrics.node.parent().parent()[0], candidate.node[0]);

        // Load text and replace new lines.
        var author = candidate.node.text().replace(/\n/gi, " ").replace(/[|]/gi, ",");

        // Match the author part after
        var authorMatch = author.match(/(by|from|von):?\s*(.*)/i);
        author = (authorMatch !== null && authorMatch[2] !== undefined) ? authorMatch[2] : author;

        // Normalize the author field.
        authorMatch = author.match(/(.*)(\s+-|\s+posted|\s+published|\s+updated|\s+on|\s*,)/i);
        while (authorMatch !== null) {
            if (authorMatch[1] !== undefined) {
                author = authorMatch[1];
            }
            authorMatch = author.match(/(.*)(\s+-|\s+posted|\s+published|\s+updated|\s+on|\s*,)/i);
        }

        // If an author was identified score it.
        if (author !== undefined && author !== null && author !== "" && author.length < 50) {
            // Score the author field.
            var score = ((isInBlock ? 5 : 0) + (isInParent ? 3 : 0) + (isInSkipParent ? 2 : 0)) / 10;

            // Set the author and score, if better than current author.
            if (metadata.authorScore < score || metadata.author === null) {
                metadata.authorScore = score;
                metadata.author = $.trim(author);
            }
        }
    },

    /**
     * Analyze a candidate for the publication date.
     * @param candidate     Candidate
     * @param metadata      Metadata
     */
    analyzePublicationDate: function (candidate, metadata) {
        // Test, if candidate is in the content block.
        var isInBlock = $.contains(metadata.baseMetrics.node[0], candidate.node[0]);

        // Test, if candidate and content block share the same parent.
        var isInParent = $.contains(metadata.baseMetrics.node.parent()[0], candidate.node[0]);

        // Test, if candidate and content block share the skip level parent.
        var isInSkipParent = $.contains(metadata.baseMetrics.node.parent().parent()[0], candidate.node[0]);

        // Try loading the date.
        var date = this.parseDateFromString(undefined, candidate.node.text().replace(/(posted|published|updated|on)(:)?\s*/i, ""));

        // If a date was parsed, score it.
        if (date !== null && date !== undefined) {
            // Score the author field.
            var score = ((isInBlock ? 5 : 0) + (isInParent ? 3 : 0) + (isInSkipParent ? 2 : 0)) / 10;

            // Set the author and score, if better than current author.
            if (metadata.publicationDateScore < score || metadata.publicationDate === null) {
                metadata.publicationDateScore = score;
                metadata.publicationDate = date;
            }
        }
    },

    /**
     * Analyze metatags for a given selector.
     * @param $d        HTML Document
     * @param selector  Selector
     * @param parser    Parser Logic
     */
    analyzeMetaTags: function ($d, selector, parser) {
        // Load tags for selector.
        var metaTag = $d.find(selector);
        var content = (metaTag.length > 0 ? metaTag.first().attr("content") : null);

        // Parse content, if necessary.
        if (content !== null && parser !== undefined) {
            return parser($d, content);
        }

        return content;
    },

    /**
     * Try parsing a date from a permanent URL.
     * @param url   URL
     */
    parseDateFromUrl: function (url) {
        var date = url.match(/\d{4}\/\d{1,2}\/\d{1,2}/i);
        return (date !== null) ? this.parseDateFromString(undefined, date[0]) : null;
    },

    /**
     * Parse a date from a string.
     * @param Document      Document
     * @param dateString    Date String
     */
    parseDateFromString: function ($d, dateString) {
        var date = Date.parse(dateString);
        return ((date && date.getFullYear() <= Date.today().getFullYear()) ? date.toString("MMMM dd, yyyy") : null);
    },

    /**
     * Fiter invisible elements from the selection.
     * <pre>this</pre> is the current element.
     */
    filterInvisible: function () {
        var metrics = $(this).data("s2k");
        if (metrics !== undefined) {
            return metrics.is_visible_node;
        }
    },

    /**
     * Fetch metadata from HTML document.
     * @param result        Extraction Result
     * @param metadata      Metadata identified by the extractor.
     * @param baseMetrics   Metrics of the result.
     */
    fetchMetadata: function (result, metadata, baseMetrics) {
        var $d = $($SendToKindle.getState().stateDocument);

        // Fetch domain name with TLD, but without any sub-domains.
        var source = $d[0].location.hostname.match(/.*\.(.*\.\w{3})$/);
        result.source = ((source === null || source.length < 2) ? $d[0].location.hostname : source[1]);

        // Parse document metadata from source, if result is not
        // created using the relaxed mode for NCRT or VLB.
        if (!this.relaxedMode) {
            var self = this;

            // Create metadata holder.
            var documentMetadata = {
                "baseMetrics": baseMetrics,
                "title": null,
                "subtitle": null,
                "titleScore": 0,
                "author": null,
                "authorScore": 0,
                "publicationDate": null,
                "publicationDateScore": 0
            };

            // Analyze all metadata for the document.
            $.each(metadata, function () {
                if (this.is_title_node === true) {
                    self.analyzeTitle(this, $d[0].title, documentMetadata);
                }
                else if (this.is_author_node === true) {
                    self.analyzeAuthor(this, documentMetadata);
                }
                if (this.is_date_node === true) {
                    self.analyzePublicationDate(this, documentMetadata);
                }
            });

            // Fallback for title.
            documentMetadata.title = documentMetadata.title ||
                this.analyzeMetaTags($d, this.META_TITLE, undefined) ||
                $d[0].title;

            // Fallback for author.
            documentMetadata.author = documentMetadata.author ||
                this.analyzeMetaTags($d, this.META_AUTHOR, undefined);

            // Fallback for publication date.
            documentMetadata.publicationDate = documentMetadata.publicationDate ||
                this.analyzeMetaTags($d, this.META_DATE, this.parseDateFromString) ||
                this.parseDateFromUrl($d[0].location.href);

            // Set document metadata in extraction result.
            result.author = documentMetadata.author || null;
            result.publicationDate = documentMetadata.publicationDate || null;
            result.title = documentMetadata.title ? $.trim(documentMetadata.title.replace(/\s+/g, " ")) : source;

            // Handle a possible subtitle.
            if (documentMetadata.subtitle !== null && documentMetadata.subtitle.children().length > 0) {
                result.contentNode.find(".s2k-page").first().prepend(documentMetadata.subtitle);
            }
        }
        else {
            // Set document metadata.
            result.title = $d[0].title || result.source;
            result.publicationDate = (new Date()).toString("MMMM dd, yyyy");
        }
    }
};