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

import com.github.jonathanxd.iutils.annotation.Singleton;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.exception.RethrowException;
import com.github.jonathanxd.iutils.function.checked.function.CFunction;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.Instructions;
import com.github.jonathanxd.kores.base.ClassDeclaration;
import com.github.jonathanxd.kores.base.FieldDeclaration;
import com.github.jonathanxd.kores.base.IfStatement;
import com.github.jonathanxd.kores.base.KoresModifier;
import com.github.jonathanxd.kores.base.KoresParameter;
import com.github.jonathanxd.kores.base.Line;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.TypeSpec;
import com.github.jonathanxd.kores.base.VariableAccess;
import com.github.jonathanxd.kores.bytecode.BytecodeClass;
import com.github.jonathanxd.kores.bytecode.classloader.CodeClassLoader;
import com.github.jonathanxd.kores.bytecode.processor.BytecodeGenerator;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.factory.PartFactory;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.util.ClassUtil;
import com.github.jonathanxd.kores.util.conversion.ConversionsKt;
import com.github.jonathanxd.koresexperiments.annotation.internal.AccessedAtRuntime;
import com.github.jonathanxd.koresexperiments.experiment.KoresExperiment;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import kotlin.collections.CollectionsKt;

/**
 * Generates a class with a static method that resolves and invokes a method based on class of the
 * receiver (aka {@code this}), this class is the second part of {@link LateBinding} and is used to
 * generate a reified class, that avoids {@code boxing} of primitive values and {@code casts}. The
 * generated class invokes the method using {@link MethodHandle#invokeExact(Object...)}, which is a
 * polymorphic method.
 *
 * The generated method looks like this:
 * <pre>
 *     {@code
 *     public static String method(Lookup callerLookup, Object arg0, int a) {
 *         return callerLookup.findVirtual(arg0.getClass(), "hello",
 * MethodType.methodType(String.class, Integer.TYPE)).bindTo(arg0).invokeExact(a);
 *     }
 *     }
 * </pre>
 *
 * The generated method is commonly directly linked to an {@code invokedynamic}.
 */
@Singleton("EXPERIMENT")
public class DynamicMethodInvoker implements KoresExperiment {
    public static final DynamicMethodInvoker EXPERIMENT = new DynamicMethodInvoker();
    private static final Type LOOKUP_TYPE = MethodHandles.Lookup.class;
    private static final String LOOKUP_NAME = "callerLookup";
    private static final Type MT_TYPE = MethodType.class;
    private static final String MT_NAME = "METHOD_TYPE";
    private static final CodeClassLoader loader = new CodeClassLoader(
            DynamicMethodInvoker.class.getClassLoader());
    private static final MethodHandles.Lookup THIS_LOOKUP = MethodHandles.publicLookup();

    private DynamicMethodInvoker() {
    }

    /**
     * Generates a class with a method that resolves the {@link MethodHandle} of a method with
     * {@code name} and {@code signature} of receiver instance (which is the second argument of the
     * method) based on its class (retrieved using {@link Object#getClass()}), and invokes resolved
     * method using {@link MethodHandle#invokeExact(Object...)}.
     *
     * @param name       Name of the method to resolve (the generated method has the same name).
     * @param signature  Signature of method to resolve (the generated method has an additional
     *                   {@link java.lang.invoke.MethodHandles.Lookup} parameter plus this
     *                   signature).
     * @param invokeType Type of invocation, this will be used to determine how to resolve and
     *                   invoke the method.
     * @param dynamic    Whether the generation should be full dynamic, meaning that argument types
     *                   are resolved based on arguments instance.
     * @return A generated class with the.
     * @see DynamicMethodInvoker
     */
    public static Class<?> generate(String name, MethodType signature, int invokeType,
                                    int dynamic) {
        ClassDeclaration declaration = createDeclaration(name, signature, invokeType, dynamic);
        BytecodeGenerator bg = InternalUtil.getThreadBytecodeGenerator();
        List<BytecodeClass> process = bg.process(declaration);
        Debug.save(process, DynamicMethodInvoker.EXPERIMENT.getName());
        return loader.define(process);
    }

    /**
     * Same as {@link #generate(String, MethodType, int, int)}, but instead, returns the {@link
     * MethodHandle} resolved to generated method, you can use {@link MethodHandle#bindTo(Object)}
     * to bind the first argument to caller {@link java.lang.invoke.MethodHandles.Lookup}.
     *
     * For additional information see {@link #generate(String, MethodType, int, int)} documentation
     *
     * @param name       Name of the method to resolve.
     * @param signature  Signature of method to resolve.
     * @param invokeType Type of invocation.
     * @param dynamic    Type of dynamic generation, full (argument types are resolved) or normal
     *                   (argument types are exact, no dynamic resolution).
     * @return Method handle of class generated by {@link #generate(String, MethodType, int, int)}.
     * @see #generate(String, MethodType, int, int)
     * @see DynamicMethodInvoker
     */
    public static MethodHandle generateAndGetMethodHandle(String name, MethodType signature,
                                                          int invokeType, int dynamic) {
        try {
            Class<?> generate = DynamicMethodInvoker.generate(name, signature, invokeType, dynamic);

            return THIS_LOOKUP.findStatic(generate, name,
                    signature.insertParameterTypes(0, MethodHandles.Lookup.class));
        } catch (Throwable t) {
            throw RethrowException.rethrow(t);
        }
    }

    /**
     * Resolves a method handle of method with specified {@code name} and specified signature
     * ({@code mt}) in {@code receiver}.
     *
     * @param lookup     Lookup to use to resolve the method.
     * @param receiver   Receiver instance.
     * @param name       Name of method to invoke.
     * @param mt         Signature of method.
     * @param invokeType Type of invocation of the method, either {@link InternalUtil#VIRTUAL} or
     *                   {@link InternalUtil#STATIC}.
     * @return Resolved method handle.
     * @throws Throwable If resolution fails.
     */
    @AccessedAtRuntime
    public static MethodHandle resolveMethodHandleStatic(MethodHandles.Lookup lookup,
                                                         Object receiver,
                                                         String name,
                                                         MethodType mt,
                                                         int invokeType) throws Throwable {


        switch (invokeType) {
            case InternalUtil.VIRTUAL: {
                return lookup.bind(receiver, name, mt);
            }
            case InternalUtil.STATIC: {
                return lookup.findStatic(receiver.getClass(), name, mt);
            }
            default: {
                throw new IllegalArgumentException("Invalid invocation type '" + invokeType + "'!");
            }
        }

    }

    /**
     * Resolves the method handle of method with specified {@code name} and specified signature
     * ({@code mt} with {@code argTypes} as argument types) in ({@code receiver}).
     *
     * This resolves the method dynamically depending on a variety of combinations of argument
     * types. This means that it will try to resolve method using {@code argTypes} as arguments
     * types and return type of {@code mt}, and then try to resolve with all combinations of
     * super-types of {@code argTypes}.
     *
     * @param lookup     Lookup to use to resolve method.
     * @param receiver   Receiver to find method.
     * @param name       Name of the method.
     * @param mt         Method signature (parameter types ignored).
     * @param invokeType Type of invocation, either {@link InternalUtil#VIRTUAL} or {@link
     *                   InternalUtil#STATIC}.
     * @param argTypes   Types of arguments of method to resolve (all combinations of super-types
     *                   will be used to resolve).
     * @return Resolved method.
     * @throws Throwable If resolution fails.
     */
    @AccessedAtRuntime
    public static MethodHandle resolveMethodHandleDynamic(MethodHandles.Lookup lookup,
                                                          Object receiver,
                                                          String name,
                                                          MethodType mt,
                                                          int invokeType,
                                                          Class<?>[] argTypes) throws Throwable {

        Exception curr;
        CFunction<Class<?>[], MethodHandle> tryResolve =
                c -> resolveMethodHandleStatic(lookup, receiver, name,
                        MethodType.methodType(mt.returnType(), c), invokeType);

        try {
            return tryResolve.applyChecked(argTypes).asType(mt);
        } catch (Exception e) {
            curr = e;
        }

        int combinations = 1;
        Class[][] types = new Class[argTypes.length][];
        int[] size = new int[argTypes.length];
        int[] counter = new int[argTypes.length];

        for (int i = 0; i < argTypes.length; i++) {
            Class<?> argType = argTypes[i];

            Collection<Class<?>> allSubclasses = ClassUtil.getAllSubclasses(argType);
            types[i] = new Class[allSubclasses.size() + 2];
            types[i][0] = argType;

            int x = 1;
            for (Class<?> allSubclass : allSubclasses) {
                types[i][x++] = allSubclass;
            }

            types[i][x] = Object.class;

            size[i] = types[i].length;
            combinations *= types[i].length;
        }

        for (int c = combinations; c > 0; --c) {
            Class[] cnv = argTypes.clone();
            for (int i = 0; i < types.length; i++) {
                cnv[i] = types[i][counter[i]];
            }

            try {
                return tryResolve.applyChecked(cnv).asType(mt);
            } catch (Exception e) {
                curr.addSuppressed(e);
            }

            for (int iinc = types.length - 1; iinc >= 0; --iinc) {
                if (counter[iinc] + 1 < size[iinc]) {
                    ++counter[iinc];
                    break;
                }
                counter[iinc] = 0;
            }
        }

        throw RethrowException.rethrow(curr);
    }

    private static ClassDeclaration createDeclaration(String name, MethodType signature,
                                                      int invokeType, int dynamic) {
        String fullname = InternalUtil
                .createGenClassName(DynamicMethodInvoker.EXPERIMENT.getName(), "Dyn_" + name);

        return ClassDeclaration.Builder.builder()
                                       .publicModifier()
                                       .specifiedName(fullname)
                                       .fields(field(signature))
                                       .methods(impl(name, signature, invokeType, dynamic))
                                       .build();
    }

    private static FieldDeclaration field(MethodType signature) {
        return FieldDeclaration.Builder.builder()
                                       .modifiers(KoresModifier.PRIVATE, KoresModifier.STATIC,
                                               KoresModifier.FINAL)
                                       .type(MT_TYPE)
                                       .name(MT_NAME)
                                       .value(mt(cleanSignature(signature)))
                                       .build();
    }

    private static MethodType cleanSignature(MethodType mt) {
        return mt.dropParameterTypes(0, 1);
    }

    private static MethodDeclaration impl(String name, MethodType signature, int invokeType,
                                          int dynamic) {

        VariableAccess lookup = Factories.accessVariable(LOOKUP_TYPE, LOOKUP_NAME);
        VariableAccess receiver = Factories.accessVariable(Object.class, "arg0");
        List<KoresParameter> parameters = getParameters(signature.parameterList());
        List<Instruction> accesses = ConversionsKt.getAccess(parameters);
        List<Instruction> argumentsToForward = accesses.subList(2, accesses.size());
        MethodType targetSignature = cleanSignature(signature);

        Class<?> rType = signature.returnType();

        Instruction find = lookup(lookup, receiver, name, invokeType, dynamic, argumentsToForward);

        return MethodDeclaration.Builder.builder()
                                        .modifiers(KoresModifier.PUBLIC, KoresModifier.STATIC)
                                        .returnType(signature.returnType())
                                        .parameters(parameters)
                                        .name(name)
                                        .body(Instructions.fromPart(Factories
                                                .returnValue(rType,
                                                        invokeExact(find, argumentsToForward,
                                                                targetSignature))))
                                        .build();
    }

    private static Instruction bind(Instruction mh, Instruction instruction) {
        return InvocationFactory.invokeVirtual(MethodHandle.class,
                mh,
                "bindTo",
                Factories.typeSpec(MethodHandle.class, Object.class),
                Collections.singletonList(instruction)
        );
    }

    private static Instruction invokeExact(Instruction mh, List<Instruction> arguments,
                                           MethodType signature) {
        return InvocationFactory.invokeVirtual(MethodHandle.class,
                mh,
                "invokeExact",
                new TypeSpec(signature.returnType(), signature.parameterList()),
                arguments
        );
    }

    private static Instruction getPartClass(Instruction receiver, int alt) {
        return new Line.TypedLine(1, IfStatement.Builder.builder()
                                                        .expressions(
                                                                Factories.checkNotNull(receiver))
                                                        .body(Instructions.fromPart(
                                                                InvocationFactory
                                                                        .invokeVirtual(Object.class,
                                                                                receiver,
                                                                                "getClass",
                                                                                Factories.typeSpec(
                                                                                        Class.class),
                                                                                Collections
                                                                                        .emptyList()
                                                                        )
                                                        ))
                                                        .elseStatement(Instructions.fromPart(
                                                                InvocationFactory
                                                                        .invokeVirtual(MT_TYPE,
                                                                                Factories
                                                                                        .accessStaticField(
                                                                                                MT_TYPE,
                                                                                                MT_NAME),
                                                                                "parameterType",
                                                                                Factories.typeSpec(
                                                                                        Class.class,
                                                                                        Integer.TYPE),
                                                                                Collections
                                                                                        .singletonList(
                                                                                                Literals.INT(
                                                                                                        alt))
                                                                        )
                                                        ))
                                                        .build(), Class.class);
    }

    private static Instruction lookup(Instruction lookupInstance,
                                      Instruction instance,
                                      String name,
                                      int invokeType,
                                      int dynamic,
                                      List<Instruction> args) {

        switch (dynamic) {
            case InternalUtil.NORMAL: {
                return InvocationFactory.invokeStatic(
                        DynamicMethodInvoker.class,
                        "resolveMethodHandleStatic",
                        Factories.typeSpec(MethodHandle.class, LOOKUP_TYPE, Object.class,
                                String.class, MethodType.class, Integer.TYPE),
                        Collections3.listOf(lookupInstance, instance,
                                Literals.STRING(name),
                                Factories.accessStaticField(MT_TYPE, MT_NAME),
                                Literals.INT(invokeType))
                );
            }
            case InternalUtil.DYNAMIC: {
                return InvocationFactory.invokeStatic(
                        DynamicMethodInvoker.class,
                        "resolveMethodHandleDynamic",
                        Factories.typeSpec(MethodHandle.class, LOOKUP_TYPE, Object.class,
                                String.class, MethodType.class, Integer.TYPE, Class[].class),
                        Collections3.listOf(lookupInstance, instance,
                                Literals.STRING(name),
                                Factories.accessStaticField(MT_TYPE, MT_NAME),
                                Literals.INT(invokeType), Factories.createArray(Class[].class,
                                        Collections.singletonList(Literals.INT(args.size())),
                                        CollectionsKt.mapIndexed(args, (i, c) ->
                                                DynamicMethodInvoker.getPartClass(c, i))
                                ))
                );
            }

            default:
                throw new IllegalArgumentException("Invalid dynamic flag '" + dynamic + "'");
        }


    }

    private static Instruction lookupVirtually(Instruction lookupInstance,
                                               Instruction instance,
                                               String name) {
        return InvocationFactory.invokeVirtual(
                LOOKUP_TYPE,
                lookupInstance,
                "bind",
                Factories
                        .typeSpec(MethodHandle.class, Object.class, String.class, MethodType.class),
                Collections3.listOf(instance, Literals.STRING(name),
                        Factories.accessStaticField(MT_TYPE, MT_NAME))
        );
    }

    private static Instruction lookupFor(Instruction lookupInstance,
                                         String type,
                                         Instruction refcInstance,
                                         String name,
                                         MethodType mt) {
        return InvocationFactory.invokeVirtual(
                LOOKUP_TYPE,
                lookupInstance,
                "find" + type,
                Factories.typeSpec(MethodHandle.class, Class.class, String.class, MethodType.class),
                Collections3.listOf(refcInstance, Literals.STRING(name),
                        Factories.accessStaticField(MT_TYPE, MT_NAME))
        );
    }

    private static Instruction mt(MethodType mt) {
        switch (mt.parameterCount()) {
            case 0: {
                return InvocationFactory.invokeStatic(
                        MethodType.class,
                        "methodType",
                        Factories.typeSpec(MethodType.class, Class.class),
                        Collections3.listOf(Literals.CLASS(mt.returnType()))
                );
            }
            case 1: {
                return InvocationFactory.invokeStatic(
                        MethodType.class,
                        "methodType",
                        Factories.typeSpec(MethodType.class, Class.class, Class.class),
                        Collections3.listOf(Literals.CLASS(mt.returnType()),
                                Literals.CLASS(mt.parameterType(0)))
                );
            }
            default: {
                return InvocationFactory.invokeStatic(
                        MethodType.class,
                        "methodType",
                        Factories.typeSpec(MethodType.class, Class.class, Class[].class),
                        Collections3.listOf(Literals.CLASS(mt.returnType()),
                                Factories.createArray(Class[].class,
                                        Collections
                                                .singletonList(Literals.INT(mt.parameterCount())),
                                        mt.parameterList().stream().map(Literals::CLASS)
                                          .collect(Collectors.toList())
                                )
                        )
                );
            }
        }
    }

    private static List<KoresParameter> getParameters(List<? extends Type> types) {
        List<KoresParameter> parameters = new ArrayList<>(types.size());

        parameters.add(PartFactory.koresParameter().type(LOOKUP_TYPE).name(LOOKUP_NAME).build());

        for (int i = 0; i < types.size(); i++) {
            Type type = types.get(i);
            parameters.add(PartFactory.koresParameter().type(type).name("arg" + i).build());
        }

        return parameters;
    }

    @Override
    public String getName() {
        return "DynamicMethodInvoker";
    }
}
