/* $This file is distributed under the terms of the license in LICENSE$ */

$(function(){
    $("ul.dropdown #user-menu").each(function(index) {
        var menu = $(this);
        var trigger = menu.children("a").first();
        var submenu = menu.children("ul").first();
        var open = false;
        var submenuId = submenu.attr("id");

        if (!submenuId) {
            submenuId = "user-submenu-" + index;
            while (document.getElementById(submenuId)) {
                submenuId += "-menu";
            }
            submenu.attr("id", submenuId);
        }

        trigger.attr({ "role": "button", "aria-controls": submenuId });

        function containsFocus() {
            return $.contains(menu[0], document.activeElement);
        }

        function setOpen(value) {
            open = value;
            menu.toggleClass("hover", open);
            // display:none hides the submenu from both tab order and assistive technology.
            // A separate aria-hidden state is unnecessary.
            submenu.toggle(open).css("visibility", open ? "visible" : "hidden")
                .removeAttr("aria-hidden");
            trigger.attr("aria-expanded", open ? "true" : "false");
            if (open) {
                var width = menu.closest("ul.dropdown").width();
                if (width > 88) {
                    submenu.children("li").css("width", (width - 22) + "px");
                }
                submenu.children("li:last-child").css("width", (width - 14) + "px");
            }
        }

        setOpen(containsFocus());

        menu.on("mouseenter", function() {
            setOpen(true);
        }).on("mouseleave", function() {
            if (!containsFocus()) {
                setOpen(false);
            }
        }).on("focusin", function(event) {
            if ($.contains(submenu[0], event.target)) {
                setOpen(true);
            }
        }).on("focusout", function(event) {
            if (event.relatedTarget) {
                if (!$.contains(menu[0], event.relatedTarget)) {
                    setOpen(false);
                }
            } else {
                setTimeout(function() {
                    if (!containsFocus()) {
                        setOpen(false);
                    }
                }, 0);
            }
        }).on("keydown", function(event) {
            if (event.key === "Escape" || event.which === 27) {
                event.preventDefault();
                event.stopPropagation();
                trigger[0].focus();
                setOpen(false);
            }
        });

        trigger.on("click", function(event) {
            event.preventDefault();
            // Return focus before hiding, including browsers that don't focus clicked links.
            trigger[0].focus();
            setOpen(!open);
        }).on("keydown", function(event) {
            if (event.key === " " || event.which === 32) {
                event.preventDefault();
                setOpen(!open);
            } else if (event.key === "ArrowDown" || event.which === 40) {
                event.preventDefault();
                setOpen(true);
                submenu.find("a").first().trigger("focus");
            }
        });
    });

    $("ul.dropdown li ul li:has(ul)").find("a:first").append(" &raquo; ");

});
