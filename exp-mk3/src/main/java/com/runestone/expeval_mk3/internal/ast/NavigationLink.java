package com.runestone.expeval_mk3.internal.ast;

public sealed interface NavigationLink extends AstNode permits CallNavigationLink, FilterNavigationLink,
        IndexSubscriptNavigationLink, PropertyNavigationLink, SliceSubscriptNavigationLink,
        StringKeySubscriptNavigationLink, WildcardNavigationLink {
}
