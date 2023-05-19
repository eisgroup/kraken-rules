/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.model.project.gson;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import kraken.model.Condition;
import kraken.model.Dimension;
import kraken.model.DimensionDataType;
import kraken.model.ErrorMessage;
import kraken.model.Expression;
import kraken.model.Function;
import kraken.model.FunctionParameter;
import kraken.model.Metadata;
import kraken.model.Payload;
import kraken.model.Rule;
import kraken.model.ValueList;
import kraken.model.ValueList.DataType;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.context.external.ExternalContextDefinitionAttribute;
import kraken.model.context.external.ExternalContextDefinitionAttributeType;
import kraken.model.context.external.ExternalContextDefinitionReference;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;
import kraken.model.payload.PayloadType;
import kraken.model.project.KrakenProject;
import kraken.model.state.AccessibilityPayload;
import kraken.model.state.VisibilityPayload;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.LengthPayload;
import kraken.model.validation.NumberSetPayload;
import kraken.model.validation.RegExpPayload;
import kraken.model.validation.SizePayload;
import kraken.model.validation.SizeRangePayload;
import kraken.model.validation.UsagePayload;
import kraken.model.validation.ValueListPayload;
import kraken.namespace.Namespaced;
import kraken.utils.Dates;
import kraken.utils.GsonUtils;

/**
 * Kraken {@code Gson} adapter that handles correct serialization and deserialization of Kraken
 * specific types.
 *
 * @author Tomas Dapkunas
 * @since 1.48.0
 */
public final class KrakenGsonAdapter {

    private static final Gson internal = GsonUtils.gson();

    public static class Builder {

        private final KrakenProject krakenProject;
        private boolean prettyPrint;

        public Builder(@Nonnull KrakenProject krakenProject) {
            this.krakenProject = Objects.requireNonNull(krakenProject);
        }

        public Builder setPrettyPrinting() {
            this.prettyPrint = true;
            return this;
        }

        public Gson create() {
            KrakenMetadataAdapter metadataAdapter = new KrakenMetadataAdapter(internal, krakenProject);

            GsonBuilder builder = GsonUtils.builder()
                .registerTypeAdapter(Metadata.class, metadataAdapter)
                .registerTypeAdapter(RulesModelFactory.getInstance().getImplClass(Metadata.class), metadataAdapter)
                .registerTypeAdapter(ErrorMessage.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(Condition.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(Expression.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(ContextDefinition.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(ContextNavigation.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(ContextField.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(ExternalContext.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(ExternalContextDefinition.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(ExternalContextDefinitionReference.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(ExternalContextDefinitionAttribute.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(ExternalContextDefinitionAttributeType.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(EntryPoint.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(Rule.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(Payload.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(DefaultValuePayload.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(AccessibilityPayload.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(VisibilityPayload.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(SizeRangePayload.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(RegExpPayload.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(AssertionPayload.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(SizePayload.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(UsagePayload.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(LengthPayload.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(ValueListPayload.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(ValueList.class, new ValueListAdapter(internal))
                .registerTypeAdapter(NumberSetPayload.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(Function.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(FunctionParameter.class, new KrakenAdapter<>(internal))
                .registerTypeAdapter(URI.class, new UriAdapter());

            if (prettyPrint) {
                builder.setPrettyPrinting();
            }

            return builder.create();
        }

    }

    private static class KrakenAdapter<T> implements JsonDeserializer<T>, JsonSerializer<T> {

        private final Gson internal;

        public KrakenAdapter(Gson internal) {
            this.internal = internal;
        }

        @Override
        public T deserialize(JsonElement json, Type t, JsonDeserializationContext context) throws JsonParseException {
            Class<T> implClass = determineImplementationClass(json, t);
            return context.deserialize(json, implClass);
        }

        private Class<T> determineImplementationClass(JsonElement json, Type t) {
            Class<T> type = (Class<T>) t;
            if (Payload.class.isAssignableFrom(type)) {
                type = (Class<T>) determinePayloadClass(json);
            }
            return RulesModelFactory.getInstance().getImplClass(type);
        }

        private Optional<PayloadType> toPayloadType(String type) {
            return Arrays.stream(PayloadType.values())
                .filter(p -> p.getTypeName().equals(type))
                .findFirst();
        }

        private Class<? extends Payload> determinePayloadClass(JsonElement json) {
            return Optional.ofNullable(json.getAsJsonObject().get("type"))
                .flatMap(typeJson -> toPayloadType(typeJson.getAsString()))
                .map(this::toPayloadClass)
                .orElseThrow(() -> new JsonParseException("payloadType is missing"));
        }

        @Override
        public JsonElement serialize(T src, Type t, JsonSerializationContext context) {
            return internal.toJsonTree(src);
        }

        private Class<? extends Payload> toPayloadClass(PayloadType payloadType) {
            switch (payloadType) {
                case ASSERTION:
                    return AssertionPayload.class;
                case DEFAULT:
                    return DefaultValuePayload.class;
                case USAGE:
                    return UsagePayload.class;
                case REGEX:
                    return RegExpPayload.class;
                case ACCESSIBILITY:
                    return AccessibilityPayload.class;
                case VISIBILITY:
                    return VisibilityPayload.class;
                case LENGTH:
                    return LengthPayload.class;
                case SIZE:
                    return SizePayload.class;
                case SIZE_RANGE:
                    return SizeRangePayload.class;
                case VALUE_LIST:
                    return ValueListPayload.class;
                case NUMBER_SET:
                    return NumberSetPayload.class;
            }

            String availablePayloadTypes = Arrays.stream(PayloadType.values())
                .map(PayloadType::getTypeName)
                .collect(Collectors.joining(", "));

            throw new JsonParseException(
                "Cannot determine payload type: " + payloadType + ". " +
                    "PayloadType must be one of: " + availablePayloadTypes
            );
        }
    }

    private static class UriAdapter implements JsonDeserializer<URI>, JsonSerializer<URI> {
        @Override
        public URI deserialize(JsonElement json, Type t, JsonDeserializationContext context) throws JsonParseException {
            return URI.create(json.getAsString());
        }

        @Override
        public JsonElement serialize(URI src, Type t, JsonSerializationContext context) {
            return context.serialize(src.toString());
        }
    }

    private static class ValueListAdapter implements JsonDeserializer<ValueList>, JsonSerializer<ValueList> {

        private final Gson gson;

        public ValueListAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public ValueList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

            if (!json.isJsonObject()) {
                throw new IllegalArgumentException("ValueList must be an object");
            }

            JsonObject valueListObject = json.getAsJsonObject();

            JsonPrimitive valueType = mandatoryGet(valueListObject, "valueType", JsonElement::getAsJsonPrimitive);
            JsonArray values = mandatoryGet(valueListObject, "values", JsonElement::getAsJsonArray);

            DataType dataType = DataType.getDataType(valueType.getAsString());

            if (dataType == null) {
                throw new JsonParseException("Unable to find DataType for field type : " + values.getAsString());
            }

            switch (dataType) {
                case DECIMAL:
                    List<Number> numberValues = IntStream.range(0, values.size())
                        .mapToObj(index -> values.get(index).getAsJsonPrimitive())
                        .map(jsonPrimitive -> {
                            if (jsonPrimitive.isNumber()) {
                                return jsonPrimitive.getAsBigDecimal();
                            }

                            throw new JsonParseException("Value : " + jsonPrimitive +
                                " cannot be converter to required type: " + dataType);
                        })
                        .collect(Collectors.toList());

                    return ValueList.fromNumber(numberValues);
                case STRING:
                    List<String> stringValues = IntStream.range(0, values.size())
                        .mapToObj(index -> values.get(index).getAsJsonPrimitive())
                        .map(jsonPrimitive -> {
                            if (jsonPrimitive.isString()) {
                                return jsonPrimitive.getAsString();
                            }

                            throw new JsonParseException("Value : " + jsonPrimitive +
                                " cannot be converter to required type: " + dataType);
                        })
                        .collect(Collectors.toList());

                    return ValueList.fromString(stringValues);
                default:
                    throw new JsonParseException("Not support value list data type: " + dataType);
            }
        }

        private <R> R mandatoryGet(JsonObject object,
                                   String memberName,
                                   java.util.function.Function<JsonElement, R> converter) {
            if (!object.has(memberName)) {
                throw new JsonParseException("Unable to resolve '" + memberName + "' value");
            }

            try {
                return converter.apply(object.get(memberName));
            } catch (Exception e) {
                throw new JsonParseException("Unable to convert '" + memberName + "' value '"
                    + object.get(memberName) + "' to required type.", e);
            }
        }

        @Override
        public JsonElement serialize(ValueList src, Type typeOfSrc, JsonSerializationContext context) {
            return gson.toJsonTree(src);
        }
    }

    private static class KrakenMetadataAdapter implements JsonDeserializer<Metadata>, JsonSerializer<Metadata> {

        private final Gson gson;
        private final Map<String, DimensionDataType> dimensionDataTypes;

        public KrakenMetadataAdapter(Gson gson, KrakenProject krakenProject) {
            this.gson = gson;
            this.dimensionDataTypes = krakenProject.getDimensions().stream()
                .collect(Collectors.toMap(Namespaced::getName, Dimension::getDataType));
        }

        @Override
        public Metadata deserialize(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context
        ) throws JsonParseException {
            Metadata metadata = RulesModelFactory.getInstance().createMetadata();
            if (!json.isJsonObject()) {
                throw new IllegalArgumentException("Metadata in rule must be an object");
            }
            JsonObject metadataJson = json.getAsJsonObject().getAsJsonObject("properties");
            for (Map.Entry<String, JsonElement> entry : metadataJson.entrySet()) {
                JsonElement value = entry.getValue();
                if (!value.isJsonPrimitive()) {
                    throw new IllegalArgumentException("Metadata value in rule must be primitive");
                }
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                String key = entry.getKey();
                DimensionDataType dataType = dimensionDataTypes.get(key);

                if (dataType != null) {
                    metadata.setProperty(key, convertToType(primitive, dataType));
                } else {
                    if (primitive.isBoolean()) {
                        metadata.setProperty(key, primitive.getAsBoolean());
                    } else if (primitive.isNumber()) {
                        metadata.setProperty(key, primitive.getAsBigDecimal());
                    } else if (primitive.isString()) {
                        metadata.setProperty(key, primitive.getAsString());
                    }
                }
            }
            return metadata;
        }

        private Object convertToType(JsonPrimitive element, DimensionDataType type) {
            switch (type) {
                case DATE:
                    if (StringUtils.isEmpty(element.getAsString())) {
                        return null;
                    }

                    return Dates.convertISOToLocalDate(element.getAsString());
                case DATETIME:
                    if (StringUtils.isEmpty(element.getAsString())) {
                        return null;
                    }

                    return Dates.convertISOToLocalDateTime(element.getAsString());
                case INTEGER:
                    return element.getAsInt();
                case DECIMAL:
                    return element.getAsBigDecimal();
                case STRING:
                    return element.getAsString();
                case BOOLEAN:
                    return element.getAsBoolean();
                default:
                    throw new IllegalArgumentException("Unable to convert element to data type. "
                        + "Not supported type encountered: " + type);
            }
        }

        @Override
        public JsonElement serialize(Metadata src, Type typeOfSrc, JsonSerializationContext context) {
            return gson.toJsonTree(src);
        }
    }

}
