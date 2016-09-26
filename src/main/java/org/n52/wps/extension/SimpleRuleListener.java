package org.n52.wps.extension;

import org.n52.epos.event.EposEvent;
import org.n52.epos.rules.RuleListener;

/**
 * Simple {@code RuleListener} that has some default methods.
 *
 * @author Christian Autermann
 */
public interface SimpleRuleListener extends RuleListener {

    @Override
    default Object getEndpointReference() {
        return null;
    }

    @Override
    default void onMatchingEvent(EposEvent ee) {
        onMatchingEvent(ee, null);
    }

}
