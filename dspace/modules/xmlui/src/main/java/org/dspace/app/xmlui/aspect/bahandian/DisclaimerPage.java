/**
 * AboutPage.java
 *
 * Basead on the code by Peter Dietz:
 * https://gist.github.com/842301#file_dspace_add_about.diff (acessed 11-05-23)
 *
 * Modified to work with internationalization (i18n locales) and breadcrumbs
 * by Andre Nito Assada e Josi Perez Alvarez on 11-05-23
 */

package org.dspace.app.xmlui.aspect.bahandian;


import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.xml.sax.SAXException;


/**
 * Display about us page.
 *
 * @author Peter Dietz
 */
public class DisclaimerPage extends AbstractDSpaceTransformer
{

    /**
     * Internationalization
     * 110523
     */
    public static final Message T_dspace_home =
            message("xmlui.general.dspace_home");
    public static final Message T_title =
            message("xmlui.Bahandian.DisclaimerPage.title");
    public static final Message T_trail =
            message("xmlui.Bahandian.DisclaimerPage.trail");
    public static final Message T_head =
            message("xmlui.Bahandian.DisclaimerPage.head");
    public static final Message T_para =
            message("xmlui.Bahandian.DisclaimerPage.para");

    private static Logger log = Logger.getLogger(DisclaimerPage.class);

    /**
     * Add a page title and trail links.
     */
    public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException {
        // Set the page title

        // pageMeta.addMetadata("title").addContent("About Us");
        // 110523 modified page title with internationalization and added breadcrumbs
        pageMeta.addMetadata("title").addContent(T_title);
        // add trail
        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        pageMeta.addTrail().addContent(T_trail);
    }

    /**
     * Add some basic contents
     */
    public void addBody(Body body) throws SAXException, WingException {
        //Division division = body.addDivision("about-page", "primary");
        //Division.setHead("About Us - Institutional Repository");
        //Division.addPara("We are an institutional repository that specializes in storing your digital artifacts.");

        //110523 modified with internationalization
        Division division = body.addDivision("disclaimer", "primary");
        division.setHead(T_head);
        division.addPara(T_para);
    }
}