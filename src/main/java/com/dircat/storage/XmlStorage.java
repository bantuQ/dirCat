package com.dircat.storage;

import com.dircat.model.Catalog;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;

/**
 * Handles XML serialization and deserialization of Catalog objects using JAXB.
 */
public class XmlStorage {

    private final JAXBContext jaxbContext;

    /**
     * Creates a new XmlStorage instance.
     *
     * @throws JAXBException If JAXB context cannot be created
     */
    public XmlStorage() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(Catalog.class);
    }

    /**
     * Serializes a Catalog to XML and writes it to an OutputStream.
     *
     * @param catalog      Catalog to serialize
     * @param outputStream Output stream to write to
     * @throws JAXBException If serialization fails
     */
    public void save(Catalog catalog, OutputStream outputStream) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(catalog, outputStream);
    }

    /**
     * Serializes a Catalog to XML and writes it to a File.
     *
     * @param catalog Catalog to serialize
     * @param file    File to write to
     * @throws JAXBException       If serialization fails
     * @throws FileNotFoundException If file cannot be created
     */
    public void save(Catalog catalog, File file) throws JAXBException, FileNotFoundException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            save(catalog, fos);
        } catch (IOException e) {
            throw new RuntimeException("Error closing file", e);
        }
    }

    /**
     * Deserializes a Catalog from an XML InputStream.
     *
     * @param inputStream Input stream to read from
     * @return Deserialized Catalog
     * @throws JAXBException If deserialization fails
     */
    public Catalog load(InputStream inputStream) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (Catalog) unmarshaller.unmarshal(inputStream);
    }

    /**
     * Deserializes a Catalog from an XML File.
     *
     * @param file File to read from
     * @return Deserialized Catalog
     * @throws JAXBException         If deserialization fails
     * @throws FileNotFoundException If file does not exist
     */
    public Catalog load(File file) throws JAXBException, FileNotFoundException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Error closing file", e);
        }
    }

    /**
     * Converts a Catalog to XML string.
     *
     * @param catalog Catalog to convert
     * @return XML string representation
     * @throws JAXBException If serialization fails
     */
    public String toXml(Catalog catalog) throws JAXBException {
        try (StringWriter writer = new StringWriter()) {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(catalog, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error converting to XML", e);
        }
    }

    /**
     * Parses a Catalog from XML string.
     *
     * @param xml XML string to parse
     * @return Parsed Catalog
     * @throws JAXBException If parsing fails
     */
    public Catalog fromXml(String xml) throws JAXBException {
        try (StringReader reader = new StringReader(xml)) {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (Catalog) unmarshaller.unmarshal(reader);
        }
    }
}
