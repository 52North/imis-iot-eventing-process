package org.n52.wps.extension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.stream.XMLStreamException;

import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.n52.epos.engine.EposEngine;
import org.n52.epos.engine.rules.RuleInstance;
import org.n52.epos.event.EposEvent;
import org.n52.epos.filter.EposFilter;
import org.n52.epos.filter.FilterInstantiationException;
import org.n52.epos.filter.FilterInstantiationRepository;
import org.n52.epos.filter.PassiveFilter;
import org.n52.epos.rules.PassiveFilterAlreadyPresentException;
import org.n52.epos.rules.Rule;
import org.n52.wps.extension.rss.NotificationRssFeedItem;
import org.n52.wps.extension.rss.RssFeed;
import org.n52.wps.extension.rss.RssFeedItem;
import org.n52.wps.extension.rss.xml.NotificationRssFeedItemEncoder;
import org.n52.wps.extension.rss.xml.RssFeedEncoder;
import org.n52.wps.extension.rss.xml.StreamEncoder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.CharStreams;


/**
 * Component that listens on events produced by the EPOS engine.
 *
 * @author Christian Autermann
 */
public class RssFeeder implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(RssFeeder.class);
    private static final String RSS_MEDIA_TYPE = "application/xml";
    private final URL endpoint;
    private final URL insertEndpoint;
    private final BlockingQueue<EposEvent> events = new LinkedBlockingQueue<>();
    private final StreamEncoder<RssFeed> feedEncoder = new RssFeedEncoder(new NotificationRssFeedItemEncoder());
    private final HttpClient client;
    private final XmlObject xmlRule;
    /**
     * Creates a new {@code RssFeeder}.
     *
     * @param xmlRule     the rule to filter events
     * @param rssEndpoint the URL to post RSS feeds to
     * @param client      the HTTP client to use
     *
     * @throws MalformedURLException        if the URI could not be
     *                                      converted to a URL
     */
    public RssFeeder(XmlObject xmlRule, URI rssEndpoint, HttpClient client)
            throws MalformedURLException {
        URL url = rssEndpoint.toURL();
        this.xmlRule = Objects.requireNonNull(xmlRule);
        this.client = Objects.requireNonNull(client);
        this.endpoint = Objects.requireNonNull(url);
        this.insertEndpoint = new URL(url.getProtocol(),
                                      url.getHost(),
                                      url.getPort(),
                                      url.getPath() + "/InsertRSS");
        LOG.debug("Endpoint: {}", this.insertEndpoint);
        LOG.debug("Insertion endpoint: {}", this.insertEndpoint);
    }

    @Override
    public void run() {

        try {
            EposEngine.getInstance().registerRule(createRule(xmlRule));
        } catch (FilterInstantiationException ex) {
            LOG.error("Error creating rule", ex);
        }

        while (!Thread.currentThread().isInterrupted()) {
            try {
                EposEvent event = this.events.take();
                sendNotification(event);
            } catch (InterruptedException ex) {
                LOG.info("Interrupted", ex);
                // reset the interrupted state
                Thread.currentThread().interrupt();
                break;
            } catch (IOException | XMLStreamException ex) {
                // log and continue
                LOG.error("IOException", ex);
            }
        }
    }

    /**
     * Send the RSS notification for the received event.
     *
     * @param event the event
     *
     * @throws IOException           if the HTTP call fails
     * @throws XMLStreamException    if the RSS feed could not be encoded
     * @throws MalformedURLException if the GUID/link could not be generated
     */
    @VisibleForTesting
    void sendNotification(EposEvent event)
            throws IOException, XMLStreamException, MalformedURLException {
        RssFeed feed = createFeed(event);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.feedEncoder.encode(feed, baos);
        String request = baos.toString(StandardCharsets.UTF_8.name());

        try (InputStream in = this.client.post(this.insertEndpoint, RSS_MEDIA_TYPE, request)) {
            String response = CharStreams.toString(new InputStreamReader(in));
            LOG.info("Inserted notification item: {}", response);
        }
    }

    /**
     * Creates a {@link Rule} from the EML rule and attaches a listener to it
     * that will push the events into {@linkplain #events the queue}.
     *
     * @param xmlObject the EML XML object
     *
     * @return the rule
     *
     * @throws FilterInstantiationException if the {@link EposFilter} could
     *                                      not be instantiated
     */
    private Rule createRule(XmlObject xmlObject)
            throws FilterInstantiationException {
        Rule rule = new RuleInstance((SimpleRuleListener) (e, o) -> this.events.offer(e));
        EposFilter instantiate = FilterInstantiationRepository.Instance.instantiate(xmlObject);
        try {
            rule.setPassiveFilter((PassiveFilter) instantiate);
        } catch (PassiveFilterAlreadyPresentException ex) {
            // this should not happen as we just created the rule
            throw new Error(ex);
        }
        return rule;
    }

    /**
     * Create a RSS feed containing a single item from the supplied
     * {@code event}.
     *
     * @param event the event
     *
     * @return the RSS feed object
     *
     * @throws MalformedURLException if the GUID/link could not be generated
     */
    private RssFeed createFeed(EposEvent event)
            throws MalformedURLException {
        // TODO are these fixed values?
        String feedDescription = "SOS-Event WPS feeder - alert updates";
        String feedTitle = "SOS-Event WPS feeder";
        DateTime time = new DateTime(event.getStartTime());
        RssFeed feed = new RssFeed(feedTitle, this.endpoint, feedDescription, time, createFeedItem(event));
        return feed;
    }

    /**
     * Create a RSS feed item from the supplied {@code event}.
     *
     * @param event the event
     *
     * @return the RSS feed item
     *
     * @throws MalformedURLException if the GUID/link could not be generated
     */
    private RssFeedItem createFeedItem(EposEvent event)
            throws MalformedURLException {
        DateTime time = new DateTime(event.getStartTime());
        double overshoot = 2.0;
        double value = 3.0;
        double undershoot = 1.0;
        // FIXME get these hardcoded values from the event, once it arrives
        String featureOfInterest = "featureOfInterest";
        String description = "description";
        String procedudure = "procedudure";
        String category = "category";
        String title = "title";
        String observedProperty = "observedProperty";
        URL guid = createGUID(event);
        return new NotificationRssFeedItem(title, guid, category, description,
                time, guid.toString(), procedudure, observedProperty,
                featureOfInterest, undershoot, overshoot, value);
    }

    /**
     * Create the GUID of the feed item for the specified event.
     *
     * @param event the event
     *
     * @return the GUID
     *
     * @throws MalformedURLException if the GUID could not be generated
     */
    @VisibleForTesting
    URL createGUID(EposEvent event) throws MalformedURLException {
        return new URL(this.endpoint.toString() + "/rss/#alert=" + event.getStartTime());
    }
}
