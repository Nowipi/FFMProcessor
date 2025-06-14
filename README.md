# FFMProcessor
This project makes it possible to generate Java FFM bindings using Java annotations.
## Features
- Functions
- Capturing
### Functions
You can map functions of a native library using the `@Function` keyword.
This maps native primitives to Java primitives and any other data structure or pointer to `MemorySegment`.
### Capturing
Capture variables that native functions set, by annotating the function that changes the variable with `@Capture` and create a function annotated with `@CaptureState` that will return the captured variable.
## Examples
Write an interface:
```java
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
```
FFMProcessor generates:
```java
public class LibCImpl implements LibC {
    private static final Arena arena = Arena.global();
    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup lookup = linker.defaultLookup();

    private static final MemorySegment capturedState;
    private static final VarHandle errnoHandle;
    private static final MethodHandle strlenHandle;
    private static final MethodHandle fopenHandle;
    private static final MethodHandle strerrorHandle;

    static {
        var captureStateLayout = Linker.Option.captureStateLayout();
        capturedState = arena.allocate(captureStateLayout);
        errnoHandle = captureStateLayout.varHandle(MemoryLayout.PathElement.groupElement("errno"));
        FunctionDescriptor strlenDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
        strlenHandle = linker.downcallHandle(lookup.find("strlen").orElseThrow(), strlenDescriptor);
        FunctionDescriptor fopenDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
        fopenHandle = linker.downcallHandle(lookup.find("fopen").orElseThrow(), fopenDescriptor, Linker.Option.captureCallState("errno"));
        FunctionDescriptor strerrorDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
        strerrorHandle = linker.downcallHandle(lookup.find("strerror").orElseThrow(), strerrorDescriptor);
    }

    @Override
    public int strlen(java.lang.foreign.MemorySegment str) {
        try {
            return (int) strlenHandle.invokeExact(str);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public java.lang.foreign.MemorySegment fopen(java.lang.foreign.MemorySegment filename, java.lang.foreign.MemorySegment mode) {
        try {
            return (java.lang.foreign.MemorySegment) fopenHandle.invokeExact(capturedState, filename, mode);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public java.lang.foreign.MemorySegment strerror(int errnum) {
        try {
            return (java.lang.foreign.MemorySegment) strerrorHandle.invokeExact(errnum);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int errno() {
        return (int) errnoHandle.get(capturedState, 0);
    }
}
```
And simply use it:
```java
LibC libC = new LibCImpl();
try(var arena = Arena.ofConfined()) {
    MemorySegment file = libC.fopen(arena.allocateFrom("file-does-not-exist"), arena.allocateFrom("r"));
    if (file.address() == 0) {
        MemorySegment message = libC.strerror(libC.errno());
        int messageLength = libC.strlen(message);
        System.out.println(message.reinterpret(messageLength + 1).getString(0));
    } else {
        System.out.println("no error has occurred");
    }
}
```
