package io.github.nowipi.ffm;

import java.lang.foreign.MemorySegment;

@Library
public interface LibC {

    @Function("strlen")
    int strlen(MemorySegment str);

    @Capture("errno")
    @Function("fopen")
    MemorySegment fopen(MemorySegment filename, MemorySegment mode);

    @Function("strerror")
    MemorySegment strerror(int errnum);

    @CaptureState("errno")
    int errno();
}
