package io.github.nowipi.ffm;



import io.github.nowipi.ffm.processor.Capture;
import io.github.nowipi.ffm.processor.CaptureState;
import io.github.nowipi.ffm.processor.Function;
import io.github.nowipi.ffm.processor.Library;

import java.lang.foreign.MemorySegment;

@Library
public interface LibC {

    @Function("strlen")
    int strlen(MemorySegment str);

    @Capture("errno")
    @Function(value = "fopen")
    MemorySegment fopen(MemorySegment filename, MemorySegment mode);

    @Function(value = "strerror")
    MemorySegment strerror(int errnum);

    @CaptureState("errno")
    int errno();
}
