package org.n52.wps.extension.rss;

import static com.google.common.base.Strings.emptyToNull;
import static java.util.Objects.requireNonNull;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.joda.time.DateTime;

/**
 * Simple single-channel implementation of a RSS feed.
 *
 * @author Christian Autermann
 */
public class RssFeed {

    private final String title;
    private final URL link;
    private final String description;
    private final DateTime date;
    private final List<RssFeedItem> items = new LinkedList<>();

    /**
     * Creates a new {@code RssFeed}.
     *
     * @param title       the title
     * @param link        the link
     * @param description the description
     * @param date        the publication date
     *
     * @throws NullPointerException if any of the parameters is {@code null} or
     *                              an empty string
     */
    public RssFeed(String title, URL link, String description, DateTime date) {
        this.title = requireNonNull(emptyToNull(title));
        this.link = requireNonNull(link);
        this.description = requireNonNull(emptyToNull(description));
        this.date = requireNonNull(date);
    }

    /**
     * Creates a new {@code RssFeed} with the supplied items.
     * @param title the title
     * @param link the link
     * @param description the description
     * @param date the publication date
     * @param items  the
     * * @throws NullPointerException if any of the parameters is {@code null} or
     *                              an empty string
     */
    public RssFeed(String title, URL link, String description, DateTime date, RssFeedItem... items) {
        this(title, link, description, date);
        Arrays.stream(items)
                .map(Optional::ofNullable)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this.items::add);
    }

    /**
     * Get the publication date of this feed.
     *
     * @return the date
     */
    public DateTime getDate() {
        return date;
    }

    /**
     * Get the description of this feed.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the link of this feed.
     *
     * @return the link
     */
    public URL getLink() {
        return link;
    }

    /**
     * Get the title of this feed.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the items of this feed.
     *
     * @return the items
     */
    public List<RssFeedItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Get the items of this feed as a stream.
     *
     * @return the stream of items
     */
    public Stream<RssFeedItem> stream() {
        return getItems().stream();
    }

    /**
     * Adds an item to this feed
     *
     * @param item the item
     *
     * @return {@code this}
     */
    public RssFeed addItem(RssFeedItem item) {
        this.items.add(requireNonNull(item));
        return this;
    }

}
