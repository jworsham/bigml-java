package org.bigml.binding;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.bigml.binding.resources.BatchPrediction;
import org.bigml.binding.resources.Dataset;
import org.bigml.binding.resources.Ensemble;
import org.bigml.binding.resources.Evaluation;
import org.bigml.binding.resources.Model;
import org.bigml.binding.resources.Prediction;
import org.bigml.binding.resources.Source;
import org.json.simple.JSONObject;

/**
 * Entry point to create, retrieve, list, update, and delete sources, datasets, models,
 * predictions, evaluations and ensembles.
 *
 * Full API documentation on the API can be found from BigML at: https://bigml.com/developers
 *
 * Resources are wrapped in a dictionary that includes:
        code: HTTP status code
        resource: The resource/id
        location: Remote location of the resource
        object: The resource itself
        error: An error code and message

    If left unspecified, `username` and `api_key` will default to the
    values of the `BIGML_USERNAME` and `BIGML_API_KEY` environment
    variables respectively.

    If `dev_mode` is set to `True`, the API will be used in development
    mode where the size of your datasets are limited but you are not
    charged any credits.

    If storage is set to a directory name, the resources obtained in
    CRU operations will be stored in the given directory.
 */
public class BigMLClient {
  // URLs below don't care about BigML.io version. They will hit last (current)
  // BigML version
  final public static String BIGML_URL = "https://bigml.io/";
  final public static String BIGML_DEV_URL = "https://bigml.io/dev/";

  /**
   * Logging
   */
  static Logger logger = Logger.getLogger(BigMLClient.class.getName());

  private static BigMLClient instance = null;

  private String bigmlUrl;
  private String bigmlUser;
  private String bigmlApiKey;
  private Source source;
  private Dataset dataset;
  private Model model;
  private Prediction prediction;
  private Evaluation evaluation;
  private Ensemble ensemble;
  private BatchPrediction batchPrediction;
  private Properties props;
  private Boolean devMode = false;
  private String storage;

  protected BigMLClient() {
  }

  public static BigMLClient getInstance() throws AuthenticationException {
    if (instance == null) {
      instance = new BigMLClient();
      instance.init(false);
    }
    return instance;
  }

  public static BigMLClient getInstance(final String storage) throws AuthenticationException {
    if (instance == null) {
      instance = new BigMLClient();
      instance.init(null, null, false, storage);
    }
    return instance;
  }

  public static BigMLClient getInstance(final boolean devMode) throws AuthenticationException {
    if (instance == null) {
      instance = new BigMLClient();
      instance.init(devMode);
    }
    return instance;
  }

  public static BigMLClient getInstance(final String apiUser, final String apiKey, final boolean devMode) throws AuthenticationException {
    if (instance == null) {
      instance = new BigMLClient();
      instance.init(apiUser, apiKey, devMode);
    }
    return instance;
  }

  public static BigMLClient getInstance(final String apiUser, final String apiKey, final boolean devMode, final String storage) throws AuthenticationException {
    if (instance == null) {
      instance = new BigMLClient();
      instance.init(apiUser, apiKey, devMode, storage);
    }
    return instance;
  }


  /**
   * Initialization object.
   */
  private void init(final boolean devMode) throws AuthenticationException {
	this.devMode = devMode;
    initConfiguration();

    this.bigmlUser = System.getProperty("BIGML_USERNAME");
    this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
    if (this.bigmlUser == null || this.bigmlUser.equals("") || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
      this.bigmlUser = props.getProperty("BIGML_USERNAME");
      this.bigmlApiKey = props.getProperty("BIGML_API_KEY");
      if (this.bigmlUser == null || this.bigmlUser.equals("") || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
        AuthenticationException ex = new AuthenticationException("Missing authentication information.");
        logger.info(instance, ex);
        throw ex;
      }
    }

    initResources();
  }

  /**
   * Initialization object.
   */
  private void init(final String apiUser, final String apiKey, final boolean devMode) throws AuthenticationException {
    this.devMode = devMode;
	initConfiguration();

    this.bigmlUser = apiUser != null ? apiUser : System.getProperty("BIGML_USERNAME");
    this.bigmlApiKey = apiKey != null ? apiKey : System.getProperty("BIGML_API_KEY");
    if (this.bigmlUser == null || this.bigmlUser.equals("") || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
      this.bigmlUser = props.getProperty("BIGML_USERNAME");
      this.bigmlApiKey = props.getProperty("BIGML_API_KEY");
      if (this.bigmlUser == null || this.bigmlUser.equals("") || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
        AuthenticationException ex = new AuthenticationException("Missing authentication information.");
        logger.info(instance, ex);
        throw ex;
      }
    }

    initResources();
  }

  /**
   * Initialization object.
   */
  private void init(final String apiUser, final String apiKey, final boolean devMode, final String storage) throws AuthenticationException {
    this.devMode = devMode;
    this.storage = storage;
	initConfiguration();

    this.bigmlUser = apiUser != null ? apiUser : System.getProperty("BIGML_USERNAME");
    this.bigmlApiKey = apiKey != null ? apiKey : System.getProperty("BIGML_API_KEY");
    if (this.bigmlUser == null || this.bigmlUser.equals("") || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
      this.bigmlUser = props.getProperty("BIGML_USERNAME");
      this.bigmlApiKey = props.getProperty("BIGML_API_KEY");
      if (this.bigmlUser == null || this.bigmlUser.equals("") || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
        AuthenticationException ex = new AuthenticationException("Missing authentication information.");
        logger.info(instance, ex);
        throw ex;
      }
    }

    initResources();
  }


  private void initConfiguration() {
    try {
      props = new Properties();
      FileInputStream fis = new FileInputStream(new File("src/main/resources/binding.properties"));
      props.load(fis);
      fis.close();

      bigmlUrl = this.devMode ? props.getProperty("BIGML_DEV_URL",
          BIGML_DEV_URL) : props.getProperty("BIGML_URL", BIGML_URL);
    } catch (Throwable e) {
      // logger.error("Error loading configuration", e);
      bigmlUrl = this.devMode ? BIGML_DEV_URL : BIGML_URL;
    }
  }

  private void initResources() {
    source = new Source(this.bigmlUser, this.bigmlApiKey, this.devMode);
    dataset = new Dataset(this.bigmlUser, this.bigmlApiKey, this.devMode);
    model = new Model(this.bigmlUser, this.bigmlApiKey, this.devMode);
    prediction = new Prediction(this.bigmlUser, this.bigmlApiKey, this.devMode);
    evaluation = new Evaluation(this.bigmlUser, this.bigmlApiKey, this.devMode);
    ensemble = new Ensemble(this.bigmlUser, this.bigmlApiKey, this.devMode);
    batchPrediction = new BatchPrediction(this.bigmlUser, this.bigmlApiKey,
        this.devMode);
  }

  public String getBigMLUrl() {
    return bigmlUrl;
  }


  // ################################################################
  // #
  // # Sources
  // # https://bigml.com/developers/sources
  // #
  // ################################################################

  /**
   * Creates a new source.
   *
   * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1 Host: bigml.io
   * Content-Type: multipart/form-data;
   *
   * @param fileName		file containing your data in csv format. It can be compressed, gzipped, or
   * 						zipped. Required multipart/form-data; charset=utf-8
   * @param name			the name you want to give to the new source. Optional
   * @param sourceParser	set of parameters to parse the source. Optional
   *
   */
  public JSONObject createSource(final String fileName, String name, String sourceParser) {
    return source.createLocalSource(fileName, name, sourceParser);
  }


  /**
   * Creates a source using a URL.
   *
   * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1
   * Host: bigml.io
   * Content-Type: application/json;
   *
   * @param url		url for remote source
   * @param args	set of parameters to create the source. Optional
   *
   */
  public JSONObject createRemoteSource(final String url, final String args) {
    return source.createRemoteSource(url, args);
  }


  /**
   * Creates a source using a URL.
   *
   * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1
   * Host: bigml.io
   * Content-Type: application/json;
   *
   * @param data	inline data for source
   * @param args	set of parameters to create the source. Optional
   *
   */
  public JSONObject createInlineSource(final String data, final String args) {
    return source.createInlineSource(data, args);
  }


  /**
   * Retrieves a remote source.
   *
   * GET /andromeda/source/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param sourceId 	a unique identifier in the form source/id where id is a string of 24
   * 					alpha-numeric chars.
   *
   */
  public JSONObject getSource(final String sourceId) {
    return source.get(sourceId);
  }


  /**
   * Retrieves a remote source.
   *
   * GET /andromeda/source/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param sourceJSON 	a source JSONObject
   *
   */
  public JSONObject getSource(final JSONObject sourceJSON) {
    return source.get(sourceJSON);
  }


  /**
   * Checks whether a source's status is FINISHED.
   *
   * @param sourceId 	a unique identifier in the form source/id where id is a string of 24
   * 					alpha-numeric chars.
   *
   */
  public boolean sourceIsReady(final String sourceId) {
    return source.isReady(sourceId);
  }


  /**
   * Checks whether a source's status is FINISHED.
   *
   * @param sourceJSON 	a source JSONObject
   *
   */
  public boolean sourceIsReady(final JSONObject sourceJSON) {
    return source.isReady(sourceJSON);
  }


  /**
   * Lists all your remote sources.
   *
   * GET /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; Host: bigml.io
   *
   * @param queryString		query filtering the listing.
   *
   */
  public JSONObject listSources(final String queryString) {
    return source.list(queryString);
  }


  /**
   * Updates a source.
   *
   * Updates remote `source` with `changes'.
   *
   * POST
   * /andromeda/source/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io Content-Type: application/json
   *
   * @param sourceId 	a unique identifier in the form source/id where id is a string of 24
   * 					alpha-numeric chars.
   * @param changes		set of parameters to update the source. Optional
   *
   */
  public JSONObject updateSource(final String sourceId, final String changes) {
    return source.update(sourceId, changes);
  }


  /**
   * Updates a source.
   *
   * Updates remote `source` with `changes'.
   *
   * POST
   * /andromeda/source/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io Content-Type: application/json
   *
   * @param sourceJSON 	a source JSONObject
   * @param changes		set of parameters to update the source. Optional
   *
   */
  public JSONObject updateSource(final JSONObject sourceJSON, final JSONObject json) {
    return source.update(sourceJSON, json);
  }


  /**
   * Deletes a remote source permanently.
   *
   * DELETE
   * /andromeda/source/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param sourceId 	a unique identifier in the form source/id where id is a string of 24
   * 					alpha-numeric chars.
   *
   */
  public JSONObject deleteSource(final String sourceId) {
    return source.delete(sourceId);
  }


  /**
   * Deletes a remote source permanently.
   *
   * DELETE
   * /andromeda/source/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param sourceJSON 	a source JSONObject
   *
   */
  public JSONObject delete(final JSONObject sourceJSON) {
    return source.delete(sourceJSON);
  }


  // ################################################################
  // #
  // # Datasets
  // # https://bigml.com/developers/datasets
  // #
  // ################################################################

  /**
   * Creates a remote dataset.
   *
   * Uses remote `source` to create a new dataset using the arguments in
   * `args`.  If `wait_time` is higher than 0 then the dataset creation
   * request is not sent until the `source` has been created successfuly.
   *
   * POST /andromeda/dataset?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1 Host:
   * bigml.io Content-Type: application/json
   *
   * @param sourceId	a unique identifier in the form source/id where id is a string of 24
   * 					alpha-numeric chars for the source to attach the dataset.
   * @param args		set of parameters for the new dataset. Optional
   * @param waitTime	time to wait for next check of FINISHED status for source before to start to
   * 					create the dataset. Optional
   * @param retries		number of times to try the operation. Optional
   *
   */
  public JSONObject createDataset(final String sourceId, String args, Integer waitTime, Integer retries) {
    return dataset.create(sourceId, args, waitTime, retries);
  }


  /**
   * Retrieves a dataset.
   *
   * GET
   * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param datasetId 	a unique identifier in the form datset/id where id is a string of 24
   * 					alpha-numeric chars.
   *
   */
  public JSONObject getDataset(final String datasetId) {
    return dataset.get(datasetId);
  }


  /**
   * Retrieves a dataset.
   *
   * GET
   * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param datasetJSON	a dataset JSONObject
   *
   */
  public JSONObject getDataset(final JSONObject datasetJSON) {
    return dataset.get(datasetJSON);
  }


  /**
   * Check whether a dataset's status is FINISHED.
   *
   * @param datasetId 	a unique identifier in the form dataset/id where id is a string of 24
   * 					alpha-numeric chars.
   *
   */
  public boolean datasetIsReady(final String datasetId) {
    return dataset.isReady(datasetId);
  }


  /**
   * Checks whether a dataset's status is FINISHED.
   *
   * @param datasetJSON	a dataset JSONObject
   *
   */
  public boolean datasetIsReady(final JSONObject datasetJSON) {
    return dataset.isReady(datasetJSON);
  }


  /**
   * Lists all your datasources.
   *
   * GET /andromeda/dataset?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; Host: bigml.io
   *
   * @param queryString		query filtering the listing.
   *
   */
  public JSONObject listDatasets(final String queryString) {
    return dataset.list(queryString);
  }


  /**
   * Updates a dataset.
   *
   * PUT
   * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io Content-Type: application/json
   *
   * @param datasetId 	a unique identifier in the form dataset/id where id is a string of 24
   * 					alpha-numeric chars.
   * @param changes		set of parameters to update the source. Optional
   *
   */
  public JSONObject updateDataset(final String datasetId, final String changes) {
    return dataset.update(datasetId, changes);
  }


  /**
   * Updates a dataset.
   *
   * PUT
   * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io Content-Type: application/json
   *
   * @param datasetJSON	a dataset JSONObject
   * @param changes		set of parameters to update the source. Optional
   *
   */
  public JSONObject updateDataset(final JSONObject datasetJSON, final JSONObject changes) {
    return dataset.update(datasetJSON, changes);
  }


  /**
   * Deletes a dataset.
   *
   * DELETE
   * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param datasetId 	a unique identifier in the form dataset/id where id is a string of 24
   * 					alpha-numeric chars.
   *
   */
  public JSONObject deleteDataset(final String datasetId) {
    return dataset.delete(datasetId);
  }


  /**
   * Deletes a dataset.
   *
   * DELETE
   * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param datasetJSON		a dataset JSONObject
   *
   */
  public JSONObject deleteDataset(final JSONObject datasetJSON) {
    return dataset.delete(datasetJSON);
  }


  // ################################################################
  // #
  // # Models
  // # https://bigml.com/developers/models
  // #
  // ################################################################

  /**
   * Creates a new model.
   *
   * POST /andromeda/model?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1 Host: bigml.io
   * Content-Type: application/json
   *
   * @param datsetId	a unique identifier in the form datset/id where id is a string of 24
   * 					alpha-numeric chars for the dataset to attach the model.
   * @param args		set of parameters for the new model. Optional
   * @param waitTime	time to wait for next check of FINISHED status for source before to start to
   * 					create the model. Optional
   * @param retries		number of times to try the operation. Optional
   *
   */
  public JSONObject createModel(final String datasetId, String args, Integer waitTime, Integer retries) {
    return model.create(datasetId, args, waitTime, retries);
  }


  /**
   * Retrieves a model.
   *
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param modelId 	a unique identifier in the form model/id where id is a string of 24
   * 					alpha-numeric chars.
   *
   */
  public JSONObject getModel(final String modelId) {
    return getModel(modelId, null, null);
  }


  /**
   * Retrieves a model.
   *
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param modelId 	a unique identifier in the form model/id where id is a string of 24
   * 					alpha-numeric chars.
   * @param apiUser		API user
   * @param apiKey		API key
   *
   */
  public JSONObject getModel(final String modelId, final String apiUser, final String apiKey) {
    return model.get(modelId, apiUser, apiKey);
  }


  /**
   * Retrieves a model.
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param modelJSON 	a model JSONObject
   *
   */
  public JSONObject getModel(final JSONObject modelJSON) {
    return getModel(modelJSON, null, null);
  }


  /**
   * Retrieves a model.
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param modelJSON 	a model JSONObject
   * @param apiUser		API user
   * @param apiKey		API key
   *
   */
  public JSONObject getModel(final JSONObject modelJSON, final String apiUser, final String apiKey) {
    return model.get(modelJSON, apiUser, apiKey);
  }


  /**
   * Retrieves a model.
   *
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param modelId 	a unique identifier in the form model/id where id is a string of 24
   * 					alpha-numeric chars.
   * @param queryString query for filtering.
   *
   */
  public JSONObject getModel(final String modelId, final String queryString) {
    return getModel(modelId, queryString, null, null);
  }


  /**
   * Retrieves a model.
   *
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param modelId 	a unique identifier in the form model/id where id is a string of 24
   * 					alpha-numeric chars.
   * @param queryString query for filtering.
   * @param apiUser		API user
   * @param apiKey		API key
   *
   */
  public JSONObject getModel(final String modelId, final String queryString, final String apiUser, final String apiKey) {
    return model.get(modelId, queryString, apiUser, apiKey);
  }


  /**
   * Retrieves a model.
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param modelJSON 	a model JSONObject
   * @param queryString query for filtering.
   *
   */
  public JSONObject getModel(final JSONObject modelJSON, final String queryString) {
    return getModel(modelJSON, queryString, null, null);
  }


  /**
   * Retrieves a model.
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param modelJSON 	a model JSONObject
   * @param queryString query for filtering.
   * @param apiUser		API user
   * @param apiKey		API key
   *
   */
  public JSONObject getModel(final JSONObject modelJSON, final String queryString, final String apiUser, final String apiKey) {
    return model.get(modelJSON, queryString, apiUser, apiKey);
  }


  /**
   * Checks whether a model's status is FINISHED.
   *
   * @param modelId	modelId a unique identifier in the form model/id where id is a string of 24
   * 				alpha-numeric chars.
   *
   */
  public boolean modelIsReady(final String modelId) {
    return model.isReady(modelId);
  }


  /**
   * Checks whether a model's status is FINISHED.
   *
   * @param modelJSON 	a model JSONObject
   *
   */
  public boolean modelIsReady(final JSONObject modelJSON) {
    return model.isReady(modelJSON);
  }


  /**
   * Lists all your models.
   *
   * GET /andromeda/model?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; Host: bigml.io
   *
   * @param queryString		query filtering the listing.
   *
   */
  public JSONObject listModels(final String queryString) {
    return model.list(queryString);
  }


  /**
   * Updates a model.
   *
   * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   * Content-Type: application/json
   *
   * @param modelId 	a unique identifier in the form model/id where id is a string of 24
   * 					alpha-numeric chars.
   * @param changes		set of parameters to update the source. Optional
   *
   */
  public JSONObject updateModel(final String modelId, final String changes) {
    return model.update(modelId, changes);
  }


  /**
   * Updates a model.
   *
   * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   * Content-Type: application/json
   *
   * @param model	modelJSON 	a model JSONObject
   * @param changes	set of parameters to update the source. Optional
   *
   */
  public JSONObject updateModel(final JSONObject modelJSON, final JSONObject changes) {
    return model.update(modelJSON, changes);
  }


  /**
   * Deletes a model.
   *
   * DELETE
   * /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param modelId 	a unique identifier in the form model/id where id is a string of 24
   * 					alpha-numeric chars.
   *
   */
  public JSONObject deleteModel(final String modelId) {
    return model.delete(modelId);
  }


  /**
   * Deletes a model.
   *
   * DELETE
   * /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param modelJSON 	a model JSONObject
   *
   */
  public JSONObject deleteModel(final JSONObject modelJSON) {
    return model.delete(modelJSON);
  }


  // ################################################################
  // #
  // # Predictions
  // # https://bigml.com/developers/predictions
  // #
  // ################################################################

  /**
   * Creates a new prediction.
   *
   * POST /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1 Host:
   * bigml.io Content-Type: application/json
   *
   * @param modelOrEnsembleId		a unique identifier in the form model/id  or ensembke/id where id is a string of 24
   * 								alpha-numeric chars for the nodel or ensemble to attach the prediction.
   * @param inputData	an object with field's id/value pairs representing the instance you
   * 					want to create a prediction for.
   * @param byName
   * @param args		set of parameters for the new prediction. Required
   * @param waitTime	time to wait for next check of FINISHED status for model before to start to
   * 					create the prediction. Optional
   * @param retries		number of times to try the operation. Optional
   *
   */
  public JSONObject createPrediction(final String modelOrEnsembleId, JSONObject inputData, Boolean byName, String args, Integer waitTime, Integer retries) {
    return prediction.create(modelOrEnsembleId, inputData, byName, args, waitTime, retries);
  }


  /**
   * Retrieves a prediction.
   *
   * GET
   * /andromeda/prediction/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param predictionId 	a unique identifier in the form prediction/id where id is a string of 24
   * 						alpha-numeric chars.
   *
   */
  public JSONObject getPrediction(final String predictionId) {
    return prediction.get(predictionId);
  }


  /**
   * Retrieves a prediction.
   *
   * GET
   * /andromeda/prediction/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param predictionJSON	a prediction JSONObject
   *
   */
  public JSONObject getPrediction(final JSONObject predictionJSON) {
    return prediction.get(predictionJSON);
  }


  /**
   * Checks whether a prediction's status is FINISHED.
   *
   * @param predictionId 	a unique identifier in the form prediction/id where id is a string of 24
   * 						alpha-numeric chars.
   *
   */
  public boolean predictionIsReady(final String predictionId) {
    return prediction.isReady(predictionId);
  }


  /**
   * Checks whether a prediction's status is FINISHED.
   *
   * @param predictionJSON	a prediction JSONObject
   *
   */
  public boolean predictionIsReady(final JSONObject predictionJSON) {
    return prediction.isReady(predictionJSON);
  }


  /**
   * Lists all your predictions.
   *
   * GET /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param queryString	query filtering the listing.
   *
   */
  public JSONObject listPredictions(final String queryString) {
    return prediction.list(queryString);
  }


  /**
   * Updates a prediction.
   *
   * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   * Content-Type: application/json
   *
   * @param predictionId 	a unique identifier in the form prediction/id where id is a string of 24
   * 						alpha-numeric chars.
   * @param changes			set of parameters to update the source. Optional
   *
   */
  public JSONObject updatePrediction(final String predictionId, final String changes) {
    return prediction.update(predictionId, changes);
  }


  /**
   * Updates a prediction.
   *
   * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   * Content-Type: application/json
   *
   * @param predictionJSON	a prediction JSONObject
   * @param changes			set of parameters to update the source. Optional
   *
   */
  public JSONObject updatePrediction(final JSONObject predictionJSON, final JSONObject changes) {
    return prediction.update(predictionJSON, changes);
  }


  /**
   * Deletes a prediction.
   *
   * DELETE
   * /andromeda/prediction/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param predictionId 	a unique identifier in the form prediction/id where id is a string of 24
   *						alpha-numeric chars
   *
   */
  public JSONObject deletePrediction(final String predictionId) {
    return prediction.delete(predictionId);
  }


  /**
   * Deletes a prediction.
   *
   * DELETE
   * /andromeda/prediction/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param predictionJSON	a prediction JSONObject
   *
   */
  public JSONObject deletePrediction(final JSONObject predictionJSON) {
    return prediction.delete(predictionJSON);
  }


  // ################################################################
  // #
  // # Evaluations
  // # https://bigml.com/developers/evaluations
  // #
  // ################################################################

  /**
   * Creates a new evaluation.
   *
   * POST /andromeda/evaluation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1
   * Host: bigml.io
   * Content-Type: application/json
   *
   * @param modelOrEnsembleId		a unique identifier in the form model/id or ensemble/id where id is a string of 24
   *                	alpha-numeric chars for the model/ensemble to attach the evaluation.
   * @param datasetId	a unique identifier in the form dataset/id where id is a string of 24
   * 					alpha-numeric chars for the dataset to attach the evaluation.
   * @param args		set of parameters for the new evaluation. Optional
   * @param waitTime	time to wait for next check of FINISHED status for model before to start to
   * 					create the evaluation. Optional
   * @param retries		number of times to try the operation. Optional
   *
   */
  public JSONObject createEvaluation(final String modelOrEnsembleId, final String datasetId, String args, Integer waitTime, Integer tries) {
    return evaluation.create(modelOrEnsembleId, datasetId, args, waitTime, tries);
  }


  /**
   * Retrieves an evaluation.
   *
   * An evaluation is an evolving object that is processed until it
   * reaches the FINISHED or FAULTY state, the method will return a
   * JSONObject that encloses the evaluation values and state info
   * available at the time it is called.
   *
   * GET /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param evaluationId 	a unique identifier in the form evaluation/id where id
   * 						is a string of 24 alpha-numeric chars.
   *
   */
  public JSONObject getEvaluation(final String evaluationId) {
    return evaluation.get(evaluationId);
  }


  /**
   * Retrieves an evaluation.
   *
   * An evaluation is an evolving object that is processed until it
   * reaches the FINISHED or FAULTY state, the method will return a
   * JSONObject that encloses the evaluation values and state info
   * available at the time it is called.
   *
   * GET /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param evaluationJSON 	an evaluation JSONObject.
   *
   */
  public JSONObject getEvaluation(final JSONObject evaluationJSON) {
    return evaluation.get(evaluationJSON);
  }


  /**
   * Check whether a evaluation' status is FINISHED.
   *
   * @param evaluationId 	a unique identifier in the form evaluation/id where id
   * 						is a string of 24 alpha-numeric chars.
   *
   */
  public boolean evaluationIsReady(final String evaluationId) {
    return evaluation.isReady(evaluationId);
  }


  /**
   * Check whether a evaluation' status is FINISHED.
   *
   * @param evaluationJSON 	an evaluation JSONObject.
   *
   */
  public boolean evaluationIsReady(final JSONObject evaluationJSON) {
    return evaluation.isReady(evaluationJSON);
  }


  /**
   * Lists all your evaluations.
   *
   * GET /andromeda/evaluation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; Host: bigml.io
   *
   * @param queryString	query filtering the listing.
   *
   */
  public JSONObject listEvaluations(final String queryString) {
    return evaluation.list(queryString);
  }


  /**
   * Updates an evaluation.
   *
   * PUT /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   * Content-Type: application/json
   *
   * @param evaluationId	a unique identifier in the form evauation/id where id
   * 						is a string of 24 alpha-numeric chars.
   * @param changes			set of parameters to update the evaluation. Optional
   *
   */
  public JSONObject updateEvaluation(final String evaluationId, final String changes) {
    return evaluation.update(evaluationId, changes);
  }


  /**
   * Updates an evaluation.
   *
   * PUT /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   * Content-Type: application/json
   *
   * @param evaluationJSON	an evaluation JSONObject
   * @param changes			set of parameters to update the evaluation. Optional
   */
  public JSONObject updateEvaluation(final JSONObject evaluationJSON, final JSONObject changes) {
    return evaluation.update(evaluationJSON, changes);
  }


  /**
   * Deletes an evaluation.
   *
   * DELETE /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param evaluationId 	a unique identifier in the form evaluation/id where id is a
   * 						string of 24 alpha-numeric chars.
   *
   */
  public JSONObject deleteEvaluation(final String evaluationId) {
    return evaluation.delete(evaluationId);
  }


  /**
   * Deletes an evaluation.
   *
   * DELETE /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param evaluationJSON 	an evaluation JSONObject.
   *
   */
  public JSONObject deleteEvaluation(final JSONObject evaluationJSON) {
    return evaluation.delete(evaluationJSON);
  }



 //################################################################
 // #
 // # Ensembles
 // # https://bigml.com/developers/ensembles
 // #
 // ################################################################

  /**
   * Creates a new ensemble.
   *
   * POST /andromeda/ensemble?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1
   * Host: bigml.io
   * Content-Type: application/json
   *
   * @param datasetId	a unique identifier in the form dataset/id where id is a string of 24
   * 					alpha-numeric chars for the dataset to attach the evaluation.
   * @param args		set of parameters for the new ensemble. Optional
   * @param waitTime	time to wait for next check of FINISHED status for dataset before to start to
   * 					create the ensemble. Optional
   * @param retries		number of times to try the operation. Optional
   *
   */
  public JSONObject createEnsemble(final String datasetId, String args, Integer waitTime, Integer tries) {
    return ensemble.create(datasetId, args, waitTime, tries);
  }


  /**
   * Retrieves an ensemble.
   *
   * An ensemble is an evolving object that is processed until it
   * reaches the FINISHED or FAULTY state, the method will return a
   * JSONObject that encloses the ensemble values and state info
   * available at the time it is called.
   *
   * GET /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param ensembleId 		a unique identifier in the form ensemble/id where id
   * 						is a string of 24 alpha-numeric chars.
   *
   */
  public JSONObject getEnsemble(final String ensembleId) {
    return ensemble.get(ensembleId);
  }


  /**
   * Retrieves an ensemble.
   *
   * An ensemble is an evolving object that is processed until it
   * reaches the FINISHED or FAULTY state, the method will return a
   * JSONObject that encloses the ensemble values and state info
   * available at the time it is called.
   *
   * GET /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param ensembleJSON 	an ensemble JSONObject.
   *
   */
  public JSONObject getEnsemble(final JSONObject ensembleJSON) {
    return ensemble.get(ensembleJSON);
  }



  /**
   * Check whether a ensemble's status is FINISHED.
   *
   * @param ensembleId 		a unique identifier in the form ensemble/id where id
   * 						is a string of 24 alpha-numeric chars.
   *
   */
  public boolean ensembleIsReady(final String ensembleId) {
    return ensemble.isReady(ensembleId);
  }

  /**
   * Check whether a ensemble's status is FINISHED.
   *
   * @param ensembleJSON 	an ensemble JSONObject.
   *
   */
  public boolean ensembleIsReady(final JSONObject ensembleJSON) {
    return ensemble.isReady(ensembleJSON);
  }


  /**
   * Lists all your ensembles.
   *
   * GET /andromeda/ensemble?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; Host: bigml.io
   *
   * @param queryString	query filtering the listing.
   *
   */
  public JSONObject listEnsembles(final String queryString) {
    return ensemble.list(queryString);
  }


  /**
   * Updates an ensemble.
   *
   * PUT /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   * Content-Type: application/json
   *
   * @param ensembleId		a unique identifier in the form ensemble/id where id
   * 						is a string of 24 alpha-numeric chars.
   * @param changes			set of parameters to update the ensemble. Optional
   *
   */
  public JSONObject updateEnsemble(final String ensembleId, final String changes) {
    return ensemble.update(ensembleId, changes);
  }


  /**
   * Updates an ensemble.
   *
   * PUT /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   * Content-Type: application/json
   *
   * @param ensembleJSON	an ensemble JSONObject
   * @param changes			set of parameters to update the ensemble. Optional
   */
  public JSONObject updateEnsemble(final JSONObject ensembleJSON, final JSONObject changes) {
    return ensemble.update(ensembleJSON, changes);
  }


  /**
   * Deletes an ensemble.
   *
   * DELETE /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param ensembleId 		a unique identifier in the form ensemble/id where id is a
   * 						string of 24 alpha-numeric chars.
   *
   */
  public JSONObject deleteEnsemble(final String ensembleId) {
    return ensemble.delete(ensembleId);
  }

  /**
   * Deletes an ensemble.
   *
   * DELETE /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param ensembleJSON 	an ensemble JSONObject.
   *
   */
  public JSONObject deleteEnsemble(final JSONObject ensembleJSON) {
    return ensemble.delete(ensembleJSON);
  }


  // ################################################################
  // #
  // # Batch predictions
  // # https://bigml.com/developers/batch_predictions
  // #
  // ################################################################

  /**
   * Creates a new batch prediction.
   *
   * POST /andromeda/batchprediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1
   * Host: bigml.io
   * Content-Type: application/json
   *
   * @param modelOrEnsembleId  a unique identifier in the form model/id or ensemble/id where id is a string of 24
   *                            alpha-numeric chars for the model/ensemble to attach the evaluation.
   * @param datasetId           a unique identifier in the form dataset/id where id is a string of 24
   *                            alpha-numeric chars for the dataset to attach the evaluation.
   * @param args                set of parameters for the new batch prediction. Optional
   * @param waitTime            time (milliseconds) to wait for next check of FINISHED status for model
   *                            before to start to create the batch prediction. Optional
   * @param retries             number of times to try the operation. Optional
   *
   */
  public JSONObject createBatchPrediction(final String modelOrEnsembleId, final String datasetId, String args, Integer waitTime, Integer retries) {
    return batchPrediction.create(modelOrEnsembleId, datasetId, args, waitTime,
        retries);
  }

  /**
   * Retrieves a batch prediction.
   *
   * The batch_prediction parameter should be a string containing the
   * batch_prediction id or the dict returned by create_batch_prediction. As
   * batch_prediction is an evolving object that is processed until it reaches
   * the FINISHED or FAULTY state, the function will return a dict that encloses
   * the batch_prediction values and state info available at the time it is
   * called.
   *
   * GET /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
   *
   * @param batchPredictionId
   *          a unique identifier in the form batchPrediction/id where id is a
   *          string of 24 alpha-numeric chars.
   *
   */
  public JSONObject getBatchPrediction(final String batchPredictionId) {
    return batchPrediction.get(batchPredictionId);
  }


  /**
   * Retrieves a batch prediction.
   *
   * The batch_prediction parameter should be a string containing the
   * batch_prediction id or the dict returned by create_batch_prediction. As
   * batch_prediction is an evolving object that is processed until it reaches
   * the FINISHED or FAULTY state, the function will return a dict that encloses
   * the batch_prediction values and state info available at the time it is
   * called.
   *
   * GET /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
   *
   * @param batchPredictionJSON
   *          a batch prediction JSONObject.
   *
   */
  public JSONObject getBatchPrediction(final JSONObject batchPredictionJSON) {
    return batchPrediction.get(batchPredictionJSON);
  }

  /**
   * Retrieves the batch predictions file.
   *
   * Downloads predictions, that are stored in a remote CSV file. If a path is
   * given in filename, the contents of the file are downloaded and saved
   * locally. A file-like object is returned otherwise.
   *
   * @param batchPredictionId
   *          a unique identifier in the form batchPrediction/id where id is a
   *          string of 24 alpha-numeric chars.
   * @param filename
   *          Path to save file locally
   *
   */
  public JSONObject downloadBatchPrediction(final String batchPredictionId,
      final String filename) {
    return batchPrediction.downloadBatchPrediction(batchPredictionId, filename);
  }

  /**
   * Retrieves the batch predictions file.
   *
   * Downloads predictions, that are stored in a remote CSV file. If a path is
   * given in filename, the contents of the file are downloaded and saved
   * locally. A file-like object is returned otherwise.
   *
   * @param batchPredictionJSON
   *          a batch prediction JSONObject.
   * @param filename
   *          Path to save file locally
   *
   */
  public JSONObject downloadBatchPrediction(
      final JSONObject batchPredictionJSON,
      final String filename) {
    return batchPrediction.downloadBatchPrediction(batchPredictionJSON,
        filename);
  }

  /**
   * Check whether a batch prediction's status is FINISHED.
   *
   * @param batchPredictionId
   *          a unique identifier in the form batchPrediction/id where id is a
   *          string of 24 alpha-numeric chars.
   *
   */
  public boolean batchPredictionIsReady(final String batchPredictionId) {
    return batchPrediction.isReady(batchPredictionId);
  }

  /**
   * Check whether a batch prediction's status is FINISHED.
   *
   * @param batchPredictionJSON
   *          a batchPrediction JSONObject.
   *
   */
  public boolean batchPredictionIsReady(final JSONObject batchPredictionJSON) {
    return batchPrediction.isReady(batchPredictionJSON);
  }

  /**
   * Lists all your batch predictions.
   *
   * GET /andromeda/batchprediction?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; Host: bigml.io
   *
   * @param queryString
   *          query filtering the listing.
   *
   */
  public JSONObject listBatchPredictions(final String queryString) {
    return batchPrediction.list(queryString);
  }

  /**
   * Updates a batch prediction.
   *
   * PUT /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
   *
   * @param batchPredictionId
   *          a unique identifier in the form batchPrediction/id where id is a
   *          string of 24 alpha-numeric chars.
   * @param changes
   *          set of parameters to update the batch prediction. Optional
   *
   */
  public JSONObject updateBatchPrediction(final String batchPredictionId,
      final String changes) {
    return batchPrediction.update(batchPredictionId, changes);
  }

  /**
   * Updates a batch prediction.
   *
   * PUT /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
   *
   * @param batchpredictionJSON
   *          a batch prediction JSONObject
   * @param changes
   *          set of parameters to update the batch prediction. Optional
   */
  public JSONObject updateBatchPrediction(final JSONObject batchpredictionJSON,
      final JSONObject changes) {
    return batchPrediction.update(batchpredictionJSON, changes);
  }

  /**
   * Deletes a batch prediction.
   *
   * DELETE /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; HTTP/1.1
   *
   * @param batchPredictionId
   *          a unique identifier in the form batchPrediction/id where id is a
   *          string of 24 alpha-numeric chars.
   *
   */
  public JSONObject deleteBatchPrediction(final String batchPredictionId) {
    return batchPrediction.delete(batchPredictionId);
  }

  /**
   * Deletes a batch prediction.
   *
   * DELETE /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; HTTP/1.1
   *
   * @param batchPredictionJSON
   *          a batch prediction JSONObject.
   *
   */
  public JSONObject deleteBatchPrediction(final JSONObject batchPredictionJSON) {
    return batchPrediction.delete(batchPredictionJSON);
  }

}