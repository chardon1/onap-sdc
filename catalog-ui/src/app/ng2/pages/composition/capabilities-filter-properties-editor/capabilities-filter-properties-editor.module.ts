/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
*  Copyright (C) 2020 Nordix Foundation. All rights reserved.
*  ================================================================================
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*  SPDX-License-Identifier: Apache-2.0
*  ============LICENSE_END=========================================================
*/

import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {FormElementsModule} from 'app/ng2/components/ui/form-components/form-elements.module';
import {UiElementsModule} from 'app/ng2/components/ui/ui-elements.module';
import {CapabilitiesFilterPropertiesEditorComponent} from "./capabilities-filter-properties-editor.component";

@NgModule({
  declarations: [
    CapabilitiesFilterPropertiesEditorComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    FormElementsModule,
    UiElementsModule
  ],
  exports: [],
  entryComponents: [
    CapabilitiesFilterPropertiesEditorComponent
  ],
  providers: []
})
export class CapabilitiesFilterPropertiesEditorComponentModule {
}
