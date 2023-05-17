package org.sunbird.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerUtil;
import org.sunbird.common.models.util.PropertiesCache;
import org.sunbird.common.request.RequestContext;
import org.sunbird.common.responsecode.ResponseCode;
import scala.concurrent.ExecutionContextExecutor;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

public class ExhaustAPIUtil {

  private static LoggerUtil logger = new LoggerUtil(ExhaustAPIUtil.class);
  private static ObjectMapper mapper = new ObjectMapper();
  private static String exhaustAPISubmitURL = null;
  private static String exhaustAPIListURL  = null;
  static {
    String baseUrl = System.getenv(JsonKey.EXHAUST_API_BASE_URL);
    String submitPath = System.getenv(JsonKey.EXHAUST_API_SUBMIT_ENDPOINT);
    String listPath = System.getenv(JsonKey.EXHAUST_API_LIST_ENDPOINT);
    if (StringUtils.isBlank(baseUrl))
      baseUrl = PropertiesCache.getInstance().getProperty(JsonKey.EXHAUST_API_BASE_URL);
    if (StringUtils.isBlank(submitPath))
      submitPath = PropertiesCache.getInstance().getProperty(JsonKey.EXHAUST_API_SUBMIT_ENDPOINT);
    if (StringUtils.isBlank(listPath))
      listPath = PropertiesCache.getInstance().getProperty(JsonKey.EXHAUST_API_LIST_ENDPOINT);
    exhaustAPISubmitURL = baseUrl + submitPath;
    exhaustAPIListURL = baseUrl + listPath;
  }

  private static Map<String, String> getUpdatedHeaders(Map<String, String> headers) {
    if (headers == null) {
      headers = new HashMap<>();
    }
    headers.put(
        HttpHeaders.AUTHORIZATION, JsonKey.BEARER + System.getenv(JsonKey.SUNBIRD_AUTHORIZATION));
    headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    headers.put("Connection", "Keep-Alive");
    return headers;
  }

  public static Response submitJobRequest( RequestContext requestContext,
      String queryRequestBody,
      ExecutionContextExecutor ec) {
    Unirest.clearDefaultHeaders();
    Response responseObj = null;

    try {

      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
      HttpResponse<String> apiResponse =
              Unirest.post(exhaustAPISubmitURL).headers(getUpdatedHeaders(null)).body(queryRequestBody).asString();
      if (null != apiResponse) {
        responseObj = mapper.readValue(apiResponse.getBody(), Response.class);
        if (responseObj.getResponseCode().getResponseCode() == ResponseCode.OK.getResponseCode()) {
          logger.info(requestContext, "Exhaust API submit report call success");
        } else {
          logger.info(requestContext, "Exhaust API submit report call failed : "+responseObj.getResponseCode()
                  +" : "+responseObj.getParams().getErr() +" : "+responseObj.getParams().getErrmsg());
        }
      }
    } catch (JsonMappingException e) {
      logger.error(requestContext, "Exhaust API submit report call failed : JsonMappingException : " + e.getMessage(), e);
      ProjectCommonException.throwServerErrorException(
              ResponseCode.customServerError, e.getMessage());
    } catch (UnirestException e) {
      logger.error(requestContext, "Exhaust API submit report call failed : UnirestException : " + e.getMessage(), e);
      ProjectCommonException.throwServerErrorException(
              ResponseCode.customServerError, e.getMessage());
    } catch (JsonProcessingException e) {
      logger.error(requestContext, "Exhaust API submit report call failed : JsonProcessingException : " + e.getMessage(), e);
      ProjectCommonException.throwServerErrorException(
              ResponseCode.customServerError, e.getMessage());
    } catch (Exception e) {
      logger.error(requestContext, "Exhaust API submit report call failed : " + e.getMessage(), e);
      ProjectCommonException.throwServerErrorException(
              ResponseCode.customServerError, e.getMessage());
    }
    return responseObj;
  }
  public static Response listJobRequest( RequestContext requestContext,
                                                              String queryParam,
                                                              ExecutionContextExecutor ec) {
    Unirest.clearDefaultHeaders();
    Response responseObj = null;
    try {
      HttpResponse<String> apiResponse =
              Unirest.get(exhaustAPIListURL+queryParam).headers(getUpdatedHeaders(null)).asString();
      if (null != apiResponse) {
        responseObj = mapper.readValue(apiResponse.getBody(), Response.class);
        if (responseObj.getResponseCode().getResponseCode() == ResponseCode.OK.getResponseCode()) {
          logger.info(requestContext, "Exhaust API report list call success");
        } else {
          logger.info(requestContext, "Exhaust API report list call failed" + responseObj.getResponseCode().getResponseCode() + responseObj.getResponseCode().getErrorMessage());
        }
      }
    } catch (JsonMappingException e) {
      logger.error(requestContext, "Exhaust API report list call failed : JsonMappingException : " + e.getMessage(), e);
      ProjectCommonException.throwServerErrorException(
              ResponseCode.customServerError, e.getMessage());
    } catch (UnirestException e) {
      logger.error(requestContext, "Exhaust API report list call failed : UnirestException : " + e.getMessage(), e);
      ProjectCommonException.throwServerErrorException(
              ResponseCode.customServerError, e.getMessage());
    } catch (JsonProcessingException e) {
      logger.error(requestContext, "Exhaust API report list call failed : JsonProcessingException : " + e.getMessage(), e);
      ProjectCommonException.throwServerErrorException(
              ResponseCode.customServerError, e.getMessage());
    } catch (Exception e) {
      logger.error(requestContext, "Exhaust API report list call failed : " + e.getMessage(), e);
      ProjectCommonException.throwServerErrorException(
              ResponseCode.customServerError, e.getMessage());
    }
    return responseObj;
  }
}