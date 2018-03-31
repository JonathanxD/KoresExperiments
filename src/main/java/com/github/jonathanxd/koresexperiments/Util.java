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

import com.github.jonathanxd.koresexperiments.annotation.Experiment;
import com.github.jonathanxd.koresexperiments.experiment.KoresIndyExperiment;
import com.github.jonathanxd.iutils.reflection.Reflection;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

public class Util {
    private static boolean isEqual(Method o1, Method o2) {
        return o1.getName().equals(o2.getName())
                && o1.getReturnType().equals(o2.getReturnType())
                && Arrays.equals(o1.getParameterTypes(), o2.getParameterTypes());
    }

    static boolean contains(Collection<? extends Method> methods, Method o1) {
        for (Method method : methods) {
            if (method != o1 && Util.isEqual(method, o1))
                return true;
        }

        return false;
    }

    static KoresIndyExperiment getExperiment(Method m) {
        Experiment experimentAnnotation = m.getDeclaredAnnotation(Experiment.class);
        KoresIndyExperiment experiment = experimentAnnotation == null ? null : Reflection.getInstance(experimentAnnotation.value());

        if (experiment == null) {
            Class<?> r = m.getDeclaringClass();

            do {
                experimentAnnotation = r.getDeclaredAnnotation(Experiment.class);
                experiment = experimentAnnotation == null ? null : Reflection.getInstance(experimentAnnotation.value());
            } while ((r = r.getEnclosingClass()) != null && experiment == null);

            if (experiment == null)
                throw new IllegalArgumentException("Missing experiment for an abstract method" +
                        ", read @Experiment documentation. Method: " + m + ".");
        }

        return experiment;
    }
}
