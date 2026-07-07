package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

public sealed interface NavigationLinkSource permits CollectionOperationNavigationSource, FilterNavigationSource,
        MethodNavigationSource, PropertyNavigationSource, SubscriptNavigationSource, WildcardNavigationSource {

    NodeId nodeId();

    SourceSpan sourceSpan();
}
