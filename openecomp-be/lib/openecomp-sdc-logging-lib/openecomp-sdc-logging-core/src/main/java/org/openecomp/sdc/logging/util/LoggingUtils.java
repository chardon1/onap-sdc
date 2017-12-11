/*
 * Copyright © 2016-2017 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.logging.util;

/**
 * Created by TALIO on 1/10/2017.
 */
public class LoggingUtils {

  public static String getCallingMethodNameForDebugging() {
    return Thread.currentThread().getStackTrace()[4].getMethodName();
  }

  public static String getDeclaringClass(){
    return Thread.currentThread().getStackTrace()[4].getClassName();
  }
}
