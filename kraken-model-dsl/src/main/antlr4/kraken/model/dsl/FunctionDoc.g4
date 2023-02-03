grammar FunctionDoc;

doc : description tags EOF;

tags : (since | example | parameter | unrecognized)*;

description : text;
since : SINCE WS version=text;
example : (EXAMPLE | INVALID_EXAMPLE) WS exampleText=text (EXAMPLE_RESULT WS result=text)?
        ;
parameter : PARAMETER WS parameterName=IDENTIFIER WS DASH parameterDescription=text;
unrecognized : ETA IDENTIFIER text;

text : (TEXT | DASH | IDENTIFIER | WS)*;

ETA  : '@';
DASH : '-';

SINCE           : ETA 'since';
EXAMPLE         : ETA 'example';
EXAMPLE_RESULT  : ETA 'result';
INVALID_EXAMPLE : ETA 'invalidExample';
PARAMETER       : ETA 'parameter';

IDENTIFIER : [a-zA-Z_][a-zA-Z0-9_]*;
WS         : [ ]+;
TEXT       : ~[@ -]+;
