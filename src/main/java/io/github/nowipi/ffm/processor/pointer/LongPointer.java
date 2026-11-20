package io.github.nowipi.ffm.processor.pointer;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class LongPointer extends VoidPointer implements Pointer<Long> {

    public LongPointer(MemorySegment pointerSegment) {
        super(pointerSegment);
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
    public void set(long offset, Long value) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        pointerSegment.set(ValueLayout.JAVA_LONG, offset, value);
    }

    @Override
    public long getByteSize() {
        return ValueLayout.JAVA_LONG.byteSize();
    }


}
