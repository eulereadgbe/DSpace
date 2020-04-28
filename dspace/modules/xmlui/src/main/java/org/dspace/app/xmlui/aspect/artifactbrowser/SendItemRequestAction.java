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
import org.dspace.app.requestitem.RequestItem;
import org.dspace.app.requestitem.RequestItemAuthor;
import org.dspace.app.requestitem.RequestItemAuthorExtractor;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.content.Bitstream;
import org.dspace.content.Metadatum;
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
import org.dspace.utils.DSpace;

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
        String userType = request.getParameter("userType");
        String userTypeOther = request.getParameter("userTypeOther");
        String organizationType = request.getParameter("organizationType");
        String organizationTypeOther = request.getParameter("organizationTypeOther");
        String institution = request.getParameter("institution");
        String userAddress = request.getParameter("userAddress");
        String agreement = request.getParameter("agreement");

        // User email from context
        Context context = ContextUtil.obtainContext(objectModel);
        EPerson loggedin = context.getCurrentUser();
        String eperson = null;
        if (loggedin != null)
        {
            eperson = loggedin.getEmail();
        }

        // Check all data is there
        if (StringUtils.isEmpty(requesterName) || StringUtils.isEmpty(requesterEmail) || StringUtils.isEmpty(allFiles) || StringUtils.isEmpty(message)
                || StringUtils.isEmpty(userType)
                || (StringUtils.equals(userType,"Others") && StringUtils.isEmpty(userTypeOther))
                || StringUtils.isEmpty(organizationType)
                || (StringUtils.equals(organizationType,"Others") && StringUtils.isEmpty(organizationTypeOther))
                || StringUtils.isEmpty(institution) || StringUtils.isEmpty(userAddress)
                || StringUtils.isEmpty(agreement))
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
            if (StringUtils.equals(userType,"Others") && StringUtils.isNotBlank(userTypeOther))
            {
                map.put("userType", userType);
                map.put("userTypeOther",userTypeOther);
            }
            else
            {
                map.put("userType", userType);
            }
            if (StringUtils.equals(organizationType,"Others") && StringUtils.isNotBlank(organizationTypeOther))
            {
                map.put("organizationType", organizationType);
                map.put("organizationTypeOther",organizationTypeOther);
            }
            else
            {
                map.put("organizationType", organizationType);
            }
            map.put("institution",institution);
            map.put("userAddress",userAddress);
            map.put("agreement",agreement);
            return map;
        }
    	DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (!(dso instanceof Item))
        {
            throw new Exception("Invalid DspaceObject at ItemRequest.");
        }
        
        Item item = (Item) dso;
        String title = "";
        Metadatum[] titleDC = item.getMetadata("dc","title", null, Item.ANY);
        Metadatum[] citationDC = item.getMetadata("dc", "identifier", "citation", Item.ANY);
        if (titleDC == null || titleDC.length == 0) {
            titleDC = item.getDC("title", Item.ANY, Item.ANY); // dc.title with qualifier term
        }
        if (titleDC != null && titleDC.length > 0) {
            if (citationDC != null && citationDC.length > 0){
                title = citationDC[0].value;
            } else {
                title = titleDC[0].value;
            }
        }
        
        RequestItemAuthor requestItemAuthor = new DSpace()
                .getServiceManager()
                .getServiceByName(
                        RequestItemAuthorExtractor.class.getName(),
                        RequestItemAuthorExtractor.class
                )
                .getRequestItemAuthor(context, item);

        RequestItem requestItem = new RequestItem(item.getID(), Integer.parseInt(bitstreamId), requesterEmail, requesterName, message, Boolean.valueOf(allFiles));

        // All data is there, send the email
        Email email = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "request_item.author"));
        email.addRecipient(requestItemAuthor.getEmail());

        email.addArgument(new Date()); // Date 0
        email.addArgument(requesterName);    // Name 1
        email.addArgument(requesterEmail);   // email 2
        email.addArgument(allFiles.equals("true")?I18nUtil.getMessage("itemRequest.all"):Bitstream.find(context,Integer.parseInt(bitstreamId)).getName());      
        email.addArgument(HandleManager.getCanonicalForm(item.getHandle()));      // Handle 4
        email.addArgument(title);    // request item title 5
        email.addArgument(message);   // message 6
        email.addArgument(getLinkTokenEmail(context,requestItem)); // Token link 7
        email.addArgument(requestItemAuthor.getFullName());    //   corresponding author name 8
        email.addArgument(requestItemAuthor.getEmail());    //   corresponding author email 9
        email.addArgument(ConfigurationManager.getProperty("dspace.name")); // 10
        email.addArgument(ConfigurationManager.getProperty("mail.helpdesk")); //11
        if (StringUtils.equals(userType,"Others"))
        {
            email.addArgument(userTypeOther); //12 User type
        }
        else {
            email.addArgument(userType); //12 User type
        }
        if (StringUtils.equals(organizationType,"Others"))
        {
            email.addArgument(organizationTypeOther); // Organization type 13
        }
        else {
            email.addArgument(organizationType); // Organization type 13
        }
        email.addArgument(institution);      // Institution 14
        email.addArgument(userAddress);      // Address 15


        email.setReplyTo(requesterEmail);
         
        email.send();
        // Finished, allow to pass.
        return null;
    }

    /**
     * Get the link to the author in RequestLink email.
     * @param context
     * @param requestItem
     * @return
     * @throws SQLException
     */
    protected String getLinkTokenEmail(Context context, RequestItem requestItem)
            throws SQLException
    {
        String base = ConfigurationManager.getProperty("dspace.url");

        String specialLink = (new StringBuffer()).append(base).append(
                base.endsWith("/") ? "" : "/").append(
                "itemRequestResponse/").append(requestItem.getNewToken(context))
                .toString()+"/";

        return specialLink;
    }

}
