package io.github.nowipi.ffm.processor;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

final class NativeLibraryImplementationWriter {

    private final String implementationClassName;
    private final NativeLibrary nativeLibrary;

    public NativeLibraryImplementationWriter(NativeLibrary nativeLibrary) {
        this.nativeLibrary = nativeLibrary;
        this.implementationClassName = nativeLibrary.className() + "Impl";
    }

    public void createImplementationClass(Filer filer) throws IOException {
        JavaFileObject file = filer.createSourceFile(nativeLibrary.packageName() + "." + implementationClassName);
        writeImplementationClass(file);
    }

    private void writeImplementationClass(JavaFileObject file) {
        try (Writer writer = file.openWriter()) {
            writePackage(writer);
            writeImports(writer);
            writeImplementation(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writePackage(Writer writer) throws IOException {
        writer.write("package " + nativeLibrary.packageName() + ";\n\n");
    }

    private void writeImports(Writer writer) throws IOException {
        writer.write("import java.lang.foreign.*;\nimport java.lang.invoke.MethodHandle;\nimport java.lang.invoke.VarHandle;\nimport java.net.URL;\n\n");
    }

    private void writeImplementation(Writer writer) throws IOException {

        writeClassDeclaration(writer);
        writer.write(" {\n");

        if (nativeLibrary instanceof NativeLibraryClass nativeLibraryClass) {
            var opt = nativeLibraryClass.constructor();
            if (opt.isPresent()) {
                writeMatchingSuperConstructor(writer, opt.get());
            }
        }

        writeClassBody(writer);

        writer.write("}\n");
    }

    private void writeMatchingSuperConstructor(Writer writer, ExecutableElement executableElement) throws IOException {
        writer.write("  public ");
        writer.write(implementationClassName);
        writer.write("(");
        writer.write(executableElement.getParameters().stream().map(p -> p.asType().toString() + " " + p.getSimpleName().toString()).collect(Collectors.joining(", ")));
        writer.write(") {\n");
        writer.write("      super(");
        writer.write(executableElement.getParameters().toString());
        writer.write(");\n  }\n");
    }

    private void writeClassDeclaration(Writer writer) throws IOException {
        writer.write("public ");
        if (nativeLibrary.hasVirtualMethods()) {
            writer.write("abstract ");
        }
        writer.write("class ");

        writer.write(implementationClassName);
        switch (nativeLibrary) {
            case NativeLibraryInterface _ -> writer.write(" implements ");
            case NativeLibraryClass _ -> writer.write(" extends ");
            default -> throw new IllegalStateException("Unexpected value: " + nativeLibrary);
        }

        writer.write(nativeLibrary.className());
    }

    private void writeClassBody(Writer writer) throws IOException {

        writeAttributes(writer);

        switch (nativeLibrary.loadMethod()) {
            case STATIC:
                writeStaticHandleInitialization(writer);
                break;
            case EXPLICIT:
                writeExplicitHandleInitialization(writer);
        }

        writeMethodImplementations(writer);
        writeCaptureImplementations(writer);
    }

    private void writeAttributes(Writer writer) throws IOException {
        writer.write("  protected static final Arena arena = Arena.global();\n  protected static final Linker linker = Linker.nativeLinker();\n  protected static final SymbolLookup lookup");
        writeLibraryLookupInitialization(writer);
        writer.write("\n");

        if (!nativeLibrary.captures().isEmpty())
            writeCaptureAttributes(writer);

        writeMethodHandleAttributes(writer);
    }

    private void writeLibraryLookupInitialization(Writer writer) throws IOException {
        Optional<String> nativeLibraryName = nativeLibrary.nativeLibraryName();
        if (nativeLibraryName.isEmpty()) {
            writer.write(" = linker.defaultLookup()");
        }
        writer.write(";\n");
    }

    private void writeCaptureAttributes(Writer writer) throws IOException {
        writer.write("  private static final MemorySegment capturedState;\n");
        writeCaptureStateAttributes(writer);
    }

    private void writeCaptureStateAttributes(Writer writer) throws IOException {
        for (VariableCapture capture : nativeLibrary.captures()) {
            writer.write("  private static final VarHandle ");
            writer.write(capture.handleName());
            writer.write(";\n");
        }
    }

    private void writeMethodHandleAttributes(Writer writer) throws IOException {

        //handle definitions
        for (NativeFunction nativeFunction : nativeLibrary.functions()) {
            writer.write("  private static final MethodHandle ");
            writer.write(nativeFunction.handleName());
            writer.write(";\n");
        }

    }

    private void writeExplicitHandleInitialization(Writer writer) throws IOException {

        //handle definitions
        for (NativeFunction nativeFunction : nativeLibrary.functions()) {
            writer.write("  private static MethodHandle ");
            writer.write(nativeFunction.handleName());
            writer.write(";\n");
        }

        writer.write("\n  public static void loadHandles() {\n");
        writeHandleInitializers(writer);
        writer.write("  }\n");
    }

    private void writeStaticHandleInitialization(Writer writer) throws IOException {
        writer.write("\n  static {\n");

        Optional<String> nativeLibraryOpt = nativeLibrary.nativeLibraryName();
        if (nativeLibraryOpt.isPresent()) {
            String nativeLibraryName = nativeLibraryOpt.get();

            writer.write("\tURL resource = ");
            writer.write(nativeLibrary.className());
            writer.write(".class.getResource(\"");
            writer.write(nativeLibraryName);
            writer.write("\");\n\tString path = \"");
            writer.write(nativeLibraryName);
            writer.write("\";\n\tif (resource != null) {\n\t\tpath = resource.getPath().substring(1);\n\t}\n\tlookup = SymbolLookup.libraryLookup(path, arena);\n");
        }

        writeHandleInitializers(writer);
        writer.write("  }\n");
    }

    private void writeHandleInitializers(Writer writer) throws IOException {

        List<VariableCapture> captures = nativeLibrary.captures();
        if (!captures.isEmpty()) {
            writer.write("      var captureStateLayout = Linker.Option.captureStateLayout();\n      capturedState = arena.allocate(captureStateLayout);\n");
            for (VariableCapture capture : nativeLibrary.captures()) {
                writer.write("      ");
                writer.write(capture.handleName());
                writer.write(" = captureStateLayout.varHandle(MemoryLayout.PathElement.groupElement(\"");
                writer.write(capture.nativeName());
                writer.write("\"));\n");
            }
        }


        for (NativeFunction nativeFunction : nativeLibrary.functions()) {

            writer.write("      FunctionDescriptor ");
            String descriptorName = nativeFunction.nativeName() + "Descriptor";
            writer.write(descriptorName);
            if (nativeFunction instanceof NativeMethod) {
                writer.write(" = FunctionDescriptor.ofVoid(");
            } else {
                writer.write(" = FunctionDescriptor.of(");
            }
            writer.write(nativeFunction.functionDescriptorLayout());
            writer.write(");\n      ");

            writer.write(nativeFunction.handleName());
            writer.write(" = linker.downcallHandle(lookup.find(\"");
            writer.write(nativeFunction.nativeName());
            writer.write("\").orElseThrow(), ");
            writer.write(descriptorName);

            if (nativeFunction instanceof Capturing captured) {
                writer.write(", Linker.Option.captureCallState(\"");
                writer.write(captured.capturedStateName());
                writer.write("\")");
            }

            writer.write(");\n");
        }
    }

    private void writeMethodImplementations(Writer writer) throws IOException {
        for (NativeFunction nativeFunction : nativeLibrary.functions()) {


            String javaReturnTypeName = nativeFunction.javaReturnTypeName();
            writer.write("  @Override\n  public ");
            writer.write(javaReturnTypeName);
            writer.write(" ");
            writer.write(nativeFunction.javaName());

            writer.write("(");
            writer.write(nativeFunction.javaParameterTypedNames());

            writer.write(") {\n     ");

            writer.write("  try { ");
            boolean structPointer = false;
            if (!(nativeFunction instanceof NativeMethod _)) {

                writer.write("return ");
                if (nativeFunction.hasStructReturn()) {
                    writer.write(javaReturnTypeName);
                    writer.write(".from((MemorySegment) ");
                } else if (nativeFunction.hasPointerReturn()) {

                    writer.write("new ");
                    String str = nativeFunction.getPointerClass((DeclaredType) nativeFunction.javaReturnType());
                    if (str.contains("StructPointer")) {
                        structPointer = true;
                    }
                    writer.write(str);
                    writer.write("((MemorySegment) ");
                } else {
                    writer.write("(");
                    writer.write(javaReturnTypeName);
                    writer.write(") ");
                }


            }
            writer.write(nativeFunction.handleName());
            writer.write(".invoke(");
            if (nativeFunction instanceof Capturing) {
                writer.write("capturedState,");
            }
            if (nativeFunction.hasStructReturn()) {
                writer.write("arena,");
            }
            writer.write(nativeFunction.javaParameterNames());

            writer.write(")");
            if (structPointer) {
                writer.write("), ");
                writer.write(((DeclaredType)nativeFunction.javaReturnType()).getTypeArguments().get(0).toString());
                writer.write(".LAYOUT");
            } else if (nativeFunction.hasPointerReturn()) {
                writer.write(")");
            }
            if (nativeFunction.hasStructReturn() || nativeFunction.hasPointerReturn()) {
                writer.write(")");
            }
            writer.write(";} catch (Throwable e) { throw new RuntimeException(e); }\n");
            writer.write("  }\n");
        }
    }

    private void writeCaptureImplementations(Writer writer) throws IOException {
        for (VariableCapture capture : nativeLibrary.captures()) {


            var javaType = capture.javaType();
            writer.write("  @Override\n  public ");
            writer.write(javaType);
            writer.write(" ");
            writer.write(capture.javaName());

            writer.write("() {\n        return (");
            writer.write(javaType);
            writer.write(") ");

            writer.write(capture.handleName());
            writer.write(".get(");
            writer.write("capturedState, 0);\n  }\n");
        }
    }



}
