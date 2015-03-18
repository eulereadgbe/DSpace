/*
 * CoverPage.java
 *
 * Version: 1.1.2
 *
 * Date: 2011-11-22
 *
 * Copyright (c) 2010-2011, Keiji Suzuki, All rights reserved.
 *
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package jp.zuki_ebetsu.dspace.util;

import com.itextpdf.text.*;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.collection.PdfCollection;
import com.itextpdf.text.pdf.PdfPageLabels;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.text.BaseColor;
import org.apache.log4j.Logger;
import org.dspace.content.*;
import org.dspace.content.Collection;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.handle.HandleManager;

import java.awt.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.lang.String;
import java.net.*;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Cover Page
 * 
 * @author Keiji Suzuki
 * @version 1.1
 */
public class CoverPage {

    private static Logger log = Logger.getLogger(CoverPage.class);

    /** header text */
    protected static final String header_text = ConfigurationManager.getProperty("coverpage.header");

    /** footer text */
    protected static final String footer_text = ConfigurationManager.getProperty("coverpage.footer");

    /** header logo image path */
    protected static final String logo_path   = ConfigurationManager.getProperty("coverpage.logo");

    /** footer logo image path */
    protected static final String logo2_path  = ConfigurationManager.getProperty("coverpage.logo2");

    protected static final String images_path = ConfigurationManager.getProperty("dspace.dir") + "/webapps/xmlui/static/";

    /** footer twitter logo image path */
    protected static final String twitter_path  = ConfigurationManager.getProperty("coverpage.twitter_logo");

    /** footer facebook logo image path */
    protected static final String facebook_path  = ConfigurationManager.getProperty("coverpage.facebook_logo");

    /** footer googleplus logo image path */
    protected static final String googleplus_path  = ConfigurationManager.getProperty("coverpage.googleplus_logo");

    /** footer instagram logo image path */
    protected static final String instagram_path  = ConfigurationManager.getProperty("coverpage.instagram_logo");

    protected static final String linkedin_path  = ConfigurationManager.getProperty("coverpage.linkedin_logo");

    protected static final String mendeley_path  = ConfigurationManager.getProperty("coverpage.mendeley_logo");

    protected static final String researchgate_path  = ConfigurationManager.getProperty("coverpage.researchgate_logo");

    protected static final String citeulike_path  = ConfigurationManager.getProperty("coverpage.citeulike_logo");

    /** Whether does security parameters of original pass to the new file? */
    private static final boolean security = ConfigurationManager.getBooleanProperty("coverpage.copysecurity", true);
    
    /** New ownew password assigning to the new file */
    private static final String ownerpass = ConfigurationManager.getProperty("coverpage.ownerpass");

    /** Whether write pdf as PDF/A-1B */
    private static final boolean pdfa = ConfigurationManager.getBooleanProperty("coverpage.pdfa", false);

    /** Whether write pdf as PDF/A-1B */
    private static final String PROFILE = ConfigurationManager.getProperty("coverpage.icc.file");

    /** BaseFont family: sams-serif (gochic)*/
    private static final BaseFont FONT_SANS = setBaseFont("coverpage.font.sans", pdfa);

    /** BaseFont family: serif (mincho)*/
    private static final BaseFont FONT_SERIF = setBaseFont("coverpage.font.serif", pdfa);

    /** Header Font */
    protected static final Font FONT_HEADER = setFont(FONT_SANS, Font.FontFamily.HELVETICA, 23, Font.BOLD);

    /** Footer Font Serif*/
    protected static final Font FONT_FOOTER_SERIF = setFont(FONT_SERIF, Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);

    /** Footer Font Sans*/
    protected static final Font FONT_FOOTER_SANS = setFont(FONT_SANS, Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    /** Footer Font */
    protected static final Font FONT_FOOTER_URL = setFont(FONT_SERIF, Font.FontFamily.TIMES_ROMAN, 9, Font.BOLD);

    /** Cell Tag Font */
    private static final Font FONT_TAG    = setFont(FONT_SANS, Font.FontFamily.HELVETICA, 11, Font.BOLD);

    /** Cell Value Font */
    private static final Font FONT_VALUE  = setFont(FONT_SERIF, Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);

    /** Date Font */
    private static final Font FONT_DATE   = setFont(FONT_SANS, Font.FontFamily.HELVETICA, 8, Font.NORMAL);

    /** Message Font */
    private static final Font FONT_MESS    = setFont(FONT_SERIF, Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);

    /** Overlay Font */
    private static final Font FONT_OVERLAY    = setFont(FONT_SANS, Font.FontFamily.HELVETICA, 7, Font.NORMAL);

    /** URL Font */
    private static final Font FONT_URL    = setFont(FONT_SERIF, Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);

    /** URL Font for Overlay*/
    private static final Font FONT_URL_OVERLAY    = setFont(FONT_SANS, Font.FontFamily.HELVETICA, 7, Font.BOLD);

    /** Communities not to set cover page */
    private static final Community[] OMIT_COMMS = setOmitCommunities();

    /** Collections not to set cover page */
    private static final Collection[] OMIT_COLLS = setOmitCollections();

    /** Display fields */
    private static String[] fields;

    /** context */
    private Context context;

    /** item this bitstream is belonged */
    private Item item;
    
    /** bitstream to be added cover page  */
    private Bitstream bitstream;

    private Font facebook;

    private Font twitter;

    private Font googleplus;

    private Font instagram;

    private Font handle_url;

    static
    {
        // Set display metadata fields
        ArrayList<String> clist = new ArrayList<String>();
        for (int i = 1; ConfigurationManager.getProperty("coverpage.field." + i) != null; i++)
        {
            clist.add(ConfigurationManager.getProperty("coverpage.field." + i));
        }
        if (clist.size() > 0)
        {
            fields = (String[]) clist.toArray(new String[clist.size()]);
        }
        else
        {
            fields = new String[]{ "Title:dc.title", 
                                   "Author(s):dc.contributor.author",
                                   "Citation:dc.identifier.citation",
                                   "Issue Date:dc.date.issued",
                                   "Doc URL:dc.identifier.uri",
                                   "Right:dc.rights.note" };
        }
    }


    public CoverPage(Context context, Item item, Bitstream bitstream)
    {
        this.context   = context;
        this.item      = item;
        this.bitstream = bitstream;
    }
    
    
    /**
     * Concatenate cover page to the PDF file
     * 
     * @return the new PDF stream, or null if there is not COVER bundle or error occurs
     */
    public ByteArrayOutputStream getConcatenatePDF()
    {
        if (bitstream == null)
            return null;

        if (item == null)
        {
            item = getItem();
            if (item == null)
                return null;
        }

        ByteArrayOutputStream byteout = null;
        //InputStream coverStream = null;

        try
        {
            if (OMIT_COMMS != null)
            {
                Community[] item_comms = item.getCommunities();
                for (Community item_comm : item_comms)
                {
                    for (Community omit_comm : OMIT_COMMS)
                    {
                        if (item_comm.getID() == omit_comm.getID())
                        {
                            return null;
                        }
                    }
                }
            }

            if (OMIT_COLLS != null)
            {
                Collection[] item_colls = item.getCollections();
                for (Collection item_coll : item_colls)
                {
                    for (Collection omit_coll : OMIT_COLLS)
                    {
                        if (item_coll.getID() == omit_coll.getID())
                        {
                            return null;
                        }
                    }
                }
            }

            // Get Cover Page
            InputStream coverStream = getCoverStream();
            if (coverStream == null) 
                return null;

            byteout = new ByteArrayOutputStream();

            InputStream documentStream = bitstream.retrieve();


            PdfReader coverPageReader = new PdfReader(coverStream);
            PdfReader reader = new PdfReader(documentStream);
            PdfStamper stamper = new PdfStamper(reader, byteout);

            PdfImportedPage page = stamper.getImportedPage(coverPageReader, 1);
            stamper.insertPage(1, coverPageReader.getPageSize(1));
            PdfContentByte content = stamper.getOverContent(1);

            int n = reader.getNumberOfPages();
            for (int j = 2; j <= n; j++) {
                Rectangle mediabox = reader.getPageSize(j);
                Rectangle crop = new Rectangle(mediabox);
                // add overlay text
                FONT_URL_OVERLAY.setColor(0, 191, 255);
                // get current user
                String downloader = null;
                if (getEperson() != "[Anonymous]") {
                    downloader = getEperson();
                } else {
                    downloader = "[" + getIp() + "]";
                }
                Phrase downloaded = new Phrase();
                downloaded.add(new Chunk("Downloaded by " + downloader + " from ", FONT_OVERLAY));
                downloaded.add(new Chunk("http://repository.seafdec.org.ph", FONT_URL_OVERLAY));
                downloaded.add(new Chunk(" on ", FONT_OVERLAY));
                downloaded.add(new Chunk(new SimpleDateFormat("MMMM d, yyyy").format(new Date()), FONT_OVERLAY));
                downloaded.add(new Chunk(" at ", FONT_OVERLAY));
                downloaded.add(new Chunk(new SimpleDateFormat("h:mm a z").format(new Date()), FONT_OVERLAY));
                ColumnText.showTextAligned(stamper.getOverContent(j), Element.ALIGN_CENTER, downloaded,
                        crop.getLeft(10), crop.getHeight() / 2 + crop.getBottom(), 90);
            }
            /*for (int i = 0; i < is.length; i++)
            {
                // we create a reader for a certain document
                reader = new PdfReader(is[i], password);
                reader.consolidateNamedDestinations();

                if (i == 0) 

                int n = reader.getNumberOfPages();
                // step 4: we add content
                PdfImportedPage page = stamper.getImportedPage(coverStream, 1);
                stamper.insertPage(1, coverStream.getPageSize(1));
                for (int j = 0; j < n; )
                {
                    ++j;
                    page = writer.getImportedPage(reader, j);
                    if (i == 1) {
                        stamp = writer.createPageStamp(page);
                        Rectangle mediabox = reader.getPageSize(j);
                        Rectangle crop = new Rectangle(mediabox);
                        writer.setCropBoxSize(crop);
                        // add overlay text
                        FONT_URL_OVERLAY.setColor(0, 191, 255);
                        // get current user
                        String downloader = null;
                        if (getEperson() != "[Anonymous]")
                        {
                            downloader = getEperson();
                        } else {
                            downloader = "[" + getIp() + "]";
                        }
                        Phrase downloaded = new Phrase();
                        downloaded.add(new Chunk("Downloaded by " + downloader + " from ", FONT_OVERLAY));
                        downloaded.add(new Chunk("http://repository.seafdec.org.ph", FONT_URL_OVERLAY));
                        downloaded.add(new Chunk(" on ", FONT_OVERLAY));
                        downloaded.add(new Chunk(new SimpleDateFormat("MMMM d, yyyy").format(new Date()), FONT_OVERLAY));
                        downloaded.add(new Chunk(" at ", FONT_OVERLAY));
                        downloaded.add(new Chunk(new SimpleDateFormat("h:mm a z").format(new Date()), FONT_OVERLAY));
                        ColumnText.showTextAligned(stamper.getOverContent(), Element.ALIGN_CENTER, downloaded,
                                crop.getLeft(10), crop.getHeight() / 2 + crop.getBottom(), 90);
                        stamper.alterContents();
                    }
                    writer.addPage(page);
                }
            // step 5: we close the document
            document.close();
            */
            content.addTemplate(page, 0, 0);
            copyLinks(stamper, 1, coverPageReader, 1);
            PdfDictionary root = reader.getCatalog();
            PdfDictionary labels = root.getAsDict(PdfName.PAGELABELS);
            if (labels != null)
            {
                PdfArray newNums = new PdfArray();

                newNums.add(new PdfNumber(0));
                PdfDictionary coverDict = new PdfDictionary();
                coverDict.put(PdfName.P, new PdfString("Cover Page"));
                newNums.add(coverDict);

                PdfArray nums = labels.getAsArray(PdfName.NUMS);
                if (nums != null)
                {
                    for (int i = 0; i < nums.size() - 1; )
                    {
                        int k = nums.getAsNumber(i++).intValue();
                        newNums.add(new PdfNumber(k+1));
                        newNums.add(nums.getPdfObject(i++));
                    }
                }

                labels.put(PdfName.NUMS, newNums);
                stamper.markUsed(labels);
            }
            stamper.close();
        }
        catch (Exception e) 
        {
            log.info(LogManager.getHeader(context, "cover_page: getConcatenatePDF", "bitstream_id="+bitstream.getID()+", error="+e.getMessage()));
            e.printStackTrace();
            return null;
        }
        
        return byteout;
    }

    /**
     * 
     * @return InputStream the resulting output stream
     */
    private InputStream getCoverStream()
    {
        ByteArrayOutputStream byteout = getCover();
        return new ByteArrayInputStream(byteout.toByteArray());
    }

    /**
     * 
     * @return InputStream the resulting output stream
     */
    private byte[] getCoverByte()
    {
        ByteArrayOutputStream byteout = getCover();
        return byteout.toByteArray();
    }

    /**
     * 
     * @return InputStream the resulting output stream
     */
    private ByteArrayOutputStream getCover()


    {
        ByteArrayOutputStream byteout;
        Document doc = null;
        try 
        {
            byteout = new ByteArrayOutputStream();   
            doc = new Document(PageSize.LETTER, 24, 24, 20, 40);
            PdfWriter pdfwriter = PdfWriter.getInstance(doc, byteout);

            pdfwriter.setPageEvent(new HeaderFooter());
            doc.open(); 

            if (logo_path != null && !logo_path.equals("")) 
            {
                Image img = Image.getInstance(images_path+logo_path);
                img.scalePercent(100 * 72 / 300);
                img.setAlignment(Element.ALIGN_CENTER);
                doc.add(img);
                doc.add(new Paragraph(""));
            }

            Chunk title;
            String item_title = item.getName();
            BaseFont bf_header = FONT_HEADER.getCalculatedBaseFont(false);
            float width_header = bf_header.getWidthPointKerned(item_title, 20);
            title = new Chunk(item_title, FONT_HEADER);
            Paragraph header = new Paragraph(title);
            header.setAlignment(Element.ALIGN_JUSTIFIED);
            header.setLeading(26);
            LineSeparator ls = new LineSeparator(2, 100, null, Element.ALIGN_CENTER, -9);
            header.setSpacingAfter(5);
            header.add(ls);
            doc.add(header);

            Metadatum[] authorDC = item.getMetadata("dc", "contributor", "author", Item.ANY);
            if (authorDC.length >= 1) {
                Paragraph item_author = new Paragraph();
                if (authorDC.length >= 1) {
                    Chunk author = new Chunk(authorDC[0].value, FONT_VALUE);
                    item_author.add(author);
                }

                for (int i = 1; i < authorDC.length; i++) {
                    if (i == authorDC.length - 1) {
                        Chunk author = new Chunk(" & " + authorDC[i].value, FONT_VALUE);
                        item_author.add(author);
                    } else {
                        Chunk author = new Chunk("; " + authorDC[i].value, FONT_VALUE);
                        item_author.add(author);
                    }
                }
                item_author.add(new Chunk("\n", FONT_HEADER));
                doc.add(item_author);
            }

            Paragraph date_issued = new Paragraph();
            Metadatum[] issue_date = item.getMetadata("dc", "date", "issued", Item.ANY);
            date_issued.add(new Chunk("Date published: ", FONT_TAG));
            date_issued.add(new Chunk(issue_date[0].value, FONT_VALUE));
            date_issued.add(new Chunk("\n", FONT_HEADER));
            doc.add(date_issued);

            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20f);
            table.setSpacingAfter(24f);

            Paragraph citation_date = new Paragraph();
            Metadatum[] citationDC = item.getMetadata("dc", "identifier", "citation", Item.ANY);
            if (citationDC.length == 1)
            {
                Phrase citation = new Phrase();
                citation.add(new Chunk("To cite this document :  ", FONT_TAG));
                citation.add(new Chunk(citationDC[0].value, FONT_VALUE));
                citation.add(new Chunk("\n\n", FONT_VALUE));
                citation_date.add(citation);
            }

            Metadatum[] ASFAkeywordsDC = item.getMetadata("dc", "subject", "asfa", Item.ANY);
            Metadatum[] keywordsDC = item.getMetadata("dc", "subject", null, Item.ANY);
            if (keywordsDC.length >= 1 || ASFAkeywordsDC.length >= 1) {
                citation_date.add(new Chunk("Keywords : ", FONT_TAG));
                if (ASFAkeywordsDC.length >= 1) {
                    citation_date.add(new Chunk(" " + ASFAkeywordsDC[0].value, FONT_VALUE));
                }

                for (int i = 1; i < ASFAkeywordsDC.length; i++) {
                    citation_date.add(new Chunk(", " + ASFAkeywordsDC[i].value, FONT_VALUE));
                }
                if (keywordsDC.length >= 1 && ASFAkeywordsDC.length != 0) {
                    citation_date.add(new Chunk(", " + keywordsDC[0].value, FONT_VALUE));
                }

                if (keywordsDC.length >= 1 && ASFAkeywordsDC.length == 0) {
                    citation_date.add(new Chunk(" " + keywordsDC[0].value, FONT_VALUE));
                }

                for (int m = 1; m < keywordsDC.length; m++) {
                    citation_date.add(new Chunk(", " + keywordsDC[m].value, FONT_VALUE));
                }
                citation_date.add(new Chunk("\n\n", FONT_VALUE));
            }

            handle_url = new Font(FONT_SERIF, 11);
            handle_url.setColor(0, 191, 255);
            citation_date.add(new Chunk("To link to this document :  ", FONT_TAG));
            citation_date.add(new Chunk(HandleManager.getCanonicalForm(item.getHandle()), handle_url));
            citation_date.add(new Chunk("\n\n", FONT_VALUE));

            Phrase share = new Phrase();
            share.add(new Chunk("Share on :  ", FONT_TAG));
            String handleURI = HandleManager.getCanonicalForm(item.getHandle());

            String fb_url = "http://www.facebook.com/sharer.php?u=http://repository.seafdec.org.ph/handle" + handleURI.substring(21);
            Image fb = Image.getInstance(images_path+facebook_path);
            fb.scalePercent(25);

            String twt_url = "http://twitter.com/share?url=http://repository.seafdec.org.ph/handle" + handleURI.substring(21);
            Image twitter = Image.getInstance(images_path+twitter_path);
            twitter.scalePercent(25);

            String gplus_url = "https://plus.google.com/share?url=http://repository.seafdec.org.ph/handle" + handleURI.substring(21);
            Image googleplus = Image.getInstance(images_path+googleplus_path);
            googleplus.scalePercent(25);

            String linkedin_url = "http://www.linkedin.com/shareArticle?mini=true&url=http://repository.seafdec.org.ph/handle" + handleURI.substring(21);
            Image linkedin = Image.getInstance(images_path+linkedin_path);
            linkedin.scalePercent(25);

            String mendeley_url = "http://www.mendeley.com/import/?url=http://repository.seafdec.org.ph/handle" + handleURI.substring(21);
            Image mendeley = Image.getInstance(images_path+mendeley_path);
            mendeley.scalePercent(25);

            String researchgate_url = "https://www.researchgate.net/go.Share.html?url=http://repository.seafdec.org.ph/handle" + handleURI.substring(21);
            Image researchgate = Image.getInstance(images_path+researchgate_path);
            researchgate.scalePercent(25);

            String citeulike_url = "http://www.citeulike.org/posturl2?url=http://repository.seafdec.org.ph/handle" + handleURI.substring(21);
            Image citeulike = Image.getInstance(images_path+citeulike_path);
            citeulike.scalePercent(25);

            share.add(new Chunk(fb, 0, -2, true).setAnchor(fb_url + "&title=" + item_title));
            share.add(new Chunk(" ", FONT_VALUE));
            share.add(new Chunk(twitter, 0, -2, true).setAnchor(twt_url));
            share.add(new Chunk(" ", FONT_VALUE));
            share.add(new Chunk(googleplus, 0, -2, true).setAnchor(gplus_url));
            share.add(new Chunk(" ", FONT_VALUE));
            share.add(new Chunk(linkedin, 0, -2, true).setAnchor(linkedin_url + "&title=" + item_title));
            share.add(new Chunk(" ", FONT_VALUE));
            share.add(new Chunk(mendeley, 0, -2, true).setAnchor(mendeley_url));
            share.add(new Chunk(" ", FONT_VALUE));
            share.add(new Chunk(researchgate, 0, -2, true).setAnchor(researchgate_url + "&title=" + item_title));
            share.add(new Chunk(" ", FONT_VALUE));
            share.add(new Chunk(citeulike, 0, -2, true).setAnchor(citeulike_url + "&title=" + item_title));
            citation_date.add(share);

            PdfPCell date_citation = new PdfPCell(citation_date);
            date_citation.setPadding(7f);
            date_citation.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
            date_citation.setLeading(15, 0);

            table.addCell(date_citation);
            doc.add(table);

            Paragraph p = new Paragraph();

            BarcodeQRCode qrCode = new BarcodeQRCode(handleURI, 144, 144, null);
            Image img = qrCode.getImage();
            Annotation annotation;
            annotation = new Annotation(0, 0, 0, 0, handleURI);
            img.setAnnotation(annotation);

            if (logo2_path != null && !logo2_path.equals("")) 
            {
                p = new Paragraph("");
                p.setSpacingBefore(60f);
                doc.add(p);

                Image img2 = Image.getInstance(logo2_path);
                img2.setAlignment(Element.ALIGN_CENTER);
                doc.add(img2);
                doc.add(new Paragraph(""));
            }

            doc.add(new Paragraph(new Phrase("PLEASE SCROLL DOWN TO SEE THE FULL TEXT", FONT_FOOTER_SANS)));

            FONT_URL.setColor(0, 191, 255);

            Anchor sair = new Anchor("SEAFDEC/AQD Institutional Repository (SAIR)", FONT_URL);
            sair.setReference("http://repository.seafdec.org.ph");

            Phrase downloadDate = new Phrase();
            downloadDate.add(new Chunk("On: " + new SimpleDateFormat("MMMM d, yyyy").format(new Date()), FONT_FOOTER_SANS));
            downloadDate.add(new Chunk(" at " + new SimpleDateFormat("h:mm a z").format(new Date()), FONT_FOOTER_SANS));
            String ip = getIp();

            Phrase userIP = new Phrase("IP Address: " + ip, FONT_FOOTER_SANS);
            Paragraph downloadDetails = new Paragraph();
            downloadDetails.add(new Phrase("This content was downloaded from ", FONT_FOOTER_SANS));
            downloadDetails.add(sair);
            downloadDetails.add(new Phrase(" - the official digital repository of scholarly and research information of the department", FONT_FOOTER_SANS));
            downloadDetails.add(new Chunk("\n", FONT_FOOTER_SANS));
            downloadDetails.add(new Phrase("Downloaded by: " + getEperson(), FONT_FOOTER_SANS));
            downloadDetails.add(new Chunk("\n", FONT_FOOTER_SANS));
            downloadDetails.add(downloadDate);
            downloadDetails.add(new Chunk("\n", FONT_FOOTER_SANS));
            downloadDetails.add(userIP);

            Paragraph qr = new Paragraph();
            qr.add(new Chunk(img, 0, 0, true));

            PdfPTable table2 = new PdfPTable(2);
            table2.setSpacingBefore(9f);
            table2.setWidthPercentage(100);
            int table_width2[] = {80,20};
            table2.setWidths(table_width2);
            PdfPCell cell = new PdfPCell(downloadDetails);
            cell.setPadding(7f);
            cell.setLeading(11, 0);
            cell.setBorder(Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM);
            table2.addCell(cell);
            PdfPCell cell2 = new PdfPCell(qr);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setBorder(Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM);
            table2.addCell(cell2);
            table2.completeRow();
            doc.add(table2);

            doc.close();
            return byteout; 
        } 
        catch (Exception e)
        {
            log.info(LogManager.getHeader(context, "cover_page", "bitstream_id="+bitstream.getID()+", error="+e.getMessage()));
            return null;
        }
    }

    private Item getItem()
    {
        try 
        {
            Bundle[] bundles = bitstream.getBundles();
            Item item = null;
            for (int i = 0; i < bundles.length; i++) 
            {
                if (bundles[i].getName().equals("ORIGINAL")) 
                {
                    Item[] items = bundles[i].getItems();
                    for (int j = 0; j < items.length; j++) 
                    {
                        if (!items[j].isWithdrawn()) 
                        {
                            item = items[j];
                            break;
                        }
                    }
                }
            }
            
            return item;
        }
        catch (SQLException sqle)
        {
            log.info(LogManager.getHeader(context, "cover_page: getItem", "bitstream_id="+bitstream.getID()+", error="+sqle.getMessage())); 
            return null;
        }
    }

    private String getFieldValue(String field)
    {
        Metadatum[] dcvalues = item.getMetadataByMetadataString(field);
        if (dcvalues.length == 0)
        {
            return "";
        }
        else if (dcvalues.length == 1)
        {
            return dcvalues[0].value;
        }
        else
        {
            StringBuffer sb = new StringBuffer();
            for (int i=0, len=dcvalues.length; i<len; i++)
                sb.append("; ").append(dcvalues[i].value);
            return sb.toString().substring(2);
        }
    }

    private static BaseFont setBaseFont(String property, boolean embedded)
    {
        String info = ConfigurationManager.getProperty(property);
        if (info == null || info.trim().equals(""))
            return null;

        String[] finfo = info.split(",");
        if (finfo.length != 2)
            return null;

        String font     = finfo[0].trim();
        String encoding = finfo[1].trim();

        try
        {
            return BaseFont.createFont(font, encoding, embedded);
        }
        catch (Exception e)
        {
            log.error("setBaseFont error: " + e.getMessage());
            return null;
        }
    }


    private static Font setFont(BaseFont bs, Font.FontFamily family, int size, int style)
    {
        if (bs == null && family == null)
        {
            return null;
        }
        else if (bs == null)
        {
            return new Font(family, size, style);
        }
        else
        {
            return new Font(bs, size, style);
        }
    }

    private class HeaderFooter extends PdfPageEventHelper {
        public void onEndPage(PdfWriter writer, Document doc) {
            // set header
            if (header_text != null && !header_text.equals("")) {
                ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_CENTER,
                        new Phrase(CoverPage.header_text, CoverPage.FONT_HEADER),
                        (doc.right() - doc.left()) / 2 + doc.leftMargin(),
                        doc.top() + 15,
                        0);
            }
            // set footer
            if (footer_text != null && !footer_text.equals("")) {
                try {

                    facebook = new Font(FONT_SERIF, 8);
                    twitter = new Font(FONT_SERIF, 8);
                    googleplus = new Font(FONT_SERIF, 8);
                    instagram = new Font(FONT_SERIF, 8);

                    facebook.setColor(59, 89, 152);
                    twitter.setColor(41, 197, 246);
                    googleplus.setColor(216, 74, 56);
                    instagram.setColor(188, 128, 95);

                    Image facebook_logo = Image.getInstance(images_path+facebook_path);
                    facebook_logo.scalePercent(15);
                    Image twitter_logo = Image.getInstance(images_path+twitter_path);
                    twitter_logo.scalePercent(15);
                    Image googleplus_logo = Image.getInstance(images_path+googleplus_path);
                    googleplus_logo.scalePercent(15);
                    Image instagram_logo = Image.getInstance(images_path+instagram_path);
                    instagram_logo.scalePercent(15);

                    String fb = new String(" Facebook");
                    String twt = new String(" Twitter");
                    String gplus = new String(" Google Plus");
                    String inst = new String(" Instagram");

                    Paragraph follow = new Paragraph(new Phrase("Follow us on: ", FONT_FOOTER_SERIF));
                    Phrase follow_facebook = new Phrase();
                    follow_facebook.add(new Chunk(fb, facebook).setAnchor("http://www.facebook.com/seafdecaqdlib"));
                    Phrase follow_twitter = new Phrase();
                    follow_twitter.add(new Chunk(twt, twitter).setAnchor("http://twitter.com/seafdecaqdlib"));
                    Phrase follow_gplus = new Phrase();
                    follow_gplus.add(new Chunk(gplus, googleplus).setAnchor("https://plus.google.com/111749266242133800967"));
                    Phrase follow_instagram = new Phrase();
                    follow_instagram.add(new Chunk(inst, instagram).setAnchor("http://instagram.com/seafdecaqdlib"));
                    follow.add(new Chunk(facebook_logo, 0, -2, true).setAnchor("http://www.facebook.com/seafdecaqdlib"));
                    follow.add(follow_facebook);
                    follow.add(" | ");
                    follow.add(new Chunk(twitter_logo, 0, -2, true).setAnchor("http://twitter.com/seafdecaqdlib"));
                    follow.add(follow_twitter);
                    follow.add(" | ");
                    follow.add(new Chunk(googleplus_logo, 0, -2, true).setAnchor("https://plus.google.com/111749266242133800967"));
                    follow.add(follow_gplus);
                    follow.add(" | ");
                    follow.add(new Chunk(instagram_logo, 0, -2, true).setAnchor("http://instagram.com/seafdecaqdlib"));
                    follow.add(follow_instagram);
                    follow.setAlignment(Element.ALIGN_CENTER);

                    PdfPTable footer_table = new PdfPTable(1);
                    footer_table.setTotalWidth(564f);

                    PdfPCell social = new PdfPCell(follow);
                    social.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    social.setHorizontalAlignment(Element.ALIGN_CENTER);
                    social.setBorder(Rectangle.TOP);
                    social.setPaddingBottom(-2);

                    Paragraph pfooter= new Paragraph(footer_text.replaceAll(";", "\n"), FONT_FOOTER_SERIF);

                    PdfPCell cell = new PdfPCell(pfooter);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBorder(Rectangle.NO_BORDER);
                    cell.setLeading(11, 0);
                    footer_table.addCell(social);
                    footer_table.addCell(cell);
                    footer_table.writeSelectedRows(0, -1, 24, footer_table.getTotalHeight() + 9, writer.getDirectContent());
                /*ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_CENTER,
                        new Phrase(CoverPage.footer_text, CoverPage.FONT_FOOTER_SERIF),
                        (doc.right() - doc.left()) / 2 + doc.leftMargin(),
                        doc.bottom(),
                        0);*/
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Community[] setOmitCommunities()
    {
        Context c = null;
        try
        {
            ArrayList<Community> comms = new ArrayList<Community>();
            String omits = ConfigurationManager.getProperty("coverpage.omit.community");
            if (omits == null || "".equals(omits.trim()))
                return null;

            c = new Context();
            for (String comm : omits.split(","))
            {
                DSpaceObject dso = HandleManager.resolveToObject(c, comm);
                if (dso != null && dso.getType() == Constants.COMMUNITY)
                {
                    comms.add((Community)dso);
                }
            }
            c.complete();

            return comms.toArray(new Community[comms.size()]);
        }
        catch (Exception e)
        {
            if (c != null)
            {
                c.abort();
            }
            return null;
        }
    }

    private static Collection[] setOmitCollections()
    {
        Context c = null;
        try
        {
            ArrayList<Collection> colls = new ArrayList<Collection>();
            String omits = ConfigurationManager.getProperty("coverpage.omit.collection");
            if (omits == null || "".equals(omits.trim()))
                return null;

            c = new Context();
            for (String coll : omits.split(","))
            {
                DSpaceObject dso = HandleManager.resolveToObject(c, coll);
                if (dso != null && dso.getType() == Constants.COLLECTION)
                {
                    colls.add((Collection)dso);
                }
            }
            c.complete();

            return colls.toArray(new Collection[colls.size()]);
        }
        catch (Exception e)
        {
            if (c != null)
            {
                c.abort();
            }
            return null;
        }
    }

    public String getIp() throws Exception {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            return ip;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getEperson() {
        // get current user
        EPerson loggedin = context.getCurrentUser();
        String eperson = null;
        if (loggedin != null)
        {
            eperson = loggedin.getFullName();
            return eperson;
        }
        else return "[Anonymous]";
    }
    /**
     * <p>
     * A primitive attempt at copying links from page <code>sourcePage</code>
     * of <code>PdfReader reader</code> to page <code>targetPage</code> of
     * <code>PdfStamper stamper</code>.
     * </p>
     * <p>
     * This method is meant only for the use case at hand, i.e. copying a link
     * to an external URI without expecting any advanced features.
     * </p>
     */
    public void copyLinks(PdfStamper stamper, int targetPage, PdfReader reader, int sourcePage)
    {
        PdfDictionary sourcePageDict = reader.getPageNRelease(sourcePage);
        PdfArray annotations = sourcePageDict.getAsArray(PdfName.ANNOTS);
        if (annotations != null && annotations.size() > 0)
        {
            for (PdfObject annotationObject : annotations)
            {
                annotationObject = PdfReader.getPdfObject(annotationObject);
                if (!annotationObject.isDictionary())
                    continue;
                PdfDictionary annotation = (PdfDictionary) annotationObject;
                if (!PdfName.LINK.equals(annotation.getAsName(PdfName.SUBTYPE)))
                    continue;

                PdfArray rectArray = annotation.getAsArray(PdfName.RECT);
                if (rectArray == null || rectArray.size() < 4)
                    continue;
                Rectangle rectangle = PdfReader.getNormalizedRectangle(rectArray);

                PdfName hightLight = annotation.getAsName(PdfName.H);
                if (hightLight == null)
                    hightLight = PdfAnnotation.HIGHLIGHT_INVERT;

                PdfDictionary actionDict = annotation.getAsDict(PdfName.A);
                if (actionDict == null || !PdfName.URI.equals(actionDict.getAsName(PdfName.S)))
                    continue;
                PdfString urlPdfString = actionDict.getAsString(PdfName.URI);
                if (urlPdfString == null)
                    continue;
                PdfAction action = new PdfAction(urlPdfString.toString());

                PdfAnnotation link = PdfAnnotation.createLink(stamper.getWriter(), rectangle, hightLight, action);
                link.setBorder(new PdfBorderArray(0, 0, 0));
                link.setFlags(PdfAnnotation.FLAGS_PRINT);
                stamper.addAnnotation(link, targetPage);
            }
        }
    }
}
