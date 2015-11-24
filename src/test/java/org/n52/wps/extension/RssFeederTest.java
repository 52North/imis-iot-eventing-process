package org.n52.wps.extension;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.MockitoAnnotations;

import org.n52.epos.event.EposEvent;
import org.n52.epos.event.MapEposEvent;
import org.n52.epos.filter.FilterInstantiationException;

import com.google.common.io.CharStreams;

/**
 * @author Christian Autermann
 */
public class RssFeederTest {
    private URI endpoint;
    private URLConnectionHttpClient httpClient;
    private EposEvent event;
    private RssFeeder feeder;
    public final @Rule ErrorCollector errors = new ErrorCollector();

    @Before
    public void setup()
            throws MalformedURLException,
                   FilterInstantiationException,
                   URISyntaxException,
                   IOException,
                   XmlException {
        MockitoAnnotations.initMocks(this);
        this.endpoint = new URI("http://115.146.93.218:8080/iddss-service");
        this.httpClient = new URLConnectionHttpClient();
        this.feeder = new RssFeeder(getXmlResource("/OvershootUndershootRule.xml"), this.endpoint, this.httpClient);
        long end = DateTime.now().getMillis();
        long start = end - 5;
        this.event = new MapEposEvent(start, end);
        this.event.addCausalAncestor(createEvent(start, 0.0d));
        this.event.addCausalAncestor(createEvent(end, 20.0d));
    }

    private MapEposEvent createEvent(long time, double value) {
        MapEposEvent e = new MapEposEvent(time, time);
        e.setValue(MapEposEvent.FEATURE_TYPE_KEY, "feature");
        e.setValue(MapEposEvent.SENSORID_KEY, "procedure");
        e.setValue(MapEposEvent.OBSERVED_PROPERTY_KEY, "observedProperty");
        e.setValue(MapEposEvent.DOUBLE_VALUE_KEY, value);
        return e;
    }

    @Test
    public void testPost()
            throws MalformedURLException, IOException, XMLStreamException {
        String guid = this.feeder.createGUID(event).toString();
        this.feeder.sendNotification(this.event);
        URL url = new URL(this.endpoint + "/GetRSS?id=" + URIUtil
                          .encodeWithinQuery(guid));
        String response;
        try (InputStream in = this.httpClient.get(url);
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            response = CharStreams.toString(reader);
        }
        assertThat(response, is(notNullValue()));
        errors.checkThat(response.length(), is(not(0)));
        errors.checkThat(response, is(not("guid " + guid +
                                          " does not exist in the data store.")));
    }

    private static XmlObject getXmlResource(String name)
            throws IOException, XmlException {
        try (InputStream stream = getResource(name)) {
            return XmlObject.Factory.parse(stream);
        }
    }

    private static InputStream getResource(String name)
            throws IOException {
        InputStream stream = EventingProcessTest.class.getResourceAsStream(name);
        return Optional.ofNullable(stream).orElseThrow(() -> new IOException());
    }

}
