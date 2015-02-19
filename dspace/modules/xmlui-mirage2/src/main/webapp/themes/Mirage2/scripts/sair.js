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
$(function(){

    var external_link = $("#external-link");
    if (external_link.length) {
        // looking at the href of the link, if it contains .pdf or .doc or .mp3
        var link = external_link.attr('href');


        var url= "http://json-head.appspot.com/?url="+encodeURI(link)+"&callback=?";

        // then call the json thing and insert the size back into the link text
        $.getJSON(url, function(json){
            if(json.ok && json.headers['content-length']) {
                var length = parseInt(json.headers['content-length'], 10);
                var content = json.headers['content-type'].split('/');
                var format = content[content.length -1];
                // divide the length into its largest unit
                var units = [
                    [1024 * 1024 * 1024, 'Gb'],
                    [1024 * 1024, 'Mb'],
                    [1024, 'Kb'],
                    [1, 'Bytes']
                ];

                for(var i = 0; i < units.length; i++){

                    var unitSize = units[i][0];
                    var unitText = units[i][1];

                    if (length >= unitSize) {
                        length = length / unitSize;
                        // 1 decimal place
                        length = Math.ceil(length * 10) / 10;
                        var lengthUnits = unitText;
                        break;
                    }
                }

                // insert the text directly after the link and add a class to the link
                if (format.indexOf("html") === 0) {
                    $('.type').after(' [' + format.substring(0,4) + ']');
                }
                else {
                    $('.type').after(' [' + format.substring(0,3) + ']');
                }
                if (length != 0) {
                $(".fa-external-link").before(' (' + length + ' ' + lengthUnits + ') ');
                }
            }
        });
    }
});
