package org.n52.wps.extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.xmlbeans.XmlObject;

/**
 * Implementation of {@code HttpClient} using
 * {@linkplain URL#openConnection() URL connections}.
 *
 * @author Christian Autermann
 */
public class URLConnectionHttpClient implements HttpClient {

    @Override
    public InputStream post(URL url, XmlObject request)
            throws ProtocolException, IOException {
        byte[] bytes = request.xmlText().getBytes(StandardCharsets.UTF_8);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setRequestProperty("Accept", "application/xml");
        conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));

        try (OutputStream outputStream = conn.getOutputStream()) {
            outputStream.write(bytes);
            outputStream.flush();
        }
        return conn.getInputStream();
    }

    @Override
    public OutputStream post(URL url, String contentType)
            throws ProtocolException, IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestMethod("POST");
        return conn.getOutputStream();
    }

    @Override
    public InputStream get(URL url)
            throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml");
        return conn.getInputStream();
    }

}
