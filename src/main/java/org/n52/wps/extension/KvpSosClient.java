package org.n52.wps.extension;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import net.opengis.sos.x20.GetObservationResponseDocument;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Implementation of {@code SosClient} using KVP.
 *
 * @author Christian Autermann
 */
public class KvpSosClient implements SosClient {

    private final HttpClient client;
    private final URL requestTemplate;

    /**
     * Create a new {@code KvpSosClient}.
     * @param client the underlying HTTP client
     * @param requestTemplate the request template
     */
    public KvpSosClient(HttpClient client, URL requestTemplate) {
        this.client = Objects.requireNonNull(client);
        this.requestTemplate = Objects.requireNonNull(requestTemplate);
    }

    @Override
    public GetObservationResponseDocument getObservations(DateTime begin,
                                                         DateTime end)
            throws XmlException, IOException {
        try {
            URL url = createRequest(begin, end);
            try (InputStream in = this.client.get(url)) {
                return GetObservationResponseDocument.Factory.parse(in);
            }
        } catch (URIException | MalformedURLException ex) {
            throw new Error(ex);
        }
    }

    /**
     * Creates the request URL for the specified time span.
     *
     * @param begin the exclusive lower bound of the time span
     * @param end   the exclusive upper bound of the time span
     *
     * @return the request URL
     *
     * @throws MalformedURLException if the URL generation fails
     * @throws URIException          if the URL generation fails
     */
    private URL createRequest(DateTime begin, DateTime end)
            throws MalformedURLException, URIException {
        Objects.requireNonNull(begin);
        Objects.requireNonNull(end);
        StringBuilder builder = new StringBuilder();
        builder.append(this.requestTemplate.toString());
        builder.append("&namespaces=").append(URIUtil.encodeQuery("xmlns(om:http://www.opengis.net/om/2.0)"));
        builder.append("&temporalFilter=");
        String temporalFilter = "om:phenomenonTime/" +
                                ISODateTimeFormat.dateTime().print(begin) +
                                "/" +
                                ISODateTimeFormat.dateTime().print(end);
        builder.append(URIUtil.encodeQuery(temporalFilter));
        URL url = new URL(builder.toString());
        return url;
    }

}
