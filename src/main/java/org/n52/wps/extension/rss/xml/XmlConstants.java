package org.n52.wps.extension.rss.xml;

import java.util.Locale;

import javax.xml.namespace.QName;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Constants for RSS serialization.
 *
 * @author Christian Autermann
 */
public interface XmlConstants {

    String XML_ENCODING = "UTF-8";
    String XML_VERSION = "1.0";

    String NS_PREFIX = "sc";
    String NS_URI = "http://iddss-sensor.cdmps.org.au:8080/rssschemas/";
    String RSS_VERSION = "2.0";

    String AN_VERSION = "version";
    String EN_CATEGORY = "category";
    String EN_CHANNEL = "channel";
    String EN_DESCRIPTION = "description";
    String EN_FEATURE_OF_INTEREST = "featureOfInterest";
    String EN_GUID = "guid";
    String EN_ITEM = "item";
    String EN_LINK = "link";
    String EN_OBSERVED_PROPERTY = "observedProperty";
    String EN_OVERSHOOT = "overshoot";
    String EN_PROCEDURE = "procedure_id";
    String EN_PUB_DATE = "pubDate";
    String EN_RSS = "rss";
    String EN_TITLE = "title";
    String EN_UNDERSHOOT = "undershoot";
    String EN_VALUE = "observation_value";

    QName QN_CATEGORY = new QName(EN_CATEGORY);
    QName QN_CHANNEL = new QName(EN_CHANNEL);
    QName QN_DESCRIPTION = new QName(EN_DESCRIPTION);
    QName QN_FEATURE_OF_INTEREST = new QName(NS_URI, EN_FEATURE_OF_INTEREST, NS_PREFIX);
    QName QN_GUID = new QName(EN_GUID);
    QName QN_ITEM = new QName(EN_ITEM);
    QName QN_LINK = new QName(EN_LINK);
    QName QN_OBSERVED_PROPERTY = new QName(NS_URI, EN_OBSERVED_PROPERTY, NS_PREFIX);
    QName QN_OVERSHOOT = new QName(NS_URI, EN_OVERSHOOT, NS_PREFIX);
    QName QN_PROCEDURE = new QName(NS_URI, EN_PROCEDURE, NS_PREFIX);
    QName QN_PUB_DATE = new QName(EN_PUB_DATE);
    QName QN_RSS = new QName(EN_RSS);
    QName QN_TITLE = new QName(EN_TITLE);
    QName QN_UNDERSHOOT = new QName(NS_URI, EN_UNDERSHOOT, NS_PREFIX);
    QName QN_VALUE = new QName(NS_URI, EN_VALUE, NS_PREFIX);

    DateTimeFormatter RFC_1123_DATE_TIME_FORMAT = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withLocale(Locale.US).withZoneUTC();
}
