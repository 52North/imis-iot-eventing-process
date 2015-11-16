package org.n52.wps.extension;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericXMLDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.server.ExceptionReport;


/**
 * @author Christian Autermann
 */
public class EventingProcessTest {

    private static final Answer<InputStream> EMPTY = x -> getResource("/EmptyGetObservationResponse.xml");
    private static final Answer<InputStream> UNDERSHOOT = x -> getResource("/UndershootObservation.xml");
    private static final Answer<InputStream> OVERSHOOT = x -> getResource("/OvershootObservation.xml");
    private static final long POLLING_TIME = 1000L;
    private static final long RUNTIME = 5000L;
    private @Mock HttpClient client;
    private Map<String, List<IData>> input;
    private EventingProcess eventingProcess;

    @Before
    public void setup() throws IOException, XmlException {
        MockitoAnnotations.initMocks(this);
        this.eventingProcess = new EventingProcess(this.client);
        this.input = new HashMap<>(7);
        this.input.put(EventingProcess.SAMPLING_RATE_INPUT, createLongInput(POLLING_TIME));
        this.input.put(EventingProcess.RUNTIME_INPUT, createLongInput(RUNTIME));
        this.input.put(EventingProcess.RSS_ENDPOINT_INPUT, createURIInput("http://localhost/rss"));
        this.input.put(EventingProcess.SOS_ENDPOINT_INPUT, createURIInput("http://localhost/sos"));
        this.input.put(EventingProcess.POX_GET_OBSERVATION_TEMPLATE_INPUT, createXmlInput("/GetObservationRequestTemplate.xml"));
        this.input.put(EventingProcess.EML_RULE_INPUT, createXmlInput("/OvershootUndershootRule.xml"));
    }

    @Test
    public void test_undershoot_overshoot() throws XmlException, IOException, ExceptionReport {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Mockito.when(client.post(any(URL.class), any(XmlObject.class)))
                .thenAnswer(UNDERSHOOT)
                .thenAnswer(OVERSHOOT)
                .thenAnswer(EMPTY);
        Mockito.when(client.post(any(URL.class), any(String.class)))
                .thenReturn(baos);
        this.eventingProcess.run(this.input);
        assertThat(baos.size(), is(greaterThan(0)));
    }

    @Test
    public void test_overshoot_undershoot() throws XmlException, IOException, ExceptionReport {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Mockito.when(client.post(any(URL.class), any(XmlObject.class)))
                .thenAnswer(OVERSHOOT)
                .thenAnswer(UNDERSHOOT)
                .thenAnswer(EMPTY);
        Mockito.when(client.post(any(URL.class), any(String.class)))
                .thenReturn(baos);
        this.eventingProcess.run(this.input);
        assertThat(baos.size(), is(greaterThan(0)));
    }

    private List<IData> createXmlInput(String name) throws IOException, XmlException {
        try (InputStream stream = getResource(name)) {
            XmlObject xml = XmlObject.Factory.parse(stream);
            GenericXMLDataBinding binding = new GenericXMLDataBinding(xml);
            return Collections.singletonList(binding);
        }
    }

    private static InputStream getResource(String name) throws IOException {
        InputStream stream = EventingProcessTest.class.getResourceAsStream(name);
        return Optional.ofNullable(stream).orElseThrow(() -> new IOException());
    }

    private static List<IData> createLongInput(long value) {
        return Collections.singletonList(new LiteralLongBinding(value));
    }

    private static List<IData> createURIInput(String uri) {
        return Collections.singletonList(new LiteralAnyURIBinding(URI.create(uri)));
    }
}
