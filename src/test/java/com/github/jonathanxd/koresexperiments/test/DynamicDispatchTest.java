/*
 *      KoresExperiments - CodeAPI Bytecode Experiments! <https://github.com/JonathanxD/CodeProxy>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2018 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.koresexperiments.test;

import com.github.jonathanxd.koresexperiments.KoresExperimentsIndyHelper;
import com.github.jonathanxd.koresexperiments.DynamicDispatch;
import com.github.jonathanxd.koresexperiments.annotation.Dynamic;
import com.github.jonathanxd.koresexperiments.annotation.Experiment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class DynamicDispatchTest {

    @Test
    public void dynamicDispatchTest() {
        Stringifier base = KoresExperimentsIndyHelper.createFromInterface(Stringifier.class);

        Person p = new PersonImpl("Mary", 30);
        Entity e = new EntityImpl("en");
        MyStringifier stringifier = new MyStringifier();
        Assertions.assertEquals("Hello", base.stringify(stringifier, "Hello"));
        Assertions.assertEquals("Person{name=Mary,arg=30}", base.stringify(stringifier, p));
        Assertions.assertEquals("Object[h] & Object[null] & Entity[Entity{id=en}]",
                base.stringify(stringifier, "h", null, e));

        Assertions.assertEquals("Person[Person{name=Mary,age=30}] & Object[null] & Entity[Entity{id=en}]",
                base.stringify(stringifier, p, null, e));
        Assertions.assertEquals("Object[Person{name=Mary,age=30}] & Object[null] & Object[null]",
                base.stringify(stringifier, p, null, null));

    }

    @Experiment(DynamicDispatch.class)
    public interface Stringifier {
        @Dynamic
        String stringify(Object receiver, Object v);

        @Dynamic
        String stringify(Object receiver, Object v, Object v2, Object v3);
    }

    public interface Person {
        String getName();

        int getAge();
    }

    public interface Entity {
        String getId();
    }

    public static class MyStringifier {
        public String stringify(Person p) {
            return "Person{name=" + p.getName() + ",arg=" + p.getAge() + "}";
        }

        public String stringify(String s) {
            return s;
        }

        public String stringify(Object o) {
            return "Object[" + Objects.toString(o) + "]";
        }

        public String stringify(Object o, Object o2, Object o3) {
            return "Object[" + Objects.toString(o) + "] & Object[" + Objects.toString(o2) + "] & Object[" + Objects.toString(o3) + "]";
        }

        public String stringify(Object o, Object o2, Entity o3) {
            return "Object[" + Objects.toString(o) + "] & Object[" + Objects.toString(o2) + "] & Entity[" + Objects.toString(o3) + "]";
        }

        public String stringify(Person o, Object o2, Entity o3) {
            return "Person[" + Objects.toString(o) + "] & Object[" + Objects.toString(o2) + "] & Entity[" + Objects.toString(o3) + "]";
        }
    }

    public static class EntityImpl implements Entity {

        private final String id;

        public EntityImpl(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public String toString() {
            return "Entity{id=" + this.getId() + "}";
        }
    }

    public static class PersonImpl implements Person {
        private final String name;
        private final int age;

        public PersonImpl(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getAge() {
            return this.age;
        }

        @Override
        public String toString() {
            return "Person{name=" + this.getName() + ",age=" + this.getAge() + "}";
        }
    }
}
