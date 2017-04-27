/**
 * Functions to add citation count or other statistics from several
 * services (like Scopus) to the impact section of an item view.
 */

var IMPACT_SERVLET_URL = "/impact/ImpactServlet.py";

function addImpactServiceCitationCount(service) {
    var idSelector = "#" + service;
    var doi = $(idSelector).data("doi");
    $.getJSON(IMPACT_SERVLET_URL + "?service=" + service + "&doi=" + doi)
        .done(function(data) {
            var citations = data["citationCount"];
            var linkBack = data["linkBack"];
            if (citations && linkBack) {
                $(idSelector + " a").attr("href", linkBack);
                $(idSelector + " .citation-count").html(citations);
                $(idSelector).removeClass("hidden");
            }
        })
        .fail(function(jqxhr, textStatus, error) {
            console.log("Error retrieving " + service + " citation count: " + error);
        });
}

function addCitationCounts() {
    addImpactServiceCitationCount("wos");
    addImpactServiceCitationCount("scopus");
}

$(document).ready(addCitationCounts);