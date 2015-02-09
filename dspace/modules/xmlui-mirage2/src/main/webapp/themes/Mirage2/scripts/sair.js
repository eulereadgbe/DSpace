$(function () {
    $("[data-toggle='tooltip']").tooltip({
        title: '<a style="color: #ffffff" href="#" data-toggle="modal" data-target="#externalLinkModal"><b>EXTERNAL LINKS DISCLAIMER</b></a>',
        html: 'true',
        delay: {show: 0, hide: 5000}});
    $('a').filter(function () {
        return this.hostname && this.hostname !== location.hostname;
    }).addClass("external");
    $('.file-wrapper a.image-link').addClass("external");
    $(".external").attr('target','_blank');
    $(".simple-item-view-show-full").addClass('hidden-xs');
});
