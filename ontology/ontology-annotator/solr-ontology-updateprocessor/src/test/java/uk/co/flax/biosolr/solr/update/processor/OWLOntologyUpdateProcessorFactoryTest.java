/**
 * Copyright (c) 2015 Lemur Consulting Ltd.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.flax.biosolr.solr.update.processor;

import org.junit.BeforeClass;

/**
 * Unit tests for the OntologyUpdateProcessorFactory, using SolrTestCaseJ4
 * to add and check some example records.
 * 
 * @author mlp
 */
public class OWLOntologyUpdateProcessorFactoryTest extends OntologyUpdateProcessorFactoryTest {

	@BeforeClass
	public static void beforeClass() throws Exception {
		// Initialise a single Solr core
		initCore("solrconfig.xml", "schema.xml", "src/test/resources/ontologyUpdate/solr", "documents");
	}

}
