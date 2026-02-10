function setTooltip(elementId, data) {
    const elements = document.querySelectorAll(elementId);

    const tooltipCleanup = (element) => {
        if (element.cleanupListeners) {
            element.cleanupListeners();
        }
    };

    elements.forEach((element) => {
        const trigger = data.trigger || 'hover';
        setupTriggerHandlers(element, trigger, data);
    });

    return () => elements.forEach(tooltipCleanup);
}

function setupTriggerHandlers(element, trigger, data) {
    element.setAttribute('tabindex', '0');

    if (trigger === 'click') {
        setupClickTrigger(element, data);
    } else if (trigger === 'hover') {
        setupHoverTrigger(element, data);
    }
}

function isTooltipHidden(tooltip) {
    // If tooltip is not defined or there are no tooltips in the document, it is hidden
    if (!tooltip) return true;

    // Fallback if tooltip is defined but not in the document, it is hidden
    if (document.querySelectorAll('.tooltip').length === 0) return true

    // Variable is deffined and there is at least one tooltip element in the document
    return false;
}

function setupClickTrigger(element, data) {
    let tooltip;

    const handleDocumentClick = (event) => {
        if (tooltip && !tooltip.contains(event.target) && !element.contains(event.target)) {
            tooltip = removeTooltip(tooltip);
        }
    };

    element.addEventListener('click', () => {
        if (isTooltipHidden(tooltip)) {
            tooltip = setupTooltip(element, data, true);
        } else {
            tooltip = removeTooltip(tooltip);
        }
    });

    document.addEventListener('click', handleDocumentClick);

    element.cleanupListeners = () => {
        document.removeEventListener('click', handleDocumentClick);
        tooltip = removeTooltip(toolip);
    };
}

function setupHoverTrigger(element, data) {
    let tooltip;
    let timeout;

    const showTooltip = () => {
        clearTimeout(timeout);
        if (isTooltipHidden(tooltip)) {
            tooltip = setupTooltip(element, data, false);
            tooltip.addEventListener('mouseenter', () => clearTimeout(timeout));
            tooltip.addEventListener('mouseleave', () => timeout = setTimeout(() => {tooltip = removeTooltip(tooltip)}, 300));
        }
    };

    const handleMouseLeave = () => {
        timeout = setTimeout(() => {tooltip = removeTooltip(tooltip)}, 300);
    };

    element.addEventListener('mouseenter', showTooltip);
    element.addEventListener('mouseleave', handleMouseLeave);

    element.addEventListener('focus', showTooltip);
    element.addEventListener('blur', handleMouseLeave);

    element.cleanupListeners = () => {
        element.removeEventListener('mouseenter', showTooltip);
        element.removeEventListener('mouseleave', handleMouseLeave);
        tooltip = removeTooltip(tooltip);
    };
}

function removeTooltip(tooltip) {
    if (tooltip) {
        if (tooltip.cleanup) {
            tooltip.cleanup();
        }
        tooltip.remove();
        tooltip = undefined;
    }
    return tooltip
}

function trapFocus(container, focusOutElement = undefined ) {
    const focusableSelectors = `
        button:not(:disabled),
        [href]:not([aria-disabled]),
        input:not(:disabled),
        select:not(:disabled),
        textarea:not(:disabled),
        [tabindex]:not([tabindex="-1"]):not(:disabled):not([aria-disabled])
    `;

    const focusableElements = Array.from(container.querySelectorAll(focusableSelectors));
    const first = focusableElements[0];
    const last = focusableElements[focusableElements.length - 1];

    function handleKeyDown(e) {
        if (e.key === 'Tab') {
            if (e.shiftKey) {
                // Shift + Tab
                if (document.activeElement === first) {
                    e.preventDefault();

                    if (focusOutElement) {
                        focusOutElement.focus();
                    } else {
                        last.focus();
                    }
                }
            } else {
                // Tab
                if (document.activeElement === last) {
                    e.preventDefault();
                    
                    if (focusOutElement) {
                        focusOutElement.focus();
                    } else {
                        first.focus();
                    }
                }
            }
        }
    }

    container.addEventListener('keydown', handleKeyDown);

    // Focus the first element when trap starts
    setTimeout(() => {
        const inner = container.querySelector('.tooltip-inner');
        if (inner) {
            inner.focus();
        } else {
            first?.focus();
        }
    }, 50)

    // Return a cleanup function in case you want to remove the trap later
    return () => container.removeEventListener('keydown', handleKeyDown);
}

function setupTooltip(element, data, trapFocusRotate = false) {
    const tooltip = createTooltipElement(data);
    document.body.appendChild(tooltip);
    setupCloseButtonHandler(element, tooltip);

    const updatePosition = () => updateTooltipPosition(element, tooltip, data);

    updatePosition();

    if (trapFocusRotate) {
        // Tab focus loop elements in modal
        trapFocus(tooltip);
    } else {
        // Tab focus go back to the element that triggered the tooltip
        trapFocus(tooltip, element);
    }

    const cleanup = FloatingUIDOM.autoUpdate(element, tooltip, updatePosition);

    if (data?.afterCreate) {
        data.afterCreate();
    }

    return Object.assign(tooltip, { cleanup });
}

function createTooltipElement(data) {
    const tooltip = document.createElement('div');
    tooltip.className = `vitroTooltip tooltip ${data.customClass}`;

    const arrow = document.createElement('div');
    arrow.setAttribute('data-tooltip-arrow', '');
    arrow.className = 'tooltip-arrow';
    tooltip.appendChild(arrow);

    const innerTooltip = document.createElement('div');
    innerTooltip.className = 'tooltip-inner';
    innerTooltip.setAttribute('role', 'dialog');
    innerTooltip.setAttribute('aria-modal', 'true');
    innerTooltip.setAttribute('tabindex', '-1');
    innerTooltip.innerHTML = data.title;
    tooltip.appendChild(innerTooltip);
    
    requestAnimationFrame(() => {
        innerTooltip.focus();
    });
    
    return tooltip;
}

function setupCloseButtonHandler(element, tooltip) {
    $('.tooltip a.close').click((event) => {
        event.preventDefault();
        removeTooltip(tooltip);
        element.focus();
        // $(event.target).closest('.tooltip').remove();
    });
}

function updateTooltipPosition(element, tooltip, data) {
    const arrow = tooltip.querySelector('[data-tooltip-arrow]');

    FloatingUIDOM.computePosition(element, tooltip, {
        placement: data.placements?.[0] || 'auto',
        middleware: [
            FloatingUIDOM.offset(20),
            data.placements
                ? FloatingUIDOM.flip({ fallbackPlacements: data.placements })
                : FloatingUIDOM.autoPlacement(),
            FloatingUIDOM.arrow({ element: arrow, padding: 5 }),
        ],
    }).then(({ x, y, placement, middlewareData }) => {
        tooltip.style.left = `${x}px`;
        tooltip.style.top = `${y}px`;

        if (middlewareData.arrow) {
            const { x: arrowX, y: arrowY } = middlewareData.arrow;
            const staticSide = {
                top: 'bottom',
                right: 'left',
                bottom: 'top',
                left: 'right',
            }[placement.split('-')[0]];

            Object.assign(arrow.style, {
                left: arrowX != null ? `${arrowX}px` : '',
                top: arrowY != null ? `${arrowY}px` : '',
                [staticSide]: '-4px',
            });
        }

        tooltip.setAttribute('data-placement', placement);
    });
}
