/* $This file is distributed under the terms of the license in /doc/license.txt$ */

String.prototype.capitalize = function() {
    return this.substring(0,1).toUpperCase() + this.substring(1);
};