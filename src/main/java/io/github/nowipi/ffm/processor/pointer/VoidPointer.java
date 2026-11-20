package io.github.nowipi.ffm.processor.pointer;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.function.Function;

public class VoidPointer {

    private static final VoidPointer NULL = new VoidPointer(MemorySegment.NULL);

    MemorySegment pointerSegment;

    public VoidPointer(MemorySegment pointerSegment) {
        this.pointerSegment = pointerSegment;
    }


    public Pointer<Integer> toIntegerPointer() {
        return new IntegerPointer(pointerSegment);
    }

    public Pointer<Byte> toBytePointer() {
        return new BytePointer(pointerSegment);
    }

    public Pointer<Float> toFloatPointer() {
        return new FloatPointer(pointerSegment);
    }

    public Pointer<Double> toDoublePointer() {
        return new DoublePointer(pointerSegment);
    }

    public Pointer<Short> toShortPointer() {
        return new ShortPointer(pointerSegment);
    }

    public Pointer<Long> toLongPointer() {
        return new LongPointer(pointerSegment);
    }

    public <S> StructPointer<S> toStructPointer(Function<S, MemorySegment> serializer,
                                                Function<MemorySegment, S> deserializer,
                                                MemoryLayout layout) {
        return new StructPointer<>(serializer, deserializer, pointerSegment, layout);
    }

    public long getAddress() {
        return pointerSegment.address();
    }

    public MemorySegment getMemorySegment() {
        return pointerSegment;
    }
}
