/* 'Magic' date parsing, by Simon Willison (6th October 2003)
   http://simon.incutio.com/archive/2003/10/06/betterDateInput

   example of usage:
   <form action="" method="get"><div>
   <input type="text" size="10" id="dateField" onblur="magicDate(this)"
          onfocus="if (this.className != 'error') this.select()">
   <div id="dateFieldMsg">mm/dd/yyyy</div>
   </div></form>
*/

/* Finds the index of the first occurence of item in the array, or -1 if not found */
Array.prototype.indexOf = function(item) {
    for (var i = 0; i < this.length; i++) {
        if (this[i] == item) {
            return i;
        }
    }
    return -1;
};
/* Returns an array of items judged 'true' by the passed in test function */
Array.prototype.filter = function(test) {
    var matches = [];
    for (var i = 0; i < this.length; i++) {
        if (test(this[i])) {
            matches[matches.length] = this[i];
        }
    }
    return matches;
};

var monthNames = "January February March April May June July August September October November December".split(" ");
var weekdayNames = "Sunday Monday Tuesday Wednesday Thursday Friday Saturday".split(" ");

/* Takes a string, returns the index of the month matching that string, throws
   an error if 0 or more than 1 matches
*/
function parseMonth(month) {
    var matches = monthNames.filter(function(item) {
        return new RegExp("^" + month, "i").test(item);
    });
    if (matches.length == 0) {
        throw new Error("Invalid month string");
    }
    if (matches.length > 1) {
        throw new Error("Ambiguous month");
    }
    return monthNames.indexOf(matches[0]);
}
/* Same as parseMonth but for days of the week */
function parseWeekday(weekday) {
    var matches = weekdayNames.filter(function(item) {
        return new RegExp("^" + weekday, "i").test(item);
    });
    if (matches.length == 0) {
        throw new Error("Invalid day string");
    }
    if (matches.length > 1) {
        throw new Error("Ambiguous weekday");
    }
    return weekdayNames.indexOf(matches[0]);
}

/* Array of objects, each has 're', a regular expression and 'handler', a
   function for creating a date from something that matches the regular
   expression. Handlers may throw errors if string is unparseable.
*/
var dateParsePatterns = [
    // Today
    {   re: /^tod/i,
        handler: function() {
            return new Date();
        }
    },
    // Tomorrow
    {   re: /^tom/i,
        handler: function() {
            var d = new Date();
            d.setDate(d.getDate() + 1);
            return d;
        }
    },
    // Yesterday
    {   re: /^yes/i,
        handler: function() {
            var d = new Date();
            d.setDate(d.getDate() - 1);
            return d;
        }
    },
    // 4th
    {   re: /^(\d{1,2})(st|nd|rd|th)?$/i,
        handler: function(bits) {
            var d = new Date();
            d.setDate(parseInt(bits[1], 10));
            return d;
        }
    },
    // 4th Jan
    {   re: /^(\d{1,2})(?:st|nd|rd|th)? (\w+)$/i,
        handler: function(bits) {
            var d = new Date();
            d.setDate(parseInt(bits[1], 10));
            d.setMonth(parseMonth(bits[2]));
            return d;
        }
    },
    // 4th Jan 2003
    {   re: /^(\d{1,2})(?:st|nd|rd|th)? (\w+),? (\d{4})$/i,
        handler: function(bits) {
            var d = new Date();
            d.setDate(parseInt(bits[1], 10));
            d.setMonth(parseMonth(bits[2]));
            d.setYear(bits[3]);
            return d;
        }
    },
    // Jan 4th
    {   re: /^(\w+) (\d{1,2})(?:st|nd|rd|th)?$/i,
        handler: function(bits) {
            var d = new Date();
            d.setDate(parseInt(bits[2], 10));
            d.setMonth(parseMonth(bits[1]));
            return d;
        }
    },
    // Jan 4th 2003
    {   re: /^(\w+) (\d{1,2})(?:st|nd|rd|th)?,? (\d{4})$/i,
        handler: function(bits) {
            var d = new Date();
            d.setDate(parseInt(bits[2], 10));
            d.setMonth(parseMonth(bits[1]));
            d.setYear(bits[3]);
            return d;
        }
    },
    // next Tuesday - this is suspect due to weird meaning of "next"
    {   re: /^next (\w+)$/i,
        handler: function(bits) {
            var d = new Date();
            var day = d.getDay();
            var newDay = parseWeekday(bits[1]);
            var addDays = newDay - day;
            if (newDay <= day) {
                addDays += 7;
            }
            d.setDate(d.getDate() + addDays);
            return d;
        }
    },
    // last Tuesday
    {   re: /^last (\w+)$/i,
        handler: function(bits) {
            throw new Error("Not yet implemented");
        }
    },
    // mm/dd/yyyy (American style)
    {   re: /(\d{1,2})\/(\d{1,2})\/(\d{4})/,
        handler: function(bits) {
            var d = new Date();
            d.setYear(bits[3]);
            d.setDate(parseInt(bits[2], 10));
            d.setMonth(parseInt(bits[1], 10) - 1); // Because months indexed from 0
            return d;
        }
    },
    // yyyy-mm-dd (ISO style)
    {   re: /(\d{4})-(\d{1,2})-(\d{1,2})/,
        handler: function(bits) {
            var d = new Date();
            d.setYear(parseInt(bits[1]));
            d.setDate(parseInt(bits[3], 10));
            d.setMonth(parseInt(bits[2], 10) - 1);
            return d;
        }
    },
];

function parseDateString(s) {
    for (var i = 0; i < dateParsePatterns.length; i++) {
        var re = dateParsePatterns[i].re;
        var handler = dateParsePatterns[i].handler;
        var bits = re.exec(s);
        if (bits) {
            return handler(bits);
        }
    }
    throw new Error("Invalid date string" + s);
}

function magicDate(input) {
    var messagespan = input.id + 'Msg';
    try {
        var d = parseDateString(input.value);
        input.value = (d.getMonth() + 1) + '/' + d.getDate() + '/' + d.getFullYear();
        input.className = '';
        // Human readable date
        document.getElementById(messagespan).firstChild.nodeValue = d.toDateString();
        document.getElementById(messagespan).className = 'normal';
    }
    catch (e) {
        input.className = 'error';
        var message = e.message;
        // Fix for IE6 bug
        if (message.indexOf('is null or not an object') > -1) {
            message = 'Invalid date string';
        }
        document.getElementById(messagespan).firstChild.nodeValue = message;
        document.getElementById(messagespan).className = 'error';
    }
}
