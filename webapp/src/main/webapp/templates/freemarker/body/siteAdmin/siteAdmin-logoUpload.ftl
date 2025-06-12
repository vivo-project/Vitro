<div class="logo-upload-container">
    <h2>${i18n().branding_logo_title}</h2>

    <form id="logoUploadForm" action="${actionUpload}" enctype="multipart/form-data" method="post" role="form">
        <!-- Desktop Logo Section -->
        <div class="logo-section">
            <label class="logo-label">${i18n().desktop_logo}</label>
            <div class="logo-preview">
                <img id="portalLogoPreview" src="${logoUrl!}" alt="${i18n().desktop_logo_preview}"
                style="${logoUrl?has_content?then('display: block;', '')}" />
            </div>
            <div class="logo-controls">
                <input type="hidden" id="portalLogoActionInput" name="portalLogoAction" value="keep" />
                <input type="file" name="portalLogo" accept="image/*" id="portalLogoInput" />
                <button type="button" id="portalLogoResetButton">${i18n().reset}</button>
            </div>
        </div>

        <!-- Mobile Logo Section -->
        <div class="logo-section">
            <label class="logo-label">${i18n().mobile_logo}</label>
            <div class="logo-preview">
                <img id="mobilePortalLogoPreview" src="${logoSmallUrl!}" alt="${i18n().mobile_logo_preview}"
                    style="${logoSmallUrl?has_content?then('display: block;', '')}" />
            </div>

            <div class="logo-controls">
                <input type="hidden" id="mobilePortalLogoActionInput" name="mobilePortalLogoAction" value="keep" />
                <input type="file" name="mobilePortalLogo" accept="image/*" id="mobilePortalLogoInput" />
                <button type="button" id="mobilePortalLogoResetButton">${i18n().reset}</button>
            </div>
        </div>


        <div class="button-group">
            <a href="${backLocation}" class="button">${i18n().back}</a>
            <input class="button green" type="submit" value="${i18n().save_changes}" />
        </div>

    </form>

    <script>
        const defaultLogoPlaceholder = ''; // Leave blank or use a base64/placeholder image if needed

        document.getElementById('portalLogoInput').addEventListener('change', function (event) {
            handleFileChange(event, 'portalLogoPreview', 'portalLogoActionInput');
        });

        document.getElementById('portalLogoResetButton').addEventListener('click', function () {
            resetFileInput('portalLogoInput', 'portalLogoPreview', 'portalLogoActionInput');
        });

        document.getElementById('mobilePortalLogoInput').addEventListener('change', function (event) {
            handleFileChange(event, 'mobilePortalLogoPreview', 'mobilePortalLogoActionInput');
        });

        document.getElementById('mobilePortalLogoResetButton').addEventListener('click', function () {
            resetFileInput('mobilePortalLogoInput', 'mobilePortalLogoPreview', 'mobilePortalLogoActionInput');
        });

        function handleFileChange(event, previewId, actionInputId) {
            const file = event.target.files[0];
            const preview = document.getElementById(previewId);
            const actionInput = document.getElementById(actionInputId);

            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    preview.src = e.target.result;
                    preview.style.display = 'block';
                };
                reader.readAsDataURL(file);
                actionInput.value = 'update';
            }
        }

        function resetFileInput(inputId, previewId, actionInputId) {
            const input = document.getElementById(inputId);
            const preview = document.getElementById(previewId);
            const actionInput = document.getElementById(actionInputId);

            input.value = "";
            preview.src = defaultLogoPlaceholder;
            preview.style.display = 'none'; // still show blank box
            actionInput.value = 'reset';
        }
    </script>
</div>
