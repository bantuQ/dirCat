package com.dircat.storage;

import com.dircat.model.Catalog;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Handles compressed storage of Catalog objects using GZIP compression.
 * Wraps XmlStorage to provide transparent compression/decompression.
 */
public class GzipStorage {

    private final XmlStorage xmlStorage;

    /**
     * Creates a new GzipStorage instance.
     *
     * @throws JAXBException If XML storage cannot be initialized
     */
    public GzipStorage() throws JAXBException {
        this.xmlStorage = new XmlStorage();
    }

    /**
     * Saves a Catalog to a compressed file (.cat.gz).
     *
     * @param catalog Catalog to save
     * @param file    File to save to (should have .cat.gz extension)
     * @throws IOException   If I/O error occurs
     * @throws JAXBException If serialization fails
     */
    public void save(Catalog catalog, File file) throws IOException, JAXBException {
        try (FileOutputStream fos = new FileOutputStream(file);
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
            xmlStorage.save(catalog, gzos);
        }
    }

    /**
     * Loads a Catalog from a compressed file (.cat.gz).
     *
     * @param file File to load from
     * @return Loaded Catalog
     * @throws IOException   If I/O error occurs
     * @throws JAXBException If deserialization fails
     */
    public Catalog load(File file) throws IOException, JAXBException {
        try (FileInputStream fis = new FileInputStream(file);
             GZIPInputStream gzis = new GZIPInputStream(fis)) {
            return xmlStorage.load(gzis);
        }
    }

    /**
     * Checks if a file is a valid GZIP file by reading the magic number.
     *
     * @param file File to check
     * @return True if file is GZIP compressed, false otherwise
     */
    public static boolean isGzipFile(File file) {
        if (!file.exists() || !file.isFile()) {
            return false;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] magic = new byte[2];
            if (fis.read(magic) != 2) {
                return false;
            }
            // GZIP magic number: 0x1f 0x8b
            return magic[0] == (byte) 0x1f && magic[1] == (byte) 0x8b;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Gets the recommended file extension.
     *
     * @return File extension (.cat.gz)
     */
    public static String getFileExtension() {
        return ".cat.gz";
    }
}
