(function ($) {
    $(document).ready(function () {
        var itemUrl = (document.URL);
        var addthisbuttons = "<div id='addthis' class='addthis_toolbox addthis_default_style'>" +
            "<a class='addthis_button_preferred_1'>&#160;</a>" +
            "<a class='addthis_button_preferred_2'>&#160;</a>" +
            "<a class='addthis_button_preferred_3'>&#160;</a>" +
            "<a class='addthis_button_preferred_4'>&#160;</a>" +
            "<a class='addthis_button_compact'>&#160;</a>" +
            "<a class='addthis_counter addthis_bubble_style'>&#160;</a>" +
            "</div>";

        $('p.ds-paragraph.item-view-toggle.item-view-toggle-top').after(addthisbuttons); // Add this toolbars in item view
        $('table.ds-includeSet-table.detailtable').after(addthisbuttons);
        $('div.item-summary-view-metadata').prepend(addthisbuttons);
        $('#aspect_artifactbrowser_CollectionViewer_div_collection-home').before(addthisbuttons);
        $('#aspect_artifactbrowser_CommunityViewer_div_community-home').before(addthisbuttons);
        $('#file_news_div_news').before(addthisbuttons);

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