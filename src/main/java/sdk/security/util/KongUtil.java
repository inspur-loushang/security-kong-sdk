package sdk.security.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.gson.Gson;

public class KongUtil {

	private static final String logout = "/protocol/openid-connect/logout?redirect_uri=";

	@SuppressWarnings("unchecked")
	public static Map<String, String> getUserInfo() {
		String userinfo = getUserInfoFromHeader();
		Map<String, String> map = new HashMap<String, String>();
		Gson gson = new Gson();
		map = gson.fromJson(userinfo, Map.class);
		return map;
	}

	public static String getAccessTokenString() {
		return getAccessTokenFromHeader();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getAccessToken() {
		Map<String, Object> map = new HashMap<String, Object>();
		String accessTokenStr = getAccessTokenFromHeader();
		if (!"".equals(accessTokenStr) && accessTokenStr != null) {
			try {
				String actJson = new String(Base64.decodeBase64(accessTokenStr.split(".")[1]), "UTF-8");
				Gson gson = new Gson();
				map = gson.fromJson(actJson, Map.class);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return map;
	}

	public static String getRealm() {
		Map<String, Object> accessToken = getAccessToken();
		String iss = (String) accessToken.get("iss");
		if (!"".equals(iss) && iss != null) {
			String realm = iss.substring(iss.lastIndexOf("/") + 1, iss.length());
			return realm;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static List<String> getRealmAccessRoles() {
		List<String> roles = new ArrayList<String>();
		try {
			Map<String, Object> accessToken = getAccessToken();
			Map<String, Object> realmAccess = (Map<String, Object>) accessToken.get("realm_access");
			roles = (List<String>) realmAccess.get("roles");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return roles;
	}

	public static String getSecurityContextUrl() {
		String authServerUrl = "";
		Map<String, Object> accessToken = getAccessToken();
		String iss = (String) accessToken.get("iss");
		try {
			URL url = new URL(iss);
			authServerUrl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + "/auth";

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return authServerUrl;
	}

	/**
	 * 回退URL
	 * 
	 * @param backUrl
	 * @return
	 */
	public static String getLogoutUrl(String backUrl) {
		String logoutUrl = "";
		if (!"".equals(backUrl) && backUrl != null) {
			logoutUrl = (String) getAccessToken().get("iss") + logout;
			if (backUrl.startsWith("http") || backUrl.startsWith("https")) {
				logoutUrl += backUrl;
			} else {
				try {
					HttpServletRequest request = HttpServletThreadLocal.getRequest();
					String contexPath = request.getContextPath();
					URL url = new URL(request.getRequestURL().toString());
					String protocol = url.getProtocol();
					String host = url.getHost();
					int port = url.getPort();
					logoutUrl += protocol + "://" + host;
					if (port > 0) {
						logoutUrl += ":" + port;
					}
					if (backUrl.startsWith("/")) {
						logoutUrl += contexPath + backUrl;
					} else {
						logoutUrl += contexPath + "/" + backUrl;
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

		}
		return logoutUrl;
	}

	private static String getAccessTokenFromHeader() {

		return getRequest().getHeader("X-AccessToken");
	}

	private static String getUserInfoFromHeader() {
		return getRequest().getHeader("X-Userinfo");
	}

	private static HttpServletRequest getRequest() {
		HttpServletRequest request = null;
		try {
			request = HttpServletThreadLocal.getRequest();
			if ("".equals(request) || request == null) {
				request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return request;
	}
}
