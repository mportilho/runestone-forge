package com.runestone.expeval_mk3.internal.ast;

public sealed interface SubscriptSource permits IndexSubscriptSource, SliceSubscriptSource, StringKeySubscriptSource,
        WildcardSubscriptSource {
}
