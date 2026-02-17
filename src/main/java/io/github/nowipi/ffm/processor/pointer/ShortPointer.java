package io.github.nowipi.ffm.processor.pointer;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class ShortPointer implements Pointer<Short> {
    private MemorySegment pointerSegment;

    public ShortPointer(MemorySegment pointerSegment) {
        this.pointerSegment = pointerSegment;
    }

    public ShortPointer(Arena arena, short... values) {
       this(arena.allocateFrom(ValueLayout.JAVA_SHORT, values));
    }

    private void growViewSize() {
        pointerSegment = pointerSegment.reinterpret(pointerSegment.byteSize() + ValueLayout.JAVA_SHORT.byteSize());
    }

    @Override
    public Short get(long offset) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        return pointerSegment.get(ValueLayout.JAVA_SHORT, offset);
    }

    @Override
    public Short getAtIndex(long index) {
        return get(ValueLayout.JAVA_SHORT.byteSize() * index);
    }

    @Override
    public void set(long offset, Short value) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        pointerSegment.set(ValueLayout.JAVA_SHORT, offset, value);
    }

    @Override
    public void setAtIndex(long index, Short value) {
        set(ValueLayout.JAVA_SHORT.byteSize() * index, value);
    }

    @Override
    public long getByteSize() {
        return ValueLayout.JAVA_SHORT.byteSize();
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
