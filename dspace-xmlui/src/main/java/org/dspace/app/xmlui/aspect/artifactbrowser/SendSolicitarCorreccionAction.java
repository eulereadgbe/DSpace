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
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.EPerson;
import org.dspace.handle.HandleManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Scott Phillips
 * @author Adán Román Ruiz at arvo.es
 */

public class SendSolicitarCorreccionAction extends AbstractAction
{

    /**
     *
     */
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel,
            String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        String page = request.getParameter("page");
       // String address = request.getParameter("email");
        String agent = request.getHeader("User-Agent");
        String session = request.getSession().getId();
        String comments = request.getParameter("comments");
        String handle = parameters.getParameter("handle");
        
        // Obtain information from request
        // The page where the user came from
        String fromPage = request.getHeader("Referer");

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
        if ( (comments == null) || comments.equals("") 
                || (handle == null) || handle.equals(""))
        {
            // Either the user did not fill out the form or this is the
            // first time they are visiting the page.
            Map<String,String> map = new HashMap<String,String>();
            map.put("page",page);
            map.put("comments",comments);
            map.put("handle",handle);
            
            return map;
        }
        DSpaceObject dso = HandleManager.resolveToObject(context, handle);
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

        // All data is there, send the email
        Email email = ConfigurationManager.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "send-correction"));
        email.addRecipient(ConfigurationManager.getProperty("feedback.recipient"));

        email.addArgument(new Date()); // 0 Date
        email.addArgument(eperson);    // 1 Logged in as
        email.addArgument(page.replace("/solicitarCorreccion/", "/handle/"));       // 2 Referring page
        email.addArgument(agent);      // 3 User agent
        email.addArgument(session);    // 4 Session ID
        email.addArgument(comments);   // 5 The feedback itself
        email.addArgument(title);     //6 title
        // Replying to feedback will reply to email on form
        email.setReplyTo(eperson);

        // May generate MessageExceptions.
        email.send();

        // Finished, allow to pass.
        return null;
    }

}
