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

import com.github.jonathanxd.kores.bytecode.BytecodeClass;
import com.github.jonathanxd.kores.bytecode.util.ClassSaveUtilKt;

import java.nio.file.Paths;
import java.util.List;

public class Debug {
    /**
     * Name of the debug property used to determine whether to save or not generated classes.
     */
    public static final String SAVE_CLASSES_PROPERTY_NAME = "kores_experiments.save_classes";

    /**
     * Whether to save generated classes or not.
     */
    public static final boolean SAVE_CLASSES = Boolean.valueOf(System.getProperty(SAVE_CLASSES_PROPERTY_NAME, "false"));

    /**
     * Save generated {@code bytecodeClasses} of the module in {@code experiments_gen/module} path.
     *
     * @param bytecodeClasses Classes to save.
     * @param module          Module name.
     */
    static void save(List<BytecodeClass> bytecodeClasses, String module) {
        if (!SAVE_CLASSES)
            return;

        for (BytecodeClass bytecodeClass : bytecodeClasses) {
            ClassSaveUtilKt.save(bytecodeClass, Paths.get("experiments_gen", module), true, true);
        }

    }
}
