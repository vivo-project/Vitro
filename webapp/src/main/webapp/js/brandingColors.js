/* $This file is distributed under the terms of the license in LICENSE$ */


let theme = "themes/wilma/";

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


const defaultColorPalete = {
    "themes/wilma/": {
        "primary-lighter": "#7bb3cb",
        "primary-base": "#2485ae",
        "primary-darker": "#064d68",
        "banner-base": "#023048",
        "secondary-base": "#398aac",
        "accent-base": "#749a02",
        "text-base": "#5f6464",
        "link-base": "#2485ae"
    },
    "themes/vitro/": {
        "primary-lighter": "#62b6d7",
        "primary-base": "#47b6d0",
        "primary-darker": "#006279",
        "banner-base": "#012d3d",
        "secondary-base": "#398aac",
        "accent-base": "#0ea5c8",
        "text-base": "#595B5B",
        "link-base": "#47b6d0"
    },
    "themes/tenderfoot/": {
        "primary-lighter": "#8bb9cc",
        "primary-base": "#3E8BAA",
        "primary-darker": "#0c0cb5",
        "banner-base": "#013049",
        "secondary-base": "#93c3d4",
        "accent-base": "#3446A0",
        "text-base": "#595B5B",
        "link-base": "#2485ae"
    },
    "themes/nemo/": {
        "primary-lighter": "#337ab7",
        "primary-base": "#064d68",
        "primary-darker": "#023447",
        "banner-base": "#064d68",
        "secondary-base": "#064d68",
        "accent-base": "#8BAB2E",
        "text-base": "#333",
        "link-base": "#064d68"
    }
};

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
        console.log("SAVED");
        
    }

    function loadDateFromLocalStorage() {
        let data = getSchemaData();
        Object.keys(data.colors).forEach(function(color) {
            let colorFormated = colorNameConvertorInverse[color]; // Convert format from themePrimaryColor to primary-base etc.

            if (!data.colors[color]) return;
            $('#' + colorFormated + '-color').val(data.colors[color]);
            handleColorInput(colorFormated.split('-')[0], colorFormated.split('-')[1], data.colors[color]);
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
        let pallete = defaultColorPalete[theme];

        $('#' + inputId + '-color').val(pallete[inputId]);
        $('#' + inputId + '-color').attr('default-color', true);
        updateCSSVariable(colorPallete[inputId], null);
    }

    function handleResetButton(buttonName) {
        resetLinks[buttonName].forEach(inputId => {
            resetColor(inputId);
        });

        toggleResetButton(buttonName + "-reset", false);
        saveDateToLocalStorage();
    }

    function handleColorInput(color, shade, value) {
        updateCSSVariable(colorPallete[color + '-' + shade], value);
        toggleResetButton(color + '-reset', true);
        $('#' + color + '-' + shade + '-color').attr('default-color', false);

        if (color === 'primary' && shade === 'base') {
            ["lighter", "darker"].forEach(newShade => {
                const variation = color + '-' + newShade
                const variationId = variation + '-color';
                const adjustedValue = adjustHexColor(value, variation === "primary-lighter" ? 40 : -40);
                
                $('#' + variationId).val(adjustedValue);
                handleColorInput(color, newShade, adjustedValue);
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

    function loadTheme() {
        let data = getSchemaData();
        theme = data.theme || "themes/wilma/";
    }

    function initColors() {
        loadTheme();
        Object.keys(colorPallete).forEach(function(color) {
            resetColor(color);
        });
    }


    // Creating the editor
    function renderEditor() {
        console.log("loading");
        
        $('body').css('margin-bottom', '150px');
        var $footer = $('<footer>', {
            id: 'fixed-footer',
            css: {
                position: 'fixed',
                bottom: '0',
                width: '100%',
                background: 'rgb(243 243 240)',
                borderTop: '1px solid #ccc',
                color: '#fff',
                textAlign: 'center',
                padding: '10px',
                height: 'auto',
                maxHeight: '160px',
                overflowY: 'auto',
                zIndex: '10000'
            }
        }).appendTo('body');

        // Add color inputs
        var colors = {'primary': ['lighter', 'base', 'darker'], 'secondary': ['base'], 'accent': ['base'], 'text': ['base'], 'banner': ['base'], 'link': ['base']};
        var $colorContainer = $('<div>', {
            id: 'color-inputs',
            css: {
                display: 'flex',
                justifyContent: 'center',
                columnGap: '50px',
                flexWrap: 'wrap',
                padding: '0 10px',
            }
        }).appendTo($footer);

        Object.keys(colors).forEach(function(color) {
            var $colorDiv = $('<div>', {
                css: {
                display: 'flex',
                alignItems: 'center',
                margin: '5px'
                }
            }).appendTo($colorContainer);

            var $label = $('<label>', {
                for: color + '-color',
                text: color.charAt(0).toUpperCase() + color.slice(1) + ' Color: ',
                css: {
                marginRight: '10px',
                color: '#000'
                }
            }).appendTo($colorDiv);

            var $colorGroup = $('<div>', {
                css: {
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center'
                }
            }).appendTo($colorDiv);

            colors[color].forEach(function(shade) {
                $('<input>', {
                    type: 'color',
                    id: color + '-' + shade + '-color',
                    name: color + '-' + shade + '-color',
                    on: {
                        input: function() {
                            handleColorInput(color, shade, $(this).val());
                        },
                        change: function() {
                            saveDateToLocalStorage();
                        }
                    }
                }).appendTo($colorGroup);
            });
            $('<button>', {
                text: 'Reset',
                css: {
                    marginLeft: '10px',
                    padding: '5px 10px',
                    background: '#f44336',
                    color: '#fff',
                    border: 'none',
                    cursor: 'pointer',
                    display: 'none'
                },
                id: color + '-reset',
                click: function() {
                    handleResetButton(color);
                }
            }).appendTo($colorDiv);

            
        });

        // Add Submit and Cancel buttons
        var $buttonContainer = $('<div>', {
            css: {
                display: 'flex',
                gap: '10px',
                justifyContent: 'center',
                marginTop: '10px'
            }
        }).appendTo($footer);



        var $cancelButton = $('<button>', {
            text: 'Cancel',
            css: {
                padding: '5px 10px',
                background: '#f44336',
                color: '#fff',
                border: 'none',
                cursor: 'pointer'
            }
        }).appendTo($buttonContainer);

        var $resetAllButton = $('<button>', {
            text: 'Reset All',
            css: {
                padding: '5px 10px',
                background: '#f44336',
                color: '#fff',
                border: 'none',
                cursor: 'pointer'
            }
        }).appendTo($buttonContainer);

        var $submitButton = $('<button>', {
            text: 'Submit',
            css: {
                padding: '5px 10px',
                background: '#4CAF50',
                color: '#fff',
                border: 'none',
                cursor: 'pointer'
            }
        }).appendTo($buttonContainer);


        // Event handlers for buttons
        $submitButton.click(function() {
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
                    var results = jQuery.parseJSON(xhr.responseText);
                    window.location.href = getSchemaData().lastUrl;
                    localStorage.removeItem('colorSchemeEditor');
                    alert('Color scheme changes submitted.');
                    console.log(results);
                }
            });

        });

        $cancelButton.click(function() {
            window.location.href = getSchemaData().lastUrl;
            localStorage.removeItem('colorSchemeEditor');
            alert('Color scheme changes canceled.');
        });
        
        $resetAllButton.click(function() {
            $.ajax({
                url: baseUrl + "/siteBranding",
                dataType: 'json',
                type: 'POST',
                data: {
                    action: "removeall",
                },
                complete: function(xhr, status) {
                    var results = jQuery.parseJSON(xhr.responseText);
                    window.location.href = getSchemaData().lastUrl;
                    localStorage.removeItem('colorSchemeEditor');
                    alert('Color scheme reset successfully.');
                }
            });


        });

        
    }
    // End of Creating the editor


});
