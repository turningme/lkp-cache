/**
 * status-message.js
 * @author: Bernhard Wolkerstorfer
 *
 * Description: Generic message container.
 *
 * Copyright (c) 2012 Amazon.com, Inc. All rights reserved.
 *
 */

/**
 * @constructor
 */
$SendToKindle.StatusMessage = function (mode, actionHandler) {
    // ######## Privileged Members ########
    this.stateWindow = $($SendToKindle.getState().stateWindow);
    this.containerMode = "message";
    this.messageMode = mode || "status";

    this.containerSettings = undefined;
    this.modeSettings = undefined;

    this.container = undefined;
    this.icon = undefined;
    this.message = undefined;

    this.actions = undefined;
    this.neverShowAgain = undefined;
    this.actionHandler = actionHandler;
};

$SendToKindle.StatusMessage.prototype = {
    // ######## Constants ########
    /**
     * Message Container Types
     */
    CONTAINER: {
        /**
         * Standard message container for S2K.
         */
        "message": {
            "layout": '<div id="s2k-status-message"></div>',
            "styles": {
                "#s2k-status-message": {
                    "display": "none",
                    "position": "fixed",
                    "height": "107px",
                    "top": "90px",
                    "padding": "15px",
                    "z-index": 999999999999,
                    "box-shadow": "0px 0px 25px #484848",
                    "text-align": "center",
                    "border-radius": "5px",
                    "background": "#3c3c3c url(_EXTURL_/images/s2k-hsprite.png) repeat-x 0px -115px"
                }
            }
        }
    },

    /**
     * Message Modes
     */
    MODE: {
        /**
         * Status Message for processing actions.
         */
        "status": {
            "width": 330,
            "abortSupported": true,
            "layout":
            '<div class="s2k-icon"></div>' +
            '<div class="s2k-message"></div>',
            "styles": {
                "#s2k-status-message": {
                    "width": "300px",
                    "height": "86px"
                },
                ".s2k-message": {
                    "margin-top": "0px",
                    "font-weight": "bold",
                    "font-size": "12px",
                    "color": "#fff",
                    "font-family": "'Helvetica Neue', Helvetica, Arial, sans-serif"
                }
            },
            "messages": {
                "default": "<div style='text-align:center;font-size:1.2em'>Loading Send to Kindle...</div>",
                "analyze": "<div style='text-align:center;font-size:1.2em'>Analyzing website...</div>",
                "preview": "<div style='text-align:center;font-size:1.2em'>Loading preview...</div>",
                "setup": "<div style='text-align:center;font-size:1.2em'>Loading device setup...</div>",
                "send": "<div style='text-align:center;font-size:1.2em'>Sending content...</div>",
                "success": "<div style='text-align:center;font-size:1.2em;margin-bottom:10px'>Upload complete.</div><span style='font-weight:normal'>Give us a few minutes to format and deliver your content.</span>",
                "error": "We're sorry but we are unable to send documents to your Kindle. Please ensure that you are connected to the network and try again.",
                "extraction": "We're sorry but there has been an error while analyzing this page.",
                "preview-plugin": "We're sorry but we are unable to preview documents of this type. Please send the document to your Kindle instead."
            },
            "icons": {
                "default": {
                    "width": "54px",
                    "height": "54px",
                    "margin": "5px auto 10px",
                    "background": "transparent url(_EXTURL_/images/s2k-spinner-large.gif)"
                },
                "success": {
                    "margin": "0px auto 10px",
                    "width": "25px",
                    "height": "25px",
                    "background": "transparent url(_EXTURL_/images/s2k-sprite.png) no-repeat -221px -78px"
                },
                "error": {
                    "margin": "0px auto 10px",
                    "width": "25px",
                    "height": "25px",
                    "background": "transparent url(_EXTURL_/images/s2k-sprite.png) no-repeat -247px -52px"
                },
                "extraction": {
                    "margin": "0px auto 10px",
                    "width": "25px",
                    "height": "25px",
                    "background": "transparent url(_EXTURL_/images/s2k-sprite.png) no-repeat -247px -52px"
                },
                "preview-plugin": {
                    "margin": "0px auto 10px",
                    "width": "25px",
                    "height": "25px",
                    "background": "transparent url(_EXTURL_/images/s2k-sprite.png) no-repeat -247px -52px"
                }
            }
        },

        /**
         * Message for low quality extractions.
         */
        "low_quality": {
            "width": 590,
            "abortSupported": false,
            "layout":
            '<style type="text/css">' +
            '    .s2k-action { cursor: pointer; color: #f90; text-decoration: underline }' +
            '</style>' +
            '<div class="s2k-icon"></div>' +
            '<div class="s2k-message"></div>',
            "styles": {
                "#s2k-status-message": {
                    "width": "560px"
                },
                ".s2k-message": {
                    "line-height": "1.5em",
                    "margin": "10px 0px",
                    "font-weight": "normal",
                    "font-size": "12px",
                    "color": "#fff",
                    "font-family": "'Helvetica Neue', Helvetica, Arial, sans-serif"
                },
                ".s2k-icon": {
                    "margin-top": "-4px"
                }
            },
            "messages": {
                "default": 'We are uncertain about creating a good representation of this webpage for reading later on Kindle. You can ' +
                'highlight the specific text you wish to read on webpage and send using "Send Selected Text" option.' +
                '<div style="margin-top:5px">Do you want to ' +
                '    <span class="s2k-action" s2k-action="PREVIEW" style="">Preview & Send</span> or ' +
                '    <span class="s2k-action" s2k-action="SEND">Continue Sending</span>?' +
                '</div>' +
                '<div style="font-size:10px;text-align:left;margin-top:1px">' +
                '    <input class="s2k-never-show-again" type="checkbox" name="s2k-always-send" />' +
                '    <label for="s2k-always-send">Do not show this message again.</label>' +
                '</div>'
            },
            "icons": {
                "default": {
                    "margin": "0px auto 10px",
                    "width": "25px",
                    "height": "25px",
                    "background": "transparent url(_EXTURL_/images/s2k-sprite.png) no-repeat -247px -78px"
                }
            }
        }
    },

    /**
     * Format a node with the provided styles.
     * @param $node     Node
     * @param styles    Hash(Selector, Style)
     */
    format: function ($node, styles) {
        for (var selector in styles) {
            // Load the CSS style.
            var cssStyle = styles[selector];

            // Replace a possible sprite URL.
            if (cssStyle.background !== undefined) {
                cssStyle.background = cssStyle.background.replace("_EXTURL_", $SendToKindle.extensionUrl);
            }

            // Format matching elements with the mapped style.
            if ($node.is(selector)) {
                $node.css(cssStyle);
            }
            else {
                $node.find(selector).css(cssStyle);
            }
        }
    },

    /**
     * Remove status message container from DOM.
     * @param delay         Delay the termination in favor of a message.
     * @param immediate     Skip delay and remove immediately.
     */
    terminate: function (delay, immediate) {
        setTimeout(function () {
            this.container.fadeOut("fast", function () {
                this.container.remove();
            }.bind(this));

            this.stateWindow.off("resize.s2k-status");
            this.stateWindow.off("keyup.s2k-status");
        }.bind(this), 2000 * (immediate ? 0 : (delay ? 2.5 : 1)));
    },


    /**
     * Set the message that should be displayed.
     * @param messageKey   Key for Message
     */
    setMessage: function (messageKey) {
        var message = this.modeSettings.messages[messageKey];
        var icon = this.modeSettings.icons[messageKey];

        // Set message on view.
        if (message !== undefined) {
            this.message.html(message);
        }

        // Set icon on view.
        if (icon !== undefined) {
            // Replace extension URL place holder with the real URL.
            if (icon.background !== undefined) {
                icon.background = icon.background.replace("_EXTURL_", $SendToKindle.extensionUrl);
            }

            if (icon["-webkit-mask-image"] !== undefined) {
                icon["-webkit-mask-image"] = icon["-webkit-mask-image"].replace("_EXTURL_", $SendToKindle.extensionUrl);
            }

            this.icon.css(icon);
        }
    },

    // ######## Event Handler ########
    /**
     * Event handler for resize events.
     */
    onResize: function () {
        // Calculate left position for progress information.
        this.container.css("left", ((this.stateWindow.width() - this.modeSettings.width) / 2) + "px");
    },

    /**
     * Event handler for abort actions.
     * @param event Event
     */
    onAbortAction: function (event) {
        if (this.actionHandler !== undefined && event.which === 27) {
            this.actionHandler("status.abort");
        }
        return false;
    },

    /**
     * Event handler for message actions.
     * @param event Event
     */
    onMessageAction: function (event) {
        var action = $(event.target).attr("s2k-action");
        var isNeverShowAgain = this.neverShowAgain.is(":checked");

        if (action !== undefined && this.actionHandler !== undefined) {
            this.actionHandler(action, isNeverShowAgain);
        }
        return false;
    }
};
