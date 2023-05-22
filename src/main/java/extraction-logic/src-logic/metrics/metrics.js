/**
 * metrics.js
 * @author: Bernhard Wolkerstorfer
 * 
 * Description: Metrics
 * 
 * Copyright (c) 2012 Amazon.com, Inc. All rights reserved.
 */

// ######## Constructor ########
/** @constructor */
$SendToKindle.Metrics = function (service) {
    this.sessionTime = undefined;
    this.counters = {};
    this.timers = {};
    this.service = service;

    // Flag set by the compiler to enable/disable metrics.
    this.metricsMode = "@metricsMode@";
};
        
/**
 * Initialize a metrics session.
 */
$SendToKindle.Metrics.prototype = {
    // ######## Constants ########
    NAMES: {
        // -------- Counters (Common) --------
        "c_source": { "ctr": "source." },
        
        // -------- Counters (Extensions) --------
        "c_extraction_ncrt": { "ctr": "extraction.ncrt", "ref": "et_nrct" },
        "c_extraction_maps": { "ctr": "extraction.maps", "ref": "et_mps" },
        "c_extraction_multipage": { "ctr": "extraction.multipage", "ref": "et_mlp" },
        "c_extraction_result_ncrt": { "ctr": "extraction.result.ncrt", "ref": "et_r_nrct" },
        "c_extraction_result_vlb": { "ctr": "extraction.result.vlb", "ref": "et_r_vlb" },
        "c_extraction_result_atfp": { "ctr": "extraction.result.atfp", "ref": "et_r_atfp" },
        "c_extraction_error": { "ctr": "extraction.error", "ref": "et_err" },
        "c_invalid_page":  { "ctr": "extraction.invalid_page", "ref": "et_ip" },
        "c_invalid_image":  { "ctr": "extraction.invalid_image", "ref": "et_ii" },
        
        // -------- Counters for S2K Preview --------
        "c_preview_show": { "ctr": "preview.show", "ref": "show" },
        "c_preview_close": { "ctr": "preview.close", "ref": "close" },
        "c_preview_send": { "ctr": "preview.document", "ref": "sdd" },
        "c_quality_good": { "ctr": "quality.good", "ref": "qlty_gd" },
        "c_quality_bad": { "ctr": "quality.bad", "ref": "qlty_bd" },
        "c_quality_lowcd": { "ctr": "quality.confidence", "ref": "qlty_lwcd" },
        "c_feedback": { "ctr": "feedback", "ref": "fdbck" },
        "c_ui_resize": { "ctr": "ui.resize", "ref": "rsz" },
        "c_ui_menu_layout": { "ctr": "ui.menu_layout", "ref": "mn_lyt" },
        "c_ui_fsize_1": { "ctr": "ui.font_size_1", "ref": "fnt_sz_1" },
        "c_ui_fsize_2": { "ctr": "ui.font_size_2", "ref": "fnt_sz_2" },
        "c_ui_fsize_3": { "ctr": "ui.font_size_3", "ref": "fnt_sz_3" },
        "c_ui_fsize_4": { "ctr": "ui.font_size_4", "ref": "fnt_sz_4" },
        "c_ui_fsize_5": { "ctr": "ui.font_size_5", "ref": "fnt_sz_5" },
        "c_ui_margin_1": { "ctr": "ui.margin_1", "ref": "mrgn_1" },
        "c_ui_margin_2": { "ctr": "ui.margin_2", "ref": "mrgn_2" },
        "c_ui_margin_3": { "ctr": "ui.margin_3", "ref": "mrgn_3" },
        "c_ui_margin_4": { "ctr": "ui.margin_4", "ref": "mrgn_4" },
        "c_ui_margin_5": { "ctr": "ui.margin_5", "ref": "mrgn_5" },
        "c_ui_line_1": { "ctr": "ui.line_1", "ref": "ln_1" },
        "c_ui_line_2": { "ctr": "ui.line_2", "ref": "ln_2" },
        "c_ui_line_3": { "ctr": "ui.line_3", "ref": "ln_3" },
        "c_ui_line_4": { "ctr": "ui.line_4", "ref": "ln_4" },
        "c_ui_line_5": { "ctr": "ui.line_5", "ref": "ln_5" },
        "c_ui_mode_white": { "ctr": "ui.mode_white", "ref": "md_wht" },
        "c_ui_mode_sepia": { "ctr": "ui.mode_sepia", "ref": "md_sp" },
        "c_ui_mode_black": { "ctr": "ui.mode_black", "ref": "md_blck" },
        "c_ui_face_georgia": { "ctr": "ui.face_georgia", "ref": "fc_grg" },
        "c_ui_face_pt": { "ctr": "ui.face_georgia", "ref": "fc_pt" },
        "c_ui_face_palatino": { "ctr": "ui.face_georgia", "ref": "fc_pltn" },
        "c_ui_scroll": { "ctr": "ui.scroll", "ref": "scr" },
        "c_ui_scroll_small": { "ctr": "ui.scroll_small", "ref": "scr_smll" },
        "c_ui_scroll_medium": { "ctr": "ui.scroll_medium", "ref": "scr_mdm" },
        "c_ui_scroll_large": { "ctr": "ui.scroll_large", "ref": "scr_lrg" },

        // -------- Timers (Common) --------
        "t_send": { "tmr": "send.time_send" },                
        "t_send_url": { "tmr": "send.time_url" },                
        "t_send_upload": { "tmr": "send.time_upload" },                
        "t_send_enqueue": { "tmr": "send.time_enqueue" },                

        // -------- Timers (Extensions) --------
        "t_extraction": {"tmr": "extraction"},
        
        // -------- Timers (S2K Preview) -------- 
        "t_ui_resize": { "tmr": "ui.time_resize" },                
        "t_ui_layout": { "tmr": "ui.time_layout" },
        "t_feedback": { "tmr": "send.time_feedback" }
    },
        
    // ######## Methods ########
    /**
     * Initialize metrics session.
     */
    init: function () {
        this.sessionTime = new Date().getTime();
        this.counters = {};
        this.timers = {};
    },

    /**
     * Terminate a metrics session.
     * @param {function()} callback Callback
     */        
    terminate: function (callback) {
        // Setup callback trigger.
        var invokeCallback = true;
        
        if (this.sessionTime !== undefined) {
            // Stop session timing.   
            this.sessionTime = new Date().getTime() - this.sessionTime;
            
            // Process timer metrics data.
            var perfTimers = {};
            for (var tn in this.timers) {
                var t = this.timers[tn];
                if (t.count > 0) {
                    perfTimers[tn] = {"value": parseInt(t.sum / t.count, 10), "repeat": t.count};
                }
            }
    
            // Process counter metrics data.
            var perfCounters = {};
            for (var c in this.counters) {
                perfCounters[c] = {"value" : this.counters[c]};
            }
            
            // Create metrics object.
            var performanceData = {
                "sessionTime": this.sessionTime,
                "counters":  perfCounters,
                "timers": perfTimers
            };
            
            if (this.metricsMode === "emit-metrics") {
                // Deactivate callback trigger.
                invokeCallback = false;
                
                // Trigger service request to store metrics.
                this.service.emitMetrics(JSON.stringify(performanceData), callback);
            }
            else if (window.console !== undefined) {
                // Log metrics to console.
                window.console.log(JSON.stringify(performanceData));
            }
            
            // Reset session data.
            this.init();
        }
        
        // Invoke callback right ways, if not async action was triggered.
        if (invokeCallback === true && callback !== undefined) {
            callback();
        }
    },

    /**
     * Record an out-of-band value in a timer.
     * @param timer  Timer
     * @param value Value
     */
    recordTimer: function (timer, value) {
        if (timer !== undefined && timer.tmr !== undefined) {
            var t = this.timers[timer.tmr];
            if (t === undefined) {
                t = { sum: 0, count: 0 };
                this.timers[timer.tmr] = t; 
            }
            
            t.sum += value;
            t.count++;
        }
    },

    /**
     * Start a timer session.
     * @param timer  Timer
     */
    startTimer: function (timer) {
        if (timer !== undefined && timer.tmr !== undefined) {
            var t = this.timers[timer.tmr];
            if (t !== undefined && t.start === undefined) {
                t.start = new Date().getTime();
            }
            else if (t === undefined) {
                this.timers[timer.tmr] = { sum: 0, count: 0, start: new Date().getTime() };
            }
        }
    },

    /**
     * Stop a timer session.
     * @param timer  Timer
     */        
    stopTimer: function (timer) {
        if (timer !== undefined && timer.tmr !== undefined) {
            var t = this.timers[timer.tmr];
            if (t !== undefined && t.start !== undefined) {
                t.sum += new Date().getTime() - t.start;
                t.count++;
                t.start = undefined;
            }
        }
    },
    
    /**
     * Increase prefix counter.
     * @param prefix Counter Prefix
     * @param value  Value
     */
    countWithPrefix: function (prefix, value) {
        if (prefix !== undefined && prefix.ctr !== undefined) {
            var counter = prefix.ctr + (value || "");
            if (this.counters[counter] !== undefined) {
                this.counters[counter]++;
            }
            else {
                this.counters[counter] = 1;
            }
        }
    },
    
    /**
     * Increase counters.
     * This method uses a variable parameter list.
     */
    count: function () {
        for (var i = 0; i < arguments.length; i++) {
            var counter = arguments[i];
            if (counter !== undefined && counter.ctr !== undefined) {
                // Increase counter.
                this.countWithPrefix(counter, undefined);
                
                // Send a RefTag to the service.
                if (counter.ref !== undefined) {
                    this.service.sendRefTag(counter.ref);
                }
            }
        }
    }
};