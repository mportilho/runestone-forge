# Programmatic Single-Field Filters

Use `FilterData.single` when an application already has the value for one field. Both forms require an explicit target attribute type and an operation class:

```java
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.types.Equals;

// Explicit parameter name, retained as metadata.
FilterData named = FilterData.single("name", "customerName", String.class, Equals.class, "John");

// Abbreviated form: the parameter name defaults to the path ("name").
FilterData abbreviated = FilterData.single("name", String.class, Equals.class, "John");

// The input need not already have the target type.
FilterData byId = FilterData.single("id", Long.class, Equals.class, "42");
```

The path and parameter name must be non-null and non-blank. The parameter name is metadata, not an instruction to look up a request parameter. The supplied value is used directly as filter input. Both forms keep the usual defaults: no negation, no modifiers and no description.

## One-Slot Semantics And Ownership

Each call creates one path slot, one parameter slot and one **outer value slot**. The value argument is `Object`, not varargs:

```java
import com.runestone.dynafilter.core.operation.types.IsIn;

FilterData nullValue = FilterData.single("name", String.class, Equals.class, null);
// nullValue.values() is an Object[] of length 1 whose element is null.

String[] names = {"John", "Mary"};
FilterData severalNames = FilterData.single("name", String.class, IsIn.class, names);
// severalNames.values() has length 1; values()[0] is the same names array.

int[] ids = {1, 2};
FilterData primitiveArray = FilterData.single("id", Integer.class, IsIn.class, ids);
// Primitive arrays also remain one outer slot, without flattening or cloning.
```

A null value does not omit the filter or create an empty values array. Reference arrays, primitive arrays, collections and application objects are retained by reference. This is **not deep immutability**: do not mutate shared values or the exposed `FilterData` arrays while a query using them is in progress.

The factories neither infer the target type nor convert the value. Conversion and operation-specific validation remain in the existing resolution flow. Null and container interpretation, including whether an operation accepts a particular array type, remain that operation's responsibility.

## Existing Query Flow

Wrap the filter using the existing statement constructors and pass the complete statement to a configured `DynamicFilterJpaRepository`:

```java
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;

ConditionalStatement statement = new ConditionalStatement(
        new StatementWrapper(new LogicalStatement(byId), null, null),
        null);

var records = repository.findAll(statement);
```

No additional builder is required. For multiple parameters or other general-purpose construction, keep using the unchanged `FilterData.of(String, String[], Class<?>, Class<? extends FilterOperation>, Object[])` factory. There are no scalar `of` overloads, so existing calls, including null arguments, keep their established compilation and validation behavior.
