package org.n52.wps.extension.rss;

import static com.google.common.base.Strings.emptyToNull;
import static java.util.Objects.requireNonNull;

import java.net.URL;

import org.joda.time.DateTime;

/**
 * Specialized {@linkplain RssFeedItem RSS feed item} for notifications.
 *
 * @author Christian Autermann
 */
public class NotificationRssFeedItem extends RssFeedItem {

    private final String procedure;
    private final String observedProperty;
    private final String featureOfInterest;
    private final double undershoot;
    private final double overshoot;
    private final double value;

    /**
     * Creates a new {@code NotificationRssFeedItem}.
     *
     * @param title             the title
     * @param link              the link
     * @param category          the category
     * @param description       the description
     * @param date              the date
     * @param guid              the GUID
     * @param procedure         the procedure
     * @param observedProperty  the observed property
     * @param featureOfInterest the feature of interest
     * @param undershoot        the undershoot value
     * @param overshoot         the overshoot value
     * @param value             the value
     */
    public NotificationRssFeedItem(String title,
                                   URL link, String category, String description,
                                   DateTime date, String guid,
                                   String procedure, String observedProperty,
                                   String featureOfInterest, double undershoot,
                                   double overshoot, double value) {
        super(title, link, category, description, date, guid);
        this.procedure = requireNonNull(emptyToNull(procedure));
        this.observedProperty = requireNonNull(emptyToNull(observedProperty));
        this.featureOfInterest = requireNonNull(emptyToNull(featureOfInterest));
        this.undershoot = undershoot;
        this.overshoot = overshoot;
        this.value = value;
    }

    /**
     * Get the procedure of this item.
     *
     * @return the procedure
     */
    public String getProcedure() {
        return procedure;
    }

    /**
     * Get the observed property of this item.
     *
     * @return the observed property
     */
    public String getObservedProperty() {
        return observedProperty;
    }

    /**
     * Get the feature of interest of this item.
     *
     * @return the feature of interest
     */
    public String getFeatureOfInterest() {
        return featureOfInterest;
    }

    /**
     * Get the undershoot value of this item.
     *
     * @return the undershoot value
     */
    public double getUndershoot() {
        return undershoot;
    }

    /**
     * Get the overshoot value of this item.
     *
     * @return the overshoot value
     */
    public double getOvershoot() {
        return overshoot;
    }

    /**
     * Get the value of this item.
     *
     * @return the value
     */
    public double getValue() {
        return value;
    }

}
