package io.github.nowipi.ffm.processor;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.util.ArrayList;
import java.util.List;

public final class Padding {
    private Padding() {
    }

    public static StructLayout createPaddedLayout(MemoryLayout... layout) {
        long offset = 0;
        long structAlignment = 1;

        List<MemoryLayout> padded = new ArrayList<>();

        for (MemoryLayout member : layout) {
            long size = member.byteSize();
            long align = member.byteAlignment();

            long alignedOffset = alignUp(offset, align);
            long pad = alignedOffset - offset;

            if (pad > 0) {
                padded.add(MemoryLayout.paddingLayout(pad));
            }

            padded.add(member);
            offset = alignedOffset + size;
            structAlignment = Math.max(structAlignment, align);
        }

        long totalSize = alignUp(offset, structAlignment);
        long tailPad = totalSize - offset;

        if (tailPad > 0) {
            padded.add(MemoryLayout.paddingLayout(tailPad));
        }

        return MemoryLayout.structLayout(padded.toArray(MemoryLayout[]::new))
                .withByteAlignment(structAlignment);
    }

    private static long alignUp(long offset, long alignment) {
        return (offset + alignment - 1) & -alignment;
    }
}
