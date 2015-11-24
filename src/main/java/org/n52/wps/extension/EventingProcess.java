package org.n52.wps.extension;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericXMLDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;

/**
 * {@linkplain IAlgorithm Algorithm} that filters a stream of observations using
 * a EML rule and pushes notifications as a RSS feed.
 *
 * @author Christian Autermann
 */
public class EventingProcess extends ConvenientAbstractAlgorithm {
    private static final Logger LOG = LoggerFactory.getLogger(EventingProcess.class);
    public static final String EML_RULE_INPUT = "notification-rule";
    public static final String KVP_GET_OBSERVATION_TEMPLATE_INPUT = "kvp-getobservation-template";
    public static final String POX_GET_OBSERVATION_TEMPLATE_INPUT = "pox-getobservation-template";
    public static final String SOS_ENDPOINT_INPUT = "sos-endpoint";
    public static final String RSS_ENDPOINT_INPUT = "notification-endpoint";
    public static final String RSS_ENDPOINT_OUTPUT = "notification-endpoint";
    public static final String SAMPLING_RATE_INPUT = "sampling-rate";
    public static final String RUNTIME_INPUT = "runtime";
    public static final long DEFAULT_RUNTIME = -1L;
    public static final long DEFAULT_SAMPLING_RATE = TimeUnit.MINUTES.toMillis(10L);
    private final HttpClient httpClient;

    /**
     * Create a new {@code EventingProcess} using the
     * {@linkplain URLConnectionHttpClient default HTTP client}.
     */
    public EventingProcess() {
        this(new URLConnectionHttpClient());
    }

    /**
     * Create a new {@code EventingProcess} using the specified
     * {@code HttpClient}.
     *
     * @param client the HTTP client to use
     */
    public EventingProcess(HttpClient client) {
        this.httpClient = client;
    }

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) throws ExceptionReport {
        try {

            // extract the inputs
            URI kvpRequest = getOptionalSingleInput(inputData, KVP_GET_OBSERVATION_TEMPLATE_INPUT, null);
            URI sosEndpoint = getOptionalSingleInput(inputData, SOS_ENDPOINT_INPUT, null);
            URI rssEndpoint = getSingleInput(inputData, RSS_ENDPOINT_INPUT);
            XmlObject poxRequest = getOptionalSingleInput(inputData, POX_GET_OBSERVATION_TEMPLATE_INPUT, null);
            XmlObject xmlRule = getSingleInput(inputData, EML_RULE_INPUT);
            long runtime = getOptionalSingleInput(inputData, RUNTIME_INPUT, DEFAULT_RUNTIME);
            long samplingRate = getOptionalSingleInput(inputData, SAMPLING_RATE_INPUT, DEFAULT_SAMPLING_RATE);


            // roughly validate the inputs
            if (kvpRequest == null && poxRequest == null) {
                throw missingRequestTemplateParameter();
            }

            if (kvpRequest != null && sosEndpoint == null) {
                // extract the endpoint from the KVP template
                sosEndpoint = new URI(kvpRequest.getScheme(),
                        kvpRequest.getUserInfo(),
                        kvpRequest.getHost(),
                        kvpRequest.getPort(),
                        kvpRequest.getPath(),
                        null, null);
            }

            if (poxRequest != null && sosEndpoint == null) {
                throw missingParameterValue(SOS_ENDPOINT_INPUT);
            }

            SosClient sosClient;
            // prefer POX over KVP...
            if (poxRequest != null) {
                sosClient = new PoxSosClient(this.httpClient, poxRequest, sosEndpoint.toURL());
            } else {
                sosClient = new KvpSosClient(this.httpClient, sosEndpoint.toURL());
            }

            Thread supervisor = Thread.currentThread();
            // SOS -> EPOS
            Thread sesFeeder = new Thread(new SesFeeder(sosClient, samplingRate));
            // EPOS -> RSS
            Thread rssFeeder = new Thread(new RssFeeder(xmlRule, rssEndpoint, this.httpClient));

            List<Throwable> uncaughtExceptions = Collections.synchronizedList(new LinkedList<>());

            UncaughtExceptionHandler eh = (t, e) -> {
                uncaughtExceptions.add(e);
                supervisor.interrupt();
            };

            rssFeeder.setUncaughtExceptionHandler(eh);
            sesFeeder.setUncaughtExceptionHandler(eh);

            rssFeeder.start();
            sesFeeder.start();

            if (runtime > 0) {
                // we have a limited live span, so sleep for
                // that time and interrupt the workers
                try {
                    try {
                        Thread.sleep(runtime);
                    } catch (InterruptedException ex) {
                        LOG.debug(Thread.currentThread() + " interrupted", ex);
                        // reset the interrupted state, the workers will be
                        // interrupted in the finally clause
                        Thread.currentThread().interrupt();
                    }
                } finally {
                    // kill the workers
                    sesFeeder.interrupt();
                    rssFeeder.interrupt();
                    try {
                        // wait till both workers are done
                        sesFeeder.join();
                        rssFeeder.join();
                    } catch (InterruptedException ex) {
                        LOG.debug(Thread.currentThread() + " interrupted", ex);
                        // reset the interrupted state
                        Thread.currentThread().interrupt();
                    }
                }
            } else {
                // we're running forever, so were just blocking
                try {
                    sesFeeder.join();
                } catch (InterruptedException ex) {
                    LOG.debug(Thread.currentThread() + " interrupted", ex);
                    // make sure the workers will also be interrupted
                    sesFeeder.interrupt();
                    rssFeeder.interrupt();
                    // reset the interrupted state
                    Thread.currentThread().interrupt();
                } finally {
                    try {
                        rssFeeder.join();
                    } catch (InterruptedException ex) {
                        LOG.debug(Thread.currentThread() + " interrupted", ex);
                        // make sure the workers will also be interrupted
                        sesFeeder.interrupt();
                        rssFeeder.interrupt();
                        // reset the interrupted state
                        Thread.currentThread().interrupt();
                    }
                }
            }

            synchronized(uncaughtExceptions) {
                if (!uncaughtExceptions.isEmpty()) {
                    Throwable throwable = uncaughtExceptions.iterator().next();
                    if (!(throwable instanceof InterruptedException)) {
                        throw unknownError(throwable);
                    }
                }
            }

            return createResultMap(rssEndpoint);
        } catch (URISyntaxException| MalformedURLException e) {
            LOG.error(e.getMessage(), e);
            throw unknownError(e);
        }
    }

    /**
     * Creates the result map of this process.
     *
     * @param rssEndpoint the RSS endpoint
     *
     * @return the result map
     */
    private Map<String, IData> createResultMap(URI rssEndpoint) {
        Map<String, IData> result = new HashMap<>(1);
        result.put(RSS_ENDPOINT_OUTPUT, new LiteralAnyURIBinding(rssEndpoint));
        return result;
    }

    @Override
    public  Class<?> getInputDataType(String id) {
        switch (id) {
            case EML_RULE_INPUT:
            case POX_GET_OBSERVATION_TEMPLATE_INPUT:
                return GenericXMLDataBinding.class;
            case KVP_GET_OBSERVATION_TEMPLATE_INPUT:
            case SOS_ENDPOINT_INPUT:
            case RSS_ENDPOINT_INPUT:
                return LiteralAnyURIBinding.class;
            case RUNTIME_INPUT:
            case SAMPLING_RATE_INPUT:
                return LiteralLongBinding.class;
            default:
                return null;
        }
    }

    @Override
    public  Class<?> getOutputDataType(String id) {
        switch (id) {
            case RSS_ENDPOINT_OUTPUT:
                return LiteralAnyURIBinding.class;
            default:
                return null;
        }
    }


    /**
     * Creates an {@code ExceptionReport} describing that either a KVP or POX
     * {@code GetObservation} template has to be supplied.
     *
     * @return the exception report
     */
    private static ExceptionReport missingRequestTemplateParameter() {
        return new ExceptionReport(String
                .format("Either %s or %s have to be provided",
                        KVP_GET_OBSERVATION_TEMPLATE_INPUT,
                        POX_GET_OBSERVATION_TEMPLATE_INPUT),
                                   ExceptionReport.MISSING_PARAMETER_VALUE);
    }
}
