package com.dircat.model;

import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a file or directory entry in the catalog.
 * Contains metadata about files/directories including name, path, size, and modification date.
 */
@XmlRootElement(name = "entry")
@XmlAccessorType(XmlAccessType.FIELD)
public class CatalogEntry {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String path;

    @XmlAttribute
    private long size;

    @XmlAttribute
    private LocalDateTime modified;

    @XmlAttribute
    private boolean isDirectory;

    @XmlElement(name = "entry")
    private List<CatalogEntry> children;

    /**
     * Default constructor for JAXB.
     */
    public CatalogEntry() {
        this.children = new ArrayList<>();
    }

    /**
     * Creates a new catalog entry.
     *
     * @param name        Name of the file or directory
     * @param path        Full path to the file or directory
     * @param size        Size in bytes (0 for directories)
     * @param modified    Last modification date
     * @param isDirectory True if this is a directory, false if it's a file
     */
    public CatalogEntry(String name, String path, long size, LocalDateTime modified, boolean isDirectory) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.modified = modified;
        this.isDirectory = isDirectory;
        this.children = new ArrayList<>();
    }

    /**
     * Adds a child entry (for directories).
     *
     * @param child Child entry to add
     */
    public void addChild(CatalogEntry child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public List<CatalogEntry> getChildren() {
        return children;
    }

    public void setChildren(List<CatalogEntry> children) {
        this.children = children;
    }

    /**
     * Calculates the total size of this entry including all children recursively.
     *
     * @return Total size in bytes
     */
    public long getTotalSize() {
        long total = size;
        if (children != null) {
            for (CatalogEntry child : children) {
                total += child.getTotalSize();
            }
        }
        return total;
    }

    /**
     * Counts total number of files in this entry and all children.
     *
     * @return Number of files
     */
    public int getFileCount() {
        int count = isDirectory ? 0 : 1;
        if (children != null) {
            for (CatalogEntry child : children) {
                count += child.getFileCount();
            }
        }
        return count;
    }

    /**
     * Counts total number of directories in this entry and all children.
     *
     * @return Number of directories
     */
    public int getDirectoryCount() {
        int count = isDirectory ? 1 : 0;
        if (children != null) {
            for (CatalogEntry child : children) {
                count += child.getDirectoryCount();
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return name + (isDirectory ? " [DIR]" : "") + " (" + formatSize(size) + ")";
    }

    /**
     * Formats size in human-readable format.
     *
     * @param bytes Size in bytes
     * @return Formatted string (e.g., "1.5 MB")
     */
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
