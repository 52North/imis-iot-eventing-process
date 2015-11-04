package org.n52.wps.extension.rss.xml;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import org.n52.wps.extension.rss.NotificationRssFeedItem;
import org.n52.wps.extension.rss.RssFeed;

import javanet.staxutils.IndentingXMLStreamWriter;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class RssFeedEncoderTest {

    private RssFeedEncoder encoder;

    @Before
    public void setup() {
        this.encoder = new RssFeedEncoder(new NotificationRssFeedItemEncoder());
    }

    @Test
    public void test_encodeDocument()
            throws MalformedURLException, XMLStreamException {

        RssFeed feed = new RssFeed("SOS-Event WPS feeder", new URL("http://iddss-sensor.cdmps.org.au:8080/rss"), "SOS-Event WPS feeder - alert updates", DateTime.now());

        feed.addItem(new NotificationRssFeedItem("itemTitle", new URL("http://localhost/feed/item"), "category", "description",
                DateTime.now(), "guid", "procedure", "observedProperty", "featureOfInterest", 4.0, 6.0, 3.0));

        XMLOutputFactory factory = XMLOutputFactory.newFactory();
        XMLStreamWriter writer = factory.createXMLStreamWriter(System.out);
        writer = new IndentingXMLStreamWriter(writer);
        this.encoder.encode(writer, feed);
        writer.flush();
        writer.close();
        System.out.println();

    }

}
