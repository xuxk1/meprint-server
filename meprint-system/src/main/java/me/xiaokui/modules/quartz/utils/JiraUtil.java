package me.xiaokui.modules.quartz.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static com.alipay.api.AlipayConstants.CHARSET_UTF8;

/**
 * @ClassName: JiraUtil
 * @Description: 获取jira中的数据
 * @Author xuxk
 * @Date 2021-03-10 09:58
 * @Memo 备注信息
 **/
public class JiraUtil {

    private static String JIRA_URL = "http://jira.diy8.com";
    private static String PROJECT_URI = "/rest/api/2/project";
    private static String BUG_URI = "/rest/gadget/1.0/statistics?filterId=10602&statType=assignees";
    private static String REPAIRED_URI = "/rest/gadget/1.0/statistics?filterId=10700&statType=assignees";
    private static String COMPLETED_URI = "/rest/gadget/1.0/statistics?jql=project%3D10200&statType=assignees";
    private static String ONLINE_URI = "/rest/gadget/1.0/statistics?jql=project%3D10100&statType=assignees";
    private static String UPCOMING_URI = "/rest/gadget/1.0/statistics?filterId=10602&statType=assignees";
    private static String DEV_URI = "/rest/gadget/1.0/statistics?filterId=10601&statType=customfield_10600";
    private static String ISSUETABLE_URI = "/rest/issueNav/1/issueTable";
    private static String Cookies = "seraph.rememberme.cookie=11943%3A9d0056b4d9252bc9183a14da75213edc7a28fbcd; JSESSIONID=E3A6F4CE0D21707C0EA6C29C6E090A81; atlassian.xsrf.token=BMXM-6QDP-0P5F-LZ0B_5773457c7d602be1ae061ebb432db997b6eb70d3_lin";
    private static String DEFAULT_ENCODING = "UTF-8";
    private static String DEFUALT_CONTENT_TYPE = "application/json";
    private static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36";
    private static int DEFAULT_SOCKET_TIMEOUT = 10000;

    private static Logger logger = LoggerFactory.getLogger(JiraUtil.class);

    public JiraUtil(){

    }

    private static String sendGetRequest(String url){
        String responseContent = null;
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader(HTTP.USER_AGENT,USER_AGENT);
            httpGet.setHeader(HTTP.CONTENT_TYPE,DEFUALT_CONTENT_TYPE);
            httpGet.setHeader(HTTP.CONTENT_ENCODING,DEFAULT_ENCODING);
            httpGet.setHeader("Cookie",Cookies);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                    .setConnectTimeout(
                            DEFAULT_SOCKET_TIMEOUT).build();
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            int rspCode = response.getStatusLine().getStatusCode();
            if (rspCode != 200){
                logger.error("Response code is" + rspCode + " instead of 200. Url: " + httpGet.getURI().toString());
            }
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                responseContent = EntityUtils.toString(entity);
                close(entity);
            }
            response.close();

        }catch (ClientProtocolException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            logger.warn(e.getMessage());
            e.printStackTrace();
        }

        return responseContent;
    }

    public static String sendPostRequest(String url, HashMap<String, String> map) throws Exception {
        String result = "";
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(550000).setConnectTimeout(550000)
                .setConnectionRequestTimeout(550000).setStaleConnectionCheckEnabled(true).build();
        client = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
        URIBuilder uriBuilder = new URIBuilder(url);

        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader("X-Atlassian-Token", "no-check");
        httpPost.setHeader("Host", "jira.diy8.com");
        httpPost.setHeader("Cookie", Cookies);
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue());
            params.add(pair);
        }

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        try {
            response = client.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, CHARSET_UTF8);
                }
            }
        } catch (ClientProtocolException e) {
            throw new RuntimeException("创建连接失败" + e);
        } catch (IOException e) {
            throw new RuntimeException("创建连接失败" + e);
        }

        return result;
    }

    private static void close(HttpEntity entity){
        if(entity==null){
            return;
        }
        if(entity.isStreaming()){
            try {
                final InputStream inputStream = entity.getContent();
                if(inputStream !=null){
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SneakyThrows
    public static JSONObject getAllProjectName(String project_url){
        logger.info("url======" + project_url);
        String response = sendGetRequest(project_url);
        JSONArray jsonArray = new JSONArray(response);
        int len = jsonArray.length();
        ArrayList<String> res = new ArrayList<>();
        ArrayList<String> res2 = new ArrayList<>();
        for (int i = 0;i < len;i++) {
            res.add((String) jsonArray.getJSONObject(i).get("name"));
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("projectCount",len);
        jsonObject.put("projectName",res);
        return jsonObject;
    }

    public static JSONObject getIssueCount(String bug_url){

        logger.info("bug_url======" + bug_url);
        String response = sendGetRequest(bug_url);
        JSONObject object= JSONObject.parseObject(response);
        String filterTitle = (String) object.get("filterTitle");
        String filterUrl = (String) object.get("filterUrl");
        String result = JSON.toJSONString(object.get("results"));
        JSONArray jsonArray = new JSONArray(result);
        ArrayList<Object> res = new ArrayList<>();
        ArrayList<Object> res2 = new ArrayList<>();
        ArrayList<Object> res3 = new ArrayList<>();
        for (int i = 0;i < jsonArray.length();i++) {
            res.add(jsonArray.getJSONObject(i).get("key"));
            res2.add(jsonArray.getJSONObject(i).get("value"));
            res3.add(jsonArray.getJSONObject(i).get("url"));
        }
        int issueCount = (int) object.get("issueCount");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("filterTitle",filterTitle);
        jsonObject.put("filterUrl",filterUrl);
        jsonObject.put("issueCount",issueCount);
        jsonObject.put("userNames",res);
        jsonObject.put("taskCount",res2);
        jsonObject.put("taskAddress",res3);
        return jsonObject;

    }

    public static void main(String[] args) throws Exception {
        String url = JIRA_URL + PROJECT_URI;
        String url_bug = JIRA_URL + BUG_URI;
        String urlRepaired = JIRA_URL + REPAIRED_URI;
        String urlCompleted = JIRA_URL + COMPLETED_URI;
        String urlOnline = JIRA_URL + ONLINE_URI;
        String url_uncoming = JIRA_URL + UPCOMING_URI;
        String urlDev = JIRA_URL + DEV_URI;
        String issueTable = JIRA_URL + ISSUETABLE_URI;
        String params = "{\"startIndex\": 0,\"jql\": \"created >= 2021-06-01 AND created <= 2021-06-30 ORDER BY created DESC\",\"layoutKey\": \"list-view\"}";
        logger.info("params======" + params);
        JSONObject projectName = getAllProjectName(url);
        logger.info("project====" + projectName);

        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
        client.getHttpConnectionManager().getParams().setSoTimeout(3000);

        logger.info("URL========" + issueTable);
        PostMethod post = new PostMethod(issueTable);
        String result = "";
        post.addRequestHeader("X-Atlassian-Token","no-check");
        post.addRequestHeader("Host","jira.diy8.com");
        post.addRequestHeader("Cookie",Cookies);
        post.addParameter("startIndex", String.valueOf(0));
        post.addParameter("jql","created >= 2021-06-01 AND created <= 2021-06-30 ORDER BY created DESC");
        post.addParameter("layoutKey","list-view");
        client.executeMethod(post);
        InputStream inputStream = post.getResponseBodyAsStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer stringBuffer = new StringBuffer();
        String str= "";
        while((str = br.readLine()) != null){
            stringBuffer.append(str);
        }
        logger.info(String.valueOf(stringBuffer));
    }
}

