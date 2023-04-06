parser grammar Value;

options {tokenVocab=Common;}

valueBlock : valueWithVariables | value;
// validation of missing 'RETURN', 'value' and 'TO' cases are handled semantically to not depend on ANTLR auto recovery
valueWithVariables : variable+ RETURN? value? | RETURN value?;
variable : SET identifier TO? value?;

value : L_ROUND_BRACKETS value R_ROUND_BRACKETS        #Precedence
      | <assoc=right> value op=OP_EXP value            #Exponent
      | op=OP_MINUS value                              #Negative
      | op=OP_NEGATION value                           #Negation
      | value op=OP_IN value                           #In
      | value op=MATCHES STRING                        #MatchesRegExp
      | value op=(OP_MULT|OP_DIV|OP_MOD) value         #MultiplicationOrDivision
      | value op=(OP_MINUS|OP_ADD) value               #SubtractionOrAddition
      | value op=(OP_MORE|OP_MORE_EQUALS|OP_LESS|OP_LESS_EQUALS) value             #NumericalComparison
      | value op=(OP_INSTANCEOF | OP_TYPEOF) identifier #TypeComparison
      | value op=(OP_EQUALS|OP_NOT_EQUALS) value       #EqualityComparison
      | value op=OP_AND value                          #Conjunction
      | value op=OP_OR value                           #Disjunction
      | L_CURLY_BRACKETS valueList? R_CURLY_BRACKETS   #InlineArray
      | L_CURLY_BRACKETS keyValuePairs R_CURLY_BRACKETS #InlineMap
      | IF condition=value
        THEN thenExpression=valueBlock
        (ELSE elseExpression=valueBlock)?              #IfValue
      | reference                                      #ReferenceValue
      | opStart=FOR var=identifier
        opIn=OP_IN collection=value
        opReturn=RETURN returnExpression=valueBlock    #ForEach
      | opStart=EVERY var=identifier
        opIn=OP_IN collection=value
        opReturn=SATISFIES returnExpression=valueBlock #ForEvery
      | opStart=SOME var=identifier
        opIn=OP_IN collection=value
        opReturn=SATISFIES returnExpression=valueBlock #ForSome
      | literal                                        #LiteralValue
      ;

type : identifier                                 #PlainType
     | L_ROUND_BRACKETS type R_ROUND_BRACKETS     #PlainTypePrecedence
     | type L_SQUARE_BRACKETS R_SQUARE_BRACKETS   #ArrayType
     | type OP_PIPE type                          #UnionType
     | OP_LESS identifier OP_MORE                 #GenericType
     ;

template           : TEMPLATE_START templateText* TEMPLATE_END ;
templateText       : TEMPLATE_TEXT | templateExpression;
templateExpression : TEMPLATE_EXPRESSION_START value? R_CURLY_BRACKETS;

indexValue :
      L_ROUND_BRACKETS indexValue R_ROUND_BRACKETS    #PrecedenceValue
     | reference                                      #ReferenceValueValue
     | literal                                        #LiteralValueValue
     | value op=(OP_MINUS|OP_ADD) value               #SubtractionOrAdditionValue
     | value op=(OP_MULT|OP_DIV|OP_MOD) value         #MultiplicationOrDivisionValue
     | op=OP_MINUS value                              #NegativeValue
     | <assoc=right> value op=OP_EXP value            #ExponentValue
     ;

valuePredicate :
       L_ROUND_BRACKETS valuePredicate R_ROUND_BRACKETS        #PrecedencePredicate
     | opStart=EVERY var=identifier
       opIn=OP_IN collection=value
       opReturn=SATISFIES returnExpression=valueBlock          #ForEveryPredicate
     | opStart=SOME var=identifier
       opIn=OP_IN collection=value
       opReturn=SATISFIES returnExpression=valueBlock          #ForSomePredicate
     | value op=OP_OR value                                    #DisjunctionPredicate
     | value op=OP_AND value                                   #ConjunctionPredicate
     | value op=(OP_EQUALS|OP_NOT_EQUALS) value                #EqualityComparisonPredicate
     | value op=(OP_INSTANCEOF | OP_TYPEOF) identifier         #TypeComparisonPredicate
     | value op=(OP_MORE|OP_MORE_EQUALS|OP_LESS|OP_LESS_EQUALS) value   #NumericalComparisonPredicate
     | value op=MATCHES STRING                                 #MatchesRegExpPredicate
     | value op=OP_IN value                                    #InPredicate
     | op=OP_NEGATION value                                    #NegationPredicate
     ;

reference : L_ROUND_BRACKETS reference R_ROUND_BRACKETS        #ReferencePrecedence
         | identifier                                          #IdentifierReference
         | functionCall                                        #Function
         | collection=reference indices                        #AccessByIndex
         | object=reference pathSeparator property=reference   #Path
         // optional path property to allow incomplete path to parse
         | object=reference pathSeparator                      #IncompletePath
         | filterCollection=reference (predicate | asterix)    #Filter
         | L_ROUND_BRACKETS type R_ROUND_BRACKETS reference    #Cast
         | thisValue                                           #This
         ;

pathSeparator : DOT | QDOT;

// optional brackets and predicate to allow incomplete filters to parse
predicate : L_SQUARE_BRACKETS valuePredicate? R_SQUARE_BRACKETS?
          | OP_QUESTION L_SQUARE_BRACKETS value? R_SQUARE_BRACKETS?
          ;

// optional brackets and predicate to allow incomplete access by index to parse
indices : L_SQUARE_BRACKETS indexValue? R_SQUARE_BRACKETS?;

asterix : L_SQUARE_BRACKETS OP_MULT R_SQUARE_BRACKETS;

functionCall : (functionName=identifier) L_ROUND_BRACKETS (arguments=valueList)? R_ROUND_BRACKETS ;

valueList : value (COMMA value)*;
keyValuePairs : keyValuePair (COMMA keyValuePair)*;
keyValuePair : key=STRING COLON value;

thisValue : THIS;

literal : DATE_TOKEN                                  #Date
        | TIME_TOKEN                                  #DateTime
        | BOOL                                        #Boolean
        | positiveDecimalLiteral                      #Decimal
        | STRING                                      #String
        | NULL                                        #Null
        ;

positiveDecimalLiteral : REAL | positiveIntegerLiteral;
positiveIntegerLiteral : NATURAL;

identifier : IDENTIFIER | krakenModelReservedWord;

krakenModelReservedWord : krakenModelReservedWordNoChild | CHILD;
krakenModelReservedWordNoChild : DESCRIPTION
                               | ENTRYPOINT
                               | ENTRYPOINTS
                               | SYSTEM
                               | CONTEXT
                               | CONTEXTS
                               | EXTERNAL
                               | FORBID_TARGET
                               | FORBID_REFERENCE
                               | SERVERSIDEONLY
                               | NOTSTRICT
                               | ROOT
                               | NAMESPACE
                               | FUNCTION
                               | RULES
                               | RULE
                               | DIMENSION
                               | ERROR
                               | WARN
                               | INFO
                               | MANDATORY
                               | EMPTY
                               | TODISABLED
                               | TOHIDDEN
                               | DEFAULT
                               | RESET
                               | OVERRIDABLE
                               | MIN
                               | MAX
                               | STEP
                               | NUMBER
                               | SIZE
                               | WHEN
                               | ASSERT
                               | LENGTH
                               | ON
                               | TO
                               | INCLUDE
                               | IMPORT
                               | FROM
                               | SET
                               ;

kelReservedWord : THIS
                | IF
                | THEN
                | ELSE
                | FOR
                | EVERY
                | SOME
                | RETURN
                | SATISFIES
                | OP_INSTANCEOF
                | OP_TYPEOF
                | IN
                | NOT
                | AND
                | OR
                | MATCHES
                | SET
                | TO
                ;
