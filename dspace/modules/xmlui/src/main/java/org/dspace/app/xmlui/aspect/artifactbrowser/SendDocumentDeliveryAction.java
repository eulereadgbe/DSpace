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
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.EPerson;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.content.service.ItemService;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Scott Phillips
 */

public class SendDocumentDeliveryAction extends AbstractAction
{
    private final transient ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    private final transient HandleService handleService = HandleServiceFactory.getInstance().getHandleService();

    /**
     *
     */
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel,
            String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        String page = request.getParameter("page");
        String address = request.getParameter("email");
        String agent = request.getHeader("User-Agent");
        String session = request.getSession().getId();
        String purpose = request.getParameter("purpose");
        String handle = parameters.getParameter("handle");
        String requesterName = request.getParameter("requesterName");
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
        String host = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.hostname");

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

        if ((fromPage == null) || ((!fromPage.contains(basicHost)) && (!isValidReferral(fromPage))))
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
        if ((address == null) || address.equals("")
                || (purpose == null) || purpose.equals("")
                || (requesterName == null) || requesterName.equals("")
                || (userAddress == null) || userAddress.equals("")
                || (institution == null) || institution.equals("")
                || (userType == null) || userType.equals("")
                || (agreement == null) || agreement.equals("")
                || (StringUtils.equals(userType,"Others") && StringUtils.isEmpty(userTypeOther))
                || StringUtils.isEmpty(organizationType)
                || (StringUtils.equals(organizationType,"Others") && StringUtils.isEmpty(organizationTypeOther))
                || (handle == null) || handle.equals(""))
        {
            // Either the user did not fill out the form or this is the
            // first time they are visiting the page.
            Map<String,String> map = new HashMap<String,String>();
            map.put("page",page);

            if (address == null || address.equals(""))
            {
                map.put("email", eperson);
            }
            else
            {
                map.put("email", address);
            }

            map.put("requesterName",requesterName);
            map.put("userAddress",userAddress);
            map.put("purpose",purpose);
            map.put("handle",handle);
            map.put("institution",institution);
            map.put("userAddress",userAddress);
            map.put("agreement",agreement);
            map.put("userType", userType);
            if (StringUtils.equals(userType,"Others") && StringUtils.isNotBlank(userTypeOther))
            {
                map.put("userTypeOther",userTypeOther);
            }
            map.put("organizationType", organizationType);
            if (StringUtils.equals(organizationType,"Others") && StringUtils.isNotBlank(organizationTypeOther))
            {
                map.put("organizationTypeOther",organizationTypeOther);
            }
            return map;
        }

        DSpaceObject dso = handleService.resolveToObject(context, handle);
        Item item = (Item) dso;

        String citationDC = itemService.getMetadataFirstValue(item, "dc", "identifier", "citation", Item.ANY);
        String titleDC = item.getName();
        String title = "";
        if (citationDC != null && citationDC.length() > 0) {
            title = citationDC;
        } else {
            if (titleDC != null && titleDC.length() > 0)
                title = titleDC;
        }
            // All data is there, send the email
        Email email = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "documentdelivery"));
        email.addRecipient(DSpaceServicesFactory.getInstance().getConfigurationService()
                .getProperty("mail.documentdelivery.recipient"));

        email.addArgument(new Date()); //00 Date
        email.addArgument(address);    //01 Email
        email.addArgument(eperson);    //02 Logged in as
        email.addArgument(page.replace("/documentdelivery/", "/handle/")); //03 Referring page
        email.addArgument(agent);      //04 User agent
        email.addArgument(session);    //05 Session ID
        email.addArgument(purpose);   //06 The feedback itself
        email.addArgument(title);    //07 request item title
        email.addArgument(handleService.getCanonicalForm(item.getHandle())); //08 Handle
        email.addArgument(requesterName); //09 Requester Name
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
        email.setReplyTo(address);

        // May generate MessageExceptions.
        email.send();

        // Finished, allow to pass.
        return null;
    }

    private boolean isValidReferral(String fromPage)
    {
        String[] allowedReferrers = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty("mail.allowed.referrers");
        if (allowedReferrers != null && fromPage != null)
        {
            for (String allowedReferrer : allowedReferrers)
            {
                if (fromPage.contains(allowedReferrer))
                {
                    return true;
                }
            }
        }

        return false;
    }

}
