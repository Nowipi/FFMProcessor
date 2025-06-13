package io.github.nowipi.ffm;

import java.lang.foreign.MemorySegment;

@Library
public interface LibC {

    @Function("strlen")
    int strlen(MemorySegment str);
}
