/**
 * ncrt-extraction.js
 *
 * @author: Bernhard Wolkerstorfer
 *
 * Description: Extractor based on the NCR-Traversal-Algorithm.
 *
 * Copyright (c) 2012 Amazon.com, Inc. All rights reserved.
 */


/**
 * @constructor
 */
$SendToKindle.NcrtExtractor = function () {
    // ######## Members ########
    /**
     * Candidate for NCRT and VLB.
     */
    this.candidates = [];

    /**
     * Candidates for NCRT.
     */
    this.ncrt = [];

    /**
     * Candidates for VLB.
     */
    this.vlb = [];

    /**
     * Metadata Candidates
     */
    this.metadata = [];

    /**
     * Candidates for pagination areas.
     */
    this.pagination = [];

    /**
     * Traversal index.
     */
    this.traversal_index = 0;

    /**
     * Page index.
     */
    this.page_index = 0;

    /**
     * Area in square pixels of document.
     */
    this.document_base_area = 0;

    /**
     * Width of the document in pixels.
     */
    this.document_base_width = 0;

    /**
     * Flag for relaxed formatting.
     */
    this.relaxedMode = false;

    /**
     * Flag for feed formatting.
     */
    this.feedMode = false;

    /**
     * Flag for a low quality result.
     */
    this.lowQualityResult = false;

    /**
     * Extractor Utils
     */
    this.extractorUtils = new $SendToKindle.ExtractorUtils();

    /**
     * Service Instance
     */
    this.service = new $SendToKindle.Service();
};

$SendToKindle.NcrtExtractor.prototype = {
    // ######## Constants ########
    /**
     * Supported data types for plug-ins.
     */
    SUPPORTED_DATA_TYPES: /^(application\/(pdf|msword|rtf)|text\/plain)$/i,

    /**
     * Tag to be remove while copying the content.
     */
    REMOVABLE_TAGS: "script,noscript,object,embed,iframe,frame,frameset,noframes,aside,menu,header,footer,source,audio,meta," +
                    "video,form,ins,del,style,form,cite,button,textarea,input,select,.social-media-container,.article-resources",

    /**
     * Exclusions from multi-page extraction for
     * technical reason such as JavaScript content fetchers.
     */
    MULTIPAGE_EXCLUDED: /google\.*/i,

    /**
     * The maximum number of pages to be extracted.
     */
    MAX_PAGE_INDEX: 10,

    // ######## Methods #########
    /**
     * Analyze node visibility.
     * @param $node Node
     * @returns Visibility
     */
    isVisible: function ($node) {
        // Load node type.
        var nt = $node[0].nodeType;

        if (nt === 1) {
            // Load node location.
            var offset = $node.offset();

            // Extract text-indent.
            var textIndent = $node.css("text-indent");
            textIndent = textIndent ? textIndent.match(/\-\d+/i) : null;
            textIndent = (textIndent ? parseInt(textIndent[0], 10) : 0);

            // Extract the padding.
            var paddingLeft = parseInt($node.css("padding-left").replace("px"), 10);
            var paddingTop = parseInt($node.css("padding-top").replace("px"), 10);

            return (nt === 3) ||                                                    // Text
                   (nt === 1 &&                                                     // Element
                   ((offset.left + paddingLeft + $node.width()) >= 0 &&             // Node is in visible area of the document.
                    (offset.top  + paddingTop + $node.height()) >= 0) &&
                   //($node.width() > 0 || $node.height() > 0) &&                   // Node uses space
                   (textIndent >= 0) &&                                             // Node content visible.
                   ($node.css("display") !== "none") &&                             // Node is visible
                   ($node.css("visibility") !== "hidden"));                         // Node is visible
        }

        return (nt === 3);
    },

    /**
     * Analyze metrics as candidate for NCRT.
     * @param metrics          Metrics
     */
    analyzeNcrtCandidate: function (metrics) {
        // Check for possible pagination, sanity test conducted later.
        if (metrics.is_pagination === true) {
            this.pagination.push(metrics);
        }

        // Score with NCRT.
        if (metrics.is_metadata_node === true) {
            this.metadata.push(metrics);
            return false;
        }
        else {
            // Apply candidate heuristics.
            metrics.is_ncrt_candidate = (metrics.is_inline_node === false &&
                                         metrics.is_link === false &&
                                         metrics.is_inside_link === false &&
                                         metrics.is_inside_metadata === false &&
                                         metrics.is_valid_container === true &&
                                         metrics.subtree_link_text_density <= 0.7 &&
                                         metrics.subtree_plain_texts > 0 &&
                                         metrics.node_id !== "s2k-status-message" &&
                                         metrics.node_id.match(/sidebar|inline/i) === null &&
                                         metrics.node_style_class.match(/sidebar|inline/i) === null &&
                                         metrics.tag_name.match(/span/i) === null &&
                                         (metrics.subtree_inline_nodes > 0 || metrics.subtree_large_paragraphs > 1));

            return metrics.is_ncrt_candidate;
        }
    },

    /**
     * Analyze metrics as candidate for VLB.
     * @param metrics       Metrics
     */
    analyzeVlbCandidate: function (metrics) {
        // Calculate node area ratio.
        metrics.ratio_node_area = (metrics.node_area / this.document_base_area);
        metrics.ratio_node_width = metrics.node_width / this.document_base_width;

        // Apply candidate conditions and heuristics.
        metrics.is_vlb_candidate = (metrics.is_metadata_node === false &&
                                    metrics.is_inline_node === false &&
                                    metrics.is_link === false &&
                                    metrics.is_inside_link === false &&
                                    metrics.is_valid_container === true &&
                                    metrics.tag_name.match(/span/i) === null &&
                                    metrics.ratio_node_width >= 0.2 &&
                                    metrics.ratio_node_area > 0.05 &&
                                    metrics.ratio_node_area < 0.8);

        return metrics.is_vlb_candidate;
    },

    /**
     * Analyze a node as candidate for registered algorithms.
     * @param metrics   Metrics
     */
    analyzeCandidate: function (metrics) {
        return (this.analyzeNcrtCandidate(metrics) || this.analyzeVlbCandidate(metrics));
    },

    /**
     * Analyze the page for a pagination.
     * @return Pagination Flag
     */
    analyzeMultiPageArticle: function () {
        var self = this;
        var pages = [];
        var location = $SendToKindle.getState().stateDocument.location;

        // Analyze every item identified as pagination.
        $.each(this.pagination, function () {
            // Find all possible page links.
            var pageLinks = this.node.find("a");

            // Analyze, if links are available.
            if (pageLinks.length > 0 && pages.length === 0) {
                // Load the next page link.
                var link = pageLinks[0].href;

                // Take next link, if there are more as the first page is currently displayed.
                var linkOverride = ((link === undefined || link === null || link === "") && pageLinks.length > 1);

                // Test for a link page instead of permlinks.
                var linkPage = (pageLinks.length > 1 && linkOverride === false && pageLinks[1].href.match(/_\d+\.\w+$/i) !== null);

                // Update the page link.
                if (linkOverride === true || linkPage === true) {
                    link = pageLinks[1].href;
                }

                // Sanity test for link on existence, link to the same page and text contains a number.
                if (link === undefined || $(pageLinks[0]).text().match(/\d/i) === null) {
                    this.is_pagination = false;
                    return;
                }

                // Try to build a page link template for the pages.
                var pageLink = null;
                if (pageLinks.length > 1 && linkOverride === false && linkPage === false) {
                    pageLink = self.extractorUtils.analyzePageLinks(link, pageLinks[1].href);
                }
                else if (pageLinks.length > 2 && linkOverride === true && linkPage === false) {
                    pageLink = self.extractorUtils.analyzePageLinks(link, pageLinks[2].href);
                }
                else {
                    pageLink = self.extractorUtils.analyzePageLinks(link);
                }

                // Sanity test for page link.
                if (pageLink === null) {
                    this.is_pagination = false;
                    return;
                }

                // Fetch name of page parameter and perform a sanity test.
                var pageParam = pageLink.match(/([\?&]\w+=)_PN_|([\?&]\w+=\d+,)_PN_|(\/_PN_\/)|(\/_PN_)|(__PN_)/i);
                if (pageParam === null) {
                    this.is_pagination = false;
                    return;
                }
                pageParam = pageParam[1] || pageParam[2] || pageParam[3] || pageParam[4] || pageParam[5];

                // Sanity test all page links and push all passing to the
                // page link array for extraction.
                for (var i = 0, len = Math.min(pageLinks.length, self.MAX_PAGE_INDEX); i < len; i++) {
                    if (self.extractorUtils.testPageLink(pageLinks[i].href, pageLink, pageParam) === true &&
                            pages.indexOf(pageLinks[i].href) === -1) {

                        pages.push(pageLinks[i].href);
                    }
                }
            }
            else {
                this.is_pagination = false;
            }
        });

        return (pages.length > 0 ? pages : null);
    },

    /**
     * Traverse the DOM and generate node metrics and candidates.
     *
     * @param {Object}  $node         Node
     * @param {number}  index         Index of node
     * @param {number}  depth         DOM depth
     * @param {number}  layerIndex    Index of node in current layer.
     * @param {boolean} isLink        Node is a link.
     * @param {boolean} isGlance      Glance Traversal (needed for multipage extraction)
     * @param {function(result)}      callback      Callback
     */
    traverse: function ($node, index, depth, layerIndex, isLink, isMetadata, isGlance, callback) {
        // Abort recursion, if the node is not valid.
        if (undefined === $node || null === $node || $node.length === 0) {
            callback(null);
        }

        // Get visibility flag.
        var isVisible = this.isVisible($node) || isGlance;

        // Load metrics for node.
        var metrics = new $SendToKindle.NodeMetrics($node, index, depth, layerIndex, isVisible, isLink, isMetadata);

        // Analyze node and decide on further traversal.
        if (metrics.is_visible_node && !metrics.is_skip_node && !metrics.is_comment_node && !metrics.is_ad_node) {
            if (metrics.count_child_nodes === 0) {
                this.onTraverseComplete(metrics, undefined, isGlance, callback);
            }
            else {
                // Traverse child nodes.
                for (var i = 0; i < ($node[0].childNodes.length); ++i) {
                    this.traverse(
                        $($node[0].childNodes[i]),
                        ++(this.traversal_index), depth + 1, i,
                        metrics.is_link || metrics.is_inside_link,
                        metrics.is_metadata_node || metrics.is_inside_metadata,
                        isGlance,
                        function (childMetrics) {
                            this.onTraverseComplete(metrics, childMetrics, isGlance, callback);
                        }.bind(this));
                }
            }
        }
        else {
            $node.data("s2k", metrics);
            callback(null);
        }
    },

    /**
     * Handler for merging metrics up the tree.
     * @param {$SendToKindle.NodeMetrics} metrics               Node Metrics
     * @param {$SendToKindle.NodeMetrics} childMetrics          Child Metrics
     * @param {boolean} isGlance                                Glance Traversal (needed for multipage extraction)
     * @param {function($SendToKindle.NodeMetrics)} callback     Callback
     */
    onTraverseComplete: function (metrics, childMetrics, isGlance, callback) {
        // Merge the child metrics.
        if (childMetrics !== undefined) {
            metrics.mergeChild(childMetrics);
        }

        // If all children have been process, process the node.
        if (metrics.count_child_nodes === 0) {
            // Prepare the node metrics for analysis.
            metrics.prepare();

            // Avoid too large metadata nodes.
            if (metrics.is_metadata_node && metrics.subtree_valid_containers > 3) {
                metrics.is_metadata_node = metrics.is_date_node = metrics.is_author_node = metrics.is_title_node = false;
            }

            // Analyze candidate quality.
            if (this.analyzeCandidate(metrics) && !isGlance) {
                this.candidates.push(metrics);
            }

            // Push the metrics to the parent.
            callback(metrics);
        }
    },

    /**
     * Build a result from the NCRT candidates.
     * @param progress  Progress Callback
     */
    buildNcrtResult: function (progress) {
        // Local instance of the extractor instance.
        var self = this;

        // Select the content node.
        var $content = $("<div id='s2k-result' class='s2k-result-content'></div>");

        if (this.ncrt[0].index !== 0) {
            var resultNodes = [];

            // Load the result node.
            var ncrtNode = this.ncrt[0];

            // Load all styles set on the node.
            var styles = (ncrtNode.node.attr("class") || "").split(" ").join(",.").replace(/^\.,|,\.,|[\.,]+$|^,/gi, "");
            styles = (styles !== "" ? "." : "") + styles;

            // Fetch nodes on the same layer that have the same style class.
            ncrtNode.node.parent().children().each(function () {
                var metrics = $(this).data("s2k");

                // Check, if the node is an NCRT candidate or the style class matches.
                if (metrics !== undefined && ((metrics.is_ncrt_candidate && metrics.ncrt_score < 0.7) ||
                    (metrics.node.is(styles) === true && metrics.node_style_class !== "#unknown_class") ||
                    ((metrics.subtree_large_images > 0 || metrics.subtree_medium_images > 0) && metrics.depth > 2))) {
                    resultNodes.push(metrics);
                }
            });

            // Sort the candidate by occurance in layer.
            resultNodes.sort(function (c1, c2) {
                return c1.layer_index - c2.layer_index;
            });

            // Create a result for the first page.
            this.buildContentPage($content, resultNodes, (resultNodes.length > 1 ? this.ncrt[0].node.parent().data("s2k") : this.ncrt[0]));

            // Hand control to the multi-page extraction logic.
            this.extractMultiPageArticle($content, this.ncrt[0], progress);
        }
        else {
            // Clone the body content and remove unwanted nodes.
            var removableTagsRegEx = new RegExp("^(" + this.REMOVABLE_TAGS.replace(",", "|") + ")$", "i");
            var rs = this.ncrt[0].node.contents().filter(function () {
                return (this.nodeName.match(removableTagsRegEx) === null);
            }).clone(true);
            rs.find(this.REMOVABLE_TAGS).remove();

            // Create a content page for the result.
            $content.append('<div id="s2k-page-1" class="s2k-page"></div>');
            $content.find(".s2k-page").append(rs).data("s2k", this.ncrt[0]);

            // Prepare the pDocs Document Model.
            this.prepareDocumentModel($content, this.ncrt[0], progress);
        }
    },

    /**
     * Build a result with the VLB block.
     * @param progress      Progress Callback
     */
    buildVlbResult: function (progress) {
        // Create result container.
        var $content = $("<div id='s2k-result' class='s2k-result-content'></div>");

        // Build a page for the content.
        this.buildContentPage($content, [this.vlb[0]], this.vlb[0]);

        // Hand control to the multi-page extraction logic.
        this.extractMultiPageArticle($content, this.vlb[0], progress);
    },

    /**
     * Build a result with the "Above the Fold Priority" logic.
     * @param $baseNode     Extraction Base Node
     * @param progress      Progress Callback
     */
    buildAtfpResult: function ($baseNode, progress) {
        // Create result container.
        var $content = $("<div id='s2k-result' class='s2k-result-content'></div>");

        // Clone the body content and remove unwanted nodes.
        var removableTagsRegEx = new RegExp("^(" + this.REMOVABLE_TAGS.replace(",", "|") + ")$", "i");
        var rs = $baseNode.contents().filter(function () {
            return (this.nodeName.match(removableTagsRegEx) === null);
        }).clone(true);
        rs.find(this.REMOVABLE_TAGS).remove();

        // Append the nodes to the result.
        $content.append(rs);

        // Prepare the pDocs Document Model.
        this.prepareDocumentModel($content, $baseNode.data("s2k"), progress);
    },

    /**
     * Build a result for a page.
     * @param $content      Content
     * @param resultNodes   Nodes for page.
     * @param pageMetrics   Metrics to append to the page.
     */
    buildContentPage: function ($content, resultNodes, pageMetrics) {
        // Create a page container.
        var pageId = "s2k-page-" + (++this.page_index);
        $content.append($('<div>', {"id": pageId, "class": "s2k-page"}));

        // Setup page container.
        var $page = $content.find("#" + pageId);
        $page.data("s2k", pageMetrics);

        // Build a continuous result from the result blocks.
        var self = this;
        $.each(resultNodes, function () {
            var $n = this.node;

            // Clone a possible header before the content block.
            if ($n.prev().is(":header")) {
                $page.append($n.prev().clone(true));
            }

            // Clone the node and remove unwanted tags.
            var rs = $n.clone(true);
            rs.find(self.REMOVABLE_TAGS).remove();

            // Append the nodes to the result.
            $page.append((rs.is("table") === true) ? rs : rs.contents());
        });
    },

    /**
     * Extraction logic for multi-page articles.
     * @param $content      Result Content
     * @param $baseMetrics  Metrics for initial page block.
     * @param progress      Progress Callback
     */
    extractMultiPageArticle: function ($content, baseMetrics, progress) {
        // Check for an article that spans over multiple pages and
        // kick off extraction for this article type.
        var pages = this.analyzeMultiPageArticle();

        if ($SendToKindle.getState().stateDocument.location.href.match(this.MULTIPAGE_EXCLUDED) === null && pages !== null) {
            // Analyze logic requirements.
            var path = baseMetrics.node_id;
            if (path === "#unknown_id") {
                path = this.extractorUtils.getPathForNode(baseMetrics.node);
            }
            else {
                path = "#" + path;
            }

            this.extractMultiPageContent(pages, 0, path, $content, baseMetrics, progress);
        }
        else {
            // Prepare the pDocs Document Model for single-page article.
            this.prepareDocumentModel($content, baseMetrics, progress);
        }
    },

    /**
     * Extract content from a page.
     *
     * @param pages         URLs for all pages.
     * @param index         Page Index
     * @param path          Path to Content Node
     * @param $content      Extraction Result
     * @param metrics       Metrics for NCRT result.
     * @param progress      Progress Callback
     */
    extractMultiPageContent: function (pages, index, path, $content, metrics, progress) {
        if (index >= 0 && index < pages.length) {
            var self = this;

            // Fetch the requested page.
            this.service.ajax({
                url: self.extractorUtils.makeUrlAbsolute(pages[index]),
                type: "GET",
                dataType: "html",
                timeout: 5000,
                success: function (data) {
                    // Fetch body tag.
                    var bodyTag = data.match(/(.*<\s*body[^>]*>)/i);
                    var startIndex = data.indexOf(bodyTag[0]) + bodyTag[0].length;
                    var endIndex = data.lastIndexOf("</body>");

                    // Load content from result.
                    var $baseNode = $(data.substring(startIndex, endIndex));
                    $baseNode = this.extractorUtils.getNodeFromPath($baseNode, path);

                    // Page contained content node.
                    if ($baseNode !== undefined && $baseNode.length !== 0) {
                        // Load the number of base nodes.
                        var cntBaseNodes = $baseNode.length;

                        // Generate metrics for result node(s).
                        $baseNode.each(function () {
                            self.traverse($(this), 1, metrics.depth, 0, false, false, true, function () {
                                if ((--cntBaseNodes) === 0) {
                                    // Create metrics for the page.
                                    var pageMetrics = new $SendToKindle.NodeMetrics($("<div></div>"), 0, metrics.depth, 0, true, false, false);

                                    // Create a page result for the nodes.
                                    var nodeMetrics = $baseNode.map(function () {
                                        pageMetrics.mergeChild($(this).data("s2k"));
                                        return $(this).data("s2k");
                                    });

                                    pageMetrics.prepare();

                                    // Build a page for the extraction result.
                                    self.buildContentPage($content, nodeMetrics, pageMetrics);

                                    // Fetch the next page.
                                    self.extractMultiPageContent(pages, index + 1, path, $content, metrics, progress);
                                }
                            });
                        });
                    }
                    else {
                        // Invoke the extraction one more time to trigger the document generator.
                        this.extractMultiPageContent(pages, -1, path, $content, metrics, progress);
                    }

                }.bind(this),
                error: function (xhr, error, errorThrown) {
                    // Emit error metrics for tracking.
                    $SendToKindle.getState().metrics.count($SendToKindle.getState().metrics.NAMES.c_invalid_page);

                    // Invoke the extraction one more time to trigger the document generator.
                    this.extractMultiPageContent(pages, -1, path, $content, metrics, progress);
                }.bind(this)
            });
        }
        else {
            // Prepare the pDocs Document Model.
            this.prepareDocumentModel($content, metrics, progress);
        }
    },

    /**
     * Extract content from DOM.
     * @param $baseNode     Base Node
     * @param progress      Progress Callback
     */
    extractFromDOM: function ($baseNode, progress) {
        // Calculate document area.
        var state = $SendToKindle.getState();
        this.document_base_area = $(state.stateDocument).width() * $(state.stateDocument).height();
        this.document_base_width = $(state.stateDocument).width();

        // Traverse and analyze the DOM.
        this.traverse($baseNode, 0, 0, 0, false, false, false, function () {
            // Score NCRT candidates.
            var self = this;
            var globalMetrics = $baseNode.data("s2k");
            $.each(this.candidates, function () {
                this.score(globalMetrics);

                // NCRT expectes a maximum of 60% noise.
                if (this.is_ncrt_candidate === true && this.ncrt_score < 0.6) {
                    self.ncrt.push(this);
                }

                // VLB expects at least 10% noise but less than 70%.
                if (this.is_vlb_candidate === true && (this.vlb_score > 0.1 || this.subtree_large_images > 0) && this.vlb_score < 0.7) {
                    self.vlb.push(this);
                }
            });

            // Sort NCRT candidates by NCR.
            this.ncrt.sort(function (c1, c2) {
                return c1.ncrt_score - c2.ncrt_score;
            });

            // Sort VLB candidates by VLB score.
            this.vlb.sort(function (c1, c2) {
                return c1.vlb_score - c2.vlb_score;
            });

            // Build the result HTML document based on the extraction results.
            if (this.ncrt.length > 0) {

                // Turn on feed node formatting for nodes with high metadata nodes.
                this.feedMode = this.ncrt[0].subtree_metadata_nodes > 5;

                // Turn on relaxed formatting for high link text density on NCRT results.
                this.relaxedMode = this.ncrt[0].subtree_link_text_density > 0.45;

                // NCRT quality confidence is high.
                this.buildNcrtResult(progress);
            }
            else if (this.vlb.length > 0) {

                // Set low quality flag for VLB results with score over 0.45.
                this.lowQualityResult = (this.vlb[0].vlb_score > 0.45);

                // Turn on feed node formatting for nodes with high metadata nodes.
                this.feedMode = this.vlb[0].subtree_metadata_nodes > 3 && this.vlb[0].subtree_link_text_density < 1;

                // Enable relaxed mode for formatting / metadata.
                this.relaxedMode = !this.feedMode;

                // VLB quality confidence is high.
                this.buildVlbResult(progress);
            }
            else {

                // Enable relaxed mode for formatting / metadata.
                this.relaxedMode = true;

                // Mark result as low quality result.
                this.lowQualityResult = true;

                // Content does not have low NCR block or a VLB (worst case).
                this.buildAtfpResult($baseNode, progress);
            }
        }.bind(this));
    },

    /**
     * Prepare the pDocs Document Model.
     * @param $content      Content
     * @param baseMetrics   Metrics of content node.
     * @param progress      Progress Callback
     */
    prepareDocumentModel: function ($content, baseMetrics, progress) {
        if ($content !== null) {
            // Create a pDocs Document Model
            var result = new $SendToKindle.Document($content);

            // Extract metadata.
            (new $SendToKindle.NcrtExtractor.Metadata(this.relaxedMode || this.feedMode)).fetchMetadata(result, this.metadata, baseMetrics);

            // Format the candidates.
            (new $SendToKindle.NcrtExtractor.Formatter(this, this.relaxedMode, this.feedMode)).format($content);

            // Store extraction location.
            result.url =  window.location.href;

            // Store extraction metadata.
            result.metadata = "";
            $.each(this.candidates, function () {
                result.metadata += this.asRecord() + "\n";
            });

            this.extractorUtils.embedImages(result.contentNode.find("img"), 0, function () {
                progress({"value": 1.0, "success": true, "data": result, "lowQuality": this.lowQualityResult});
            }.bind(this));
        }
        else {
            progress({"error": true, "message": "NO_CONTENT"});
        }
    },

    /**
     * Extract content from the DOM for the current
     * active document (S2K State Model).
     *
     * @param progress  Progress Callback
     * @return Extraction Result
     */
    extract: function (progress) {
        try {
            var state = $SendToKindle.getState();
            var $baseNode = $(state.stateDocument.body);

            // Run analyzing algorithms.
            this.extractFromDOM($baseNode, progress);
        }
        catch (e) {
            progress({"error": true, "data": e});
        }
    }
};