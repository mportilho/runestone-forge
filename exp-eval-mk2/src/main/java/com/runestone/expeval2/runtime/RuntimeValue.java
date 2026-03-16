package com.runestone.expeval2.runtime;

import com.runestone.expeval2.semantic.ResolvedType;

public sealed interface RuntimeValue permits BooleanValue, DateTimeValue, DateValue, NullValue, NumberValue, StringValue, TimeValue, VectorValue {

    ResolvedType type();

    Object raw();
}
