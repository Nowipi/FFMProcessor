package io.github.nowipi.ffm.processor.pointer;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class IntegerPointer extends VoidPointer implements Pointer<Integer> {

    public IntegerPointer(MemorySegment pointerSegment) {
        super(pointerSegment);
    }

    public IntegerPointer(Arena arena, int... values) {
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
    public void set(long offset, Integer value) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        pointerSegment.set(ValueLayout.JAVA_INT, offset, value);
    }


    @Override
    public long getByteSize() {
        return ValueLayout.JAVA_INT.byteSize();
    }

}
