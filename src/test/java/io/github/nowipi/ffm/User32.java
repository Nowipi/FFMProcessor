package io.github.nowipi.ffm;

import java.lang.foreign.MemorySegment;

@Library("user32")
interface User32 {

    @Function("MessageBoxW")
    int messageBox(MemorySegment hWnd, MemorySegment lpText, MemorySegment lpCaption, long uType);

}
