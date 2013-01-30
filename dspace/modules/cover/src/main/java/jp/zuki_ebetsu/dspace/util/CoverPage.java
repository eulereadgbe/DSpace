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

import org.apache.log4j.Logger;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Bitstream;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.content.DCDate;
import org.dspace.content.DCValue;
import org.dspace.content.Item;
import org.dspace.content.Bundle;
import org.dspace.content.Bitstream;
import org.dspace.handle.HandleManager;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.ICC_Profile;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfEncryption;
import com.itextpdf.text.pdf.PdfFileSpecification;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PRAcroForm;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.SimpleBookmark;
import com.itextpdf.text.pdf.collection.PdfCollection;

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
    protected static final Font FONT_HEADER = setFont(FONT_SANS, Font.FontFamily.HELVETICA, 18, Font.NORMAL);

    /** Footer Font */
    protected static final Font FONT_FOOTER = setFont(FONT_SANS, Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    /** Cell Tag Font */
    private static final Font FONT_TAG    = setFont(FONT_SANS, Font.FontFamily.HELVETICA, 12, Font.NORMAL);

    /** Cell Value Font */
    private static final Font FONT_VALUE  = setFont(FONT_SERIF, Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);

    /** Date Font */
    private static final Font FONT_DATE   = setFont(FONT_SANS, Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    /** Message Font */
    private static final Font FONT_MESS    = setFont(FONT_SERIF, Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);

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
        InputStream coverStream = null;

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
            coverStream = getCoverStream();
            if (coverStream == null) 
                return null;

            byteout = new ByteArrayOutputStream();
            int pageOffset = 0;
            ArrayList<HashMap<String, Object>> master = new ArrayList<HashMap<String, Object>>();
    
            Document document = null;
            PdfCopy    writer = null;
            PdfReader  reader = null;

            byte[] password = (ownerpass != null && !"".equals(ownerpass)) ? ownerpass.getBytes() : null;

            // Get infomation of the original pdf
            reader = new PdfReader(bitstream.retrieve(), password);

            boolean isPortfolio = reader.getCatalog().contains(PdfName.COLLECTION);
            char version = reader.getPdfVersion();
            int permissions = reader.getPermissions();

            // Get metadata
            HashMap<String, String> info = reader.getInfo();
            String title = (info.get("Title") == null || "".equals(info.get("Title")))
                ? getFieldValue("dc.title") : info.get("Title");
            String author = (info.get("Author") == null || "".equals(info.get("Author")))
                ? getFieldValue("dc.contributor.author") : info.get("Author");
            String subject = (info.get("Subject") == null || "".equals(info.get("Subject")))
                ? "" : info.get("Subject");
            String keywords = (info.get("Keywords") == null || "".equals(info.get("Keywords")))
                ? "" : info.get("Keywords");

            reader.close();

            // Merge cover page and the original pdf
            InputStream[] is = new InputStream[2];
            is[0] = coverStream;
            is[1] = bitstream.retrieve();

            for (int i = 0; i < is.length; i++) 
            {
                // we create a reader for a certain document
                reader = new PdfReader(is[i], password);
                reader.consolidateNamedDestinations();

                if (i == 0) 
                {
                    // step 1: creation of a document-object
                    document = new Document(reader.getPageSizeWithRotation(1));

                    // step 2: we create a writer that listens to the document
                    writer = new PdfCopy(document, byteout);

                    // Set metadata from the original pdf 
                    // the position of these lines is important
                    document.addTitle(title);
                    document.addAuthor(author);
                    document.addSubject(subject);
                    document.addKeywords(keywords);

                    if (pdfa)
                    {
                        // Set thenecessary information for PDF/A-1B
                        // the position of these lines is important
                        writer.setPdfVersion(PdfWriter.VERSION_1_4);
                        writer.setPDFXConformance(PdfWriter.PDFA1B);
                        writer.createXmpMetadata();
                    }
                    else if (version == '5')
                        writer.setPdfVersion(PdfWriter.VERSION_1_5);
                    else if (version == '6')
                        writer.setPdfVersion(PdfWriter.VERSION_1_6);
                    else if (version == '7')
                        writer.setPdfVersion(PdfWriter.VERSION_1_7);
                    else
                        ;  // no operation

                    // Set security parameters
                    if (!pdfa)
                    {
                        if (password != null)
                        {
                            if (security && permissions != 0) 
                            {
                                writer.setEncryption(null, password, permissions, PdfWriter.STANDARD_ENCRYPTION_128);
                            } 
                            else
                            {
                                writer.setEncryption(null, password, PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_SCREENREADERS, PdfWriter.STANDARD_ENCRYPTION_128);
                            }
                        }
                    }

                    // step 3: we open the document
                    document.open();

                    // if this pdf is portfolio, does not add cover page
                    if (isPortfolio)
                    {
                        reader.close();
                        byte[] coverByte = getCoverByte();
                        if (coverByte == null || coverByte.length == 0) 
                            return null;
                        PdfCollection collection = new PdfCollection(PdfCollection.TILE);
                        writer.setCollection(collection);

                        PdfFileSpecification fs = PdfFileSpecification.fileEmbedded(writer, null, "cover.pdf", coverByte);
                        fs.addDescription("cover.pdf", false);
                        writer.addFileAttachment(fs);
                        continue;
                    }
                }
                int n = reader.getNumberOfPages();
                // step 4: we add content
                PdfImportedPage page;
                for (int j = 0; j < n; ) 
                {
                    ++j;
                    page = writer.getImportedPage(reader, j);
                    writer.addPage(page);
                }

                PRAcroForm form = reader.getAcroForm();
                if (form != null && !pdfa)
                {
                    writer.copyAcroForm(reader);
                }
                // we retrieve the total number of pages
                List<HashMap<String, Object>> bookmarks = SimpleBookmark.getBookmark(reader);
                //if (bookmarks != null && !pdfa) 
                if (bookmarks != null) 
                {
                    if (pageOffset != 0)
                    {
                        SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
                    }
                    master.addAll(bookmarks);
                }
                pageOffset += n;
            }
            if (!master.isEmpty())
            {
                writer.setOutlines(master);
            }

            if (isPortfolio)
            {
                reader = new PdfReader(bitstream.retrieve(), password);
                PdfDictionary catalog = reader.getCatalog();
                PdfDictionary documentnames = catalog.getAsDict(PdfName.NAMES);
                PdfDictionary embeddedfiles = documentnames.getAsDict(PdfName.EMBEDDEDFILES);
                PdfArray filespecs = embeddedfiles.getAsArray(PdfName.NAMES);
                PdfDictionary filespec;
                PdfDictionary refs;
                PRStream stream;
                PdfFileSpecification fs;
                String path;
                // copy embedded files
                for (int i = 0; i < filespecs.size(); ) 
                {
                    filespecs.getAsString(i++);     // remove description
                    filespec = filespecs.getAsDict(i++);
                    refs = filespec.getAsDict(PdfName.EF);
                    for (PdfName key : refs.getKeys()) 
                    {
                        stream = (PRStream) PdfReader.getPdfObject(refs.getAsIndirectObject(key));
                        path = filespec.getAsString(key).toString();
                        fs = PdfFileSpecification.fileEmbedded(writer, null, path, PdfReader.getStreamBytes(stream));
                        fs.addDescription(path, false);
                        writer.addFileAttachment(fs);
                    }
                }
            }

            if (pdfa)
            {
                InputStream iccFile = this.getClass().getClassLoader().getResourceAsStream(PROFILE);
                ICC_Profile icc = ICC_Profile.getInstance(iccFile);
                writer.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);
                writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
            }
            // step 5: we close the document
            document.close();
        } 
        catch (Exception e) 
        {
            log.info(LogManager.getHeader(context, "cover_page: getConcatenatePDF", "bitstream_id="+bitstream.getID()+", error="+e.getMessage())); 
            // e.printStackTrace();
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
            doc = new Document(PageSize.A4, 20, 20, 50, 50); 
            PdfWriter pdfwriter = PdfWriter.getInstance(doc, byteout);

            pdfwriter.setPageEvent(new HeaderFooter());
            doc.open(); 

            if (logo_path != null && !logo_path.equals("")) 
            {
                Image img = Image.getInstance(logo_path);
                img.scalePercent(72.0f / 96.0f * 150f);
                img.setAlignment(Element.ALIGN_CENTER);
                doc.add(img);
                doc.add(new Paragraph(""));
            }

            Phrase ph_head = new Phrase("SEAFDEC/AQD Institutional Repository (SAIR)", FONT_HEADER);
            Paragraph para_head = new Paragraph();
            para_head.setAlignment(Element.ALIGN_CENTER);
            para_head.add(ph_head);
            doc.add(new Paragraph(""));
            doc.add(para_head);
            doc.add(new Paragraph(""));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(90f);
            int table_width[] = {25,75};    
            table.setWidths(table_width);
            table.setSpacingBefore(20f);

            for (int i=0, len=fields.length; i<len; i++)
            {
                String[] flds = fields[i].split(":");

                // Tag
                PdfPCell tag = new PdfPCell(new Phrase(flds[0], FONT_TAG));
                tag.setGrayFill(0.8f);
                tag.setHorizontalAlignment(Element.ALIGN_LEFT);
                tag.setVerticalAlignment(Element.ALIGN_MIDDLE);
                tag.setPaddingLeft(5f);

                PdfPCell value = new PdfPCell(new Phrase(getFieldValue(flds[1]), FONT_VALUE));
                value.setHorizontalAlignment(Element.ALIGN_LEFT);
                value.setVerticalAlignment(Element.ALIGN_MIDDLE);
                value.setMinimumHeight(40f);
                value.setPadding(5f);

                table.addCell(tag);
                table.addCell(value);
            }

            doc.add(table);

            Paragraph p = new Paragraph();
            p.setAlignment(Element.ALIGN_RIGHT);
            //String downTime = "This document is downloaded at: " + DCDate.getCurrent().toString();
            String downTime = "This document is downloaded at: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date());
            Phrase phrase = new Phrase(downTime, FONT_DATE);
            p.add(phrase);
            doc.add(p);

            java.net.URL url = new
                    java.net.URL("https://chart.googleapis.com/chart?cht=qr&chs=200x200&chl="
                    + org.dspace.handle.HandleManager.getCanonicalForm(item.getHandle()));
            Image qrcode = Image.getInstance(url);
            qrcode.scalePercent(72.0f / 96.0f * 100f);
            qrcode.setAlignment(Element.ALIGN_CENTER);
            doc.add(qrcode);

            if (logo2_path != null && !logo2_path.equals("")) 
            {
                p = new Paragraph("");
                p.setSpacingBefore(60f);
                doc.add(p);

                Image img2 = Image.getInstance(logo2_path);
                img2.scalePercent(72.0f / 96.0f * 100f);
                img2.setAlignment(Element.ALIGN_CENTER);
                doc.add(img2);
                doc.add(new Paragraph(""));
            }

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
        DCValue[] dcvalues = item.getMetadata(field);
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

    private class HeaderFooter extends PdfPageEventHelper
    {
        public void onEndPage(PdfWriter writer, Document doc)
        {
            // set header
            if (header_text != null && !header_text.equals("")) 
            {
                ColumnText.showTextAligned(writer.getDirectContent(), 
                    Element.ALIGN_CENTER,
                    new Phrase(CoverPage.header_text, CoverPage.FONT_HEADER),
                    (doc.right() - doc.left()) / 2 + doc.leftMargin(), 
                    doc.top() + 15, 
                    0);
            } 
            // set footer
            if (footer_text != null && !footer_text.equals("")) 
            {
                ColumnText.showTextAligned(writer.getDirectContent(), 
                    Element.ALIGN_CENTER,
                    new Phrase(CoverPage.footer_text, CoverPage.FONT_FOOTER),
                    (doc.right() - doc.left()) / 2 + doc.leftMargin(), 
                    doc.bottom() - 15, 
                    0);
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

}
