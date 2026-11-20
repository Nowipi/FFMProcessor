package io.github.nowipi.ffm.processor.pointer;

public interface Pointer<T> {

    T get(long offset);
    default T getAtIndex(long index) {
        return get(getByteSize() *  index);
    }

    void set(long offset, T value);
    default void setAtIndex(long index, T value) {
        set(getByteSize() * index, value);
    }


    long getByteSize();
    long getAddress();

}
