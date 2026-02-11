package io.github.nowipi.ffm.processor.pointer;

import java.lang.foreign.MemorySegment;

public class VoidPointer implements Pointer<Void> {

    private final MemorySegment pointerSegment;

    public VoidPointer(MemorySegment pointerSegment) {
        this.pointerSegment = pointerSegment;
    }

    @Override
    public Void get(long offset) {
        return null;
    }

    @Override
    public Void getAtIndex(long index) {
        return null;
    }

    @Override
    public void set(long offset, Void value) {

    }

    @Override
    public void setAtIndex(long index, Void value) {

    }

    @Override
    public long getByteSize() {
        return 0;
    }

    @Override
    public long getAddress() {
        return pointerSegment.address();
    }

    @Override
    public MemorySegment getNativeSegment() {
        return pointerSegment;
    }
}
