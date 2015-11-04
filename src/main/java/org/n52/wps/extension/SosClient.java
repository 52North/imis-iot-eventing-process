package org.n52.wps.extension;

import java.io.IOException;

import net.opengis.sos.x20.GetObservationResponseDocument;

import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;

/**
 * Very simple SOS client to abstract from POX, SOAP and KVP.
 *
 * @author Christian Autermann
 */
public interface SosClient {
    /**
     * Request observations in the specified time slot (filtered using the
     * during operator).
     *
     * @param begin the exclusive lower bound of the time interval
     * @param end   the exclusive upper bound of the time interval
     *
     * @return the response document
     *
     * @throws XmlException if the response could not parsed as
     *                      {@code GetObservationResponseDocument}
     * @throws IOException  if the underlying HTTP call fails
     */
    GetObservationResponseDocument getObservation(DateTime begin, DateTime end)
            throws XmlException, IOException;
}
