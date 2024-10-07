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
import com.sphenon.basics.cache.*;
import com.sphenon.basics.system.*;
import com.sphenon.basics.expression.*;
import com.sphenon.basics.data.*;

import com.sphenon.basics.javacode.*;
import com.sphenon.basics.javacode.returncodes.*;

import com.sphenon.basics.tracking.annotations.*;

import java.util.Map;
import java.util.HashMap;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.lang.annotation.*;

/* [Issue:GeneratorChecks - Class_DynamicClass.java,.configuration-cache this is not safe and convenient enough] */

abstract public class Class_DynamicClass<Interface> implements DynamicClass<Interface> {
    static final public Class _class = Class_DynamicClass.class;

    static protected long notification_level;
    static public    long adjustNotificationLevel(long new_level) { long old_level = notification_level; notification_level = new_level; return old_level; }
    static public    long getNotificationLevel() { return notification_level; }
    static { notification_level = NotificationLocationContext.getLevel(_class); };

    protected Class<Interface> base;

    public Class_DynamicClass(CallContext context, String full_class_name, Class<Interface> base) {
        this.full_class_name = full_class_name;
        this.base            = base;
    }

    protected String full_class_name;

    public String getFullClassName (CallContext context) {
        return this.full_class_name;
    }

    public Interface createInstance (CallContext context) {
        return createInstance (context, true, true, true);
    }

    public Interface createInstance (CallContext context, boolean generation, boolean compilation, boolean loading) {
        try {
            this.prepare(context, generation, compilation, loading);
            Interface i = (Interface) (this.use_context_argument ? compiled_java_class_constructor.newInstance(context) : compiled_java_class_constructor.newInstance());
            if (base.isAssignableFrom(i.getClass()) == false) {
                CustomaryContext.create((Context)context).throwConfigurationError(context, "Compiled class '%(classname)' exists, but is not derived from '%(base)'", "classname", this.getFullClassName(context), "base", this.base.getName());
                throw (ExceptionConfigurationError) null; // compiler insists
            }
            return i;
        } catch (InvalidJavaSource ijs) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, ijs, "Underlying template produces invalid java sourcecode");
            throw (ExceptionConfigurationError) null; // compiler insists
        } catch (InstantiationException ie) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, ie, "Compiled class '%(classname)' exists, but could not be instantiated", "classname", this.getFullClassName(context));
            throw (ExceptionConfigurationError) null; // compiler insists
        } catch (IllegalAccessException iae) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, iae, "Compiled class '%(classname)' exists, but could not be accessed", "classname", this.getFullClassName(context));
            throw (ExceptionConfigurationError) null; // compiler insists
        } catch (InvocationTargetException ite) {
            if (ite.getTargetException() instanceof java.lang.RuntimeException) {
                throw (java.lang.RuntimeException) ite.getTargetException();
            }
            if (ite.getTargetException() instanceof java.lang.Error) {
                throw (java.lang.Error) ite.getTargetException();
            }
            CustomaryContext.create((Context)context).throwConfigurationError(context, ite.getTargetException(), "Constructor of compiled class '%(classname)' threw an exception", "classname", this.getFullClassName(context));
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public Class<Interface> loadClass (CallContext context) {
        return loadClass (context, true, true, true);
    }

    public Class<Interface> loadClass (CallContext context, boolean generation, boolean compilation, boolean loading) {
        try {
            this.prepare(context, generation, compilation, loading);
            if (base.isAssignableFrom(compiled_java_class) == false) {
                CustomaryContext.create((Context)context).throwConfigurationError(context, "Compiled class '%(classname)' exists, but is not derived from '%(base)'", "classname", this.getFullClassName(context), "base", this.base.getName());
                throw (ExceptionConfigurationError) null; // compiler insists
            }
            return (Class<Interface>) compiled_java_class;
        } catch (InvalidJavaSource ijs) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, ijs, "Underlying template produces invalid java sourcecode");
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public void createCode (CallContext context) {
        if (this.needsGeneration(context) && this.doAutoGeneration(context) == false) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, "Dynamic class '%(class)' can't create code on explicit request", "class", this.getClass().getName());
            throw (ExceptionConfigurationError) null; // compiler insists
        }
        try {
            this.prepare(context, true, false, false);
        } catch (InvalidJavaSource ijs) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, ijs, "Underlying template produces invalid java sourcecode");
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    protected JavaCodeManagerImpl java_code_manager;

    public JavaCodeManager getJavaCodeManager(CallContext context) {
        if (this.java_code_manager == null) {
            this.java_code_manager = new JavaCodeManagerImpl(context, this.getFullClassName(context));

            String[] ardrs = this.getAdditionalRelativeDataResources(context);
            if (ardrs != null) {
                for (String ardr : ardrs) {
                    this.java_code_manager.getRelativeDataResource(context, ardr);
                }
            }
        }
        return this.java_code_manager;
    }

    // ----------------------------------------------------------------------------

    static protected long forced_generation_base_line;
    static protected boolean forced_generation_base_line_checked;

    static protected long getForcedGenerationBaseLine(CallContext context) {
        if (forced_generation_base_line_checked == false) {
            if (JavaCodeManagerImpl.config.getForceGeneration(context)) {
                forced_generation_base_line = (new Date()).getTime();
            }
            forced_generation_base_line_checked = true;
        }
        return forced_generation_base_line;
    }

    protected boolean loaded             = false;
    protected boolean reloading_required = false;

    public boolean needsGeneration(CallContext context) {
        String generation_reason = null;
        boolean needs_generation = false;

        long lmtc = 0;
        long lmtg = 0;
        long lmts = 0;
        long fgbl = 0;

        if (this.tryToLoadAsResource(context) && this.useExistingResourceUnconditionally(context) && this.tryToLoadJavaClassAsResource(context)) {
            // nothing to do
        } else if (this.getJavaCodeManager(context).getDefaultResource(context).getJavaFile(context).exists() == false) {
            generation_reason = "source code does not exist yet";
            needs_generation = true;
        } else if (this.testImplementationVersionMatch(context) == false) {
            generation_reason = "source exists, but code generator software is newer";
            needs_generation = true;
        } else if (    (lmtg = getLastModificationOfCodeGeneratorSource(context)) > 0
                    && ((lmts > 0 ? lmts : (lmts = getJavaCodeManager(context).getDefaultResource(context).getJavaFile(context).lastModified())) < lmtg)
                  ) {
            generation_reason = "source exists, but code generator source is newer (source: " + lmts + ", generator source: " + lmtg + ")";
            needs_generation = true;
        } else if (    (fgbl = getForcedGenerationBaseLine(context)) > 0
                    && ((lmts > 0 ? lmts : (lmts = getJavaCodeManager(context).getDefaultResource(context).getJavaFile(context).lastModified())) < fgbl)
                  ) {
            generation_reason = "source exists, but forced generation is enabled and source is not newer than generation baseline of this execution (source: " + lmts + ", forced generation baseline: " + fgbl + ")";
            needs_generation = true;
        } else if (    (lmtc = getLastModificationOfCodeGeneratorConfiguration(context)) > 0
                    && ((lmts > 0 ? lmts : (lmts = getJavaCodeManager(context).getDefaultResource(context).getJavaFile(context).lastModified())) < lmtc)
                  ) {
            generation_reason = "source exists, but code generator configuration is newer (source: " + lmts + ", generator configuration: " + lmtc + ")";
            needs_generation = true;
        }
        
        if (needs_generation) {
            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendDiagnostics(context, "Dynamic class '%(class)' needs code generation (%(reason))", "class", this.getFullClassName(context), "reason", generation_reason); }
            return true;
        }

        return false;
    }

    protected boolean needsCompilation(CallContext context) {
        JavaCodeResource jcrdef = this.getJavaCodeManager(context).getDefaultResource(context);

        String compilation_reason = null;
        boolean needs_compilation = false;

        if (this.tryToLoadAsResource(context) && this.useExistingResourceUnconditionally(context) && this.tryToLoadJavaClassAsResource(context)) {
            // nothing to do
        } else if (jcrdef.getJavaFile(context).exists() == false) {
            needs_compilation = true;
            compilation_reason = "java source code does not exist";
        } else if (jcrdef.getClassFile(context).exists() == false) {
            needs_compilation = true;
            compilation_reason = "java class file does not exist";
        } else if (   jcrdef.getClassFile(context).lastModified()
                    < jcrdef.getJavaFile(context).lastModified()) {
            needs_compilation = true;
            compilation_reason = "java source code is newer than java class file";
        }

        if (needs_compilation) {
            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendDiagnostics(context, "Dynamic class '%(class)' needs compilation (%(reason))", "class", this.getFullClassName(context), "reason", compilation_reason); }
        }

        return needs_compilation;
    }

    protected boolean needsLoading(CallContext context) {
        return loaded ? false : true;
    }

    static protected Map<ClassLoader,URLClassLoaderWithId> compiled_java_class_loaders;
    static protected long                                  cjcl_count = 0;
    protected URLClassLoaderWithId                         compiled_java_class_loader;
    protected Class                                        compiled_java_class;
    protected boolean                                      use_context_argument;
    protected Constructor                                  compiled_java_class_constructor;

    static protected DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    protected void compile (CallContext context) throws InvalidJavaSource {
        if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendDiagnostics(context, "Generator: compiling '%(template)'", "template", this.getFullClassName(context)); }

        this.getJavaCodeManager(context).compile(context);

        loaded             = false;
        reloading_required = true;
    }

    protected boolean tryToLoadJavaClassAsResource(CallContext context) {
        if (this.useExistingResourceUnconditionally(context) && this.compiled_java_class != null) {
            // got it already from previous check
            return true;
        } else {
            try {
                this.compiled_java_class = ClassCache.getClassForName(context, this.getFullClassName(context));

                if (this.useExistingResourceUnconditionally(context) == false) {
                    ArtefactHistory ah = (ArtefactHistory) this.compiled_java_class.getAnnotation(ArtefactHistory.class);
                    if (ah != null && ah.Created() != null && ah.Created().isEmpty() == false) {
                        Date class_creation_date;
                        try {
                            class_creation_date = date_format.parse(ah.Created());
                        } catch (ParseException pe) {
                            System.err.println("warning: class file in class path contains creation date with invalid syntax, not loading this way (" + this.getFullClassName(context) + ")");
                            this.compiled_java_class = null;
                            return false;
                        }
                        long java_modification = getJavaCodeManager(context).getDefaultResource(context).getJavaFile(context).lastModified();

                        // Here are some ugly issues:
                        // 1. ArtefactHistory.Created is written into generated file,
                        //    but file last modification obviously is a little later
                        // 2. We should not use file created date (birth date), since
                        //    these do not change when files get overwritten
                        // 3. File.lastModified says it returns milliseconds, but
                        //    it does not, it always returns '000' as last digits
                        // 4. most reliable and correct solution would be not to
                        //    use File.lastModified, but read the ArtefactHistory.Created
                        //    value from the source file directly; yet, this check
                        //    would be rather costly
                        // 5. therefore, we stay with File.lastModified and add a
                        //    tolerance; worst case that can happen is when two
                        //    creations happen very shortly one after the other

                        // Tools for examination:
                        // emacs: date-to-stamp and stamp-to-date
                        // show fs modification timestamp and annotation value:
                        // FILE= ; stat -c "%y" ${FILE} ; grep ArtefactHistory ${FILE} | sed -e 's/^.*Created="//' -e 's/").*$//'

                        if (java_modification > class_creation_date.getTime() + 1000) {
                            System.err.println("note: class file in class path is older than java source, not loading this way (" + this.getFullClassName(context) + ")");
                            this.compiled_java_class = null;
                            return false;
                        }
                    }
                }
                return true;
            } catch (ClassNotFoundException cnfe) {
                return false;
            }
        }
    }

    protected void load (CallContext context) throws InvalidJavaSource {
        if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendDiagnostics(context, "Generator: loading '%(template)'", "template", this.getFullClassName(context)); }

        try {
            boolean success = false;
            if (this.tryToLoadAsResource(context)) {
                if (reloading_required) {
                    // System.err.println("Load as resource skipped: class was compiled, assuming imperative need to load via ClassLoader");
                } else {
                    success = this.tryToLoadJavaClassAsResource(context);
                }
            }
            if (success == false) {
                ClassLoader ccl = Thread.currentThread().getContextClassLoader();
                URLClassLoaderWithId cjcl = null;
                if (compiled_java_class_loaders == null) {
                    compiled_java_class_loaders = new HashMap<ClassLoader,URLClassLoaderWithId>();
                } else {
                    cjcl = compiled_java_class_loaders.get(ccl);
                }

                if (reloading_required) {
                    if (cjcl != null && cjcl.isLoaded(this.getFullClassName(context))) {
                        // System.err.println("Class " + this.getFullClassName(context) + " needs reloading");
                        cjcl = null;
                    }
                }

                if (cjcl == null) {
                    String cjcl_id = "DynamicClassLoader#" + (cjcl_count++);
                    if (cjcl_count > 0 && cjcl_count % 10 == 0) {
                        System.err.println("New " + cjcl_id);
                    }
                    cjcl = new URLClassLoaderWithId(new URL[]{(new File(JavaCodeManagerImpl.config.getDestinationPath(context))).toURL()}, ccl, cjcl_id, null, null, true);
                    compiled_java_class_loaders.put(ccl, cjcl);
                }
                cjcl.addExcludedClass(this.getFullClassName(context));
                this.compiled_java_class_loader = cjcl;
                this.compiled_java_class = this.compiled_java_class_loader.loadClass(this.getFullClassName(context));

                reloading_required = false;
            }
        } catch (MalformedURLException mue) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, mue, "URL, created by File class, for '%(destination)', is unexpectedly invalid", "destination", JavaCodeManagerImpl.config.getDestinationPath(context));
            throw (ExceptionConfigurationError) null; // compiler insists
        } catch (ClassNotFoundException cnfe) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, cnfe, "Compiled class could not be found '%(classname)'", "classname", this.getFullClassName(context));
            throw (ExceptionConfigurationError) null; // compiler insists
        }

        try {
            this.use_context_argument = true;
            this.compiled_java_class_constructor = this.compiled_java_class.getConstructor(CallContext.class);
        } catch (NoSuchMethodException nsme) {
            try {
                this.use_context_argument = false;
                this.compiled_java_class_constructor = this.compiled_java_class.getConstructor();
            } catch (NoSuchMethodException nsme2) {
                InvalidJavaSource.createAndThrow(context, nsme2, "Compiled class '%(classname)' exists, but does not provide an appropriate constructor", "classname", this.getFullClassName(context));
                throw (InvalidJavaSource) null; // compiler insists
            }
        }

        loaded = true;
    }

    public void notifyCodeGenerationCompleted(CallContext context) {
        this.getJavaCodeManager(context).closeResources(context);
        this.writeImplementationVersion(context);
    }

    protected void prepare(CallContext context) throws InvalidJavaSource {
        prepare(context, true, true, true);
    }

    protected void prepare(CallContext context, boolean generation, boolean compilation, boolean loading) throws InvalidJavaSource {
        if (generation && this.doGeneration(context) && this.doAutoGeneration(context) && needsGeneration(context)) {
            this.generateCode(context);
        }

        if (compilation && this.doCompilation(context) && needsCompilation(context)) {
            compile(context);
        }

        if (loading && needsLoading(context)) {
            load(context);
        }
    }

    protected void writeImplementationVersion(CallContext context) {
        this.implementation_version_successfully_tested = false;
        File jmf = this.getJavaCodeManager(context).getDefaultResource(context).getJavaMetaFile(context);
        String current_generator_version = Package.getPackage(this.getCodeGeneratorPackage(context)).getImplementationVersion();
        try {
            FileWriter fw = new FileWriter(jmf);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("CodeGeneratorPackage:" + this.getCodeGeneratorPackage(context) + "\n");
            bw.write("CodeGeneratorVersion:" + current_generator_version + "\n");
            bw.write(this.getAdditionalMetaData(context) + "\n");
            bw.close();
            fw.close();
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, ioe, "Cannot access or read meta file");
            throw (ExceptionConfigurationError) null; // compiler insists
        }    
    }

    protected boolean implementation_version_successfully_tested = false;
    protected RegularExpression version_re = new RegularExpression("^CodeGeneratorVersion:(.*)$");

    protected boolean testImplementationVersionMatch(CallContext context) {
        if (this.implementation_version_successfully_tested == false) {
            String current_code_generator_version = Package.getPackage(this.getCodeGeneratorPackage(context)).getImplementationVersion();
            if (current_code_generator_version == null) {
                return true;
            }
            File jmf = this.getJavaCodeManager(context).getDefaultResource(context).getJavaMetaFile(context);
            if (jmf.exists()) {
                try {
                    FileReader fr = new FileReader(jmf);
                    BufferedReader br = new BufferedReader(fr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] m = version_re.tryGetMatches(context, line);
                        if (m != null && m[0] != null && m[0].equals(current_code_generator_version)) {
                            this.implementation_version_successfully_tested = true;
                            break;
                        }
                    }
                    br.close();
                    fr.close();
                } catch (FileNotFoundException fnfe) {
                    CustomaryContext.create((Context)context).throwConfigurationError(context, fnfe, "Cannot access or read meta file");
                    throw (ExceptionConfigurationError) null; // compiler insists
                } catch (IOException ioe) {
                    CustomaryContext.create((Context)context).throwConfigurationError(context, ioe, "Cannot access or read meta file");
                    throw (ExceptionConfigurationError) null; // compiler insists
                }
            }
        }
        return this.implementation_version_successfully_tested;
    }

    abstract protected String getAdditionalMetaData(CallContext context);

    abstract protected String getCodeGeneratorPackage(CallContext context);

    abstract protected long getLastModificationOfCodeGeneratorSource(CallContext context);

    abstract protected long getLastModificationOfCodeGeneratorConfiguration(CallContext context);

    abstract protected boolean doGeneration(CallContext context);

    abstract protected boolean doAutoGeneration(CallContext context);

    abstract protected boolean doCompilation(CallContext context);

    abstract protected boolean tryToLoadAsResource(CallContext context);

    abstract protected boolean useExistingResourceUnconditionally(CallContext context);

    abstract protected void generateCode(CallContext context);

    protected String[] getAdditionalRelativeDataResources(CallContext context) {
        return null;
    }
}
