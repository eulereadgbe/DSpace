/**
 * Created with IntelliJ IDEA.
 * User: euler
 * Date: 9/24/14
 * Time: 11:33 AM
 * To change this template use File | Settings | File Templates.
 */

//Use this for select
$(document).ready(function () {

    var userType = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_userType');
    var userTypeOther = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_userTypeOther');
    var organizationType = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_organization');
    var organizationTypeOther = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_organizationOther');
    var requestButton = $('input[type="submit"]');
    var agreement = $('input[type="checkbox"]');
    var email = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_email');
    var firstName = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_firstName');
    var lastName = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_lastName');
    var itemTitle = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_title');
    var institution = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_institution');
    var itemHandle = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_page');
    var textArea = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_message');
    var text = "To Whom It May Concern:\n\nPlease supply document: \":title:\" (:handle:)" +
        "\n\nI will use the requested materials only for private study, scholarship or research. " +
        "I understand that use for any other purpose may require the authorization of the copyright owner. " +
        "I accept responsibility for determining if copyright owner authorization is required and for obtaining" +
        " authorization when it is required.\n\nRegards,\n";


    var userAddress = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_userAddress');
    textArea.parent().after('<div id="id-preview"/>');
    var preview = $('#id-preview');
    var required = $('#aspect_artifactbrowser_DocumentDeliveryForm_field_email,' +
        '#aspect_artifactbrowser_DocumentDeliveryForm_field_firstName,' +
        '#aspect_artifactbrowser_DocumentDeliveryForm_field_institution').addClass('required');
    $(':text.required').bind('input', function() {
        var NOTfilled = $(':text.required').filter(function() {
            return !this.value;
        });
        if (NOTfilled.length) {
            textArea.hide();
            preview.hide();
            $("label[for='aspect_artifactbrowser_DocumentDeliveryForm_field_message']").hide();

        } else {
            $("label[for='aspect_artifactbrowser_DocumentDeliveryForm_field_message']").slideDown('easing');
            textArea.slideDown();
        }
    });

    userAddress.parent().addClass('yui3-skin-sam');
    textArea.focus(function(){
        text = text.replace(":title:", itemTitle.val());
        text = text.replace(":handle:", itemHandle.val());
        $(this).val(text + firstName.val() + " " + lastName.val() +
            " <" + email.val() + ">\n" + institution.val() + "\n" + userAddress.val());
    });

    textArea.bind('keyup mouseout',function() {
        var keyed = textArea.val().replace("<", "&lt;").replace(">", "&gt;").replace(/\n/g, '<br/>');
        preview.slideDown('fast').html("<p>This is the text to be sent to the responsible person.</p>" +
                keyed).css({"border": "2px dotted #ccc", "word-wrap": "break-word", "width": "508px",
                "padding": "10px"});
    });

    (agreement.is(':checked') == false) ?
        requestButton.attr("disabled","disabled") : requestButton.removeAttr('disabled');

    (userType.val() != "Others") ?
        userTypeOther.hide() : userTypeOther.slideDown('fast');

    (organizationType.val() != "Others") ?
        organizationTypeOther.hide() : organizationTypeOther.slideDown('fast');

    userType.change(function() {
        ($(this).val() == "Others") ?
            userTypeOther.slideDown('fast') : userTypeOther.hide() && userTypeOther.next('span').hide();
    });

    organizationType.change(function() {
        ($(this).val() == "Others") ?
            organizationTypeOther.slideDown('fast') : organizationTypeOther.hide() && organizationTypeOther.next('span').hide();
    });

    agreement.click(function(){
        (($(this).is(':checked') == false) ?
            requestButton.attr("disabled","disabled") : requestButton.removeAttr('disabled'));
    });

    $.getScript("http://yui.yahooapis.com/3.18.0/build/yui/yui-min.js", function () {

        console.log("Script loaded and executed.");

        // Autocomplete user address
        YUI().use('autocomplete', function (Y) {
            var acNode = Y.one('#aspect_artifactbrowser_DocumentDeliveryForm_field_userAddress');

            acNode.plug(Y.Plugin.AutoComplete, {
                // Highlight the first result of the list.
                activateFirstItem: true,

                // The list of the results contains up to 10 results.
                maxResults: 10,

                // To display the suggestions, the minimum of typed chars is five.
                minQueryLength: 3,

                // Number of milliseconds to wait after user input before triggering a
                // `query` event. This is useful to throttle queries to a remote data
                // source.
                queryDelay: 500,

                // Handling the list of results is mandatory, because the service can be
                // unavailable, can return an error, one result, or an array of results.
                // However `resultListLocator` needs to always return an array.
                resultListLocator: function (response) {
                    // Makes sure an array is returned even on an error.
                    if (response.error) {
                        return [];
                    }

                    var query = response.query.results.json,
                        addresses;

                    if (query.status !== 'OK') {
                        return [];
                    }

                    // Grab the actual addresses from the YQL query.
                    addresses = query.results;

                    // Makes sure an array is always returned.
                    return addresses.length > 0 ? addresses : [addresses];
                },

                // When an item is selected, the value of the field indicated in the
                // `resultTextLocator` is displayed in the input field.
                resultTextLocator: 'formatted_address',

                // {query} placeholder is encoded, but to handle the spaces correctly,
                // the query is has to be encoded again:
                //
                // "my address" -> "my%2520address" // OK => {request}
                // "my address" -> "my%20address"   // OK => {query}
                requestTemplate: function (query) {
                    return encodeURI(query);
                },

                // {request} placeholder, instead of the {query} one, this will insert
                // the `requestTemplate` value instead of the raw `query` value for
                // cases where you actually want a double-encoded (or customized) query.
                source: 'SELECT * FROM json WHERE ' +
                    'url="http://maps.googleapis.com/maps/api/geocode/json?' +
                    'sensor=false&' +
                    'address={request}"',

                // Automatically adjust the width of the dropdown list.
                width: 'auto'
            });

            // Adjust the width of the input container.
            acNode.ac.after('resultsChange', function () {
                var newWidth = this.get('boundingBox').get('offsetWidth');
                acNode.setStyle('width', Math.max(newWidth, 100));
            });
        });
    });
});