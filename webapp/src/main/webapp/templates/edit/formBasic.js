<!-- $This file is distributed under the terms of the license in LICENSE$ -->
<script language="JavaScript" type="text/javascript">
    function confirmDelete() {
        var msg="Are you SURE you want to delete this record? If in doubt, CANCEL."
        return confirm(msg);
    }
    
    // SITE THEMES SETTINGS
    function adjustHexColor(hex, percent) {
        if (hex.startsWith("#")) hex = hex.slice(1);
        if (hex.length !== 6) throw new Error("Invalid hex color format.");

        const adjust = (value) => Math.max(0, Math.min(255, Math.round(value + (percent / 100) * (percent > 0 ? (255 - value) : value))));
        const [r, g, b] = [0, 2, 4].map(offset => adjust(parseInt(hex.substring(offset, offset + 2), 16)));

        return `#${[r, g, b].map(value => value.toString(16).padStart(2, "0")).join("")}`;
    }
    
    function toggleVisibility(elementId, visible) {
        document.getElementById(elementId).style.display = visible ? "block" : "none";
    }

    
    function toggleAdvancedColors(show) {
        isShowedAdvancedColors = show;
        toggleVisibility('advancedColorsButton', !show);
        document.querySelectorAll('.advancedThemeColor').forEach(element => {
            element.style.display = show ? "block" : "none";
        });
    }

    function initResetButtons() {
        for (const [inputId, cssVar] of Object.entries(colorPallete)) {
            for (const [resetLinkId, inputIds] of Object.entries(resetLinks)) {
                if (inputIds.includes(inputId)) {
                    const colorInput = document.getElementById(inputId);
                    const resetLink = document.getElementById(resetLinkId);
                    if (colorInput.getAttribute("initial-value") != "null") {
                        toggleResetButton(inputId, true)
                        updateCSSVariable(inputId, cssVar, colorInput.getAttribute("initial-value"));
                    } else {
                        resetColor(inputId);
                        // toggleResetButton(inputId, false);
                    }
                }
            }
        }
        checkIfResetAllIsVisible();

    }

    function checkIfResetAllIsVisible() {
        const hasChanged = Object.keys(colorPallete).some(inputId => document.querySelector(`[name="${inputId}"]`)?.value !== "null");
        toggleVisibility("resetStylesButton", hasChanged);
    }

    function updateCSSVariable(inputId, cssVar, value) {
        document.documentElement.style.setProperty(cssVar, value ? value : 'unset');
        const hiddenInput = document.getElementById(inputId + "Hidden");
        const colorInput = document.getElementById(inputId);

        hiddenInput.setAttribute("name", value ? null : inputId);
        colorInput.setAttribute("name", value ? inputId : null);
    }

    function toggleResetButton(inputId, show) {
        for (const [resetLinkId, inputIds] of Object.entries(resetLinks)) {
            if (inputIds.includes(inputId)) {
                const resetLink = document.getElementById(resetLinkId);
                resetLink.style.display = show ? '' : 'none';
            }
        }
    }

    function resetColor(inputId) {
        const colorInput = document.getElementById(inputId);
        colorInput.value = getDefaultColorValue(inputId);
        updateCSSVariable(inputId, colorPallete[inputId], null);
    }
    
    function handleColorInput(event, inputId, cssVar) {
        const colorValue = event.target.value;
        updateCSSVariable(inputId, cssVar, colorValue);
        toggleResetButton(inputId, true);

        if (inputId === 'ThemePrimaryColor') {
            ["Lighter", "Darker"].forEach(variation => {
                const variationId = `ThemePrimaryColor${variation}`;
                const adjustedValue = adjustHexColor(colorValue, variation === "Lighter" ? 40 : -40);
                document.getElementById(variationId).value = adjustedValue;
                document.getElementById(variationId).dispatchEvent(new Event('input'));
            });

            if (!isShowedAdvancedColors) {
                document.getElementById('ThemeBannerColor').value = colorValue;
                document.getElementById('ThemeBannerColor').dispatchEvent(new Event('input'));
            }
        }

        checkIfResetAllIsVisible();
    }

    function handleResetClick(event, inputIds) {
        event.preventDefault();
        inputIds.forEach(inputId => {
            resetColor(inputId)
            toggleResetButton(inputId, false);

            if (inputId === 'ThemePrimaryColor' && !isShowedAdvancedColors) {
                resetColor('ThemeBannerColor');
                toggleResetButton('ThemeBannerColor', false);
            }
        });
        checkIfResetAllIsVisible();
    }

    function handleThemeChange() {
        Object.keys(colorPallete).forEach(inputId => {
            if (document.querySelector(`[name="${inputId}"]`)?.value === "null") {
                resetColor(inputId);
            }
        });
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
    
        // Initial hide reset buttons Hide 
        Object.keys(resetLinks).forEach(element => {
            document.getElementById(element).style.display = 'none'   
        });

        // Initial show/hide reset links
        initResetButtons();
        
        // Update variable and add reset-option
        Object.entries(colorPallete).forEach(([inputId, cssVar]) => {
            const colorInput = document.getElementById(inputId);
            colorInput.addEventListener('input', event => handleColorInput(event, inputId, cssVar));
        });

        Object.entries(resetLinks).forEach(([resetLinkId, inputIds]) => {
            const resetLink = document.getElementById(resetLinkId);
            resetLink.addEventListener('click', event => handleResetClick(event, inputIds));
        });

        // Advanced button
        document.getElementById('advancedColorsButton').addEventListener('click', () => toggleAdvancedColors(true));
        document.getElementById('hideAdvancedColorsButton').addEventListener('click', () => toggleAdvancedColors(false));

        // On theme change update deafult colors
        document.getElementById("ThemeDir").addEventListener('change', handleThemeChange);

        // Reset all button
        document.getElementById("resetStylesButton").addEventListener('click', event => {
            event.preventDefault();
            Object.keys(resetLinks).forEach(resetLinkId => document.getElementById(resetLinkId).click());
        });

    });

</script>
