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
                        result = sendAMessage(copy, s); //method shown below
                    }
                    return result;
                }
            }

## sendAMessage
Here we take in some object and a string as parameters. The goal is to send the string message to the object. As each object contains slots, we want to check all of them to find if any of them contains the message as a key. If this message is not found in the original object's slots, we continue the pattern of checking the next level (all parents' slots, then grandparents' slots, etc.). This can be done via a Breadth-First-Search using a queue while managing our visited objects via a set. Once we have found our slot containing the message key, we return the evaluated object. Otherwise, we return a null value to indicate no message key was found.

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

## sendAMessageWithParameters
The idea here is very similar to the previous method. We still take in an object and a string message; however, this time we are attaching another object to this message, which will contain a parameter. We do the same steps as before, finding the message key in one of the slots via BFS. If found, we attach our parameter object to the message and return the newly evaluated object.

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
                            obj.slots.put("parameter", parameter); //Where we attach the parameter object to the message
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

## assignSlot
This method is simple but essential to the structure of how the SOM works. As objects need to be able to reference each other, we can do this by assigning an object to another object's slot. We take two objects and a string as parameters here. obj1 is the assigning object that will contain the reference to obj2, the assignee. 

        public void assignSlot(AnObject obj, String objectName, AnObject obj2) {
                obj.slots.put(objectName, obj2);
            }

## makeParent
Another integral feature of the SOM is letting objects refer directly to a parent. This is what the parents list structure we defined earlier is for. We first check if the object (objName) we want to define as a parent is referenced inside some object that is to be the child (obj). If it is, we can add that object to the parents list to keep it tracked as a parent.

        public void makeParent(AnObject obj,String objName) {
                if (obj.slots.containsKey(objName)) {
                        obj.parents.add(objName);
                }
                else {
                    throw new IllegalArgumentException("No such slot to make parent: " + objName);
                }
            }

## assignParentSlot
Here, we combine assignSlot() and makeParent() to create a slot in our first object for a new second object, and then we make that new object the parent of our first object.

        public void assignParentSlot(AnObject obj, String objName, AnObject obj2){
                assignSlot(obj, objName, obj2);
                makeParent(obj, objName);
            }

## print
Returns the object as a string representation using an overridden toString() method

        public String print(AnObject obj) {
                return obj.toString();
            }
    
## toString
This will be how we build the string representation of any object: Header -> slots (if any) -> messages (if any)

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
        
 ### Header: 
        sb.append("Object@").append(System.identityHashCode(this)).append(" {");
        - Shows the object id as "Object@<some number>" 
        - System.identityHashCode(this) is the identity hash and not the memory address of the object

### slots: 

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
            - prints a leading ^ if the slot is marked as a parent
            - prints the slot name
            - prints -> Object@<id> where <id> is the identity hash of the referenced object

### messages:
        if (!messages.isEmpty()) {
                sb.append("\n  messages: ").append(messages);
            }
        - prints the list of messages in chronological order


