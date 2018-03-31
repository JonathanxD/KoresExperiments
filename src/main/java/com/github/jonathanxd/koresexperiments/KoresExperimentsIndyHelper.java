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

import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.exception.RethrowException;
import com.github.jonathanxd.kores.Instructions;
import com.github.jonathanxd.kores.base.ClassDeclaration;
import com.github.jonathanxd.kores.base.KoresModifier;
import com.github.jonathanxd.kores.base.KoresParameter;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.VariableAccess;
import com.github.jonathanxd.kores.bytecode.BytecodeClass;
import com.github.jonathanxd.kores.bytecode.classloader.CodeClassLoader;
import com.github.jonathanxd.kores.common.DynamicMethodSpec;
import com.github.jonathanxd.kores.factory.DynamicInvocationFactory;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.util.conversion.ConversionsKt;
import com.github.jonathanxd.koresexperiments.experiment.KoresIndyExperiment;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import kotlin.collections.CollectionsKt;

/**
 * Helper that generates a class that implements interface methods and use {@code invokedynamic} to
 * dynamically bind invocations using {@link KoresIndyExperiment#getBootstrapMethod() indy
 * experiments bootstrap}.
 *
 * Note that the interface must have a first argument that is the receiver, and other arguments is
 * the arguments to forward to method invocation.
 *
 * Example of a valid interface:
 *
 * <pre>
 * {@code
 * public interface Printer {
 *     void println(Object o, String s);
 *     void println(Object o, int i);
 * }
 * }
 * </pre>
 */
public class KoresExperimentsIndyHelper {

    /**
     * Generates a class that implements abstract methods of {@code itf} with dynamic invocations of
     * methods of the receiver object with its arguments. This method uses {@link
     * com.github.jonathanxd.koresexperiments.annotation.Experiment} annotation to determine whether
     * {@link KoresIndyExperiment} to use the bootstrap to dynamically bind invocations.
     *
     * @param itf Interface to implement.
     * @param <T> Interface type.
     * @return Instance of the generated implementation.
     * @see KoresExperimentsIndyHelper For more information.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createFromInterface(Class<T> itf) {
        List<Class<?>> itfs = Collections.singletonList(itf);

        final Map<Method, KoresIndyExperiment> table = new HashMap<>();

        return create(itfs,
                InternalUtil.loopMethods(itfs, m -> table.put(m, Util.getExperiment(m))),
                table::get);
    }

    /**
     * Generates a class that implements abstract methods of {@code itf} with dynamic invocations of
     * methods of the receiver object with its arguments. This method generates dynamic invocation
     * to {@link KoresIndyExperiment#getBootstrapMethod() bootstrap} of {@code experiment} in all
     * methods.
     *
     * @param itf        Interface to implement.
     * @param experiment Experiment to use to generated dynamic invocations to be added to methods
     *                   to implement.
     * @param <T>        Type of interface.
     * @return Instance of the generated implementation.
     * @see KoresExperimentsIndyHelper For more information.
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> itf, KoresIndyExperiment experiment) {
        return create(Collections.singletonList(itf), experiment);
    }

    /**
     * Generates a class that implements abstract methods of all {@code itfs} with dynamic
     * invocations of methods of the receiver object with its arguments. This method generates
     * dynamic invocation to {@link KoresIndyExperiment#getBootstrapMethod() bootstrap} of {@code
     * experiment} in all methods.
     *
     * @param itfs       Interfaces to implement.
     * @param experiment Experiment to use to generated dynamic invocations to be added to methods
     *                   to implement.
     * @param <T>        Expected interface type, must be in {@code itfs} list, otherwise a class
     *                   cast exception will be thrown.
     * @return Instance of the generated implementation.
     * @see KoresExperimentsIndyHelper For more information.
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(List<Class<?>> itfs, KoresIndyExperiment experiment) {
        return create(itfs, ignored -> experiment);
    }

    /**
     * Generates a class that implements abstract methods of all {@code itfs} with dynamic
     * invocations of methods of the receiver object with its arguments. This method generates
     * dynamic invocation to {@link KoresIndyExperiment#getBootstrapMethod() bootstrap} of {@link
     * KoresIndyExperiment} resolved by {@code experimentResolver}.
     *
     * @param itfs               Interfaces to implement.
     * @param experimentResolver Resolver of experiment by method, the resolved experiment will be
     *                           used to get the bootstrap to use to generate dynamic invocation of
     *                           the method provided to function.
     * @param <T>                Expected interface type, must be in {@code itfs} list, otherwise a
     *                           class cast exception will be thrown.
     * @return Instance of the generated implementation.
     * @see KoresExperimentsIndyHelper For more information.
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(List<Class<?>> itfs,
                               Function<Method, KoresIndyExperiment> experimentResolver) {
        if (itfs.isEmpty())
            throw new IllegalArgumentException("No interface provided: " + itfs + "!");
        if (CollectionsKt.any(itfs, i -> !i.isInterface()))
            throw new IllegalArgumentException("All input classes for 'itfs' must be interface." +
                    " Inputs: " + itfs + ".");

        return create(itfs, InternalUtil.loopMethods(itfs, m -> {
        }), experimentResolver);
    }

    /**
     * Generates a class that implements abstract methods of all {@code itfs} with dynamic
     * invocations of methods of the receiver object with its arguments. This method generates
     * dynamic invocation to {@link KoresIndyExperiment#getBootstrapMethod() bootstrap} of {@link
     * KoresIndyExperiment} resolved by {@code experimentResolver}.
     *
     * @param itfs               Interfaces to implement.
     * @param methodsToImplement Methods to implement.
     * @param experimentResolver Resolver of experiment by method, the resolved experiment will be
     *                           used to get the bootstrap to use to generate dynamic invocation of
     *                           the method provided to function.
     * @param <T>                Expected interface type, must be in {@code itfs} list, otherwise a
     *                           class cast exception will be thrown.
     * @return Instance of the generated implementation.
     * @see KoresExperimentsIndyHelper For more information.
     */
    @SuppressWarnings("unchecked")
    private static <T> T create(List<Class<?>> itfs,
                                Collection<? extends Method> methodsToImplement,
                                Function<Method, KoresIndyExperiment> experimentResolver) {
        ClassDeclaration declaration = KoresExperimentsIndyHelper
                .createClass(itfs, methodsToImplement, experimentResolver);
        List<BytecodeClass> process = InternalUtil.getThreadBytecodeGenerator()
                .process(declaration);
        Debug.save(process, "indy_helper");
        CodeClassLoader loader = new CodeClassLoader(itfs.get(0).getClassLoader());
        try {
            return (T) loader.define(process).getConstructors()[0].newInstance();
        } catch (Exception e) {
            throw RethrowException.rethrow(e);
        }
    }

    /**
     * Creates the declaration and implementation of methods of {@code interfaces}.
     *
     * @param interfaces         Interfaces to implement.
     * @param methods            Methods to implement.
     * @param experimentResolver Resolver of experiments to get the bootstrap to use in dynamic
     *                           invocation.
     * @return Class declaration of the implementation.
     */
    private static ClassDeclaration createClass(List<Class<?>> interfaces,
                                                Collection<? extends Method> methods,
                                                Function<Method, KoresIndyExperiment> experimentResolver) {
        final String name = InternalUtil.createGenClassName("indy_helper", "Impl");
        return ClassDeclaration.Builder.builder()
                .modifiers(KoresModifier.PUBLIC)
                .specifiedName(name)
                .implementations(new ArrayList<>(interfaces))
                .methods(methods.stream().map(it -> impl(it, experimentResolver.apply(it)))
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Implements the {@code m} and generate the body with dynamic invocation to bootstrap of {@code
     * experiment}.
     *
     * @param m          Method to implement.
     * @param experiment Experiment to use to get the bootstrap to invoke.
     * @return Implementation of {@code m}.
     */
    private static MethodDeclaration impl(Method m, KoresIndyExperiment experiment) {
        List<Object> args = new ArrayList<>();
        experiment.handle(m, args);

        MethodDeclaration methodDeclaration = ConversionsKt.toMethodDeclaration(m);
        KoresParameter receiverParameter = methodDeclaration.getParameters().get(0);
        VariableAccess receiverAccess = Factories
                .accessVariable(receiverParameter.getType(), receiverParameter.getName());

        Type rType = methodDeclaration.getReturnType();
        List<Type> pTypes = methodDeclaration.getParameters().stream().map(KoresParameter::getType)
                .collect(Collectors.toList());

        return methodDeclaration
                .builder()
                .body(Instructions.fromPart(Factories.returnValue(rType,
                        DynamicInvocationFactory.invokeDynamic(
                                experiment.getBootstrapMethod(),
                                new DynamicMethodSpec(
                                        methodDeclaration.getName(),
                                        Factories.typeSpec(rType, pTypes),
                                        Collections3.append(receiverAccess, ConversionsKt
                                                .getAccess(methodDeclaration.getParameters()))
                                ),
                                args
                        )))
                )
                .build();
    }

}
