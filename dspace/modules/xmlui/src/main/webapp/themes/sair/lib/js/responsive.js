var headerWrapper = $("#ds-header-wrapper");
var headerLogo = $("#ds-header-logo");
var sublist = $('div.ds-option-set ul.sublist');
var NavList = $('#aspect_discovery_Navigation_list_discovery ul li h2, #aspect_viewArtifacts_Navigation_list_browse ul li h2,' +
    '#aspect_viewArtifacts_Navigation_list_administrative ul li h2');
var toggleBottom = $("p.ds-paragraph.item-view-toggle-bottom");
(function ($) {
    $(document).ready(function () { // On load
        if ($(window).width() > 501) { //Adaptive background image for header
            headerWrapper.backstretch("/themes/sair/images/SAIR-banner.jpg");
            headerWrapper.backstretch("resize");
        }
        if ($(window).width() <= 500) {
            headerWrapper.backstretch("/themes/sair/images/SAIR-banner-small.jpg");
            headerWrapper.backstretch("resize");
        }

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
        if ($(window).width() <= 600) {
            sublist.css('display', 'none');
        }

        //Accordion menu
        NavList.hover(function () {
            $(this).css({'color': '#036', 'text-decoration': 'underline', 'font-weight': 'bold', 'cursor': 'hand', 'cursor': 'pointer'});
        }, function () {
            $(this).css({'color': '#444444', 'text-decoration': 'none', 'font-weight': 'normal', 'cursor': 'pointer'});
        });


        NavList.click
        (function (event) {
            var elem = $(this).next();
            var menu = $('#menu').find('ul:visible');
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

        //Insert 'http://dx.doi.org/' to resolve to DOI
        $('.label-cell:contains(DOI)').next('td').text(function (_, txt) {
            return 'http://dx.doi.org/' + txt;
        });

        /* Linkify All Items Metadata content (Item Detail View) */
        $('table.ds-includeSet-table tr.ds-table-row td.value-cell').each(function () {
            var that = $(this),
                text = that.html();
            that.html(linkify_html(text));
        });

        //Open External Links In New Tab/Window
        $('a').filter(function () {
            return this.hostname && this.hostname !== location.hostname;
        }).addClass("external");
        var relatedLink = $("#ds-body").find("a[href^='http://']");
        relatedLink.attr("target", "_blank");

        // Remove the 'http://dx.doi.org/' to display the DOI only
        $('.external').replaceText(/\bhttp:\/\/dx.doi.org\/\b/gi, '');//http://www.benalman.com/projects/jquery-replacetext-plugin/

        // Open PDFs in new tab/window //
        $("div.file-link a").addClass("button small white");
        $("a[href*='.pdf']").attr("target", "_blank");

        // Create buttons from link //
        $("p.ds-paragraph.item-view-toggle a").addClass("button small white");
        toggleBottom.css('margin-top', '20px');
    });

})(jQuery);

(function ($) {
    $(document).ready(function () { // On load
        $(window).resize(function () {
            if ($(window).width() > 501) { //Adaptive background image for header
                headerWrapper.backstretch("/themes/sair/images/SAIR-banner.jpg");
                headerWrapper.backstretch("resize");
            }
            if ($(window).width() <= 500) {
                headerWrapper.backstretch("/themes/sair/images/SAIR-banner-small.jpg");
                headerWrapper.backstretch("resize");
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