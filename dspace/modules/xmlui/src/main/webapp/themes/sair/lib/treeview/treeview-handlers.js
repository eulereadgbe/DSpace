/*
 *	@author Colin Gormley
 *	treeview-handlers.js
 *	
 *	Configures treeview behaviour
 */

$(document).ready(function () {
    // All links in Community treeview go through this function
    // This ensures that clicking on a node that is a link doesn't
    // expand the tree - instead it follows the link
    $var('#tree a').click(function () {
        // follow the link in the href attribute
        location.href = this.href;
        // have to return false to prevent event bubbling - otherwise the tree
        // would expand
        // briefly before the href target page opens up
        return false;
    });

    // all trees set to be collapsed by default
    $var("#tree").treeview({
        animated:"medium",
        collapsed:false
    });

});