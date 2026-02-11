package io.github.nowipi.ffm;



import io.github.nowipi.ffm.processor.annotations.Capture;
import io.github.nowipi.ffm.processor.annotations.CaptureState;
import io.github.nowipi.ffm.processor.annotations.Function;
import io.github.nowipi.ffm.processor.annotations.Library;
import io.github.nowipi.ffm.processor.pointer.Pointer;

@Library
public interface LibC {

    @Function("strlen")
    int strlen(Pointer<Byte> str);

    @Capture("errno")
    @Function(value = "fopen")
    Pointer<Void> fopen(Pointer<Byte> filename, Pointer<Byte> mode);

    @Function(value = "strerror")
    Pointer<Byte> strerror(int errnum);

    @CaptureState("errno")
    int errno();
}
