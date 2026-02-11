package io.github.nowipi.ffm.processor.pointer;

import java.lang.foreign.MemorySegment;

public interface Pointer<T> {

    Pointer<Void> NULL = new VoidPointer(MemorySegment.NULL);

    T get(long offset);
    T getAtIndex(long index);

    void set(long offset, T value);
    void setAtIndex(long index, T value);


    long getByteSize();
    long getAddress();

    MemorySegment getNativeSegment();
}
