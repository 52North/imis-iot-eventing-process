package org.n52.wps.extension.rss.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.n52.wps.extension.rss.NotificationRssFeedItem;
import org.n52.wps.extension.rss.RssFeedItem;

/**
 * {@code RssFeedItemEncoder} for {@link NotificationRssFeedItem}s.
 *
 * @author Christian Autermann
 */
public class NotificationRssFeedItemEncoder extends RssFeedItemEncoder {

    @Override
    protected void writeAdditionalElements(XMLStreamWriter writer, RssFeedItem feedItem) throws XMLStreamException {
        if (!(feedItem instanceof NotificationRssFeedItem)) { return; }
        NotificationRssFeedItem nFeedItem = (NotificationRssFeedItem) feedItem;
        writeSimpleElement(writer, XmlConstants.QN_PROCEDURE, nFeedItem.getProcedure());
        writeSimpleElement(writer, XmlConstants.QN_OBSERVED_PROPERTY, nFeedItem.getObservedProperty());
        writeSimpleElement(writer, XmlConstants.QN_FEATURE_OF_INTEREST, nFeedItem.getFeatureOfInterest());
        writeSimpleElement(writer, XmlConstants.QN_UNDERSHOOT, nFeedItem.getUndershoot());
        writeSimpleElement(writer, XmlConstants.QN_OVERSHOOT, nFeedItem.getOvershoot());
        writeSimpleElement(writer, XmlConstants.QN_VALUE, nFeedItem.getValue());
    }

    @Override
    public void writeNamespaces(XMLStreamWriter writer) throws XMLStreamException {
        writer.setPrefix(XmlConstants.NS_PREFIX, XmlConstants.NS_URI);
        writer.writeNamespace(XmlConstants.NS_PREFIX, XmlConstants.NS_URI);
    }
}
