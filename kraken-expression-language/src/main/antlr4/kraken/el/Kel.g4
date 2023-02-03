parser grammar Kel;

import Value;

options {tokenVocab=Common;}

expression : (valueBlock? | template) EOF;

