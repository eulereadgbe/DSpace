$(function () {
    $('#aspect_discovery_Navigation_list_discovery ul li h2').click
    (function (event) {
        var elem = $(this).next();
        if (elem.is('ul')) {
            event.preventDefault();
            $('#menu ul:visible').not(elem).slideUp();
            elem.slideToggle();
        }
    });
});
