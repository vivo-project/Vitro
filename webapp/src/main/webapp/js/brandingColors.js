/* $This file is distributed under the terms of the license in LICENSE$ */

$.extend(this, globalI18nStrings);

const colorNameConvertor = {
    "primary-lighter": "themePrimaryColorLighter",
    "primary-base": "themePrimaryColor",
    "primary-darker": "themePrimaryColorDarker",
    "banner-base": "themeBannerColor",
    "secondary-base": "themeSecondaryColor",
    "accent-base": "themeAccentColor",
    "text-base": "themeTextColor",
    "link-base": "themeLinkColor"
}

const colorNameConvertorInverse = Object.keys(colorNameConvertor).reduce((acc, key) => {
        acc[colorNameConvertor[key]] = key;
        return acc;
    }, {})

const colorPallete = {
    "primary-lighter": "--primary-color-lighter",
    "primary-base": "--primary-color",
    "primary-darker": "--primary-color-darker",
    "banner-base": "--banner-color",
    "secondary-base": "--secondary-color",
    "accent-base": "--accent-color",
    "text-base": "--text-color",
    "link-base": "--link-color"
};

const resetLinks = {
    "primary": ["primary-lighter", "primary-base", "primary-darker"],
    "banner": ["banner-base"],
    "secondary": ["secondary-base"],
    "accent": ["accent-base"],
    "text": ["text-base"],
    "link": ["link-base"],
};

$(document).ready(function(){
    showColorSchemeEditor();

    function getSchemaData() {
        return JSON.parse(localStorage.getItem('colorSchemeEditor'));
    }

    function saveDateToLocalStorage() {
        let data = getSchemaData();
        Object.keys(colorPallete).forEach(function(color) {
            const colorInput = $('#' + color + '-color');
            data.colors[colorNameConvertor[color]] = colorInput.attr('default-color') === 'true' ? undefined : colorInput.val();
        });
        localStorage.setItem('colorSchemeEditor', JSON.stringify(data));        
    }

    function loadDateFromLocalStorage() {
        let data = getSchemaData();
        Object.keys(data.colors).forEach(function(color) {
            let colorFormated = colorNameConvertorInverse[color]; // Convert format from themePrimaryColor to primary-base etc.

            if (!data.colors[color]) return;
            $('#' + colorFormated + '-color').val(data.colors[color]);
            handleColorInput(colorFormated.split('-')[0], colorFormated.split('-')[1], data.colors[color], false);
        });
    }

    function toggleResetButton(inputId, show) {
        const resetLink = document.getElementById(inputId);
        resetLink.style.display = show ? '' : 'none';
    }

    // SITE THEMES SETTINGS
    function adjustHexColor(hex, percent) {
        if (hex.startsWith("#")) hex = hex.slice(1);
        if (hex.length !== 6) throw new Error("Invalid hex color format.");

        const adjust = (value) => Math.max(0, Math.min(255, Math.round(value + (percent / 100) * (percent > 0 ? (255 - value) : value))));
        const [r, g, b] = [0, 2, 4].map(offset => adjust(parseInt(hex.substring(offset, offset + 2), 16)));

        return `#${[r, g, b].map(value => value.toString(16).padStart(2, "0")).join("")}`;
    }
    
    function resetColor(inputId) {
        let pallete = getSchemaData().defaultColors || {};

        $('#' + inputId + '-color').val(pallete[inputId]);
        $('#' + inputId + '-color').attr('default-color', true);
        $('#' + inputId + '-color').attr('user-changed', false);

        updateCSSVariable(colorPallete[inputId], null);
    }

    function handleResetButton(buttonName) {
        resetLinks[buttonName].forEach(inputId => {
            resetColor(inputId);
        });

        toggleResetButton(buttonName + "-reset", false);
        saveDateToLocalStorage();
    }

    function handleColorInput(color, shade, value, updateShades = true, userChanged = true) {
        updateCSSVariable(colorPallete[color + '-' + shade], value);
        toggleResetButton(color + '-reset', true);
        const colorInput = $('#' + color + '-' + shade + '-color')
        colorInput.attr('default-color', false);
        colorInput.attr('user-changed', userChanged);

        if (updateShades && color === 'primary' && shade === 'base') {
            ["lighter", "darker"].forEach(newShade => {
                const variation = color + '-' + newShade
                const variationId = variation + '-color';
                const adjustedValue = adjustHexColor(value, variation === "primary-lighter" ? 40 : -40);
                
                const shadeColorInput = $('#' + variationId)
                if (shadeColorInput.attr('user-changed') === 'true') {
                    return
                }
                
                shadeColorInput.val(adjustedValue);
                handleColorInput(color, newShade, adjustedValue, false, false);
            });
        }
    }

    function updateCSSVariable(cssVar, value) {
        document.documentElement.style.setProperty(cssVar, value ? value : 'unset');
        
    }



    // Create fixed footer
    function showColorSchemeEditor() {
        renderEditor();
        initColors();
        loadDateFromLocalStorage();
    }

    function initColors() {
        Object.keys(colorPallete).forEach(function(color) {
            resetColor(color);
        });
    }

    // Creating the editor
    function renderEditor() {
        
        $('body').addClass('branding-editor-body');
        var $footer = $('<footer>', {
            id: 'fixed-footer',
            class: 'branding-fixed-footer'
        }).appendTo('body');

        // Add color inputs
        var colors = {'primary': ['lighter', 'base', 'darker'], 'secondary': ['base'], 'accent': ['base'], 'text': ['base'], 'banner': ['base'], 'link': ['base']};
        var $colorContainer = $('<div>', {
            id: 'color-inputs',
            class: 'branding-color-inputs'
        }).appendTo($footer);

        Object.keys(colors).forEach(function(color) {
            var $colorDiv = $('<div>', {
                class: 'branding-color-row'
            }).appendTo($colorContainer);

            var $label = $('<label>', {
                for: color + '-base-color',
                text: color.charAt(0).toUpperCase() + color.slice(1) + ' Color: ',
                class: 'branding-color-label'
            }).appendTo($colorDiv);

            var $colorGroup = $('<div>', {
                class: 'branding-color-group'
            }).appendTo($colorDiv);

            colors[color].forEach(function(shade) {
                $('<input>', {
                    type: 'color',
                    id: color + '-' + shade + '-color',
                    name: color + '-' + shade + '-color',
                    class: 'branding-color-input' + ((shade === 'lighter' || shade === 'darker') ? ' shade-borderless' : ''),
                    on: {
                        input: function() {
                            handleColorInput(color, shade, $(this).val());
                        },
                        change: function() {
                            saveDateToLocalStorage();
                        }
                    }
                }).appendTo($colorGroup);
                $('<div>', {
                    class: 'chain'
                }).appendTo($colorGroup);
            });
            $('<button>', {
                text: 'Reset',
                class: 'branding-btn btn-danger branding-reset-btn',
                id: color + '-reset',
                click: function() {
                    handleResetButton(color);
                }
            }).appendTo($colorDiv);

            
        });

        // Add Submit and Cancel buttons
        var $buttonContainer = $('<div>', {
            class: 'branding-button-container'
        }).appendTo($footer);



        var $cancelButton = $('<button>', {
            text: 'Cancel',
            class: 'branding-btn btn-danger'
        }).appendTo($buttonContainer);

        var $resetAllButton = $('<button>', {
            text: 'Reset All',
            class: 'branding-btn btn-danger'
        }).appendTo($buttonContainer);

        var $submitButton = $('<button>', {
            text: 'Submit',
            class: 'branding-btn btn-success'
        }).appendTo($buttonContainer);


        // Event handlers for buttons
        $submitButton.click(function() {
            saveDateToLocalStorage();
            const data = getSchemaData();
            $.ajax({
                url: baseUrl + "/siteBranding",
                dataType: 'json',
                type: 'POST',
                data: {
                    action: "update",
                    themePrimaryColor: data.colors["themePrimaryColor"] || null,
                    themePrimaryColorLighter: data.colors["themePrimaryColorLighter"] || null,
                    themePrimaryColorDarker: data.colors["themePrimaryColorDarker"] || null,
                    themeSecondaryColor: data.colors["themeSecondaryColor"] || null,
                    themeAccentColor: data.colors["themeAccentColor"] || null,
                    themeLinkColor: data.colors["themeLinkColor"] || null,
                    themeTextColor: data.colors["themeTextColor"] || null,
                    themeBannerColor: data.colors["themeBannerColor"] || null,
                },
                complete: function(xhr, status) {
                    alert(globalI18nStrings.brandingColorsSubmitAlert);
                    var location = getSchemaData().lastUrl;
                    localStorage.removeItem('colorSchemeEditor');
                    window.location.href = location;
                }
            });

        });

        $cancelButton.click(function() {
            alert(globalI18nStrings.brandingColorsCancelAlert);
            var location = getSchemaData().lastUrl;
            localStorage.removeItem('colorSchemeEditor');
            window.location.href = location;

        });
        
        $resetAllButton.click(function() {
            $.ajax({
                url: baseUrl + "/siteBranding",
                dataType: 'json',
                type: 'POST',
                data: {
                    action: "remove-all",
                },
                complete: function(xhr, status) {
                    alert(globalI18nStrings.brandingColorsResetAlert);
                    var location = getSchemaData().lastUrl;
                    localStorage.removeItem('colorSchemeEditor');
                    window.location.href = location;
                }
            });


        });

        
    }
    // End of Creating the editor


});
