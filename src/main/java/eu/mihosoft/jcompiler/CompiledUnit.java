/*
 * Copyright 2020 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Code is based on abandoned project by Trung (https://github.com/trung/InMemoryJavaCompiler).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.mihosoft.jcompiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CompiledUnit {
    private final String name;
    private CompiledClass mainClass;
    private final String code;
    private final List<CompiledClass> classes;
    private final List<CompiledClass> classesUnmodifiable;
     
    /*pkg private*/ CompiledUnit(String name, String code, List<CompiledClass> classes) {
        this.name = name;
        this.classes = classes;
        this.classesUnmodifiable = Collections.unmodifiableList(classes);

        this.code = code;
    }

    /**
     * @return the compiled classes contained in this unit (order from source code is preserved)
     */
    public List<CompiledClass> getClasses() {
        return classesUnmodifiable;
    }
    
    /**
     * Sorts the classes according to their appearance in code.
     */
    /*pkg private*/ void initAndSortClassNames() {

        String mainClassName = LangUtils.getClassNameFromCode(code);

        // lookup main class by name
        this.mainClass = this.classes.stream().
            filter(cc->Objects.equals(mainClassName,cc.getClassName())).findAny().orElse(null);

        // at least one type has to be present in the file
        if(this.mainClass==null) {
            throw new CompilationException("No type declaration found in '" + getName() + "'");
        }

        // get the class names in order of appearance
        List<String> classNamesInOrder = LangUtils.getClassNamesFromCode(this.code);

        // build map
        Map<String, CompiledClass> classesByName = new HashMap<>();
        for(CompiledClass cc : this.classes) {
            classesByName.put(cc.getClassName(), cc);
        }

        // clear list
        this.classes.clear();

        // and rebuild it in correct order (appearance in file)
        for(String clsName : classNamesInOrder) {
            this.classes.add(classesByName.get(clsName));
        }
    }

    /**
     * Returns the main class (public class/interface or first class/interface if no public type declaration is present).
     * @return the main class of this compilation unit
     */
    public CompiledClass getMainClass() {
        return this.mainClass;
    }

    /**
     * @return the name of this compiled unit
     */
    public String getName() {
        return name;
    }

}