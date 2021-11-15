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
package kraken.runtime.expressions;

import static kraken.el.functions.TypeProvider.TYPE_PROVIDER_PROPERTY;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.money.MonetaryAmount;

import kraken.el.ExpressionEvaluationException;
import kraken.el.ExpressionLanguage;
import kraken.el.KrakenKel;
import kraken.el.TargetEnvironment;
import kraken.el.accelerated.PropertyExpressionEvaluator;
import kraken.el.math.Numbers;
import kraken.model.context.Cardinality;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.context.data.ExternalDataReference;
import kraken.runtime.model.context.ContextNavigation;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.expression.ExpressionType;
import kraken.runtime.model.rule.payload.validation.ErrorMessage;
import kraken.utils.Assertions;

/**
 * Central point for evaluating Kraken Expressions in Kraken Model.
 * Expressions must be translated for the appropriate Target Environment.
 *
 * @author mulevicius
 */
public class KrakenExpressionEvaluator {

    public static final DateTimeFormatter TEMPLATE_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter TEMPLATE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ExpressionLanguage expressionLanguage;

    private final PropertyExpressionEvaluator propertyExpressionEvaluator;

    public KrakenExpressionEvaluator() {
        this.expressionLanguage = KrakenKel.create(TargetEnvironment.JAVA);

        this.propertyExpressionEvaluator = new PropertyExpressionEvaluator();
    }

    public Object evaluate(CompiledExpression expression, DataContext dataContext, EvaluationSession session) {
        Assertions.assertNotNull(expression, "Expression");
        Assertions.assertNotEmpty(expression.getExpressionString(), "Expression");
        Assertions.assertNotNull(dataContext.getDataObject(), "Data");

        if (expression.getExpressionType() == ExpressionType.LITERAL) {
            return expression.getCompiledLiteralValue();
        }
        if (expression.getExpressionType() == ExpressionType.PATH) {
            return evaluateGetProperty(expression.getExpressionString(), dataContext.getDataObject());
        }

        Map<String, Object> vars = createExpressionVars(session, dataContext);
        return evaluate(expression.getExpressionString(), dataContext.getDataObject(), vars);
    }

    public Object evaluateSetProperty(Object valueToSet, String path, Object dataObject) {
        Assertions.assertNotEmpty(path, "Path");
        Assertions.assertNotNull(dataObject, "Data");

        try {
            expressionLanguage.evaluateSetExpression(valueToSet, path, dataObject);
            return evaluateGetProperty(path, dataObject);
        } catch (ExpressionEvaluationException ex) {
            throw new KrakenExpressionEvaluationException("Error while evaluating set", path, dataObject, ex);
        }
    }

    public Object evaluateNavigationExpression(ContextNavigation contextNavigation, Object dataObject) {
        if(contextNavigation.getNavigationExpression().getExpressionType() == ExpressionType.PATH) {
            return evaluateGetProperty(contextNavigation.getNavigationExpression().getExpressionString(), dataObject);
        }
        return evaluate(contextNavigation.getNavigationExpression().getExpressionString(), dataObject, Collections.emptyMap());
    }

    public Object evaluateGetProperty(String path, Object dataObject) {
        try {
            if(path.indexOf('.') < 0) {
                return propertyExpressionEvaluator.evaluate(path, dataObject);
            }
            return expressionLanguage.evaluate(path, dataObject, Collections.emptyMap());
        } catch (ExpressionEvaluationException ex) {
            throw new KrakenExpressionEvaluationException("Error while evaluating path", path, dataObject, ex);
        }
    }

    public List<String> evaluateTemplateVariables(ErrorMessage message, DataContext context, EvaluationSession session) {
        if(message == null) {
            return List.of();
        }
        return message.getTemplateExpressions().stream()
            .map(e -> {
                try {
                    return evaluate(e, context, session);
                } catch (KrakenExpressionEvaluationException ex) {
                    return null;
                }
            })
            .map(this::render)
            .collect(Collectors.toList());
    }

    private String render(Object object) {
        if(object instanceof MonetaryAmount) {
            return render(((MonetaryAmount) object).getNumber().numberValue(BigDecimal.class));
        }
        if(object instanceof BigDecimal) {
            return Numbers.toString((BigDecimal) object);
        }
        if(object instanceof LocalDate) {
            return ((LocalDate) object).format(TEMPLATE_DATE_FORMAT);
        }
        if(object instanceof LocalDateTime) {
            return ((LocalDateTime) object).format(TEMPLATE_DATE_TIME_FORMAT);
        }
        if(object instanceof Collection) {
            return ((Collection<?>) object).stream()
                .map(this::render)
                .collect(Collectors.joining(", ", "[", "]"));
        }
        return object == null ?  "" : object.toString();
    }

    private Object evaluate(String expression, Object dataObject, Map<String, Object> vars) {
        Assertions.assertNotEmpty(expression, "Expression");
        Assertions.assertNotNull(vars, "Variables");
        Assertions.assertNotNull(dataObject, "Data");

        try {
            return expressionLanguage.evaluate(expression, dataObject, vars);
        } catch (ExpressionEvaluationException ex) {
            throw new KrakenExpressionEvaluationException("Error while evaluating expression", expression, dataObject, ex);
        }
    }

    private static Map<String, Object> createExpressionVars(EvaluationSession session, DataContext dataContext) {
        final HashMap<String, Object> vars = new HashMap<>();
        for(ExternalDataReference reference : dataContext.getExternalReferences().values()) {
            if(reference.getCardinality() == Cardinality.SINGLE) {
                if(reference.getDataContext() != null) {
                    vars.put(reference.getName(), reference.getDataContext().getDataObject());
                }
            } else if(reference.getCardinality() == Cardinality.MULTIPLE) {
                vars.put(reference.getName(), reference.getDataContexts().stream().map(DataContext::getDataObject).collect(Collectors.toList()));
            }
        }

        vars.put("context", session.getExpressionContext());
        vars.put(TYPE_PROVIDER_PROPERTY, session.getKrakenTypeProvider());

        if(dataContext.getContextDefinition() != null) {
            dataContext.getContextDefinition().getInheritedContexts()
                    .forEach(inheritedName -> vars.put(inheritedName, dataContext.getDataObject()));
        }
        vars.put(dataContext.getContextName(), dataContext.getDataObject());
        return vars;
    }

}

