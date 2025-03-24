/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.requestitem.factory.RequestItemServiceFactory;
import org.dspace.app.requestitem.service.RequestItemService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Context;
import org.dspace.disseminate.service.CitationDocumentService;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.GroupService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.utils.DSpace;
import org.springframework.core.io.AbstractResource;

/**
 * This class acts as a {@link AbstractResource} used by Spring's framework to send the data in a proper and
 * streamlined way inside the {@link org.springframework.http.ResponseEntity} body.
 * This class' attributes are being used by Spring's framework in the overridden methods so that the proper
 * attributes are given and used in the response.
 *
 * Unlike the BitstreamResource, this resource expects a valid and authorised access token to use when
 * retrieving actual bitstream data. It will either set a special group in the temp context for READ of the
 * bitstream, or turn off authorisation for the lifetime of the temp (autocloseable in try-with-resources) context
 *
 * @author Kim Shepherd
 */
public class BitstreamResourceAccessByToken extends BitstreamResource {

    private String name;
    private UUID uuid;
    private boolean shouldGenerateCoverPage;
    private byte[] file;
    private Set<UUID> currentSpecialGroups;
    private String accessToken;

    private BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
    private CitationDocumentService citationDocumentService =
        new DSpace().getServiceManager()
                    .getServicesByType(CitationDocumentService.class).get(0);

    private RequestItemService requestItemService = RequestItemServiceFactory.getInstance().getRequestItemService();

    private ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();
    private GroupService groupService = EPersonServiceFactory.getInstance().getGroupService();

    /**
     * Initialise logger
     */
    Logger log = LogManager.getLogger();

    public BitstreamResourceAccessByToken(String name, UUID uuid, UUID currentUserUUID, Set<UUID> currentSpecialGroups,
                                          boolean shouldGenerateCoverPage, String accessToken) {
        super(name, uuid, currentUserUUID, currentSpecialGroups, shouldGenerateCoverPage);
        this.name = name;
        this.uuid = uuid;
        this.currentSpecialGroups = currentSpecialGroups;
        this.shouldGenerateCoverPage = shouldGenerateCoverPage;
        this.accessToken = accessToken;
    }

    /**
     * Get the input stream using the special temporary context if the request-a-copy access request
     * is properly authenticated and authorised
     *
     * @return input stream
     * @throws IOException
     */
    @Override
    public InputStream getInputStream() throws IOException {

        // If the feature is not enabled, throw exception
        if (configurationService.getProperty("request.item.type") == null) {
            throw new RuntimeException("Request a copy is not enabled, download via access token will not be allowed");
        }

        try (Context fileRetrievalContext = initializeContext()) {
            // Set special privileges for context for this access
            // Note - this is a try-with-resources statement. The context has a very limited lifetime.
            // However, be very careful using context in this block! It should ONLY perform authorization
            // of the access token and retrieval of the bitstream content.
            fileRetrievalContext.turnOffAuthorisationSystem();
            // Get bitstream from uuid
            Bitstream bitstream = bitstreamService.find(fileRetrievalContext, uuid);

            try {
                // Explicitly authenticate the access request acceptance for the bitstream
                // even if we have already done it in the REST controller and throw Authorize exception if not valid
                requestItemService.authorizeAccessByAccessToken(fileRetrievalContext, bitstream, this.accessToken);

            } catch (AuthorizeException e) {
                throw new AuthorizeException("Authorization to bitstream " + uuid + " by access token FAILED");
            }

            // Initialise outstream
            InputStream out;

            if (shouldGenerateCoverPage) {
                out = new ByteArrayInputStream(getCoverpageByteArray(fileRetrievalContext, bitstream));
            } else {
                out = bitstreamService.retrieve(fileRetrievalContext, bitstream);
            }

            this.file = null;
            return out;

            // This is the last line in the closeable Context try-with-resources.
            // If this scope completes, there should be no more context.

        } catch (SQLException | AuthorizeException e) {
            throw new IOException(e);
        }
    }

    /**
     * Initialise a new temporary context just for the use of this download
     *
     * @return totally new context
     * @throws SQLException
     * @throws AuthorizeException
     */
    private Context initializeContext() throws SQLException, AuthorizeException {
        return new Context();
    }

}