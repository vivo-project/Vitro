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

function setupClickTrigger(element, data) {
    let tooltip;

    const handleDocumentClick = (event) => {
        if (tooltip && !tooltip.contains(event.target) && !element.contains(event.target)) {
            tooltip = removeTooltip(tooltip);
        }
    };

    element.addEventListener('click', () => {
        if (!tooltip || document.querySelectorAll('.tooltip').length === 0) {
            tooltip = setupPopper(element, data);
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
        if (!tooltip || document.querySelectorAll('.tooltip').length === 0) {
            tooltip = setupPopper(element, data);
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
            console.log("Cleanup");
            tooltip.cleanup();
        }
        tooltip.remove();
        tooltip = undefined;
    }
    return tooltip
}

function setupPopper(element, data) {
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
    arrow.setAttribute('data-popper-arrow', '');
    arrow.className = 'popover-arrow';
    tooltip.appendChild(arrow);

    const innerPopper = document.createElement('div');
    innerPopper.className = 'tooltip-inner';
    innerPopper.innerHTML = data.title || 'TEST';
    tooltip.appendChild(innerPopper);

    return tooltip;
}

function setupCloseButtonHandler(tooltip) {
    $('.tooltip a.close').click((event) => {
        event.preventDefault();
        removeTooltip(tooltip);
        // $(event.target).closest('.tooltip').remove();
    });
}

function updateTooltipPosition(element, tooltip, data) {
    const arrow = tooltip.querySelector('[data-popper-arrow]');

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
