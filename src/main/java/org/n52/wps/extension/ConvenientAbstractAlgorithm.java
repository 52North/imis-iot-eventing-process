package org.n52.wps.extension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ExceptionReport;

/**
 * Extension of {@code AbstractAlgorithm} adding some convenient methods.
 *
 * @author Christian Autermann
 */
public abstract class ConvenientAbstractAlgorithm extends AbstractAlgorithm {

    @Override
    public List<String> getErrors() {
        return Collections.emptyList();
    }

    /**
     * Will return the first input with the id {@code key}.
     *
     * @param <T>    the resulting type
     * @param inputs the input data map
     * @param key    the input id
     *
     * @return the input
     *
     * @throws ExceptionReport if the input does not exist
     */
    protected static <T> T getSingleInput(Map<String, List<IData>> inputs,
                                          String key)
            throws ExceptionReport {
        T input = getOptionalSingleInput(inputs, key, null);
        if (input == null) {
            throw missingParameterValue(key);
        }
        return input;
    }

    /**
     * Will return the first input with the id {@code key} if it exist or else
     * {@code defaultValue}.
     *
     * @param <T>          the resulting type
     * @param inputs       the input data map
     * @param key          the input id
     * @param defaultValue the default value (may be {@code null})
     *
     * @return the input or {@code defaultValue}
     */
    protected static <T> T getOptionalSingleInput(
            Map<String, List<IData>> inputs,
            String key, T defaultValue) {
        return ConvenientAbstractAlgorithm
                .<T>getOptionalSingleInput(inputs, key)
                .orElse(defaultValue);
    }

    /**
     * Will return the first input with the id {@code key} as an
     * {@code Optional}.
     *
     * @param <T>    the resulting type
     * @param inputs the input data map
     * @param key    the input id
     *
     * @return the optional input
     */
    @SuppressWarnings("unchecked")
    protected static <T> Optional<T> getOptionalSingleInput(
            Map<String, List<IData>> inputs, String key) {
        return Optional.ofNullable(inputs)
                .map(m -> m.get(key))
                .filter(l -> !l.isEmpty())
                .map(m -> (T) m.get(0).getPayload());
    }

    /**
     * Creates an {@code ExceptionReport} describing the absence of the required
     * parameter {@code inputId}
     *
     * @param inputId the input id
     *
     * @return the exception report
     */
    protected static ExceptionReport missingParameterValue(String inputId) {
        return new ExceptionReport(String.format("Missing %s input", inputId),
                                   ExceptionReport.MISSING_PARAMETER_VALUE);
    }

    /**
     * Creates an {@code ExceptionReport} wrapping a generic {@code Throwable}.
     *
     * @param throwable the {@code Throwable}
     *
     * @return the {@code ExceptionReport}
     */
    protected static ExceptionReport unknownError(Throwable throwable) {
        return new ExceptionReport("Error executing process",
                                   ExceptionReport.NO_APPLICABLE_CODE, throwable);
    }
}
