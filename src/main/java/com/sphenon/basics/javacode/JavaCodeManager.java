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

import java.io.Writer;
import java.util.Vector;

public interface JavaCodeManager {
    /**
       gets the default resource, i.e. the resource for the
       core generator class to be constructed
     **/
    public JavaCodeResource getDefaultResource (CallContext context);

    /**
       gets an absolute resource, i.e. a resource for an
       arbitraty class to be constructed
       @param absolute_class_name : fully qualified class name
     **/
    public JavaCodeResource getAbsoluteResource (CallContext context, String absolute_class_name);

    /**
       gets an absolute resource which must already exist, like
       {@link JavaCodeManager#getAbsoluteResource}
       @param absolute_class_name : fully qualified class name
       @return resource, or null if no such resource was requested before
     **/
    public JavaCodeResource tryGetAbsoluteResource (CallContext context, String absolute_class_name);

    /**
       gets a relative resource, i.e. a resource for a
       class to be constructed whose name is given relative
       to the package where the core generator class is located
       @param relative_class_name : part of a fully qualified class name, will be prefixed by package of core generator class
     **/
    public JavaCodeResource getRelativeResource (CallContext context, String relative_class_name);

    /**
       gets a relative resource which must already exist, like
       {@link JavaCodeManager#getRelativeResource}
       @param relative_class_name : part of a fully qualified class name, will be prefixed by package of core generator class
       @return resource, or null if no such resource was requested before
     **/
    public JavaCodeResource tryGetRelativeResource (CallContext context, String relative_class_name);

    /**
       gets an absolute data resource, i.e. a data resource for an
       arbitraty resource name to be constructed
       @param absolute_resource_name : fully qualified resource name
     **/
    public DataResource getAbsoluteDataResource (CallContext context, String absolute_resource_name);

    /**
       gets an absolute data resource which must already exist, like
       {@link JavaCodeManager#getAbsoluteDataResource}
       @param absolute_resource_name : fully qualified resource name
       @return resource, or null if no such resource was requested before
     **/
    public DataResource tryGetAbsoluteDataResource (CallContext context, String absolute_resource_name);

    /**
       gets a relative data resource, i.e. a data resource
       to be constructed whose name is given relative
       to the package where the core generator class is located
       @param relative_resource_name : part of a fully qualified resource name, will be prefixed by package of core generator class
     **/
    public DataResource getRelativeDataResource (CallContext context, String relative_resource_name);

    /**
       gets a relative data resource which must already exist, like
       {@link JavaCodeManager#getRelativeDataResource}
       @param relative_resource_name : part of a fully qualified resource name, will be prefixed by package of core generator class
       @return resource, or null if no such resource was requested before
     **/
    public DataResource tryGetRelativeDataResource (CallContext context, String relative_resource_name);

    /**
       get all up to this point created resources
     **/
    public Vector<JavaCodeResource> getJavaCodeResources(CallContext context);

    /**
       get all up to this point created data resources
     **/
    public Vector<DataResource> getDataResources(CallContext context);

    /**
     * get all imported classes the resources depend on
     * and that are not created by this JavaCodeManager,
     * but are to be created, too
     *
     * @return a list of fully qualified class names
     **/
    public Vector<String> getImports(CallContext context);

    /**
     * adds a class to the list of imports whose creation
     * has to be taken care of externally
     *
     * @param an_import a fully qualified class name
     **/
    public void addImport(CallContext context, String an_import);

    /**
       compiles all resources maintained by this JavaCodeManager instance
     **/
    public void compile (CallContext context) throws InvalidJavaSource;

    /**
       closes all resources maintained by this JavaCodeManager instance,
       afterwards no writing is permitted anymore
     **/
    public void closeResources(CallContext context);

    /**
       clears all internal resources and makes this instance available for a
       fresh resource creation cycle
     **/
    public void reset(CallContext context);
}
