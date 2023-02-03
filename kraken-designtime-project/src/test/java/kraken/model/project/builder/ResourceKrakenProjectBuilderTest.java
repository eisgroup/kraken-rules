package kraken.model.project.builder;

import static java.util.List.of;
import static kraken.model.project.KrakenProjectMocks.contextDefinitions;
import static kraken.model.project.KrakenProjectMocks.contextDefinitionsWithRoot;
import static kraken.model.project.KrakenProjectMocks.entryPoint;
import static kraken.model.project.KrakenProjectMocks.entryPoints;
import static kraken.model.project.KrakenProjectMocks.externalContext;
import static kraken.model.project.KrakenProjectMocks.externalContextDefinitions;
import static kraken.model.project.KrakenProjectMocks.function;
import static kraken.model.project.KrakenProjectMocks.functionSignature;
import static kraken.model.project.KrakenProjectMocks.imports;
import static kraken.model.project.KrakenProjectMocks.includes;
import static kraken.model.project.KrakenProjectMocks.resource;
import static kraken.model.project.KrakenProjectMocks.resourceWithFunctions;
import static kraken.model.project.KrakenProjectMocks.ri;
import static kraken.model.project.KrakenProjectMocks.rules;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.NamespaceNode;
import kraken.model.project.exception.IllegalKrakenProjectStateException;

/**
 * @author mulevicius
 */
public class ResourceKrakenProjectBuilderTest {

    @Test
    public void shouldThrowWhenBuildingKrakenProjectForNamespaceThatDoesNotExist() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of());

        assertThrows(IllegalKrakenProjectStateException.class,
                () -> krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy"));
    }

    @Test
    public void shouldBuildEmptyKrakenProjectWhenResourceIsEmpty() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy", contextDefinitionsWithRoot("Policy"), entryPoints(), rules())
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getNamespace(), is("PersonalAutoPolicy"));
        assertThat(krakenProject.getContextDefinitions().values(), hasSize(1));
        assertThat(krakenProject.getEntryPoints(), empty());
        assertThat(krakenProject.getRules(), empty());
        assertThat(krakenProject.getNamespaceTree(), is(notNullValue()));
    }

    @Test
    public void shouldBuildKrakenProjectFromSingleResource() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy", "RiskItem"),
                        externalContext(List.of("context")),
                        externalContextDefinitions("PreviousProjection", "Security"),
                        of(entryPoint("Validate", of("R01", "R02"))),
                        rules("R01", "R01", "R02")
                )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getNamespace(), is("PersonalAutoPolicy"));
        assertThat(krakenProject.getContextDefinitions().values(), hasSize(2));
        assertThat(krakenProject.getExternalContextDefinitions().values(), hasSize(2));
        assertThat(krakenProject.getEntryPoints(), hasSize(1));
        assertThat(krakenProject.getRules(), hasSize(3));
    }

    @Test
    public void shouldBuildKrakenProjectWithSingleExternalContext() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy"),
                        externalContext(List.of("context")),
                        externalContextDefinitions("PreviousProjection"),
                        of(entryPoint("Validate", of("R01"))),
                        rules("R01")
                )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getNamespace(), is("PersonalAutoPolicy"));
        assertThat(krakenProject.getExternalContext().getContexts().size(), is(1));
    }

    @Test
    public void shouldBuildKrakenProjectFromMultipleResourcesInTheSameNamespaceAndMergeRulesAndNamespacesWithSameName() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy", "RiskItem"),
                        externalContext(List.of("context")),
                        externalContextDefinitions("PreviousProjection", "Security"),
                        of(entryPoint("Validate", of("R01", "R02"))),
                        rules("R01", "R01", "R02")
                ),
                resource("PersonalAutoPolicy",
                        contextDefinitions("Coverage"),
                        null,
                        externalContextDefinitions("Other", "PreviousProjection"),
                        of(
                                entryPoint("Default", of("R03")),
                                entryPoint("Validate", of("R01"))
                        ),
                        rules("R01", "R03")
                )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getNamespace(), is("PersonalAutoPolicy"));
        assertThat(krakenProject.getContextDefinitions().values(), hasSize(3));
        assertThat(krakenProject.getExternalContextDefinitions().values(), hasSize(3));
        assertThat(krakenProject.getEntryPoints(), hasSize(3));
        assertThat(krakenProject.getRules(), hasSize(5));
    }

    @Test
    public void shouldIncludeNamespaceTransitively() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy", "RiskItem"),
                        externalContext(List.of("context")),
                        externalContextDefinitions("Other", "PreviousProjection"),
                        of(entryPoint("Validate", "R01")),
                        rules("R01"),
                        includes("Base")
                ),
                resource("Base",
                        contextDefinitions("Coverage"),
                        null,
                        externalContextDefinitions("Security"),
                        of(entryPoint("ValidateCoverage", "R02")),
                        rules("R02"),
                        includes("Root")
                ),
                resource("Root",
                        contextDefinitionsWithRoot("Root"),
                        null,
                        externalContextDefinitions("NextProjection"),
                        of(entryPoint("ValidateRoot", "R03")),
                        rules("R03")
                )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getNamespace(), is("PersonalAutoPolicy"));
        assertThat(krakenProject.getContextDefinitions().values(), hasSize(4));
        assertThat(krakenProject.getExternalContextDefinitions().values(), hasSize(4));
        assertThat(krakenProject.getEntryPoints(), hasSize(3));
        assertThat(krakenProject.getRules(), hasSize(3));
        assertThat(krakenProject.getNamespaceTree(), is(notNullValue()));
        assertThat(krakenProject.getNamespaceTree().getRoot().getName(), is("PersonalAutoPolicy"));

        NamespaceNode personalNs = krakenProject.getNamespaceTree().getRoot();

        assertThat(personalNs.getChildNodes(), hasSize(1));
        assertThat(personalNs.getChildNodes().get(0).getName(), is("Base"));

        NamespaceNode baseNs = personalNs.getChildNodes().get(0);

        assertThat(baseNs.getChildNodes(), hasSize(1));
        assertThat(baseNs.getChildNodes().get(0).getName(), is("Root"));
        assertThat(baseNs.getChildNodes().get(0).getChildNodes(), is(empty()));
    }

    @Test
    public void shouldIncludeSameItemOnceFromTheSameTransitiveNamespace() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy"),
                        externalContext(List.of("context")),
                        externalContextDefinitions("NextProjection"),
                        entryPoints(),
                        rules(),
                        includes("Personal", "Car")
                ),
                resource("Personal",
                        contextDefinitions(),
                        entryPoints(),
                        rules(),
                        includes("Base")
                ),
                resource("Car",
                        contextDefinitions(),
                        entryPoints(),
                        rules(),
                        includes("Base")
                ),
                resource("Base",
                        contextDefinitionsWithRoot("Policy"),
                        null,
                        externalContextDefinitions("NextProjection"),
                        of(entryPoint("Validate", "R01")),
                        rules("R01")
                )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getNamespace(), is("PersonalAutoPolicy"));
        assertThat(krakenProject.getContextDefinitions().values(), hasSize(1));
        assertThat(krakenProject.getContextDefinitions().get("Policy").getPhysicalNamespace(), equalTo("PersonalAutoPolicy"));
        assertThat(krakenProject.getExternalContextDefinitions().values(), hasSize(1));
        assertThat(krakenProject.getExternalContextDefinitions().get("NextProjection").getPhysicalNamespace(), equalTo("PersonalAutoPolicy"));
        assertThat(krakenProject.getEntryPoints(), hasSize(1));
        assertThat(krakenProject.getEntryPoints().get(0).getPhysicalNamespace(), equalTo("Base"));
        assertThat(krakenProject.getRules(), hasSize(1));
        assertThat(krakenProject.getRules().get(0).getPhysicalNamespace(), equalTo("Base"));
    }

    @Test
    public void shouldMergeMultipleResourcesByNamespace() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitions(),
                        entryPoints(),
                        rules(),
                        includes("Base")
                ),
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy"),
                        entryPoints(),
                        rules(),
                        includes("Base")
                ),
                resource("PersonalAutoPolicy",
                        contextDefinitions(),
                        of(entryPoint("Validate", "R01")),
                        rules(),
                        includes("Base")
                ),
                resource("PersonalAutoPolicy",
                        contextDefinitions(),
                        entryPoints(),
                        rules("R01"),
                        includes("Base")
                ),
                resource("Base",
                        contextDefinitions(),
                        entryPoints(),
                        rules()
                ),
                resource("Base",
                        contextDefinitions("RiskItem"),
                        entryPoints(),
                        rules()
                ),
                resource("Base",
                        contextDefinitions(),
                        of(entryPoint("Default", "R02")),
                        rules()
                ),
                resource("Base",
                        contextDefinitions(),
                        entryPoints(),
                        rules("R02")
                )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getNamespace(), is("PersonalAutoPolicy"));
        assertThat(krakenProject.getContextDefinitions().values(), hasSize(2));
        assertThat(krakenProject.getEntryPoints(), hasSize(2));
        assertThat(krakenProject.getRules(), hasSize(2));
    }

    @Test
    public void shouldOverrideIncludedItemsWhenTheyAreRedefinedInNamespace() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy", "RiskItem"),
                        of(
                                entryPoint("Validate", of("R03")),
                                entryPoint("Validate", of("R03")),
                                entryPoint("Default", of("R01"))
                        ),
                        rules("R01", "R01", "R02"),
                        includes("Base")
                ),
                resource("Base",
                        contextDefinitions("RiskItem", "Coverage"),
                        of(
                                entryPoint("Validate", of("R03")),
                                entryPoint("Default", of("R01")),
                                entryPoint("ValidateCoverage", of("R02"))
                        ),
                        rules("R01", "R02", "R03"),
                        includes("Root")
                ),
                resource("Root",
                        contextDefinitions("Coverage"),
                        of(
                                entryPoint("ValidateCoverage", of("R03")),
                                entryPoint("ValidateCoverage", of("R03"))
                        ),
                        rules("R03", "R03")
                )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        Map<String, ContextDefinition> contextDefinitions = krakenProject.getContextDefinitions();
        Map<String, List<EntryPoint>> entryPoints = krakenProject.getEntryPoints().stream()
                .collect(Collectors.groupingBy(EntryPoint::getName));
        Map<String, List<Rule>> rules = krakenProject.getRules().stream()
                .collect(Collectors.groupingBy(Rule::getName));

        assertThat(krakenProject.getNamespace(), is("PersonalAutoPolicy"));
        assertThat(contextDefinitions.entrySet(), hasSize(3));
        assertThat(contextDefinitions.get("Policy").getPhysicalNamespace(), is("PersonalAutoPolicy"));
        assertThat(contextDefinitions.get("RiskItem").getPhysicalNamespace(), is("PersonalAutoPolicy"));
        assertThat(contextDefinitions.get("Coverage").getPhysicalNamespace(), is("Base"));

        assertThat(entryPoints.entrySet(), hasSize(3));
        assertThat(entryPoints.get("Validate"), hasSize(2));
        assertThat(entryPoints.get("Validate").get(0).getPhysicalNamespace(), is("PersonalAutoPolicy"));
        assertThat(entryPoints.get("Validate").get(1).getPhysicalNamespace(), is("PersonalAutoPolicy"));
        assertThat(entryPoints.get("Default"), hasSize(1));
        assertThat(entryPoints.get("Default").get(0).getPhysicalNamespace(), is("PersonalAutoPolicy"));
        assertThat(entryPoints.get("ValidateCoverage"), hasSize(1));
        assertThat(entryPoints.get("ValidateCoverage").get(0).getPhysicalNamespace(), is("Base"));

        assertThat(rules.entrySet(), hasSize(3));
        assertThat(rules.get("R01"), hasSize(2));
        assertThat(rules.get("R01").get(0).getPhysicalNamespace(), is("PersonalAutoPolicy"));
        assertThat(rules.get("R01").get(1).getPhysicalNamespace(), is("PersonalAutoPolicy"));
        assertThat(rules.get("R02"), hasSize(1));
        assertThat(rules.get("R02").get(0).getPhysicalNamespace(), is("PersonalAutoPolicy"));
        assertThat(rules.get("R03"), hasSize(1));
        assertThat(rules.get("R03").get(0).getPhysicalNamespace(), is("Base"));
    }

    @Test
    public void shouldImportRuleFromAnotherNamespace() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy"),
                        of(entryPoint("Validate", "R01")),
                        rules(),
                        includes(),
                        imports(ri("Base", "R01"))
                ),
                resource("Base",
                        contextDefinitions(),
                        entryPoints(),
                        rules("R01", "R02")
                )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getNamespace(), is("PersonalAutoPolicy"));
        assertThat(krakenProject.getRules(), hasSize(1));
    }

    @Test
    public void shouldImportRuleFromAnotherNamespaceAndOverrideRuleFromInclude() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy"),
                        of(entryPoint("Validate", "R01")),
                        rules(),
                        includes("Base"),
                        imports(ri("Car", "R01"))
                ),
                resource("Car",
                        contextDefinitions(),
                        entryPoints(),
                        rules("R01")
                ),
                resource("Base",
                        contextDefinitions(),
                        entryPoints(),
                        rules("R01")
                )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getNamespace(), is("PersonalAutoPolicy"));
        assertThat(krakenProject.getRules(), hasSize(1));
        assertThat(krakenProject.getRules().get(0).getPhysicalNamespace(), is("Car"));
    }

    @Test
    public void shouldTransitivelyIncludeRuleWhichIsImportedFromAnotherNamespace() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy"),
                        entryPoints(),
                        rules(),
                        includes("Base")
                ),
                resource("Base",
                        contextDefinitions(),
                        of(entryPoint("Validate", "R01")),
                        rules(),
                        includes("Root"),
                        imports(ri("Car", "R01"))
                ),
                resource("Car",
                        contextDefinitions(),
                        entryPoints(),
                        rules("R01")
                ),
                resource("Root",
                        contextDefinitions(),
                        entryPoints(),
                        rules("R01")
                )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getNamespace(), is("PersonalAutoPolicy"));
        assertThat(krakenProject.getRules(), hasSize(1));
        assertThat(krakenProject.getRules().get(0).getPhysicalNamespace(), is("Car"));
    }

    @Test
    public void shouldThrowWhenRuleIsImportedFromNamespaceThatDoesNotExist() {
        assertThrows(IllegalKrakenProjectStateException.class, () -> new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy"),
                        entryPoints(),
                        rules(),
                        includes(),
                        imports(ri("Base", "R01"))
                )
        )));
    }

    @Test
    public void shouldThrowWhenImportedRuleDoesNotExistInThatNamespace() {
        assertThrows(IllegalKrakenProjectStateException.class, () -> new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy"),
                        entryPoints(),
                        rules(),
                        includes(),
                        imports(ri("Base", "R01"))
                ),
                resource("Base",
                        contextDefinitions(),
                        entryPoints(),
                        rules()
                )
        )));
    }

    @Test
    public void shouldThrowWhenImportedRuleIsAlreadyDefined() {
        assertThrows(IllegalKrakenProjectStateException.class, () -> new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy"),
                        entryPoints(),
                        rules("R01"),
                        includes(),
                        imports(ri("Base", "R01"))
                ),
                resource("Base",
                        contextDefinitions(),
                        entryPoints(),
                        rules("R01")
                )
        )));
    }

    @Test
    public void shouldThrowWhenSameRuleIsImportedFromMultipleNamespaces() {
        assertThrows(IllegalKrakenProjectStateException.class, () -> new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy"),
                        entryPoints(),
                        rules(),
                        includes(),
                        imports(ri("Base", "R01"))
                ),
                resource("PersonalAutoPolicy",
                        contextDefinitions(),
                        entryPoints(),
                        rules(),
                        includes(),
                        imports(ri("Policy", "R01"))
                ),
                resource("Base",
                        contextDefinitions(),
                        entryPoints(),
                        rules("R01")
                ),
                resource("Policy",
                        contextDefinitions(),
                        entryPoints(),
                        rules("R01")
                )
        )));
    }

    @Test
    public void shouldThrowWhenIncludedNamespaceDoesNotExist() {
        assertThrows(IllegalKrakenProjectStateException.class, () -> new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy"),
                        entryPoints(),
                        rules(),
                        includes("Base")
                )
        )));
    }

    @Test
    public void shouldThrowWhenNamespaceIncludesItself() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("Base", contextDefinitionsWithRoot("Policy"), entryPoints(), rules(), includes("Base"))
        ));

        assertThrows(IllegalKrakenProjectStateException.class,
                () -> krakenProjectBuilder.buildKrakenProject("Base"));
    }

    @Test
    public void shouldThrowWhenNamespaceIncludesAreCyclical() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("A", contextDefinitionsWithRoot("Root"), entryPoints(), rules(), includes("B")),
                resource("B", contextDefinitions(), entryPoints(), rules(), includes("C")),
                resource("C", contextDefinitions(), entryPoints(), rules(), includes("A"))
        ));

        assertThrows(IllegalKrakenProjectStateException.class,
                () -> krakenProjectBuilder.buildKrakenProject("A"));
    }

    @Test
    public void shouldNotThrowWhenIncludeHasAmbiguitiesButItIsRedefined() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy"),
                        of(entryPoint("Validate", "R01")),
                        rules("R01"),
                        includes("Policy", "Car")
                ),
                resource("Policy",
                        contextDefinitionsWithRoot("Policy"),
                        of(entryPoint("Validate", "R01")),
                        rules("R01")
                ),
                resource("Car",
                        contextDefinitionsWithRoot("Policy"),
                        of(entryPoint("Validate", "R01")),
                        rules("R01")
                )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getNamespace(), is("PersonalAutoPolicy"));
        assertThat(krakenProject.getContextDefinitions().values(), hasSize(1));
        assertThat(krakenProject.getEntryPoints(), hasSize(1));
        assertThat(krakenProject.getRules(), hasSize(1));
    }

    @Test
    public void shouldThrowWhenIncludeHasRuleAmbiguities() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy", contextDefinitionsWithRoot("Policy"), entryPoints(), rules(), includes("Policy", "Car")),
                resource("Policy", contextDefinitions(), entryPoints(), rules("R01")),
                resource("Car", contextDefinitions(), entryPoints(), rules("R01"))
        ));

        assertThrows(IllegalKrakenProjectStateException.class,
                () -> krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy"));
    }

    @Test
    public void shouldThrowWhenIncludeHasEntryPointAmbiguities() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy", contextDefinitionsWithRoot("Policy"), entryPoints(), rules(), includes("Policy", "Car")),
                resource("Policy", contextDefinitions(), entryPoints("Validate"), rules()),
                resource("Car", contextDefinitions(), entryPoints("Validate"), rules())
        ));

        assertThrows(IllegalKrakenProjectStateException.class,
                () -> krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy"));
    }

    @Test
    public void shouldThrowWhenIncludeHasContextDefinitionAmbiguities() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy", contextDefinitions(), entryPoints(), rules(), includes("Policy", "Car")),
                resource("Policy", contextDefinitionsWithRoot(), entryPoints(), rules()),
                resource("Car", contextDefinitionsWithRoot("Policy"), entryPoints(), rules())
        ));

        assertThrows(IllegalKrakenProjectStateException.class,
                () -> krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy"));
    }

    @Test
    public void shouldThrowWhenDuplicateContextDefinitionsWithDifferentContentsAreDefinedInSameNamespace() {
        assertThrows(IllegalKrakenProjectStateException.class, () -> new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy", contextDefinitionsWithRoot("Policy", "RiskItem"), entryPoints(), rules()),
                resource("PersonalAutoPolicy", contextDefinitions("Policy"), entryPoints(), rules())
        )));
    }

    @Test
    public void shouldNotThrowWhenDuplicateContextDefinitionsWithSameContentsAreDefinedInSameNamespace() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy", contextDefinitionsWithRoot("Policy", "RiskItem"), entryPoints(), rules()),
                resource("PersonalAutoPolicy", contextDefinitionsWithRoot("Policy"), entryPoints(), rules())
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");
        assertThat(krakenProject.getContextDefinitions().values(), hasSize(2));
    }

    @Test
    public void shouldThrowWhenAmbiguitiesAreTransitive() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy", contextDefinitions(), entryPoints(), rules(), includes("Policy", "Car")),
                resource("Policy", contextDefinitionsWithRoot("Policy"), entryPoints(), rules(), includes("Base")),
                resource("Car", contextDefinitions(), entryPoints(), rules(), includes("Base"), imports(ri("SpecialCar", "R01"))),
                resource("Base", contextDefinitions(), entryPoints(), rules("R01")),
                resource("SpecialCar", contextDefinitions(), entryPoints(), rules("R01"))
        ));

        assertThrows(IllegalKrakenProjectStateException.class,
                () -> krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy"));
    }

    @Test
    public void shouldRetainRulesThatAreNotIncludedIntoAnyEntryPoint() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy",
                        contextDefinitionsWithRoot("Policy"),
                        of(entryPoint("Validate", "R01")),
                        rules("R01", "R02"),
                        includes(),
                        imports(ri("Base", "R03"))
                ),
                resource("Base",
                        contextDefinitions(),
                        of(entryPoint("Default", "R03")),
                        rules("R03")
                )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getNamespace(), is("PersonalAutoPolicy"));
        assertThat(krakenProject.getRules(), hasSize(1));
    }

    @Test
    public void shouldThrowWhenMultipleExternalContextAvailableInParentAndIncluded() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy", contextDefinitionsWithRoot("Policy"),
                        null,
                        List.of(), entryPoints(), rules(), includes("Policy", "Base")),
                resource("Policy", contextDefinitions(), externalContext("ExternalContext_root", "Policy"),
                        List.of(), entryPoints("Validate"), rules()),
                resource("Base", contextDefinitions(), externalContext("ExternalContext_root", "Base"),
                        List.of(), List.of(), rules())
        ));

        var message = "Kraken Project 'PersonalAutoPolicy' has namespace include errors:" +
                " Item 'ExternalContext_root' is ambiguous, because it is included from multiple namespaces: Policy, Base.";

        assertThrows(message, IllegalKrakenProjectStateException.class,
                () -> krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy"));
    }

    @Test
    public void shouldThrowExceptionIfNoContextDefinitionExistsForNamespace() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
                resource("PersonalAutoPolicy", List.of(),
                        null,
                        List.of(), entryPoints(), rules(), List.of())
        ));

        var message = "KrakenProject for namespace PersonalAutoPolicy does not have any" +
                " context definition defined. At least one context definition is expected.";

        assertThrows(message, IllegalKrakenProjectStateException.class,
                () -> krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy"));
    }

    @Test
    public void shouldAddFunctionSignaturesToKrakenProject() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
            resource(
                "PersonalAutoPolicy",
                List.of(
                    functionSignature("GetAutoPolicy", "AutoPolicy", List.of()),
                    functionSignature("GetPolicy", "Policy", List.of())
                ),
                List.of("Base")
            ),
            resource(
                "Base",
                List.of(
                    functionSignature("GetPolicy", "Policy", List.of())
                ),
                List.of()
            )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getFunctionSignatures(), hasSize(2));
    }

    @Test
    public void shouldThrowIfNamespaceHasClashingFunctionSignatures() {
        assertThrows(IllegalKrakenProjectStateException.class, () -> new ResourceKrakenProjectBuilder(of(
            resource(
                "PersonalAutoPolicy",
                List.of(
                    functionSignature("GetAutoPolicy", "AutoPolicy", List.of()),
                    functionSignature("GetAutoPolicy", "Policy", List.of())
                ),
                List.of("Base")
            )
        )));
    }

    @Test
    public void shouldAddFunctionsToKrakenProject() {
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(of(
            resourceWithFunctions(
                "PersonalAutoPolicy",
                List.of(
                    function("USD", "'USD'"),
                    function("EUR", "'EUR'")
                ),
                List.of("Base")
            ),
            resourceWithFunctions(
                "Base",
                List.of(
                    function("EUR", "'EUR'")
                ),
                List.of()
            )
        ));

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject("PersonalAutoPolicy");

        assertThat(krakenProject.getFunctions(), hasSize(2));
    }

    @Test
    public void shouldThrowIfNamespaceHasDuplicateFunctions() {
        assertThrows(IllegalKrakenProjectStateException.class, () -> new ResourceKrakenProjectBuilder(of(
            resourceWithFunctions(
                "PersonalAutoPolicy",
                List.of(
                    function("USD", "'USD'"),
                    function("USD", "'USD'")
                ),
                List.of("Base")
            )
        )));
    }
}
