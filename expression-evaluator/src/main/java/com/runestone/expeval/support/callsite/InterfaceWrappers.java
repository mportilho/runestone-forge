package com.runestone.expeval.support.callsite;

/**
 * Contains interfaces for wrapping functions with a specific number of parameters.
 *
 * @author Marcelo Portilho
 */
class InterfaceWrappers {

    public interface Function1 {
        Object call(Object p1);
    }

    public interface Function2 {
        Object call(Object p1, Object p2);
    }

    public interface Function3 {
        Object call(Object p1, Object p2, Object p3);
    }

    public interface Function4 {
        Object call(Object p1, Object p2, Object p3, Object p4);
    }

    public interface Function5 {
        Object call(Object p1, Object p2, Object p3, Object p4, Object p5);
    }

    public interface Function6 {
        Object call(Object p1, Object p2, Object p3, Object p4, Object p5, Object p6);
    }

    public interface Function7 {
        Object call(Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7);
    }

    public interface Function8 {
        Object call(Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8);
    }

    public interface Function9 {
        Object call(Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9);
    }

    public interface Function10 {
        Object call(
                Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9,
                Object p10);
    }

    public interface Function11 {
        Object call(
                Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9,
                Object p10, Object p11);
    }

}
