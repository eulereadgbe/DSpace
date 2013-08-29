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
        $('div.responsive').append(addthisbuttons);
        $('div.item-summary-view-metadata').prepend(addthisbuttons);

        if (location.href.match(/show=full/) != null) {
        $('div#addthis').attr("addthis:url", (itemUrl).replace("?show=full", ""));
            $('div#at4-share.at-vertical-menu.addthis_32x32_style.ats-transparent.at4-show').attr("addthis:url", (itemUrl).replace("?show=full", ""));
        }
        else {
            $('div#addthis').attr("addthis:url", (document.URL));
        }

        if ($(window).width() <= 1080) { // This is to avoid the Add this toolbar from covering the footer signature
                $('#ds-content-wrapper').css('padding-bottom', '160px');
                $('#ds-footer-wrapper').css({'margin-top': '-160px', 'height': '160px'});
                $("#back-top").css('bottom','44px');
            }

    })
})(jQuery);