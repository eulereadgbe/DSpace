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
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.apache.commons.lang.StringUtils;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

import org.dspace.content.service.ItemService;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;


/**
 * Display to the user a simple form letting the user give feedback.
 * 
 * @author Scott Phillips
 */
public class DocumentDeliveryForm extends AbstractDSpaceTransformer implements CacheableProcessingComponent
{
    private final transient ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    private final transient HandleService handleService = HandleServiceFactory.getInstance().getHandleService();

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
    
    private static final Message T_email =
        message("xmlui.ArtifactBrowser.DocumentDeliveryForm.email");

    private static final Message T_email_help =
        message("xmlui.ArtifactBrowser.DocumentDeliveryForm.email_help");
    
    private static final Message T_requesterEmail_error =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.email.error");

    private static final Message T_requesterName =
            message("xmlui.ArtifactBrowser.ItemRequestForm.requesterName");

    private static final Message T_requesterName_help =
            message("xmlui.ArtifactBrowser.ItemRequestForm.requesterName.help");

    private static final Message T_requesterName_error =
            message("xmlui.ArtifactBrowser.ItemRequestForm.requesterName.error");

    private static final Message T_purpose =
        message("xmlui.ArtifactBrowser.DocumentDeliveryForm.purpose");
    
    private static final Message T_purpose_help =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.purpose.help");

    private static final Message T_purpose_error =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.purpose.error");

    private static final Message T_submit =
        message("xmlui.ArtifactBrowser.DocumentDeliveryForm.submit");
    
    private static final Message T_userType =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.userType");

    private static final Message T_userType_help =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.userType.help");

    private static final Message T_userType_error =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.userType.error");

    private static final Message T_userTypeOther_help =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.userTypeOther.help");

    private static final Message T_userTypeOther_error =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.userTypeOther.error");

    private static final Message T_organizationType =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.organizationType");

    private static final Message T_organizationType_help =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.organizationType.help");

    private static final Message T_organizationType_error =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.organizationType.error");

    private static final Message T_organizationTypeOther_help =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.organizationTypeOther.help");

    private static final Message T_organizationTypeOther_error =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.organizationTypeOther.error");

    private static final Message T_institution =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.institution");

    private static final Message T_institution_help =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.institution.help");

    private static final Message T_institution_error =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.institution.error");

    private static final Message T_userAddress =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.userAddress");

    private static final Message T_userAddress_help =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.userAddress.help");

    private static final Message T_userAddress_error =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.userAddress.error");

    private static final Message T_agreement_help =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.agreement.help");

    private static final Message T_agreement_error =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.agreement.error");

    /**
     * Generate the unique caching key.
     * This key must be unique inside the space of this component.
     */
    public Serializable getKey() {
        
        String email = parameters.getParameter("email","");
        String requesterName = parameters.getParameter("requesterName","");
        String purpose = parameters.getParameter("purpose","");
        String page = parameters.getParameter("page","unknown");
        String handle = parameters.getParameter("handle","unknown");
        String userType = parameters.getParameter("userType","");
        String userTypeOther = parameters.getParameter("userTypeOther","");
        String organizationType = parameters.getParameter("organizationType","");
        String organizationTypeOther = parameters.getParameter("organizationTypeOther","");
        String institution = parameters.getParameter("institution","");
        String userAddress = parameters.getParameter("userAddress","");
        String agreement = parameters.getParameter("agreement","");

        return HashUtil.hash(email + "-" + requesterName + "-" + purpose + "-" + page + "-" + handle + "-" + userType
        + "-" + userTypeOther+ "-" + organizationType + "-" + organizationTypeOther + "-" + institution + "-" + userAddress
        + "-" + agreement);
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
                contextPath+"/documentdelivery/"+parameters.getParameter("handle", "unknown"),Division.METHOD_POST,"primary");

        documentdelivery.setHead(T_head);

        String handle = parameters.getParameter("handle","unknown");
        DSpaceObject dso = handleService.resolveToObject(context, handle);
        Item item = (Item) dso;

        String citationDC = itemService.getMetadataFirstValue(item, "dc", "identifier", "citation", org.dspace.content.Item.ANY);
        String titleDC = item.getName();
        String title = "";
        if (citationDC != null && citationDC.length() > 0) {
            title = citationDC;
        } else {
            if (titleDC != null && titleDC.length() > 0)
                title = titleDC;
        }
        documentdelivery.addPara(title);

        List form = documentdelivery.addList("form",List.TYPE_FORM);
        
        Text email = form.addItem().addText("email");
        email.setAutofocus("autofocus");
        email.setLabel(T_email);
        email.setHelp(T_email_help);
        email.setValue(parameters.getParameter("email",""));

        Text requesterName = form.addItem().addText("requesterName");
        requesterName.setLabel(T_requesterName);
        requesterName.setHelp(T_requesterName_help);
        requesterName.setValue(parameters.getParameter("requesterName", ""));

        Text institution = form.addItem().addText("institution");
        institution.setLabel(T_institution);
        institution.setHelp(T_institution_help);
        institution.setValue(parameters.getParameter("institution",""));

        Text userAddress = form.addItem().addText("userAddress");
        userAddress.setLabel(T_userAddress);
        userAddress.setHelp(T_userAddress_help);
        userAddress.setValue(parameters.getParameter("userAddress",""));

        Select userType = form.addItem().addSelect("userType");
        userType.setLabel(T_userType);
        userType.setHelp(T_userType_help);
        userType.addOption("", "Click here to select your user type");
        userType.addOption("Businessman/Private","Businessman/Private");
        userType.addOption("Faculty/Teacher","Faculty/Teacher");
        userType.addOption("Fish farmer","Fish farmer");
        userType.addOption("Government Employee/Officer","Government Employee/Officer");
        userType.addOption("High School Student","High School Student");
        userType.addOption("Librarian/Library Staff","Librarian/Library Staff");
        userType.addOption("MS Student","MS Student");
        userType.addOption("PhD Student","PhD Student");
        userType.addOption("Researcher","Researcher");
        userType.addOption("Undergraduate/College","Undergraduate/College");
        userType.addOption("University/School Staff","University/School Staff");
        userType.addOption("Others", "Others");
        userType.setOptionSelected(parameters.getParameter("userType",""));

        Text userTypeOther = form.addItem().addText("userTypeOther", "hidden");
        userTypeOther.setHelp(T_userTypeOther_help);
        userTypeOther.setValue(parameters.getParameter("userTypeOther",""));

        Select organizationType = form.addItem().addSelect("organizationType");
        organizationType.setLabel(T_organizationType);
        organizationType.setHelp(T_organizationType_help);
        organizationType.addOption("","Click here to select your organization type");
        organizationType.addOption("Academic/Educational Institution","Academic/Educational Institution");
        organizationType.addOption("Governmental Organization","Governmental Organization");
        organizationType.addOption("NGO","NGO");
        organizationType.addOption("Private Sector","Private Sector");
        organizationType.addOption("Regional/International Organization","Regional/International Organization");
        organizationType.addOption("Others","Others");
        organizationType.setOptionSelected(parameters.getParameter("organizationType",""));

        Text organizationTypeOther = form.addItem().addText("organizationTypeOther", "hidden");
        organizationTypeOther.setHelp(T_organizationTypeOther_help);
        organizationTypeOther.setValue(parameters.getParameter("organizationTypeOther",""));

        TextArea purpose = form.addItem().addTextArea("purpose");
        purpose.setLabel(T_purpose);
        purpose.setHelp(T_purpose_help);
        purpose.setValue(parameters.getParameter("purpose",""));
        
        form.addItem().addButton("submit").setValue(T_submit);

        CheckBox agreement = form.addItem().addCheckBox("agreement");
        agreement.setRequired(true);
        agreement.addOption("accept",T_agreement_help);
        agreement.setOptionSelected(parameters.getParameter("agreement",""));

        // if button is pressed and form is re-loaded it means some parameter is missing
        if(request.getParameter("submit")!=null){
            if(StringUtils.isEmpty(parameters.getParameter("requesterName", ""))){
                requesterName.addError(T_requesterName_error);
            }
            if(StringUtils.isEmpty(parameters.getParameter("email", ""))){
                email.addError(T_requesterEmail_error);
            }
            if(StringUtils.isEmpty(parameters.getParameter("institution", ""))){
                institution.addError(T_institution_error);
            }
            if(StringUtils.isEmpty(parameters.getParameter("userType", ""))){
                userType.addError(T_userType_error);
            }
            if(request.getParameter("userType").equals("Others") &&
                    StringUtils.isEmpty(parameters.getParameter("userTypeOther",""))) {
                userTypeOther.addError(T_userTypeOther_error);
            }
            if(StringUtils.isEmpty(parameters.getParameter("organizationType",""))){
                organizationType.addError(T_organizationType_error);
            }
            if (request.getParameter("organizationType").equals("Others") &&
                    StringUtils.isEmpty(parameters.getParameter("organizationOther", ""))) {
                organizationTypeOther.addError(T_organizationTypeOther_error);
            }
            if(StringUtils.isEmpty(parameters.getParameter("purpose", ""))){
                purpose.addError(T_purpose_error);
            }
            if(StringUtils.isEmpty(parameters.getParameter("userAddress",""))){
                userAddress.addError(T_userAddress_error);
            }
            if(StringUtils.isEmpty(parameters.getParameter("agreement",""))){
                agreement.addError(T_agreement_error);
            }
        }
        documentdelivery.addHidden("page").setValue(parameters.getParameter("page","unknown"));
        documentdelivery.addHidden("handle").setValue(parameters.getParameter("handle","unknown"));
    }
}
