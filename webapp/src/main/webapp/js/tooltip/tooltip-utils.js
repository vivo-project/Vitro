function setTooltip(elementId, data) {
    let elements = document.querySelectorAll(elementId);
    elements.forEach(element => {
        let tooltip = undefined
        let trigger = data.trigger || 'hover';

        if (trigger === 'click') {

            element.addEventListener('click', () => {
                if (!tooltip) {
                    tooltip = setupPopper(element, data);
                } else {
                    tooltip?.remove();
                    tooltip = undefined;
                }
            });
            
            document.addEventListener('click', (event) => {                
                if (!tooltip?.contains(event.target) && !element.contains(event.target)) {
                    if (tooltip) {
                        tooltip.remove();
                        tooltip = undefined;
                    }
                } 
            });
        }


        if (trigger === 'hover') {

            function hidePopover() {
                tooltip?.remove();
                tooltip = undefined;
            }

            let timeout;
            
            element.addEventListener('mouseenter', () => {
                if (!tooltip || $('.tooltip').length == 0 ) {
                    tooltip = setupPopper(element, data);
                    tooltip.addEventListener('mouseenter', () => {
                        clearTimeout(timeout);
                    });
        
                    tooltip.addEventListener('mouseleave', () => {
                        timeout = setTimeout(hidePopover, 300);
                    });
                }
                clearTimeout(timeout);
                

            });

            element.addEventListener('mouseleave', () => {
                timeout = setTimeout(hidePopover, 300);
            });
        }
    })
}

function setupPopper(element, data) {
    let tooltip = document.createElement('div');
            
    let arrow = document.createElement('div');
    arrow.setAttribute('data-popper-arrow', '')
    arrow.className = 'popover-arrow';
    arrow.id = 'arrow';
    tooltip.appendChild(arrow);
    tooltip.className = 'vitroTooltip tooltip ' + data.customClass;
    document.body.appendChild(tooltip);

    let innerPopper = document.createElement('div');
    innerPopper.className = 'tooltip-inner';
    innerPopper.innerHTML = data.title || 'TEST';
    tooltip.appendChild(innerPopper);

    $('.tooltip a.close').click(function(event) {
        event.preventDefault();
        $(this).closest('.tooltip').remove();
    });

    Popper.createPopper(element, tooltip, {
        placement: data.placements?.[0] || 'auto',
        modifiers: [
            {
                name: 'offset',
                options: {
                  offset: [0, 10],
                },
            },
            {
                fallbackPlacements: data.placements || ['auto'],
                name: 'arrow',
                options: {
                  padding: 5,
                },
            },
        ],
    });

    if (data?.afterCreate) { data.afterCreate(); }

    return tooltip;
}
