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

    
    
    
    function onSave() {
        if (openThemeEditorOnSave) {

            openEditor();

        }
    }
    
    function openEditor() {
        let data = {
                enabled: true,
                lastUrl: window.location.href,
                colors: themeBrandingColors,
                theme: document.getElementById('ThemeDir').value
            }

        localStorage.setItem('colorSchemeEditor', JSON.stringify(data));

        return data

    }

    document.addEventListener('DOMContentLoaded', function() {
    
       
        oldTheme = document.getElementById('ThemeDir').value;
        handleThemeChange();

        document.getElementById("ThemeDir").addEventListener('change', handleThemeChange);

        document.querySelector("[name=_update][type=submit]").addEventListener('click', onSave);
        // document.querySelector("[name=_cancel][type=submit]").addEventListener('click', onCancel);

        document.getElementById('changeColorsButton').addEventListener('click', (event) => {
            event.preventDefault();
            let data = openEditor()

            if (themeChanged) {
                let storedData = JSON.parse(localStorage.getItem('colorSchemeEditor')) || {};
                storedData.enabled = false;
                openThemeEditorOnSave = true;
                localStorage.setItem('colorSchemeEditor', JSON.stringify(storedData));
                alert("The editor will open once you save your changes.");
            } else {
                window.location.href = data.lastUrl.substring(0, data.lastUrl.lastIndexOf('/'));
            }
        });

    });


</script>
