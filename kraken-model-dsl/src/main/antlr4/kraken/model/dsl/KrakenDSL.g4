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
      | functionSignature;

contexts : CONTEXTS L_CURLY_BRACKETS (context | contexts)* R_CURLY_BRACKETS;
context : (ETA NOTSTRICT)? ROOT? CONTEXT contextName inheritedContexts? L_CURLY_BRACKETS (child | field)* R_CURLY_BRACKETS;

externalContext : EXTERNALCONTEXT L_CURLY_BRACKETS externalContextItems? R_CURLY_BRACKETS;
externalContextItem : extContextNodeItem | extContextEntityItem;
extContextNodeItem : key=identifier COLON L_CURLY_BRACKETS externalContextItems R_CURLY_BRACKETS;
extContextEntityItem : key=identifier COLON externalContextDefinitionName=identifier;
externalContextItems : externalContextItem (COMMA externalContextItem)*;
externalContextDefinition : EXTERNALENTITY contextName L_CURLY_BRACKETS (externalContextField)* R_CURLY_BRACKETS;
inheritedContexts : IS contextName (COMMA contextName)*;
contextName : identifier;
externalContextField : fieldType OP_MULT? fieldName=identifier;
field : EXTERNAL? fieldType OP_MULT? fieldName=identifier (COLON pathExpression)?;

child : CHILD OP_MULT? contextName (COLON inlineExpression)?;

rules : annotations? RULES L_CURLY_BRACKETS (aRule | rules)* R_CURLY_BRACKETS;
aRule : annotations? RULE ruleName ON contextName DOT pathExpression L_CURLY_BRACKETS (DESCRIPTION ruleDescription)? ruleCondition? payload R_CURLY_BRACKETS;
ruleDescription : STRING;
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
        ;
usagePayload : SET MANDATORY payloadMessage? override?   #Mandatory
             | ASSERT EMPTY payloadMessage? override?    #Empty
             ;
assertionPayload : ASSERT inlineExpression payloadMessage? override?;
defaultValuePayload : DEFAULT TO inlineExpression       #Default
                    | RESET TO inlineExpression         #Reset
                    | RESET                             #ResetToNull
                    ;
accessibilityPayload : SET TODISABLED;
visibilityPayload : SET TOHIDDEN;
regExpPayload : ASSERT MATCHES regExp=STRING payloadMessage? override?;
sizePayload : ASSERT SIZE (MIN | MAX)? size=positiveIntegerLiteral payloadMessage? override?;
sizeRangePayload : ASSERT SIZE MIN sizeFrom=positiveIntegerLiteral MAX sizeTo=positiveIntegerLiteral payloadMessage? override?;
lengthPayload : ASSERT LENGTH length=positiveIntegerLiteral payloadMessage? override?;
override : OVERRIDABLE group=STRING?;
payloadMessage : (ERROR | WARN | INFO) code=STRING (COLON message=STRING)?;

entryPoints : annotations? ENTRYPOINTS L_CURLY_BRACKETS (entryPoint | entryPoints)* R_CURLY_BRACKETS;
entryPoint : annotations? ENTRYPOINT entryPointName=STRING L_CURLY_BRACKETS entryPointItems? R_CURLY_BRACKETS;
entryPointItems : entryPointItem (COMMA entryPointItem)*;
entryPointItem : ruleName | ENTRYPOINT entryPointName=STRING;
ruleNames : ruleName (COMMA ruleName)*;

functionSignature : FUNCTION functionName=identifier L_ROUND_BRACKETS parameters? R_ROUND_BRACKETS COLON returnType=type;
parameters : parameter (COMMA parameter)*;
parameter : type;

annotations: annotationEntry+;
annotationEntry : metadataEntry | serverSideOnlyEntry;

serverSideOnlyEntry : ETA SERVERSIDEONLY;

metadataEntry : ETA DIMENSION L_ROUND_BRACKETS metadataKey=inlineExpression COMMA metadataValue=inlineExpression R_ROUND_BRACKETS;

pathExpression : identifier (DOT identifier)*;

inlineExpression : value;

fieldType : IDENTIFIER | reservedWordNoChild;
