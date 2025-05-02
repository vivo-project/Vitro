<div class="logo-upload-container">
    <h2>Logo Configuration</h2>

    <form id="logoUploadForm" action="/vivo/site-branding-logo?action=upload" enctype="multipart/form-data" method="post" role="form">

        <!-- Desktop Logo Section -->
        <div class="logo-section">
            <label class="logo-label">Desktop Logo</label>
            <div class="logo-preview">
                <img id="portalLogoPreview" src="${logoUrl!}" alt="Desktop Logo Preview"
                style="${logoUrl?has_content?then('display: block;', '')}" />
            </div>
            <div class="logo-controls">
                <input type="hidden" id="portalLogoActionInput" name="portalLogoAction" value="keep" />
                <input type="file" name="portalLogo" accept="image/*" id="portalLogoInput" />
                <button type="button" id="portalLogoResetButton">Reset</button>
            </div>
        </div>

        <!-- Mobile Logo Section -->
        <div class="logo-section">
            <label class="logo-label">Mobile Logo</label>
            <div class="logo-preview">
                <img id="mobilePortalLogoPreview" src="${logoSmallUrl!}" alt="Mobile Logo Preview"
                    style="${logoSmallUrl?has_content?then('display: block;', '')}" />
            </div>

            <div class="logo-controls">
                <input type="hidden" id="mobilePortalLogoActionInput" name="mobilePortalLogoAction" value="keep" />
                <input type="file" name="mobilePortalLogo" accept="image/*" id="mobilePortalLogoInput" />
                <button type="button" id="mobilePortalLogoResetButton">Reset</button>
            </div>
        </div>


        <div class="button-group">
            <a href="${backLocation}" class="button">Back</a>
            <input class="submit" type="submit" value="Save Changes" />
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
