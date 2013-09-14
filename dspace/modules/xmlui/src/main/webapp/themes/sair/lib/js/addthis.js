(function ($) {
    $(document).ready(function () {

        if (location.href.match(/show=full/) != null) {
        $('div#addthis').attr("addthis:url", (itemUrl).replace("?show=full", ""));
            $('div#at4-share.at-vertical-menu.addthis_32x32_style.ats-transparent.at4-show').attr("addthis:url", (itemUrl).replace("?show=full", ""));
        }
        else {
            $('div#addthis').attr("addthis:url", (document.URL));
        }

        var resizeTimer;

        $(window).resize(function () {
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(Resize, 100);
        });

        function Resize() {

            if ($(window).width() < 1080) { // This is to avoid the Add this toolbar from covering the footer signature
                var footer = $('#ds-footer').height() + 62;
                document.getElementById("ds-footer-wrapper").style.height = footer + 'px';
                document.getElementById('ds-footer-wrapper').style.marginTop = '-' + footer + 'px';
                document.getElementById('ds-content-wrapper').style.paddingBottom = footer  + 'px';
            }
        }
        $(window).resize();
    });
})(jQuery);