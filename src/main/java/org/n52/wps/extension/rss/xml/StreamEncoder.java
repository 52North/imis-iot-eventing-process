package org.n52.wps.extension.rss.xml;

import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import javanet.staxutils.IndentingXMLStreamWriter;

/**
 * Interface for XML encoders based on {@link XMLStreamWriter}.
 *
 * @param <T> the type this implementation encodes
 *
 * @author Christian Autermann
 *
 */
public interface StreamEncoder<T> {

    /**
     * Encode {@code t} using {@code writer}.
     *
     * @param writer the writer to use
     * @param t      the object to encode
     *
     * @throws XMLStreamException if the encoding fails
     */
    void encode(XMLStreamWriter writer, T t)
            throws XMLStreamException;

    /**
     * Encode {@code t} using {@code writer} as document.
     *
     * @param writer the writer to use
     * @param t      the object to encode
     *
     * @throws XMLStreamException if the encoding fails
     */
    default void encodeDocument(XMLStreamWriter writer, T t)
            throws XMLStreamException {
        writer.writeStartDocument(XmlConstants.XML_ENCODING,
                                  XmlConstants.XML_VERSION);
        encode(writer, t);
        writer.writeEndDocument();
    }

    /**
     * Add all namespaces this encode is using to {@code writer}.
     *
     * @param writer the writer
     *
     * @throws XMLStreamException if the operation fails
     */
    default void writeNamespaces(XMLStreamWriter writer)
            throws XMLStreamException {
        /* noop */
    }

    /**
     * Write the encoded {@code t} to the output stream.
     *
     * @param t   the object to encode
     * @param out the output stream (will not be closed)
     *
     * @throws XMLStreamException if the encoding fails
     */
    default void encode(T t, OutputStream out)
            throws XMLStreamException {
        XMLOutputFactory factory = XMLOutputFactory.newFactory();
        XMLStreamWriter writer = new IndentingXMLStreamWriter(
                factory.createXMLStreamWriter(out));
        encode(writer, t);
        writer.flush();
    }

    /**
     * Write the encoded {@code t} as a document to the output stream.
     *
     * @param t   the object to encode
     * @param out the output stream (will not be closed)
     *
     * @throws XMLStreamException if the encoding fails
     */
    default void encodeDocument(T t, OutputStream out)
            throws XMLStreamException {
        XMLOutputFactory factory = XMLOutputFactory.newFactory();
        XMLStreamWriter writer = new IndentingXMLStreamWriter(
                factory.createXMLStreamWriter(out));
        encodeDocument(writer, t);
        writer.flush();
    }
}
