/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.xoai.services.impl.cache;

import static com.lyncode.xoai.dataprovider.core.Granularity.Second;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.io.IOUtils.write;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import javax.xml.stream.XMLStreamException;

import com.lyncode.xoai.dataprovider.core.XOAIManager;
import com.lyncode.xoai.dataprovider.exceptions.WritingXmlException;
import com.lyncode.xoai.dataprovider.xml.XmlOutputContext;
import com.lyncode.xoai.dataprovider.xml.oaipmh.OAIPMH;
import com.lyncode.xoai.util.Base64Utils;
import org.apache.commons.io.FileUtils;
import org.dspace.xoai.services.api.cache.XOAICacheService;
import org.dspace.xoai.services.api.config.ConfigurationService;
import org.dspace.xoai.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;


public class DSpaceXOAICacheService implements XOAICacheService {
    private static final String REQUEST_DIR = File.separator + "requests";
    private static String baseDir;
    private static String staticHead;

    @Autowired
    ConfigurationService configurationService;

    private String getBaseDir() {
        if (baseDir == null) {
            String dir = configurationService.getProperty("oai.cache.dir") + REQUEST_DIR;
            baseDir = dir;
        }
        return baseDir;
    }

    private static String getStaticHead(XOAIManager manager, Instant date) {
        if (staticHead == null) {
            staticHead = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + ((manager.hasStyleSheet()) ? ("<?xml-stylesheet type=\"text/xsl\" href=\""
                + manager.getStyleSheet() + "\"?>") : "")
                + "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" xmlns:xsi=\"http://www" +
                ".w3.org/2001/XMLSchema-instance\" "
                + "xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ http://www.openarchives" +
                ".org/OAI/2.0/OAI-PMH.xsd\">";
        }

        return staticHead + "<responseDate>" + DateUtils.format(date) + "</responseDate>";
    }

    private final XOAIManager manager;

    public DSpaceXOAICacheService(XOAIManager manager) {
        this.manager = manager;
    }

    private File getCacheFile(String id) {
        File dir = new File(getBaseDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String name = File.separator + Base64Utils.encode(id);
        return new File(getBaseDir() + name);
    }

    @Override
    public boolean isActive() {
        return configurationService.getBooleanProperty("oai.cache", true);
    }

    @Override
    public boolean hasCache(String requestID) {
        return this.getCacheFile(requestID).exists();
    }

    @Override
    public void handle(String requestID, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(this.getCacheFile(requestID));
        write(getStaticHead(manager, Instant.now()), out);
        copy(in, out);
        in.close();
    }

    @Override
    public void store(String requestID, OAIPMH response) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            XmlOutputContext context = XmlOutputContext.emptyContext(output, Second);
            response.write(context);
            context.getWriter().flush();
            context.getWriter().close();

            String xoaiResponse = output.toString();

            // Cutting the header (to allow one to change the response time)
            String end = "</responseDate>";
            int pos = xoaiResponse.indexOf(end);
            if (pos > 0) {
                xoaiResponse = xoaiResponse.substring(pos + (end.length()));
            }

            FileUtils.write(this.getCacheFile(requestID), xoaiResponse);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        } catch (WritingXmlException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void delete(String requestID) {
        this.getCacheFile(requestID).delete();
    }

    @Override
    public void deleteAll() throws IOException {
        deleteDirectory(new File(getBaseDir()));
    }

}
