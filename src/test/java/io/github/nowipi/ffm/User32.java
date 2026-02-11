package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.annotations.Function;
import io.github.nowipi.ffm.processor.annotations.Library;
import io.github.nowipi.ffm.processor.pointer.Pointer;

import java.lang.foreign.MemorySegment;

@Library("user32")
interface User32 {

    @Function("MessageBoxW")
    int messageBox(Pointer<Void> hWnd, Pointer<Byte> lpText, Pointer<Byte> lpCaption, long uType);


    @Function("SetWindowPos")
    void setWindowPos(Pointer<Void> hWnd, Pointer<Void> hWndInsertAfter, int X, int Y, int cx, int cy, int uFlags);


}
