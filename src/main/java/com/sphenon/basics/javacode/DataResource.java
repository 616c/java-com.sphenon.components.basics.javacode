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

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.File;

public interface DataResource {
    public BufferedWriter getWriter (CallContext context);
    public OutputStream getStream (CallContext context);
    public void close(CallContext context);
    public String getResourceName (CallContext context);
    public String getJavaFilePath (CallContext context);
    public String getClassFilePath (CallContext context);
    public File getJavaFile (CallContext context);
    public File getClassFile (CallContext context);
}
