(function ($) {
    $(document).ready(function () { // On load
        if ($(window).width() > 501) {
            $("#ds-header-wrapper").backstretch("/themes/sair/images/SAIR-banner.jpg");
        }
        if ($(window).width() <= 500) {
            $("#ds-header-wrapper").backstretch("/themes/sair/images/SAIR-banner-small.jpg");
        }
        $("#ds-header-logo").backstretch("/themes/sair/images/seafdec-logo.png");
        $("#ds-header-logo-text").fitText(1.5, { minFontSize: '16px', maxFontSize: '48px' });
        $('#ds-trail').wrap('<div class="breadCrumb" id="breadCrumb"></div>');// Will insert class and id for breadcrumbs
        $("#breadCrumb").jBreadCrumb();
        $(".ds-trail-arrow").remove();// This will remove the arrows in ds-trail before using the breadCrumbs jquery plugin

        // Will not fire if screen widths is equal to values specified in media queries (media.css)
        if ($(window).width() <= 972 && ($(window).width() !== 320 && $(window).width() !== 360 && $(window).width() !== 480 && $(window).width() !== 600)) {
            document.getElementById("ds-main").style.maxWidth = $(window).width() + 'px';
            document.getElementById("ds-main").style.width = (($(window).width() - 12) / $(window).width()) * 100 + '%';
        }
        //Collapse if screen width is <=600
        if ($(window).width() <= 600) {
            $('div.ds-option-set ul.sublist').css('display', 'none');
        }

        //Accordion menu
        $('#aspect_discovery_Navigation_list_discovery ul li h2, #aspect_viewArtifacts_Navigation_list_browse ul li h2,' +
            '#aspect_viewArtifacts_Navigation_list_administrative ul li h2').click
        (function (event) {
            var elem = $(this).next();
            if (elem.is('ul')) {
                event.preventDefault();
                $('#menu ul:visible').not(elem).slideUp();
                elem.slideToggle();
            }
        });

        //Add Expand / Collapse for the Front page and Community-List
        $('ul#tree').before('<div id="sidetreecontrol">' + //Will not display if javascript disabled
            '<a class="button white small" href="?#">Collapse All</a>&#160;' +
            '<a class="button white small" href="?#">Expand All</a></div>');
        $("#tree").treeview({
            collapsed: true,
            animated: "medium",
            control: "#sidetreecontrol",
            persist: "cookie"
        });
    });
})(jQuery);

(function ($) {
    $(document).ready(function () { // On load
        $(window).resize(function () {
            if ($(window).width() > 501) {
                $("#ds-header-wrapper").backstretch("/themes/sair/images/SAIR-banner.jpg");
            }
            if ($(window).width() <= 500) {
                $("#ds-header-wrapper").backstretch("/themes/sair/images/SAIR-banner-small.jpg");
            }

            // Will not fire if screen widths is equal to values specified in media queries (media.css)
            if ($(window).width() <= 972 && ($(window).width() !== 320 && $(window).width() !== 360 && $(window).width() !== 480 && $(window).width() !== 600)) {
                document.getElementById("ds-main").style.maxWidth = $(window).width() + 'px';
                document.getElementById("ds-main").style.width = (($(window).width() - 12) / $(window).width()) * 100 + '%';
                $("#ds-header-logo").backstretch("/themes/sair/images/seafdec-logo.png");

                // This will update the width of breadcrumbs when resizing
                document.getElementById("breadCrumb").style.width = ($(window).width() - 12) + 'px';
                var nodes = document.getElementById("breadCrumb").childNodes;
                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i].nodeName.toLowerCase() == 'div') {
                        nodes[i].style.width = $(window).width() - 12 + 'px';
                    }
                }
            }
            if ($(window).width() <= 600) {
                $('div.ds-option-set ul.sublist').css('display', 'none');
            }
            else {
                $('div.ds-option-set ul.sublist').css('display', 'inherit');
            }
        });


        $(window).resize();
    });
})(jQuery);