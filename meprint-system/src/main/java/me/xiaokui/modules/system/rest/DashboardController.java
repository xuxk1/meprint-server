package me.xiaokui.modules.system.rest;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.annotation.ResponseJSONP;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.xiaokui.modules.quartz.utils.JiraUtil;
import me.xiaokui.modules.system.service.dto.JiraDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


/**
 * @ClassName: DashboardController
 * @Description: 首页数据展示
 * @Author xuxk
 * @Date 2021-02-19 18:07
 * @Memo 备注信息
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：获取jira数据接口")
@RequestMapping("/api")
public class DashboardController {

    @Autowired
    private JiraDto jiraEntity;
    private Logger logger = LoggerFactory.getLogger(DashboardController.class);

    /**
     * 获取jira中的数据
     *
     * @param
     * @return
     */
    @ApiOperation("获取jira中的数据")
    @GetMapping("/dashboard")
    @ResponseJSONP
    public JSONObject getConfigVariable(){
        String projectUrl = jiraEntity.getJIRA_URL() + jiraEntity.getPROJECT_URI();
        String filterUrl = jiraEntity.getALL_PROJECT_URI();
        String bugAll = jiraEntity.getJIRA_URL() + jiraEntity.getBUG_ALL_URI();
        String repairedUrl = jiraEntity.getJIRA_URL() + jiraEntity.getREPAIRED_URI();
        String completedUrl = jiraEntity.getJIRA_URL() + jiraEntity.getCOMPLETED_URI();
        String onlineUrl = jiraEntity.getJIRA_URL() + jiraEntity.getONLINE_URI();
        String upcomingUrl = jiraEntity.getJIRA_URL() + jiraEntity.getUPCOMING_URI();
        String devUrl = jiraEntity.getJIRA_URL() + jiraEntity.getDEV_URI();
        System.out.println("获取jira配置信息" + projectUrl + "\n" + bugAll);
        JSONObject projectName = JiraUtil.getAllProjectName(projectUrl);
        projectName.put("filterUrl",filterUrl);
        JSONObject jsonObt = new JSONObject();
        JSONObject jsonObjct = JiraUtil.getIssueCount(repairedUrl);
        JSONObject jsonObj = JiraUtil.getIssueCount(bugAll);
        JSONObject jsonOcom = JiraUtil.getIssueCount(completedUrl);
        JSONObject jsonObje = JiraUtil.getIssueCount(onlineUrl);
        JSONObject jsonO = JiraUtil.getIssueCount(devUrl);
        logger.info("获取jira接口中数据---获取当前项目数量" + projectName);
        logger.info("获取jira接口中数据---设计师版待修复bug数量" + jsonObjct.get("issueCount") + "\n" + jsonObjct.get("filterTitle"));
        logger.info("获取jira接口中数据---设计师版已修复bug数量" + jsonOcom.get("issueCount") + "\n" + jsonOcom.get("filterTitle"));
        jsonObt.put("project",projectName);
        jsonObt.put("repaired",jsonObjct);
        jsonObt.put("personal",jsonObj);
        jsonObt.put("completed",jsonOcom);
        jsonObt.put("online",jsonObje);
        jsonObt.put("product",jsonO);
        return jsonObt;
    }

    /**
     * 根据筛选条件获取jira中的数据
     *
     * @param
     * @return
     */
    @ApiOperation("根据筛选条件获取jira中的数据")
    @PostMapping("/issueTable")
    @ResponseJSONP
    public String getConditionResult(@RequestParam() HashMap<String, String> jql) throws Exception {
        logger.info("访问issueTable接口=======" + jql);
        String issueTableUrl = jiraEntity.getJIRA_URL() + jiraEntity.getISSUETABLE_URI();
        String issueTableData = JiraUtil.sendPostRequest(issueTableUrl,jql);
        logger.info("issueTableData=====" + issueTableData);
        return issueTableData;
    }
}

