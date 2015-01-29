/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.artifactbrowser;

import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.authorize.AuthorizeException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

/**
 * Simple page to let the user know their documentdelivery has been sent.
 * 
 * @author Scott Phillips
 */
public class DocumentDeliverySent extends AbstractDSpaceTransformer implements CacheableProcessingComponent
{
    /** language strings */
    public static final Message T_title =
        message("xmlui.ArtifactBrowser.DocumentDeliverySent.title");
    
    public static final Message T_dspace_home =
        message("xmlui.general.dspace_home");
    
    public static final Message T_trail = 
        message("xmlui.ArtifactBrowser.DocumentDeliverySent.trail");
    
    public static final Message T_head =
        message("xmlui.ArtifactBrowser.DocumentDeliverySent.head");

    public static final Message T_para1 =
            message("xmlui.ArtifactBrowser.DocumentDeliverySent.para1");

    public static final Message T_notice =
            message("xmlui.ArtifactBrowser.DocumentDeliverySent.notice");

    
    /**
     * Generate the unique caching key.
     */
    public Serializable getKey() {
        return "1";
    }

    /**
     * Generate the cache validity object.
     */
    public SourceValidity getValidity() 
    {
       return NOPValidity.SHARED_INSTANCE;
    }
    
    
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
        
        pageMeta.addMetadata("title").addContent(T_title);
       
        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        pageMeta.addTrail().addContent(T_trail);
    }

  
    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
        Division documentdelivery = body.addDivision("documentdelivery-sent","primary");
     
        documentdelivery.setHead(T_head);

        documentdelivery.addPara(T_para1);

        documentdelivery.addPara(T_notice);

    }
}
