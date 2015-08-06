/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.artifactbrowser;

import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.util.HashUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Metadatum;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.handle.HandleManager;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

/**
 * Display to the user a simple form letting the user give feedback.
 * 
 * @author Scott Phillips
 */
public class DocumentDeliveryForm extends AbstractDSpaceTransformer implements CacheableProcessingComponent
{
    /** Language Strings */
    private static final Message T_title =
        message("xmlui.ArtifactBrowser.DocumentDeliveryForm.title");
    
    private static final Message T_dspace_home =
        message("xmlui.general.dspace_home");
    
    private static final Message T_trail =
        message("xmlui.ArtifactBrowser.DocumentDeliveryForm.trail");
    
    private static final Message T_head = 
        message("xmlui.ArtifactBrowser.DocumentDeliveryForm.head");
    
    private static final Message T_para1 =
        message("xmlui.ArtifactBrowser.DocumentDeliveryForm.para1");
    
    private static final Message T_requesterName =
            message("xmlui.ArtifactBrowser.ItemRequestForm.requesterName");

    private static final Message T_requesterName_error =
            message("xmlui.ArtifactBrowser.ItemRequestForm.requesterName.error");

    private static final Message T_email =
        message("xmlui.ArtifactBrowser.DocumentDeliveryForm.email");

    private static final Message T_email_help =
        message("xmlui.ArtifactBrowser.DocumentDeliveryForm.email_help");

    private static final Message T_message =
        message("xmlui.ArtifactBrowser.DocumentDeliveryForm.message");

    private static final Message T_agreement =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.agreement");

    private static final Message T_submit =
        message("xmlui.ArtifactBrowser.DocumentDeliveryForm.submit");
    
    /**
     * Generate the unique caching key.
     * This key must be unique inside the space of this component.
     */
    public Serializable getKey() {
        
        String email = parameters.getParameter("email","");
        String lastName = parameters.getParameter("lastName","");
        String firstName = parameters.getParameter("firstName","");
        String institution = parameters.getParameter("institution","");
        String userAddress = parameters.getParameter("userAddress","");
        String userType = parameters.getParameter("userType","");
        String userTypeOther = parameters.getParameter("userTypeOther","");
        String organization = parameters.getParameter("organization","");
        String organizationOther = parameters.getParameter("organizationOther","");
        String reason = parameters.getParameter("reason","");
        String message = parameters.getParameter("message","");
        String decision = parameters.getParameter("decision","");
        String page = parameters.getParameter("page","unknown");
        String title = parameters.getParameter("title","");

        return HashUtil.hash(lastName + "-" + firstName + "-" + email + "-" + page + "-" + institution + "-"
                + userAddress + "-" + userType + "-" + userTypeOther + "-" + organization + "-" + organizationOther + "-" + reason + "-"
                + message + "-" + decision + "-" + title);
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
        pageMeta.addMetadata("javascript", "static", null, true).addContent("static/js/request-forms.js");
    }

    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        // Build the item viewer division.
        Division documentdelivery = body.addInteractiveDivision("documentdelivery-form",
                contextPath+"/documentdelivery/"+parameters.getParameter("handle","unknown"),Division.METHOD_POST,"primary");
        
        documentdelivery.setHead(T_head);
        
        String handle=parameters.getParameter("handle","unknown");
        DSpaceObject dso = HandleManager.resolveToObject(context, handle);

        if (dso instanceof Item) {
            Item item = ((Item) dso);
            Metadatum[] citationDC = item.getMetadata("dc", "identifier", "citation", Item.ANY);
            Metadatum[] isPartOfDC = item.getMetadata("dc", "relation", "ispartof", Item.ANY);
            Metadatum[] titleDC = item.getMetadata("dc", "title", null, Item.ANY);
            String document = "";
            if (citationDC != null && citationDC.length > 0) {
                document = citationDC[0].value;
            } else {
                if (isPartOfDC != null && isPartOfDC.length > 0) {
                    document = titleDC[0].value + " " + isPartOfDC[0].value;
                } else {
                    document = titleDC[0].value;
                }
            }
            documentdelivery.addPara(document); // check for nulls or multiple values;
            documentdelivery.addHidden("title").setValue(document);
        }

        List form = documentdelivery.addList("form",List.TYPE_FORM);
        
        Text email = form.addItem().addText("email");
        email.setAutofocus("autofocus");
        email.setSize(50);
        email.setLabel(T_email);
        email.setHelp(T_email_help);
        email.setValue(parameters.getParameter("email",""));
        
        // Composite
        Composite requesterName = form.addItem().addComposite("requesterName");
        requesterName.setLabel("Name");
        Text text = requesterName.addText("lastName");
        text.setLabel("Last name");
        text.setValue(parameters.getParameter("lastName",""));

        text = requesterName.addText("firstName");
        text.setLabel("First name");
        text.setValue(parameters.getParameter("firstName", ""));

        Select userType = form.addItem().addSelect("userType");
        userType.setLabel("User type");
        userType.addOption("","Please select your category");
        userType.addOption("Undergraduate/BS","Undergraduate/BS");
        userType.addOption("MS Student","MS Student");
        userType.addOption("PhD Student","PhD Student");
        userType.addOption("Faculty","Faculty");
        userType.addOption("Researcher","Researcher");
        userType.addOption("Businessman/Private","Businessman/Private");
        userType.addOption("Fish farmer","Fish farmer");
        userType.addOption("Others", "Others");
        userType.setOptionSelected(parameters.getParameter("userType",""));
        TextArea userTypeOther = form.addItem().addTextArea("userTypeOther");
        userTypeOther.setValue(parameters.getParameter("userTypeOther", ""));

        Select organization = form.addItem().addSelect("organization");
        organization.setLabel("Organization type");
        organization.addOption("","Please select your organization type");
        organization.addOption("Governmental Organization","Governmental Organization");
        organization.addOption("Academic Institution","Academic Institution");
        organization.addOption("Regional/International Organization","Regional/International Organization");
        organization.addOption("Private Sector","Private Sector");
        organization.addOption("NGO","NGO");
        organization.addOption("Others","Others");
        organization.setOptionSelected(parameters.getParameter("organization",""));
        TextArea organizationOther = form.addItem().addTextArea("organizationOther");
        organizationOther.setValue(parameters.getParameter("organizationOther", ""));

        Text institution = form.addItem().addText("institution");
        institution.setLabel("Institution Name");
        institution.setValue(parameters.getParameter("institution",""));
        institution.setSize(50);

        Text userAddress = form.addItem().addText("userAddress");
        userAddress.setLabel("Address");
        userAddress.setValue(parameters.getParameter("userAddress",""));
        userAddress.setSize(50);

        TextArea reason = form.addItem().addTextArea("reason");
        reason.setLabel("Reason");
        reason.setHelp("Please provide a rationale for requesting this document. This will be included in the email" +
                " to the responsible person for authorisation.");
        reason.setValue(parameters.getParameter("reason",""));

        TextArea message = form.addItem().addTextArea("message");
        message.setLabel(T_message);
        message.setValue(parameters.getParameter("message",""));

        // Checkbox field
        CheckBox decision = form.addItem().addCheckBox("decision");
        decision.setRequired(true);
        decision.addOption("agree", T_agreement);
        decision.setOptionSelected(parameters.getParameter("decision",""));

        form.addItem().addButton("submit").setValue(T_submit);
        
        // if button is pressed and form is re-loaded it means some parameter is missing
        if(request.getParameter("submit")!=null){
            if(StringUtils.isEmpty(parameters.getParameter("email", ""))){
                email.addError("email is required");
            }
            if(StringUtils.isEmpty(parameters.getParameter("lastName", "")) ||
                    StringUtils.isEmpty(parameters.getParameter("firstName", "")))
            {
                requesterName.addError("Complete name is required");
            }
            if(StringUtils.isEmpty(parameters.getParameter("institution", ""))){
                institution.addError("Institution Name is required");
            }
            if(StringUtils.isEmpty(parameters.getParameter("userType", "")))
            {
                userType.addError("Please select your user type.");
            }
            if(StringUtils.isEmpty(parameters.getParameter("userAddress", "")))
            {
                userAddress.addError("Address is required.");
            }
            if (request.getParameter("userType").equals("Others") &&
                    StringUtils.isEmpty(parameters.getParameter("userTypeOther",""))) {
                userTypeOther.addError("Please write your user type.");
            }
            if(StringUtils.isEmpty(parameters.getParameter("organization", "")))
            {
                organization.addError("Please select your organization type.");
            }
            if (request.getParameter("organization").equals("Others") &&
                    StringUtils.isEmpty(parameters.getParameter("organizationOther", ""))) {
                organizationOther.addError("Please write your organization type.");
            }
            if(StringUtils.isEmpty(parameters.getParameter("reason", ""))){
                reason.addError("This field is required");
            }
            if(StringUtils.isEmpty(parameters.getParameter("message", ""))){
                message.addError("This field is required");
            }
            if(StringUtils.isEmpty(parameters.getParameter("decision", ""))){
                decision.addError("Click \"I agree\" to complete your request.");
            }
        }
        documentdelivery.addHidden("page").setValue(parameters.getParameter("page","unknown"));
    }
}
