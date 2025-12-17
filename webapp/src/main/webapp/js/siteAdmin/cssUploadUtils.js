<!-- $This file is distributed under the terms of the license in LICENSE$ -->
<script language="JavaScript" type="text/javascript">

    async function onSave(e) {
        const fileInput = document.getElementById('fileUpload');
        const file = fileInput.files[0];
        if (file) {
            e.preventDefault();
            await saveStyle();
            fileInput.value = '';
            document.querySelector("[name=_update][type=submit]").click();
        }
    }

    async function saveStyle() {
        const fileInput = document.getElementById('fileUpload');
        const file = fileInput.files[0];
        if (!file) {
            return;
        }
        const formData = new FormData();
        formData.append('fileUpload', file);

        await fetch(fromUrls.actionUploadUrl, {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                alert('File upload failed.');
            }
        })
        .catch(error => {
            alert('An error occurred during upload.');
            console.error(error);
        });
    }

    function resetStyles() {
        if (confirm(i18nStrings.confirmRemove)) {
            fetch(fromUrls.actionRemoveUrl, { method: 'POST', credentials: 'same-origin' })
                .then(function () {
                    window.location.reload();
                });
        }
    }

    document.addEventListener('DOMContentLoaded', function() {
        initializeFileUpload();
        initializeFormSubmission();
        setFormEncoding();
    });

    function initializeFileUpload() {
        const fileInput = document.getElementById('fileUpload');
        if (!fileInput) return;

        fileInput.addEventListener('change', handleFileSelection);
    }

    function handleFileSelection(event) {
        const file = event.target.files[0];
        const fileInput = event.target;
        
        removeClearButton();
        
        if (file && file.type === 'text/css') {
            createClearButton(fileInput);
            previewCssFile(file);
        }
    }

    function removeClearButton() {
        const existingClearBtn = document.getElementById('clearFileBtn');
        if (existingClearBtn) {
            existingClearBtn.remove();
        }
    }

    function createClearButton(fileInput) {
        const clearBtn = document.createElement('button');
        clearBtn.id = 'clearFileBtn';
        clearBtn.type = 'button';
        clearBtn.innerHTML = '✕';
        clearBtn.title = 'Remove selected file';
        clearBtn.classList.add('clear-file-btn');
        
        clearBtn.addEventListener('click', function() {
            clearFileSelection(fileInput, clearBtn);
        });
        
        fileInput.parentNode.insertBefore(clearBtn, fileInput.nextSibling);
    }

    function clearFileSelection(fileInput, clearBtn) {
        fileInput.value = '';
        clearBtn.remove();
        removeDynamicCss();
        restoreCustomCss();
    }

    function removeDynamicCss() {
        const dynamicCss = document.getElementById('dynamic-uploaded-css');
        if (dynamicCss) {
            dynamicCss.remove();
        }
    }

    function previewCssFile(file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            removeExistingCustomCss();
            applyDynamicCss(e.target.result);
        };
        reader.readAsText(file);
    }

    function removeExistingCustomCss() {
        const existingCustomCss = document.getElementById('custom-css-path');
        if (existingCustomCss) {
            // Store the custom CSS element for later restoration
            window.storedCustomCss = existingCustomCss.cloneNode(true);
            existingCustomCss.remove();
        }
    }

    function restoreCustomCss() {
        // Restore the previously stored custom CSS if it exists
        if (window.storedCustomCss) {
            document.head.appendChild(window.storedCustomCss);
            window.storedCustomCss = null; // Clear the stored reference
        }
    }

    function applyDynamicCss(cssContent) {
        let styleTag = document.getElementById('dynamic-uploaded-css');
        if (!styleTag) {
            styleTag = document.createElement('style');
            styleTag.id = 'dynamic-uploaded-css';
            document.head.appendChild(styleTag);
        }
        styleTag.textContent = cssContent;
    }

    function initializeFormSubmission() {
        const submitButton = document.querySelector("[name=_update][type=submit]");
        if (submitButton) {
            submitButton.addEventListener('click', onSave);
        }
    }

    function setFormEncoding() {
        const form = document.getElementById('editForm');
        if (form) {
            form.setAttribute('enctype', 'multipart/form-data');
        }
    }


    function showFileInput() {
        document.getElementById('fileUpload').classList.remove('hidden');
        document.getElementById('fileUpload').click();
        document.getElementById('uploadNewAction').classList.add('hidden');
    }
    
</script>
