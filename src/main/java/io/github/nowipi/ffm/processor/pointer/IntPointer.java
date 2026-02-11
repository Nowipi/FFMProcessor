package io.github.nowipi.ffm.processor.pointer;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class IntPointer implements Pointer<Integer> {
    private MemorySegment pointerSegment;

    public IntPointer(MemorySegment pointerSegment) {
        this.pointerSegment = pointerSegment;
    }

    public IntPointer(Arena arena, int... values) {
       this(arena.allocateFrom(ValueLayout.JAVA_INT, values));
    }

    private void growViewSize() {
        pointerSegment = pointerSegment.reinterpret(pointerSegment.byteSize() + ValueLayout.JAVA_INT.byteSize());
    }

    @Override
    public Integer get(long offset) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        return pointerSegment.get(ValueLayout.JAVA_INT, offset);
    }

    @Override
    public Integer getAtIndex(long index) {
        return get(ValueLayout.JAVA_INT.byteSize() * index);
    }

    @Override
    public void set(long offset, Integer value) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        pointerSegment.set(ValueLayout.JAVA_INT, offset, value);
    }

    @Override
    public void setAtIndex(long index, Integer value) {
        set(ValueLayout.JAVA_INT.byteSize() * index, value);
    }

    @Override
    public long getByteSize() {
        return ValueLayout.JAVA_INT.byteSize();
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
