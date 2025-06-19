package io.github.nowipi.ffm;



import io.github.nowipi.ffm.processor.annotations.Capture;
import io.github.nowipi.ffm.processor.annotations.CaptureState;
import io.github.nowipi.ffm.processor.annotations.Function;
import io.github.nowipi.ffm.processor.annotations.Library;

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
