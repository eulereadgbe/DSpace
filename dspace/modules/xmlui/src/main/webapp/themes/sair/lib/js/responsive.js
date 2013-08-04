(function ($) {
    $(document).ready(function () { // On load
        if ($(window).width() > 501) { //Adaptive background image for header
            $("#ds-header-wrapper").backstretch("/themes/sair/images/SAIR-banner.jpg");
        }
        if ($(window).width() <= 500) {
            $("#ds-header-wrapper").backstretch("/themes/sair/images/SAIR-banner-small.jpg");
        }
        var headerLogo = $("#ds-header-logo");

        headerLogo.css({'width': '9.7894736842105263157894736842105%',
            'height': '0', 'padding-bottom': '7.3684210526315789473684210526316%',
            'margin': '1.0526315789473684210526315789474%'});
        headerLogo.backstretch("/themes/sair/images/seafdec-logo.png");
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
        var sublist = $('div.ds-option-set ul.sublist');
        if ($(window).width() <= 600) {
            sublist.css('display', 'none');
        }

        //Accordion menu
        var selector = $('#aspect_discovery_Navigation_list_discovery ul li h2, #aspect_viewArtifacts_Navigation_list_browse ul li h2,' +
            '#aspect_viewArtifacts_Navigation_list_administrative ul li h2');
        $(selector).hover(function () {
            $(this).css({'color': '#036', 'text-decoration': 'underline', 'font-weight': 'bold', 'cursor': 'hand', 'cursor': 'pointer'});
        }, function () {
            $(this).css({'color': '#444444', 'text-decoration': 'none', 'font-weight': 'normal', 'cursor': 'pointer'});
        });


        selector.click
        (function (event) {
            var elem = $(this).next();
            var menu = $('#menu ul:visible');
            if (elem.is('ul')) {
                event.preventDefault();
                menu.not(elem).slideUp();
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

        /* Linkify All Items Metadata content (Item Detail View) */
        $('#aspect_artifactbrowser_ItemViewer_div_item-view table.ds-includeSet-table tr.ds-table-row td.value-cell').each(function () {
            var that = $(this),
                text = that.html();
            that.html(linkify_html(text));
        });

        //Open External Links In New Tab/Window
        $('a').filter(function () {
            return this.hostname && this.hostname !== location.hostname;
        }).addClass("external");
        $("#ds-body a[href^='http://']").attr("target", "_blank");
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
            if ($(window).width() <= 600) { // Accordion menu collapsed
                $('div.ds-option-set ul.sublist').css('display', 'none');
            }
            else {
                $('div.ds-option-set ul.sublist').css('display', 'inherit');
            }
        });


        $(window).resize();
    });
})(jQuery);