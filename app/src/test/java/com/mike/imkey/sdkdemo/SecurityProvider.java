package com.mike.imkey.sdkdemo;

import java.security.Security;

/**
 * JCE 提供者工具
 * @author 
 *2013-5-15
 */
public class SecurityProvider {
private static String PROVIDER_NAME="BC";
private static boolean ISDECIDE=false;
public static String getProvierName(){
	if (ISDECIDE && Security.getProvider(PROVIDER_NAME)!=null){
		return PROVIDER_NAME;
	}else{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		ISDECIDE=true;
		return PROVIDER_NAME;
	}
 
}
}
