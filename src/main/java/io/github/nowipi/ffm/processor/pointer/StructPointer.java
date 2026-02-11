package io.github.nowipi.ffm.processor.pointer;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.function.Function;

public class StructPointer<T> implements Pointer<T> {
    private final Function<T, MemorySegment> serializer;
    private final Function<MemorySegment, T> deserializer;
    private final MemoryLayout layout;
    private MemorySegment pointerSegment;

    public StructPointer(Function<T, MemorySegment> serializer, Function<MemorySegment, T> deserializer, MemorySegment pointerSegment, MemoryLayout layout) {
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.layout = layout;
        this.pointerSegment = pointerSegment;
    }

    public static <T> StructPointer<T> from(Function<T, MemorySegment> serializer, Function<MemorySegment, T> deserializer, T value, MemoryLayout layout) {
        return new StructPointer<>(serializer, deserializer, serializer.apply(value), layout);
    }

    private void growViewSize() {
        pointerSegment = pointerSegment.reinterpret(pointerSegment.byteSize() + layout.byteSize());
    }

    @Override
    public T get(long offset) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        return deserializer.apply(pointerSegment.asSlice(offset, layout));
    }

    @Override
    public T getAtIndex(long index) {
        return get(layout.byteSize() * index);
    }

    @Override
    public void set(long offset, T value) {
        if (offset >= pointerSegment.byteSize()) {
            growViewSize();
        }
        MemorySegment valueSegment = serializer.apply(value);
        MemorySegment.copy(pointerSegment, offset, valueSegment, 0, valueSegment.byteSize());
    }

    @Override
    public void setAtIndex(long index, T value) {
        set(layout.byteSize() * index, value);
    }

    @Override
    public long getByteSize() {
        return layout.byteSize();
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
