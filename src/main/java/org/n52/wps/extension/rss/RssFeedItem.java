package org.n52.wps.extension.rss;

import static com.google.common.base.Strings.emptyToNull;
import static java.util.Objects.requireNonNull;

import java.net.URL;

import org.joda.time.DateTime;

/**
 * A {@linkplain RssFeed RSS feed} item.
 *
 * @author Christian Autermann
 */
public class RssFeedItem {

    private final String title;
    private final URL link;
    private final String category;
    private final String description;
    private final DateTime date;
    private final String guid;

    /**
     * Creates a new {@code RssFeedItem}.
     *
     * @param title       the title
     * @param link        the link
     * @param category    the category
     * @param description the description
     * @param date        the date
     * @param guid        the GUID
     *
     * @throws NullPointerException if any of the parameters is {@code null} or
     *                              the empty string
     */
    public RssFeedItem(String title, URL link, String category,
                       String description, DateTime date, String guid) {
        this.title = requireNonNull(emptyToNull(title));
        this.link = requireNonNull(link);
        this.category = requireNonNull(emptyToNull(category));
        this.description = requireNonNull(emptyToNull(description));
        this.date = requireNonNull(date);
        this.guid = requireNonNull(emptyToNull(guid));
    }

    /**
     * Get the title of this item.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the link of this item.
     *
     * @return the link
     */
    public URL getLink() {
        return link;
    }

    /**
     * Get the category of this item.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Get the description of this item.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the publication date of this item.
     *
     * @return the date
     */
    public DateTime getDate() {
        return date;
    }

    /**
     * Get the GUID of this item.
     *
     * @return the GUID
     */
    public String getGuid() {
        return guid;
    }

}
