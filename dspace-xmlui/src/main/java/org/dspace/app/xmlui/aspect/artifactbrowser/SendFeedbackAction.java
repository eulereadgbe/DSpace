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
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.EPerson;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Scott Phillips
 */

public class SendFeedbackAction extends AbstractAction
{

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
        String comments = request.getParameter("comments");
        String lastName = request.getParameter("lastName");
        String firstName = request.getParameter("firstName");
        String institution = request.getParameter("institution");
        String userAddress = request.getParameter("userAddress");
        String userType = request.getParameter("userType");
        String userTypeOther = request.getParameter("userTypeOther");
        String organization = request.getParameter("organization");
        String organizationOther = request.getParameter("organizationOther");
        String decision = request.getParameter("decision");

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
        if ((address == null) || address.equals("")
                || (institution == null) || institution.equals("")
                || (userAddress == null) || userAddress.equals("")
                || (userType == null) || userType.equals("")
                || StringUtils.isEmpty(firstName) || StringUtils.isEmpty(lastName)
                || (comments == null) || comments.equals("")
                || (organization == null) || organization.equals("")
                || (organization.equals("Others") && (organizationOther == null || organizationOther.equals("")))
                || (userType.equals("Others") && (userTypeOther == null || userTypeOther.equals("")))) {
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
            map.put("comments",comments);

            return map;
        }

        // All data is there, send the email
        Email email = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "feedback"));
        email.addRecipient(ConfigurationManager
                .getProperty("feedback.recipient"));

        email.addArgument(new Date());    // 0.  Date
        email.addArgument(address);       // 1.  Email
        email.addArgument(eperson);       // 2.  Logged in as
        email.addArgument(page);          // 3.  Referring page
        email.addArgument(agent);         // 4.  User agent
        email.addArgument(session);       // 5.  Session ID
        email.addArgument(comments);      // 6.  The feedback itself
        email.addArgument(lastName);      // 7.  The user's surname
        email.addArgument(firstName);     // 8.  The user's first name
        email.addArgument(institution);   // 9.  Institution
        email.addArgument(userAddress);   // 10. User's address
        if (StringUtils.equals(userType,"Others"))
        {
            email.addArgument(userTypeOther); // 11. User type
        }
        else {
            email.addArgument(userType);      // 11. User type
        }
        if (StringUtils.equals(organization,"Others"))
        {
            email.addArgument(organizationOther); // 12. Organization type
        }
        else {
            email.addArgument(organization);      //12. Organization type
        }

        // Replying to feedback will reply to email on form
        email.setReplyTo(address);

        // May generate MessageExceptions.
        email.send();

        // Finished, allow to pass.
        return null;
    }

}
