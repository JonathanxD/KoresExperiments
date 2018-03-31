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
import com.github.jonathanxd.koresexperiments.experiment.KoresIndyExperiment;
import com.github.jonathanxd.iutils.annotation.Singleton;
import com.github.jonathanxd.iutils.exception.RethrowException;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

/**
 * Late binding of a {@code method invocation} to a method resolved statically (we will talk about
 * it soon).
 *
 * The first invocation of the method will be bind to {@link #bind(LazyCallSite, Object[]) bind
 * fallback}, this method will resolve the target of invocation statically, in other words, based on
 * {@code returnType} and {@code parameterTypes} provided to bootstrap, after resolution of the
 * {@link MethodHandle} that matches the {@link MethodType} provided to bootstrap, the {@link
 * CallSite#getTarget() call site target} will be changed to resolved one, and all subsequent
 * invocations will be directly to this {@link MethodHandle resolved handle}. Note that, after
 * resolved, the {@code invokedynamic} bound to {@link LateBinding} will be always linked to {@link
 * MethodHandle resolved method handle}, so calling it with a different instance will not cause the
 * method to be resolved again, leading to a unexpected behavior (unless the object is of the same
 * type).
 *
 * LateBinding is only recommended when the receiver instance reference will never change by the
 * semantic.
 *
 * This is not the same as {@link DynamicDispatch}, that always resolves the method based on
 * instance.
 */
@Singleton("EXPERIMENT")
public class LateBinding implements KoresIndyExperiment {
    public static final LateBinding EXPERIMENT = new LateBinding();
    public static final MethodInvokeSpec BOOTSTRAP_SPEC = new MethodInvokeSpec(
            InvokeType.INVOKE_STATIC,
            new MethodTypeSpec(
                    LateBinding.class,
                    "resolve",
                    Factories.typeSpec(CallSite.class,
                            MethodHandles.Lookup.class,
                            String.class,
                            MethodType.class
                    )
            )
    );
    private static final MethodHandle BIND_METHOD;
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    static {
        try {
            BIND_METHOD = LOOKUP.findStatic(
                    LateBinding.class,
                    "bind",
                    MethodType.methodType(Object.class, LazyCallSite.class, Object[].class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw RethrowException.rethrow(e);
        }
    }

    private LateBinding() {
    }

    public static CallSite resolve(MethodHandles.Lookup caller,
                                   String name,
                                   MethodType type) {

        LazyCallSite lazyCallSite = new LazyCallSite(type, caller, name);

        MethodHandle handle = BIND_METHOD.bindTo(lazyCallSite).asCollector(Object[].class, type.parameterCount()).asType(type);

        lazyCallSite.setTarget(handle);

        return lazyCallSite;
    }

    private static Object bind(LazyCallSite callSite, Object[] args) {
        try {
            MethodHandles.Lookup caller = callSite.getCallerLookup();
            String name = callSite.getName();

            Object instance = args[0];
            Class<?> instanceClass = instance.getClass();
            MethodType type = callSite.getTarget().type().dropParameterTypes(0, 1);

            MethodHandle resolved = caller.findVirtual(instanceClass, name, type);

            callSite.setTarget(resolved.asType(callSite.getTarget().type()));

            return resolved.invokeWithArguments(args);
        } catch (Throwable e) {
            throw RethrowException.rethrow(e);
        }
    }

    @Override
    public MethodInvokeSpec getBootstrapMethod() {
        return BOOTSTRAP_SPEC;
    }

    @Override
    public String getName() {
        return "LateBinding";
    }

    static class LazyCallSite extends MutableCallSite {

        private final MethodHandles.Lookup callerLookup;
        private final String name;

        public LazyCallSite(MethodType type, MethodHandles.Lookup callerLookup, String name) {
            super(type);
            this.callerLookup = callerLookup;
            this.name = name;
        }

        public LazyCallSite(MethodHandle target, MethodHandles.Lookup callerLookup, String name) {
            super(target);
            this.callerLookup = callerLookup;
            this.name = name;
        }

        public MethodHandles.Lookup getCallerLookup() {
            return this.callerLookup;
        }

        public String getName() {
            return this.name;
        }
    }

}
