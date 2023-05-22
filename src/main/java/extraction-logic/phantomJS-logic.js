// Some JavascriptExecutors (PhantomJS in particular) do not have a bind 
// function implemented. This statement implements one if one does not
// already exist
if (!Function.prototype.bind) {
    Function.prototype.bind = function(oThis) {
        if (typeof this !== 'function') {
            throw new TypeError(
                    'Function.prototype.bind - what is trying to be bound is not callable');
        }
        var aArgs = Array.prototype.slice.call(arguments, 1), fToBind = this, fNOP = function() {
        }, fBound = function() {
            return fToBind.apply(this instanceof fNOP && oThis ? this : oThis,
                    aArgs.concat(Array.prototype.slice.call(arguments)));
        };
        fNOP.prototype = this.prototype;
        fBound.prototype = new fNOP();
        return fBound;
    };
};


// When callback is called from JavaScript, then the first argument
// is the return value in java land.
var callback = arguments[arguments.length - 1];

window.$SendToKindle.launch();