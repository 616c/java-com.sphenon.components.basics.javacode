package com.sphenon.basics.javacode.classes;

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
import com.sphenon.basics.customary.*;
import com.sphenon.basics.data.*;

import com.sphenon.basics.javacode.*;

import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

public class Class_DataResource implements DataResource {

    public Class_DataResource (CallContext context) {
    }

    public Class_DataResource (CallContext context, String java_file_path, String class_file_path, String resource_name) {
        this.java_file_path = java_file_path;
        this.class_file_path = class_file_path;
        this.resource_name = resource_name;
    }

    protected String resource_name;

    public String getResourceName (CallContext context) {
        return this.resource_name;
    }

    public void getResourceName (CallContext context, String resource_name) {
        this.resource_name = resource_name;
    }

    protected String java_file_path;

    public String getJavaFilePath (CallContext context) {
        return this.java_file_path;
    }

    protected String class_file_path;

    public String getClassFilePath (CallContext context) {
        return this.class_file_path;
    }

    protected File java_file;

    public File getJavaFile (CallContext context) {
        if (this.java_file == null) {
            this.java_file = new File(this.getJavaFilePath(context));
        }
        return this.java_file;
    }

    protected File class_file;

    public File getClassFile (CallContext context) {
        if (this.class_file == null) {
            this.class_file = new File(this.getClassFilePath(context));
        }
        return this.class_file;
    }

    protected OutputStream ostream;
    protected Writer writer;
    protected BufferedWriter buffered_writer;

    public OutputStream getStream (CallContext context) {
        if (this.ostream == null) {
            try {
                if (this.getJavaFile(context).getParentFile() != null) {
                    this.getJavaFile(context).getParentFile().mkdirs();
                }
                // System.err.println("Writing to " + this.getJavaFile(context).getPath());
                this.ostream = new FileOutputStream(this.getJavaFile(context));
            } catch (FileNotFoundException fnfe) {
                CustomaryContext.create((Context)context).throwConfigurationError(context, "Cannot write intermediate java file '%(filename)'", "filename", this.getJavaFilePath(context));
                throw (ExceptionConfigurationError) null; // compiler insists
            }
        }
        return this.ostream;
    }

    public BufferedWriter getWriter (CallContext context) {
        if (this.buffered_writer == null) {
            try {
                this.ostream = getStream(context);
                this.writer = new OutputStreamWriter(ostream, "UTF-8");
                this.buffered_writer = new BufferedWriter(this.writer);
            } catch (UnsupportedEncodingException uee) {
                CustomaryContext.create((Context)context).throwInstallationError(context, uee, "runtime environment does not support UTF-8 encoding");
                throw (ExceptionInstallationError) null; // compiler insists
            }
        }
        return this.buffered_writer;
    }

    public void close(CallContext context) {
        if (this.buffered_writer != null) {
            try {
                this.buffered_writer.close();
                this.writer.close();
                this.ostream.close();

            } catch (IOException ioe) {
                CustomaryContext.create((Context)context).throwEnvironmentFailure(context, ioe, "Could not close java source file after writing '%(filename)'", "filename", this.getJavaFilePath(context));
                throw (ExceptionEnvironmentFailure) null; // compiler insists
            } finally {
                this.buffered_writer = null;
                this.writer = null;
                this.ostream = null;
            }
        }
    }
}
