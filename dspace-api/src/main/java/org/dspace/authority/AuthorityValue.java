/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority;

import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.Context;
import org.dspace.util.SolrUtils;

/**
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class AuthorityValue {
    /**
     * The id of the record in solr
     */
    private String id;

    /**
     * The metadata field that this authority value is for
     */
    private String field;

    /**
     * The text value of this authority
     */
    private String value;

    /**
     * When this authority record has been created
     */
    private Instant creationDate;

    /**
     * If this authority has been removed
     */
    private boolean deleted;

    /**
     * represents the last time that DSpace got updated information from its external source
     */
    private Instant lastModified;

    public AuthorityValue() {
    }

    public AuthorityValue(SolrDocument document) {
        setValues(document);
    }

    public String getId() {
        return id;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = stringToDate(creationDate);
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = stringToDate(lastModified);
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    protected void updateLastModifiedDate() {
        this.lastModified = Instant.now();
    }

    public void update() {
        updateLastModifiedDate();
    }

    public void delete() {
        setDeleted(true);
        updateLastModifiedDate();
    }

    /**
     * Generate a solr record from this instance
     *
     * @return SolrInputDocument
     */
    public SolrInputDocument getSolrInputDocument() {

        SolrInputDocument doc = new SolrInputDocument();
        DateTimeFormatter solrDateFormatter = SolrUtils.getDateFormatter();
        doc.addField("id", getId());
        doc.addField("field", getField());
        doc.addField("value", getValue());
        doc.addField("deleted", isDeleted());
        doc.addField("creation_date", solrDateFormatter.format(getCreationDate()));
        doc.addField("last_modified_date", solrDateFormatter.format(getLastModified()));
        doc.addField("authority_type", getAuthorityType());
        return doc;
    }

    /**
     * Initialize this instance based on a solr record
     *
     * @param document SolrDocument
     */
    public void setValues(SolrDocument document) {
        this.id = String.valueOf(document.getFieldValue("id"));
        this.field = String.valueOf(document.getFieldValue("field"));
        this.value = String.valueOf(document.getFieldValue("value"));
        this.deleted = (Boolean) document.getFieldValue("deleted");
        this.creationDate = ((java.util.Date) document.getFieldValue("creation_date")).toInstant();
        this.lastModified = ((java.util.Date) document.getFieldValue("last_modified_date")).toInstant();
    }

    /**
     * Replace an item's DCValue with this authority
     *
     * @param context     context
     * @param value       metadata value
     * @param currentItem item
     * @throws SQLException       if database error
     * @throws AuthorizeException if authorization error
     */
    public void updateItem(Context context, Item currentItem, MetadataValue value)
        throws SQLException, AuthorizeException {
        value.setValue(getValue());
        value.setAuthority(getId());
        ContentServiceFactory.getInstance().getMetadataValueService().update(context, value, true);
    }

    /**
     * Information that can be used the choice ui.
     *
     * @return map
     */
    public Map<String, String> choiceSelectMap() {
        return new HashMap<>();
    }

    /**
     * Build a list of ISO date formatters to parse various forms.
     *
     * <p><strong>Note:</strong>  any formatter which does not parse a zone or
     * offset must have a default zone set.  See {@link #stringToDate(String)}.
     *
     * @return the formatters.
     */
    static private List<DateTimeFormatter> getDateFormatters() {
        List<java.time.format.DateTimeFormatter> list = new ArrayList<>();
        list.add(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]X"));
        list.add(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
                .withZone(ZoneId.systemDefault().normalized()));
        return list;
    }

    /**
     * Convert a date string to internal form, trying several parsers.
     *
     * @param date serialized date to be converted.
     * @return converted date, or null if no parser accepted the input.
     */
    static public Instant stringToDate(String date) {
        Instant result = null;
        if (StringUtils.isNotBlank(date)) {
            for (DateTimeFormatter formatter : getDateFormatters()) {
                try {
                    ZonedDateTime dateTime = ZonedDateTime.parse(date, formatter);
                    result = dateTime.toInstant();
                    break;
                } catch (DateTimeException e) {
                    log.debug("Input '{}' did not match {}", date, formatter);
                }
            }
        }
        if (null == result) {
            log.error("Could not find a valid date format for: \"{}\"", date);
        }
        return result;
    }

    /**
     * log4j logger
     */
    private static Logger log = LogManager.getLogger();

    @Override
    public String toString() {
        return "AuthorityValue{" +
            "id='" + id + '\'' +
            ", field='" + field + '\'' +
            ", value='" + value + '\'' +
            ", creationDate=" + creationDate +
            ", deleted=" + deleted +
            ", lastModified=" + lastModified +
            '}';
    }

    /**
     * Provides a string that will allow this AuthorityType to be recognized and
     * provides information to create a new instance to be created using {@link #newInstance(String)}.
     * See the implementation of
     * {@link org.dspace.authority.AuthorityValueServiceImpl#generateRaw(java.lang.String, java.lang.String,
     * java.lang.String)} for more details.
     *
     * @return see {@link org.dspace.authority.service.AuthorityValueService#GENERATE AuthorityValueService.GENERATE}
     */
    public String generateString() {
        return AuthorityValueServiceImpl.GENERATE;
    }

    /**
     * Makes an instance of the AuthorityValue with the given information.
     *
     * @param info string info
     * @return AuthorityValue
     */
    public AuthorityValue newInstance(String info) {
        return new AuthorityValue();
    }

    /**
     * Get the type of authority which created this value.
     * @return type name.
     */
    public String getAuthorityType() {
        return "internal";
    }

    /**
     * The regular equals() only checks if both AuthorityValues describe the same authority.
     * This method checks if the AuthorityValues have different information
     * E.g. it is used to decide when lastModified should be updated.
     *
     * @param o object
     * @return true or false
     */
    public boolean hasTheSameInformationAs(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AuthorityValue that = (AuthorityValue) o;

        if (deleted != that.deleted) {
            return false;
        }
        if (field != null ? !field.equals(that.field) : that.field != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }
}
