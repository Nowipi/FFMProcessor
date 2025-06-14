package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.Function;
import io.github.nowipi.ffm.processor.Library;

import java.lang.foreign.MemorySegment;

@Library("user32")
interface User32 {

    @Function("MessageBoxW")
    int messageBox(MemorySegment hWnd, MemorySegment lpText, MemorySegment lpCaption, long uType);

}
