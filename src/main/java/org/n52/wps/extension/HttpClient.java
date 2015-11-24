package org.n52.wps.extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * Mockable HTTP client to allow better testing.
 *
 * @author Christian Autermann
 */
public interface HttpClient {
    /**
     * Supply {@code request} to {@code url} using the content type
     * {@code application/xml} and return the response as a stream
     *
     * @param url     the endpoint
     * @param request the XML request
     *
     * @return the response stream
     *
     * @throws IOException  if the HTTP call fails
     * @throws XmlException if the request can not be serialized
     */
    InputStream post(URL url, XmlObject request)
            throws IOException, XmlException;

    /**
     * Request {@code url} using HTTP {@code GET} and return the response as a
     * stream.
     *
     * @param url the request
     *
     * @return the response stream
     *
     * @throws IOException if the HTTP call fails
     */
    InputStream get(URL url)
            throws IOException;

    /**
     * Initiate a HTTP {@code POST} to endpoint using the specified content type
     * and return a stream to supply the request.
     *
     * @param url         the endpoint
     * @param contentType the content type
     *
     * @return a stream to supply the request to
     *
     * @throws IOException if the HTTP call fails
     */
    OutputStream post(URL url, String contentType)
            throws IOException;

    /**
     * Initiate a HTTP {@code POST} to endpoint using the specified content type
     * and send the specified request.
     *
     * @param url         the endpoint
     * @param contentType the content type
     * @param request     the request
     *
     * @return the response stream
     *
     * @throws IOException if the HTTP call fails
     */
    InputStream post(URL url, String contentType, String request)
            throws IOException;
}
