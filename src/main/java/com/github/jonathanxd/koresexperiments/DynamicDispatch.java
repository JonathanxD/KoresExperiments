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
package com.github.jonathanxd.koresexperiments;

import com.github.jonathanxd.kores.base.InvokeType;
import com.github.jonathanxd.kores.common.MethodInvokeSpec;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.koresexperiments.annotation.Dynamic;
import com.github.jonathanxd.koresexperiments.annotation.Static;
import com.github.jonathanxd.koresexperiments.annotation.internal.AccessedAtRuntime;
import com.github.jonathanxd.koresexperiments.experiment.KoresIndyExperiment;
import com.github.jonathanxd.iutils.annotation.Singleton;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Dynamically invokes the method in the target instance. The bootstrap binds the invocation to
 * method handle returned by {@link DynamicMethodInvoker#generateAndGetMethodHandle(String,
 * MethodType, int, int)}.
 *
 * The main difference between this invocation and {@code invokevirtual}, {@code invokeinterface}
 * and {@code invokestatic} is that the method is resolved base on the runtime type of the instance,
 * instead of the compile-time type, also there is an option to resolve argument types dynamically,
 * which can be enabled through {@link Dynamic}.
 */
@Singleton("EXPERIMENT")
public class DynamicDispatch implements KoresIndyExperiment {
    public static final DynamicDispatch EXPERIMENT = new DynamicDispatch();
    public static final MethodInvokeSpec BOOTSTRAP_SPEC = new MethodInvokeSpec(
            InvokeType.INVOKE_STATIC,
            new MethodTypeSpec(
                    DynamicDispatch.class,
                    "bind",
                    Factories.typeSpec(CallSite.class,
                            MethodHandles.Lookup.class,
                            String.class,
                            MethodType.class,
                            Integer.TYPE,
                            Integer.TYPE
                    )
            )
    );

    private DynamicDispatch() {
    }

    @AccessedAtRuntime
    public static CallSite bind(MethodHandles.Lookup caller,
                                String name,
                                MethodType type,
                                int invokeType,
                                int dynamic) {
        return new ConstantCallSite(DynamicMethodInvoker.generateAndGetMethodHandle(name, type, invokeType, dynamic).bindTo(caller));
    }

    @Override
    public MethodInvokeSpec getBootstrapMethod() {
        return BOOTSTRAP_SPEC;
    }

    @Override
    public String getName() {
        return "DynamicDispatch";
    }

    @Override
    public void handle(Method m, List<Object> args) {
        int invokationType = m.isAnnotationPresent(Static.class) ? InternalUtil.STATIC : InternalUtil.VIRTUAL;
        int dynamic = m.isAnnotationPresent(Dynamic.class) ? InternalUtil.DYNAMIC : InternalUtil.NORMAL;
        args.add(invokationType);
        args.add(dynamic);
    }
}
