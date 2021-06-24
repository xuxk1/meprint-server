package me.xiaokui.modules.quartz.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
    private static String Cookies = "JSESSIONID=046F54BAAE7658123CEA4BF50677A52D; seraph.rememberme.cookie=11900%3A57d03abc16e518d20ce8559ea105aed8e084abd8; atlassian.xsrf.token=BMXM-6QDP-0P5F-LZ0B_ab0881a790e6e8b876201a740463c9f7a202662a_lin";
    private static String DEFAULT_ENCODING = "UTF-8";
    private static String DEFUALT_CONTENT_TYPE = "application/json";
    private static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.182 Safari/537.36";
    private static int DEFAULT_SOCKET_TIMEOUT = 10000;

    private static Logger logger = LoggerFactory.getLogger(JiraUtil.class);

    public JiraUtil(){

    }

    private static String sendGetRequest(String url){
        String responseContent = null;
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader(HTTP.USER_AGENT,USER_AGENT);
            httpGet.setHeader(HTTP.CONTENT_ENCODING,DEFUALT_CONTENT_TYPE);
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
        logger.info("res=======" + res);
        logger.info("res=======" + res2);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("projectCount",len);
        jsonObject.put("projectName",res);
        return jsonObject;
    }

    public static JSONObject getIssueCount(String bug_url){

        logger.info("bug_url======" + bug_url);
        String response = sendGetRequest(bug_url);
        logger.info("response======" + response);
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
        logger.info("key======" + res + "\n" + res2 + "\n" + res3);
        int issueCount = (int) object.get("issueCount");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("filterTitle",filterTitle);
        jsonObject.put("filterUrl",filterUrl);
        jsonObject.put("issueCount",issueCount);
        jsonObject.put("userNames",res);
        jsonObject.put("taskCount",res2);
        jsonObject.put("taskAddress",res3);
        logger.info("jsonObject======" + jsonObject);
        return jsonObject;

    }

    public static void main(String[] args){
        String url = JIRA_URL + PROJECT_URI;
        String url_bug = JIRA_URL + BUG_URI;
        String urlRepaired = JIRA_URL + REPAIRED_URI;
        String urlCompleted = JIRA_URL + COMPLETED_URI;
        String urlOnline = JIRA_URL + ONLINE_URI;
        String url_uncoming = JIRA_URL + UPCOMING_URI;
        String urlDev = JIRA_URL + DEV_URI;
        JSONObject getProjectName = getAllProjectName(url);
        JSONObject issueCount2 = getIssueCount(url_bug);
//        JSONObject issueCount3 = getIssueCount(url3);
//        JSONObject issueCount4 = getIssueCount(url4);
        JSONObject issueCount5 = getIssueCount(urlOnline);
//        JSONObject issueCount6 = getIssueCount(url6);
        JSONObject issueCountDev = getIssueCount(urlDev);
        logger.info(String.valueOf(issueCountDev.get("issueCount")));
        logger.info(String.valueOf(issueCountDev.get("filterTitle")));
    }
}

