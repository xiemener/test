package com.mike.imkey.sdkdemo;


import com.google.gson.Gson;

import org.junit.Test;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testGet() {
        String res = null;
        try {
            res = conn("http://172.21.75.33:8080/imkey/seSecureCheck");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(res);
        assertEquals(false, res == null);
    }

    @Test
    public void testPost() {
        try {
            String res = post("http://172.21.75.33:8080/imkey/seSecureCheck");
            System.out.println(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPostJson() {
        try {
            Gson gson = new Gson();
            HashMap<String, String> map = new HashMap();
            map.put("stepKey", "01");
            map.put("seid", "00000000000000000000000000000000");
            map.put("sn", "123456");
            String json = gson.toJson(map);
            String res = postJson("http://172.21.75.33:8080/imkey/seSecureCheck", json);
            System.out.println(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    OkHttpClient client = new OkHttpClient();

    String conn(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    String post(String url) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("platform", "android")
                .add("name", "bug")
                .add("subject", "XXXXXXXXXXXXXXX")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    String postJson(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Test
    public void registerSe() {
        ActiveSE activeSE = new ActiveSE();
        activeSE.setStepKey("01");
        activeSE.setStatusWord("");
        List<String> list = new ArrayList<>();
        activeSE.setCardRetDataList(list);
        activeSE.setSeid("18000001010000000016");
        activeSE.setSn("123456");
        regse(activeSE,true);
    }

    public void regse(ActiveSE activeSE,boolean isfirst) {
        Gson gson = new Gson();
        System.out.println("request>>>>>" + activeSE.toString());
        String json = gson.toJson(activeSE);
        String res = null;
        try {
            res = postJson("http://172.21.75.33:8080/imkey/seActivate", json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("response<<<< " + res);
        CommonResponse commonResponse = gson.fromJson(res, CommonResponse.class);
        if (commonResponse == null) {
            System.out.println("json error");
            return;
        }
        if (commonResponse.get_ReturnCode().equals("000000")) {
            CommonResponse.ReturnDataBean returnDataBean = commonResponse.get_ReturnData();
            if (returnDataBean != null) {
                if ("end".equals(returnDataBean.nextStepKey)) {
                    System.out.println("end...");
                } else {
                    ActiveSE reActive = new ActiveSE();
                    reActive.setStepKey(returnDataBean.getNextStepKey());
                    List<String> apdus = new ArrayList<>();
                    if (returnDataBean.getApduList() != null) {
                        for (int i = 0; i < returnDataBean.getApduList().size(); i++) {
                            String apdu = null;
                            if(isfirst){
                                try {
                                    apdu = generate8050RetData(returnDataBean.getApduList().get(i));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else {
                                apdu = "9000";
                            }
                            apdus.add(apdu);
                        }
                        reActive.setCardRetDataList(apdus);
                    }
                    String status = "9000";//正常情况是卡返回的最后4位
                    reActive.setStatusWord(status);
                    reActive.setSeid("18000001010000000016");
                    reActive.setSn("123456");
                    regse(reActive,false);
                }
            }
        } else {
            System.out.println(commonResponse.get_ReturnCode() + " " + commonResponse.get_ReturnMsg());
        }
    }

    class CommonRequest {
        String stepKey;
        String statusWord;
        List<String> cardRetDataList;

        public String getStepKey() {
            return stepKey;
        }

        public void setStepKey(String stepKey) {
            this.stepKey = stepKey;
        }

        public String getStatusWord() {
            return statusWord;
        }

        public void setStatusWord(String statusWord) {
            this.statusWord = statusWord;
        }

        public List<String> getCardRetDataList() {
            return cardRetDataList;
        }

        public void setCardRetDataList(List<String> cardRetDataList) {
            this.cardRetDataList = cardRetDataList;
        }

        @Override
        public String toString() {
            return "CommonRequest{" +
                    "stepKey='" + stepKey + '\'' +
                    ", statusWord='" + statusWord + '\'' +
                    ", cardRetDataList=" + cardRetDataList +
                    '}';
        }
    }

    class CommonResponse {

        /**
         * _ReturnCode : 000000
         * _ReturnMsg : 操作成功
         * _ReturnData : {"seid":"18000001010000000016","nextStepKey":"02","apduList":["00A4040008A00000000300000000","80500000085D2EAC17C525B4EE00"]}
         */

        private String _ReturnCode;
        private String _ReturnMsg;
        private ReturnDataBean _ReturnData;

        public String get_ReturnCode() {
            return _ReturnCode;
        }

        public void set_ReturnCode(String _ReturnCode) {
            this._ReturnCode = _ReturnCode;
        }

        public String get_ReturnMsg() {
            return _ReturnMsg;
        }

        public void set_ReturnMsg(String _ReturnMsg) {
            this._ReturnMsg = _ReturnMsg;
        }

        public ReturnDataBean get_ReturnData() {
            return _ReturnData;
        }

        public void set_ReturnData(ReturnDataBean _ReturnData) {
            this._ReturnData = _ReturnData;
        }

        public class ReturnDataBean {
            /**
             * seid : 18000001010000000016
             * nextStepKey : 02
             * apduList : ["00A4040008A00000000300000000","80500000085D2EAC17C525B4EE00"]
             */

            private String seid;
            private String nextStepKey;
            private List<String> apduList;

            public String getSeid() {
                return seid;
            }

            public void setSeid(String seid) {
                this.seid = seid;
            }

            public String getNextStepKey() {
                return nextStepKey;
            }

            public void setNextStepKey(String nextStepKey) {
                this.nextStepKey = nextStepKey;
            }

            public List<String> getApduList() {
                return apduList;
            }

            public void setApduList(List<String> apduList) {
                this.apduList = apduList;
            }

            @Override
            public String toString() {
                return "ReturnDataBean{" +
                        "seid='" + seid + '\'' +
                        ", nextStepKey='" + nextStepKey + '\'' +
                        ", apduList=" + apduList +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "CommonResponse{" +
                    "_ReturnCode='" + _ReturnCode + '\'' +
                    ", _ReturnMsg='" + _ReturnMsg + '\'' +
                    ", _ReturnData=" + _ReturnData +
                    '}';
        }
    }

    class ActiveSE extends CommonRequest {
        private String seid;
        private String sn;

        public String getSeid() {
            return seid;
        }

        public void setSeid(String seid) {
            this.seid = seid;
        }

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "ActiveSE{" +
                    "stepKey='" + stepKey + '\'' +
                    ", statusWord='" + statusWord + '\'' +
                    ", cardRetDataList=" + cardRetDataList +
                    ", seid='" + seid + '\'' +
                    ", sn='" + sn + '\'' +
                    '}';
        }
    }

    /**
     * 模拟卡片生成8050的反馈数据
     *
     * @param data
     * @param
     * @return
     * @throws Exception
     */
    public static String generate8050RetData(String data) throws Exception {
        if (data == null) {
            throw new Exception("请求的8050指令不能为空");
        }
        String hostRandom = data.substring(10, 26);
//        System.out.println("平台随机数-->" + hostRandom);
        String cardRandom = "B1B79602B1CB";//卡片随机数
        String sourceStr = hostRandom + "0000" + cardRandom + "8000000000000000";
        //初始向量VIC
        byte[] civ = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        //生成会话ENC密钥

        String key = "404142434445464748494a4b4c4d4e4f";//机构二级密钥
        byte[] sencKey = generateSesssionKey(key);

        //对拼接数据进行加密操作
        byte[] cardEncData = encryptTripleDesCbc(sencKey, ConverterUtil.hexString2ByteArray(sourceStr), civ);
        byte[] cardEncData2 = new byte[8];
        System.arraycopy(cardEncData, cardEncData.length - 8, cardEncData2, 0, 8);
//        System.out.println("卡片加密数据-->" + ConverterUtil.byteArray2HexString(cardEncData) + "\n截取后的卡片加密数据" + ConverterUtil.byteArray2HexString(cardEncData2));
        String retData = "00001286001982957394FF020000" + cardRandom + ConverterUtil.byteArray2HexString(cardEncData2);
//        System.out.println("最终返回的数据-->" + retData);

        return retData;
    }

    /**
     * 生成会话密钥
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] generateSesssionKey(String key) throws Exception {
        //初始向量VIC
        byte[] civ = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        //卡片计数器
        byte[] sequenceCounter = new byte[]{(byte) 0x00, (byte) 0x00};
        // 机构二级密钥
        String key1 = "404142434445464748494a4b4c4d4e4f";
//				String key2 = "D5BCA7452AABFBD67F6EF1027C028AC2";
//				String key3 = "9B1F02B61F2F9BA149A87A75576B7C15";
        //分散因子-PAMID
//				String pamid = "04400100000112000001039801000011";

//                //对分散因子进行分散
//                byte[] macKey = disperKeyBySoftAlgorithm(ConverterUtil.hexString2ByteArray(key1), pamid);
//                byte[] encKey = disperKeyBySoftAlgorithm(ConverterUtil.hexString2ByteArray(key1), pamid);
//                byte[] dekKey = disperKeyBySoftAlgorithm(ConverterUtil.hexString2ByteArray(key1), pamid);

//        System.out.println("分散后的密钥-->" + ConverterUtil.byteArray2HexString(macKey));;
//        System.out.println("分散后的密钥-->" + ConverterUtil.byteArray2HexString(encKey));;
//        System.out.println("分散后的密钥-->" + ConverterUtil.byteArray2HexString(dekKey));;


        //生成会话密钥
        byte[] scmacDerData = new byte[16];
        byte[] srmacDerData = new byte[16];
        byte[] sencDerData = new byte[16];
        byte[] sdekDerData = new byte[16];

        scmacDerData[0] = (byte) 0x01;
        scmacDerData[1] = (byte) 0x01;
        System.arraycopy(sequenceCounter, 0, scmacDerData, 2, 2);

        srmacDerData[0] = (byte) 0x01;
        srmacDerData[1] = (byte) 0x02;
        System.arraycopy(sequenceCounter, 0, srmacDerData, 2, 2);

        sencDerData[0] = (byte) 0x01;
        sencDerData[1] = (byte) 0x82;
        System.arraycopy(sequenceCounter, 0, sencDerData, 2, 2);

        sdekDerData[0] = (byte) 0x01;
        sdekDerData[1] = (byte) 0x81;
        System.arraycopy(sequenceCounter, 0, sdekDerData, 2, 2);
        //生成会话密钥
//				byte[] scmacKey = CryptographyUtil.des3_CBC_encryption(macKey, scmacDerData, civ);
//				byte[] sencKey = CryptographyUtil.des3_CBC_encryption(encKey, sencDerData, civ);
//				byte[] sdekKey = CryptographyUtil.des3_CBC_encryption(dekKey, sdekDerData, civ);

        byte[] scmacKey = encryptTripleDesCbc(ConverterUtil.hexString2ByteArray(key), scmacDerData, civ);
        byte[] sencKey = encryptTripleDesCbc(ConverterUtil.hexString2ByteArray(key), sencDerData, civ);
        byte[] sdekKey = encryptTripleDesCbc(ConverterUtil.hexString2ByteArray(key), sdekDerData, civ);

//        System.out.println("scmackey-->" + ConverterUtil.byteArray2HexString(scmacKey));
//        System.out.println("sencKey-->" + ConverterUtil.byteArray2HexString(sencKey));
//        System.out.println("sdekKey-->" + ConverterUtil.byteArray2HexString(sdekKey));
        return sencKey;
    }

    /**
     * 3DES CBC加密
     *
     * @param
     * @param
     * @return
     * @throws Exception
     */
    public static byte[] encryptTripleDesCbc(byte[] des3key, byte[] data, byte[] civ) throws Exception {
        des3key = transferKeyFrom16To24(des3key);
        IvParameterSpec IvSpec = new IvParameterSpec(civ);
        DESedeKeySpec key = new DESedeKeySpec(des3key);
        SecretKeyFactory kf = SecretKeyFactory.getInstance("DESede", SecurityProvider.getProvierName());
        Key sk = kf.generateSecret(key);

        Cipher c = Cipher.getInstance("DESede" + "/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, sk, IvSpec);

        return c.doFinal(data);
    }

    private static byte[] transferKeyFrom16To24(byte[] key) {

        if (key.length != 16) {
            return key;
        }

        byte[] tempKey = new byte[24];
        System.arraycopy(key, 0, tempKey, 0, key.length);
        System.arraycopy(key, 0, tempKey, key.length, 8);

        return tempKey;
    }


    public class AppDownloadRequest extends CommonRequest {
        //SE唯一标识
        public String seid;
        public String instanceAid;//实例AID

        public String getSeid() {
            return seid;
        }

        public void setSeid(String seid) {
            this.seid = seid;
        }

        public String getInstanceAid() {
            return instanceAid;
        }

        public void setInstanceAid(String instanceAid) {
            this.instanceAid = instanceAid;
        }

    }

    public class AppDownloadResponse extends CommonResponse {
        //SE唯一标识
        public String seid;
        public String instanceAid;//实例AID

        public String getSeid() {
            return seid;
        }

        public void setSeid(String seid) {
            this.seid = seid;
        }

        public String getInstanceAid() {
            return instanceAid;
        }

        public void setInstanceAid(String instanceAid) {
            this.instanceAid = instanceAid;
        }
    }

    @Test
    public void appDownloadTest(){
        AppDownloadRequest appDownloadRequest = new AppDownloadRequest();
        appDownloadRequest.setSeid("18000001010000000016");
        appDownloadRequest.setInstanceAid("123456");
        appDownloadRequest.setStepKey("01");
        Gson gson = new Gson();
        String json = gson.toJson(appDownloadRequest);
        String res = null;
        try {
            res = postJson("http://172.21.75.33:8080/imkey/appDownload",json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("response:" + res);
        AppDownloadResponse response = gson.fromJson(res,AppDownloadResponse.class);
    }
}