package io.github.nowipi.ffm.processor.pointer;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class DoublePointer implements Pointer<Double> {
    private MemorySegment pointerSegment;

    public DoublePointer(MemorySegment pointerSegment) {
        this.pointerSegment = pointerSegment;
    }

    public DoublePointer(Arena arena, double... values) {
       this(arena.allocateFrom(ValueLayout.JAVA_DOUBLE, values));
    }

    private void growViewSize() {
        pointerSegment = pointerSegment.reinterpret(pointerSegment.byteSize() + ValueLayout.JAVA_DOUBLE.byteSize());
    }

    @Override
    public Double get(long offset) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        return pointerSegment.get(ValueLayout.JAVA_DOUBLE, offset);
    }

    @Override
    public Double getAtIndex(long index) {
        return get(ValueLayout.JAVA_DOUBLE.byteSize() * index);
    }

    @Override
    public void set(long offset, Double value) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        pointerSegment.set(ValueLayout.JAVA_DOUBLE, offset, value);
    }

    @Override
    public void setAtIndex(long index, Double value) {
        set(ValueLayout.JAVA_DOUBLE.byteSize() * index, value);
    }

    @Override
    public long getByteSize() {
        return ValueLayout.JAVA_DOUBLE.byteSize();
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
