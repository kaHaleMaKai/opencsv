# opencsv wrapper [![travis-ci.org](https://travis-ci.org/kaHaleMaKai/opencsv.svg?branch=master)](https://travis-ci.org/kaHaleMaKai/opencsv/branches) [![codecov.io](https://codecov.io/github/kaHaleMaKai/opencsv/coverage.svg?branch=master)](https://codecov.io/gh/kaHaleMaKai/opencsv/branch/master) [ ![Download](https://api.bintray.com/packages/kahalemakai/maven/opencsv/images/download.svg) ](https://bintray.com/kahalemakai/maven/opencsv/_latestVersion)

easily parse csv data into java beans

---

The [opencsv](http://opencsv.sourceforge.net) library is a great and high performance tool 
for parsing csvs. In more recent version, it includes a mapper from csv data to java beans, and vice versa.

Unfortunetely, it can only be configured programmatically. The conversion of csv columns into (boxed) 
primitive bean fields works out of the box, but relies on reflection and doesn't cache e.g. setter lookups efficiently.
Converting fields into non-standard objects is a burden, as one must use custom `PropertyEditor`s and figure out
how to inject them properly.

The great parsing/ETL framework [smooks](https://github.com/smooks/smooks) uses opencsv for csv data sets.
However, it relies on version 2.3 from 2011, which is fairly old. Furthermore, smooks is
by design most apt for xml/hierarchical data formats. For csvs, it has too much overhead. Also, smooks
only offers to emit parsed beans as a complete list, and not line-wise e.g. as an iterator. For huge data sets,
this is a major drawback. Nevertheless, smooks offers an easy to use xml configuration, which eases 
development by large.

Enter this wrapper. It builds just a thin layer around opencsv and provides an xml-based configuration
inspired by smooks. The benefits are:
* xml configuration
* a builder class for programmatic access
* high throughput by avoiding or caching reflextive lookups as much as possible
* supply a simple data-to-field decoding approach, similar to smooks
* output an iterator of beans

## examples

Assuming the input data
```
age,name,points
49,John,17.6
31,Alfred,33.756
```

and a bean class in the namespace `com.example`
```java
class Person {
    private int age;
    private String name;
    private double points;
    // getters, setters...
}
```
, we can setup the convertsion with an xml configuration as follows:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<opencsv:resources xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://kahalemakai.github.io/schemas/opencsv/0.3.7/opencsv.xsd"
                   xmlns:opencsv="http://github.com/kaHaleMaKai/opencsv"
                   xmlns:csv="http://github.com/kaHaleMaKai/opencsv/csv"
                   xmlns:bean="http://github.com/kaHaleMaKai/opencsv/bean">

    <csv:reader quoteChar="," skipLines="1">
        <csv:column>age</column>
        <csv:column>name</column>
        <csv:column>points</column>
    </csv:reader>

    <bean:config class="com.example.Person">
        <bean:field name="age" type="int"/>
        <bean:field name="name" />
        <bean:field name="points" type="double"/>
    </bean:config>

</opencsv:resources>
```

In your code, you would use it like that:
```java
ConfigParser configParser = ConfigParser.ofFile(new File("your-config.xml"), new File("your.csv"));
CsvToBeanMapper<Person> mapper = configParser.parse();
for (Person person : mapper) { ... }
```

Or, you set it up programmatically:
```java
CsvToBeanMapper mapper = CsvToBeanMapper
        .builder(Person.class)
        .addDecoder("age", IntDecoder.class)
        .addDecoder("points", DoubleDecoder.class)
        .separator(',')
        withFile(new File("your.csv"))
        .build();
for (Person person : mapper) { ... }
```
The more verbose configuration is the trade-off for avoiding reflection.

For both xml and programmatic configuration, different input sources of csv data 
can be chosen such as `InputStream`, `Reader`, or `Iterable`.

The `CsvToBeanMapper` instance automatically uses the first column
as header. If this behaviour is undesired, the header can be set explicitely:
```java
CsvToBeanMapper.setHeader("age", "name", "points")
```

## xml configuration

Currently, only two basic building blocks are supported: the `<csv:reader>` tag
and the `bean:config` tag.
The setup of the parser is done in the first tag. A complete example using the default values
would be
```xml
<csv:reader separator="," 
            skipLines="0" 
            quoteChar="&apos;" 
            ignoreLeadingWhiteSpace="true"
            onErrorSkipLine="false"
            quotingBehaviour="non-strict"
            charset="UTF-8">
    <csv:column>name</csv:column>
    <csv:ignore />
    <csv:column>time</csv:column>
    <csv:ignore count="24" />
    <csv:column>end</csv:column>
</csv:reader>
```

This block expects the list of header fields. Columns can be ignored by using the `<csv:ignore count="2"/>` tag, with `count` being
the number of columns to ignore. It defaults to 1.

Most options for the parser are rather obvious.
`ignoreLeadingWhiteSpace` sets the corresponding `opencsv` parser option, that allows
for removing of whitespace at the beginning of a field. `quotingBehaviour` can be
* `strict`: only allow data inside of quotes
* `non-strict`: only enclose data optionally
* `ignore`: ignore all quote characters
`onErrorSkipLine` tells the parser to ignore any kind of errors and to continue with the next line, that can be parsed.
It offers rather low performance, so it might be better to use instead custom error suppression on the resulting iterator.
The `charset` option is only required when using an input source that needs character conversion.

## decoding

The `<bean:config>` block configures the bean mapping. Individual fields are defined with the `<bean:field>` tag.
Usually, field is mapped to type `String`, unless a type such `int` is given. This kind of type definition is shorthand
for 
```xml
<bean:field name="age">
  <bean:decoder type="IntDecoder" /> 
</bean:field>
```

This wrapper provides a set of default decoders, custom ones can be used by giving the fully-qualified class name.
A decoder has to implement the `Decoder` interface. Below find the code the the `IntDecoder`:
```java
public class IntDecoder implements Decoder<Integer> {
    @Override
    public ResultWrapper<? extends Integer> decode(String value) {
        try {
            return success(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return decodingFailed();
        }
    }
}
```

`Decoder#success(T value)` and `Decoder#decodingFailed()` are provided methods, that
signal a successful, or failed decoding from textual data to the actual field.

Under the hood, a `decoder` that converts String data into a type `T` has to return
an instance of type `ResultWrapper<? extends T>`. That class is just a wrapper similar
to `Optional<T>`, but doesn't use `null` for denoting an absence of value (since `null` 
might be a valid result of a decoding operation). When writing custom decoders, it
is sufficient to just call `this.success(objectToReturn)` on success, and
`this.decodingFailed()` on error.

## decoding chain

It is possible to register multiple decoders for a single column, which allows for 
code re-use. If a decoder returns `decodingFailed()`, the next decoder in the chain
is called with the same String of input data. The first successful decoded result
will be used and no further decoders will be called for that column and that row.
If even the last decoders fails to decode the data, a `DataDecodingException` is thrown.

Some common decoding operations have abbreviations in the xml configuration.
They can be set on the `<bean:field>` tag. Currently, the following are provided:
* `nullable={true, false}`: try to decode the data to `null`
* `type={int, short, ...primitives}`: return a (boxed) primitive

Further more, an `EnumDecoder` is provided, that can be set up like that (for an
enumeration of type `com.example.Weekdays`):
```xml
<bean:field name="someEnumeratedField">
  <bean:enum type="com.example.Weekdays" />
    <bean:map key="mon" value="MONDAY" />
    <bean:map key="tue" value="TUESDAY" />
    <bean:map key="wed" value="WEDNESDAY" />
    <bean:map key="thu" value="THURSDAY" />
    <bean:map key="fri" value="FRIDAY" />
    <bean:map key="sat" value="SATURDAY" />
    <bean:map key="sun" value="SUNDAY" />
  </bean:enum>
</bean:field>
```

The `EnumDecoder` supports many-to-one mappings, because it is backed
by a map.

Of course, you can also extend the `EnumDecoder` class and use it as
`<bean:decoder type="com.example.MyExtendedEnumDecoder">`.

## post processing and validation

#### post processing

After a csv column has been decoded from String to the respective type `T`, the result 
can be further transformed (aka post processed). This allows for decoupling the sole 
type conversion and arbitrary transformation steps. An example would be to convert
a String "10" to the integer number 10 and afterwards incrementing it. Of course, one 
could do the incrementing in a `Decoder`. But from a semantical point of view (and 
when considering re-usability) supports this kind of separation.

A post processor instance has to implement the `PostProcessor<T>` interface, consisting
only of the method `T process(T value)`.  Again, multiple post processors can be 
registered for a given column, and all transformation steps are executed sequentially. 
Errors are re-thrown promptly as `PostProcessingException`.

In the xml configuration, a `PostProcessor` can be set up as a nested
`<bean:postprocessor type="com.example.MyPostProcessor" />` inside of the 
`<bean:field>` tag.

### post validation

Finally, the decoded and transformed value can be validated by extending the functional
`PostValidator` interface with the method `boolean validate(T value)`. This allows for 
constraining decoded data, e.g. integers to positive numbers.

Post-validation is executed sequentially for all registered `PostValidators`, too. If
all `PostValidators` return `true`, the validation was successful, otherwise a
`PostValidationException` is thrown.

In the xml configuration, a `PostValidator` can be set up as a nested
`<bean:postvalidator type="com.example.MyPostValidator" />` inside of the 
`<bean:field>` tag.

## to do

* multi-threaded parsing and decoding
* improve javadoc of `ConfigParser`/xml config
* provide concrete examples
* add further map-phases to the xml config
* add collection-/reduce-operations to the xml config (write to file etc.)
* allow for sets of null strings in the xml config
* allow for many-to-one boolean mappings in the xml config
* allow plugging into the xml config/extend the xsd schema
