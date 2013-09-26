(function ($) {
    $(window).load(function(){
        var clickdesk_bar = $('div.cd-bubble.clickdesk_bubble').attr('id', 'clickdesk');
        var bottomPosition = ($('at4m-dock').length > 0) ? 44 : 0;
        document.getElementById('clickdesk').style.bottom = bottomPosition + 'px';
    });

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
                var footer = $('#ds-footer').height() + 98;
                document.getElementById("ds-footer-wrapper").style.height = footer + 'px';
                document.getElementById('ds-footer-wrapper').style.marginTop = '-' + footer + 'px';
                document.getElementById('ds-content-wrapper').style.paddingBottom = footer  + 'px';
            }

            if ($(window).width() < 320 || $(window).height() < 361) {
                $('div#clickdesk').css('display', 'none');
                $('div#clickdesk_container').css('display', 'none');
            }
            else {
                $('div#clickdesk').css('display', 'block');
            }
        }
        $(window).resize();
    });
})(jQuery);