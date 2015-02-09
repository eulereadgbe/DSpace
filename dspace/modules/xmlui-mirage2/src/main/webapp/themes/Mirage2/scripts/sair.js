$(function () {
    //Open External Links In New Tab/Window
    $('a').filter(function () {
        return this.hostname && this.hostname !== location.hostname;
    }).addClass("external");
    $('.file-wrapper a.image-link').addClass("external");
    $(".external").attr('target','_blank');
    $(".simple-item-view-show-full").addClass('hidden-xs');
});
