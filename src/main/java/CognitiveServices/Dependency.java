package CognitiveServices;

public class Dependency {
    private String label;
    private String target;
    private String pointer;

    public Dependency(String label, String target, String pointer) {
        this.label = label;
        this.target = target;
        this.pointer = pointer;
    }

    public String getLabel() {
        return label;
    }

    public String getTarget() {
        return target;
    }

    public String getPointer() {
        return pointer;
    }

    @Override
    public String toString() {
        return label + "(" + target + "," + pointer + ")";
    }
}
