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
package com.github.jonathanxd.koresexperiments.annotation;

import com.github.jonathanxd.koresexperiments.KoresExperimentsIndyHelper;
import com.github.jonathanxd.koresexperiments.experiment.KoresIndyExperiment;
import com.github.jonathanxd.iutils.annotation.Singleton;
import com.github.jonathanxd.iutils.reflection.Reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a {@link KoresIndyExperiment} to use to implement methods of this interface, the {@link
 * #value() experiment class} must have {@link Singleton} annotation or follow rules of {@link
 * Reflection#getInstance(Class)}. This also can be applied to methods, changing whether experiment
 * to use to implement them.
 *
 * To create instance use {@link KoresExperimentsIndyHelper#createFromInterface(Class)}, make sure
 * that all method have {@link Experiment} or at least any declaring type of them is annotated with
 * {@link Experiment}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Experiment {
    Class<? extends KoresIndyExperiment> value();
}
