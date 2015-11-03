package org.n52.wps.extension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.epos.engine.EposEngine;
import org.n52.epos.engine.rules.RuleInstance;
import org.n52.epos.event.EposEvent;
import org.n52.epos.filter.FilterInstantiationException;
import org.n52.epos.filter.FilterInstantiationRepository;
import org.n52.epos.filter.PassiveFilter;
import org.n52.epos.rules.PassiveFilterAlreadyPresentException;
import org.n52.epos.rules.Rule;
import org.n52.epos.rules.RuleListener;
import org.n52.epos.transform.TransformationException;
import org.n52.epos.transform.TransformationRepository;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericXMLDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventingProcess extends AbstractAlgorithm{

    private final String inputRuleId = "notification-rule";
    private final String inputGetObservationTemplateId = "getobservation-template";
    private final String inputEndpointId = "notification-endpoint";
    private final String outputEndpointId = "notification-endpoint";
    
    private static Logger logger = LoggerFactory.getLogger(EventingProcess.class);

    private static final long waitTime = 5000;

    private Object mutex = new Object();
    private List<EposEvent> results = new ArrayList<EposEvent>();
    
    public Map<String, IData> run(Map<String, List<IData>> inputData) throws ExceptionReport {
        
        Map<String, IData> result = new HashMap<>(1);
        
        //register rule        
        List<IData> inputRuleList = inputData.get(inputRuleId);
        
        XmlObject xmlRule = (XmlObject) inputRuleList.get(0);
        
        try {
            registerRule(xmlRule);
            
            //TODO: get observations from SOS Feeder
            //Use test Observation for now
            pushEvents("Overshoot_Notify1.xml", "Overshoot_Notify2.xml");
            
            EposEvent resultEvent = waitForFirstResult();
            
            //handle result, send possible notifications to endpoint
            
        } catch (PassiveFilterAlreadyPresentException | FilterInstantiationException | XmlException | IOException | TransformationException e) {
            logger.error(e.getMessage());
        }
        
        return result;
    }
    
    private Rule registerRule(XmlObject xmlObject) throws PassiveFilterAlreadyPresentException, FilterInstantiationException, XmlException, IOException {
            Rule rule = createBasicRule();
            rule.setPassiveFilter((PassiveFilter)
                            FilterInstantiationRepository.Instance.instantiate(xmlObject));
            EposEngine.getInstance().registerRule(rule);
            return rule;
    }

    protected List<EposEvent> pushEvents(XmlObject... xmlEvent)
                    throws TransformationException, XmlException, IOException {
            List<EposEvent> result = new ArrayList<EposEvent>();

            for (XmlObject xmlObject : xmlEvent) {
                    result.add((EposEvent) TransformationRepository.Instance.transform(
                            xmlObject, EposEvent.class));
                    EposEngine.getInstance().filterEvent(result.get(result.size()-1));
            }

            return result;
    }

    protected List<EposEvent> pushEvents(String... fileNames)
                    throws TransformationException, XmlException, IOException {
            List<EposEvent> result = new ArrayList<EposEvent>();

            for (String fn : fileNames) {
                    result.add((EposEvent) TransformationRepository.Instance.transform(
                                    readXmlContent(fn), EposEvent.class));
                    EposEngine.getInstance().filterEvent(result.get(result.size()-1));
            }

            return result;
    }

    protected XmlObject readXmlContent(String resource) throws XmlException,
                    IOException {
            return XmlObject.Factory
                            .parse(getClass().getResourceAsStream(resource));
    }

    protected EposEvent waitForFirstResult() {
            oneTimeWait();

            EposEvent result = null;

            if (results.size() > 0) {
                    result = results.get(0);
            }

            results.clear();

            return result;
    }

    protected List<EposEvent> waitForResult() {
            oneTimeWait();

            List<EposEvent> resultsCopy = new ArrayList<EposEvent>(results);
            results.clear();

            return resultsCopy;
    }

    private void oneTimeWait() {
            synchronized (mutex) {
                    if (results.size() == 0) {
                            try {
                                    mutex.wait(waitTime);
                            } catch (InterruptedException e) {
                                    logger.warn(e.getMessage(), e);
                            }
                    }
            }
    }

    protected Rule createBasicRule() {
            return new RuleInstance(new RuleListener(){

                
                public void onMatchingEvent(EposEvent event) {
                        onMatchingEvent(event, null);
                }

                
                public void onMatchingEvent(EposEvent event,
                                Object desiredOutputToConsumer) {
                        logger.info("Desired Output: {}", desiredOutputToConsumer);

                        synchronized (mutex) {
                                results.add(event);
                                mutex.notifyAll();
                        }
                }

                
                public Object getEndpointReference() {
                        return null;
                }
                
            });
    }
    
    public List<String> getErrors() {
        return null;
    }

    public Class<?> getInputDataType(String id) {
        switch (id) {
        case inputRuleId:
            return GenericXMLDataBinding.class;
        case inputGetObservationTemplateId:
            return GenericXMLDataBinding.class;
        case inputEndpointId:
            return LiteralAnyURIBinding.class;
        default:
            return null;
        }
    }

    public Class<?> getOutputDataType(String id) {
        return LiteralAnyURIBinding.class;
    }

}
