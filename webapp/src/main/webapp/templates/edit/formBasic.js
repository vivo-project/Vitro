<!-- $This file is distributed under the terms of the license in LICENSE$ -->
<script language="JavaScript" type="text/javascript">
    function confirmDelete() {
        var msg="Are you SURE you want to delete this record? If in doubt, CANCEL."
        return confirm(msg);
    }
    
    var oldTheme = null;
    var themeChanged = false;
    var openThemeEditorOnSave = false;
    var themeBrandingColors = {};
    
    function handleThemeChange() {
        var theme = document.getElementById('ThemeDir').value;

        if (oldTheme != theme) {
            themeChanged = true;
        } else {
            themeChanged = false;
        }

        getThemeColors(theme).done(function(colors) {

            if (Object.keys(colors).length > 0) {
                themeBrandingColors = colors
                document.getElementById('themeChangeIndicator').style.display = 'inline-block';
            } else {
                themeBrandingColors = {}
                document.getElementById('themeChangeIndicator').style.display = 'none'; 
            }

        }).fail(function(error) {
            console.error('Error loading colors:', error);
        });
    }

    function getThemeColors(theme) {
        return $.ajax({
            url: baseUrl + "/siteBranding",
            dataType: 'json',
            type: 'GET',
            data: {
                theme: theme,
            }
        })
    }

    async function onSave(e) {
        if (isLogoChanges()) {
            e.preventDefault();
            await saveLogoInput();

            resetLogoInputActions();
            document.querySelector("[name=_update][type=submit]").click();
            return;
        }


        if (openThemeEditorOnSave) {
            await openEditor();
            openThemeEditorOnSave = false;
        }
    }

    function getThemeDefaultColors(themeDir) {
        return fetch(themeDir)
            .then((response) => {
                if (!response.ok) {
                    console.error("Error fetching theme-config.json");
                    alert(globalI18nStrings.brandingColorsErrorFetchConfig);
                    
                    return null;
                }
                return response.json();
            })
            .then((data) => {
                if (!data) return null;
                if (
                    !data ||
                    !data.defaultBrandingColors
                ) {
                    console.error("Invalid theme-config.json format");
                    alert(globalI18nStrings.brandingColorsErrorFormatConfig);
                    return null;
                }
                return data.defaultBrandingColors;
            })
            .catch((error) => {
                console.error("Error fetching theme-config.json:", error);
                alert(globalI18nStrings.brandingColorsErrorUnexpectedConfig);
            });
    } 

    async function openEditor() {
        let baseUrl = window.location.href.substring(0, window.location.href.lastIndexOf('/'));
        let themeDir = document.getElementById('ThemeDir').value;
        let themeConfigUrl = baseUrl + "/" + themeDir + "theme-config.json";
        let defaultColors = await getThemeDefaultColors(themeConfigUrl);

        let data = {
                enabled: true,
                lastUrl: window.location.href,
                script: baseUrl + "/js/brandingColors.js",
                colors: themeBrandingColors,
                theme: document.getElementById('ThemeDir').value,
                defaultColors: defaultColors
            }

        localStorage.setItem('colorSchemeEditor', JSON.stringify(data));

        return data
    }

    document.addEventListener('DOMContentLoaded', function() {
    
       
        oldTheme = document.getElementById('ThemeDir').value;
        handleThemeChange();

        document.getElementById("ThemeDir").addEventListener('change', handleThemeChange);

        document.querySelector("[name=_update][type=submit]").addEventListener('click', onSave);

        document.getElementById('changeColorsButton').addEventListener('click', (event) => {
            event.preventDefault();
            openEditor().then(() => {
                if (themeChanged) {
                    let storedData = JSON.parse(localStorage.getItem('colorSchemeEditor')) || {};
                    storedData.enabled = false;
                    openThemeEditorOnSave = true;
                    localStorage.setItem('colorSchemeEditor', JSON.stringify(storedData));
                    alert(globalI18nStrings.brandingColorsOpenAfterSave);
                } else {
                    window.location.href = window.location.href.substring(0, window.location.href.lastIndexOf('/'));
                }
            })

        });


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

    });

    // Upload logo
    const defaultLogoPlaceholder = '';

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

    async function saveLogoInput() {
        const formData = new FormData();

        // Desktop logo
        const portalLogoInput = document.getElementById('portalLogoInput');
        const portalLogoAction = document.getElementById('portalLogoActionInput').value;
        if (portalLogoInput.files.length > 0) {
            formData.append('portalLogo', portalLogoInput.files[0]);
        }
        formData.append('portalLogoAction', portalLogoAction);

        // Mobile logo
        const mobilePortalLogoInput = document.getElementById('mobilePortalLogoInput');
        const mobilePortalLogoAction = document.getElementById('mobilePortalLogoActionInput').value;
        if (mobilePortalLogoInput.files.length > 0) {
            formData.append('mobilePortalLogo', mobilePortalLogoInput.files[0]);
        }
        formData.append('mobilePortalLogoAction', mobilePortalLogoAction);

        // Send request
        await fetch(fromUrls.actionLogoUploadUrl, {
            method: 'POST',
            body: formData,
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Upload failed');
            }
        })
        .catch(error => {
            alert("error");
            console.error(error);
        });
    }

    function isLogoChanges() {
        const portalLogoAction = document.getElementById('portalLogoActionInput').value;
        const mobilePortalLogoAction = document.getElementById('mobilePortalLogoActionInput').value;

        if (portalLogoAction === "keep" && mobilePortalLogoAction === "keep") {
            return false;
        }
        return true;
    }

    function resetLogoInputActions() {
        document.getElementById('portalLogoActionInput').value = 'keep';
        document.getElementById('mobilePortalLogoActionInput').value = 'keep';
    }



</script>
