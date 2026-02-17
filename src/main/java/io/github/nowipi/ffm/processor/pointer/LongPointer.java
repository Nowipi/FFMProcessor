package io.github.nowipi.ffm.processor.pointer;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class LongPointer implements Pointer<Long> {
    private MemorySegment pointerSegment;

    public LongPointer(MemorySegment pointerSegment) {
        this.pointerSegment = pointerSegment;
    }

    public LongPointer(Arena arena, long... values) {
       this(arena.allocateFrom(ValueLayout.JAVA_LONG, values));
    }

    private void growViewSize() {
        pointerSegment = pointerSegment.reinterpret(pointerSegment.byteSize() + ValueLayout.JAVA_LONG.byteSize());
    }

    @Override
    public Long get(long offset) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        return pointerSegment.get(ValueLayout.JAVA_LONG, offset);
    }

    @Override
    public Long getAtIndex(long index) {
        return get(ValueLayout.JAVA_LONG.byteSize() * index);
    }

    @Override
    public void set(long offset, Long value) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        pointerSegment.set(ValueLayout.JAVA_LONG, offset, value);
    }

    @Override
    public void setAtIndex(long index, Long value) {
        set(ValueLayout.JAVA_LONG.byteSize() * index, value);
    }

    @Override
    public long getByteSize() {
        return ValueLayout.JAVA_LONG.byteSize();
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
