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

import javax.xml.stream.XMLStreamException;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.n52.epos.event.EposEvent;
import org.n52.epos.filter.FilterInstantiationException;

import com.google.common.io.CharStreams;

/**
 * @author Christian Autermann
 */
public class RssFeederTest {
    private URI endpoint;
    private URLConnectionHttpClient httpClient;
    private @Mock EposEvent event;
    private @Mock XmlObject rule;
    private RssFeeder feeder;
    public final @Rule ErrorCollector errors = new ErrorCollector();

    @Before
    public void setup()
            throws MalformedURLException,
                   FilterInstantiationException,
                   URISyntaxException {
        MockitoAnnotations.initMocks(this);
        this.endpoint = new URI("http://115.146.93.218:8080/iddss-service");
        this.httpClient = new URLConnectionHttpClient();
        this.feeder = new RssFeeder(this.rule, this.endpoint, this.httpClient);
    }


    @Test
    public void testPost() throws MalformedURLException, IOException, XMLStreamException {
        Mockito.when(event.getStartTime()).thenReturn(DateTime.now().getMillis());

        String guid = this.feeder.createGUID(event).toString();

        this.feeder.sendNotification(this.event);

        URL url = new URL(this.endpoint + "/GetRSS?id=" + URIUtil.encodeWithinQuery(guid));

        String response;
        try (InputStream in = this.httpClient.get(url);
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            response = CharStreams.toString(reader);
        }

        assertThat(response, is(notNullValue()));

        errors.checkThat(response.length(), is(not(0)));
        errors.checkThat(response, is(not("guid " + guid + " does not exist in the data store.")));
    }

}
