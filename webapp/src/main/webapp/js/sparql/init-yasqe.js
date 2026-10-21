YASQE.defaults.createShareLink = false;
var queryArea = document.getElementById('query-area');
var yasqe = YASQE.fromTextArea(queryArea);
// YASQE creates its own input field without copying the textarea's label.
yasqe.getInputField().setAttribute('aria-labelledby', queryArea.getAttribute('aria-labelledby'));
