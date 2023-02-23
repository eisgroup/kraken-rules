- [Collection](#collection)
  - [Count](#count)
  - [Flat](#flat)
  - [Join](#join)
- [Date](#date)
  - [AsDate](#asdate)
  - [AsTime](#astime)
  - [Date](#date)
  - [Date](#date)
  - [DateTime](#datetime)
  - [Format](#format)
  - [GetDay](#getday)
  - [GetMonth](#getmonth)
  - [GetYear](#getyear)
  - [IsDateBetween](#isdatebetween)
  - [Now](#now)
  - [NumberOfDaysBetween](#numberofdaysbetween)
  - [NumberOfMonthsBetween](#numberofmonthsbetween)
  - [NumberOfYearsBetween](#numberofyearsbetween)
  - [PlusDays](#plusdays)
  - [PlusMonths](#plusmonths)
  - [PlusYears](#plusyears)
  - [Today](#today)
  - [WithDay](#withday)
  - [WithMonth](#withmonth)
  - [WithYear](#withyear)
- [Generic Value](#generic-value)
  - [IsEmpty](#isempty)
- [Math](#math)
  - [Abs](#abs)
  - [Avg](#avg)
  - [Ceil](#ceil)
  - [Floor](#floor)
  - [Max](#max)
  - [Max](#max)
  - [Min](#min)
  - [Min](#min)
  - [NumberSequence](#numbersequence)
  - [NumberSequence](#numbersequence)
  - [Round](#round)
  - [Round](#round)
  - [RoundEven](#roundeven)
  - [RoundEven](#roundeven)
  - [Sign](#sign)
  - [Sqrt](#sqrt)
  - [Sum](#sum)
- [Money](#money)
  - [FromMoney](#frommoney)
- [Policy](#policy)
  - [CalculateTotalLimit](#calculatetotallimit)
  - [ConcatInsuredNameWithCreditCardInfo](#concatinsurednamewithcreditcardinfo)
  - [ConcatPartyRelations](#concatpartyrelations)
  - [Fibonacci](#fibonacci)
  - [GenericCoverageLimit](#genericcoveragelimit)
  - [GenericCoverageLimits](#genericcoveragelimits)
  - [GetCoverages](#getcoverages)
  - [GetName](#getname)
  - [MinLimit](#minlimit)
  - [MinLimitCoverages](#minlimitcoverages)
- [Quantifier](#quantifier)
  - [All](#all)
  - [Any](#any)
- [Set](#set)
  - [Difference](#difference)
  - [Distinct](#distinct)
  - [Intersection](#intersection)
  - [SymmetricDifference](#symmetricdifference)
  - [Union](#union)
- [String](#string)
  - [Concat](#concat)
  - [EndsWith](#endswith)
  - [Includes](#includes)
  - [IsBlank](#isblank)
  - [LowerCase](#lowercase)
  - [NumberToString](#numbertostring)
  - [PadLeft](#padleft)
  - [PadRight](#padright)
  - [StartsWith](#startswith)
  - [StringLength](#stringlength)
  - [Substring](#substring)
  - [Substring](#substring)
  - [Trim](#trim)
  - [UpperCase](#uppercase)
- [Type](#type)
  - [GetType](#gettype)
- [TypeAnnotationTestFunctions](#typeannotationtestfunctions)
  - [GetCarCoverage](#getcarcoverage)
  - [GetFirstElement](#getfirstelement)
  - [GetVehicleModel](#getvehiclemodel)


___

# Collection

Functions for operating with collections.  
**since** 1.0.28  

## Count

```
Count(Any collection) : Number
```

Resolve size of collection. If collection is `null`, returns 0. If argument is not a collection, returns 1.

**parameter** collection - collection if items to count  
**examples**
```
Count(coverages) // ✔
Count(null) // ✔ 0
Count('abc') // ✔ 1
```
**since** 1.0.28  

## Flat

```
<T is Any> Flat(<T>[][] collection) : <T>[]
```

Flatten collection of collections to a one-dimensional collection.

**parameter** collection - collection of collections  
**examples**
```
Flat({{'a', 'b'}, {'c', 'd'}}) // ✔ {'a', 'b', 'c', 'd'}
Flat(null) // ✔ null
```
**since** 1.0.28  

## Join

```
<T is Any> Join(<T>[] collection1, <T>[] collection2) : <T>[]
```

Join two collections into single collection by preserving order from the original collections and by preserving duplicates. Parameter types in both collections must be the same. Joining collections of different types is not supported. `null` value in collection is treated as a valid value and is preserved. `null` collection parameter is treated as an empty collection.

**parameter** collection1 - First collection to join  
**parameter** collection2 - Second collection to join  
**examples**
```
Join(RiskItem.coverages.coverageCd, Policy.coverages.coverageCd) // ✔
```
**since** 1.0.30  

# Date

Functions for operating with Date and DateTime values.  
**since** 1.0.28  

## AsDate

```
AsDate(DateTime time) : Date
```

Convert datetime to date by discarding time.

  
**examples**
```
AsDate(2011-11-11T10:10:10Z) // ✔
AsDate(txEffectiveDate) // ✔
```
**since** 1.0.28  

## AsTime

```
AsTime(Date date) : DateTime
```

Convert date to datetime by adding zero time.

  
**examples**
```
AsTime(2011-11-11) // ✔
AsTime(termEffectiveDate) // ✔
```
**since** 1.0.28  

## Date

```
Date(String dateString) : Date
```

Create a date from ISO String.

> Be aware when creating date-time without explicit time zone, date-time will be created with a working machine time zone shift. For UI engine it is browser timezone, for server it is server timezone. It may cause different behaviour depending on execution runtime.
It is not recommended to create a date-time without a time zone as `2021-01-01T01:01:01`. It is recommended to create date-time with a time zone as `2021-01-01T01:01:01Z` instead. Using this function with the string containing time zone will have deterministic behaviour.

**parameter** dateString - ISO string  
**throws error** if string does not match date pattern 'YYYY-MM-DD'  
**examples**
```
Date('2011-11-11') // ✔
Date('abc') // ✘
Date(null) // ✘
```
**since** 1.0.28  

## Date

```
Date(Number year, Number month, Number day) : Date
```

Create a date from the year, month, and day.

  
  
  
**throws error** if any of parameters is not a number  
**examples**
```
Date(2011, 12, 31) // ✔
```
**since** 1.0.28  

## DateTime

```
DateTime(String dateTimeString) : DateTime
```

Create a datetime from ISO String.

**parameter** dateTimeString - ISO string  
**throws error** if string does not match datetime pattern 'YYYY-MM-DDThh:mm:ssZ'  
**examples**
```
DateTime('2011-11-11T10:10:10Z') // ✔
DateTime('abc') // ✘
DateTime(null) // ✘
```
**since** 1.0.28  

## Format

```
Format(Date date, String format) : String
```

Formats date according to the format passed as a parameter. Format can contain Y, M and D characters to denote year, month or day respectively.

**parameter** date - to parse to string  
**parameter** format - if null is passed, then default format is "YYYY-MM-DD"  
**throws error** if pattern is invalid  
**examples**
```
Format(2012-11-10, "YYYY/MM/DD") // ✔ 2112/11/10
Format(2012-11-10, null) // ✔ 2012-11-10
Format(null, "MM/YY/DD") // ✘
Format(null, "abc") // ✘
```
**since** 1.0.28  

## GetDay

```
GetDay(Date | DateTime dateOrDatetime) : Number
```

Get the day of the month from date or date time.

**parameter** dateOrDatetime - number of day in month starting from 1  
**throws error** if parameter is not Date or DateTime  
**examples**
```
GetDay(2011-11-11) // ✔ 11
GetDay(2011-11-11T01:01:01Z) // ✔ 11
GetDay(null) // ✘
GetDay(1) // ✘
```
**since** 1.0.28  

## GetMonth

```
GetMonth(Date | DateTime dateOrDatetime) : Number
```

Get the month number from date or date time.

**parameter** dateOrDatetime - number of day in month starting from 1  
**throws error** if parameter is not Date or DateTime  
**examples**
```
GetMonth(2011-11-11) // ✔ 11
GetMonth(null) // ✘
GetMonth(1) // ✘
```
**since** 1.0.28  

## GetYear

```
GetYear(Date | DateTime dateOrDatetime) : Number
```

Get the year number from date or datetime.

**parameter** dateOrDatetime - number of day in month starting from 1  
**throws error** if parameter is not Date or DateTime  
**examples**
```
GetYear(2011-11-11) // ✔ 11
GetYear(null) // ✘
GetYear(1) // ✘
```
**since** 1.0.28  

## IsDateBetween

```
IsDateBetween(Date date, Date start, Date end) : Boolean
```

Check if the date is between two dates

**parameter** date - date to be between dates  
**parameter** start - start of the range  
**parameter** end - end of the range  
**throws error** if any of parameters is not a number  
**examples**
```
IsDateBetween(AsDate(TermDetails.termEffectiveDate), 2011-11-11, Today()) // ✔
IsDateBetween(2011-10-01, 2011-12-01, 2011-11-11) // ✔ true
IsDateBetween(null, 2011-12-01, 2011-11-11) // ✘
IsDateBetween(2011-10-01, null, 2011-11-11) // ✘
IsDateBetween(2011-10-01, 2011-12-01, null) // ✘
```
**since** 1.0.28  

## Now

```
Now() : DateTime
```

Resolve to the current time.

> This function is NOT idempotent and will not produce the same result if called multiple times. For example, if this function is used in a default value rule to reset the value to the current date it will change to a new date if a rule is executed multiple times on different dates and as a side, the result will result in changes to a data image.

**since** 1.0.28  

## NumberOfDaysBetween

```
NumberOfDaysBetween(Date date1, Date date2) : Number
```

Calculate days between two dates.The output of this function is always positive, it doesn't matter which parameter is a start date.

  
  
**examples**
```
NumberOfDaysBetween(2011-11-11, 2011-11-12) // ✔ 1
NumberOfDaysBetween(2011-11-11, 2011-11-10) // ✔ 1
```
**since** 1.0.34  

## NumberOfMonthsBetween

```
NumberOfMonthsBetween(Date date1, Date date2) : Number
```

Calculate months between two dates.The output of this function is always positive, it doesn't matter which parameter is a start date.

  
  
**examples**
```
NumberOfMonthsBetween(2011-11-11, 2011-12-11) // ✔ 1
NumberOfMonthsBetween(2011-11-11, 2011-12-10) // ✔ 0
NumberOfMonthsBetween(2020-01-30, 2020-02-29) // ✔ 0
```
**since** 1.0.34  

## NumberOfYearsBetween

```
NumberOfYearsBetween(Date date1, Date date2) : Number
```

Calculate years between two dates.The output of this function is always positive, it doesn't matter which parameter is a start date.

  
  
**examples**
```
NumberOfYearsBetween(2010-11-11, 2011-11-11) // ✔ 1
NumberOfYearsBetween(2010-11-11, 2011-11-10) // ✔ 0
NumberOfYearsBetween(2020-02-29, 2021-02-28) // ✔ 0
```
**since** 1.0.34  

## PlusDays

```
<T is Date | DateTime> PlusDays(<T> dateOrDatetime, Number numberOfDays) : <T>
```

Add or remove days from date or datetime.

**parameter** dateOrDatetime - Date or DateTime typeof parameter  
**parameter** numberOfDays - can be positive or negative number  
**examples**
```
PlusDays(2011-11-11, 1) // ✔ 2011-11-12
PlusDays(2011-11-11, -1) // ✔ 2011-11-10
PlusDays(2011-11-11T01:01:01Z, 1) // ✔ 2011-11-12T01:01:01Z
PlusDays('abc', 1) // ✘
PlusDays(1, 1) // ✘
PlusDays(null, 1) // ✘
```
**since** 1.0.28  

## PlusMonths

```
<T is Date | DateTime> PlusMonths(<T> dateOrDatetime, Number numberOfMonths) : <T>
```

Add or remove months from the date or datetime. If the day of month is invalid for the year, then it is changed to the last valid day of the month.

**parameter** dateOrDatetime - Date or DateTime typeof parameter  
**parameter** numberOfMonths - can be positive or negative number  
**throws error** if parameter is not Date or DateTime  
**examples**
```
PlusMonths(2011-11-11, 1) // ✔ 2011-12-11
PlusMonths(2011-11-11, -1) // ✔ 2011-10-11
PlusMonths(2011-11-11T01:01:01Z, 1) // ✔ 2011-12-11T01:01:01Z
PlusMonths('abc', 1) // ✘
PlusMonths(1, 1) // ✘
PlusMonths(null, 1) // ✘
```
**since** 1.0.28  

## PlusYears

```
<T is Date | DateTime> PlusYears(<T> dateOrDatetime, Number numberOfYears) : <T>
```

Add or remove years from the date or datetime. If the day of month is invalid for the year, then it is changed to the last valid day of the month.

**parameter** dateOrDatetime - Date or DateTime typeof parameter  
**parameter** numberOfYears - can be positive or negative number  
**throws error** if first parameter is not Date  
**examples**
```
PlusYears(2011-11-11, 1) // ✔ 2012-11-11
PlusYears(2011-11-11, -1) // ✔ 2010-11-11
PlusYears(2011-11-11T01:01:01Z, 1) // ✔ 2012-11-11T01:01:01Z
PlusYears('abc', 1) // ✘
PlusYears(1, 1) // ✘
PlusYears(null, 1) // ✘
```
**since** 1.0.28  

## Today

```
Today() : Date
```

Resolve to the current date

> This function is NOT idempotent and will not produce the same result if called multiple times. For example, if this function is used in a default value rule to reset the value to the current date it will change to a new date if a rule is executed multiple times on different dates and as a side, the result will result in changes to a data image.

**since** 1.0.28  

## WithDay

```
<T is Date | DateTime> WithDay(<T> value, Number day) : <T>
```

Returns Date or DateTime with modified day of month value. Day must be a valid day of month starting from 1.

  
  
**throws error** if parameter type is not Date or DateTime, or if month does not have such a day.  
**examples**
```
WithDay(2011-11-11, 1) // ✔ 2011-11-01
WithDay(2011-11-11, 0) // ✘
WithDay(2000-02-02, 30) // ✘
```
**since** 1.30.0  

## WithMonth

```
<T is Date | DateTime> WithMonth(<T> value, Number month) : <T>
```

Returns Date or DateTime with modified month value. Month value must be between 1 (January) and 12 (December).If the day of month is invalid for the year, then it is changed to the last valid day of the month.

  
  
**throws error** if parameter type is not Date or DateTime, or if month value is smaller than 1 or larger than 12.  
**examples**
```
WithMonth(2011-11-11, 1) // ✔ 2011-12-11
WithMonth(2000-01-30, 2) // ✔ 2000-02-29
WithMonth(2011-11-11, 0) // ✘
```
**since** 1.30.0  

## WithYear

```
<T is Date | DateTime> WithYear(<T> value, Number year) : <T>
```

Returns Date or DateTime with modified year value. Year value must be between 1 and 9999. If the day of month is invalid for the year, then it is changed to the last valid day of the month.

  
  
**throws error** Throws error if parameter type is not Date or DateTime.  
**examples**
```
WithYear(2011-11-11, 2000) // ✔ 2000-11-11
WithYear(2000-02-29, 2001) // ✔ 2001-02-28
```
**since** 1.30.0  

# Generic Value

Functions that operate with general values.  
**since** 1.0.30  

## IsEmpty

```
IsEmpty(Any stringOrCollection) : Boolean
```

Return `true` if collection parameter value is `null` or is empty collection. `null` value in collection is treated as a valid value and the collection is considered not empty. Handles string and collections. Otherwise will return false.

  
**examples**
```
IsEmpty(null) // ✔ true
IsEmpty({}) // ✔ true
IsEmpty({1}) // ✔ false
IsEmpty('') // ✔ true
IsEmpty('a') // ✔ false
```
**since** 1.0.30  

# Math

Functions that operate with numerical values.  
**since** 1.0.28  

## Abs

```
Abs(Number number) : Number
```

Calculates absolute number.

  
**examples**
```
Abs(-5) // ✔ 5
Abs(5) // ✔ 5
```
**since** 1.5.0  

## Avg

```
Avg(Number[] collection) : Number
```

Find average value from all values provided in the collection and return a single value. Can find average only from numbers. When collection is null or empty, returns `null`.

  
**examples**
```
Avg({1,2,3}}) // ✔ 2
Avg({}) // ✔ null
Avg(null) // ✔ null
Avg({1, 2, null}) // ✘
Avg(Coverage.limitAmount) // ✔
```
**since** 1.0.28  

## Ceil

```
Ceil(Number number) : Number
```

Calculates a result of a mathematical ceiling function which returns the least integer that is greater than or equal to provided number.

  
**examples**
```
Ceil(1.1) // ✔ 2
Ceil(-1.9) // ✔ -1
```
**since** 1.5.0  

## Floor

```
Floor(Number number) : Number
```

Calculates a result of a mathematical floor function which returns greatest integer that is less than or equal to provided number.

  
**examples**
```
Floor(1.9 // ✔ 1
Floor(-1.9 // ✔ 1
```
**since** 1.5.0  

## Max

```
<T is Number | Date | DateTime> Max(<T>[] collection) : <T>
```

Finds the largest value between all values in the collection. Collection must contain Number, Date or DateTime values. If collection is null or empty, then this function returns `null`.

  
**throws error** If Collection contains value other than Number, Date or DateTime.  
**examples**
```
Max({1,2}) // ✔ 2
Max({}) // ✔ null
Max(null) // ✔ null
Max({2020-01-01, 2021-01-01}) // ✔ 2021-01-01
Max({2020-01-01, 1}) // ✘
Max({1, 2, null}) // ✘
```
**since** 1.0.28  

## Max

```
<T is Number | Date | DateTime> Max(<T> first, <T> second) : <T>
```

Returns larger of the two numbers, dates or times.

  
  
**examples**
```
Max(1, 2) // ✔ 2
Max(2020-01-01, 2021-01-01) // ✔ 2021-01-01
```
**since** 1.5.0  

## Min

```
<T is Number | Date | DateTime> Min(<T>[] collection) : <T>
```

Finds the smallest value between all values in the collection. Collection must contain Number, Date or DateTime values. If collection is null or empty, then this function returns `null`.

  
**throws error** If collection contains value other than Number, Date or DateTime.  
**examples**
```
Min({1,2}) // ✔ 1
Min({}) // ✔ null
Min(null) // ✔ null
Min({2020-01-01, 2021-01-01}) // ✔ 2020-01-01
Min({2020-01-01, 1}) // ✘
Min({1, 2, null}) // ✘
```
**since** 1.0.28  

## Min

```
<T is Number | Date | DateTime> Min(<T> first, <T> second) : <T>
```

Returns smaller of the two numbers.

  
  
**examples**
```
Min(1, 2) // ✔ 1
Min(2020-01-01, 2021-01-01) // ✔ 2020-01-01
```
**since** 1.5.0  

## NumberSequence

```
NumberSequence(Number from, Number to) : Number[]
```

Generates a sequence of numbers by adding 1 or -1 to the starting number until it reaches the ending number inclusively. Returns a collection of numbers in order. Parameters cannot be `null`.

> This function physically generates each number and returns a collection will every generated number. This is NOT an abstract representation of a mathematical sequence and should not be used for that. Generating very long sequences will negatively effect performance and will require large amounts of memory.
>
> Primary use case of this function is to generate a list of numerical values to be displayed in a drop down for the user therefore this functions is designed for generating a countable amount of numbers.

  
  
**examples**
```
NumberSequence(0, 5) // ✔ {0,1,2,3,4,5}
```
**since** 1.0.28  

## NumberSequence

```
NumberSequence(Number from, Number to, Number step) : Number[]
```

Generates a sequence of numbers by adding 1 or -1 to the starting number until it reaches the ending number inclusively. Returns a collection of numbers in order. Parameters cannot be `null`.

> This function physically generates each number and returns a collection will every generated number. This is NOT an abstract representation of a mathematical sequence and should not be used for that. Generating very long sequences will negatively effect performance and will require large amounts of memory.
>
> Primary use case of this function is to generate a list of numerical values to be displayed in a drop down for the user therefore this functions is designed for generating a countable amount of numbers.

**parameter** from - sequence starting number  
**parameter** to - sequence end number  
**parameter** step - number to add each step  
**throws error** if step is positive and from->to parameters are going negative and vice versa  
**examples**
```
NumberSequence(0, 5) // ✔ {0,1,2,3,4,5}
NumberSequence(5, 0) // ✔ {5,4,3,2,1,0}
NumberSequence(0, 5) // ✔ {0}
```
**since** 1.0.35  

## Round

```
Round(Number number) : Number
```

Rounds to Integer using IEEE 754 Round Half Up strategy. If the number falls midway, then it is rounded to the nearest value above (for positive numbers) or below (for negative numbers).

  
**examples**
```
Round(1.5) // ✔ 2
Round(-1.5) // ✔ -2
Round(2.5) // ✔ 3
Round(-2.5) // ✔ -3
```
**since** 1.5.0  

## Round

```
Round(Number number, Number scale) : Number
```

Rounds to scale using IEEE 754 Round Half Up strategy. If the number falls midway, then it is rounded to the nearest value above (for positive numbers) or below (for negative numbers).

  
**parameter** scale - Indicates how many floating point digits should remain after rounding. If scale is zero, then number will be rounded to integer  
**examples**
```
Round(11.555, 2) // ✔ 11.56
Round(-11.555, 2) // ✔ -11.56
Round(11.565, 2) // ✔ 11.57
Round(-11.565, 2) // ✔ -11.57
```
**since** 1.5.0  

## RoundEven

```
RoundEven(Number number) : Number
```

Rounds to Integer using IEEE 754 Round Half Even strategy. If the number falls midway, then it is rounded to the nearest value with an even least significant digit.

  
**examples**
```
RoundEven(1.5) // ✔ 2
RoundEven(-1.5) // ✔ -2
RoundEven(2.5) // ✔ 2
RoundEven(-2.5) // ✔ -2
```
**since** 1.5.0  

## RoundEven

```
RoundEven(Number number, Number scale) : Number
```

Rounds to Integer using IEEE 754 Round Half Even strategy. If the number falls midway, then it is rounded to the nearest value with an even least significant digit.

  
**parameter** scale - Indicates how many floating point digits should remain after rounding. If scale is zero, then number will be rounded to integer  
**examples**
```
RoundEven(11.555, 2) // ✔ 11.56
RoundEven(-11.555, 2) // ✔ -11.56
RoundEven(11.565, 2) // ✔ 11.56
RoundEven(-11.565, 2) // ✔ -11.56
```
**since** 1.5.0  

## Sign

```
Sign(Number number) : Number
```

Calculates sign of a number. Returns 1 when number is positive, 0 when number is zero and -1 when number is negative.

  
**examples**
```
Sign(100) // ✔ 1
Sign(0) // ✔ 0
Sign(-100) // ✔ -1
```
**since** 1.5.0  

## Sqrt

```
Sqrt(Number number) : Number
```

Calculates squared root of a number..

  
**examples**
```
Sqrt(1) // ✔ 2
```
**since** 1.5.0  

## Sum

```
Sum(Number[] collection) : Number
```

Sum all values in collection and return single value. Can sum only numbers. When collection is `null` or empty, returns `null`.

  
**throws error** if value in collection is not a number  
**examples**
```
Sum({1, 2, 3}) // ✔ 6
Sum({}) // ✔ null
Sum(null) // ✔ null
Sum({1, 2, null}) // ✘
Sum(Coverage.limitAmount) // ✔
```
**since** 1.0.28  

# Money

Functions that operate with Money values.  
**since** 1.0.28  

## FromMoney

```
FromMoney(Money monetaryAmount) : Number
```

Coerces money type to number. Can be used with dynamic context, when data type is no known in compile time

  
**examples**
```
FromMoney(context.preRevision.limitAmount) // ✔
FromMoney(null) // ✔ null
```
**since** 1.0.28  

# Policy

## CalculateTotalLimit

```
CalculateTotalLimit(Policy policy) : Number
```

  

## ConcatInsuredNameWithCreditCardInfo

```
ConcatInsuredNameWithCreditCardInfo(Insured insured, CreditCardInfo credit) : String
```

  
  

## ConcatPartyRelations

```
ConcatPartyRelations(Party[] parties) : String
```

  

## Fibonacci

```
Fibonacci(Number n) : Number
```

Calculates n-th number of a fibonacci sequence.

**parameter** n - number of a fibonacci sequence, starting from 0  
**examples**
```
Fibonacci(0) // ✔ 0
Fibonacci(1) // ✔ 1
Fibonacci(6) // ✔ 8
```
**since** 1.33.0  

## GenericCoverageLimit

```
GenericCoverageLimit(CarCoverage coverage) : Number
```

  

## GenericCoverageLimits

```
GenericCoverageLimits(CarCoverage[] coverages) : Number[]
```

  

## GetCoverages

```
GetCoverages(Policy policy) : CarCoverage[]
```

  

## GetName

```
GetName(Any object) : String
```

  

## MinLimit

```
MinLimit(Policy policy) : Number
```

  

## MinLimitCoverages

```
MinLimitCoverages(CarCoverage[] coverages) : Number
```

  

# Quantifier

Functions that provide logical quantification operations.  
**since** 1.0.30  

## All

```
All(Boolean[] collection) : Boolean
```

Returns `true` if collection is empty or if all boolean values in collection are `true`. `null` value in collection is treated as not `true` value. `null` collection parameter is treated as an empty collection.

**parameter** collection - collection of booleans  
**examples**
```
All({true, false}) // ✔ false
All({true, true}) // ✔ true
All({false, false}) // ✔ false
All({null}) // ✔ false
All({}) // ✔ false
All(Policy.riskItems[*].isPrimary) // ✔
```
**since** 1.0.30  

## Any

```
Any(Boolean[] collection) : Boolean
```

Returns `true` if collection is not empty and if any boolean value in collection is `true`. `null` value in collection is treated as not `true` value. `null` collection parameter is treated as an empty collection.

**parameter** collection - collection of booleans  
**examples**
```
Any({true, false}) // ✔ true
Any({false, false}) // ✔ false
Any({null}) // ✔ false
Any({}) // ✔ false
Any(Policy.riskItems[*].isPrimary) // ✔
```
**since** 1.0.30  

# Set

Functions that provide Set operations.  
**since** 1.0.30  

## Difference

```
<T is Any> Difference(<T>[] collection1, <T>[] collection2) : <T>[]
```

Create a set difference between two collections. Parameter types in both collections must be the same and must be primitive types. Joining collections of different types or of complex object types is not supported. `null` value in collection is treated as a valid value and is preserved. `null` collection parameter is treated as an empty collection.

**parameter** collection1 - `null` collection parameter is treated as an empty collection  
**parameter** collection2 - `null` collection parameter is treated as an empty collection  
**examples**
```
Difference({1,2}, {2,3}) // ✔ {1}
Difference({1,2}, null) // ✔ {1,2}
Difference(null, {1,2}) // ✔ {1,2}
Difference(RiskItem.coverages[*].coverageCd, Policy.coverages[*].coverageCd) // ✔
```
**since** 1.0.30  

## Distinct

```
<T is Any> Distinct(<T>[] collection) : <T>[]
```

Discard duplicates. Order of items in original collection is not preserved. Parameter type in collection must be primitive type. `null` value in collection is treated as a valid value and is preserved. `null` collection parameter is treated as an empty collection.

**parameter** collection - `null` collection parameter is treated as an empty collection  
**examples**
```
Distinct({1,2,2,3,3,3}) // ✔ {1,2,3}
Distinct(null) // ✔ {}
Distinct({null}) // ✔ {null}
```
**since** 1.0.30  

## Intersection

```
<T is Any> Intersection(<T>[] collection1, <T>[] collection2) : <T>[]
```

Create a set intersection between two collections. Parameter types in both collections must be the same and must be primitive types. Joining collections of different types or of complex object types is not supported. `null` value in collection is treated as a valid value and is preserved. `null` collection parameter is treated as an empty collection.

**parameter** collection1 - `null` collection parameter is treated as an empty collection  
**parameter** collection2 - `null` collection parameter is treated as an empty collection  
**examples**
```
Intersection({1,2}, {2,3}) // ✔ {2}
Intersection({1,2}, null) // ✔ {} // empty collection
Intersection(null, {1,2}) // ✔ {} // empty collection
Intersection(RiskItem.coverages[*].coverageCd, Policy.coverages[*].coverageCd) // ✔
```
**since** 1.0.30  

## SymmetricDifference

```
<T is Any> SymmetricDifference(<T>[] collection1, <T>[] collection2) : <T>[]
```

Create a set symmetric difference between two collections. Parameter types in both collections must be the same and must be primitive types. Joining collections of different types or of complex object types is not supported. `null` value in collection is treated as a valid value and is preserved. `null` collection parameter is treated as an empty collection.

**parameter** collection1 - `null` collection parameter is treated as an empty collection  
**parameter** collection2 - `null` collection parameter is treated as an empty collection  
**examples**
```
SymmetricDifference({1,2}, {2,3}) // ✔ {1,3}
SymmetricDifference({1,2}, {2,3,null}) // ✔ {1,3,null}
SymmetricDifference({1,2}, null) // ✔ {1,2}
SymmetricDifference(null, {1,2}) // ✔ {1,2}
SymmetricDifference(RiskItem.coverages[*].coverageCd, Policy.coverages[*].coverageCd) // ✔
```
**since** 1.0.30  

## Union

```
<T is Any> Union(<T>[] collection1, <T>[] collection2) : <T>[]
```

Create a set union between two collections. Parameter types in both collections must be the same and must be primitive types. Joining collections of different types or of complex object types is not supported. `null` value in collection is treated as a valid value and is preserved. `null` collection parameter is treated as an empty collection.

**parameter** collection1 - `null` collection parameter is treated as an empty collection  
**parameter** collection2 - `null` collection parameter is treated as an empty collection  
**examples**
```
Union({1,2}, {2,3}) // ✔ {1,2,3}
Union({1,2}, null) // ✔ {1,2}
Union(null, {1,2}) // ✔ {1,2}
Union(RiskItem.coverages[*].coverageCd, Policy.coverages[*].coverageCd) // ✔
```
**since** 1.0.30  

# String

Functions that operate with String values.  
**since** 1.0.28  

## Concat

```
Concat(Any[] items) : String
```

Concatenate objects to one string.

  
**examples**
```
Concat({"a", "b", "c", 1}) // ✔ 'abc1'
Concat(null) // ✔ null
Concat({'a', null, 'c'}) // ✔ 'ac'
```
**since** 1.0.28  

## EndsWith

```
EndsWith(String text, String end) : Boolean
```

Check string to end with any text. If text to check is shorter than subtext, return `false`.

**parameter** text - to check  
**parameter** end - to be at the end of the text  
**examples**
```
EndsWith("123abc", "abc") // ✔ true
EndsWith("bc", "abc") // ✔ false
```
**since** 1.0.28  

## Includes

```
Includes(Any collectionOrString, Any searchElement) : Boolean
```

Check for element to be included in collection or string to contain string. If null appears in as a collection or element to search `false` be returned.

**parameter** collectionOrString - collection or string  
**parameter** searchElement - element to search in string or collection. If first parameter is string, then this parameter must be a string, otherwise type must match collection elements type  
**throws error** if element is not a string or a collection  
**examples**
```
Includes('abcd', 'cd') // ✔ true
Includes({1, 2, 3}, 2) // ✔ true
Includes({'a', 'b', 'c'}, 'b') // ✔ true
Includes(null, 2) // ✔ false
Includes('abc', null) // ✔ false
Includes(123, null) // ✘
Includes(Policy, null) // ✘
Includes(false, null) // ✘
```
**since** 1.0.28  

## IsBlank

```
IsBlank(String text) : Boolean
```

Return `true` if string parameter is `null`, empty string or consists only of whitespace characters.

  
**examples**
```
IsBlank(null) // ✔ true
IsBlank('') // ✔ true
IsBlank(' ') // ✔ true
IsBlank('test') // ✔ false
```
**since** 1.0.28  

## LowerCase

```
LowerCase(String arg0) : String
```

Convert text to lower case. If text is `null`, return `null`.

  
**examples**
```
LowerCase('AbC') // ✔ 'abc'
LowerCase(null) // ✔ null
```
**since** 1.0.28  

## NumberToString

```
NumberToString(Number number) : String
```

Create a string from number. If `null` is passed then return empty string.

  
**examples**
```
NumberToString(2) // ✔ '2'
NumberToString(null) // ✔ null
```
**since** 1.0.28  

## PadLeft

```
PadLeft(String base, String filler, Number length) : String
```

Pad string on the left side if it's shorter than length. If it is longer than length, return base string as is.

**parameter** base - string to pad. `null` equals to an empty string `''`  
**parameter** filler - to fill the space to desired length. String of one char is acceptable. Null equals to ' '  
**parameter** length - length of final string, if `null` equals to `0`  
**throws error** if filler string has more than one character  
**examples**
```
PadLeft('1', '0', '4') // ✔ 0001
PadLeft('1', ' ', '4') // ✔    1
PadLeft('1', null, '4') // ✔    1
PadLeft('1', 'abc', '4') // ✘
```
**since** 1.0.28  

## PadRight

```
PadRight(String base, String filler, Number length) : String
```

Pad string on the right side if it's shorter than length. If it is longer than length, return base string as is.

**parameter** base - string to pad. `null` equals to an empty string `''`  
**parameter** filler - to fill the space to desired length. String of one char is acceptable. Null equals to ' '  
**parameter** length - length of final string, if `null` equals to `0`  
**throws error** if filler string has more than one character  
**examples**
```
PadRight('1', '0', '4') // ✔ 1000
PadRight('1', ' ', '4') // ✔ 1    
PadRight('1', null, '4') // ✔ 1    
PadRight('1', 'abc', '4') // ✘
```
**since** 1.0.28  

## StartsWith

```
StartsWith(String text, String start) : Boolean
```

Check the string to start with any text. If text to check is shorter than subtext, return `false`.

**parameter** text - to check  
**parameter** start - to be at the start of the text  
**examples**
```
StartsWith("abc123", "abc") // ✔ true
StartsWith("abc123", "123") // ✔ false
```
**since** 1.0.28  

## StringLength

```
StringLength(String text) : Number
```

Calculate string length. If string is `null`, returns `0`

  
**examples**
```
StringLength('four') // ✔ 4
StringLength(null) // ✔ 0
```
**since** 1.0.28  

## Substring

```
Substring(String text, Number beginIndex) : String
```

Return string that is a substring of provided value. The substring begins at that specified beginIndex and extends to the end of provided value.

  
  
**throws error** if index is less than 0 or more than a string length  
**examples**
```
Substring('abc', 2) // ✔ 'c'
Substring('abc', -1) // ✘
Substring('abc', 5) // ✘
```
**since** 1.0.28  

## Substring

```
Substring(String text, Number beginIndex, Number endIndex) : String
```

Return string that is a substring of provided value. The substring begins at specified beginIndex and extends to the character at endIndex - 1.

  
  
  
**throws error** if beginIndex and endIndex are less than 0 or more than a string length  
**examples**
```
Substring('smiles', 1, 5) // ✔ mile
Substring('smiles', -1, 5) // ✘
Substring('smiles', 5, 10) // ✘
Substring('smiles', 10, 5) // ✘
```
**since** 1.0.28  

## Trim

```
Trim(String text) : String
```

Remove leading and trailing whitespaces. If text is `null`, return null`.

  
**examples**
```
Trim(' a ') // ✔ 'a'
Trim(null) // ✔ null
```
**since** 1.0.28  

## UpperCase

```
UpperCase(String text) : String
```

Convert text to upper case. If text is `null`, return `null`.

  
**examples**
```
UpperCase('abc') // ✔ 'ABC'
UpperCase(null) // ✔ null
```
**since** 1.0.28  

# Type

Functions that operate with types of values and objects.  
**since** 1.0.34  

## GetType

```
GetType(Any entity) : String
```

Returns type of object as a string. If the object is `null` then return `null`. If the object does not have a type then `null` is returned. The function only returns the type of entity object. The function does not return types of native types like String, Number, or Boolean.

**parameter** entity - Returns type of object as a string. If the object is `null` or the object does not have a type then `null` is returned. The function only returns the type of entity object. The function does not return types of native types like String, Number, or Boolean.  
**examples**
```
GetType(Coverage) // ✔ RRCoverage
for c in Policy.blob.lobs[*].riskItems[*].coverages return GetType(c) // ✔ {"RRCoverage", "MEDCoverage", "RACoverage"}
GetType(5) // ✔ null
```
**since** 1.0.34  

# TypeAnnotationTestFunctions

## GetCarCoverage

```
GetCarCoverage(Policy arg0) : CarCoverage
```

  

## GetFirstElement

```
GetFirstElement(String[] arg0) : String
```

  

## GetVehicleModel

```
GetVehicleModel(Vehicle arg0) : String
```

  
