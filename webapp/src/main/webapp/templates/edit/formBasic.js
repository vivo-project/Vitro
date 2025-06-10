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

            console.log(colors);
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
                    alert(
                        "Failed to load default theme pallete. Please check if 'theme-config.json' file exists and is accessible."
                    );
                    
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
                    alert("The theme configuration file is not in the expected format. Please verify its structure.");
                    return null;
                }
                console.log("Theme Config JSON:", data);
                return data.defaultBrandingColors;
            })
            .catch((error) => {
                console.error("Error fetching theme-config.json:", error);
                alert("An unexpected error occurred while loading the theme configuration. Please verify its structure.");
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
                    alert("The editor will open once you save your changes.");
                } else {
                    window.location.href = window.location.href.substring(0, window.location.href.lastIndexOf('/'));
                }
            })

        });

    });


</script>
