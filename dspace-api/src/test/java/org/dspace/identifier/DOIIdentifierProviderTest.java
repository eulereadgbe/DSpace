/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.identifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.logic.DefaultFilter;
import org.dspace.content.logic.LogicalStatement;
import org.dspace.content.logic.TrueFilter;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.identifier.doi.DOIConnector;
import org.dspace.identifier.doi.DOIIdentifierException;
import org.dspace.identifier.doi.DOIIdentifierNotApplicableException;
import org.dspace.identifier.factory.IdentifierServiceFactory;
import org.dspace.identifier.service.DOIService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.workflow.WorkflowException;
import org.dspace.workflow.WorkflowItem;
import org.dspace.workflow.factory.WorkflowServiceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DOIIdentifierProvider}.
 *
 * @author Mark H. Wood
 * @author Pascal-Nicolas Becker
 */
public class DOIIdentifierProviderTest
    extends AbstractUnitTest {
    /**
     * log4j category
     */
    private static final Logger log = LogManager.getLogger(DOIIdentifierProviderTest.class);

    private static final String PREFIX = "10.5072";
    private static final String NAMESPACE_SEPARATOR = "dspaceUnitTests-";

    private static ConfigurationService config = null;

    protected DOIService doiService = IdentifierServiceFactory.getInstance().getDOIService();
    protected CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
    protected CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
    protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    protected WorkspaceItemService workspaceItemService = ContentServiceFactory.getInstance().getWorkspaceItemService();


    private static Community community;
    private static Collection collection;

    private static DOIConnector connector;
    private DOIIdentifierProvider provider;

    public DOIIdentifierProviderTest() {
    }

    /**
     * This method will be run before every test as per @Before. It will
     * initialize resources required for the tests.
     *
     * Other methods can be annotated with @Before here or in subclasses
     * but no execution order is guaranteed
     */
    @Before
    @Override
    public void init() {
        super.init();

        try {
            context.turnOffAuthorisationSystem();
            // Create an environment for our test objects to live in.
            community = communityService.create(null, context);
            communityService.setMetadataSingleValue(context, community,
                    CommunityService.MD_NAME, null, "A Test Community");
            communityService.update(context, community);
            collection = collectionService.create(context, community);
            collectionService.setMetadataSingleValue(context, collection,
                    CollectionService.MD_NAME, null, "A Test Collection");
            collectionService.update(context, collection);
            //we need to commit the changes so we don't block the table for testing
            context.restoreAuthSystemState();

            config = DSpaceServicesFactory.getInstance().getConfigurationService();
            // Configure the service under test.
            config.setProperty(DOIIdentifierProvider.CFG_PREFIX, PREFIX);
            config.setProperty(DOIIdentifierProvider.CFG_NAMESPACE_SEPARATOR,
                               NAMESPACE_SEPARATOR);

            connector = mock(DOIConnector.class);

            provider = new DOIIdentifierProvider();
            provider.doiService = doiService;
            provider.contentServiceFactory = ContentServiceFactory.getInstance();
            provider.itemService = itemService;
            provider.setConfigurationService(config);
            provider.setDOIConnector(connector);
            provider.setFilter(null);
        } catch (AuthorizeException ex) {
            log.error("Authorization Error in init", ex);
            fail("Authorization Error in init: " + ex.getMessage());
        } catch (SQLException ex) {
            log.error("SQL Error in init", ex);
            fail("SQL Error in init: " + ex.getMessage());
        }

    }

    /**
     * This method will be run after every test as per @After. It will
     * clean resources initialized by the @Before methods.
     *
     * Other methods can be annotated with @After here or in subclasses
     * but no execution order is guaranteed
     */
    @After
    @Override
    public void destroy() {
        community = null;
        collection = null;
        connector = null;
        provider = null;
        super.destroy();
    }

    /**
     * Create a fresh Item, installed in the repository.
     *
     * @throws SQLException       if database error
     * @throws AuthorizeException if authorization error
     * @throws IOException        if IO error
     */
    private Item newItem()
        throws SQLException, AuthorizeException, IOException, IllegalAccessException, IdentifierException,
        WorkflowException {
        context.turnOffAuthorisationSystem();

        WorkspaceItem wsItem = workspaceItemService.create(context, collection, false);

        WorkflowItem wfItem = WorkflowServiceFactory.getInstance().getWorkflowService().start(context, wsItem);

        Item item = wfItem.getItem();
        itemService.addMetadata(context, item, "dc", "contributor", "author", null, "Author, A. N.");
        itemService.addMetadata(context, item, "dc", "title", null, null, "A Test Object");
        itemService.addMetadata(context, item, "dc", "publisher", null, null, "DSpace Test Harness");

        // If DOIIdentifierProvider is configured
        // (dspace/conf/spring/api/identifier-service.xml) the new created item
        // gets automatically a DOI. We remove this DOI as it can make problems
        // with the tests.
        provider.delete(context, item);

        List<MetadataValue> metadata = itemService.getMetadata(item,
                                                               DOIIdentifierProvider.MD_SCHEMA,
                                                               DOIIdentifierProvider.DOI_ELEMENT,
                                                               DOIIdentifierProvider.DOI_QUALIFIER,
                                                               null);
        List<String> remainder = new ArrayList<>();

        for (MetadataValue id : metadata) {
            if (!id.getValue().startsWith(doiService.getResolver())) {
                remainder.add(id.getValue());
            }
        }

        itemService.clearMetadata(context, item,
                                  DOIIdentifierProvider.MD_SCHEMA,
                                  DOIIdentifierProvider.DOI_ELEMENT,
                                  DOIIdentifierProvider.DOI_QUALIFIER,
                                  null);
        itemService.addMetadata(context, item, DOIIdentifierProvider.MD_SCHEMA,
                                DOIIdentifierProvider.DOI_ELEMENT,
                                DOIIdentifierProvider.DOI_QUALIFIER,
                                null,
                                remainder);

        itemService.update(context, item);
        //we need to commit the changes so we don't block the table for testing
        context.restoreAuthSystemState();

        return item;
    }

    public String createDOI(Item item, Integer status, boolean metadata)
        throws SQLException, IdentifierException, AuthorizeException {
        return this.createDOI(item, status, metadata, null);
    }

    /**
     * Create a DOI to an item.
     *
     * @param item     Item the DOI should be created for.
     * @param status   The status of the DOI.
     * @param metadata Whether the DOI should be included in the metadata of the item.
     * @param doi      The DOI or null if we should generate one.
     * @return the DOI
     * @throws SQLException if database error
     * @throws org.dspace.identifier.IdentifierException passed through.
     * @throws org.dspace.authorize.AuthorizeException passed through.
     */
    public String createDOI(Item item, Integer status, boolean metadata, String doi)
        throws SQLException, IdentifierException, AuthorizeException {
        context.turnOffAuthorisationSystem();
        // we need some random data. UUIDs would be bloated here
        Random random = new Random();
        if (null == doi) {
            doi = DOI.SCHEME + PREFIX + "/" + NAMESPACE_SEPARATOR
                + Long.toHexString(Instant.now().toEpochMilli()) + "-"
                + random.nextInt(997);
        }

        DOI doiRow = doiService.create(context);
        doiRow.setDoi(doi.substring(DOI.SCHEME.length()));
        doiRow.setDSpaceObject(item);
        doiRow.setStatus(status);
        doiService.update(context, doiRow);

        if (metadata) {
            itemService.addMetadata(context, item, DOIIdentifierProvider.MD_SCHEMA,
                                    DOIIdentifierProvider.DOI_ELEMENT,
                                    DOIIdentifierProvider.DOI_QUALIFIER,
                                    null,
                                    doiService.DOIToExternalForm(doi));
            itemService.update(context, item);
        }

        //we need to commit the changes so we don't block the table for testing
        context.restoreAuthSystemState();
        return doi;
    }

    /**
     * Test of supports method, of class DataCiteIdentifierProvider.
     */
    @Test
    public void testSupports_Class() {
        Class<? extends Identifier> identifier = DOI.class;
        assertTrue("DOI should be supported", provider.supports(identifier));
    }

    @Test
    public void testSupports_valid_String() {
        String[] validDOIs = new String[] {
            "10.5072/123abc-lkj/kljl",
            PREFIX + "/" + NAMESPACE_SEPARATOR + "lkjljasd1234",
            DOI.SCHEME + "10.5072/123abc-lkj/kljl",
            "http://dx.doi.org/10.5072/123abc-lkj/kljl",
            doiService.getResolver() + "/10.5072/123abc-lkj/kljl"
        };

        for (String doi : validDOIs) {
            assertTrue("DOI " + doi + " should be supported", provider.supports(doi));
        }
    }

    @Test
    public void testDoes_not_support_invalid_String() {
        String[] invalidDOIs = new String[] {
            "11.5072/123abc-lkj/kljl",
            "http://hdl.handle.net/handle/10.5072/123abc-lkj/kljl",
            "",
            null
        };

        for (String notADoi : invalidDOIs) {
            assertFalse("Invalid DOIs shouldn't be supported",
                        provider.supports(notADoi));
        }
    }

    @Test
    public void testStore_DOI_as_item_metadata()
        throws SQLException, AuthorizeException, IOException, IdentifierException, IllegalAccessException,
        WorkflowException {
        Item item = newItem();
        String doi = DOI.SCHEME + PREFIX + "/" + NAMESPACE_SEPARATOR
            + Long.toHexString(Instant.now().toEpochMilli());
        context.turnOffAuthorisationSystem();
        provider.saveDOIToObject(context, item, doi);
        context.restoreAuthSystemState();

        List<MetadataValue> metadata = itemService.getMetadata(item, DOIIdentifierProvider.MD_SCHEMA,
                                                               DOIIdentifierProvider.DOI_ELEMENT,
                                                               DOIIdentifierProvider.DOI_QUALIFIER,
                                                               null);
        boolean result = false;
        for (MetadataValue id : metadata) {
            if (id.getValue().equals(doiService.DOIToExternalForm(doi))) {
                result = true;
            }
        }
        assertTrue("Cannot store DOI as item metadata value.", result);
    }

    @Test
    public void testGet_DOI_out_of_item_metadata()
        throws SQLException, AuthorizeException, IOException, IdentifierException, IllegalAccessException,
        WorkflowException {
        Item item = newItem();
        String doi = DOI.SCHEME + PREFIX + "/" + NAMESPACE_SEPARATOR
            + Long.toHexString(Instant.now().toEpochMilli());

        context.turnOffAuthorisationSystem();
        itemService.addMetadata(context, item, DOIIdentifierProvider.MD_SCHEMA,
                                DOIIdentifierProvider.DOI_ELEMENT,
                                DOIIdentifierProvider.DOI_QUALIFIER,
                                null,
                                doiService.DOIToExternalForm(doi));
        itemService.update(context, item);
        context.restoreAuthSystemState();

        assertEquals("Failed to recognize DOI in item metadata.",
                doi, provider.getDOIOutOfObject(item));
    }

    @Test
    public void testRemove_DOI_from_item_metadata()
        throws SQLException, AuthorizeException, IOException, IdentifierException, WorkflowException,
        IllegalAccessException {
        Item item = newItem();
        String doi = DOI.SCHEME + PREFIX + "/" + NAMESPACE_SEPARATOR
            + Long.toHexString(Instant.now().toEpochMilli());

        context.turnOffAuthorisationSystem();
        itemService.addMetadata(context, item, DOIIdentifierProvider.MD_SCHEMA,
                                DOIIdentifierProvider.DOI_ELEMENT,
                                DOIIdentifierProvider.DOI_QUALIFIER,
                                null,
                                doiService.DOIToExternalForm(doi));
        itemService.update(context, item);

        provider.removeDOIFromObject(context, item, doi);
        context.restoreAuthSystemState();

        List<MetadataValue> metadata = itemService.getMetadata(item, DOIIdentifierProvider.MD_SCHEMA,
                                                               DOIIdentifierProvider.DOI_ELEMENT,
                                                               DOIIdentifierProvider.DOI_QUALIFIER,
                                                               null);
        boolean foundDOI = false;
        for (MetadataValue id : metadata) {
            if (id.getValue().equals(doiService.DOIToExternalForm(doi))) {
                foundDOI = true;
            }
        }
        assertFalse("Cannot remove DOI from item metadata.", foundDOI);
    }

    @Test
    public void testGet_DOI_by_DSpaceObject()
        throws SQLException, AuthorizeException, IOException,
        IllegalArgumentException, IdentifierException, WorkflowException, IllegalAccessException {
        Item item = newItem();
        String doi = this.createDOI(item, DOIIdentifierProvider.IS_REGISTERED, false);

        String retrievedDOI = provider.getDOIByObject(context, item);

        assertNotNull("Failed to load DOI by DSpaceObject.", retrievedDOI);
        assertTrue("Loaded wrong DOI by DSpaceObject.", doi.equals(retrievedDOI));
    }

    @Test
    public void testGet_DOI_lookup()
        throws SQLException, AuthorizeException, IOException,
        IllegalArgumentException, IdentifierException, WorkflowException, IllegalAccessException {
        Item item = newItem();
        String doi = this.createDOI(item, DOIIdentifierProvider.IS_REGISTERED, false);

        String retrievedDOI = provider.lookup(context, (DSpaceObject) item);

        assertNotNull("Failed to loookup doi.", retrievedDOI);
        assertTrue("Loaded wrong DOI on lookup.", doi.equals(retrievedDOI));
    }

    @Test
    public void testGet_DSpaceObject_by_DOI()
        throws SQLException, AuthorizeException, IOException,
        IllegalArgumentException, IdentifierException, WorkflowException, IllegalAccessException {
        Item item = newItem();
        String doi = this.createDOI(item, DOIIdentifierProvider.IS_REGISTERED, false);

        DSpaceObject dso = provider.getObjectByDOI(context, doi);

        assertNotNull("Failed to load DSpaceObject by DOI.", dso);
        if (item.getType() != dso.getType() || ObjectUtils.notEqual(item.getID(), dso.getID())) {
            fail("Object loaded by DOI was another object then expected!");
        }
    }

    @Test
    public void testResolve_DOI()
        throws SQLException, AuthorizeException, IOException,
        IllegalArgumentException, IdentifierException, WorkflowException, IllegalAccessException {
        Item item = newItem();
        String doi = this.createDOI(item, DOIIdentifierProvider.IS_REGISTERED, false);

        DSpaceObject dso = provider.resolve(context, doi);

        assertNotNull("Failed to resolve DOI.", dso);
        if (item.getType() != dso.getType() || ObjectUtils.notEqual(item.getID(), dso.getID())) {
            fail("Object return by DOI lookup was another object then expected!");
        }
    }

    /*
     * The following test seems a bit silly, but it was helpful to debug some
     * problems while deleting DOIs.
     */
    @Test
    public void testRemove_two_DOIs_from_item_metadata()
        throws SQLException, AuthorizeException, IOException, IdentifierException, WorkflowException,
        IllegalAccessException {
        // add two DOIs.
        Item item = newItem();
        String doi1 = this.createDOI(item, DOIIdentifierProvider.IS_REGISTERED, true);
        String doi2 = this.createDOI(item, DOIIdentifierProvider.IS_REGISTERED, true);

        // remove one of it
        context.turnOffAuthorisationSystem();
        provider.removeDOIFromObject(context, item, doi1);
        context.restoreAuthSystemState();

        // assure that the right one was removed
        List<MetadataValue> metadata = itemService.getMetadata(item, DOIIdentifierProvider.MD_SCHEMA,
                                                               DOIIdentifierProvider.DOI_ELEMENT,
                                                               DOIIdentifierProvider.DOI_QUALIFIER,
                                                               null);
        boolean foundDOI1 = false;
        boolean foundDOI2 = false;
        for (MetadataValue id : metadata) {
            if (id.getValue().equals(doiService.DOIToExternalForm(doi1))) {
                foundDOI1 = true;
            }
            if (id.getValue().equals(doiService.DOIToExternalForm(doi2))) {
                foundDOI2 = true;
            }

        }
        assertFalse("Cannot remove DOI from item metadata.", foundDOI1);
        assertTrue("Removed wrong DOI from item metadata.", foundDOI2);

        // remove the otherone as well.
        context.turnOffAuthorisationSystem();
        provider.removeDOIFromObject(context, item, doi2);
        context.restoreAuthSystemState();

        // check it
        metadata = itemService.getMetadata(item, DOIIdentifierProvider.MD_SCHEMA,
                                           DOIIdentifierProvider.DOI_ELEMENT,
                                           DOIIdentifierProvider.DOI_QUALIFIER,
                                           null);
        foundDOI1 = false;
        foundDOI2 = false;
        for (MetadataValue id : metadata) {
            if (id.getValue().equals(doiService.DOIToExternalForm(doi1))) {
                foundDOI1 = true;
            }
            if (id.getValue().equals(doiService.DOIToExternalForm(doi2))) {
                foundDOI2 = true;
            }

        }
        assertFalse("Cannot remove DOI from item metadata.", foundDOI1);
        assertFalse("Cannot remove DOI from item metadata.", foundDOI2);
    }

    @Test
    public void testMintDOI()
        throws SQLException, AuthorizeException, IOException, IllegalAccessException, IdentifierException,
        WorkflowException {
        Item item = newItem();
        String doi = null;
        try {
            // get a DOI (skipping any filters)
            doi = provider.mint(context, item);
        } catch (IdentifierException e) {
            e.printStackTrace(System.err);
            fail("Got an IdentifierException: " + e.getMessage());
        }

        assertNotNull("Minted DOI is null!", doi);
        assertFalse("Minted DOI is empty!", doi.isEmpty());

        try {
            doiService.formatIdentifier(doi);
        } catch (DOIIdentifierException e) {
            e.printStackTrace(System.err);
            fail("Minted an unrecognizable DOI: " + e.getMessage());
        }
    }

    @Test
    public void testMint_returns_existing_DOI()
        throws SQLException, AuthorizeException, IOException, IdentifierException, WorkflowException,
        IllegalAccessException {
        Item item = newItem();
        String doi = this.createDOI(item, null, true);

        String retrievedDOI = provider.mint(context, item);

        assertNotNull("Minted DOI is null?!", retrievedDOI);
        assertEquals("Mint did not returned an existing DOI!", doi, retrievedDOI);
    }

    /**
     * Test minting a DOI with a filter that always returns false and therefore never mints the DOI
     */
    @Test
    public void testMint_DOI_withNonMatchingFilter()
        throws SQLException, AuthorizeException, IOException, IllegalAccessException, IdentifierException,
        WorkflowException {
        Item item = newItem();
        boolean wasFiltered = false;
        try {
            // Mint this with the filter
            DefaultFilter doiFilter = new DefaultFilter();
            LogicalStatement alwaysFalse = (context, i) -> false;
            doiFilter.setStatement(alwaysFalse);
            // get a DOI with the method that applies filters by default
            provider.mint(context, item, doiFilter);
        } catch (DOIIdentifierNotApplicableException e) {
            // This is what we wanted to see - we can return safely
            wasFiltered = true;
        } catch (IdentifierException e) {
            e.printStackTrace();
            fail("Got an IdentifierException: " + e.getMessage());
        }
        // Fail the test if the filter didn't throw a "not applicable" exception
        assertTrue("DOI minting attempt was not filtered by filter service", wasFiltered);
    }

    /**
     * Test minting a DOI with a filter that always returns true and therefore allows the DOI to be minted
     * (this should have the same results as base testMint_DOI, but here we use an explicit filter rather than null)
     */
    @Test
    public void testMint_DOI_withMatchingFilter()
        throws SQLException, AuthorizeException, IOException, IllegalAccessException, IdentifierException,
        WorkflowException {
        Item item = newItem();
        String doi = null;
        boolean wasFiltered = false;
        try {
            // Temporarily set the provider to have a filter that always returns true for an item
            // (therefore, the item is allowed to have a DOI minted)
            DefaultFilter doiFilter = new DefaultFilter();
            LogicalStatement alwaysTrue = (context, i) -> true;
            doiFilter.setStatement(alwaysTrue);
            // get a DOI with the method that applies filters by default
            doi = provider.mint(context, item, doiFilter);
        } catch (DOIIdentifierNotApplicableException e) {
            // This is what we wanted to see - we can return safely
            wasFiltered = true;
        } catch (IdentifierException e) {
            e.printStackTrace();
            fail("Got an IdentifierException: " + e.getMessage());
        }
        // If the attempt was filtered, fail
        assertFalse("DOI minting attempt was incorrectly filtered by filter service", wasFiltered);

        // Continue with regular minting tests
        assertNotNull("Minted DOI is null!", doi);
        assertFalse("Minted DOI is empty!", doi.isEmpty());
        try {
            doiService.formatIdentifier(doi);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Minted an unrecognizable DOI: " + e.getMessage());
        }
    }


    @Test
    public void testReserve_DOI()
        throws SQLException, SQLException, AuthorizeException, IOException,
        IdentifierException, WorkflowException, IllegalAccessException {
        Item item = newItem();
        String doi = this.createDOI(item, null, true);

        provider.reserve(context, item, doi);

        DOI doiRow = doiService.findByDoi(context, doi.substring(DOI.SCHEME.length()));
        assumeNotNull(doiRow);

        assertTrue("Reservation of DOI did not set the correct DOI status.",
                   DOIIdentifierProvider.TO_BE_RESERVED.equals(doiRow.getStatus()));
    }

    @Test
    public void testRegister_unreserved_DOI()
        throws SQLException, SQLException, AuthorizeException, IOException,
        IdentifierException, WorkflowException, IllegalAccessException {
        Item item = newItem();
        String doi = this.createDOI(item, null, true);

        provider.register(context, item, doi);

        DOI doiRow = doiService.findByDoi(context, doi.substring(DOI.SCHEME.length()));
        assumeNotNull(doiRow);

        assertTrue("Registration of DOI did not set the correct DOI status.",
                   DOIIdentifierProvider.TO_BE_REGISTERED.equals(doiRow.getStatus()));
    }

    @Test
    public void testRegister_reserved_DOI()
        throws SQLException, SQLException, AuthorizeException, IOException,
        IdentifierException, WorkflowException, IllegalAccessException {
        Item item = newItem();
        String doi = this.createDOI(item, DOIIdentifierProvider.IS_RESERVED, true);

        provider.register(context, item, doi);

        DOI doiRow = doiService.findByDoi(context, doi.substring(DOI.SCHEME.length()));
        assumeNotNull(doiRow);

        assertTrue("Registration of DOI did not set the correct DOI status.",
                   DOIIdentifierProvider.TO_BE_REGISTERED.equals(doiRow.getStatus()));
    }

    @Test
    public void testCreate_and_Register_DOI()
        throws SQLException, SQLException, AuthorizeException, IOException,
        IdentifierException, WorkflowException, IllegalAccessException {
        Item item = newItem();

        // Register, skipping the filter
        String doi = provider.register(context, item,
                DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName(
                        "always_true_filter", TrueFilter.class));

        // we want the created DOI to be returned in the following format:
        // doi:10.<prefix>/<suffix>.
        String formated_doi = doiService.formatIdentifier(doi);
        assertTrue("DOI was not in the expected format!", doi.equals(formated_doi));

        DOI doiRow = doiService.findByDoi(context, doi.substring(DOI.SCHEME.length()));
        assertNotNull("Created DOI was not stored in database.", doiRow);

        assertTrue("Registration of DOI did not set the correct DOI status.",
                   DOIIdentifierProvider.TO_BE_REGISTERED.equals(doiRow.getStatus()));
    }

    @Test
    public void testDelete_specified_DOI()
        throws SQLException, AuthorizeException, IOException, IdentifierException, WorkflowException,
        IllegalAccessException {
        Item item = newItem();
        String doi1 = this.createDOI(item, DOIIdentifierProvider.IS_REGISTERED, true);
        String doi2 = this.createDOI(item, DOIIdentifierProvider.IS_REGISTERED, true);

        // remove one of it
        context.turnOffAuthorisationSystem();
        provider.delete(context, item, doi1);
        context.restoreAuthSystemState();

        // assure that the right one was removed
        List<MetadataValue> metadata = itemService.getMetadata(item, DOIIdentifierProvider.MD_SCHEMA,
                                                               DOIIdentifierProvider.DOI_ELEMENT,
                                                               DOIIdentifierProvider.DOI_QUALIFIER,
                                                               null);
        boolean foundDOI1 = false;
        boolean foundDOI2 = false;
        for (MetadataValue id : metadata) {
            if (id.getValue().equals(doiService.DOIToExternalForm(doi1))) {
                foundDOI1 = true;
            }
            if (id.getValue().equals(doiService.DOIToExternalForm(doi2))) {
                foundDOI2 = true;
            }
        }
        assertFalse("Cannot remove DOI from item metadata.", foundDOI1);
        assertTrue("Removed wrong DOI from item metadata.", foundDOI2);

        DOI doiRow1 = doiService.findByDoi(context, doi1.substring(DOI.SCHEME.length()));
        assumeNotNull(doiRow1);
        assertTrue("Status of deleted DOI was not set correctly.",
                   DOIIdentifierProvider.TO_BE_DELETED.equals(doiRow1.getStatus()));

        DOI doiRow2 = doiService.findByDoi(context, doi2.substring(DOI.SCHEME.length()));
        assumeNotNull(doiRow2);
        assertTrue("While deleting a DOI the status of another changed.",
                   DOIIdentifierProvider.IS_REGISTERED.equals(doiRow2.getStatus()));
    }

    @Test
    public void testDelete_all_DOIs()
        throws SQLException, AuthorizeException, IOException, IdentifierException, IllegalAccessException,
        WorkflowException {
        Item item = newItem();
        String doi1 = this.createDOI(item, DOIIdentifierProvider.IS_REGISTERED, true);
        String doi2 = this.createDOI(item, DOIIdentifierProvider.IS_REGISTERED, true);

        // remove one of it
        context.turnOffAuthorisationSystem();
        provider.delete(context, item);
        context.restoreAuthSystemState();

        // assure that the right one was removed
        List<MetadataValue> metadata = itemService.getMetadata(item, DOIIdentifierProvider.MD_SCHEMA,
                                                               DOIIdentifierProvider.DOI_ELEMENT,
                                                               DOIIdentifierProvider.DOI_QUALIFIER,
                                                               null);
        boolean foundDOI1 = false;
        boolean foundDOI2 = false;
        for (MetadataValue id : metadata) {
            if (id.getValue().equals(doiService.DOIToExternalForm(doi1))) {
                foundDOI1 = true;
            }
            if (id.getValue().equals(doiService.DOIToExternalForm(doi2))) {
                foundDOI2 = true;
            }
        }
        assertFalse("Cannot remove DOI from item metadata.", foundDOI1);
        assertFalse("Did not removed all DOIs from item metadata.", foundDOI2);

        DOI doiRow1 = doiService.findByDoi(context, doi1.substring(DOI.SCHEME.length()));
        assumeNotNull(doiRow1);
        assertTrue("Status of deleted DOI was not set correctly.",
                   DOIIdentifierProvider.TO_BE_DELETED.equals(doiRow1.getStatus()));

        DOI doiRow2 = doiService.findByDoi(context, doi1.substring(DOI.SCHEME.length()));
        assumeNotNull(doiRow2);
        assertTrue("Did not set the status of all deleted DOIs as expected.",
                   DOIIdentifierProvider.TO_BE_DELETED.equals(doiRow2.getStatus()));
    }

    @Test
    public void testUpdateMetadataSkippedForPending()
            throws SQLException, AuthorizeException, IOException, IdentifierException, IllegalAccessException,
            WorkflowException  {
        context.turnOffAuthorisationSystem();
        Item item = newItem();
        // Mint a new DOI with PENDING status
        String doi1 = this.createDOI(item, DOIIdentifierProvider.PENDING, true);
        // Update metadata for the item.
        // This would normally shift status to UPDATE_REGISTERED, UPDATE_BEFORE_REGISTERING or UPDATE_RESERVED.
        // But if the DOI is just pending, it should return without changing anything.
        provider.updateMetadata(context, item, doi1);
        // Get the DOI from the service
        DOI doi = doiService.findDOIByDSpaceObject(context, item);
        // Ensure it is still PENDING
        assertEquals("Status of updated DOI did not remain PENDING",
                DOIIdentifierProvider.PENDING, doi.getStatus());
        context.restoreAuthSystemState();
    }


    @Test
    public void testMintDoiAfterOrphanedPendingDOI()
        throws SQLException, AuthorizeException, IOException, IdentifierException, IllegalAccessException,
            WorkflowException {
        context.turnOffAuthorisationSystem();
        Item item1 = newItem();
        // Mint a new DOI with PENDING status
        String doi1 = this.createDOI(item1, DOIIdentifierProvider.PENDING, true);
        // remove the item
        itemService.delete(context, item1);
        // Get the DOI from the service
        DOI doi = doiService.findDOIByDSpaceObject(context, item1);
        // ensure DOI has no state
        assertNull("Orphaned DOI was not set deleted", doi);
        // create a new item and a new DOI
        Item item2 = newItem();
        String doi2 = null;
        try {
            // get a DOI (skipping any filters)
            doi2 = provider.mint(context, item2);
        } catch (IdentifierException e) {
            e.printStackTrace(System.err);
            fail("Got an IdentifierException: " + e.getMessage());
        }

        assertNotNull("Minted DOI is null?!", doi2);
        assertFalse("Minted DOI is empty!", doi2.isEmpty());
        assertNotEquals("Minted DOI equals previously orphaned DOI.", doi1, doi2);

        try {
            doiService.formatIdentifier(doi2);
        } catch (DOIIdentifierException e) {
            e.printStackTrace(System.err);
            fail("Minted an unrecognizable DOI: " + e.getMessage());
        }

        context.restoreAuthSystemState();
    }

    @Test
    public void testUpdateMetadataSkippedForMinted()
            throws SQLException, AuthorizeException, IOException, IdentifierException, IllegalAccessException,
            WorkflowException  {
        context.turnOffAuthorisationSystem();
        Item item = newItem();
        // Mint a new DOI with MINTED status
        String doi1 = this.createDOI(item, DOIIdentifierProvider.MINTED, true);
        // Update metadata for the item.
        // This would normally shift status to UPDATE_REGISTERED, UPDATE_BEFORE_REGISTERING or UPDATE_RESERVED.
        // But if the DOI is just minted, it should return without changing anything.
        provider.updateMetadata(context, item, doi1);
        // Get the DOI from the service
        DOI doi = doiService.findDOIByDSpaceObject(context, item);
        // Ensure it is still MINTED
        assertEquals("Status of updated DOI did not remain PENDING",
                DOIIdentifierProvider.MINTED, doi.getStatus());
        context.restoreAuthSystemState();
    }

    @Test
    public void testLoadOrCreateDOIReturnsMintedStatus()
            throws SQLException, AuthorizeException, IOException, IdentifierException, IllegalAccessException,
            WorkflowException {
        Item item = newItem();
        // Mint a DOI without an explicit reserve or register context
        String mintedDoi = provider.mint(context, item, DSpaceServicesFactory.getInstance()
                .getServiceManager().getServiceByName("always_true_filter", TrueFilter.class));
        DOI doi = doiService.findByDoi(context, mintedDoi.substring(DOI.SCHEME.length()));
        // This should be minted
        assertEquals("DOI is not of 'minted' status", DOIIdentifierProvider.MINTED, doi.getStatus());
        provider.updateMetadata(context, item, mintedDoi);
        DOI secondFind = doiService.findByDoi(context, mintedDoi.substring(DOI.SCHEME.length()));
        // After an update, this should still be minted
        assertEquals("DOI is not of 'minted' status",
                DOIIdentifierProvider.MINTED, secondFind.getStatus());

    }

    // test the following methods using the MockDOIConnector.
    // updateMetadataOnline
    // registerOnline
    // reserveOnline

}
