package com.runestone.converters;

public interface DataConverter<T, R> {

    R convert(T data);

}
