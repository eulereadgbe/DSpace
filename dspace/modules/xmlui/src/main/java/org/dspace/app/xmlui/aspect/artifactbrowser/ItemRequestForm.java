/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.util.HashUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.AuthenticationUtil;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Bitstream;
import org.dspace.content.Metadatum;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletResponse;

/**
 * Display to the user a simple form letting the user to request a protected item.
 * 
 * Original Concept, JSPUI version:    Universidade do Minho   at www.uminho.pt
 * Sponsorship of XMLUI version:    Instituto Oceanográfico de España at www.ieo.es
 * 
 * @author Adán Román Ruiz at arvo.es (added request item support)
 */
public class ItemRequestForm extends AbstractDSpaceTransformer implements CacheableProcessingComponent
{
    private static Logger log = Logger.getLogger(ItemRequestForm.class);

    /** Language Strings */
    private static final Message T_title =
        message("xmlui.ArtifactBrowser.ItemRequestForm.title");
    
    private static final Message T_dspace_home =
        message("xmlui.general.dspace_home");
    
    private static final Message T_trail =
        message("xmlui.ArtifactBrowser.ItemRequestForm.trail");
    
    private static final Message T_head = 
        message("xmlui.ArtifactBrowser.ItemRequestForm.head");
    
    private static final Message T_para1 =
        message("xmlui.ArtifactBrowser.ItemRequestForm.para1");

    private static final Message T_login_para =
            message("xmlui.ArtifactBrowser.ItemRequestForm.login_para");

    private static final Message T_login =
            message("xmlui.ArtifactBrowser.ItemRequestForm.login");
    
    private static final Message T_requesterEmail =
        message("xmlui.ArtifactBrowser.ItemRequestForm.requesterEmail");

    private static final Message T_requesterEmail_help =
        message("xmlui.ArtifactBrowser.ItemRequestForm.requesterEmail_help");
    
    private static final Message T_requesterEmail_error =
        message("xmlui.ArtifactBrowser.ItemRequestForm.requesterEmail.error");
    
    private static final Message T_message = 
        message("xmlui.ArtifactBrowser.DocumentDeliveryForm.purpose");
    
    private static final Message T_message_error = 
        message("xmlui.ArtifactBrowser.ItemRequestForm.message.error");

    private static final Message T_purpose_help =
            message("xmlui.ArtifactBrowser.DocumentDeliveryForm.purpose.help");

    private static final Message T_requesterName =
        message("xmlui.ArtifactBrowser.ItemRequestForm.requesterName");
    
    private static final Message T_requesterName_error = 
        message("xmlui.ArtifactBrowser.ItemRequestForm.requesterName.error");
    
    private static final Message T_allFiles = 
        message("xmlui.ArtifactBrowser.ItemRequestForm.allFiles");
    
    private static final Message T_files = 
        message("xmlui.ArtifactBrowser.ItemRequestForm.files");
    
    private static final Message T_notAllFiles = 
        message("xmlui.ArtifactBrowser.ItemRequestForm.notAllFiles");
    
    private static final Message T_submit =
        message("xmlui.ArtifactBrowser.ItemRequestForm.submit");

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
    	
        String requesterName = parameters.getParameter("requesterName","");
        String requesterEmail = parameters.getParameter("requesterEmail","");
        String allFiles = parameters.getParameter("allFiles","");
        String message = parameters.getParameter("message","");
        String bitstreamId = parameters.getParameter("bitstreamId","");
        String title = parameters.getParameter("title","");
        String userType = parameters.getParameter("userType","");
        String userTypeOther = parameters.getParameter("userTypeOther","");
        String organizationType = parameters.getParameter("organizationType","");
        String organizationTypeOther = parameters.getParameter("organizationTypeOther","");
        String institution = parameters.getParameter("institution","");
        String userAddress = parameters.getParameter("userAddress","");
        String agreement = parameters.getParameter("agreement","");

       return HashUtil.hash(requesterName + "-" + requesterEmail + "-" + allFiles +"-"+message+"-"+bitstreamId
               + "-" + title + "-" + userType + "-" + userTypeOther + "-" + organizationType + "-" +  "-"
               + organizationTypeOther + "-" + institution + "-" + userAddress + "-"+ agreement);
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
			UIException, SQLException, IOException, AuthorizeException {
		
		DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
		if (!(dso instanceof Item)) {
			return;
		}
		Request request = ObjectModelHelper.getRequest(objectModel);
		boolean firstVisit=Boolean.valueOf(request.getParameter("firstVisit"));
		
		Item item = (Item) dso;
		// Build the item viewer division.
		Division itemRequest = body.addInteractiveDivision("itemRequest-form",
				request.getRequestURI(), Division.METHOD_POST, "primary");
		itemRequest.setHead(T_head);

        Metadatum[] titleDC = item.getMetadata("dc", "title", null, Item.ANY);
        Metadatum[] citationDC = item.getMetadata("dc", "identifier", "citation", Item.ANY);
        String document = "";
        if (citationDC != null && citationDC.length > 0) {
            document = citationDC[0].value;
        } else {
            if (titleDC != null && titleDC.length > 0)
                document = titleDC[0].value;
        }
        itemRequest.addPara(document);
        //if (titleDC != null && titleDC.length > 0)
        //itemRequest.addPara(titleDC[0].value);

        // add a login link if user !loggedIn
        if (context.getCurrentUser() == null)
        {
            Para loginPara = itemRequest.addPara();
            loginPara.addContent(T_login_para);
            itemRequest.addPara().addXref(contextPath + "/login", T_login);

            // Interrupt request if the user is not authenticated, so they may come back to
            // the restricted resource afterwards.
            String header = parameters.getParameter("header", null);
            String message = parameters.getParameter("message", null);
            String characters = parameters.getParameter("characters", null);

            // Interrupt this request
            AuthenticationUtil.interruptRequest(objectModel, header, message, characters);
        } else {
            //If user has read permissions to bitstream, redirect them to bitstream, instead of restrict page
            try {
                int bitstreamID = parameters.getParameterAsInteger("bitstreamId");
                Bitstream bitstream = Bitstream.find(context, bitstreamID);

                if(AuthorizeManager.authorizeActionBoolean(context, bitstream, Constants.READ)) {
                    String redirectURL = request.getContextPath() + "/bitstream/handle/" + item.getHandle() + "/"
                            + bitstream.getName() + "?sequence=" + bitstream.getSequenceID();

                    HttpServletResponse httpResponse = (HttpServletResponse)
                            objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
                    httpResponse.sendRedirect(redirectURL);
                }
            } catch (ParameterException e) {
                log.error(e.getMessage());
            }
        }


        itemRequest.addPara(T_para1);

		List form = itemRequest.addList("form", List.TYPE_FORM);

        Text requesterEmail = form.addItem().addText("requesterEmail");
        requesterEmail.setAutofocus("autofocus");
        requesterEmail.setLabel(T_requesterEmail);
        requesterEmail.setHelp(T_requesterEmail_help);
        requesterEmail.setValue(parameters.getParameter("requesterEmail", ""));

        Text requesterName = form.addItem().addText("requesterName");
		requesterName.setLabel(T_requesterName);
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

		Radio radio = form.addItem().addRadio("allFiles","hidden");
		String selected=!parameters.getParameter("allFiles","true").equalsIgnoreCase("false")?"true":"false";
		radio.setOptionSelected(selected);
		//radio.setLabel(T_files);
		radio.addOption("true", T_allFiles);
		radio.addOption("false", T_notAllFiles);
		
        TextArea message = form.addItem().addTextArea("message");
		message.setLabel(T_message);
		message.setHelp(T_purpose_help);
		message.setValue(parameters.getParameter("message", ""));
		form.addItem().addHidden("bitstreamId").setValue(parameters.getParameter("bitstreamId", ""));
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
            if(StringUtils.isEmpty(parameters.getParameter("requesterEmail", ""))){
                requesterEmail.addError(T_requesterEmail_error);
            }
            if(StringUtils.isEmpty(parameters.getParameter("message", ""))){
                message.addError(T_message_error);
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
            if(StringUtils.isEmpty(parameters.getParameter("userAddress",""))){
                userAddress.addError(T_userAddress_error);
            }
            if(StringUtils.isEmpty(parameters.getParameter("agreement",""))){
                agreement.addError(T_agreement_error);
            }
        }
		itemRequest.addHidden("page").setValue(parameters.getParameter("page", "unknown"));
	}
}
