/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.content.Bitstream;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.core.Utils;
import org.dspace.eperson.EPerson;
import org.dspace.handle.HandleManager;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;

 /**
 * This action will send a mail to request a item to administrator when all mandatory data is present.
 * It will record the request into the database.
 * 
 * Original Concept, JSPUI version:    Universidade do Minho   at www.uminho.pt
 * Sponsorship of XMLUI version:    Instituto Oceanográfico de España at www.ieo.es
 * 
 * @author Adán Román Ruiz at arvo.es (added request item support)
 */
public class SendItemRequestAction extends AbstractAction
{
    private static Logger log = Logger.getLogger(SendItemRequestAction.class);

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel,
            String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
       
        String requesterName = request.getParameter("requesterName");
        String requesterEmail = request.getParameter("requesterEmail");
        String allFiles = request.getParameter("allFiles");
        String message = request.getParameter("message");
        String bitstreamId = request.getParameter("bitstreamId");
     
        // User email from context
        Context context = ContextUtil.obtainContext(objectModel);
        EPerson loggedin = context.getCurrentUser();
        String eperson = null;
        if (loggedin != null)
        {
            eperson = loggedin.getEmail();
        }

        // Check all data is there
        if (StringUtils.isEmpty(requesterName) || StringUtils.isEmpty(requesterEmail) || StringUtils.isEmpty(allFiles) || StringUtils.isEmpty(message))
        {
            // Either the user did not fill out the form or this is the
            // first time they are visiting the page.
            Map<String,String> map = new HashMap<String,String>();
            map.put("bitstreamId",bitstreamId);

            if (StringUtils.isEmpty(requesterEmail))
            {
                map.put("requesterEmail", eperson);
            }
            else
            {
                map.put("requesterEmail", requesterEmail);
            }
            map.put("requesterName",requesterName);
            map.put("allFiles",allFiles);
            map.put("message",message);
            return map;
        }
    	DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (!(dso instanceof Item))
        {
            throw new Exception("Invalid DspaceObject at ItemRequest.");
        }
        
        Item item = (Item) dso;
        String title="";
        DCValue[] citationDC = item.getMetadata("dc", "identifier", "citation", Item.ANY);
        DCValue[] titleDC = item.getMetadata("dc", "title", null, Item.ANY);
        if (citationDC != null || citationDC.length > 0) {
            title=citationDC[0].value;
        }
        else if (titleDC != null || titleDC.length > 0) {
            title=titleDC[0].value;
        }
        String emailRequest;
        EPerson submitter = item.getSubmitter();
        if(submitter!=null){
            emailRequest=submitter.getEmail();
        }else{
            emailRequest=ConfigurationManager.getProperty("mail.helpdesk");
        }
        if(emailRequest==null){
            emailRequest=ConfigurationManager.getProperty("mail.admin");
        }
        // All data is there, send the email
        Email email = ConfigurationManager.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "request_item.author"));
        email.addRecipient(emailRequest);

        email.addArgument(requesterName);    
        email.addArgument(requesterEmail);   
        email.addArgument(allFiles.equals("true")?I18nUtil.getMessage("itemRequest.all"):Bitstream.find(context,Integer.parseInt(bitstreamId)).getName());      
        email.addArgument(HandleManager.getCanonicalForm(item.getHandle()));      
        email.addArgument(title);    // request item title
        email.addArgument(message);   // message
        email.addArgument(getLinkTokenEmail(context,request, bitstreamId, item.getID(), requesterEmail, requesterName, Boolean.parseBoolean(allFiles)));    
        email.addArgument(submitter.getFullName());    //   submmiter name
        email.addArgument(submitter.getEmail());    //   submmiter email
        email.addArgument(ConfigurationManager.getProperty("dspace.name"));
        email.addArgument(ConfigurationManager.getProperty("mail.helpdesk"));

        email.setReplyTo(requesterEmail);
         
        email.send();
        // Finished, allow to pass.
        return null;
    }

    /**
     * Get the link to the author in RequestLink email.
     * 
     * @param email
     *            The email address to mail to
     *
     * @exception SQLExeption
     *
     */
    protected String getLinkTokenEmail(Context context,Request request, String bitstreamId
            , int itemID, String reqEmail, String reqName, boolean allfiles)
            throws SQLException
    {
        String base = ConfigurationManager.getProperty("dspace.url");
        
        request.getPathInfo();
        String specialLink = (new StringBuffer()).append(base).append(
                base.endsWith("/") ? "" : "/").append(
                "itemRequestResponse/").append(getNewToken(context, Integer.parseInt(bitstreamId), itemID, reqEmail, reqName, allfiles))
                .toString()+"/";
        
        return specialLink;
    }
    /**
     * Generate a unique id of the request and put it into the ddbb 
     * @param context
     * @param bitstreamId
     * @param itemID
     * @param reqEmail
     * @param reqName
     * @param allfiles
     * @return
     * @throws SQLException
     */
    protected String getNewToken(Context context, int bitstreamId, int itemID, String reqEmail, String reqName, boolean allfiles) throws SQLException
    {
        TableRow rd = DatabaseManager.create(context, "requestitem");
        rd.setColumn("token", Utils.generateHexKey());
        rd.setColumn("bitstream_id", bitstreamId);
        rd.setColumn("item_id",itemID);
        rd.setColumn("allfiles", allfiles);
        rd.setColumn("request_email", reqEmail);
        rd.setColumn("request_name", reqName);
        rd.setColumnNull("accept_request");
        rd.setColumn("request_date", new Date());
        rd.setColumnNull("decision_date");
        rd.setColumnNull("expires");

        DatabaseManager.update(context, rd);

        if (log.isDebugEnabled())
        {
            log.debug("Created requestitem_token "
                    + rd.getIntColumn("requestitem_id")
                    + " with token " + rd.getStringColumn("token")
                    +  "\"");
        }
        return rd.getStringColumn("token");
         
    }
}