/**
 * Copyright (c) 2014 Lemur Consulting Ltd.
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
package uk.co.flax.biosolr.ontology.search.jena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.flax.biosolr.ontology.config.JenaConfiguration;
import uk.co.flax.biosolr.ontology.config.SolrConfiguration;
import uk.co.flax.biosolr.ontology.search.ResultsList;
import uk.co.flax.biosolr.ontology.search.SearchEngineException;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Matt Pearce
 */
public class JenaOntologySearch {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JenaOntologySearch.class);
	
	private static final String URI_FIELD = "uri";
	private static final String LABEL_FIELD = "label";
	
	private final JenaConfiguration jenaConfig;
	private final SolrConfiguration solrConfig;

	private final Dataset dataset;

	public JenaOntologySearch(JenaConfiguration jenaConfig, SolrConfiguration solrConfig) {
		this.jenaConfig = jenaConfig;
		this.solrConfig = solrConfig;
		this.dataset = buildDataSet();
	}
	
	private Dataset buildDataSet() {
		LOGGER.info("Construct a dataset backed by Solr");
		// Build a text dataset by code.
		FileManager fileManager = FileManager.get();
		Model model = fileManager.loadModel(jenaConfig.getOntologyUri());

		// Build the base dataset backed by the model loaded from the URI
		Dataset ds1 = DatasetFactory.create(model);
		// Define the index mapping
		EntityDefinition entDef = new EntityDefinition(URI_FIELD, LABEL_FIELD, RDFS.label.asNode());
		// Lucene, in memory.
		SolrServer server = new HttpSolrServer(solrConfig.getOntologyUrl());
		// Join together into a dataset
		Dataset ds = TextDatasetFactory.createSolrIndex(ds1, server, entDef);
		return ds;
	}

	public ResultsList<Map<String, String>> searchOntology(String prefix, String query, int rows) throws SearchEngineException {
		ResultsList<Map<String, String>> results = null;
		
		dataset.begin(ReadWrite.READ);
		try {
			List<Map<String, String>> resultsList = new ArrayList<>();
			
			Query q = QueryFactory.create(prefix + "\n" + query);
			QueryExecution qexec = QueryExecutionFactory.create(q, dataset);
			ResultSet rs = qexec.execSelect();
			List<String> vars = rs.getResultVars();
			while (rs.hasNext()) {
				Map<String, String> resultMap = new HashMap<>();
				QuerySolution qs = rs.next();
				for (String var : vars) {
					RDFNode node = qs.get(var);
					if (node != null) {
						resultMap.put(var, node.toString());
					}
				}
				
				if (!resultMap.isEmpty()) {
					resultsList.add(resultMap);
				}
			}
			
			results = new ResultsList<>(resultsList, rows, 0, rows);
		} catch (QueryException e) {
			throw new SearchEngineException(e);
		} finally {
			dataset.end();
		}
		
		return results;
	}

}
