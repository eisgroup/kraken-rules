- [Library](#library)
  - [Function](#function)
  - [GenericFunction](#genericfunction)
- [Simple Library](#simple-library)
  - [SimpleFunction](#simplefunction)


___

# Library

Library description  
**since** 1.0  

## Function

```
Function(Boolean p1, String p2) : Boolean
```

Function description

> Additional info in block

**parameter** p1 - Boolean parameter description  
**parameter** p2 - String parameter description  
**throws error** If error happens  
**examples**
```
Function(true) // ✔ result
Function(null) // ✘
```
**since** 1.0  

## GenericFunction

```
<T is Date | DateTime, N is Number> GenericFunction(<T> p1, <N> p2) : <T>
```

Function description

**parameter** p1 - Generic parameter description  
**parameter** p2 - Generic parameter description  
**since** 1.0  

# Simple Library

## SimpleFunction

```
SimpleFunction() : String
```

