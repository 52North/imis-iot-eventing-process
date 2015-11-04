package org.n52.wps.extension;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.opengis.fes.x20.BinaryTemporalOpType;
import net.opengis.fes.x20.DuringDocument;
import net.opengis.gml.x32.TimePeriodDocument;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.sos.x20.GetObservationDocument;
import net.opengis.sos.x20.GetObservationResponseDocument;
import net.opengis.sos.x20.GetObservationType.TemporalFilter;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import org.n52.iceland.ogc.om.OmConstants;

/**
 * Implementation of {@code SosClient} using POX.
 *
 * @author Christian Autermann
 */
public class PoxSosClient implements SosClient {
    private static final String PHENOMENON_TIME_REFERENCE = "phenomenonTime";
    private final HttpClient client;
    private final GetObservationDocument requestTemplate;
    private final URL endpoint;

    /**
     * Create a new {@code PoxSosClient}.
     *
     * @param client          the HTTP client to use
     * @param requestTemplate the request template
     * @param endpoint        the SOS endpoint
     */
    public PoxSosClient(HttpClient client,
                        GetObservationDocument requestTemplate,
                        URL endpoint) {
        this.client = client;
        this.requestTemplate = prepareTemplate(requestTemplate);
        this.endpoint = endpoint;
    }

    /**
     * Create a new {@code PoxSosClient}.
     *
     * @param client          the HTTP client to use
     * @param requestTemplate the request template
     * @param endpoint        the SOS endpoint
     */
    public PoxSosClient(HttpClient client,
                        XmlObject requestTemplate,
                        URL endpoint) {
        this(client, (GetObservationDocument) requestTemplate, endpoint);
    }

    /**
     * Prepare {@code template} by setting required and removing conflicting
     * parameters.
     *
     * @param template the template
     *
     * @return the prepared template
     */
    private GetObservationDocument prepareTemplate(GetObservationDocument template) {
        // clear all temporal filters
        template.getGetObservation().setTemporalFilterArray(new TemporalFilter[0]);
        // be sure to get OM2
        template.getGetObservation().setResponseFormat(OmConstants.RESPONSE_FORMAT_OM_2);
        return template;
    }

    @Override
    public GetObservationResponseDocument getObservation(DateTime begin, DateTime end)
            throws XmlException, IOException {
        GetObservationDocument request = createRequest(begin, end);
        try (InputStream in = client.post(endpoint, request)) {
            return GetObservationResponseDocument.Factory.parse(in);
        }
    }

    /**
     * Create the {@code GetObservation} request from the template using the
     * specified time span.
     *
     * @param begin the exclusive lower bound of the time span
     * @param end   the exclusive upper bound of the time span
     *
     * @return the request document
     */
    private GetObservationDocument createRequest(DateTime begin, DateTime end) {
        GetObservationDocument request = (GetObservationDocument) this.requestTemplate.copy();
        DuringDocument document = DuringDocument.Factory.newInstance();
        BinaryTemporalOpType addNewDuring = document.addNewDuring();
        TimePeriodDocument document1 = TimePeriodDocument.Factory.newInstance();
        TimePeriodType period = document1.addNewTimePeriod();
        period.addNewBeginPosition().setStringValue(ISODateTimeFormat.dateTime().print(begin));
        period.addNewEndPosition().setStringValue(ISODateTimeFormat.dateTime().print(end));
        addNewDuring.set(document1);
        addNewDuring.setValueReference(PHENOMENON_TIME_REFERENCE);
        request.getGetObservation().addNewTemporalFilter().addNewTemporalOps().set(document);
        return request;
    }


}
