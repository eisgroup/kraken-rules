lexer grammar Common;

LINE_COMMENT    : ('//' ~[\r\n]*) -> channel(HIDDEN);
BLOCK_COMMENT    : ('/*' .*? '*/') -> channel(HIDDEN);

STRING     : '"' (ESC|.)*? '"' | '\'' (ESC|.)*? '\'';

OP_EXP         : '**';
OP_NEGATION    : NOT | '!';
OP_IN          : IN;
OP_MULT        : '*';
OP_DIV         : '/';
OP_MOD         : '%';
OP_MINUS       : '-';
OP_ADD         : '+';
OP_MORE_EQUALS : '>=';
OP_MORE        : '>';
OP_LESS_EQUALS : '<=';
OP_LESS        : '<';
OP_NOT_EQUALS  : '!=';
OP_EQUALS      : '=' | '==';
OP_AND         : AND | '&&';
OP_OR          : OR | '||';
OP_PIPE        : '|';
OP_QUESTION    : '?';
OP_INSTANCEOF  : 'instanceof' | 'INSTANCEOF' | 'InstanceOf' | 'instanceOf';
OP_TYPEOF      : 'typeof' | 'TYPEOF' | 'TypeOf' | 'typeOf';

L_ROUND_BRACKETS : '(';
R_ROUND_BRACKETS : ')';

// pushes and pops defautl mode each time curly brackets are encountered during normal model.
// Used to count when curly brackets starts/ends template. See 'mode TEMPLATE'
L_CURLY_BRACKETS : '{' -> pushMode(DEFAULT_MODE);
R_CURLY_BRACKETS : '}' -> popMode;

L_SQUARE_BRACKETS : '[';
R_SQUARE_BRACKETS : ']';

COMMA : ',';
DOT : '.';
ETA : '@';
COLON : ':';

MATCHES : 'matches' | 'Matches' | 'MATCHES';
NOT : 'not' | 'Not' | 'NOT';
IN : 'in' | 'In' | 'IN';
AND : 'and' | 'And' | 'AND';
OR : 'or' | 'Or' | 'OR';
FOR : 'for' | 'For' | 'FOR';
EVERY : 'every' | 'Every' | 'EVERY';
SOME : 'some' | 'Some' | 'SOME';
RETURN : 'return' | 'Return' | 'RETURN';
SATISFIES : 'satisfies' | 'Satisfies' | 'SATISFIES';
IF : 'if' | 'If' | 'IF';
THEN : 'then' | 'Then' | 'THEN';
ELSE : 'else' | 'Else' | 'ELSE';
NULL : 'null' | 'Null' | 'NULL';
THIS : 'this' | 'This' | 'THIS';

DESCRIPTION : 'description' | 'Description' | 'DESCRIPTION';
RULES : 'rules' | 'Rules' | 'RULES';
RULE : 'rule' | 'Rule' | 'RULE';
ENTRYPOINTS : 'entrypoints' | 'entryPoints' | 'EntryPoints' | 'ENTRYPOINTS';
ENTRYPOINT : 'entrypoint' | 'entryPoint' | 'EntryPoint' | 'ENTRYPOINT';
CONTEXTS : 'contexts' | 'Contexts' | 'CONTEXTS';
CONTEXT : 'context' | 'Context' | 'CONTEXT';
EXTERNALCONTEXT : 'externalcontext' | 'ExternalContext' | 'Externalcontext' | 'externalContext' | 'EXTERNALCONTEXT';
EXTERNALENTITY : 'externalentity' | 'ExternalEntity' | 'Externalentity' | 'externalEntity' | 'EXTERNALENTITY';
PROVIDED : 'provided' | 'Provided' | 'PROVIDED';
BY : 'by' | 'By' | 'BY';
CHILD : 'child' | 'Child' | 'CHILD';
EXTERNAL : 'external' | 'External' | 'EXTERNAL';
WHEN : 'when' | 'When' | 'WHEN';
ASSERT : 'assert' | 'Assert' | 'ASSERT';
LENGTH : 'length' | 'Length' | 'LENGTH';
OVERRIDABLE : 'overridable' | 'Overridable' | 'OVERRIDABLE';
SET : 'set' | 'Set' | 'SET';
IS : 'is' | 'Is' | 'IS';
ON : 'on' | 'On' | 'ON';
TO : 'to' | 'To' | 'TO';
ROOT : 'Root' | 'root' | 'ROOT';
NOTSTRICT : 'notstrict' | 'notStrict' | 'NotStrict' | 'NOTSTRICT';
DIMENSION : 'dimension' | 'Dimension' | 'DIMENSION';
SERVERSIDEONLY : 'serversideonly' | 'serverSideOnly' | 'ServerSideOnly' | 'SERVERSIDEONLY';
NAMESPACE : 'namespace' | 'Namespace' | 'NameSpace' | 'NAMESPACE';
INCLUDE : 'include' | 'Include' | 'INCLUDE';
IMPORT : 'import' | 'Import' | 'IMPORT';
FROM : 'from' | 'From' | 'FROM';
ERROR : 'error' | 'Error' | 'ERROR';
WARN  : 'warn' | 'Warn' | 'WARN';
INFO  : 'info' | 'Info' | 'INFO';
MIN         : 'min' | 'Min' | 'MIN';
MAX         : 'max' | 'Max' | 'MAX';
SIZE        : 'size' | 'Size' | 'SIZE';
EMPTY       : 'empty' | 'Empty' | 'EMPTY';
MANDATORY   : 'mandatory' | 'Mandatory' | 'MANDATORY';
TODISABLED  : 'disabled' | 'Disabled' | 'DISABLED';
TOHIDDEN    : 'hidden' | 'Hidden' | 'HIDDEN';
DEFAULT     : 'default' | 'Default' | 'DEFAULT';
RESET       : 'reset' | 'Reset' | 'RESET';
FUNCTION    : 'function' | 'Function' | 'FUNCTION';

TIME_TOKEN  : DATE_FRAGMENT TIME_FRAGMENT OFFSET_FRAGMENT;
DATE_TOKEN  : DATE_FRAGMENT;
BOOL       : TRUE | FALSE;
TRUE : 'true' | 'True' | 'TRUE';
FALSE : 'false' | 'False' | 'FALSE';
NATURAL    : NATURAL_NUMBER;
REAL       : REAL_NUMBER;
IDENTIFIER : [a-zA-Z_][a-zA-Z0-9_]* ;

WS : [ \t\r\n\u000C] -> channel(HIDDEN);

fragment OFFSET_FRAGMENT : 'Z';
fragment TIME_FRAGMENT  : 'T' DIGIT DIGIT ':' DIGIT DIGIT ':' DIGIT DIGIT;
fragment DATE_FRAGMENT  : DIGIT DIGIT DIGIT DIGIT '-' DIGIT DIGIT '-' DIGIT DIGIT;
fragment REAL_NUMBER    : NATURAL_NUMBER '.' DIGIT*;
fragment NATURAL_NUMBER : [1-9] DIGIT* | '0';
fragment DIGIT          : [0-9] ;
fragment ESC            : '\\"' | '\\\\' | '\\\'' ;

TEMPLATE_START : '`' -> pushMode(TEMPLATE);

mode TEMPLATE;
TEMPLATE_END : '`' -> popMode;
TEMPLATE_EXPRESSION_START : '${' -> pushMode(DEFAULT_MODE);
// template text is defined as a complementary set of characters to match anything except unescaped '${' and '`'
TEMPLATE_TEXT : '$' (TEMPLATE_ESC | ~('{'|'`')) (TEMPLATE_ESC | ~('$'|'`'))*? // any 2+ char seq that starts with $, but not ${
              | '$'                                                           // just the single $
              | (TEMPLATE_ESC | ~('$'|'`'))+                                  // sequence that ends before $ or `
              ;

// allows to escape template syntax symbols ('${' and '`') within template
fragment TEMPLATE_ESC  : '\\\\' | '\\$' | '\\`';