# ICS-313-Self-Object-Model-Java-Implementation

This is a (crude) Java implementation of the Self Object Model (SOM) for my ICS 313 assignment 01. It builds the basic outline for how the SOM works.

# Why Java?
Java was the first language I ever learned, and so I am very comfortable writing Java code. Although it can be a very verbose language, I believe the additional details can help readers understand what exactly is going on. In regards to the technical reason for writing in Java, I prefer the object-oriented programming model as it allows for easy object creation, specification, and manipulation.

# The AnObject Class
In the SOM, an object is defined as a collection of slots where each slot contains a string name and a reference to another object. Objects in SOM are capable of sending messages to either themselves or another object as a way to communicate instructions to each other. Like nodes in a tree data structure, objects also have parents that they may refer to (useful for modeling recursion).

In my implementation, I used a HashMap/hashtable to take in a key/value pair where the key parameter is the string name of the object and the value parameter is the object's value. Messages are held in a list, and each index is a separate message. This is to ensure message history is preserved, which proves useful when we need to copy an object. A feature I didn't know about until recently is the Set structure. Essentially, it behaves like a HashMap, but the value field remains null. This is useful for setting up the parent structure of each object because we can assign each object a parent in tandem with our main hashmap, and, since there's no need to assign a value to the parent slots, we can keep them null. 

        private Map<String, AnObject> slots;
        private List<String> messages;
        private Set<String> parents;
        private Object primitiveValue = null;
        private java.util.function.Function<AnObject, Object> primitiveFn = null;
        
        public AnObject() { //initalizing our data structures in the constructor
        this.slots = new HashMap<>();
        this.messages = new ArrayList<>();
        this.parents = new HashSet<>();
    }

The last two private variable fields: 
primitiveValue - stores immutable data; evaluate returns a copy with the same value. We initialize it as a null value, but we have a method to set this later.
primitiveFn - stores built-in behavior; evaluate copies the object and executes the function (i.e., add, inc, etc.) on that copy, returning the function’s result.

# Methods:
## Set and Boolean Check For Primitive Values
These first four methods are simply setters/boolean checkers for the aforementioned primitiveValue and primitiveFn fields. These come in handy for the evaluate() method. In Java, primitive values are not considered objects themselves, and thus need to be wrapped in an object class (i.e., int 5 --> Integer 5).

        public void setPrimitiveValue(Object v) { this.primitiveValue = v; }
        public void setPrimitiveFunction(java.util.function.Function<AnObject,Object> f) { this.primitiveFn = f; }
        public boolean isPrimitiveValue()    { return primitiveValue != null; }
        public boolean isPrimitiveFunction() { return primitiveFn != null; }

## evaluate
We want to be able to evaluate our objects, and this method does exactly that. We take in an object as a parameter and run checks on it first. If the object is a primitive function, then we create a new copy of the object and return the copy with the function applied to it. If the object is a primitive value, we just copy it and return the copy. If there are no messages attached to the object, there's nothing to evaluate, and so we return the object without copying it. Finally, if these conditions are not met, then we just copy the object and its messages, create a new dummy object (result), send the messages to our dummy object in chronological order, and then finally return the dummy object. The reason for this convolution is to avoid having to mutate our original object.

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
