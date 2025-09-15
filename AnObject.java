import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;  
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class AnObject {
    private Map<String, AnObject> slots;
    private List<String> messages;
    private Set<String> parents;
    private Object primitiveValue = null;
    private java.util.function.Function<AnObject, Object> primitiveFn = null;

    public AnObject() {
        this.slots = new HashMap<>();
        this.messages = new ArrayList<>();
        this.parents = new HashSet<>();
    }
    public void setPrimitiveValue(Object v) { this.primitiveValue = v; }
    public void setPrimitiveFunction(java.util.function.Function<AnObject,Object> f) { this.primitiveFn = f; }
    public boolean isPrimitiveValue()    { return primitiveValue != null; }
    public boolean isPrimitiveFunction() { return primitiveFn != null; }

    public Object evaluate(AnObject obj) {
        if (obj.isPrimitiveFunction()) {
        AnObject c = copy(obj);
        return obj.primitiveFn.apply(c);
        }
        if (obj.isPrimitiveValue()) {
            return copy(obj);
        }
        if (obj.messages.isEmpty()) {
            return obj;
        }
        else {
            AnObject copy = copy(obj);
            Object result = null;
            for (String s : new ArrayList<>(copy.messages)) {
                result = sendAMessage(copy, s);
            }
            return result;
        }
    }

    public AnObject copy(AnObject obj) {
        AnObject copy = new AnObject();
        copy.slots.putAll(obj.slots);
        copy.messages.addAll(obj.messages);
        copy.parents.addAll(obj.parents);
        copy.primitiveValue = obj.primitiveValue;
        copy.primitiveFn = obj.primitiveFn;
        return copy;
    }

    public Object sendAMessage(AnObject obj, String message) {
            Queue<AnObject> queue = new LinkedList<>();
            Set<AnObject> visited = new java.util.HashSet<>();
            queue.add(obj);

            while (!queue.isEmpty()) {
                AnObject current = queue.poll();
                if (visited.contains(current)) {
                    continue;
                }
                visited.add(current);

                if (current.slots.containsKey(message)) {
                    AnObject object = current.slots.get(message);
                    return evaluate(object);
                }

                for (String someParent : current.parents) {
                    AnObject parent = current.slots.get(someParent);
                    if (parent != null) {
                        queue.add(parent);
                    }
                }
            }
            return null;
    }
    
    public Object sendAMessageWithParameters(AnObject obj, String message, AnObject parameter) {
        Queue<AnObject> queue = new LinkedList<>();
        Set<AnObject> visited = new java.util.HashSet<>();
        queue.add(obj);

        while (!queue.isEmpty()) {
            AnObject current = queue.poll();
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            if (current.slots.containsKey(message)) {
                AnObject object = current.slots.get(message);
                if(object != null){
                    obj.slots.put("parameter", parameter);
                    return evaluate(object);
                }
            for (String someParent : current.parents) {
                AnObject parent = current.slots.get(someParent);
                if (parent != null) {
                    queue.add(parent);
                }
            }   
            }
        }
        return null;
    }

    public void assignSlot(AnObject obj, String objectName, AnObject obj2) {
        obj.slots.put(objectName, obj2);
    }

    public void makeParent(AnObject obj,String objName) {
        if (obj.slots.containsKey(objName)) {
                obj.parents.add(objName);
        }
        else {
            throw new IllegalArgumentException("No such slot to make parent: " + objName);
        }
    }

    public void assignParentSlot(AnObject obj, String objName, AnObject obj2){
        assignSlot(obj, objName, obj2);
        makeParent(obj, objName);
    }

    public String print(AnObject obj) {
        return obj.toString();
    }
    @Override
public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Object@").append(System.identityHashCode(this)).append(" {");

    if (!slots.isEmpty()) {
        sb.append("\n  slots:");
        for (Map.Entry<String, AnObject> e : slots.entrySet()) {
            String name = e.getKey();
            AnObject ref = e.getValue();
            boolean isParent = parents.contains(name);
            sb.append("\n    ").append(isParent ? "^" : "").append(name)
              .append(" -> Object@").append(System.identityHashCode(ref));
        }
    }
    if (!messages.isEmpty()) {
        sb.append("\n  messages: ").append(messages);
    }
    sb.append("\n}");
    return sb.toString();
}

}