parser grammar Value;

options {tokenVocab=Common;}

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
        THEN thenExpression=value
        (ELSE elseExpression=value)?                   #IfValue
      | THIS                                           #This
      | (THIS DOT)? reference                          #ReferenceValue
      | FOR var=identifier
        OP_IN collection=value
        RETURN returnExpression=value                  #ForEach
      | EVERY var=identifier
        OP_IN collection=value
        SATISFIES returnExpression=value               #ForEvery
      | SOME var=identifier
        OP_IN collection=value
        SATISFIES returnExpression=value               #ForSome
      | literal                                        #LiteralValue
      | template                                       #TemplateValue
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
     | THIS                                           #ThisValue
     | (THIS DOT)? reference                          #ReferenceValueValue
     | literal                                        #LiteralValueValue
     | value op=(OP_MINUS|OP_ADD) value               #SubtractionOrAdditionValue
     | value op=(OP_MULT|OP_DIV|OP_MOD) value         #MultiplicationOrDivisionValue
     | op=OP_MINUS value                              #NegativeValue
     | <assoc=right> value op=OP_EXP value            #ExponentValue
     ;

valuePredicate :
       L_ROUND_BRACKETS valuePredicate R_ROUND_BRACKETS        #PrecedencePredicate
     | EVERY var=identifier
       OP_IN collection=value
       SATISFIES returnExpression=value                        #ForEveryPredicate
     | SOME var=identifier
       OP_IN collection=value
       SATISFIES returnExpression=value                        #ForSomePredicate
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
         | L_ROUND_BRACKETS type R_ROUND_BRACKETS reference    #Cast
         | identifier                                          #IdentifierReference
         | functionCall                                        #Function
         | collection=reference indices                        #AccessByIndex
         | object=reference DOT property=reference             #Path
         | filterCollection=reference (predicate | asterix)    #Filter
         ;

predicate : L_SQUARE_BRACKETS valuePredicate R_SQUARE_BRACKETS
          | OP_QUESTION L_SQUARE_BRACKETS value R_SQUARE_BRACKETS
          ;

indices : L_SQUARE_BRACKETS indexValue R_SQUARE_BRACKETS;
asterix : L_SQUARE_BRACKETS OP_MULT R_SQUARE_BRACKETS;

functionCall : (functionName=identifier) L_ROUND_BRACKETS (arguments=valueList)? R_ROUND_BRACKETS ;

valueList : value (COMMA value)*;
keyValuePairs : keyValuePair (COMMA keyValuePair)*;
keyValuePair : key=STRING COLON value;

literal : DATE_TOKEN                                  #Date
        | TIME_TOKEN                                  #DateTime
        | BOOL                                        #Boolean
        | decimalLiteral                              #Decimal
        | STRING                                      #String
        | NULL                                        #Null
        ;

decimalLiteral : REAL | integerLiteral;
integerLiteral : positiveIntegerLiteral;
positiveIntegerLiteral : NATURAL;

identifier : IDENTIFIER | reservedWord;
reservedWord : reservedWordNoChild | CHILD;
reservedWordNoChild : DESCRIPTION | RULES | RULE | ENTRYPOINT | ENTRYPOINTS | CONTEXT | CONTEXTS | EXTERNAL | THIS
             | WHEN | ASSERT | MATCHES | LENGTH | SET | IS | ON | TO | NOT | IN | AND | OR
             | ERROR | WARN | INFO | MANDATORY | EMPTY | TODISABLED
             | TOHIDDEN | DEFAULT | RESET | DIMENSION | SERVERSIDEONLY | OVERRIDABLE | MIN | MAX | SIZE
             | IF | THEN | ELSE | NOTSTRICT | NAMESPACE | INCLUDE | IMPORT | FROM
             | FOR | EVERY | SOME | RETURN | SATISFIES | OP_INSTANCEOF | OP_TYPEOF | FUNCTION;