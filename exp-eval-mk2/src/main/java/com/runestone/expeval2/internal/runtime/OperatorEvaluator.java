package com.runestone.expeval2.internal.runtime;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.runestone.expeval2.internal.ast.BinaryOperator;
import com.runestone.expeval2.internal.ast.PostfixOperator;
import com.runestone.expeval2.internal.ast.TernaryOperator;
import com.runestone.expeval2.internal.ast.UnaryOperator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

final class OperatorEvaluator {

    private OperatorEvaluator() {
    }

    static Object evaluateUnary(UnaryOperator operator, Object operand, RuntimeServices runtimeServices, MathContext mathContext) {
        return switch (operator) {
            case NEGATE      -> asBigDecimal(operand, runtimeServices).negate();
            case LOGICAL_NOT -> !asBoolean(operand, runtimeServices);
            case SQRT        -> asBigDecimal(operand, runtimeServices).sqrt(mathContext);
            case MODULUS     -> asBigDecimal(operand, runtimeServices).abs();
        };
    }

    static Object evaluateBinary(BinaryOperator operator, Object left, Object right, RuntimeServices runtimeServices, MathContext mathContext) {
        return switch (operator) {
            case ADD      -> asBigDecimal(left, runtimeServices).add(asBigDecimal(right, runtimeServices));
            case SUBTRACT -> asBigDecimal(left, runtimeServices).subtract(asBigDecimal(right, runtimeServices));
            case MULTIPLY -> asBigDecimal(left, runtimeServices).multiply(asBigDecimal(right, runtimeServices));
            case DIVIDE   -> asBigDecimal(left, runtimeServices).divide(asBigDecimal(right, runtimeServices), mathContext);
            case MODULO   -> asBigDecimal(left, runtimeServices).remainder(asBigDecimal(right, runtimeServices));
            case POWER    -> pow(asBigDecimal(left, runtimeServices), asBigDecimal(right, runtimeServices), mathContext);
            case ROOT     -> BigDecimalMath.root(asBigDecimal(left, runtimeServices), asBigDecimal(right, runtimeServices), mathContext);
            case AND      -> asBoolean(left, runtimeServices) && asBoolean(right, runtimeServices);
            case OR       -> asBoolean(left, runtimeServices) || asBoolean(right, runtimeServices);
            case XOR      -> asBoolean(left, runtimeServices) ^ asBoolean(right, runtimeServices);
            case XNOR     -> !(asBoolean(left, runtimeServices) ^ asBoolean(right, runtimeServices));
            case NAND     -> !(asBoolean(left, runtimeServices) && asBoolean(right, runtimeServices));
            case NOR      -> !(asBoolean(left, runtimeServices) || asBoolean(right, runtimeServices));
            case GREATER_THAN          -> compare(left, right, runtimeServices) > 0;
            case GREATER_THAN_OR_EQUAL -> compare(left, right, runtimeServices) >= 0;
            case LESS_THAN             -> compare(left, right, runtimeServices) < 0;
            case LESS_THAN_OR_EQUAL    -> compare(left, right, runtimeServices) <= 0;
            case EQUAL                 -> compareEquality(left, right, runtimeServices);
            case NOT_EQUAL             -> !compareEquality(left, right, runtimeServices);
            case IN -> {
                List<?> vector = asList(right);
                for (Object element : vector) {
                    if (compareEquality(left, element, runtimeServices)) yield true;
                }
                yield false;
            }
            case NOT_IN -> {
                List<?> vector = asList(right);
                for (Object element : vector) {
                    if (compareEquality(left, element, runtimeServices)) yield false;
                }
                yield true;
            }
            case CONCATENATE           -> asString(left, runtimeServices) + asString(right, runtimeServices);
            case NULL_COALESCE         -> throw new IllegalStateException("NULL_COALESCE must be handled explicitly");
            case REGEX_MATCH, REGEX_NOT_MATCH -> throw new IllegalStateException("REGEX_MATCH/REGEX_NOT_MATCH must be handled explicitly");
        };
    }

    static Object evaluateTernary(TernaryOperator operator, Object value, Object lower, Object upper, RuntimeServices runtimeServices) {
        boolean inRange = compare(value, lower, runtimeServices) >= 0 && compare(value, upper, runtimeServices) <= 0;
        return (operator == TernaryOperator.BETWEEN) == inRange;
    }

    static Object evaluatePostfix(PostfixOperator operator, Object operand, RuntimeServices runtimeServices, MathContext mathContext) {
        BigDecimal value = asBigDecimal(operand, runtimeServices);
        return switch (operator) {
            case PERCENT   -> value.movePointLeft(2);
            case FACTORIAL -> BigDecimalMath.factorial(value, mathContext);
        };
    }

    static int compare(Object left, Object right, RuntimeServices runtimeServices) {
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            return asBigDecimal(left, runtimeServices).compareTo(asBigDecimal(right, runtimeServices));
        }
        if (left instanceof LocalDate || right instanceof LocalDate) {
            return asLocalDate(left, runtimeServices).compareTo(asLocalDate(right, runtimeServices));
        }
        if (left instanceof LocalTime || right instanceof LocalTime) {
            return asLocalTime(left, runtimeServices).compareTo(asLocalTime(right, runtimeServices));
        }
        if (left instanceof LocalDateTime || right instanceof LocalDateTime) {
            return asLocalDateTime(left, runtimeServices).compareTo(asLocalDateTime(right, runtimeServices));
        }
        if (left instanceof String || right instanceof String) {
            return asString(left, runtimeServices).compareTo(asString(right, runtimeServices));
        }
        if (left instanceof Boolean || right instanceof Boolean) {
            return Boolean.compare(asBoolean(left, runtimeServices), asBoolean(right, runtimeServices));
        }
        throw new IllegalStateException("unsupported comparison between values");
    }

    static boolean compareEquality(Object left, Object right, RuntimeServices runtimeServices) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            return asBigDecimal(left, runtimeServices).compareTo(asBigDecimal(right, runtimeServices)) == 0;
        }
        if (left instanceof List<?> leftList && right instanceof List<?> rightList) {
            if (leftList.size() != rightList.size()) return false;
            for (int i = 0; i < leftList.size(); i++) {
                if (!compareEquality(leftList.get(i), rightList.get(i), runtimeServices)) return false;
            }
            return true;
        }
        return Objects.equals(left, right);
    }

    private static BigDecimal pow(BigDecimal base, BigDecimal exponent, MathContext mathContext) {
        BigDecimal normalized = exponent.stripTrailingZeros();
        if (normalized.scale() <= 0 && normalized.precision() <= 9) {
            return base.pow(normalized.intValue(), mathContext);
        }
        return BigDecimalMath.pow(base, exponent, mathContext);
    }

    private static List<?> asList(Object value) {
        if (value instanceof List<?> list) return list;
        throw new IllegalStateException(
                "expected a List but found: " + (value == null ? "null" : value.getClass().getName()));
    }

    static BigDecimal asBigDecimal(Object value, RuntimeServices runtimeServices) {
        if (value instanceof BigDecimal bd) return bd;
        return runtimeServices.asNumber(value);
    }

    static boolean asBoolean(Object value, RuntimeServices runtimeServices) {
        if (value instanceof Boolean b) return b;
        return runtimeServices.asBoolean(value);
    }

    static String asString(Object value, RuntimeServices runtimeServices) {
        if (value instanceof String s) return s;
        return runtimeServices.asString(value);
    }

    static LocalDate asLocalDate(Object value, RuntimeServices runtimeServices) {
        if (value instanceof LocalDate d) return d;
        return runtimeServices.asDate(value);
    }

    static LocalTime asLocalTime(Object value, RuntimeServices runtimeServices) {
        if (value instanceof LocalTime t) return t;
        return runtimeServices.asTime(value);
    }

    static LocalDateTime asLocalDateTime(Object value, RuntimeServices runtimeServices) {
        if (value instanceof LocalDateTime dt) return dt;
        return runtimeServices.asDateTime(value);
    }
}