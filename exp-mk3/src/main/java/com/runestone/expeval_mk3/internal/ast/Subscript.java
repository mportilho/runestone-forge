package com.runestone.expeval_mk3.internal.ast;

sealed interface Subscript permits IndexSubscript, SliceSubscript, StringKeySubscript, WildcardSubscript {
}
