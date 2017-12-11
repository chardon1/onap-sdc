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

package org.openecomp.sdc.logging.context;

import org.openecomp.sdc.logging.types.LoggerConstants;
import org.slf4j.MDC;


public abstract class MdcData {
  private String level;
  private String errorCode;

  MdcData() {
  }

  public MdcData(String level, String errorCode) {
    this.level = level;
    this.errorCode = errorCode;
  }

  public void setMdcValues() {
    MDC.put(LoggerConstants.ERROR_CATEGORY, this.level);
    MDC.put(LoggerConstants.ERROR_CODE, this.errorCode);
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }
}
