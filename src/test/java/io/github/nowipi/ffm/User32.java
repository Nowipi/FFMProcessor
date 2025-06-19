package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.annotations.Function;
import io.github.nowipi.ffm.processor.annotations.Library;

import java.lang.foreign.MemorySegment;

@Library("user32")
interface User32 {

    @Function("MessageBoxW")
    int messageBox(MemorySegment hWnd, MemorySegment lpText, MemorySegment lpCaption, long uType);


    @Function("SetWindowPos")
    void setWindowPos(MemorySegment hWnd, MemorySegment hWndInsertAfter, int X, int Y, int cx, int cy, int uFlags);


}
