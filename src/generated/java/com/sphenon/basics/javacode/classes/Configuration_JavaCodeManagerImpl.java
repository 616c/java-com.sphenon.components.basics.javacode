package com.sphenon.basics.javacode.classes;

import com.sphenon.basics.context.*;
import com.sphenon.basics.customary.*;
import com.sphenon.basics.exception.*;
import com.sphenon.basics.configuration.*;

public class Configuration_JavaCodeManagerImpl implements com.sphenon.basics.javacode.classes.JavaCodeManagerImpl.Config {

    protected Configuration configuration;

    protected Configuration_JavaCodeManagerImpl (CallContext context) {
        configuration = Configuration.create(context, "com.sphenon.basics.javacode.classes.JavaCodeManagerImpl");
    }

    static public Configuration_JavaCodeManagerImpl get (CallContext context) {
        return new Configuration_JavaCodeManagerImpl(context);
    }

    public java.lang.String getSourcePath(CallContext context) {
        String entry = "SourcePath";
        try {
            return configuration.mustGet(context, entry, (java.lang.String) null);
        } catch(ConfigurationEntryNotFound cenf) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, "Required configuration entry '%(entry)' not found", "entry", entry);
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public java.lang.String getClassPath(CallContext context) {
        String entry = "ClassPath";
        try {
            return configuration.mustGet(context, entry, (java.lang.String) null);
        } catch(ConfigurationEntryNotFound cenf) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, "Required configuration entry '%(entry)' not found", "entry", entry);
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public java.lang.String getDestinationPath(CallContext context) {
        String entry = "DestinationPath";
        try {
            return configuration.mustGet(context, entry, (java.lang.String) null);
        } catch(ConfigurationEntryNotFound cenf) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, "Required configuration entry '%(entry)' not found", "entry", entry);
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public java.lang.String getExternalCompiler(CallContext context) {
        String entry = "ExternalCompiler";
        return configuration.get(context, entry, (java.lang.String) null);
    }

    public java.lang.String getOptions(CallContext context) {
        String entry = "Options";
        return configuration.get(context, entry, (java.lang.String) null);
    }

    public boolean getJavaDocEnabled(CallContext context) {
        String entry = "JavaDocEnabled";
        return configuration.get(context, entry, true);
    }

    public boolean getForceGeneration(CallContext context) {
        String entry = "ForceGeneration";
        return configuration.get(context, entry, false);
    }
}
