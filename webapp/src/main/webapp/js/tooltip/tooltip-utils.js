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
            tooltip = setupTooltip(element, data);
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
            tooltip = setupTooltip(element, data);
            tooltip.addEventListener('mouseenter', () => clearTimeout(timeout));
            tooltip.addEventListener('mouseleave', () => timeout = setTimeout(() => {tooltip = removeTooltip(tooltip)}, 300));
        }
    };

    const handleMouseLeave = () => {
        timeout = setTimeout(() => {tooltip = removeTooltip(tooltip)}, 300);
    };

    element.addEventListener('mouseenter', showTooltip);
    element.addEventListener('mouseleave', handleMouseLeave);

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

function setupTooltip(element, data) {
    const tooltip = createTooltipElement(data);
    document.body.appendChild(tooltip);
    setupCloseButtonHandler(tooltip);

    const updatePosition = () => updateTooltipPosition(element, tooltip, data);

    updatePosition();

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
    innerTooltip.innerHTML = data.title;
    tooltip.appendChild(innerTooltip);

    return tooltip;
}

function setupCloseButtonHandler(tooltip) {
    $('.tooltip a.close').on("click", (event) => {
        event.preventDefault();
        removeTooltip(tooltip);
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
