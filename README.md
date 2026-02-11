# FFMProcessor
This project makes it possible to generate Java FFM bindings using Java annotations.
## Example
Write an interface:
```java
@Library
public interface LibC {

    @Function("strlen")
    int strlen(MemorySegment s);
    
}
```
FFMProcessor generates:
```java
public class LibCImpl implements LibC {
    protected static final Arena arena = Arena.global();
    protected static final Linker linker = Linker.nativeLinker();
    protected static final SymbolLookup lookup = linker.defaultLookup();

    private static final MethodHandle strlenHandle;

    static {
        FunctionDescriptor strlenDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
        strlenHandle = linker.downcallHandle(lookup.find("strlen").orElseThrow(), strlenDescriptor);
    }
    @Override
    public int strlen(java.lang.foreign.MemorySegment s) {
        try { return (int) strlenHandle.invokeExact(s);} catch (Throwable e) { throw new RuntimeException(e); }
    }
}
```
And simply use it:
```java
LibC libC = new LibCImpl();
try(Arena arena = Arena.ofConfined()) {
    String str = "Hello World!";
    MemorySegment nativeString = arena.allocateFrom(str, StandardCharsets.US_ASCII);
    int l = libC.strlen(nativeString);
    Assertions.assertEquals(str.length(), l);
}
```
## Features
- Functions
- Capturing
- Structs

### Functions
You can map functions of a native library using the `@Function` annotation.
### Capturing
Capture global variables that native functions set, by annotating the function that changes the variable with `@Capture` and create a function annotated with `@CaptureState` that will return the captured variable.
### Structs
You can map a native struct to a Java class by annotating an interface with the `@Struct` annotation.
Every method inside the interface is interpreted as a member of the struct you are trying to map.
With the name of the method as the name of the member and the return type as the member's type, parameters are not allowed.
When doing this a class will be generated with your given name and with getters and setters for every member of the struct.
Plus some additional helper methods.