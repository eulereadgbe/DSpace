$(function () {
    $("[data-toggle='tooltip']").tooltip();
    //$(".social-links > li > a").addClass('external');
    //$(".external").attr('target','_blank');
    //Open External Links In New Tab/Window
    $('a').filter(function () {
        return this.hostname && this.hostname !== location.hostname;
    }).addClass("external");
    $(".external").attr('target','_blank');
    $(".simple-item-view-show-full").addClass('hidden-xs');
});
