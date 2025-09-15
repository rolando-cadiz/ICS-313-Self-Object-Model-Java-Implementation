# ICS-313-Self-Object-Model-Java-Implementation

This is a (crude) Java implementation of the Self Object Model (SOM) for my ICS 313 assignment 01. It builds the basic outline for how the SOM works.

# Why Java?
Java was the first language I ever learned and so I am very confortable writing Java code. Although it can be a very verbose language, I believe the additional details can help readers understand what exactly is going on. In regards to the technical reason for writing in Java, I prefer the Object Oriented Programming model as it allows for easy object creation and specification.

## The AnObject Class
In the SOM, an object is defined as a collection of slots where each slot contains a string name and a reference to another object. Objects in SOM are capable of sending messages to either themselves or another object as a way to communicate instructions to eachother. Like nodes in a tree data structure, objects also have parents that they may refer to (useful for modeling recursion).

In my implementation, I used a hashmap/hashtable to take in a key/value pair where the key parameter is the string name of the object and the value parameter is the objects value. Messages are held in a list and each index is a seperate message. This is to ensure message history is preserved which proves useful when we need to copy an object. A feature I didn't know about until recent is the Set structure. Essentially, it behaves like a hashmap but the value field remains null. This is useful for setting up the parent structure of each object because we can assign each object a parent in tandem with our main hashmap and, since theres no need to assign a value to the parent slots, we can keep them null. 

        private Map<String, AnObject> slots;
        private List<String> messages;
        private Set<String> parents;

The last two fields: 
primitiveValue - stores immutable data; evaluate returns a copy with the same value.
primitiveFn - stores built-in behavior; evaluate copies the object and executes the function (i.e. add, inc,etc.) on that copy, returning the function’s result.

        private Object primitiveValue = null;
        private java.util.function.Function<AnObject, Object> primitiveFn = null;
