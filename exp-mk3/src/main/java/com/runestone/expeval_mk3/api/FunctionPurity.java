package com.runestone.expeval_mk3.api;

/**
 * Purity and foldability policy for a registered expression function.
 */
public enum FunctionPurity {
    IMPURE {
        @Override
        public boolean pure() {
            return false;
        }

        @Override
        public boolean foldable() {
            return false;
        }
    },

    PURE {
        @Override
        public boolean pure() {
            return true;
        }

        @Override
        public boolean foldable() {
            return false;
        }
    },

    FOLDABLE {
        @Override
        public boolean pure() {
            return true;
        }

        @Override
        public boolean foldable() {
            return true;
        }
    };

    public abstract boolean pure();

    public abstract boolean foldable();
}
