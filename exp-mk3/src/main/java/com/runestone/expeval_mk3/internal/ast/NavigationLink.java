package com.runestone.expeval_mk3.internal.ast;

sealed interface NavigationLink extends AstNode permits MethodNavigationLink, PropertyNavigationLink,
        SubscriptNavigationLink, WildcardNavigationLink {

    boolean safeNavigation();
}
