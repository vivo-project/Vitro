/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/*
    ORNG Shindig Helper functions for gadget-to-container commands
 */
 
 // dummy function so google analytics does not break for institutions who do not use it
 
_gaq = {};
_gaq.push = function(data) {    // 
 };

// pubsub
gadgets.pubsubrouter.init(function(id) {
    return my.gadgets[shindig.container.gadgetService.getGadgetIdFromModuleId(id)].url; 
  }, {
    onSubscribe: function(sender, channel) {
      setTimeout("my.onSubscribe('" + sender + "', '" + channel + "')", 3000);
      // return true to reject the request.
      return false;
    },
    onUnsubscribe: function(sender, channel) {
      //alert(sender + " unsubscribes from channel '" + channel + "'");
      // return true to reject the request.
      return false;
    },
    onPublish: function(sender, channel, message) {
      // return true to reject the request.
      
      // track with google analytics
      if (sender != '..' ) {
          var moduleId = shindig.container.gadgetService.getGadgetIdFromModuleId(sender);
      }
      
      if (channel == 'VISIBLE') {
	      var statusId = document.getElementById(sender + '_status');	      
          if (statusId) {
            // only act on these in HOME view since they are only  meant to be seen when viewer=owner
            if (my.gadgets[moduleId].view != 'home') {
                return true;
            }
            if (message == 'Y') {
/*		        statusId.style.color = 'GREEN';
		        statusId.innerHTML = 'This section is VISIBLE';
		        if (my.gadgets[moduleId].visible_scope == 'U') {
		            statusId.innerHTML += ' to UCSF';
		        }
		        else {
		            statusId.innerHTML += ' to the public';
		        }
*/
                /*  changed the gui here -- tlw72 */
                statusId.style.color = '#5e6363';
                statusId.innerHTML = 'public';
            }
            else {
/*		        statusId.style.color = '#CC0000';
		        statusId.innerHTML = 'This section is HIDDEN';
		        if (my.gadgets[moduleId].visible_scope == 'U') {
		            statusId.innerHTML += ' from UCSF';
		        }
		        else {
		            statusId.innerHTML += ' from the public';
		        }
*/
            /*  changed the gui here -- tlw72 */
	            statusId.style.color = '#5e6363';
	            statusId.innerHTML = 'private';
            }
         }
      }
      else if (channel == 'added' && my.gadgets[moduleId].view == 'home') {
          if (message == 'Y') {
            _gaq.push(['_trackEvent', my.gadgets[moduleId].name, 'SHOW', 'profile_edit_view']);    
            // find out whose page we are on, if any
        	var userId = gadgets.util.getUrlParameters()['uri'] || document.URL.replace('/display/', '/individual/');
            osapi.activities.create(
		    { 	'userId': userId,
			    'appId': my.gadgets[moduleId].appId,
			    'activity': {'postedTime': new Date().getTime(), 'title': 'added a gadget', 'body': 'added the ' + my.gadgets[moduleId].name + ' gadget to their profile' }
		    }).execute(function(response){});
		  }
		  else {
            _gaq.push(['_trackEvent', my.gadgets[moduleId].name, 'HIDE', 'profile_edit_view']);    
		  }
      }
      else if (channel == 'status') {
          // message should be of the form 'COLOR:Message Content'
	      var statusId = document.getElementById(sender + '_status');	      
          if (statusId) {
            var messageSplit = message.split(':');
            if (messageSplit.length == 2) {
		        statusId.style.color = messageSplit[0];
		        statusId.innerHTML = messageSplit[1];
		    }
		    else {
		        statusId.innerHTML = message;
		    }
	      }
      }
      else if (channel == 'analytics') {
          // publish to google analytics
          // message should be JSON encoding object with required action and optional label and value 
          // as documented here: http://code.google.com/apis/analytics/docs/tracking/eventTrackerGuide.html
          // note that event category will be set to the gadget name automatically by this code
          // Note: message will be already converted to an object 
          if (message.hasOwnProperty('value')) {
            _gaq.push(['_trackEvent', my.gadgets[moduleId].name, message.action, message.label, message.value]);    
          }
          else if (message.hasOwnProperty('label')) {
            _gaq.push(['_trackEvent', my.gadgets[moduleId].name, message.action, message.label]);    
          }
          else {
            _gaq.push(['_trackEvent', my.gadgets[moduleId].name, message.action]);    
          }
      }
      else if (channel == 'profile') {
          _gaq.push(['_trackEvent', my.gadgets[moduleId].name, 'go_to_profile', message]);    
          document.location.href = '/' + location.pathname.split('/')[1] + '/display/n' + message;
	  }
      else if (channel == 'hide') {
    	  document.getElementById(sender).parentNode.parentNode.style.display = 'none';
   	  }
      else if (channel == 'JSONPersonIds' || channel == 'JSONPubMedIds') {
          // do nothing, no need to alert
	  }
      else {
	      alert(sender + " publishes '" + message + "' to channel '" + channel + "'");
	  }
      return false;
    }
});

// helper functions
my.findGadgetsAttachingTo = function(chromeId) {
    var retval = [];
    for (var i = 0; i < my.gadgets.length; i++) {
        if (my.gadgets[i].chrome_id == chromeId) {
            retval[retval.length] = my.gadgets[i];
        }
    }
    return retval;
};
    
my.removeGadgets = function(gadgetsToRemove) {
    for (var i = 0; i < gadgetsToRemove.length; i++) {
        for (var j = 0; j < my.gadgets.length; j++) {
            if (gadgetsToRemove[i].url == my.gadgets[j].url) {
                my.gadgets.splice(j, 1);
                break;
            }
        }
    }
};

my.onSubscribe = function(sender, channel) {     
     // lookup pubsub data based on channel and if a match is found, publish the data to that channel after a delay
     if (my.pubsubData[channel]) {
        gadgets.pubsubrouter.publish(channel, my.pubsubData[channel]);
     }
     else {
        alert(sender + " subscribes to channel '" + channel + "'");
     }
     //PageMethods.onSubscribe(sender, channel, my.pubsubHint, my.CallSuccess, my.CallFailed);
};

my.removeParameterFromURL = function(url, parameter) {
    var urlparts= url.split('?');   // prefer to use l.search if you have a location/link object
    if (urlparts.length>=2) {
        var prefix= encodeURIComponent(parameter)+'=';
        var pars= urlparts[1].split(/[&;]/g);
        for (var i= pars.length; i-->0;)               //reverse iteration as may be destructive
            if (pars[i].lastIndexOf(prefix, 0)!==-1)   //idiom for string.startsWith
                pars.splice(i, 1);
        url= urlparts[0]+'?'+pars.join('&');
    }
    return url;
};

 // publish the people
my.CallSuccess = function(result) {    
     gadgets.pubsubrouter.publish('person', result);
};
 
 // alert message on some failure
my.CallFailed = function(error) {    
     alert(error.get_message());
};

my.requestGadgetMetaData = function(view, opt_callback) {
    var request = {
      context: {
        country: "default",
        language: "default",
        view: view,
	    ignoreCache : my.noCache,
        container: "default"
      },
      gadgets: []
    };

    for (var moduleId = 0; moduleId < my.gadgets.length; moduleId++) {
      // only add those with matching views
      if (my.gadgets[moduleId].view == view) {
	    request.gadgets[request.gadgets.length] = {'url': my.gadgets[moduleId].url, 'moduleId': moduleId};
	  }
    }    

    var makeRequestParams = {
      "CONTENT_TYPE" : "JSON",
      "METHOD" : "POST",
      "POST_DATA" : gadgets.json.stringify(request)};

    gadgets.io.makeNonProxiedRequest(my.openSocialURL + "/gadgets/metadata",
      function(data) {
        data = data.data;
        if (opt_callback) {
            opt_callback(data);
        }
      },
      makeRequestParams,
      "application/javascript"
    );
};

my.renderableGadgets = [];

my.generateGadgets = function (metadata) {
    // put them in moduleId order
    for (var i = 0; i < metadata.gadgets.length; i++) {
        var moduleId = metadata.gadgets[i].moduleId;
        // Notes by Eric.  Not sure if I should have to calculate this myself, but I will.
        var height = metadata.gadgets[i].height;
        var width = metadata.gadgets[i].width;
        var viewPrefs = metadata.gadgets[i].views[my.gadgets[moduleId].view];
        if (viewPrefs) {
            height = viewPrefs.preferredHeight || height;
            width = viewPrefs.preferredWidth || width;
        }

        var opt_params = { 'specUrl': metadata.gadgets[i].url, 'secureToken': my.gadgets[moduleId].secureToken,
            'title': metadata.gadgets[i].title, 'userPrefs': metadata.gadgets[i].userPrefs,
            'height': height, 'width': width, 'debug': my.debug};

        // do a shallow merge of the opt_params from the database.  This will overwrite anything with the same name, and we like that 
        for (var attrname in my.gadgets[moduleId].opt_params) {
            opt_params[attrname] = my.gadgets[moduleId].opt_params[attrname]; 
        }

        my.renderableGadgets[moduleId] = shindig.container.createGadget(opt_params);
        // set the metadata for easy access
        my.renderableGadgets[moduleId].setMetadata(metadata.gadgets[i]);
    }
    // this will be called multiple times, only render when all gadgets have been processed
    var ready = my.renderableGadgets.length == my.gadgets.length;
    for (var i = 0; ready && i < my.renderableGadgets.length; i++) {
        if (!my.renderableGadgets[i]) {
            ready = false;
        }
    }

    if (ready) {
        shindig.container.addGadgets(my.renderableGadgets);
        shindig.container.renderGadgets();
    }
};

my.init = function() {
    // overwrite this RPC function.  Do it at this level so that rpc.f (this.f) is accessible for getting module ID
//    gadgets.rpc.register('requestNavigateTo', doProfilesNavigation);
	shindig.container = new ORNGContainer();

	shindig.container.gadgetService = new ORNGGadgetService();
	shindig.container.layoutManager = new ORNGLayoutManager();    

	shindig.container.setNoCache(my.noCache);

	// since we render multiple views, we need to do somethign fancy by swapping out this value in getIframeUrl
	shindig.container.setView('REPLACE_THIS_VIEW');

	  // do multiple times as needed if we have multiple views
    // find out what views are being used and call requestGadgetMetaData for each one
    var views = {};
    for (var moduleId = 0; moduleId < my.gadgets.length; moduleId++) {
        var view = my.gadgets[moduleId].view;
        if (!views[view]) {
            views[view] = view;
            my.requestGadgetMetaData(view, my.generateGadgets);
        }
    }
};

//ORNGContainer
ORNGContainer = function() {
  shindig.IfrContainer.call(this);
};

ORNGContainer.inherits(shindig.IfrContainer);

ORNGContainer.prototype.createGadget = function (opt_params) {
    if (opt_params.gadget_class) {
        return new window[opt_params.gadget_class](opt_params);
    }
    else {
        return new ORNGGadget(opt_params);
    }
}

//ORNGLayoutManager. 
ORNGLayoutManager = function() {
	shindig.LayoutManager.call(this);
};

ORNGLayoutManager.inherits(shindig.LayoutManager);

ORNGLayoutManager.prototype.getGadgetChrome = function(gadget) {
	var layoutRoot = document.getElementById(my.gadgets[gadget.id].chrome_id);
	if (layoutRoot) {
		var chrome = document.createElement('div');
		chrome.className = 'gadgets-gadget-chrome';
		layoutRoot.appendChild(chrome);
		return chrome;
	} else {
		return null;
	}
};

//ORNGGadgetService
ORNGGadgetService = function() {
    shindig.IfrGadgetService.call(this);
};

ORNGGadgetService.inherits(shindig.IfrGadgetService);

ORNGGadgetService.prototype.setTitle = function (title) {
    var moduleId = shindig.container.gadgetService.getGadgetIdFromModuleId(this.f);
    if (my.gadgets[moduleId].view == 'canvas') {
        ORNGGadgetService.setCanvasTitle(title);
    }
    else {    	
    	var element = document.getElementById(this.f + '_title');
       	element.innerHTML = my.renderableGadgets[moduleId].getTitleHtml(title); 
    }
};

ORNGGadgetService.setCanvasTitle = function (title) {
    document.getElementById("gadgets-title").innerHTML = title.replace(/&/g, '&amp;').replace(/</g, '&lt;');
}

ORNGGadgetService.prototype.requestNavigateTo = function(view, opt_params) {
    var urlTemplate = gadgets.config.get('views')[view].urlTemplate;
    var url = urlTemplate || 'OpenSocial.aspx?';

    url += window.location.search.substring(1);
    
    // remove appId if present
    url = my.removeParameterFromURL(url, 'appId');
    
    // Add appId if the URL Template begins with the word 'Gadget'
    if (urlTemplate.indexOf('Gadget') == 0) {
        var moduleId = shindig.container.gadgetService.getGadgetIdFromModuleId(this.f);
        var appId = my.gadgets[moduleId].appId;
        url += '&appId=' + appId;    
    }

	if (opt_params) {
		var paramStr = gadgets.json.stringify(opt_params);
		if (paramStr.length > 0) {
			url += '&appParams=' + encodeURIComponent(paramStr);
		}
	}
	if (url && document.location.href.indexOf(url) == -1) {
		document.location.href = url;
	}
};

//ORNGGadget
ORNGGadget = function(opt_params) {
    shindig.BaseIfrGadget.call(this, opt_params);
    this.debug = my.debug;
    this.serverBase_ = my.openSocialURL + "/gadgets/";
    var gadget = this;
    var subClass = shindig.IfrGadget;
    this.metadata = {};
    for (var name in subClass) if (subClass.hasOwnProperty(name)) {
        if (name == 'getIframeUrl') {
            // we need to keep this old one
            gadget['originalGetIframeUrl'] = subClass[name];
        }
        else if (name != 'finishRender') {
          gadget[name] = subClass[name];
        }
    }
};

ORNGGadget.inherits(shindig.BaseIfrGadget);

ORNGGadget.prototype.setMetadata = function(metadata) {
   this.metadata = metadata;
};

ORNGGadget.prototype.hasFeature = function(feature) {
    for (var i = 0; i < this.metadata.features.length; i++) {
        if (this.metadata.features[i] == feature) {
            return true;
        }
    }
    return false;
};

ORNGGadget.prototype.getAdditionalParams = function() {
   var params = '';
   for (var key in my.gadgets[this.id].additionalParams) {
   	params += '&' + key + '=' + my.gadgets[this.id].additionalParams[key];
   }
   return params;
};

ORNGGadget.prototype.getIframeUrl = function() {
    var url = this.originalGetIframeUrl();
    return url.replace('REPLACE_THIS_VIEW', my.gadgets[this.id].view);
};

ORNGGadget.prototype.getTitleHtml = function(title) {
	return  title ? title.replace(/&/g, '&amp;').replace(/</g, '&lt;') : 'Gagdget';
};

ORNGGadget.prototype.getTitleBarContent = function(continuation) {
	  if (my.gadgets[this.id].view == 'canvas') {
	    ORNGGadgetService.setCanvasTitle(this.title);
	    continuation('<span class="gadgets-gadget-canvas-title"></span>');
	  }
	  else {
	    continuation(
	      '<div id="' + this.cssClassTitleBar + '-' + this.id +
	      '" class="' + this.cssClassTitleBar + '"><span class="' +
	      this.cssClassTitleButtonBar + '">' + 
	      '</span> <span id="' +
	      this.getIframeId() + '_title" class="' + this.cssClassTitle + '">' + 
	      this.getTitleHtml(this.title) + '</span><span id="' + 
		  this.getIframeId() + '_status" class="gadgets-gadget-status"></span></div>');
	  }
};

ORNGGadget.prototype.finishRender = function(chrome) {
	window.frames[this.getIframeId()].location = this.getIframeUrl();
	if (chrome && this.width) {
		// set the gadget box width, and remember that we always render as open
		chrome.style.width = this.width + 'px';
	}
};

// ORNGToggleGadget
ORNGToggleGadget = function(opt_params) {
    ORNGGadget.call(this, opt_params);
};

ORNGToggleGadget.inherits(ORNGGadget);

ORNGToggleGadget.prototype.handleToggle = function (track) {
    var gadgetIframe = document.getElementById(this.getIframeId());
    if (gadgetIframe) {
        var gadgetContent = gadgetIframe.parentNode;
        var gadgetImg = document.getElementById('gadgets-gadget-title-image-'
				+ this.id);
        if (gadgetContent.style.display) {
            //OPEN
            if (this.width) {
                gadgetContent.parentNode.style.width = this.width + 'px';
            }

            gadgetContent.style.display = '';
            gadgetImg.src = '/' + location.pathname.split('/')[1] + '/themes/wilma/images/green_minus_sign.gif'
            // refresh if certain features require so
            //if (this.hasFeature('dynamic-height')) {
            if (my.gadgets[this.id].chrome_id == 'gadgets-search') {
                this.refresh();
                document.getElementById(this.getIframeId()).contentWindow.location
						.reload(true);
            }

            if (my.gadgets[this.id].view == 'home') {
                if (track) {
                    // record in google analytics     
                    _gaq.push(['_trackEvent', my.gadgets[this.id].name,
						'OPEN_IN_EDIT', 'profile_edit_view']);
                }
            } else {
                // only do this for user centric activities
                if (gadgets.util.getUrlParameters()['Person'] != undefined) {
                    osapi.activities
							.create(
									{
									    'userId': gadgets.util
												.getUrlParameters()['Person'],
									    'appId': my.gadgets[this.id].appId,
									    'activity': {
									        'postedTime': new Date().getTime(),
									        'title': 'gadget was viewed',
									        'body': my.gadgets[this.id].name
													+ ' gadget was viewed'
									    }
									}).execute(function (response) {
									});
                }
                if (track) {
                    // record in google analytics     
                    _gaq.push(['_trackEvent', my.gadgets[this.id].name, 'OPEN']);
                }
            }
        } else {
            //CLOSE
            if (this.closed_width) {
                gadgetContent.parentNode.style.width = this.closed_width + 'px';
            }

            gadgetContent.style.display = 'none';
            gadgetImg.src = '/' + location.pathname.split('/')[1] + '/themes/wilma/images/green_plus_sign.gif'
            if (track) {
                if (my.gadgets[this.id].view == 'home') {
                    // record in google analytics     
                    _gaq.push(['_trackEvent', my.gadgets[this.id].name,
							'CLOSE_IN_EDIT', 'profile_edit_view']);
                } else {
                    // record in google analytics     
                    _gaq.push(['_trackEvent', my.gadgets[this.id].name, 'CLOSE']);
                }
            }
        }
    }
};

ORNGToggleGadget.prototype.getTitleHtml = function(title) {
	return '<a href="#" onclick="shindig.container.getGadget('
		+ this.id + ').handleToggle(true);return false;">'
		+ (title ? title.replace(/&/g, '&amp;').replace(/</g, '&lt;') : 'Gadget') + '</a>';
};


ORNGToggleGadget.prototype.getTitleBarContent = function(continuation) {
	if (my.gadgets[this.id].view == 'canvas') {
	    ORNGGadgetService.setCanvasTitle(title);
		continuation('<span class="gadgets-gadget-canvas-title"></span>');
	} else {
		continuation('<div id="'
				+ this.cssClassTitleBar
				+ '-'
				+ this.id
				+ '" class="'
				+ this.cssClassTitleBar
				+ '"><span class="'
				+ this.cssClassTitleButtonBar
				+ '">'
				+ '<a href="#" onclick="shindig.container.getGadget('
				+ this.id
				+ ').handleToggle(true);return false;" class="'
				+ this.cssClassTitleButton
				+ '"><img id="gadgets-gadget-title-image-'
				+ this.id
				+ '" src="/' + location.pathname.split('/')[1] + '/themes/wilma/images/green_minus_sign.gif"/></a></span> <span id="'
				+ this.getIframeId() + '_title" class="' + this.cssClassTitle
				+ '">' + this.getTitleHtml(this.title)
				+ '</span><span id="' + this.getIframeId()
				+ '_status" class="gadgets-gadget-status"></span></div>');
	}
};

ORNGToggleGadget.prototype.finishRender = function(chrome) {
	window.frames[this.getIframeId()].location = this.getIframeUrl();
	if (this.start_closed) {
		this.handleToggle(false);
	} 
	else if (chrome && this.width) {
		chrome.style.width = this.width + 'px';
	}
};

