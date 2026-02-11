package io.github.nowipi.ffm.processor.pointer;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class FloatPointer implements Pointer<Float> {
    private MemorySegment pointerSegment;

    public FloatPointer(MemorySegment pointerSegment) {
        this.pointerSegment = pointerSegment;
    }

    public FloatPointer(Arena arena, float... values) {
       this(arena.allocateFrom(ValueLayout.JAVA_FLOAT, values));
    }

    private void growViewSize() {
        pointerSegment = pointerSegment.reinterpret(pointerSegment.byteSize() + ValueLayout.JAVA_FLOAT.byteSize());
    }

    @Override
    public Float get(long offset) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        return pointerSegment.get(ValueLayout.JAVA_FLOAT, offset);
    }

    @Override
    public Float getAtIndex(long index) {
        return get(ValueLayout.JAVA_FLOAT.byteSize() * index);
    }

    @Override
    public void set(long offset, Float value) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        pointerSegment.set(ValueLayout.JAVA_FLOAT, offset, value);
    }

    @Override
    public void setAtIndex(long index, Float value) {
        set(ValueLayout.JAVA_FLOAT.byteSize() * index, value);
    }

    @Override
    public long getByteSize() {
        return ValueLayout.JAVA_FLOAT.byteSize();
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
