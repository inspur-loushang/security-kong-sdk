package sdk.security.service.impl;

import java.util.HashMap;
import java.util.Map;

import sdk.security.service.IAuthenticationProvider;
import sdk.security.util.KongUtil;

/**
 * 认证
 *
 */
public class AuthenticationProviderImpl implements IAuthenticationProvider {

	/**
	 * 获取当前登录用户标识
	 * 
	 * @return String userId[用户ID]
	 */
	public String getLoginUserId() {
		try {
			return KongUtil.getUserInfo().get("preferred_username");
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取当前登录用户Token
	 * 
	 * @return String，token信息
	 */
	public String getToken() {
		try {
			return KongUtil.getAccessTokenString();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取当前登录用户userId-realmname
	 * 
	 * @return
	 */
	public String getKrbPrincipalName() {
		try {
			return KongUtil.getUserInfo().get("preferred_username") + "-" + KongUtil.getRealm();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取当前登录用户的详细信息
	 * 
	 * @return Map，key分别为： userId[用户标识]，userName[用户名]，email[邮箱地址]，...
	 */
	public Map<String, String> getLoginUserInfo() {
		try {
			Map<String, String> getMap = KongUtil.getUserInfo();
			Map<String, String> map = new HashMap<String, String>();
			map.put("userId", getMap.get("preferred_username"));
			map.put("userName", getMap.get("preferred_username"));
			map.put("email", getMap.get("email"));
			return map;
		} catch (Exception e) {
			return null;
		}
	}
}
