$(function($) {
    $(document).ready(function () {
        $("#ds-header-wrapper").backstretch("http://localhost:8181/themes/sair/images/SAIR-banner.jpg");
        $("#ds-header-logo").backstretch("http://localhost:8181/themes/sair/images/seafdec-logo.png");
    });

    $('#aspect_discovery_Navigation_list_discovery ul li h2, #aspect_viewArtifacts_Navigation_list_browse ul li h2').click
    (function (event) {
        var elem = $(this).next();
        if (elem.is('ul')) {
            event.preventDefault();
            $('#menu ul:visible').not(elem).slideUp();
            elem.slideToggle();
        }
    });

    $(document).ready(function () {
        $("#breadCrumb").jBreadCrumb();
    });

});
