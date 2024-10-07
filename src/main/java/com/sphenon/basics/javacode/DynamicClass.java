package com.sphenon.basics.javacode;

/****************************************************************************
  Copyright 2001-2024 Sphenon GmbH

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
import com.sphenon.basics.customary.*;
import com.sphenon.basics.data.*;

import com.sphenon.basics.javacode.returncodes.*;

/**
   A DynamicClass is a class which is loaded and optionally reloaded at
   runtime, depending on whether it's source code is up to date. Managing of
   source code location, compilation and loading is handled by this wrapper.
 */
public interface DynamicClass <Interface> {

    /**
       Full Java class name of dynamic class
    */
    public String getFullClassName(CallContext context);

    /**
       If up to date, optionally compiles and loads the class code and creates
       an instance of it. If not up to date, the code must be updated before
       calling this method.

       @return an instance of the loaded class
    */
    public Interface createInstance (CallContext context);

    /**
       If up to date, optionally compiles and loads the class code. If not up
       to date, the code must be updated before calling this method.

       @return the loaded class
    */
    public Class<Interface> loadClass (CallContext context);

    /**
       If not up to date, this method returns a JavaCodeManager, which
       provides streams to write the updated code to.

       @return an instance of JavaCodeManager, prepared to use
     */
    public JavaCodeManager getJavaCodeManager (CallContext context);


    /**
       If this dynamic class is able to control the associated code generator,
       it is invoked and creates the code, but does not compile it. If the
       dynamic class has no control over the generator and thus cannot invoke
       it, an exception is thrown.
    */
    public void createCode (CallContext context);
}
