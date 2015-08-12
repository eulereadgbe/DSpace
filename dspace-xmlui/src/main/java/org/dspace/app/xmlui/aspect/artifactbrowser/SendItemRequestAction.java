/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.artifactbrowser;

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
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.EPerson;
import org.dspace.handle.HandleManager;
import org.dspace.utils.DSpace;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
       
        String requesterEmail = request.getParameter("requesterEmail");
        String allFiles = request.getParameter("allFiles");
        String message = request.getParameter("message");
        String bitstreamId = request.getParameter("bitstreamId");
        String lastName = request.getParameter("lastName");
        String firstName = request.getParameter("firstName");
        String institution = request.getParameter("institution");
        String userAddress = request.getParameter("userAddress");
        String userType = request.getParameter("userType");
        String userTypeOther = request.getParameter("userTypeOther");
        String organization = request.getParameter("organization");
        String organizationOther = request.getParameter("organizationOther");
        String reason = request.getParameter("reason");
        String requiredField = request.getParameter("requiredField");

        // User email from context
        Context context = ContextUtil.obtainContext(objectModel);
        EPerson loggedin = context.getCurrentUser();
        String eperson = null;
        if (loggedin != null)
        {
            eperson = loggedin.getEmail();
        }

        // Check all data is there
        if (StringUtils.isEmpty(lastName) || StringUtils.isEmpty(requesterEmail) || StringUtils.isEmpty(allFiles)
                || (institution == null) || institution.equals("")
                || (userAddress == null) || userAddress.equals("")
                || (userType == null) || userType.equals("")
                || StringUtils.isEmpty(firstName) || StringUtils.isEmpty(lastName)
                || (organization == null) || organization.equals("")
                || (organization.equals("Others") && (organizationOther == null || organizationOther.equals("")))
                || (userType.equals("Others") && (userTypeOther == null || userTypeOther.equals("")))
                || (reason == null) || reason.equals("")
                || StringUtils.isEmpty(message)
                || StringUtils.isNotEmpty(requiredField) || StringUtils.isNotBlank(requiredField) || !requiredField.equals(""))
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
            map.put("lastName", lastName);
            map.put("firstName", firstName);
            map.put("institution", institution);
            map.put("userAddress", userAddress);
            if (StringUtils.equals(userType, "Others")) {
                map.put("userType", userType);
                map.put("userTypeOther", userTypeOther);
            } else {
                map.put("userType", userType);
            }
            if (StringUtils.equals(organization, "Others")) {
                map.put("organization", organization);
                map.put("organizationOther", organizationOther);
            } else {
                map.put("organization", organization);
            }
            map.put("allFiles",allFiles);
            map.put("message",message);
            map.put("reason", reason);
            map.put("requiredField", requiredField);
            return map;
        }
    	DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (!(dso instanceof Item))
        {
            throw new Exception("Invalid DspaceObject at ItemRequest.");
        }

        Item item = (Item) dso;
        String title = "";
        Metadatum[] titleDC = item.getDC("title", null, Item.ANY);
        if (titleDC == null || titleDC.length == 0) {
            titleDC = item.getDC("title", Item.ANY, Item.ANY); // dc.title with qualifier term
        }
        if (titleDC != null && titleDC.length > 0) {
            title = titleDC[0].value;
        }
        
        RequestItemAuthor requestItemAuthor = new DSpace()
                .getServiceManager()
                .getServiceByName(
                        RequestItemAuthorExtractor.class.getName(),
                        RequestItemAuthorExtractor.class
                )
                .getRequestItemAuthor(context, item);
        String requesterName = firstName + " " + lastName;
        RequestItem requestItem = new RequestItem(item.getID(), Integer.parseInt(bitstreamId), requesterEmail,
                requesterName, message, Boolean.getBoolean(allFiles));

        // All data is there, send the email
        Email email = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "request_item.author"));
        email.addRecipient(requestItemAuthor.getEmail());

        email.addArgument(lastName);
        email.addArgument(firstName);
        email.addArgument(requesterEmail);
        email.addArgument(allFiles.equals("true")?I18nUtil.getMessage("itemRequest.all"):Bitstream.find(context,Integer.parseInt(bitstreamId)).getName());      
        email.addArgument(HandleManager.getCanonicalForm(item.getHandle()));
        email.addArgument(title);    // request item title
        email.addArgument(message);   // message
        email.addArgument(getLinkTokenEmail(context,requestItem));
        email.addArgument(requestItemAuthor.getFullName());    //   corresponding author name
        email.addArgument(requestItemAuthor.getEmail());    //   corresponding author email
        email.addArgument(ConfigurationManager.getProperty("dspace.name"));
        email.addArgument(ConfigurationManager.getProperty("mail.helpdesk"));
        email.addArgument(institution); //12
        email.addArgument(userAddress); //13
        if (StringUtils.equals(userType,"Others"))
        {
            email.addArgument(userTypeOther); //14 User type
        }
        else {
            email.addArgument(userType); //14 User type
        }
        if (StringUtils.equals(organization,"Others"))
        {
            email.addArgument(organizationOther); //15 Organization type
        }
        else {
            email.addArgument(organization); //15 Organization type
        }

        email.addArgument(reason);   //16 Reason for requesting
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
