package org.reasm;

final class AssemblyMessageTestsCommon {

    static final String TEXT = "text";

    static final AssemblyMessage CUSTOM_ASSEMBLY_MESSAGE = createCustomAssemblyMessage();

    static AssemblyMessage createCustomAssemblyMessage() {
        return new AssemblyMessage(MessageGravity.INFORMATION, TEXT, null) {
        };
    }

    // This class is not meant to be instantiated.
    private AssemblyMessageTestsCommon() {
    }

}
