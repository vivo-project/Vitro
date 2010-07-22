/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var vitro;
// vitro == null: true
// vitro === null: false (only true if undefined)
// typeof vitro == 'undefined': true
if (!vitro) { 
    vitro = {};
}

vitro.browserUtils = {
    
    isIELessThan8: function() {
        var version;
        if (navigator.appVersion.indexOf("MSIE") == -1) {
            return false;
        }
        else {
            version = parseFloat(navigator.appVersion.split("MSIE")[1]);
            return version < 8;
        }    
    }
};

