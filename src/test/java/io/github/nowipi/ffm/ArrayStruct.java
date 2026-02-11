package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.annotations.Struct;
import io.github.nowipi.ffm.processor.pointer.Pointer;

@Struct("Array")
interface ArrayStruct {

    Pointer<Integer> data();
    long count();

}
