/* $This file is distributed under the terms of the license in /doc/license.txt$ */

String.prototype.capitalize = function() {
    return this.substring(0,1).toUpperCase() + this.substring(1);
};

String.prototype.capitalizeWords = function() {
	var words = this.split(/\s+/), 
		wordCount = words.length,
		i,
		newWords = [];
	
	for (i = 0; i < wordCount; i++) {
		newWords.push(words[i].capitalize());
	}
	
	return newWords.join(' ');	
};
