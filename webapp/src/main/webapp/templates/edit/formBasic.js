<!-- $This file is distributed under the terms of the license in LICENSE$ -->
<script language="JavaScript" type="text/javascript">
function confirmDelete() {
    var msg="Are you SURE you want to delete this record? If in doubt, CANCEL."
    return confirm(msg);
}



function adjustHexColor(hex, percent) {
    // Ensure the hex code is in the correct format
    if (hex.startsWith("#")) hex = hex.slice(1);
    if (hex.length !== 6) throw new Error("Invalid hex color format.");

    // Parse the hex values to integers
    let r = parseInt(hex.substring(0, 2), 16);
    let g = parseInt(hex.substring(2, 4), 16);
    let b = parseInt(hex.substring(4, 6), 16);

    // Adjust each color component by the given percentage
    r = Math.max(0, Math.min(255, Math.round(r + (percent / 100) * (percent > 0 ? (255 - r) : r))));
    g = Math.max(0, Math.min(255, Math.round(g + (percent / 100) * (percent > 0 ? (255 - g) : g))));
    b = Math.max(0, Math.min(255, Math.round(b + (percent / 100) * (percent > 0 ? (255 - b) : b))));

    // Convert the components back to hex and return the result
    const newHex = `#${r.toString(16).padStart(2, "0")}${g.toString(16).padStart(2, "0")}${b.toString(16).padStart(2, "0")}`;
    return newHex;
}

const resetPrimaryColorLink = document.getElementById('resetPrimaryColorLink');

    
let primaryColorChanged = false;
let isShowedAdvancedColors = false;

const colorPallete = {
    "ThemePrimaryColorLighter": "--primary-color-lighter",
    "ThemePrimaryColor": "--primary-color",
    "ThemePrimaryColorDarker": "--primary-color-darker",
    "ThemeBannerColor": "--banner-color",
    "ThemeSecondaryColor": "--secondary-color",
    "ThemeAccentColor": "--accent-color",
    "ThemeTextColor": "--text-color",
    "ThemeLinkColor": "--link-color"
};

const defaultColorPalete = {
    "themes/wilma/": {
        "ThemePrimaryColorLighter": "#7bb3cb",
        "ThemePrimaryColor": "#2485ae",
        "ThemePrimaryColorDarker": "#064d68",
        "ThemeBannerColor": "#023048",
        "ThemeSecondaryColor": "#398aac",
        "ThemeAccentColor": "#749a02",
        "ThemeTextColor": "#5f6464",
        "ThemeLinkColor": "#2485ae"
    },

    "themes/vitro/": {
        "ThemePrimaryColorLighter": "#62b6d7",
        "ThemePrimaryColor": "#47b6d0",
        "ThemePrimaryColorDarker": "#006279",
        "ThemeBannerColor": "#012d3d",
        "ThemeSecondaryColor": "#398aac",
        "ThemeAccentColor": "#0ea5c8",
        "ThemeTextColor": "#595B5B",
        "ThemeLinkColor": "#47b6d0"
    },
    "themes/tenderfoot/": {
        "ThemePrimaryColorLighter": "#8bb9cc",
        "ThemePrimaryColor": "#3E8BAA",
        "ThemePrimaryColorDarker": "#0c0cb5",
        "ThemeBannerColor": "#013049",
        "ThemeSecondaryColor": "#93c3d4",
        "ThemeAccentColor": "#3446A0",
        "ThemeTextColor": "#595B5B",
        "ThemeLinkColor": "#2485ae"
    }

};


const resetLinks = {
    "resetPrimaryColorLink": ["ThemePrimaryColorLighter", "ThemePrimaryColor", "ThemePrimaryColorDarker"],
    "resetBannerColorLink": ["ThemeBannerColor"],
    "resetSecondaryColorLink": ["ThemeSecondaryColor"],
    "resetAccentColorLink": ["ThemeAccentColor"],
    "resetTextColorLink": ["ThemeTextColor"],
    "resetLinkColorLink": ["ThemeLinkColor"],
};

function getDefaultColorValue(element) {
    let theme = document.getElementById('ThemeDir').value;
    return defaultColorPalete[theme]?.[element] || "#FF00FF";
}


document.addEventListener('DOMContentLoaded', function() {


    // Hide 
    Object.keys(resetLinks).forEach(element => {
        document.getElementById(element).style.display = 'none'   
    });
    
    // Update variable and add reset-option
    for (const [inputId, cssVar] of Object.entries(colorPallete)) {
        const colorInput = document.getElementById(inputId);
        colorInput.addEventListener('input', function() {
            const colorValue = this.value;
            document.documentElement.style.setProperty(cssVar, colorValue);

            document.getElementById(inputId + "Hidden").setAttribute("name", null)
            document.getElementById(inputId).setAttribute("name", inputId)


            for (const [resetLinkId, inputIds] of Object.entries(resetLinks)) {
                if (inputIds.includes(inputId)) {
                    const resetLink = document.getElementById(resetLinkId);
                    resetLink.style.display = "";
                }
            }


            // Auto setup light/dark variations
            if (this.id == 'ThemePrimaryColor'){
                document.getElementById('ThemePrimaryColorLighter').value = adjustHexColor(colorValue, 40);
                document.getElementById('ThemePrimaryColorDarker').value = adjustHexColor(colorValue, -40);

                document.getElementById('ThemePrimaryColorLighter').dispatchEvent(new Event('input'));
                document.getElementById('ThemePrimaryColorDarker').dispatchEvent(new Event('input'));


                if (!isShowedAdvancedColors) {
                    document.getElementById('ThemeBannerColor').value = colorValue;
                    document.getElementById('ThemeBannerColor').dispatchEvent(new Event('input'));
                }
            }

        });

    
    }
    
    // Link reset button
    for (const [resetLinkId, inputIds] of Object.entries(resetLinks)) {
        const resetLink = document.getElementById(resetLinkId);
        resetLink.addEventListener('click', function(event) {
            event.preventDefault();
            inputIds.forEach(function(inputId) {
                const colorInput = document.getElementById(inputId);
                colorInput.value = getDefaultColorValue(inputId);
                document.getElementById(inputId + "Hidden").setAttribute("name", inputId)
                document.getElementById(inputId).setAttribute("name", null)

                document.documentElement.style.setProperty(colorPallete[inputId], 'unset');
                
                if (!isShowedAdvancedColors && resetLinkId == 'resetPrimaryColorLink') {
                    document.getElementById('resetBannerColorLink').click()
                }
            });
            resetLink.style.display = 'none';
        });
    }

    // Initial show/hide reset links
    for (const [inputId, cssVar] of Object.entries(colorPallete)) {

        for (const [resetLinkId, inputIds] of Object.entries(resetLinks)) {
            if (inputIds.includes(inputId)) {
                const colorInput = document.getElementById(inputId);
                
                const resetLink = document.getElementById(resetLinkId);
                if (colorInput.getAttribute("initial-value") != "null") {
                    resetLink.style.display = "";
                    document.getElementById(inputId + "Hidden").setAttribute("name", null)
                    document.getElementById(inputId).setAttribute("name", inputId)
                } else {
                    resetLink.click();
                }
            }
        }
    }
    
    // Advanced button
    const advancedThemeColors = document.querySelectorAll('.advancedThemeColor');
    const advancedColorsButton = document.getElementById('advancedColorsButton');
    const hideAdvancedColorsButton = document.getElementById('hideAdvancedColorsButton');
    advancedColorsButton.addEventListener('click', function(event) {
        isShowedAdvancedColors = true;
        advancedColorsButton.style.display = 'none'
        event.preventDefault();
        advancedThemeColors.forEach(function(advancedThemeColor) {
            advancedThemeColor.style.display = 'block';
        });
    });

    hideAdvancedColorsButton.addEventListener('click', function(event) {
        isShowedAdvancedColors = false;
        advancedColorsButton.style.display = 'block'
        event.preventDefault();
        advancedThemeColors.forEach(function(advancedThemeColor) {
            advancedThemeColor.style.display = 'none';
        });
    });

});

</script>
