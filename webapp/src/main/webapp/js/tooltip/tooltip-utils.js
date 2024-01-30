function setTooltip(elementId, data) {
    let elements = document.querySelectorAll(elementId);
    elements.forEach(element => {
        element.setAttribute('data-bs-toggle', "tooltip");
        let tooltip = new bootstrap.Tooltip(element, data);

        // Prevent close link for URI tooltip from requesting bogus '#' href
        element.addEventListener('inserted.bs.tooltip', function () {
            if (data?.afterCreate) { data.afterCreate(); }

            $('a.close').click(function() {
                tooltip.hide()
                return false;
            });
        })

    })
}
