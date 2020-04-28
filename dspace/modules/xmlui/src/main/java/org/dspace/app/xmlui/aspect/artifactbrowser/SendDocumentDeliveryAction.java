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
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.EPerson;
import org.dspace.handle.HandleManager;
import org.dspace.content.DSpaceObject;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Scott Phillips
 * @author AdÃ¡n RomÃ¡n Ruiz at arvo.es
 */

public class SendDocumentDeliveryAction extends AbstractAction
{

    /**
     *
     */
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel,
            String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        String requesterName = request.getParameter("requesterName");
        String requesterEmail = request.getParameter("requesterEmail");
        String page = request.getParameter("page");
        String agent = request.getHeader("User-Agent");
        String session = request.getSession().getId();
        String purpose = request.getParameter("purpose");
        String handle = parameters.getParameter("handle");
        String title = request.getParameter("title");
        String userType = request.getParameter("userType");
        String userTypeOther = request.getParameter("userTypeOther");
        String organizationType = request.getParameter("organizationType");
        String organizationTypeOther = request.getParameter("organizationTypeOther");
        String institution = request.getParameter("institution");
        String userAddress = request.getParameter("userAddress");
        String agreement = request.getParameter("agreement");

        // Obtain information from request
        // The page where the user came from
        String fromPage = request.getHeader("Referer");
        // Prevent spammers and splogbots from poisoning the feedback page
        String host = ConfigurationManager.getProperty("dspace.hostname");
        String allowedReferrersString = ConfigurationManager.getProperty("mail.allowed.referrers");

        String[] allowedReferrersSplit = null;
        boolean validReferral = false;

        if((allowedReferrersString != null) && (allowedReferrersString.length() > 0))
        {
            allowedReferrersSplit = allowedReferrersString.trim().split("\\s*,\\s*");
            for(int i = 0; i < allowedReferrersSplit.length; i++)
            {
                if(fromPage.indexOf(allowedReferrersSplit[i]) != -1)
                {
                    validReferral = true;
                    break;
                }
            }
        }

        String basicHost = "";
        if ("localhost".equals(host) || "127.0.0.1".equals(host)
                        || host.equals(InetAddress.getLocalHost().getHostAddress()))
        {
            basicHost = host;
        }
        else
        {
            // cut off all but the hostname, to cover cases where more than one URL
            // arrives at the installation; e.g. presence or absence of "www"
            int lastDot = host.lastIndexOf('.');
            basicHost = host.substring(host.substring(0, lastDot).lastIndexOf('.'));
        }

        if ((fromPage == null) || ((fromPage.indexOf(basicHost) == -1) && (!validReferral)))
        {
            // N.B. must use old message catalog because Cocoon i18n is only available to transformed pages.
            throw new AuthorizeException(I18nUtil.getMessage("feedback.error.forbidden"));
        }

        // User email from context
        Context context = ContextUtil.obtainContext(objectModel);
        EPerson loggedin = context.getCurrentUser();
        String eperson = null;
        if (loggedin != null)
        {
            eperson = loggedin.getEmail();
        }

        if (page == null || page.equals(""))
        {
            page = fromPage;
        }

        // Check all data is there
        //if ( (purpose == null) || purpose.equals("")
        //        || (handle == null) || handle.equals("")
        //        || (title == null) || title.equals(""))
        if (StringUtils.isEmpty(requesterName) || StringUtils.isEmpty(requesterEmail) || StringUtils.isEmpty(purpose)
                || StringUtils.isEmpty(handle) || StringUtils.isEmpty(title) || StringUtils.isEmpty(userType)
                || (StringUtils.equals(userType,"Others") && StringUtils.isEmpty(userTypeOther))
                || StringUtils.isEmpty(organizationType)
                || (StringUtils.equals(organizationType,"Others") && StringUtils.isEmpty(organizationTypeOther))
                || StringUtils.isEmpty(institution) || StringUtils.isEmpty(userAddress)
                || StringUtils.isEmpty(agreement))
        {
            // Either the user did not fill out the form or this is the
            // first time they are visiting the page.
            Map<String,String> map = new HashMap<String,String>();
            map.put("page",page);

            if (StringUtils.isEmpty(requesterEmail))
            {
                map.put("requesterEmail", eperson);
            }
            else
            {
                map.put("requesterEmail", requesterEmail);
            }
            map.put("requesterName",requesterName);
            map.put("purpose",purpose);
            map.put("handle",handle);
            map.put("title",title);
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

        DSpaceObject dso = HandleManager.resolveToObject(context, handle);
        Item item = (Item) dso;

        // All data is there, send the email
        Email email = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "DocumentDelivery"));
        email.addRecipient(ConfigurationManager.getProperty("mail.helpdesk"));

        email.addArgument(new Date()); // Date 0
        email.addArgument(requesterEmail);    // Email 1
        email.addArgument(eperson);    // Logged in as 2
        email.addArgument(page.replace("/documentdelivery/", "/handle/"));       // Referring page 3
        email.addArgument(agent);      // User agent 4
        email.addArgument(session);    // Session ID 5
        email.addArgument(purpose);   // The feedback itself 6
        email.addArgument(title);      // Item requested 7
        email.addArgument(HandleManager.getCanonicalForm(item.getHandle())); // Item handle 8
        email.addArgument(requesterName); // Requester Name 9
        if (StringUtils.equals(userType,"Others"))
        {
            email.addArgument(userTypeOther); //10 User type
        }
        else {
            email.addArgument(userType); //10 User type
        }
        if (StringUtils.equals(organizationType,"Others"))
        {
            email.addArgument(organizationTypeOther); // Organization type 11
        }
        else {
            email.addArgument(organizationType); // Organization type 11
        }
        email.addArgument(institution);      // Institution 12
        email.addArgument(userAddress);      // Address 13

        // Replying to feedback will reply to email on form
        email.setReplyTo(eperson);

        // May generate MessageExceptions.
        email.send();

        // Finished, allow to pass.
        return null;
    }

}
