package com.dircat.model;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Root catalog model containing metadata and all catalog entries.
 * Represents a complete disk catalog that can be serialized to XML.
 */
@XmlRootElement(name = "catalog")
@XmlAccessorType(XmlAccessType.FIELD)
public class Catalog {

    @XmlAttribute
    private String name;

    @XmlAttribute
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime created;

    @XmlElement(name = "entry")
    private List<CatalogEntry> entries;

    /**
     * Default constructor for JAXB.
     */
    public Catalog() {
        this.entries = new ArrayList<>();
        this.created = LocalDateTime.now();
    }

    /**
     * Creates a new catalog with the specified name.
     *
     * @param name Name of the catalog
     */
    public Catalog(String name) {
        this.name = name;
        this.created = LocalDateTime.now();
        this.entries = new ArrayList<>();
    }

    /**
     * Adds an entry to the catalog.
     *
     * @param entry Entry to add
     */
    public void addEntry(CatalogEntry entry) {
        if (entries == null) {
            entries = new ArrayList<>();
        }
        entries.add(entry);
    }

    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public List<CatalogEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<CatalogEntry> entries) {
        this.entries = entries;
    }

    /**
     * Calculates total size of all entries in the catalog.
     *
     * @return Total size in bytes
     */
    public long getTotalSize() {
        long total = 0;
        if (entries != null) {
            for (CatalogEntry entry : entries) {
                total += entry.getTotalSize();
            }
        }
        return total;
    }

    /**
     * Counts total number of files in the catalog.
     *
     * @return Number of files
     */
    public int getTotalFileCount() {
        int count = 0;
        if (entries != null) {
            for (CatalogEntry entry : entries) {
                count += entry.getFileCount();
            }
        }
        return count;
    }

    /**
     * Counts total number of directories in the catalog.
     *
     * @return Number of directories
     */
    public int getTotalDirectoryCount() {
        int count = 0;
        if (entries != null) {
            for (CatalogEntry entry : entries) {
                count += entry.getDirectoryCount();
            }
        }
        return count;
    }
}
