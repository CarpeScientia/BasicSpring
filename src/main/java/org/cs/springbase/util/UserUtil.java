package org.cs.springbase.util;

public final class UserUtil {

	private UserUtil() {
		//don't call
	}
	public static String underscoresToSpaces(String userName) {
		if(userName == null) {
			return null;
		}
		return userName.replace('_', ' ');
	}
}
