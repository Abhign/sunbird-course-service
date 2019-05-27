package util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.HeaderParam;
import play.mvc.Http;
import play.mvc.Http.Request;

/**
 * Request interceptor responsible to authenticated HTTP requests
 *
 * @author Amit Kumar
 */
public class RequestInterceptor {

  protected static List<String> restrictedUriList = null;
  private static ConcurrentHashMap<String, Short> apiHeaderIgnoreMap = new ConcurrentHashMap<>();

  private RequestInterceptor() {}

  static {
    restrictedUriList = new ArrayList<>();
    restrictedUriList.add("/v1/content/state/update");

    // ---------------------------
    short var = 1;
    apiHeaderIgnoreMap.put("/service/health", var);
    apiHeaderIgnoreMap.put("/v1/page/assemble", var);
    apiHeaderIgnoreMap.put("/health", var);
    apiHeaderIgnoreMap.put("/v1/notification/email", var);
    apiHeaderIgnoreMap.put("/v1/data/sync", var);
    apiHeaderIgnoreMap.put("/v1/content/link", var);
    apiHeaderIgnoreMap.put("/v1/content/unlink", var);
    apiHeaderIgnoreMap.put("/v1/content/link/search", var);
    apiHeaderIgnoreMap.put("/v1/course/batch/search", var);
    apiHeaderIgnoreMap.put("/v1/cache/clear", var);
  }

  /**
   * Authenticates given HTTP request context
   *
   * @param ctx HTTP play request context
   * @return User or Client ID for authenticated request. For unauthenticated requests, UNAUTHORIZED
   *     is returned
   */
  public static String verifyRequestData(Http.Context ctx) {
    Request request = ctx.request();
    String clientId = JsonKey.UNAUTHORIZED;
    String accessToken = request.getHeader(HeaderParam.X_Authenticated_User_Token.getName());
    String authClientToken = request.getHeader(HeaderParam.X_Authenticated_Client_Token.getName());
    String authClientId = request.getHeader(HeaderParam.X_Authenticated_Client_Id.getName());
    if (!isRequestInExcludeList(request.path()) && !isRequestPrivate(request.path())) {
      if (StringUtils.isNotBlank(accessToken)) {
        clientId = AuthenticationHelper.verifyUserAccesToken(accessToken);
      } else if (StringUtils.isNotBlank(authClientToken) && StringUtils.isNotBlank(authClientId)) {
        clientId = AuthenticationHelper.verifyClientAccessToken(authClientId, authClientToken);
        if (!JsonKey.UNAUTHORIZED.equals(clientId)) {
          ctx.flash().put(JsonKey.AUTH_WITH_MASTER_KEY, Boolean.toString(true));
        }
      }
      return clientId;
    } else {
      if (StringUtils.isNotBlank(accessToken)) {
        String clientAccessTokenId = null;
        try {
          clientAccessTokenId = AuthenticationHelper.verifyUserAccesToken(accessToken);
          if (JsonKey.UNAUTHORIZED.equalsIgnoreCase(clientAccessTokenId)) {
            clientAccessTokenId = null;
          }
        } catch (Exception ex) {
          ProjectLogger.log(ex.getMessage(), ex);
          clientAccessTokenId = null;
        }
        return StringUtils.isNotBlank(clientAccessTokenId)
            ? clientAccessTokenId
            : JsonKey.ANONYMOUS;
      }
      return JsonKey.ANONYMOUS;
    }
  }

  private static boolean isRequestPrivate(String path) {
    return path.contains(JsonKey.PRIVATE);
  }

  /**
   * Checks if request URL is in excluded (i.e. public) URL list or not
   *
   * @param requestUrl Request URL
   * @return True if URL is in excluded (public) URLs. Otherwise, returns false
   */
  public static boolean isRequestInExcludeList(String requestUrl) {
    boolean resp = false;
    if (!StringUtils.isBlank(requestUrl)) {
      if (apiHeaderIgnoreMap.containsKey(requestUrl)) {
        resp = true;
      } else {
        String[] splitPath = requestUrl.split("[/]");
        String urlWithoutPathParam = removeLastValue(splitPath);
        if (apiHeaderIgnoreMap.containsKey(urlWithoutPathParam)) {
          resp = true;
        }
      }
    }
    return resp;
  }

  /**
   * Returns URL without path and query parameters.
   *
   * @param splitPath URL path split on slash (i.e. /)
   * @return URL without path and query parameters
   */
  private static String removeLastValue(String splitPath[]) {

    StringBuilder builder = new StringBuilder();
    if (splitPath != null && splitPath.length > 0) {
      for (int i = 1; i < splitPath.length - 1; i++) {
        builder.append("/" + splitPath[i]);
      }
    }
    return builder.toString();
  }
}
