/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.openecomp.sdc.be.dao.neo4j;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;

public class BatchBuilderTest {

    private GraphRelation graphElement = new GraphRelation();

    @Test
    public void shouldAddAndGetElement() {
        BatchBuilder builder = BatchBuilder.getBuilder();
        builder.add(graphElement);
        assertEquals(graphElement, builder.getElements().get(0));
    }
}