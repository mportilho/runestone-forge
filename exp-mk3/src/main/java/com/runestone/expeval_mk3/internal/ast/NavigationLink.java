package com.runestone.expeval_mk3.internal.ast;

sealed interface NavigationLink extends AstNode permits IndexSubscriptNavigationLink, MethodNavigationLink,
        PropertyNavigationLink, SliceSubscriptNavigationLink, StringKeySubscriptNavigationLink, WildcardNavigationLink {
}
