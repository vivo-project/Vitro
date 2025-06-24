<!-- $This file is distributed under the terms of the license in LICENSE$ -->
<script language="JavaScript" type="text/javascript">
    function confirmDelete() {
        var msg="Are you SURE you want to delete this record? If in doubt, CANCEL."
        return confirm(msg);
    }

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
        document.getElementById('fileUpload').addEventListener('change', function(event) {
            const file = event.target.files[0];
            if (file && file.type === 'text/css') {
                const reader = new FileReader();
                reader.onload = function(e) {
                    let styleTag = document.getElementById('dynamic-uploaded-css');
                    if (!styleTag) {
                        styleTag = document.createElement('style');
                        styleTag.id = 'dynamic-uploaded-css';
                        document.head.appendChild(styleTag);
                    }
                    styleTag.textContent = e.target.result;
                };
                reader.readAsText(file);
            }
        });

        document.querySelector("[name=_update][type=submit]").addEventListener('click', onSave);

    });

</script>
