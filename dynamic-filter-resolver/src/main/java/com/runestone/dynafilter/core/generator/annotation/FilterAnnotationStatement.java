package com.runestone.dynafilter.core.generator.annotation;

import java.util.List;

public record FilterAnnotationStatement(List<Filter> filters, String negate) {
}
