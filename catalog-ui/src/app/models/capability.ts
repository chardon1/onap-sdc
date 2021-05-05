/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

/**
 * Created by obarda on 4/20/2016.
 */
'use strict';
import * as _ from 'lodash';
import { PropertyModel } from './properties';

export interface RequirementCapabilityModel {}

// this is an object contains keys, when each key has matching array.
// for example: key = tosca.capabilities.network.Linkable and the match array is array of capabilities objects
export class CapabilitiesGroup {
    constructor(capabilityGroupObj?: CapabilitiesGroup) {
        _.forEach(capabilityGroupObj, (capabilitiesArrayObj: Capability[], instance) => {
            this[instance] = [];
            _.forEach(capabilitiesArrayObj, (capability: Capability): void => {
                this[instance].push(new Capability(capability));
            });
        });
    }
    
    public static getFlattenedCapabilities(capabilitiesGroup: CapabilitiesGroup): Array<Capability> {
        return _.reduce(
            _.toArray(capabilitiesGroup),
            (allCaps, capArr) => allCaps.concat(capArr),
            []);
    }
}

export class Capability implements RequirementCapabilityModel {

    // server data
    name: string;
    ownerId: string;
    ownerName: string;
    type: string;
    uniqueId: string;
    capabilitySources: string[];
    leftOccurrences: string;
    minOccurrences: string | number;
    maxOccurrences: string;
    description: string;
    validSourceTypes: string[];
    properties: PropertyModel[];
    external: boolean;

    // custom
    selected: boolean;
    filterTerm: string;

    constructor(capability?: Capability) {

        if (capability) {
            // server data
            this.name = capability.name;
            this.ownerId = capability.ownerId;
            this.ownerName = capability.ownerName;
            this.type = capability.type;
            this.uniqueId = capability.uniqueId;
            this.capabilitySources = capability.capabilitySources;
            this.leftOccurrences = capability.leftOccurrences;
            this.minOccurrences = capability.minOccurrences;
            this.maxOccurrences = capability.maxOccurrences;
            this.properties = capability.properties;
            this.description = capability.description;
            this.validSourceTypes = capability.validSourceTypes;
            this.selected = capability.selected;
            this.external = capability.external;
            this.initFilterTerm();

        }
    }

    public getTitle(): string {
        return this.ownerName + ': ' + this.name;
    }

    public getFullTitle(): string {
        const maxOccurrences: string = this.maxOccurrences === 'UNBOUNDED' ? '∞' : this.maxOccurrences;
        return this.getTitle() + ': [' + this.minOccurrences + ', ' + maxOccurrences + ']';
    }

    public toJSON = (): any => {
        this.selected = undefined;
        this.filterTerm = undefined;
        return this;
    }

    public isFulfilled() {
        return parseInt(this.leftOccurrences) === 0;
    }

    private initFilterTerm = (): void => {
        this.filterTerm = this.name + ' ' +
            (this.type ? (this.type.replace('tosca.capabilities.', '') + ' ' ) : '') +
            (this.description || '') + ' ' +
            (this.ownerName || '') + ' ' +
            (this.validSourceTypes ? (this.validSourceTypes.join(',') + ' ') : '') +
            this.minOccurrences + ',' + this.maxOccurrences;
        if (this.properties && this.properties.length) {
            _.forEach(this.properties, (prop: PropertyModel) => {
                this.filterTerm += ' ' + prop.name +
                    ' ' + (prop.description || '') +
                    ' ' + prop.type +
                    (prop.schema && prop.schema.property ? (' ' + prop.schema.property.type) : '');
            });
        }
    }
}

export class CapabilityUI extends Capability {
    isCreatedManually: boolean;

    constructor(input: Capability, componentUniqueId: string) {
        super(input);
        this.isCreatedManually = input.ownerId === componentUniqueId;
    }
}


