package org.n52.wps.extension.rss.xml;

import java.text.NumberFormat;
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Abstract implementation of {@code StreamEncoder}.
 *
 * @param <T> the type this implementation encodes
 *
 * @author Christian Autermann
 *
 */
public abstract class AbstractStreamEncoder<T> implements StreamEncoder<T> {

    /**
     * Starts a new element choosing the right method depending on the contents
     * of {@code name}.
     *
     * @param writer the writer
     * @param name   the name
     *
     * @throws XMLStreamException if the call to {@code writeStartElement()}
     *                            fails
     * @see XMLStreamWriter#writeStartElement(java.lang.String)
     * @see XMLStreamWriter#writeStartElement(java.lang.String,
     * java.lang.String)
     * @see XMLStreamWriter#writeStartElement(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    protected void startElement(XMLStreamWriter writer, QName name)
            throws XMLStreamException {
        String prefix = name.getPrefix();
        String namespaceURI = name.getNamespaceURI();
        String localPart = name.getLocalPart();

        if (namespaceURI != null && !namespaceURI.isEmpty()) {
            if (prefix != null && !prefix.isEmpty()) {
                writer.writeStartElement(prefix, localPart, namespaceURI);
            } else {
                writer.writeStartElement(namespaceURI, localPart);
            }
        } else {
            writer.writeStartElement(localPart);
        }
    }

    /**
     * Writes an element with the specified {@code name} and {@code content}.
     *
     * @param writer  the writer
     * @param name    the name
     * @param content the content
     *
     * @throws XMLStreamException if the element could not be written
     */
    protected void writeSimpleElement(XMLStreamWriter writer, QName name,
                                      String content)
            throws XMLStreamException {
        startElement(writer, name);
        writer.writeCharacters(content);
        writer.writeEndElement();
    }

    /**
     * Writes an element with the specified {@code name} and {@code value}.
     *
     * @param writer the writer
     * @param name   the name
     * @param value  the value
     *
     * @throws XMLStreamException if the element could not be written
     */
    protected void writeSimpleElement(XMLStreamWriter writer, QName name,
                                      double value)
            throws XMLStreamException {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        writeSimpleElement(writer, name, format.format(value));
    }

}
