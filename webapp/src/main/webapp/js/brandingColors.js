/* $This file is distributed under the terms of the license in LICENSE$ */
$.extend(this, globalI18nStrings);

let brandingColors = null;

function initBrandingColors() {
    showColorSchemeEditor();

    async function showColorSchemeEditor() {
        await loadThemeConfig();
        if (!brandingColors) {
            return;
        }

        renderEditor();
        initColors();
        loadDateFromLocalStorage();
    }

    function getSchemaData() {
        return JSON.parse(localStorage.getItem('colorSchemeEditor'));
    }

    function saveDateToLocalStorage() {
        let data = getSchemaData();
        brandingColors.pallete.forEach(colorPaletteGroup => {
            colorPaletteGroup.colors.forEach(color => {
                const colorInput = $('#' + color.name + '-color');

                if (colorInput.attr('default-color') === 'true') {
                    data.updatedColors[color.cssVariable] = undefined;
                } else {
                    data.updatedColors[color.cssVariable] = colorInput.val();
                }
            });
        });
        localStorage.setItem('colorSchemeEditor', JSON.stringify(data));        
    }

    function loadDateFromLocalStorage() {
        let data = getSchemaData();
        brandingColors.pallete.forEach(colorPaletteGroup => {
            colorPaletteGroup.colors.forEach(color => {
                if (data.updatedColors[color.cssVariable]) {
                    const colorInput = $('#' + color.name + '-color');
                    colorInput.val(data.updatedColors[color.cssVariable]);
                    colorInput.attr('default-color', false);
                    colorInput.attr('user-changed', true);
                    handleColorInput(colorPaletteGroup, color, data.updatedColors[color.cssVariable], false, true);
                }
            });
        });
    }

    function toggleResetButton(inputId, show) {
        const resetLink = document.getElementById(inputId);
        resetLink.style.display = show ? 'block' : 'none';
    }

    function adjustHexColor(hex, percent) {
        if (hex.startsWith("#")) hex = hex.slice(1);
        if (hex.length !== 6) throw new Error("Invalid hex color format.");

        const adjust = (value) => Math.max(0, Math.min(255, Math.round(value + (percent / 100) * (percent > 0 ? (255 - value) : value))));
        const [r, g, b] = [0, 2, 4].map(offset => adjust(parseInt(hex.substring(offset, offset + 2), 16)));

        return `#${[r, g, b].map(value => value.toString(16).padStart(2, "0")).join("")}`;
    }
    
    function resetColor(color) {
        let pallete = color.value;
        
        const colorInput = $('#' + color.name + '-color');
        colorInput.val(pallete);
        colorInput.attr('default-color', true);
        colorInput.attr('user-changed', false);

        updateCSSVariable(color.cssVariable, null);
    }

    function handleResetButton(colorPaletteGroup) {
        colorPaletteGroup.colors.forEach(color => {
            resetColor(color);
        });

        toggleResetButton(colorPaletteGroup.groupName + "-reset", false);
        saveDateToLocalStorage();
    }

    function handleColorInput(group, color, value, updateShades = true, userChanged = true) {        
        const colorInput = $('#' + color.name + '-color')
        updateCSSVariable(color.cssVariable, value);
        toggleResetButton(group.groupName + '-reset', true);
        colorInput.attr('default-color', false);
        colorInput.attr('user-changed', userChanged);

        if (updateShades) {
            const palleteGroup = brandingColors.pallete.find(x => x.groupName == group.groupName)
            const dependentColors = palleteGroup.colors.filter(x => x.shade?.base == color.name)
            dependentColors.forEach(dependentColor => {

                const shadeColorInput = $('#' + dependentColor.name + '-color');
                if (shadeColorInput.attr('user-changed') === 'true') {
                    return;
                }
                const adjustedValue = applyShadeTransformation(value, dependentColor.shade);
                shadeColorInput.val(adjustedValue);
                handleColorInput(group, dependentColor, adjustedValue, false, false);
            });
        }
    }
        
    function applyShadeTransformation(color, shadeConfig) {
        if (!shadeConfig) return color;
        
        const amount = shadeConfig.amount || 0;
        const type = shadeConfig.type;
        
        if (type === 'lighten') {
            return adjustHexColor(color, amount);
        } else if (type === 'darken') {
            return adjustHexColor(color, -amount);
        }
        
        return color;
    }

    function updateCSSVariable(cssVar, value) {
        document.documentElement.style.setProperty(cssVar, value ? value : 'unset');
        
    }

    async function loadThemeConfig() {
        let data = getSchemaData();
        brandingColors = data.brandingColors;
    }

    function initColors() {
        brandingColors.pallete.forEach(colorPaletteGroup => {
            colorPaletteGroup.colors.forEach(color => {
                resetColor(color);
            });
        });
    }

    function renderEditor() {
        
        $('body').addClass('branding-editor-body');
        var $footer = $('<footer>', {
            id: 'fixed-footer',
            class: 'branding-fixed-footer'
        }).appendTo('body');

        var pallete = brandingColors?.pallete
        var $colorContainer = $('<div>', {
            id: 'color-inputs',
            class: 'branding-color-inputs'
        }).appendTo($footer);

        pallete.forEach((colorPaletteGroup) => {
            var $colorDiv = $('<div>', {
                class: 'branding-color-col'
            }).appendTo($colorContainer);

            var $label = $('<label>', {
                for: colorPaletteGroup.groupName + '-base-color',
                text: colorPaletteGroup.groupName + ' Color: ',
                class: 'branding-color-label'
            }).appendTo($colorDiv);

            var $colorGroup = $('<div>', {
                class: 'branding-color-group'
            }).appendTo($colorDiv);

            colorPaletteGroup.colors.forEach((color) => {

                var $relativeWrapper = $('<div>', {
                    class: 'relative' + (color?.hidden ? ' hidden' : '')
                }).appendTo($colorGroup);

                $('<input>', {
                    type: 'color',
                    id: color.name + '-color',
                    name: color.name + '-color',
                    class: 'branding-color-input' + (color?.shade ? ' shade-borderless' : ''),
                    on: {
                        input: function() {
                            handleColorInput(colorPaletteGroup, color, $(this).val());
                        },
                        change: function() {
                            saveDateToLocalStorage();
                        }
                    }
                }).appendTo($relativeWrapper);
                $('<div>', {
                    class: 'chain'
                }).appendTo($relativeWrapper);
            });
            $('<button>', {
                text: 'Reset',
                class: 'branding-btn btn-danger branding-reset-btn',
                id: colorPaletteGroup.groupName + '-reset',
                click: function() {
                    handleResetButton(colorPaletteGroup);
                }
            }).appendTo($colorDiv);

            
        });

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

        $submitButton.click(function() {
            saveDateToLocalStorage();
            const data = getSchemaData();

            const postData = {
                action: "update",
                colors: JSON.stringify(data.updatedColors)
            };
            
            $.ajax({
                url: baseUrl + "/siteBranding",
                dataType: 'json',
                type: 'POST',
                data: postData,
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

};


if (document.readyState === "complete" || document.readyState === "interactive") {
    initBrandingColors();
} else {
    $(document).ready(function() {
        initBrandingColors();
    });
}
