package com.runestone.expeval.internal.ast;

public sealed interface NavigationLink extends AstNode permits CallNavigationLink, FilterNavigationLink,
        IndexSubscriptNavigationLink, PropertyNavigationLink, SliceSubscriptNavigationLink,
        StringKeySubscriptNavigationLink, WildcardNavigationLink {

    boolean safe();
}
