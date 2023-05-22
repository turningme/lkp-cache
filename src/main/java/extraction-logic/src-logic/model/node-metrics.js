/**
 * node-metrics.js
 *
 * @author: Bernhard Wolkerstorfer
 *
 * Description: Metrics for a node.
 *
 * Copyright (c) 2012 Amazon.com, Inc. All rights reserved.
 */

/**
 * @constructor
 */
$SendToKindle.NodeMetrics = function ($node, index, depth, layerIndex, isVisible, isLink, isMetadata) {
    // ######## Members ########

    // -------- Index / Position --------
    this.index = index;
    this.depth = depth;
    this.layer_index = layerIndex;
    this.node_id = $node.attr("id") || "#unknown_id";
    this.node_style_class = $node.attr("class") || "#unknown_class";

    // -------- Node --------
    this.node = $node;
    this.tag_name = "#invalid";
    this.node_name = "#invalid";

    // -------- Visual Layout --------
    this.node_width = 0;
    this.node_height = 0;
    this.node_area = 0;

    // -------- Flags ---------
    this.is_valid_container = false;
    this.is_small_image = false;
    this.is_medium_image = false;
    this.is_large_image = false;
    this.is_skip_image = false;
    this.is_text = false;
    this.is_plain_text = false;
    this.is_link_text = false;
    this.is_link = false;
    this.is_inside_link = isLink;
    this.is_ad_link = false;
    this.is_ad_image = false;
    this.is_ad_node = false;
    this.is_named_node = false;
    this.is_inline_node = false;
    this.is_visible_node = isVisible;
    this.is_skip_node = false;
    this.is_comment_node = false;
    this.is_atf_node = false;
    this.is_btf_node = false;

    // -------- Metadata Flags ---------
    this.is_metadata_node = false;
    this.is_inside_metadata = isMetadata;
    this.is_author_node = false;
    this.is_title_node = false;
    this.is_date_node = false;

    // -------- Pagination --------
    this.is_pagination = false;

    // -------- Counters --------
    this.count_links = 0;
    this.count_texts = 0;
    this.count_plain_texts = 0;
    this.count_link_texts = 0;
    this.count_ad_nodes = 0;
    this.count_ad_links = 0;
    this.count_ad_images = 0;
    this.count_images = 0;
    this.count_small_images = 0;
    this.count_medium_images = 0;
    this.count_large_images = 0;
    this.count_skip_images = 0;
    this.count_lines = 0;
    this.count_words = 0;
    this.count_small_paragraphs = 0;
    this.count_large_paragraphs = 0;
    this.count_valid_containers = 0;
    this.count_inline_nodes = 0;
    this.count_metadata_nodes = 0;
    this.length_link_text =  0;
    this.length_plain_text = 0;
    this.length_text = 0;

    // --------- Statistics ---------
    this.link_density = 0;
    this.link_text_density = 0;
    this.ratio_length_plain_text = 0;
    this.ratio_length_link_text = 0;
    this.ratio_length_text = 0;
    this.ratio_node_width = 0;
    this.ratio_node_area = 0;
    this.ratio_node_atf = 0;
    this.ratio_node_btf = 0;

    // --------- Sub-Tree Counters --------
    this.subtree_links = 0;
    this.subtree_texts = 0;
    this.subtree_plain_texts = 0;
    this.subtree_link_texts = 0;
    this.subtree_ad_nodes = 0;
    this.subtree_ad_links = 0;
    this.subtree_ad_images = 0;
    this.subtree_images = 0;
    this.subtree_small_images = 0;
    this.subtree_medium_images = 0;
    this.subtree_large_images = 0;
    this.subtree_skip_images = 0;
    this.subtree_lines = 0;
    this.subtree_words = 0;
    this.subtree_small_paragraphs = 0;
    this.subtree_large_paragraphs = 0;
    this.subtree_valid_containers = 0;
    this.subtree_inline_nodes = 0;
    this.subtree_metadata_nodes = 0;
    this.subtree_length_text = 0;
    this.subtree_length_link_text =  0;
    this.subtree_length_plain_text = 0;

    // --------- Sub-Tree Statistics --------
    this.subtree_link_density = 0;
    this.subtree_link_text_density = 0;
    this.subtree_ratio_length_plain_text = 0;
    this.subtree_ratio_length_link_text = 0;

    // --------- Classification ---------
    this.is_ncrt_candidate = false;
    this.ncrt_content_index = 1;
    this.ncrt_noise_index = Number.MAX_VALUE;
    this.ncrt_score = Number.MAX_VALUE;

    this.is_vlb_candidate = false;
    this.vlb_content_index = 1;
    this.vlb_noise_index = Number.MAX_VALUE;
    this.vlb_score = Number.MAX_VALUE;

    // --------- Runtime Metrics ---------
    this.count_child_nodes = this.node[0].childNodes.length;

    // ######## Initialization ########
    this.analyzeNode();
};

$SendToKindle.NodeMetrics.prototype = {
    // ######## Constants ########
    SMALL_IMAGE_AREA: 5500,
    MEDIUM_IMAGE_AREA: 15000,
    LARGE_IMAGE_AREA: 50000,

    SMALL_IMAGE_WIDTH: 90,
    SMALL_IMAGE_HEIGHT: 90,
    MEDIUM_IMAGE_WIDTH: 150,
    MEDIUM_IMAGE_HEIGHT: 150,
    LARGE_IMAGE_WIDTH: 400,
    LARGE_IMAGE_HEIGHT: 100,

    DEFAULT_LINE_LENGTH: 80,            // 80 characters per line
    SMALL_PARAGRAPH_WORDS: 50,          // 50 words per paragraph
    LARGE_PARAGRAPH_WORDS: 80,          // 80 words per paragraph

    SKIP_ELEMENT: /^(button|input|select|textarea|optgroup|command|datalist|frame|frameset|iframe|noframes|style|script|noscript|canvas|applet|map|marquee|area|base|details|dir|object|embed|aside|menu|source|audio|video|del|ins|select)$/i,
    CONTAINER_TAGS: /^(body|article|section|div|td|li|dd|center|span|content|table)$/i,
    INLINE_TAGS: /^(h1|h2|h3|h4|h5|h6|b|strong|i|em|p|pre)$/i,

    AD_DOMAINS: /googlesyndication\.com|\.2mdn\.net|de17a\.com|content\.aimatch\.com|doubleclick\.net|adbrite\.com|adbureau\.net|admob\.com|bannersxchange\.com|buysellads\.com|impact\-ad\.jp|atdmt\.com|advertising\.com/i,
    AD_WRAPPER_ATTRS: ["id", "class"],
    AD_WRAPPER: /(^|\s|\w+_)(ad[s]?|advertisement|sponsored|strybtmmorebx|resaudio|articlead)(_\w+|\s|$)/i,
    AD_WRAPPER_EXCLUDE: /ads_backsplashSkin/i,

    NAME_ATTRS: ["id", "class", "ref", "prop", "itemprop", "property", "name"],
    NODE_NAMES: /posttext|post_text|postbody|post_body|entry|entry_body|body|storycontent|storybody|story_content|story_body|article\-body|articlebody|story|mainstory|CPLjOe|amabot_center/i,
    NODE_NAMES_EXCLUDE: /[\s\-_]?(date|time|timestamp|tmstmp|title|headline|header|author|byline)[\s\-_]?/i,

    COMMENT_NAMES: /disqus|comment|comments|dsqs|cmmnt|disqus_thread|gemAid|share/i,
    COMMENT_NAMES_EXCLUDE: /c_comment/i,

    META_DATE_NAMES: /[\s\-_]?(date|time|timestamp|datestamp|posted\-on|tmstmp)[\s\-_]?/i,
    META_TITLE_NAMES: /[\s\-_]?(title|headline|story-header|heading|hed)[\s\-_]?|(^name$)/i,
    META_AUTHOR_NAMES: /[\s\-_]?(author|byline|posted\-by|postFrom|cnnByline|writer)[\s\-_]?/i,
    META_EXCLUDE: /[\s\-_]?(hentry|entry-content|post-body|timeline|twitter|facebook|tw\w+|fb-status|mw-headline|hyper|ignore|captioned|expCaption|authorIdentification|caption|editable|nav|program|subheading|timestamp-\d+)[\s\-_]?/i,

    PAGE_NAME_ATTRS: ["id", "class"],
    PAGE_NAMES: /pag|^(nav|controls)$/i,
    PAGE_NAMES_EXCLUDE: /newpage|pagenum|\w+-item/i,

    // ######## Methods ########
    /**
     * Analyze tag name.
     */
    analyzeTagName: function () {
        var nodeType = this.node[0].nodeType;
        var nodeName = this.node[0].tagName;

        if (nodeType === 3) {
            this.tag_name = "#text";
        }
        else if (nodeType === 1 && nodeName && nodeName > '') {
            this.is_valid_container = (this.CONTAINER_TAGS.test(nodeName) && !this.is_inside_link);
            this.tag_name = nodeName.toLowerCase();

            // Set the full identifying name of the node.
            this.node_name = this.tag_name +
                (this.node_id !== "#unknown_id" ? "#" + this.node_id : "") +
                (this.node_style_class !== "#unknown_class" ? "." + $.trim(this.node_style_class).replace(/\s/g, ".") : "");
        }
    },

    /**
     * Analyze as text node.
     */
    analyzeTextNode: function () {
        var node = this.node[0];

        // Ignore non-text nodes and empty/whitespace nodes.
        if (node.nodeType !== 3 || node.nodeValue.length === 0 || (/^\s+$/.test(node.nodeValue))) {
            return;
        }

        var textLength = node.nodeValue.length;

        this.is_text = true;
        this.is_plain_text = !this.is_inside_link;
        this.is_link_text = this.is_inside_link;
        this.length_text = textLength;
        this.length_link_text = (this.is_inside_link ? textLength : 0);
        this.length_plain_text = (!this.is_inside_link ? textLength : 0);

        this.count_words = node.nodeValue.split(" ").length;
        this.count_lines = textLength / this.DEFAULT_LINE_LENGTH;
        this.count_small_paragraphs = this.count_words / this.SMALL_PARAGRAPH_WORDS;
        this.count_large_paragraphs = this.count_words / this.LARGE_PARAGRAPH_WORDS;
    },

    /**
     * Analyze node as link.
     */
    analyzeLinkNode: function () {
        // Load DOM node.
        var node = this.node[0];

        // Test the node for a valid link.
        this.is_link = (node.href && node.href !== "");

        // Test node
        if (this.is_link && this.AD_DOMAINS.test(node.href)) {
            this.is_ad_node = true;
            this.is_ad_link = true;
        }
    },

    /**
     * Analyze node as image.
     */
    analyzeImageNode: function () {
        var node = this.node[0];
        var width = this.node.width();
        var height = this.node.height();

        if ((width * height) > this.LARGE_IMAGE_AREA || ((width >= this.LARGE_IMAGE_WIDTH) && (height >= this.LARGE_IMAGE_HEIGHT))) {
            this.is_large_image = true;
        }
        else if ((width * height) > this.MEDIUM_IMAGE_AREA || ((width >= this.MEDIUM_IMAGE_WIDTH) && (height >= this.MEDIUM_IMAGE_HEIGHT))) {
            this.is_medium_image = true;
        }
        else if ((width * height) < this.SMALL_IMAGE_AREA || (width <= this.SMALL_IMAGE_WIDTH) && (height <= this.SMALL_IMAGE_HEIGHT)) {
            this.is_skip_image = true;
        }
        else {
            this.is_small_image = true;
        }

        if (node.src && this.AD_DOMAINS.test(node.src)) {
            this.is_ad_node = true;
            this.is_ad_image = true;
        }
    },

    /**
     * Update metrics with child metrics.
     * @param metrics   Child Metrics
     */
    mergeChild: function (metrics) {
        // Mark the child as processed.
        this.count_child_nodes -= 1;

        // Sanity check.
        if (null === metrics || undefined === metrics) {
            return;
        }

        // Merge direct node metrics, that are not part of a child container.
        this.count_texts             += (metrics.is_text ? 1 : 0);
        this.count_plain_texts       += (metrics.is_plain_text ? 1 : 0);
        this.count_link_texts        += (metrics.is_link_text ? 1 : 0);
        this.count_links             += (metrics.is_link ? 1 : 0);
        this.count_ad_images         += (metrics.is_ad_image ? 1 : 0);
        this.count_ad_links          += (metrics.is_ad_link ? 1 : 0);
        this.count_ad_nodes          += (metrics.is_ad_node ? 1 : 0);
        this.count_images            += ((metrics.is_small_image || metrics.is_medium_image || metrics.is_large_image) ? 1 : 0);
        this.count_small_images      += (metrics.is_small_image ? 1 : 0);
        this.count_medium_images     += (metrics.is_medium_image ? 1 : 0);
        this.count_large_images      += (metrics.is_large_image ? 1 : 0);
        this.count_skip_images       += (metrics.is_skip_image ? 1 : 0);
        this.count_inline_nodes      += (metrics.is_inline_node ? 1 : 0);
        this.count_valid_containers  += (metrics.is_valid_container ? 1 : 0);

        // Handle container metrics.
        if (false === metrics.is_valid_container) {
            this.length_text         += metrics.length_text;
            this.length_link_text    += metrics.length_link_text;
            this.length_plain_text   += metrics.length_plain_text;
        }

        // Metadata metrics.
        if (false === this.is_metadata_node && false === this.is_inside_metadata) {
            this.count_metadata_nodes    += (metrics.is_metadata_node ? 1 : 0);
            this.subtree_metadata_nodes  += metrics.subtree_metadata_nodes;
        }

        // Merge sub-tree metrics with node metrics for a global complexity analysis.
        this.subtree_links             += metrics.subtree_links;
        this.subtree_texts             += metrics.subtree_texts;
        this.subtree_ad_nodes          += metrics.subtree_ad_nodes;
        this.subtree_ad_images         += metrics.subtree_ad_images;
        this.subtree_ad_links          += metrics.subtree_ad_links;
        this.subtree_plain_texts       += metrics.subtree_plain_texts;
        this.subtree_link_texts        += metrics.subtree_link_texts;
        this.subtree_images            += metrics.subtree_images;
        this.subtree_small_images      += metrics.subtree_small_images;
        this.subtree_medium_images     += metrics.subtree_medium_images;
        this.subtree_large_images      += metrics.subtree_large_images;
        this.subtree_skip_images       += metrics.subtree_skip_images;
        this.subtree_lines             += metrics.subtree_lines;
        this.subtree_words             += metrics.subtree_words;
        this.subtree_small_paragraphs  += metrics.subtree_small_paragraphs;
        this.subtree_large_paragraphs  += metrics.subtree_large_paragraphs;
        this.subtree_inline_nodes      += metrics.subtree_inline_nodes;
        this.subtree_valid_containers  += metrics.subtree_valid_containers;
        this.subtree_length_text       += metrics.subtree_length_text;
        this.subtree_length_link_text  += metrics.subtree_length_link_text;
        this.subtree_length_plain_text += metrics.subtree_length_plain_text;
    },

    /**
     * Analyze a metadata nodes.
     */
    analyzeMetadataNode: function () {
        // Check exclude quotes from metadata.
        var parent = this.node.parent();
        if (parent.length > 0 && parent[0].nodeName.toLowerCase() !== "blockquote" && this.index !== 0) {
            // Check for author node.
            this.is_author_node =
                this.analyzeNodeNames(this.NAME_ATTRS, this.META_AUTHOR_NAMES, this.META_EXCLUDE) ||
                (this.tag_name === "cite" && this.analyzeNodeNames(this.NAME_ATTRS, null, this.META_EXCLUDE) && this.node.parent().is("figcaption") === false);

            // Check for time nodes.
            this.is_date_node =
                this.analyzeNodeNames(this.NAME_ATTRS, this.META_DATE_NAMES, this.META_EXCLUDE) ||
                (this.tag_name === "time" && this.analyzeNodeNames(this.NAME_ATTRS, null, this.META_EXCLUDE));

            // Check for title node, but only accept headers as valid titles.
            this.is_title_node =
                (this.node.is(":header") && this.node.parent().is("hgroup")) ||
                (this.analyzeNodeNames(this.NAME_ATTRS, this.META_TITLE_NAMES, this.META_EXCLUDE) && this.node.is(":header"));
        }
        return (this.is_title_node || this.is_author_node || this.is_date_node);
    },

    /**
     * Analyze a generic node.
     */
    analyzeGenericNode: function () {
        // Check for an inline tag node.
        if (this.INLINE_TAGS.test(this.tag_name)) {
            this.is_inline_node = true;
        }
        else {
            // Check for a named node.
            this.is_comment_node = this.analyzeNodeNames(this.NAME_ATTRS, this.COMMENT_NAMES, this.COMMENT_NAMES_EXCLUDE) && this.index !== 0;
            this.is_named_node = this.analyzeNodeNames(this.NAME_ATTRS, this.NODE_NAMES, this.NODE_NAMES_EXCLUDE);
        }

        // Check for metadata or pagination.
        if (!this.is_comment_node && !this.is_named_node && this.is_visible_node && /^article$/i.test(this.tag_name) === false) {
            this.is_pagination = this.analyzeNodeNames(this.PAGE_NAME_ATTRS, this.PAGE_NAMES, this.PAGE_NAMES_EXCLUDE);
            this.is_metadata_node = this.analyzeMetadataNode();
        }
    },

    /**
     * Check attributes for special node names.
     * @param attrs     Attributes
     * @param names     Names
     * @param exclude   Excluded names
     */
    analyzeNodeNames: function (attrs, names, exclude) {
        // Load node into local scope.
        var node = this.node;
        var isNamed = false;
        var isExclude = false;

        // Check all attributes.
        $.each(attrs, function () {
            if (!(isExclude || isNamed)) {
                var value = node.attr("" + this);
                isExclude = ((exclude && value) ? exclude.test(value) : false);

                // Handle include/exclude and exclude-only case.
                if (names !== null && value !== null && value !== undefined && names.test(value)) {
                    isNamed = true;
                }
            }
        });

        return (names !== null ? (isNamed && !isExclude) : !isExclude);
    },

    /**
     * Analyze the node for its basic features.
     */
    analyzeNode: function () {
        // Analyze the tag name.
        this.analyzeTagName();

        // Analyze the node's metrics.
        if (this.tag_name === "#invalid" || this.SKIP_ELEMENT.test(this.tag_name)) {
            this.is_skip_node = true;
        }
        else if (this.analyzeNodeNames(this.AD_WRAPPER_ATTRS, this.AD_WRAPPER, this.AD_WRAPPER_EXCLUDE) && this.index !== 0) {
            this.is_ad_node = true;
        }
        else {
            switch (this.tag_name) {
                case "#text":
                    this.analyzeTextNode();
                    break;
                case "a":
                    this.analyzeLinkNode();
                    break;
                case "img":
                    this.analyzeImageNode();
                    break;
                default:
                    this.analyzeGenericNode();
                    break;
            }

            // Analyze visual information.
            if (this.tag_name !== "#text") {
                this.node_width = Math.min(this.node.parent().width(), this.node.width());
                this.node_height = Math.min(this.node.parent().height(), this.node.height());
                this.node_area = this.node_width * this.node_height;

                var offset = this.node.offset();
                var $w = $($SendToKindle.getActiveWindow());

                // Node ATF or BTF.
                this.is_atf_node = offset.top < $w.height();
                this.is_btf_node = !this.is_atf_node;

                // Calculate the area that is ATF.
                var atfHeight = ($w.height() - offset.top);
                atfHeight = (atfHeight < 0 ? 0 : atfHeight);

                this.node_atf_area = this.node_width * atfHeight;
                this.ratio_node_atf = this.node_atf_area / this.node_area;

                // Calculate the area that is BTF.
                this.node_btf_area = this.node_width * (this.node_height - atfHeight);
                this.ratio_node_btf = this.node_btf_area / this.node_area;
            }
        }
    },

    /**
     * Prepare the final metrics.
     */
    prepare: function () {
        // Finalize subtree metrics.
        this.subtree_links             += this.count_links;
        this.subtree_texts             += this.count_texts;
        this.subtree_ad_nodes          += this.count_ad_nodes;
        this.subtree_ad_images         += this.count_ad_images;
        this.subtree_ad_links          += this.count_ad_links;
        this.subtree_plain_texts       += this.count_plain_texts;
        this.subtree_link_texts        += this.count_link_texts;
        this.subtree_images            += this.count_images;
        this.subtree_small_images      += this.count_small_images;
        this.subtree_medium_images     += this.count_medium_images;
        this.subtree_large_images      += this.count_large_images;
        this.subtree_skip_images       += this.count_skip_images;
        this.subtree_lines             += this.count_lines;
        this.subtree_words             += this.count_words;
        this.subtree_small_paragraphs  += this.count_small_paragraphs;
        this.subtree_large_paragraphs  += this.count_large_paragraphs;
        this.subtree_inline_nodes      += this.count_inline_nodes;
        this.subtree_valid_containers  += this.count_valid_containers;
        this.subtree_metadata_nodes    += this.count_metadata_nodes;
        this.subtree_length_text       += this.length_text;
        this.subtree_length_link_text  += this.length_link_text;
        this.subtree_length_plain_text += this.length_plain_text;

        // Calculate all dynamic statistics data.
        this.link_density = Math.min((this.count_links / (this.count_plain_texts || 1)), 1);
        this.link_text_density = Math.min((this.length_link_text / (this.length_plain_text || 1)), 1);

        this.subtree_link_density = Math.min((this.subtree_links / (this.subtree_plain_texts || 1)), 1);
        this.subtree_link_text_density = Math.min((this.subtree_length_link_text / (this.subtree_length_plain_text || 1)), 1);

        // Calculate ratios of text type to overall text in the subtree.
        this.subtree_ratio_length_link_text = Math.min(this.subtree_length_link_text / (this.subtree_length_text || 1), 1);
        this.subtree_ratio_length_plain_text = Math.min(this.subtree_length_plain_text / (this.subtree_length_text || 1), 1);

        // Append information to node.
        this.node.data("s2k", this);
    },

    /**
     * Finalize the node metrics and calculate NCR.
     * @param globalMetrics Global Metrics
     */
    score: function (globalMetrics) {
        this.scoreAsNCRT(globalMetrics, false);
        this.scoreAsVLB(globalMetrics);
    },

    /**
     * Score with NCRT parameters.
     * @param globalMetrics     Global Metrics
     * @param ncrOnly           Calculate NCR only.
     */
    scoreAsNCRT: function (globalMetrics, ncrOnly) {
        // Calculate text ratios based on global metrics.
        this.ratio_length_text = (this.subtree_length_text - this.subtree_length_link_text) / (globalMetrics.subtree_length_text - globalMetrics.subtree_length_link_text);
        this.ratio_length_plain_text = (this.subtree_length_plain_text - this.subtree_length_link_text) / (globalMetrics.subtree_length_plain_text - globalMetrics.subtree_length_link_text);
        this.ratio_length_link_text = this.subtree_length_link_text / globalMetrics.subtree_length_link_text;

        if (!ncrOnly && (this.ratio_length_plain_text < 0.90 || this.ratio_length_text < 0.90)) {
            // Discard the candidate, if it does not have at least 85% of the page's text.
            this.ncrt_noise_index = Number.MAX_VALUE;
            this.ncrt_content_index = 1;
        }
        else {
            // Calculate noise index.
            this.ncrt_noise_index = this.ratio_length_link_text +
                Math.min((this.subtree_ad_images + this.subtree_skip_images) / ((this.subtree_images + this.subtree_skip_images) || 1), 1) +
                Math.min(this.subtree_ad_links / (this.subtree_links || 1), 1) +
                Math.min(this.subtree_valid_containers / (globalMetrics.subtree_valid_containers || 1), 1) +
                (Math.min(this.subtree_small_images / (this.subtree_images || 1), 1) * 0.2);

            // Calculate content index.
            this.ncrt_content_index = this.ratio_length_plain_text +
                Math.min(this.count_inline_nodes / (globalMetrics.subtree_inline_nodes || 1), 1) +
                Math.min((this.subtree_large_images + this.subtree_medium_images) / (this.subtree_images || 1), 1) +
                Math.min(this.subtree_lines / (globalMetrics.subtree_lines || 1), 1) +
                Math.min(this.subtree_small_paragraphs / (globalMetrics.subtree_small_paragraphs || 1), 1) +
                Math.min(this.subtree_large_paragraphs / (globalMetrics.subtree_large_paragraphs || 1), 1);
        }

        // Calculate NCR.
        this.ncrt_score = Math.min((this.ncrt_noise_index || Number.MAX_VALUE) / (this.ncrt_content_index || 1), 1);
    },

    /**
     * Score as VLB.
     * @param globalMetrics Global Metrics
     */
    scoreAsVLB: function (globalMetrics) {
        // Calculate image ratio.
        var imageRatio = Math.min(this.subtree_images / (globalMetrics.subtree_images || 1), 1);

        if (this.ratio_length_text < 0.2) {
            if (imageRatio < 0.2) {
                // Discard blocks with little text.
                this.vlb_noise_index = Number.MAX_VALUE;
                this.vlb_content_index = 1;
                return;
            }
        }

        // Calculate a noise index for VLB.
        this.vlb_noise_index =
            Math.min(this.subtree_valid_containers / (globalMetrics.subtree_valid_containers || 1), 1);


        // Calculate a content index for VLB.
        this.vlb_content_index =
            (this.ratio_length_text * 2.5) +
            (this.ratio_node_area * 2.0) +
            (this.ratio_node_atf * 1.5) +
            (this.subtree_images / (this.subtree_links || 1)) +
            (Math.min(this.subtree_images / (globalMetrics.subtree_images || 1), 1)) +
            (Math.min(this.subtree_inline_nodes / (globalMetrics.subtree_inline_nodes  || 1), 1));


        // Calculate the VLB score.
        this.vlb_score = Math.min((this.vlb_noise_index || Number.MAX_VALUE) / (this.vlb_content_index || 1), 1);
    },

    /**
     * Export node metrics as ARFF record.
     */
    asRecord: function () {
        return this.index + "," +
            this.depth + "," +
            this.layer_index + "," +
            "'" + this.node_name + "'," +
            this.node_area + "," +
            this.node_atf_area + "," +
            this.node_btf_area + "," +
            this.is_valid_container + "," +
            this.is_named_node + "," +
            this.is_atf_node + "," +
            this.is_btf_node + "," +
            this.count_links + "," +
            this.count_texts + "," +
            this.count_plain_texts + "," +
            this.count_link_texts + "," +
            this.count_ad_links + "," +
            this.count_ad_images + "," +
            this.count_images + "," +
            this.count_small_images + "," +
            this.count_medium_images + "," +
            this.count_large_images + "," +
            this.count_skip_images + "," +
            this.count_lines + "," +
            this.count_words + "," +
            this.count_small_paragraphs + "," +
            this.count_large_paragraphs + "," +
            this.count_valid_containers + "," +
            this.count_inline_nodes + "," +
            this.length_link_text + "," +
            this.length_plain_text + "," +
            this.length_text + "," +
            this.subtree_links + "," +
            this.subtree_texts + "," +
            this.subtree_ad_images + "," +
            this.subtree_ad_links + "," +
            this.subtree_plain_texts + "," +
            this.subtree_link_texts + "," +
            this.subtree_images + "," +
            this.subtree_small_images + "," +
            this.subtree_medium_images + "," +
            this.subtree_large_images + "," +
            this.subtree_skip_images + "," +
            this.subtree_lines + "," +
            this.subtree_words + "," +
            this.subtree_small_paragraphs + "," +
            this.subtree_large_paragraphs + "," +
            this.subtree_inline_nodes + "," +
            this.subtree_valid_containers + "," +
            this.subtree_length_text + "," +
            this.subtree_length_link_text + "," +
            this.subtree_length_plain_text + "," +
            this.link_density + "," +
            this.link_text_density + "," +
            this.ratio_length_plain_text + "," +
            this.ratio_length_link_text + "," +
            this.ratio_length_text + "," +
            this.ratio_node_area + "," +
            this.ratio_node_atf + "," +
            this.ratio_node_btf + "," +
            this.vlb_content_index + "," +
            this.vlb_noise_index + "," +
            this.ncrt_content_index + "," +
            this.ncrt_noise_index + "," +
            this.vlb_score + "," +
            this.ncrt_score;
    }
};