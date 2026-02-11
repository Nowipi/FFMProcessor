package io.github.nowipi.ffm.processor.pointer;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

public class BytePointer implements Pointer<Byte> {
    private MemorySegment pointerSegment;

    public BytePointer(MemorySegment pointerSegment) {
        this.pointerSegment = pointerSegment;
    }

    public BytePointer(Arena arena, byte... values) {
       this(arena.allocateFrom(ValueLayout.JAVA_BYTE, values));
    }

    private void growViewSize() {
        pointerSegment = pointerSegment.reinterpret(pointerSegment.byteSize() + ValueLayout.JAVA_BYTE.byteSize());
    }

    public static String toString(Pointer<Byte> pointer, int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < data.length; i++) {
            data[i] = pointer.getAtIndex(i);
        }
        return new String(data, StandardCharsets.US_ASCII);
    }

    @Override
    public Byte get(long offset) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        return pointerSegment.get(ValueLayout.JAVA_BYTE, offset);
    }

    @Override
    public Byte getAtIndex(long index) {
        return get(index);
    }

    @Override
    public void set(long offset, Byte value) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        pointerSegment.set(ValueLayout.JAVA_BYTE, offset, value);
    }

    @Override
    public void setAtIndex(long index, Byte value) {
        set(index, value);
    }

    @Override
    public long getByteSize() {
        return ValueLayout.JAVA_BYTE.byteSize();
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
