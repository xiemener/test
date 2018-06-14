package com.mike.imkey.sdkdemo;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

//import com.justinmobile.core.converter.exception.ConverterException;

public class ConverterUtil {

	/**
	 * 将byte[]转换为Base64String
	 * 
	 * @param src
	 *            byte[]
	 * @return Base64String，英文字符均为大写
	 */
	public static String byteArray2Base64String(byte[] src) {
		return Base64.encodeBase64String(src);
	}

	/**
	 * 将Base64String转换为byte[]
	 * 
	 * @param src
	 *            Base64String
	 * @return byte[]
	 */
	public static byte[] base64String2ByteArray(String src) {
		return Base64.decodeBase64(src);
	}

	/**
	 * 将byte转换为hexString
	 * 
	 * @param src
	 *            byte
	 * @return hexString byte
	 */
	public static String byte2HexString(byte src) {
		return byteArray2HexString(new byte[] { src });
	}

	/**
	 * 将hexString转换为byte
	 *
	 * @param hex
	 *            hexString
	 * @return byte byte
	 */
	public static byte hexString2Byte(String hex) {
//		if (2 != hex.length()) {
//			throw new ConverterException("length of src must be 2, src is " + hex);
//		}

		return hexString2ByteArray(hex)[0];
	}

	/**
	 * 将byte[]转换为HexString
	 * 
	 * @param src
	 *            byte[]
	 * @return HexString，英文字符均为大写
	 */
	public static String byteArray2HexString(byte[] src) {
		return Hex.encodeHexString(src).toUpperCase();
	}

	/**
	 * 将hexString转换为byte[]<br/>
	 * 如果源数据为null或者“”，返回长度为0的byte数组
	 *
	 * @param
	 *
	 * @return hexString
	 */
	public static byte[] hexString2ByteArray(String data) {
		char[] dataCharArray = new char[data.length()];
		data.getChars(0, data.length(), dataCharArray, 0);
		try {
			return Hex.decodeHex(dataCharArray);
		} catch (DecoderException e) {
			return null;
		}
	}

	/**
	 * 将byte[]转换为int
	 * 
	 * @param src
	 *            byte[]
	 * @return
	 */
	public static int byteArray2Int(byte[] src) {
		String hexString = byteArray2HexString(src);
		return Integer.parseInt(hexString, 16);
	}

}
