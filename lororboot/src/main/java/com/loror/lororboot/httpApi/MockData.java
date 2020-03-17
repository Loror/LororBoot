package com.loror.lororboot.httpApi;

import android.content.Context;
import android.content.res.AssetManager;

import com.loror.lororUtil.asynctask.AsyncUtil;
import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;
import com.loror.lororboot.annotation.MocKType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class MockData {

    private Charset charset;
    private ApiRequest apiRequest;
    private OnRequestListener onRequestListener;
    private String url;
    private HttpClient client;
    private RequestParams params;
    private Responce responce;

    private static Context context;

    protected interface OnResult {
        void result(String data, Responce responce);
    }

    /**
     * 初始化
     */
    public static void init(Context context) {
        MockData.context = context.getApplicationContext();
    }

    protected MockData(ApiRequest apiRequest, OnRequestListener onRequestListener, Charset charset) {
        this.apiRequest = apiRequest;
        this.onRequestListener = onRequestListener;
        this.charset = charset;
    }

    /**
     * 获取结果，异步
     */
    public void getResult(final OnResult onResult) {
        AsyncUtil.excute(new AsyncUtil.Excute<String>() {
            @Override
            public String doBack() {
                return getResult(false);
            }

            @Override
            public void result(String result) {
                ApiResult apiResult = null;
                if (onRequestListener != null) {
                    apiResult = new ApiResult();
                    apiResult.url = url;
                    apiResult.params = params;
                    apiResult.responce = responce;
                    apiResult.typeInfo = null;
                    apiResult.type = 0;
                    apiResult.request = apiRequest;
                    onRequestListener.onRequestEnd(client, apiResult);
                }
                if (apiResult != null && apiResult.accept) {
                    //已被处理拦截
                } else {
                    onResult.result(result, responce);
                }
            }
        });
    }

    /**
     * 获取结果
     */
    public String getResult() {
        return getResult(true);
    }

    /**
     * 获取Responce
     */
    public Responce getResponce() {
        return responce;
    }

    /**
     * 获取结果
     */
    private String getResult(boolean callBack) {
        String result = null;
        if (apiRequest.mockType == MocKType.ASSERT) {
            result = readAssetsFile(apiRequest.mockData);
        } else if (apiRequest.mockType == MocKType.NET) {
            if (!apiRequest.mockData.startsWith("http:") && !apiRequest.mockData.startsWith("https:")) {
                apiRequest.setUrl(apiRequest.mockData);
            }
            result = readNet(apiRequest.getUrl(), callBack);
        } else if (apiRequest.mockType == MocKType.DATA) {
            result = apiRequest.mockData;
        } else {
            if (apiRequest.mockData.trim().startsWith("{") || apiRequest.mockData.trim().startsWith("[")) {
                result = apiRequest.mockData;
            } else if (apiRequest.mockData.startsWith("http") || apiRequest.mockData.startsWith("https:")) {
                result = readNet(apiRequest.mockData, callBack);
            } else {
                result = readAssetsFile(apiRequest.mockData);
            }
        }
        return result;
    }

    /**
     * 读取网路
     */
    private String readNet(String url, boolean callBack) {
        this.url = url;
        String result = null;
        client = new HttpClient();
        client.setTimeOut(10000);
        params = apiRequest.getParams();
        if (apiRequest.isKeepStream()) {
            client.setKeepStream(true);
        }
        apiRequest.client = client;
        client.setProgressListener(apiRequest.progressListener);
        if (onRequestListener != null) {
            onRequestListener.onRequestBegin(client, apiRequest);
        }
        Responce responce = null;
        if (apiRequest.getType() == 1) {
            responce = client.get(url, params);
        } else if (apiRequest.getType() == 2) {
            responce = client.post(url, params);
        } else if (apiRequest.getType() == 3) {
            responce = client.delete(url, params);
        } else if (apiRequest.getType() == 4) {
            responce = client.put(url, params);
        }
        this.responce = responce;
        if (responce != null && !apiRequest.isKeepStream()) {
            result = responce.toString(charset);
        }
        if (callBack) {
            if (onRequestListener != null) {
                ApiResult apiResult = new ApiResult();
                apiResult.url = url;
                apiResult.params = params;
                apiResult.responce = responce;
                apiResult.typeInfo = null;
                apiResult.type = 0;
                apiResult.request = apiRequest;
                onRequestListener.onRequestEnd(client, apiResult);
            }
        }
        return result;
    }

    /**
     * 读取assets文件
     */
    private String readAssetsFile(String fileName) {
        AssetManager manager = context.getResources().getAssets();
        try {
            InputStream inputStream = manager.open(fileName);
            InputStreamReader isr = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String length;
            while ((length = br.readLine()) != null) {
                sb.append(length).append("\n");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            //关流
            br.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
