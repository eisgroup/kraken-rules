/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kraken.engine.sanity.check;

import static kraken.utils.SnapshotMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import kraken.runtime.DataContextPathProvider;
import kraken.runtime.EvaluationConfig;
import kraken.runtime.EvaluationMode;
import kraken.runtime.engine.result.reducers.validation.ValidationStatusReducer;
import kraken.testproduct.domain.AccessTrackInfo;
import kraken.testproduct.domain.COLLCoverage;
import kraken.testproduct.domain.CarCoverage;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.TermDetails;
import kraken.testproduct.domain.TransactionDetails;
import kraken.testproduct.domain.Vehicle;
import kraken.tracer.observer.Slf4jTraceObserver;
import kraken.utils.Dates;
import kraken.utils.Snapshot;
import kraken.utils.SnapshotMatchers;
import kraken.utils.SnapshotTestRunner;

/**
 * @author mulevicius
 */
@RunWith(SnapshotTestRunner.class)
public class EngineTraceSnapshotTest extends SanityEngineBaseTest {

    private Snapshot snapshot;

    private ListAppender<ILoggingEvent> appender;

    private DataContextPathProvider dataContextPathProvider;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        Logger logger = (Logger) LoggerFactory.getLogger(Slf4jTraceObserver.TRACER_NAME);
        var appender = new ListAppender<ILoggingEvent>();
        appender.setName(this.getClass().getName());
        appender.start();
        logger.addAppender(appender);

        var pathsById = new LinkedHashMap<String, String>();
        pathsById.put("TermDetails-1", "path.to.TermDetails-1");
        pathsById.put("CarCoverage-1", "path.to.CarCoverage-1");

        this.appender = appender;
        this.dataContextPathProvider = new TestContextPathProvider(pathsById);
    }

    @After
    public void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(Slf4jTraceObserver.TRACER_NAME);
        appender.stop();
        logger.detachAppender(appender);
    }

    @Test
    public void shouldMatchTracerOutputSnapshot() {
        Clock mockedClock = Clock.fixed(Instant.parse("2023-01-01T00:00:00.00Z"), ZoneId.systemDefault());
        try (MockedStatic<Clock> clock = Mockito.mockStatic(Clock.class)) {
            // statically mocks current java clock in scope of this test
            // so that LocalDateTime#now and LocalDate#now returns fixed time
            clock.when(Clock::systemDefaultZone).thenReturn(mockedClock);

            var result = engine.evaluate(policy(), "TracerSnapshotTest", new EvaluationConfig(
                Map.of(),
                "USD",
                EvaluationMode.ALL,
                dataContextPathProvider
            ));

            createReducer(overriddenRuleEvaluations()).reduce(result);
            assertThat(traceOutput(), matches(snapshot));
        }
    }

    private String traceOutput() {
        return appender.list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .collect(Collectors.joining(System.lineSeparator()));
    }

    private ValidationStatusReducer createReducer(List<String> overriddenRuleEvaluations) {
        return new ValidationStatusReducer(
            (ruleInfo, ruleContextInfo) -> overriddenRuleEvaluations.contains(
                ruleInfo.getRuleName() + ":" + ruleContextInfo.getContextName() + ":" + ruleContextInfo.getContextId()
            ));
    }

    private List<String> overriddenRuleEvaluations() {
        return List.of(
            "TR_CarCoverage_code_assert:COLLCoverage:COLLCoverage-1"
        );
    }

    private static class TestContextPathProvider implements DataContextPathProvider {

        private final Map<String, String> pathsById;

        public TestContextPathProvider(Map<String, String> pathsById) {
            this.pathsById = pathsById;
        }

        @Override
        public String getPath(@Nonnull String dataContextId) {
            return pathsById.get(dataContextId);
        }

    }

    private Policy policy() {
        var policy = new Policy();
        policy.setId("Policy-1");
        policy.setState("OT");

        var vehicle = new Vehicle();
        vehicle.setId("Vehicle-1");
        vehicle.setCostNew(new BigDecimal("30000"));
        policy.setRiskItems(List.of(vehicle));

        var collCoverage = new COLLCoverage();
        collCoverage.setCode("ABC");
        collCoverage.setLimitAmount(new BigDecimal(15));
        collCoverage.setDeductibleAmount(new BigDecimal(25));

        collCoverage.setId("COLLCoverage-1");
        vehicle.setCollCoverages(List.of(collCoverage));

        var accessTrackInfo = new AccessTrackInfo();
        accessTrackInfo.setId("AccessTrackInfo-1");
        policy.setAccessTrackInfo(accessTrackInfo);

        var termDetails = new TermDetails();
        termDetails.setId("TermDetails-1");
        termDetails.setTermEffectiveDate(Dates.convertISOToLocalDate("1999-01-01"));
        policy.setTermDetails(termDetails);

        var transactionDetails = new TransactionDetails();
        transactionDetails.setId("TransactionDetails-1");
        policy.setTransactionDetails(transactionDetails);

        var carCoverage = new CarCoverage();
        carCoverage.setId("CarCoverage-1");
        carCoverage.setLimitAmount(new BigDecimal(10000));
        policy.setCoverage(carCoverage);

        return policy;
    }

}
