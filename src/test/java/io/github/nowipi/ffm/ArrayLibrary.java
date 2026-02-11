package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.annotations.Function;
import io.github.nowipi.ffm.processor.annotations.Library;
import io.github.nowipi.ffm.processor.pointer.Pointer;

@Library("array.dll")
public interface ArrayLibrary {

    @Function("array_new")
    Pointer<Array> arrayNew(long count);

}
