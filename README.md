# KoresExperiments

**Comic:** This is an experiment project. Everything here is only made for fun and may explode if you put something wrong inside.

## Dynamic dispatch

```java
public class Person { 
    public String getName() { ... }
}

public class Cat { 
    public String getName() { ... }
}

public interface MyDispatcherInterface {
    String getName(Object o);
}

public class Main {
    public static void main(String[] args) {
        MyDispatcherInterface mdi = KoresExperimentsIndyHelper.create(MyDispatcherInterface.class, DynamicDispatch.EXPERIMENT);
        
        Person person = ...;
        Cat cat = ...;
        
        String personName = mdi.getName(person);
        String catName = mdi.getName(cat);
    }
}
```


## Late binding

```java
public class Main {
    public static void main(String[] args) {
        MyDispatcherInterface mdi = KoresExperimentsIndyHelper.create(MyDispatcherInterface.class, LateBinding.EXPERIMENT);
        
        Person person = ...;
        Cat cat = ...;
        
        String personName = mdi.getName(person); // Binded to 'Person' type, cannot invoke method of any other type anymore, but consecutive invocations are meant to be faster
        String catName = mdi.getName(cat); // ClassCastException
    }
}
```

## Experiment annotation


```java
@Experiment(DynamicDispatch.class)
public interface MyDispatcherInterface {
    @Dynamic // DynamicDispatch
    String getName(Object o);
}
```

```java
public class Main {
    public static void main(String[] args) {
        MyDispatcherInterface mdi = KoresExperimentsIndyHelper.createFromInterface(MyDispatcherInterface.class);
        
        Person person = ...;
        Cat cat = ...;
        
        String personName = mdi.getName(person);
        String catName = mdi.getName(cat);
    }
}
```
