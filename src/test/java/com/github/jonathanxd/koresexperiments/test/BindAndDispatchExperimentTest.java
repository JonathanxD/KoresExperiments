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
import com.github.jonathanxd.koresexperiments.LateBinding;
import com.github.jonathanxd.iutils.object.Either;
import com.github.jonathanxd.iutils.object.Try;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BindAndDispatchExperimentTest {

    @Test
    public void lateBindingTest() {
        Base base = KoresExperimentsIndyHelper.create(Base.class, LateBinding.EXPERIMENT);

        MyObject object = new MyObject();
        Assertions.assertEquals("Hello man", base.hello(object));

        Either<Exception, String> tryEx = Try.TryEx(() -> base.hello(new MyObject2()));

        Assertions.assertTrue(tryEx.isLeft());
        Assertions.assertTrue(tryEx.getLeft() instanceof ClassCastException);
    }

    @Test
    public void dynamicDispatchTest() {
        Base base = KoresExperimentsIndyHelper.create(Base.class, DynamicDispatch.EXPERIMENT);

        MyObject object = new MyObject();
        Assertions.assertEquals("Hello man", base.hello(object));
        Assertions.assertEquals("Hello man2", base.hello(new MyObject2()));
    }

    @Test
    public void dynamicDispatchTestWP() {
        BaseWithInt base = KoresExperimentsIndyHelper.create(BaseWithInt.class, DynamicDispatch.EXPERIMENT);

        MyObjectWInt object = new MyObjectWInt();
        Assertions.assertEquals("Hello 9 times.", base.hello(object, 9));

        Either<Exception, String> tryEx = Try.TryEx(() -> base.hello(new MyObject2(), 2));

        Assertions.assertTrue(tryEx.isLeft());
        Assertions.assertTrue(tryEx.getLeft() instanceof NoSuchMethodException);
    }

    @Test
    public void dynamicDispatchTestDyn() {
        BaseWithInt base = KoresExperimentsIndyHelper.create(BaseWithInt.class, DynamicDispatch.EXPERIMENT);

        MyObjectWInt object = new MyObjectWInt();
        Assertions.assertEquals("Hello 9 times.", base.hello(object, 9));

        Either<Exception, String> tryEx = Try.TryEx(() -> base.hello(new MyObject2(), 2));

        Assertions.assertTrue(tryEx.isLeft());
        Assertions.assertTrue(tryEx.getLeft() instanceof NoSuchMethodException);
    }

    public interface Base {
        String hello(Object o);
    }

    public interface BaseWithInt {
        String hello(Object o, int n);
    }

    public class MyObject2 {
        public String hello() {
            return "Hello man2";
        }
    }

    public class MyObject {
        public String hello() {
            return "Hello man";
        }
    }

    public class MyObjectWInt {
        public String hello(int n) {
            return "Hello " + n + " times.";
        }
    }


}
