package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.annotations.Function;
import io.github.nowipi.ffm.processor.annotations.Library;

import java.lang.foreign.MemorySegment;

@Library
public abstract class ImplementationTest {

    public ImplementationTest(int y, int a) {
        System.out.println(y);
        System.out.println(a);
    }

    public abstract void test();

    @Function("strlen")
    public abstract int strlen(MemorySegment str);

}
