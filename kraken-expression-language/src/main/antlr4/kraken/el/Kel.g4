parser grammar Kel;

import Value;

options {tokenVocab=Common;}

expression : value? EOF;
