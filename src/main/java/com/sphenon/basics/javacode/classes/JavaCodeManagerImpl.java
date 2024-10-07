package com.sphenon.basics.javacode.classes;

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
import com.sphenon.basics.system.*;

import com.sphenon.basics.configuration.annotations.*;

import com.sphenon.basics.javacode.*;
import com.sphenon.basics.javacode.returncodes.*;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.PipedInputStream;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Hashtable;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

// Java tools API
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler;
// requires java 1.8:
// import javax.tools.DocumentationTool;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

public class JavaCodeManagerImpl implements JavaCodeManager {

    static final public Class _class = JavaCodeManagerImpl.class;

    static protected long notification_level;
    static public    long adjustNotificationLevel(long new_level) { long old_level = notification_level; notification_level = new_level; return old_level; }
    static public    long getNotificationLevel() { return notification_level; }
    static {
        notification_level = NotificationLocationContext.getLevel(_class);
    };

    // Configuration ------------------------------------------------------------------

    @Configuration public interface Config {
        @Required
        String getSourcePath(CallContext context);
        @Required
        String getClassPath(CallContext context);
        @Required
        String getDestinationPath(CallContext context);
        String getExternalCompiler(CallContext context);
        String getOptions(CallContext context);
        @DefaultValue("true")
        boolean getJavaDocEnabled(CallContext context);
        @DefaultValue("false")
        boolean getForceGeneration(CallContext context);
    }

    static public Config config = Configuration_JavaCodeManagerImpl.get(RootContext.getInitialisationContext());

    // --------------------------------------------------------------------------------

    protected JavaCodeResource                   default_resource;
    protected Hashtable<String,JavaCodeResource> absolute_resources;
    protected Hashtable<String,JavaCodeResource> relative_resources;
    protected Hashtable<String,DataResource>     absolute_data_resources;
    protected Hashtable<String,DataResource>     relative_data_resources;
    protected Vector<JavaCodeResource>           java_code_resources;
    protected Vector<DataResource>               data_resources;
    protected Vector<String>                     imports;

    protected String                             full_class_name;

    static volatile protected boolean initialised = false;

    static public void initialise (CallContext context) {
        if (initialised == false) {
            synchronized(JavaCodeManagerImpl.class) {
                if (initialised == false) {
                    initialised = true;

                    SystemCommandUtilities.ensureFolderExists(context, config.getSourcePath(context));
                    SystemCommandUtilities.ensureFolderExists(context, config.getDestinationPath(context));
                }
            }
        }
    }

    public JavaCodeManagerImpl (CallContext context, String full_class_name) {
        initialise(context);
        this.java_code_resources = new Vector<JavaCodeResource>();
        this.data_resources = new Vector<DataResource>();
        this.imports = new Vector<String>();
        this.full_class_name = full_class_name;
    }

    public void reset(CallContext context) {
        this.default_resource = null;
        this.absolute_resources = null;
        this.relative_resources = null;
        this.absolute_data_resources = null;
        this.relative_data_resources = null;
        this.java_code_resources = new Vector<JavaCodeResource>();
        this.data_resources = new Vector<DataResource>();
    }

    public Vector<JavaCodeResource> getJavaCodeResources(CallContext context) {
        return java_code_resources;
    }

    public Vector<DataResource> getDataResources(CallContext context) {
        return data_resources;
    }

    public Vector<String> getImports(CallContext context) {
        return imports;
    }

    public void addImport(CallContext context, String an_import) {
        this.imports.add(an_import);
    }

    public JavaCodeResource getDefaultResource (CallContext context) {
        if (default_resource == null) {
            default_resource =
                new Class_JavaCodeResource
                (context,
                 config.getSourcePath(context) + "/" + full_class_name.replace(".","/") + ".java",
                 config.getDestinationPath(context) + "/" + full_class_name.replace(".","/") + ".class",
                 full_class_name
                );
            java_code_resources.add(default_resource);
        }
        return default_resource;
    }

    public JavaCodeResource getAbsoluteResource (CallContext context, String absolute_class_name) {
        if (absolute_resources == null) {
            absolute_resources = new Hashtable<String,JavaCodeResource>();
        }
        JavaCodeResource jcr = absolute_resources.get(absolute_class_name);
        if (jcr == null) {
            jcr =
                new Class_JavaCodeResource
                (context,
                 config.getSourcePath(context) + "/" + absolute_class_name.replace(".","/") + ".java",
                 config.getDestinationPath(context) + "/" + absolute_class_name.replace(".","/") + ".class",
                 absolute_class_name
                );
            absolute_resources.put(absolute_class_name, jcr);
            java_code_resources.add(jcr);
        }
        return jcr;
    }

    public JavaCodeResource tryGetAbsoluteResource (CallContext context, String absolute_class_name) {
        if (absolute_resources == null) {
            return null;
        }
        return absolute_resources.get(absolute_class_name);
    }

    public JavaCodeResource getRelativeResource (CallContext context, String relative_class_name) {
        if (relative_resources == null) {
            relative_resources = new Hashtable<String,JavaCodeResource>();
        }
        JavaCodeResource jcr = relative_resources.get(relative_class_name);
        if (jcr == null) {
            int pos = full_class_name.lastIndexOf('.');
            String absolute_class_name = (pos == -1 ? "" : full_class_name.substring(0, pos+1)) + relative_class_name;
            jcr =
                new Class_JavaCodeResource
                (context,
                 config.getSourcePath(context) + "/" + absolute_class_name.replace(".","/") + ".java",
                 config.getDestinationPath(context) + "/" + absolute_class_name.replace(".","/") + ".class",
                 absolute_class_name
                );
            relative_resources.put(relative_class_name, jcr);
            java_code_resources.add(jcr);
        }
        return jcr;
    }

    public JavaCodeResource tryGetRelativeResource (CallContext context, String relative_class_name) {
        if (relative_resources == null) {
            return null;
        }
        return relative_resources.get(relative_class_name);
    }

    /**
       @param absolute_resource_name in contrast to JavaCode resources, the
              path must be correctly separated with slashes since the name
              part might contain dots
     **/
    public DataResource getAbsoluteDataResource (CallContext context, String absolute_resource_name) {
        if (absolute_data_resources == null) {
            absolute_data_resources = new Hashtable<String,DataResource>();
        }
        DataResource dr = absolute_data_resources.get(absolute_resource_name);
        if (dr == null) {
            dr =
                new Class_DataResource
                (context,
                 config.getSourcePath(context) + "/" + absolute_resource_name,
                 config.getDestinationPath(context) + "/" + absolute_resource_name,
                 absolute_resource_name
                );
            absolute_data_resources.put(absolute_resource_name, dr);
            data_resources.add(dr);
        }
        return dr;
    }

    public DataResource tryGetAbsoluteDataResource (CallContext context, String absolute_resource_name) {
        if (absolute_data_resources == null) {
            return null;
        }
        return absolute_data_resources.get(absolute_resource_name);
    }

    public DataResource getRelativeDataResource (CallContext context, String relative_resource_name) {
        if (relative_data_resources == null) {
            relative_data_resources = new Hashtable<String,DataResource>();
        }
        DataResource dr = relative_data_resources.get(relative_resource_name);
        if (dr == null) {
            int pos = full_class_name.lastIndexOf('.');
            String absolute_resource_name = (pos == -1 ? "" : full_class_name.substring(0, pos+1).replace(".","/")) + relative_resource_name;
            dr =
                new Class_DataResource
                (context,
                 config.getSourcePath(context) + "/" + absolute_resource_name,
                 config.getDestinationPath(context) + "/" + absolute_resource_name,
                 absolute_resource_name
                );
            relative_data_resources.put(relative_resource_name, dr);
            data_resources.add(dr);
        }
        return dr;
    }

    public DataResource tryGetRelativeDataResource (CallContext context, String relative_resource_name) {
        if (relative_data_resources == null) {
            return null;
        }
        return relative_data_resources.get(relative_resource_name);
    }

    public void closeResources(CallContext context) {
        if (this.java_code_resources != null) {
            for (JavaCodeResource jcr : this.java_code_resources) {
                jcr.close(context);
            }
        }
        if (this.data_resources != null) {
            for (DataResource dr : this.data_resources) {
                dr.close(context);
            }
        }
    }

    public void compile (CallContext context) throws InvalidJavaSource {

        String option_string = config.getOptions(context);
        if (option_string == null) {
            option_string = "-g -encoding utf-8";
        }

        String[] options = option_string.split(" ");
        String[] javacargs = new String[options.length+6 /* OLD internal compiler: +java_code_resources.size() */];
                             
        int index = 0;

        for (String option : options) {
            javacargs[index++] = option;
        }

        javacargs[index++] = "-d";
        javacargs[index++] = config.getDestinationPath(context);
        javacargs[index++] = "-classpath";
        javacargs[index++] = config.getClassPath(context);
        javacargs[index++] = "-sourcepath";
        javacargs[index++] = config.getSourcePath(context);

        this.closeResources(context);

        List<String> files = new ArrayList<String>();

        for (JavaCodeResource jcr : java_code_resources) {
            files.add(jcr.getJavaFilePath(context));
        }

        invokeCompilerNEW(context, javacargs, files);

        for (DataResource dr : data_resources) {
            File source = dr.getJavaFile(context);
            File target = dr.getClassFile(context);
            FileUtilities.copyFile(context, source, target);
        }
    }

    static public void invokeCompilerNEW(CallContext context, String[] arguments, List<String> files) throws InvalidJavaSource {

        long started = System.currentTimeMillis();

        if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "Starting compilation (javax.tools compiler)..."); }

        StringWriter output = new StringWriter();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // requires java 1.8:
        // DocumentationTool compiler = ToolProvider.getSystemDocumentationTool();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(files); // .getJavaFileObjects("TestClass.java");
        DiagnosticListener<? super JavaFileObject> diagnosticListener = null;
        Iterable<String> options = Arrays.asList(arguments);
        Iterable<String> classes = null; // to be processed by annotation processing
        CompilationTask task = compiler.getTask(output,
                                                fileManager, 
                                                diagnosticListener,
                                                options,
                                                classes,
                                                compilationUnits);
        boolean compilation_ok = task.call();

        long duration = System.currentTimeMillis() - started;

        if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "Compilation done (%(duration) ms)", "duration", duration); }

        try {
            if (output != null) { output.close(); }
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentFailure(context, ioe, "Could not close output writer");
            throw (ExceptionEnvironmentFailure) null; // compiler insists
        }

        if (true || compilation_ok == false) {
            String cmd = "javac";
            for (String arg: arguments) { cmd += " " + arg; }

            String o = null;

            if (output != null) {
                o = output.toString();
            }

            if (compilation_ok == false) {
                InvalidJavaSource.createAndThrow(context, "Compilation failed: %(javaccmd) -- %(javacoutput)", "javaccmd", cmd, "javacoutput", o);
                throw (InvalidJavaSource) null; // compiler insists
            } else {
                if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "Result: ok -- %(javaccmd) -- %(javacoutput)", "javaccmd", cmd, "javacoutput", o); }
            }
        }
    }

    static public void invokeCompiler(CallContext context, String[] arguments) throws InvalidJavaSource {
        // no official doc's :(
        // but see http://groups.google.de/group/comp.lang.java.programmer/browse_thread/thread/6eb377ba17c4da77/a0eb3808fce16a93?lnk=st&q=&rnum=3&hl=de#a0eb3808fce16a93
        // -> "I maintain javac."

        StringWriter output = null;
        PrintWriter out = null;
        SystemProcess sp = null;
        int status;

        long started = System.currentTimeMillis();

        String extcomp = config.getExternalCompiler(context);

        if (extcomp == null || extcomp.isEmpty()) {

            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "Starting compilation (internal compiler): %(arguments)", "arguments", arguments); }

            output = new StringWriter();
            out = new PrintWriter(output);

            // javac does not cleanup correctly
            ClassLoader current_class_loader = Thread.currentThread().getContextClassLoader();

            com.sun.tools.javac.Main javac = new com.sun.tools.javac.Main();
            status = javac.compile(arguments, out);

            Thread.currentThread().setContextClassLoader(current_class_loader);

        } else {

            String[] command = new String[arguments == null ? 1 : (arguments.length + 1)];
            int i=0;
            command[i++] = extcomp;
            if (arguments != null) {
                for (String argument : arguments) {
                    command[i++] = argument;
                }
            }

            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "Starting compilation (external compiler): %(command)", "command", command); }

            sp = new SystemProcess(context, command, null);
            sp.start(context,
                     null, // process_input_arg
                     false, // write_stdout_to_stdout
                     false, // write_stdout_to_piped_stream,
                     true, // write_stdout_to_string_builder,
                     false, // write_stderr_to_stderr
                     false, // write_stderr_to_piped_stream,
                     true, // write_stderr_to_string_builder,
                     true // wait
                    );

            status = sp.getExitValue(context);
        }

        long duration = System.currentTimeMillis() - started;

        if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "Compilation done (%(duration) ms)", "duration", duration); }

        try {
            if (out != null) { out.close(); }
            if (output != null) { output.close(); }
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentFailure(context, ioe, "Could not close output writer");
            throw (ExceptionEnvironmentFailure) null; // compiler insists
        }

        if (true || status != 0) {
            String cmd = "javac";
            for (String arg: arguments) { cmd += " " + arg; }

            String o = null;
            String e = null;

            if (output != null) {
                o = output.toString();
            }

            if (sp != null) {
                String po = sp.getProcessOutputAsString(context, true);
                String pe = sp.getProcessErrorAsString(context, true);
                o = (po != null && po.isEmpty() == false ? ("[output]\n" + po + "\n") : "")
                  + (pe != null && pe.isEmpty() == false ? ("[error]\n" + po) : "");
            }

            if (status != 0) {
                InvalidJavaSource.createAndThrow(context, "Compilation failed: %(javaccmd) -- %(javacoutput)", "javaccmd", cmd, "javacoutput", o);
                throw (InvalidJavaSource) null; // compiler insists
            } else {
                if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "Result: ok -- %(javaccmd) -- %(javacoutput)", "javaccmd", cmd, "javacoutput", o); }
            }
        }
    }

    static public void invokeJavadoc(CallContext context, String[] arguments) throws InvalidJavaSource {

        // http://java.sun.com/j2se/1.5.0/docs/guide/javadoc/standard-doclet.html#runningprogrammatically

        if (config.getJavaDocEnabled(context) == false) {
            System.err.println("!!! JAVADOC: OMITTED !!!");
            return;
        }

        StringWriter erroutput = new StringWriter();
        PrintWriter errout = new PrintWriter(erroutput);
        StringWriter warnoutput = new StringWriter();
        PrintWriter warnout = new PrintWriter(warnoutput);
        StringWriter infooutput = new StringWriter();
        PrintWriter infoout = new PrintWriter(infooutput);

        if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "Starting javadoc..."); }
        long started = System.currentTimeMillis();

            // javac does not cleanup correctly
        ClassLoader current_class_loader = Thread.currentThread().getContextClassLoader();

        com.sphenon.basics.cache.ClassCache.cache_disabled = true;
        int status = com.sun.tools.javadoc.Main.execute("javadoc", errout, warnout, infoout, "com.sun.tools.doclets.standard.Standard", arguments);
        com.sphenon.basics.cache.ClassCache.cache_disabled = false;

        Thread.currentThread().setContextClassLoader(current_class_loader);

        long duration = System.currentTimeMillis() - started;
        if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "Javadoc done (%(duration) ms)", "duration", duration); }

        try {
            errout.close();
            erroutput.close();
            warnout.close();
            warnoutput.close();
            infoout.close();
            infooutput.close();
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentFailure(context, ioe, "Could not close output writer");
            throw (ExceptionEnvironmentFailure) null; // compiler insists
        }

        if (status != 0) {
            String cmd = "javadoc";
            for (String arg: arguments) { cmd += " " + arg; }
            InvalidJavaSource.createAndThrow(context, "Javadoc failed (exit code %(code)): %(javadoccmd) -- ERROR: %(javadocerroutput) -- WARN: %(javadocwarnoutput) -- INFO: %(javadocinfooutput)", "javadoccmd", cmd, "javadocerroutput", erroutput.toString(), "javadocwarnoutput", warnoutput.toString(), "javadocinfooutput", infooutput.toString(), "code", status);
            throw (InvalidJavaSource) null; // compiler insists
        }
    }
}
