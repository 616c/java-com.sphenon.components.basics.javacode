package com.sphenon.basics.javacode.test;

/****************************************************************************
  Copyright 2001-2018 Sphenon GmbH

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations
  under the License.
*****************************************************************************/

import com.sphenon.basics.context.*;
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.exception.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.configuration.*;
import com.sphenon.basics.customary.*;
import com.sphenon.basics.data.*;

import com.sphenon.basics.javacode.*;
import com.sphenon.basics.javacode.classes.*;

import java.io.*;

public class Test {

    static public void main(String[] args) {

        final long now = (new java.util.Date()).getTime();

        try {
            Configuration.checkCommandLineArgs(args);
            Context context = com.sphenon.basics.context.classes.RootContext.getRootContext ();
            Configuration.initialise(context);
            
            Class_DynamicClass<DynamicTestInterface> dc = new Class_DynamicClass(context, "com.sphenon.basics.javacode.test.DynamicTestClass", DynamicTestInterface.class) {
                    protected boolean doCompilation(CallContext context) {
                        return false;
                    }
                    protected boolean doGeneration(CallContext context) {
                        return false;
                    }
                    protected boolean tryToLoadAsResource(CallContext context) {
                        return false;
                    }
                    protected boolean useExistingResourceUnconditionally(CallContext context) {
                        return false;
                    }
                    protected boolean doAutoGeneration(CallContext context) {
                        return false;
                    }
                    protected String getAdditionalMetaData(CallContext context) {
                        return "TestEntry: hello world!";
                    }
                    protected String getCodeGeneratorPackage(CallContext context) {
                        return "com.sphenon.basics.javacode";
                    }
                    protected long getLastModificationOfCodeGeneratorSource(CallContext context) {
                        return -1;
                    }
                    protected long getLastModificationOfCodeGeneratorConfiguration(CallContext context) {
                        return -1;
                    }
                    protected void generateCode(CallContext context) {
                    }
                };

            System.err.println("Needs generation: " + dc.needsGeneration(context));

            DynamicTestInterface dti;
            BufferedWriter w;
            
            System.err.println("Creating code...");

            w = dc.getJavaCodeManager(context).getDefaultResource(context).getWriter(context);
            w.append("package com.sphenon.basics.javacode.test;\n");
            w.append("public class DynamicTestClass implements DynamicTestInterface {\n");
            w.append("    public String test () { return \"4711\"; }\n");
            w.append("}\n");
            dc.notifyCodeGenerationCompleted(context);
            
            System.err.println("Needs generation: " + dc.needsGeneration(context));

            dti = dc.createInstance(context);
            
            System.err.println("Test 1: " + dti.test());
            
            System.err.println("Needs generation: " + dc.needsGeneration(context));

            dti = dc.createInstance(context);
            
            System.err.println("Test 2: " + dti.test());
            
            dc.getJavaCodeManager(context).reset(context);

            System.err.println("Creating code...");

            w = dc.getJavaCodeManager(context).getDefaultResource(context).getWriter(context);
            w.append("package com.sphenon.basics.javacode.test;\n");
            w.append("public class DynamicTestClass implements DynamicTestInterface {\n");
            w.append("    public String test () { return \"0815\"; }\n");
            w.append("}\n");
            dc.notifyCodeGenerationCompleted(context);

            System.err.println("Needs generation: " + dc.needsGeneration(context));

            dti = dc.createInstance(context);
            
            System.err.println("Test 3: " + dti.test());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
