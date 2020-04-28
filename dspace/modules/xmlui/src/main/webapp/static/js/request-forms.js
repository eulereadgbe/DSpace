$(document).ready(function () {
    var userType = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_userType,#aspect_artifactbrowser_ItemRequestForm_field_userType');
    var userTypeOther = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_userTypeOther,#aspect_artifactbrowser_ItemRequestForm_field_userTypeOther');
    var organizationType = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_organizationType,#aspect_artifactbrowser_ItemRequestForm_field_organizationType');
    var organizationTypeOther = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_organizationTypeOther,#aspect_artifactbrowser_ItemRequestForm_field_organizationTypeOther');
    var userAddress = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_userAddress,#aspect_artifactbrowser_ItemRequestForm_field_userAddress');

    (userType.val() != "Others") ?
        userTypeOther.hide() && userTypeOther.next('p').hide() : userTypeOther.slideDown('fast') && userTypeOther.next('p').slideDown('slow') && userTypeOther.removeClass('hidden');

    userType.change(function () {
        ($(this).val() == "Others") ?
            userTypeOther.slideDown('fast') && userTypeOther.next('p').slideDown('slow') && userTypeOther.removeClass('hidden') : userTypeOther.hide() && userTypeOther.next('p').hide();
    });

    (organizationType.val() != "Others") ?
        organizationTypeOther.hide() && organizationTypeOther.next('p').hide() : organizationTypeOther.slideDown('fast') && organizationTypeOther.next('p').slideDown('slow') && organizationTypeOther.removeClass('hidden');

    organizationType.change(function () {
        ($(this).val() == "Others") ?
            organizationTypeOther.slideDown('fast') && organizationTypeOther.next('p').slideDown('slow') && organizationTypeOther.removeClass('hidden') : organizationTypeOther.hide() && organizationTypeOther.next('p').hide();
    });

$(userAddress).attr('placeholder', 'Enter your address');

});