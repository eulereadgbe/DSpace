package org.dspace.identifier;

import org.apache.commons.lang3.StringUtils;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.handle.service.HandleService;
import org.dspace.identifier.factory.IdentifierServiceFactory;
import org.dspace.identifier.service.IdentifierService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ForeignHandleIdentifierProvider extends IdentifierProvider {
    private static final Logger log = LogManager.getLogger(ForeignHandleIdentifierProvider.class);

    private final ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();

    @Autowired
    private HandleService handleService;

    private String getForeignPrefix() {
        // Defaults to "20.500.12174/" if not configured
        return configurationService.getProperty("identifier.foreign.prefix", "20.500.12174/");
    }

    @Override
    public boolean supports(Class<? extends Identifier> identifier) {
        return Handle.class.isAssignableFrom(identifier);
    }

    @Override
    public boolean supports(String identifier) {
        return identifier != null && identifier.startsWith(getForeignPrefix());
    }

    public ForeignHandleIdentifierProvider() {
        log.info("ForeignHandleIdentifierProvider loaded and initialized.");
    }

    @Override
    public String register(Context context, DSpaceObject dso) throws IdentifierException {
        log.info("register() called for DSO ID: {}", dso.getID());

        try {
            String currentHandle = dso.getHandle();
            log.info("Current handle on DSO: {}", currentHandle);

            if (StringUtils.isNotBlank(currentHandle) && supports(currentHandle)) {
                log.info("Using pre-existing foreign handle: {}", currentHandle);
                handleService.createHandle(context, dso, currentHandle);
                return currentHandle;
            }

            IdentifierService identifierService = IdentifierServiceFactory.getInstance().getIdentifierService();
            List<String> identifiers = identifierService.lookup(context, dso);
            log.info("Identifiers found via lookup: {}", identifiers);

            for (String id : identifiers) {
                if (supports(id)) {
                    log.info("Matched foreign handle: {}", id);
                    handleService.createHandle(context, dso, id);
                    return id;
                }
            }

            // No foreign handle found — just skip so another provider can handle it
            log.info("No matching foreign handle found; deferring to other providers.");
            return null;

        } catch (Exception e) {
            log.error("Exception in register()", e);
            throw new IdentifierException("Error registering foreign handle", e);
        }
    }

    @Override
    public void reserve(Context context, DSpaceObject dso, String identifier) throws IdentifierException {
        if (supports(identifier)) {
            try {
                handleService.createHandle(context, dso, identifier);
            } catch (Exception e) {
                throw new IdentifierException("Error reserving foreign handle", e);
            }
        }
    }

    @Override
    public void register(Context context, DSpaceObject dso, String identifier) throws IdentifierException {
        if (supports(identifier)) {
            try {
                handleService.createHandle(context, dso, identifier);
            } catch (Exception e) {
                throw new IdentifierException("Error explicitly registering foreign handle", e);
            }
        }
    }

    @Override
    public void delete(Context context, DSpaceObject dso) throws IdentifierException {
        // No-op
    }

    @Override
    public void delete(Context context, DSpaceObject dso, String identifier) throws IdentifierException {
        // No-op
    }

    public boolean isReserved(Context context, DSpaceObject dso, String identifier) throws IdentifierException {
        return false;
    }

    public boolean isRegistered(Context context, DSpaceObject dso, String identifier) throws IdentifierException {
        return StringUtils.equals(dso.getHandle(), identifier);
    }

    @Override
    public DSpaceObject resolve(Context context, String identifier, String... attributes) {
        // This provider does not resolve identifiers
        return null;
    }

    @Override
    public String lookup(Context context, DSpaceObject dso) {
        return null;
    }

    @Override
    public String mint(Context context, DSpaceObject dso) throws IdentifierException {
        // Do not mint new handles
        return null;
    }
}
