parser grammar KrakenDSL;

import Value;

options {tokenVocab=Common;}

kraken : namespace? anImport* model* EOF;

namespace : NAMESPACE namespaceName;
namespaceName : IDENTIFIER (DOT IDENTIFIER)*;

anImport : namespaceImport
         | ruleImport
         ;
namespaceImport : INCLUDE namespaceName;
ruleImport : IMPORT RULE ruleNames FROM namespaceName;

model : contexts
      | context
      | externalContext
      | externalContextDefinition
      | rules
      | aRule
      | entryPoints
      | entryPoint
      | functionSignature
      | functionImplementation
      | dimension
      ;

contexts : CONTEXTS L_CURLY_BRACKETS (context | contexts)* R_CURLY_BRACKETS;
context : modeledContext | systemContext;
modeledContext: (ETA NOTSTRICT)? ROOT? CONTEXT contextName inheritedContexts? L_CURLY_BRACKETS (child | field)* R_CURLY_BRACKETS;
inheritedContexts : IS contextName (COMMA contextName)*;
systemContext: SYSTEM CONTEXT contextName L_CURLY_BRACKETS field* R_CURLY_BRACKETS;
externalContext : EXTERNALCONTEXT L_CURLY_BRACKETS externalContextItems? R_CURLY_BRACKETS;
externalContextItems : externalContextItem (COMMA externalContextItem)*;
externalContextItem : externalContextNodeItem | externalContextEntityItem;
externalContextEntityItem : key=modelIdentifier COLON externalContextDefinitionName=contextName;
externalContextNodeItem : key=modelIdentifier COLON L_CURLY_BRACKETS externalContextItems R_CURLY_BRACKETS;
externalContextDefinition : EXTERNALENTITY contextName L_CURLY_BRACKETS (externalContextField)* R_CURLY_BRACKETS;
contextName : modelIdentifier;
externalContextField : fieldType OP_MULT? fieldName=modelIdentifier;
field : fieldModifiers? EXTERNAL? fieldType OP_MULT? fieldName=modelIdentifier (COLON pathExpression)?;
fieldModifiers : (ETA fieldModifier)+;
fieldModifier : FORBID_TARGET | FORBID_REFERENCE;

child : childModifiers? CHILD OP_MULT? contextName (COLON inlineExpression)?;
childModifiers : (ETA childModifier)+;
childModifier : FORBID_REFERENCE;

rules : annotations? RULES L_CURLY_BRACKETS (aRule | rules)* R_CURLY_BRACKETS;
aRule : annotations?
        RULE ruleName ON contextName DOT pathExpression L_CURLY_BRACKETS
          ruleDescription?
          rulePriority?
          ruleCondition?
          payload
        R_CURLY_BRACKETS;
rulePriority: PRIORITY (integerLiteral | MIN | MAX);
ruleDescription : DESCRIPTION description=STRING;
ruleName : STRING;
ruleCondition : WHEN inlineExpression;
payload : usagePayload
        | defaultValuePayload
        | accessibilityPayload
        | visibilityPayload
        | regExpPayload
        | lengthPayload
        | sizePayload
        | sizeRangePayload
        | assertionPayload
        | numberSetPayload
        | valueListPayload
        ;
usagePayload : SET MANDATORY payloadMessage? override?   #Mandatory
             | ASSERT EMPTY payloadMessage? override?    #Empty
             ;
assertionPayload : ASSERT inlineExpression payloadMessage? override?;
defaultValuePayload : DEFAULT TO inlineExpression       #Default
                    | RESET TO inlineExpression         #Reset
                    ;
accessibilityPayload : SET TODISABLED;
visibilityPayload : SET TOHIDDEN;
regExpPayload : ASSERT MATCHES regExp=STRING payloadMessage? override?;
sizePayload : ASSERT SIZE (MIN | MAX)? size=positiveIntegerLiteral payloadMessage? override?;
sizeRangePayload : ASSERT SIZE MIN sizeFrom=positiveIntegerLiteral MAX sizeTo=positiveIntegerLiteral payloadMessage? override?;
lengthPayload : ASSERT LENGTH length=positiveIntegerLiteral payloadMessage? override?;
numberSetPayload : ASSERT NUMBER (minNumber | maxNumber | (minNumber maxNumber)) stepNumber? payloadMessage? override?;
minNumber: MIN decimalLiteral;
maxNumber: MAX decimalLiteral;
stepNumber: STEP positiveDecimalLiteral;
valueListPayload : ASSERT OP_IN (decimalValues | stringValues) payloadMessage? override?;
override : OVERRIDABLE group=STRING?;
payloadMessage : (ERROR | WARN | INFO) code=STRING (COLON message=STRING)?;

entryPoints : annotations? ENTRYPOINTS L_CURLY_BRACKETS (entryPoint | entryPoints)* R_CURLY_BRACKETS;
entryPoint : annotations? ENTRYPOINT entryPointName=STRING L_CURLY_BRACKETS entryPointItems? R_CURLY_BRACKETS;
entryPointItems : entryPointItem (COMMA entryPointItem)*;
entryPointItem : ruleName | ENTRYPOINT entryPointName=STRING;
ruleNames : ruleName (COMMA ruleName)*;
decimalValues : decimalLiteral (COMMA decimalLiteral)*;
stringValues : STRING (COMMA STRING)*;
dimensionName: STRING;
dimension: DIMENSION dimensionName COLON dimensionType=modelIdentifier;

functionImplementation : functionDoc?
                         FUNCTION genericBounds? functionName=modelIdentifier
                         L_ROUND_BRACKETS functionParameters? R_ROUND_BRACKETS returnType
                         L_CURLY_BRACKETS
                           functionBody=inlineExpression
                         R_CURLY_BRACKETS;
returnType : COLON type;
functionParameters : functionParameter (COMMA functionParameter)*;
functionParameter : parameterType=type parameterName=modelIdentifier;
functionSignature : FUNCTION genericBounds? functionName=modelIdentifier
                    L_ROUND_BRACKETS functionSignatureParameters? R_ROUND_BRACKETS functionSignatureReturnType;
functionSignatureReturnType : COLON type;
functionSignatureParameters : functionSignatureParameter (COMMA functionSignatureParameter)*;
functionSignatureParameter : type;
functionDoc : DOCS_COMMENT;

genericBounds : OP_LESS genericBound (COMMA genericBound)* OP_MORE;
genericBound : generic=modelIdentifier IS bound=type;

annotations: annotationEntry+;
annotationEntry : metadataEntry | serverSideOnlyEntry;

serverSideOnlyEntry : ETA SERVERSIDEONLY;

metadataEntry : ETA DIMENSION L_ROUND_BRACKETS metadataKey=inlineExpression COMMA metadataValue=inlineExpression R_ROUND_BRACKETS;

pathExpression : modelIdentifier (DOT modelIdentifier)*;

inlineExpression : valueBlock;

modelIdentifier : IDENTIFIER | krakenModelReservedWord | kelReservedWord;

fieldType : IDENTIFIER | krakenModelReservedWordNoChild | kelReservedWord;

integerLiteral : OP_MINUS? positiveIntegerLiteral;
decimalLiteral : OP_MINUS? positiveDecimalLiteral;
