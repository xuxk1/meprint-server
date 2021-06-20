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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
    private Logger log = LoggerFactory.getLogger(DashboardController.class);

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
//        JSONObject jsonOb = JiraUtil.getIssueCount(upcomingUrl);
        JSONObject jsonO = JiraUtil.getIssueCount(devUrl);
        log.info("获取jira接口中数据---获取当前项目数量" + projectName);
        log.info("获取jira接口中数据---设计师版待修复bug数量" + jsonObjct.get("issueCount") + "\n" + jsonObjct.get("filterTitle"));
        log.info("获取jira接口中数据---设计师版已修复bug数量" + jsonOcom.get("issueCount") + "\n" + jsonOcom.get("filterTitle"));
//        log.info("获取jira接口中数据---线上bug数量" + jsonObje.get("issueCount") + "\n" + jsonObje.get("filterTitle"));
//        log.info("获取jira接口中数据---个人待办任务数量" + jsonOb.get("issueCount") + "\n" + jsonOb.get("filterTitle"));
//        log.info("获取jira接口中数据---开发任务" + jsonO.get("issueCount") + "\n" + jsonO.get("filterTitle"));
        jsonObt.put("project",projectName);
        jsonObt.put("repaired",jsonObjct);
        jsonObt.put("personal",jsonObj);
        jsonObt.put("completed",jsonOcom);
        jsonObt.put("online",jsonObje);
//        jsonObt.put("online",jsonObje);
//        jsonObt.put("upcoming",jsonOb);
        jsonObt.put("product",jsonO);
        return jsonObt;
    }

}

