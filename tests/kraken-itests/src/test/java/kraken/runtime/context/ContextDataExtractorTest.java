/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.context;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import kraken.runtime.EvaluationConfig;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.context.data.DataContextBuilder;
import kraken.runtime.engine.context.data.ExtractedChildDataContextBuilder;
import kraken.runtime.engine.context.data.NodeInstanceInfo;
import kraken.runtime.engine.context.extraction.ContextDataExtractor;
import kraken.runtime.engine.context.extraction.instance.ContextExtractionResultBuilder;
import kraken.runtime.engine.context.info.ContextInstanceInfo;
import kraken.runtime.engine.context.info.SimpleDataObjectInfoResolver;
import kraken.runtime.engine.context.info.navpath.DataNavigationContextInstanceInfo;
import kraken.runtime.engine.context.info.navpath.DataNavigationContextInstanceInfoResolver;
import kraken.runtime.engine.context.type.registry.TypeRegistry;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.expressions.KrakenTypeProvider;
import kraken.test.TestResources;
import kraken.testproduct.domain.AddressInfo;
import kraken.testproduct.domain.BillingInfo;
import kraken.testproduct.domain.CreditCardInfo;
import kraken.testproduct.domain.DriverInfo;
import kraken.testproduct.domain.Party;
import kraken.testproduct.domain.PartyRole;
import kraken.testproduct.domain.PersonInfo;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.PolicyDetail;
import kraken.testproduct.domain.TermDetails;
import kraken.testproduct.domain.meta.Identifiable;

/**
 * Created by rimas on 25/01/17.
 */
public class ContextDataExtractorTest {

    private DataContextBuilder dataContextBuilder;

    private ContextDataExtractor contextDataExtractor;

    @Before
    public void setUp() {
        var testResources = TestResources.create(TestResources.Info.TEST_PRODUCT);
        var runtimeContextRepository = testResources.getRuntimeContextRepository();
        var instanceInfoResolver = new DataNavigationContextInstanceInfoResolver();
        instanceInfoResolver.setInfoResolver(new MockInfoResolver());

        var session = new EvaluationSession(
            new EvaluationConfig(),
            Collections.emptyMap(),
            mock(KrakenTypeProvider.class),
            Map.of(),
            testResources.getKrakenProject().getNamespace()
        );
        this.dataContextBuilder = new DataContextBuilder(runtimeContextRepository, instanceInfoResolver);
        this.contextDataExtractor = new ContextDataExtractor(
            runtimeContextRepository,
            testResources.getModelTree(),
            new ExtractedChildDataContextBuilder(
                dataContextBuilder,
                new ContextExtractionResultBuilder(TypeRegistry.builder().build()),
                new KrakenExpressionEvaluator(),
                session
            )
        );
    }

    @Test
    public void resolveChildContextShouldResolveImmediateChild() {
        Policy policy = policy();
        DataContext rootContext = dataContextBuilder.buildFromRoot(policy);

        String childContextName = "CreditCardInfo";
        List<DataContext> childContexts = contextDataExtractor.extractByName(
                childContextName,
                rootContext
        );
        assertThat(childContexts, hasSize(1));

        DataContext childContext = childContexts.get(0);
        assertThat(childContext.getContextName(), is(equalTo(childContextName)));
        assertThat(childContext.getDataObject(), is(policy.getBillingInfo().getCreditCardInfo()));
    }

    @Test
    public void resolveResolveNoChildContextIfNavigationExpressionThrowsError() {
        Policy policy = policy();
        policy.setBillingInfo(null);

        var policyContext = dataContextBuilder.buildFromRoot(policy);
        var contexts = contextDataExtractor.extractByName("CreditCardInfo", policyContext);
        assertThat(contexts, hasSize(0));
    }

    @Test
    public void resolveChildContextShouldResolveLeaf() {
        Policy policy = policy();
        DataContext rootContext = dataContextBuilder.buildFromRoot(policy);

        String childContextName = "DriverInfo";
        List<DataContext> childContexts = contextDataExtractor.extractByName(
                childContextName,
                rootContext
        );
        assertThat(childContexts, hasSize(1));

        DataContext childContext = childContexts.get(0);
        assertThat(childContext.getContextName(), is(equalTo(childContextName)));
        assertThat(
                childContext.getDataObject(),
                is(policy.getParties().get(0).getDriverInfo())
        );
    }

    @Test
    public void  resolveChildContextShouldResolveLeafWhenRestrictionIsUsed() {
        Policy policy = policy();
        DataContext rootContext = dataContextBuilder.buildFromRoot(policy);

        PersonInfo personInfo = policy.getParties().get(0).getPersonInfo();
        ContextInstanceInfo contextInstanceInfo = new DataNavigationContextInstanceInfo(personInfo.getClass().getSimpleName(),
                personInfo.getId(), "");

        NodeInstanceInfo nodeInstanceInfo = NodeInstanceInfo.from(contextInstanceInfo);

        String childContextName = "AddressInfo";
        List<DataContext> childContexts = contextDataExtractor.extractByName(
                childContextName,
                rootContext,
                nodeInstanceInfo
        );
        assertThat(childContexts, hasSize(1));

        DataContext result = childContexts.get(0);
        assertThat(result.getContextName(), is(equalTo(childContextName)));
        assertThat(result.getDataObject(), is(personInfo.getAddressInfo()));
    }

    @Test
    public void resolveChildContextShouldResolveChildCollection() {
        Policy policy = policy();
        DataContext rootContext = dataContextBuilder.buildFromRoot(policy);

        String childContextName = "Party";
        List<DataContext> childContexts = contextDataExtractor.extractByName(
                childContextName,
                rootContext
        );
        final List<? extends Party> parties = policy.getParties();
        assertThat(childContexts, hasSize(2));
        assertThat(
                childContexts,
                hasItem(allOf(
                        hasProperty("contextName", is(equalTo(childContextName))),
                        hasProperty("dataObject", is(parties.get(0)))
                ))
        );
        assertThat(
                childContexts,
                hasItem(allOf(
                        hasProperty("contextName", is(equalTo(childContextName))),
                        hasProperty("dataObject", is(parties.get(1)))
                ))
        );
    }

    @Test
    public void resolveChildContextShouldResolveLeafsInDifferentCollections() {
        Policy policy = policy();
        DataContext rootContext = dataContextBuilder.buildFromRoot(policy);

        String childContextName = "PartyRole";
        List<DataContext> childContexts = contextDataExtractor.extractByName(
                childContextName,
                rootContext
        );
        List<? extends Party> parties = policy.getParties();
        assertThat(childContexts, hasSize(3));
        assertThat(
                childContexts,
                hasItem(allOf(
                        hasProperty("contextName", is(equalTo(childContextName))),
                        hasProperty("dataObject", is(parties.get(0).getRoles().get(0)))
                ))
        );
        assertThat(
                childContexts,
                hasItem(allOf(
                        hasProperty("contextName", is(equalTo(childContextName))),
                        hasProperty("dataObject", is(parties.get(0).getRoles().get(1)))
                ))
        );
        assertThat(
                childContexts,
                hasItem(allOf(
                        hasProperty("contextName", is(equalTo(childContextName))),
                        hasProperty("dataObject", is(parties.get(1).getRoles().get(0)))
                ))
        );
    }

    @Test
    public void resolveChildContextShouldResolveCollectionInCollectionWhenRestrictionIsUsed() {
        Policy policy = policy();
        DataContext rootContext = dataContextBuilder.buildFromRoot(policy);

        Party party = policy.getParties().get(0);
        final NodeInstanceInfo instanceInfo = NodeInstanceInfo.from(
                new DataNavigationContextInstanceInfo(
                        party.getClass().getSimpleName(),
                        party.getId(),
                        ""
                )
        );
        String childContextName = "PartyRole";
        List<DataContext> childContexts = contextDataExtractor.extractByName(
                childContextName,
                rootContext,
                instanceInfo
        );
        List<? extends PartyRole> expectedPartyRoles = party.getRoles();

        assertThat(childContexts, hasSize(expectedPartyRoles.size()));
        expectedPartyRoles.forEach(partyRole -> assertThat(
                childContexts,
                hasItem(allOf(
                        hasProperty("contextName", is(equalTo(childContextName))),
                        hasProperty("dataObject", is(partyRole))
                ))
        ));
    }

    private static final class MockInfoResolver extends SimpleDataObjectInfoResolver {

        @Override
        public String resolveContextNameForObject(Object data) {
            return data.getClass().getSimpleName();
        }

        @Override
        public String resolveContextIdForObject(Object data) {
            return ((Identifiable) data).getId();
        }

    }

    private Policy policy() {
        Policy policy = new Policy();
        BillingInfo billingInfo = new BillingInfo();
        billingInfo.setCreditCardInfo(new CreditCardInfo());
        policy.setBillingInfo(billingInfo);
        Party party1 = new Party("1");
        party1.setDriverInfo(new DriverInfo());
        PersonInfo personInfo = new PersonInfo();
        personInfo.setAddressInfo(new AddressInfo());
        party1.setPersonInfo(personInfo);
        party1.setRoles(List.of(new PartyRole(), new PartyRole()));
        Party party2 = new Party("2");
        party2.setRoles(List.of(new PartyRole()));
        policy.setParties(List.of(party1, party2));
        policy.setPolicyDetail(new PolicyDetail());
        policy.setRiskItems(new ArrayList<>());
        policy.setTermDetails(new TermDetails());
        return policy;
    }

}
