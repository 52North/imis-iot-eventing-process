package org.n52.wps.extension.rss.xml;

import java.util.Objects;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.n52.wps.extension.rss.RssFeed;
import org.n52.wps.extension.rss.RssFeedItem;

/**
 * Implementation of {@link StreamEncoder} for {@linkplain RssFeed RSS feeds}.
 *
 * @author Christian Autermann
 */
public class RssFeedEncoder extends AbstractStreamEncoder<RssFeed> {

    private final StreamEncoder<? super RssFeedItem> itemEncoder;

    /**
     * The {@code StreamEncoder} to encode {@linkplain RssFeedItem items}.
     *
     * @param itemEncoder the encoder
     */
    public RssFeedEncoder(StreamEncoder<? super RssFeedItem> itemEncoder) {
        this.itemEncoder = Objects.requireNonNull(itemEncoder);
    }

    @Override
    public void encode(XMLStreamWriter writer, RssFeed feed)
            throws XMLStreamException {
        startElement(writer, XmlConstants.QN_RSS);
        writer.writeAttribute(XmlConstants.AN_VERSION, XmlConstants.RSS_VERSION);
        writeNamespaces(writer);
        this.itemEncoder.writeNamespaces(writer);
        writeChannel(writer, feed);
        writer.writeEndElement();
    }

    /**
     * Write the {@code <channel>} tag.
     *
     * @param writer the writer to use
     * @param feed   the feed to write
     *
     * @throws XMLStreamException if the encoding fails
     */
    private void writeChannel(XMLStreamWriter writer, RssFeed feed)
            throws XMLStreamException {
        startElement(writer, XmlConstants.QN_CHANNEL);
        writeSimpleElement(writer, XmlConstants.QN_TITLE, feed.getTitle());
        writeSimpleElement(writer, XmlConstants.QN_LINK, feed.getLink().toString());
        writeSimpleElement(writer, XmlConstants.QN_DESCRIPTION, feed.getDescription());
        writeSimpleElement(writer, XmlConstants.QN_PUB_DATE, XmlConstants.RFC_1123_DATE_TIME_FORMAT.print(feed.getDate()));
        for (RssFeedItem feedItem : feed.getItems()) {
            this.itemEncoder.encode(writer, feedItem);
        }
        writer.writeEndElement();
    }

}
