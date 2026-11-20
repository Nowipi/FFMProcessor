package io.github.nowipi.ffm.processor.pointer;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class FloatPointer extends VoidPointer implements Pointer<Float> {

    public FloatPointer(MemorySegment pointerSegment) {
        super(pointerSegment);
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
    public void set(long offset, Float value) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        pointerSegment.set(ValueLayout.JAVA_FLOAT, offset, value);
    }


    @Override
    public long getByteSize() {
        return ValueLayout.JAVA_FLOAT.byteSize();
    }

    @Override
    public long getAddress() {
        return pointerSegment.address();
    }

}
