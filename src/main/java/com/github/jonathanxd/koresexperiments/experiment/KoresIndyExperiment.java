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
package com.github.jonathanxd.koresexperiments.experiment;

import com.github.jonathanxd.kores.common.MethodInvokeSpec;
import com.github.jonathanxd.koresexperiments.KoresExperimentsIndyHelper;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Base class of all {@code invokedynamic} experiments. The {@link KoresExperimentsIndyHelper} can
 * generate classes that use the {@link #getBootstrapMethod() bootstrap} provided by this experiment
 * to invoke methods of a receiver object (that is the first argument of the method to implement)
 * with its arguments (that is the rest of arguments).
 */
public interface KoresIndyExperiment extends KoresExperiment {

    /**
     * Gets the bootstrap method of this indy experiment.
     *
     * @return Bootstrap method of this indy experiment.
     */
    MethodInvokeSpec getBootstrapMethod();

    /**
     * Handle additional bootstrap args for {@code m} implementation generation.
     *
     * By default, {@link KoresExperimentsIndyHelper} only generates {@code invokedynamic} for
     * bootstrap without additional arguments, if the {@link #getBootstrapMethod()} of this
     * experiment needs additional arguments, implement this method and add them to {@code args}.
     *
     * @param m    Method to implement.
     * @param args Bootstrap args.
     */
    default void handle(Method m, List<Object> args) {
    }
}
