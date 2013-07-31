(function ($) {
    $(document).ready(function () { // On load
        $('#ds-trail').wrap('<div class="breadCrumb" id="breadCrumb"></div>');
        $("#breadCrumb").jBreadCrumb();
        $("#ds-header-wrapper").backstretch("/themes/sair/images/SAIR-banner.jpg");
        $("#ds-header-logo").backstretch("/themes/sair/images/seafdec-logo.png");
        $("#ds-header-logo-text").fitText(1.5, { minFontSize: '16px', maxFontSize: '48px' });
        $(".ds-trail-arrow").remove();// This will remove the arrows in ds-trail before using the breadCrumbs jquery plugin

        if ($(window).width() <= 972 && ($(window).width() !== 320 && $(window).width() !== 360 && $(window).width() !== 480 && $(window).width() !== 600)) {
            document.getElementById("ds-main").style.maxWidth = $(window).width() + 'px';
            document.getElementById("ds-main").style.width = (($(window).width() - 12) / $(window).width()) * 100 + '%';
        }
    });
})(jQuery);

(function ($) {

    $(document).ready(function () { // On load


    $(window).resize(function () {
        // Will not fire if screen widths is equal to values specified in media queries (media.css)
        if ($(window).width() <= 972 && ($(window).width() !== 320 && $(window).width() !== 360 && $(window).width() !== 480 && $(window).width() !== 600)) {
            document.getElementById("ds-main").style.maxWidth = $(window).width() + 'px';
            document.getElementById("ds-main").style.width = (($(window).width() - 12) / $(window).width()) * 100 + '%';

            // This will update the width of breadcrumbs when resizing
            document.getElementById("breadCrumb").style.width = ($(window).width() - 12) + 'px';
            var nodes = document.getElementById("breadCrumb").childNodes;
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].nodeName.toLowerCase() == 'div') {
                    nodes[i].style.width = $(window).width() - 12 + 'px';
                }
            }
        }
    });


    $(window).resize();
    });
})(jQuery);