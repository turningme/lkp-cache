/**
 * map-extraction.js
 * 
 * @author: Bernhard Wolkerstorfer
 * 
 * Description: Extractor for map applications.
 * 
 * Copyright (c) 2012 Amazon.com, Inc. All rights reserved.
 */


/**
 * @constructor
 */
$SendToKindle.MapExtractor = function () {
    // ######## Members #######
    this.mapProviders = [
        {name: "Google Maps", urlScheme: /maps\.google\.\w+/i, handler: this.extractGMaps},
        {name: "Bing Maps", urlScheme: /\w\.bing\.\w+\/maps/i, handler: this.extractBMaps}
    ];
    this.rowFlag = false;
};

$SendToKindle.MapExtractor.prototype = {
    // ######## Constants ########
    /**
     * Tag to be remove while copying the content.
     */
    REMOVABLE_TAGS: "script,noscript,object,embed,iframe,frame,frameset,noframes,aside,menu,header,footer,source,audio,video,form,ins,del,style,form,cite,.social-media-container",

    /**
     * Template for map results.
     */
    MAP_TEMPLATE: '<div class="s2k-content-map">' +
                  '   <div class="s2k-maps-disclosure"></div>' +
                  '   <div class="s2k-maps-copyright"></div>' +
                  '</div>',
    
    MAP_WAYPOINTS: '<div class="s2k-map-waypoints">' +
                   '    <table class="s2k-maps-table">' +
                   '    </table>' +
                   '</div>',
    MAP_WP_HEADER: '<tr class="s2k-darkrow s2k-waypoint-header">' +
                   '    <td class="s2k-maps-icon"></td>' +
                   '    <td class="s2k-maps-text" colspan="2"></td>' +
                   '    <td class="s2k-maps-distance" style="font-size: 70%">' +
                   '        <div class="s2k-maps-distance s2k-maps-step-distance"></div>' +
                   '        <div class="s2k-maps-distance s2k-maps-total-distance"></div>' +
                   '    </td>' +
                   '</tr>',
                   
    MAP_WP_ENTRY: '<tr class="s2k-waypoint-step">' +
                  '    <td class="s2k-maps-icon"></td>' +
                  '    <td class="s2k-maps-num"></td>' +
                  '    <td class="s2k-maps-text">' +
                  '         <div class="s2k-maps-step-description"></div>' +
                  '         <div class="s2k-maps-step-notice"></div>' +
                  '         <div class="s2k-maps-step-duration"></div>' +
                  '    </td>' +
                  '    <td class="s2k-maps-distance">' +
                  '        <div class="s2k-maps-distance s2k-maps-step-distance"></div>' +
                  '        <div class="s2k-maps-distance s2k-maps-total-distance"></div>' +
                  '    </td>' +
                  '</tr>',
    
    MAP_WP_SUMMARY: '<tr class="s2k-waypoint-info">' +
                    '   <td colspan="4"></td>' +
                    '</tr>',
                  
    MAX_MAP_RETRIES: 10,

    // ######## Methods ########
    /**
     * Format map content.
     * @param mapData   Map Data
     */
    format: function (mapData) {
        // Create a content template.
        var $content = $(this.MAP_TEMPLATE);
        
        // Set legal information.
        if (mapData.disclaimer !== null) {
            $content.find(".s2k-maps-disclosure").text(mapData.disclaimer);
        }
        
        // Set map data copyright.        
        if (mapData.copyright !== null) {
            $content.find(".s2k-maps-copyright").text(mapData.copyright);            
        }            
        
        // Process waypoints.
        if (mapData.waypoints && mapData.waypoints.length > 0) {
            var self = this;
            var waypoints = $(this.MAP_WAYPOINTS);
            var wplist = waypoints.find("table.s2k-maps-table");
            
            // Iterate all waypoints.
            mapData.waypoints.each(function () {
                switch (this.type) {
                case "HEADER":
                    wplist.append(self.formatHeader(this));
                    self.rowFlag = false;
                    break;
                case "STEP":
                    wplist.append(self.formatStep(this));
                    self.rowFlag = !self.rowFlag;
                    break;
                case "SUMMARY":
                    wplist.append(self.formatWpSummary(this));
                }
            });

            // Append waypoint list.
            $content.prepend(waypoints);
        }
        
        // Handle image, if available.
        if (mapData.mapImage && mapData.mapImage.length > 0) {
            // Push the image to the result.
            $content.prepend($("<div class='s2k-maps-image s2k-default-image'>").append(
                                $("<img>", {"src": mapData.mapImage.attr("src"), "width": "100%" })));
            
            // Set image container width.
            $content.find(".s2k-maps-image").width(
                Math.min(mapData.mapImage[0].naturalWidth || $(mapData.mapImage[0]).width() || 500, 500)
            );
        }
        
        return $content;
    },

    /**
     * Format an image.
     * @param image
     */
    formatImage: function (image) {
        if (image && image.length > 0) {
            // Image is a real image.
            if (typeof(image) === "object" && image.attr("src").match(/(gray|transparent)\.png/i) === null) {
                var width = image.attr("width") || image.width();
                var height = image.attr("height") || image.height();
                return "<img src='" + image.attr("src") + "' " +
                       "   width='" + width + "' " +
                       "   height='" + height + "' " +
                       "   style='width:" + width + "px;height:" + height + "px' />";                
            }
            else if (typeof(image) === "string") {
                return image;
            }
        }
        return "";
    },
    
    /**
     * Format a waypoint header.
     * @param headerData
     */
    formatHeader: function (headerData) {
        var header = $(this.MAP_WP_HEADER);
        var icon = headerData.icon;
        var text = headerData.text;
        var distance = headerData.distance;
        var time = headerData.time;
        
        // Set header icon.
        header.find(".s2k-maps-icon").append(this.formatImage(icon));
        header.find(".s2k-maps-text").text(text);
        header.find(".s2k-maps-distance .s2k-maps-step-distance").text(distance);
        header.find(".s2k-maps-distance .s2k-maps-total-distance").text(time);
        
        return header;
    },

    /**
     * Format a waypoint step
     * @param stepData
     */
    formatStep: function (stepData) {
        // Create a waypoint entry.
        var step = $(this.MAP_WP_ENTRY);
        
        // Alternate row color.
        if (this.rowFlag === true) {
            step.addClass("s2k-darkrow");
        }

        // Set step data.
        step.find(".s2k-maps-icon").append(this.formatImage(stepData.icon));
        step.find(".s2k-maps-num").append(stepData.number);
        step.find(".s2k-maps-step-description").append(stepData.text);
        step.find(".s2k-maps-step-notice").append(stepData.notice);
        step.find(".s2k-maps-step-duration").append(stepData.duration);
        step.find(".s2k-maps-step-distance").append(stepData.stepDistance);
        step.find(".s2k-maps-total-distance").append(stepData.totalDistance);
        
        return step;
    },
    
    /**
     * Format waypoint summary.
     * @param wpsData   Summary Data
     */
    formatWpSummary: function (wpsData) {
        return $(this.MAP_WP_SUMMARY).find("td").append(wpsData.text);
    },
    
    /**
     * Extract directions from Google Maps.
     * @param state     State
     */
    extractGMaps: function (state, extractor, callback) {
        var $baseNode = $(state.stateDocument.body);
        
        if (state.stateDocument.location.href.search("&pw=2") === -1) {
            var permLink = $baseNode.find(".permalink-button").attr("href") + "&pw=2";
            
            // Create iframe for print view.
            $baseNode.append("<iframe id='s2k-print-frame' frameborder='0' " +
                             "    style='position:absolute;top:-800px;left:-800px;width:800px;height:800px;'></iframe>");

            // Load content in iframe.
            var printFrame = $baseNode.find("#s2k-print-frame");
            printFrame.attr("src", permLink);
            printFrame.load(function () {
                // Pass content on for formatting.
                extractor.handleGMapsContent($(printFrame[0].contentDocument.body), state.stateDocument.location.href, callback);
            });
        }
        else {
            // Pass content on for formatting.
            extractor.handleGMapsContent($baseNode, state.stateDocument.location.href, callback);
        }
    },

    /**
     * Handle Google Maps content.
     * @param $baseNode     Content Node
     * @param permLink      Link to the content.
     * @param callback      Callback
     * @param retries       Retries for map data.
     * @return Formatted Map Data
     */
    handleGMapsContent: function ($baseNode, permLink, callback, retries) {
        // Initialize retries, if necessary.
        retries = retries || 0;

        // Try to load the map image or proceed without, if maximum retries
        // was already hit by the extractor.
        var mapImage = $baseNode.find(".printimage");
        if (mapImage.length === 0 && retries < this.MAX_MAP_RETRIES) {
            // Try to trigger the event that shows the map.
            if ($baseNode.find("#main_map").css("display") === "none") {
                $baseNode.find("#showmap_cb").click();
            }
            
            // Retry in 100ms.
            setTimeout(function () {
                this.handleGMapsContent($baseNode, permLink, callback, (++retries));
            }.bind(this), 200);
            return;
        }
        else {
            // Load page block.
            var $c = $baseNode.find("#page");
            
            // Create map data.
            var mapData = {
                "mapImage": "",
                "disclaimer": $($c.find(".legal")[0]).text(),
                "copyright": $($c.find(".legal")[1]).text(),
                "waypoints": []
            };
            
            // Find the direction information.
            var directions = $c.find("#panel_dir");
            if (directions.length > 0) {
                var self = this;
                var waypoints = $(this.MAP_WAYPOINTS);
                var wplist = waypoints.find("table.s2k-maps-table");
                
                directions.children().each(function () {
                    var $n = $(this);
                    if ($n.hasClass("ddwpt") === true) {
                        mapData.waypoints.push({
                            "type": "HEADER",
                            "icon": $n.find(".ddptlnk img"),
                            "text": $.trim($n.find(".ddw-addr").text()),
                            "distance": $.trim($n.find(".ddw-dist").text()),
                            "time": ""
                        });
                    }
                    else if ($n.attr("id") && $n.attr("id").match(/ddr\d+/i) !== null) {
                        // Load steps and format them.
                        $n.find(".segmentdiv").each(function () {
                            // Load segment.
                            var $seg = $(this);
                            
                            // Create map data object for step.
                            var stepData = {
                                "type": "STEP",
                                "icon": $seg.find(".icon img"),
                                "number": $.trim($seg.find(".num").text()),
                                "text":  null,
                                "notice": null,
                                "duration": null,
                                "stepDistance": null,
                                "totalDistance": null
                            };
                            mapData.waypoints.push(stepData);
                            
                            // Load segment data.
                            var info = $seg.find(".dirsegtext");
                            var distance = $seg.find(".sdist").children();
                            
                            // Description for current step.                        
                            stepData.text = $(info.children()[1]).contents().clone(true);
                            stepData.text.remove("div");
                            
                            // Notice for current step.
                            stepData.notice = $.trim(info.find(".dirsegnote").text());
                            
                            // Duration of current step.
                            stepData.duration = $.trim(info.find(".segtime").text());
                            
                            // Distance information for current step.
                            stepData.stepDistance = $.trim($(distance[0]).text());
                            stepData.totalDistance = $.trim($(distance[1]).text());
                        });
                    }
                    else if ($n.attr("class") && $n.attr("class").match(/dir-rtesum/i)) {
                        mapData.waypoints.push({
                            "type": "SUMMARY",
                            "text": $.trim($n.text())
                        });
                    }
                });
            }
            
            // Find and append the map.
            mapData.mapImage = $c.find(".printimage");
            
            // Return map data.
            callback(true, permLink, mapData);
        }
    },
    
    /**
     * Extract directions from Bing Maps.
     * @param state     State
     * @param extractor Extractor
     * @param callback  Callback
     */
    extractBMaps: function (state, extractor, callback) {
        var $baseNode = $(state.stateDocument.body);
        var location = state.stateDocument.location;
        var printView = location.href.match(/pt=([a-z]+)/i);
        
        if (printView === null) {
            var printViewType = "pb";
            
            // Ensure that the hash is up to date for directions.
            if ($baseNode.find(".goButton").length > 0) {
                // Execute mapping functionality.
                $baseNode.find(".goButton").click();
                printViewType = "pf";
            }

            // Read mapping query string.
            var queryString = decodeURIComponent(atob(window.location.hash.substring(1)));
            if (queryString === "") {
                callback(false, undefined, undefined, undefined);
            }
            else {
                // Replace coordinates parameter with print version for directions view.
                if (printViewType === "pf") {
                    var coords = queryString.match(/cp=(-?\d+\.\d+)~(-?\d+\.\d+)/i);
                    if (coords !== null) {
                        queryString = queryString.replace(coords[0], "cp=" + coords[1] + "," + coords[2]);
                        queryString = queryString.replace("lvl=", "z=");
                        queryString +=  "&pt=" + printViewType;
                    }
                }
                
                // Create parameters fro 
                if (printViewType === "pb" && queryString !== "") {
                    var poiText = encodeURIComponent($.trim($("#searchPageContextContent h2").contents()[0].nodeValue));
                    var poiCoords = $("#searchPageLatLongContent").text().replace(" ", ",");
                    queryString = "mkt=en-us&z=10&s=r&cp=" + poiCoords + "&poi=" + poiText + "&b=1&pt=" + printViewType;
                }
                
                // Build a permlink for the print preview.
                var permLink = location.protocol + "//" + location.host + "/maps/print.aspx?" + queryString;
                
                // Create iframe for print view.
                $baseNode.append("<iframe id='s2k-print-frame' frameborder='0' " +
                                 "    style='position:absolute;top:-800px;left:-800px;width:800px;height:800px;'></iframe>");
    
                // Load content in iframe.
                var printFrame = $baseNode.find("#s2k-print-frame");
                printFrame.attr("src", permLink);
                printFrame.load(function () {
                    // Pass content on for formatting.
                    extractor.handleBMapsContent($(printFrame[0].contentDocument.body), state.stateDocument.location.href, printViewType, callback);
                });
            }
        }
        else {
            extractor.handleBMapsContent($baseNode, state.stateDocument.location.href, printView[1], callback);
        }
    },
        
    /**
     * Handle the Bing Maps content.
     * @param $baseNode         Base Node
     * @param permLink          Source Link
     * @param pvt               Print View Type
     * @param callback          Callback
     */
    handleBMapsContent: function ($baseNode, permLink, pvt, callback) {
        // Load main content block.
        var $c = $baseNode.find("#mainContents");

        // Create map data.
        var mapData = {
            "mapImage": $c.find((pvt === "pf" ? ".map" : ".map.mapbasic")),
            "disclaimer": $c.find(".disclaimer").text(),
            "copyright": "",
            "waypoints": []
        };        
        
        // Find the direction information.
        var directions = $c.find("#DrivingInstructions");
        if (directions.length > 0) {
            directions.each(function () {
                var $dir = $(this);

                // Find all steps for this waypoint.
                $dir.find("li").each(function () {
                    var $n = $(this);
                    
                    if ($n.attr("id") === undefined && $n.attr("class") === "directionStepList") {
                        // Load waypoint header data.
                        mapData.waypoints.push({
                            "type": "HEADER",
                            "icon": $.trim($n.find(".directionStepImg").text()),
                            "text": $.trim($n.find(".directionHeaderText").text()),
                            "distance": $.trim($n.find(".directionLegDistance").text()),
                            "time": $.trim($n.find(".directionLegTime").text()) 
                        });
                    }
                    else if ($n.attr("id") !== undefined && $n.attr("class") === "directionStepList") {
                        // Create step data.
                        var stepData = {
                            "type": "STEP",
                            "icon": $n.find(".directionStepImg"),
                            "number": $.trim($n.find(".directionStepNumber").text()),
                            "text":  null,
                            "notice": "",
                            "duration": "",
                            "stepDistance": $.trim($n.find(".directionStepDistance").text()),
                            "totalDistance": ""
                        };
                        mapData.waypoints.push(stepData);
                        
                        // Reformat notices.
                        $n.find(".directionStepIncident").each(function () {
                            stepData.notice += "<div>" + $.trim($(this).text()) + "</div>";
                        });
                        
                        // Reformat step description
                        var description = $("<div></div>").append($n.find(".directionStepInstruction").contents().clone(true));
                        description.find("span.instructionKeyword").replaceWith(function () { 
                            return ($("<strong>").text($(this).text()));
                        });
                        stepData.text = description;
                    }
                });
            });
        }        
        
        // Create a document title from the addresses.
        var title = "";
        $baseNode.find("#waypointsAddress, .locationText").each(function () { 
            title += $.trim($(this).text()) + " to ";
        });
        title = title.substring(0, title.length - 4);
        
        // Pass content on for formatting.
        callback(true, permLink, mapData, title);
    },
    
    /**
     * Extract from a map provider.
     * @param progress  Progress Callback
     */
    extract: function (progress) {
        // Load active document
        var state = $SendToKindle.getState();
        var d = state.stateDocument;
        
        // Load extractor for map provider.
        var mapProvider;
        for (var i = 0, len = this.mapProviders.length; i < len; i++) {
            if (d.location.href.match(this.mapProviders[i].urlScheme)) {
                mapProvider = this.mapProviders[i];
            }
        }
        
        // Extract map content.
        mapProvider.handler(state, this, function (success, url, mapData, title) {
            if (success === true) {
                // Format map data for Kindle.
                var $content = this.format(mapData);
                
                // Create result object.
                var result = new $SendToKindle.Document($content);
                result.title = (title || d.title).replace(" - Google Maps", "").replace(" on Bing Maps - Bing Maps", "");
                result.url = url;
                result.metadata = "Send To Kindle, Maps Extractor";
                result.publicationDate = (new Date()).toString("MMMM dd, yyyy");
                
                // Set source.
                var source = d.location.hostname.match(/.*\.(.*\.\w{3})$/);
                result.source = ((source === null || source.length < 2) ? d.location.hostname : source[1]);

                // Embed all images.
                (new $SendToKindle.ExtractorUtils()).embedImages(result.contentNode.find("img"), 0, function () {
                    progress({"success": true, "data": result});
                });
            }
            else {
                progress({"error": true, "data": "EXTRACTION_FAILED"});
            }
        }.bind(this)); 
    }
};