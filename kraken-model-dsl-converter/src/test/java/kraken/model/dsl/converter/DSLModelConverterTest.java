package kraken.model.dsl.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import kraken.model.Expression;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;
import kraken.model.resource.Resource;
import kraken.model.resource.RuleImport;
import kraken.model.resource.builder.ResourceBuilder;
import kraken.model.validation.AssertionPayload;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author psurinin
 */
public class DSLModelConverterTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();
    private DSLModelConverter converter;

    @Before
    public void setUp() {
        converter = new DSLModelConverter();
    }

    @Test
    public void shouldConvertContextsWithoutMissingArtifacts() {
        Resource resource = ResourceBuilder.getInstance()
                .addContextDefinition(context())
                .addContextDefinition(context())
                .addContextDefinition(context())
                .build();
        final String convert = converter.convert(resource);
        assertEquals(
                        "Context fake {" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        "Context fake {" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        "Context fake {" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator(),
                convert
        );
    }

    @Test
    public void shouldConvertContextsWithNamespace() {
        Resource resource = ResourceBuilder.getInstance()
                .addContextDefinitions(Arrays.asList(context(), context()))
                .setNamespace("NS")
                .build();
        final String convert = converter.convert(resource);
        assertEquals(
                "Namespace NS" +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        "Context fake {" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        "Context fake {" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator(),
                convert);
    }

    @Test
    public void shouldConvertContextsWithoutIncludesWhenNoNamespaceProvided() {
        Resource resource = ResourceBuilder.getInstance()
                .addContextDefinitions(Arrays.asList(context(), context()))
                .addIncludes(Collections.singletonList("fake"))
                .build();
        final String convert = converter.convert(resource);
        assertEquals(
                        "Context fake {" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        "Context fake {" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator(),
                convert);
    }

    @Test
    public void shouldConvertContextsWithIncludesAndNamespace() {
        Resource resource = ResourceBuilder.getInstance()
                .addContextDefinitions(Arrays.asList(context(), context()))
                .addIncludes(Arrays.asList("fake_include_1", "fake_include_2"))
                .setNamespace("fake_namespace")
                .build();
        final String convert = converter.convert(resource);
        assertEquals(
                "Namespace fake_namespace" + System.lineSeparator() + System.lineSeparator() +
                        "Include fake_include_1" + System.lineSeparator() +
                        "Include fake_include_2" + System.lineSeparator() +
                        System.lineSeparator() +
                        "Context fake {" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        "Context fake {" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator(),
                convert);
    }


    @Test
    public void shouldConvertContextsWithRuleImportsAndNamespace() {
        Resource krakenDSLModel = ResourceBuilder.getInstance()
                .addContextDefinition(context())
                .setNamespace("fake_namespace")
                .addIncludes(Arrays.asList("fake_include_1", "fake_include_2"))
                .addImports(reference("NS1", "R1", "R2", "R3"))
                .build();
        final String convert = converter.convert(krakenDSLModel);
        assertEquals(
                "Namespace fake_namespace" +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        "Include fake_include_1" +
                        System.lineSeparator() +
                        "Include fake_include_2" +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        "Import Rule \"R1\" from NS1" + System.lineSeparator() +
                        "Import Rule \"R2\" from NS1" + System.lineSeparator() +
                        "Import Rule \"R3\" from NS1" + System.lineSeparator() +
                        System.lineSeparator() +
                        "Context fake {" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator(),
                convert);
    }

    @Test
    public void shouldConvertModel() {
        List<RuleImport> imports = new ArrayList<>();
        imports.addAll(reference("NS1", "R1", "R2", "R3"));
        imports.addAll(reference("NS2", "R1", "R2", "R3"));

        Resource resource = ResourceBuilder.getInstance()
                .addContextDefinition(context())
                .addEntryPoint(entryPoint())
                .addRule(rule())
                .setNamespace("fake_namespace")
                .addIncludes(Arrays.asList("fake_include_1", "fake_include_2"))
                .addImports(imports)
                .build();
        final String convert = converter.convert(resource);
        assertEquals(
            "Namespace fake_namespace" + System.lineSeparator() + System.lineSeparator() +
            "Include fake_include_1" + System.lineSeparator() +
            "Include fake_include_2" + System.lineSeparator() + System.lineSeparator() +
            "Import Rule \"R1\" from NS1" + System.lineSeparator() +
            "Import Rule \"R2\" from NS1" + System.lineSeparator() +
            "Import Rule \"R3\" from NS1" + System.lineSeparator() +
            "Import Rule \"R1\" from NS2" + System.lineSeparator() +
            "Import Rule \"R2\" from NS2" + System.lineSeparator() +
            "Import Rule \"R3\" from NS2" + System.lineSeparator() + System.lineSeparator() +
            "EntryPoint \"fake\" {" + System.lineSeparator() +
            "    \"B1\"," + System.lineSeparator() +
            "    \"B2\"," + System.lineSeparator() +
            "    \"B3\"" + System.lineSeparator() +
            "}" + System.lineSeparator() + System.lineSeparator() +
            "Rule \"fake\" On context.path {" + System.lineSeparator() +
            "    Description \"description\"" + System.lineSeparator() +
            "    Priority 99" + System.lineSeparator() +
            "    Assert String expression" + System.lineSeparator() +
            "}" + System.lineSeparator() + System.lineSeparator() +
            "Context fake {" + System.lineSeparator() +
            "}" + System.lineSeparator(),
            convert);
    }

    @Test
    public void shouldConvertModelWithEntryPoint() {
        EntryPoint entryPoint = entryPoint();
        entryPoint.setServerSideOnly(true);

        Resource resource = ResourceBuilder.getInstance()
                .addEntryPoint(entryPoint)
                .build();
        final String convert = converter.convert(resource);
        assertEquals(
                        "@ServerSideOnly" + System.lineSeparator() +
                        "EntryPoint \"fake\" {" + System.lineSeparator() +
                        "    \"B1\"," + System.lineSeparator() +
                        "    \"B2\"," + System.lineSeparator() +
                        "    \"B3\"" + System.lineSeparator() +
                        "}" + System.lineSeparator() + System.lineSeparator(),
                convert);
    }

    @Test
    public void shouldConvertModelWithReusedEntryPoint() {
        EntryPoint entryPointSpecific = factory.createEntryPoint();
        entryPointSpecific.setName("fake specific");
        entryPointSpecific.setIncludedEntryPointNames(Arrays.asList("fake"));
        entryPointSpecific.setRuleNames(Arrays.asList("A1", "A2", "A3"));
        entryPointSpecific.setPhysicalNamespace("whatever");

        Resource resource = ResourceBuilder.getInstance()
                .addEntryPoint(entryPointSpecific)
                .build();
        final String convert = converter.convert(resource);
        assertEquals(
                "EntryPoint \"fake specific\" {" + System.lineSeparator() +
                        "    EntryPoint \"fake\"," + System.lineSeparator() +
                        "    \"A1\"," + System.lineSeparator() +
                        "    \"A2\"," + System.lineSeparator() +
                        "    \"A3\"" + System.lineSeparator() +
                        "}" + System.lineSeparator() + System.lineSeparator(),
                convert);
    }

    private List<RuleImport> reference(String namespaceName, String... importNames) {
        return Arrays.stream(importNames)
                .map(importName -> new RuleImport(namespaceName, importName))
                .collect(Collectors.toList());
    }

    private ContextDefinition context() {
        ContextDefinition ctx = factory.createContextDefinition();
        ctx.setName("fake");
        ctx.setStrict(true);
        ctx.setPhysicalNamespace("whatever");
        return ctx;
    }

    private EntryPoint entryPoint() {
        EntryPoint entryPoint = factory.createEntryPoint();
        entryPoint.setName("fake");
        entryPoint.setRuleNames(Arrays.asList("B1", "B2", "B3"));
        entryPoint.setPhysicalNamespace("whatever");
        return entryPoint;
    }

    private Rule rule() {
        Rule rule = factory.createRule();
        rule.setName("fake");
        rule.setDescription("description");
        rule.setContext("context");
        rule.setTargetPath("path");
        rule.setPriority(99);
        rule.setPhysicalNamespace("whatever");
        AssertionPayload assertionPayload = factory.createAssertionPayload();
        Expression expression = factory.createExpression();
        expression.setExpressionString("String expression");
        assertionPayload.setAssertionExpression(expression);
        rule.setPayload(assertionPayload);
        return rule;
    }
}
