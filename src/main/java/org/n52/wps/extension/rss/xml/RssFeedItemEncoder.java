package org.n52.wps.extension.rss.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.n52.wps.extension.rss.RssFeedItem;

/**
 * {@link StreamEncoder} for {@linkplain RssFeedItem RSS feed items}.
 *
 * @author Christian Autermann
 */
public class RssFeedItemEncoder extends AbstractStreamEncoder<RssFeedItem> {

    @Override
    public void encode(XMLStreamWriter writer, RssFeedItem feedItem) throws XMLStreamException {
        startElement(writer, XmlConstants.QN_ITEM);
        writeSimpleElement(writer, XmlConstants.QN_TITLE, feedItem.getTitle());
        writeSimpleElement(writer, XmlConstants.QN_LINK, feedItem.getLink().toString());
        writeSimpleElement(writer, XmlConstants.QN_CATEGORY, feedItem.getCategory());
        writeSimpleElement(writer, XmlConstants.QN_DESCRIPTION, feedItem.getDescription());
        writeSimpleElement(writer, XmlConstants.QN_PUB_DATE, XmlConstants.RFC_1123_DATE_TIME_FORMAT.print(feedItem.getDate()));
        writeSimpleElement(writer, XmlConstants.QN_GUID, feedItem.getGuid());
        writeAdditionalElements(writer, feedItem);
        writer.writeEndElement();
    }

    /**
     * Extension point for specialized writers. Will be called after all base
     * tags are written.
     *
     * @param writer   the writer to use
     * @param feedItem the item to encode
     *
     * @throws XMLStreamException if the encoding fails
     */
    protected void writeAdditionalElements(XMLStreamWriter writer, RssFeedItem feedItem) throws XMLStreamException { /* noop */ }

}
