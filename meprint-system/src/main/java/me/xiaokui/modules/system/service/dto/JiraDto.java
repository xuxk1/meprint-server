package me.xiaokui.modules.system.service.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName: Jira
 * @Description: 读取配置文件中jira的相关参数
 * @Author xuxk
 * @Date 2021-03-10 10:03
 * @Memo 备注信息
 **/
@Getter
@Setter
@Component
@ConfigurationProperties(prefix="jira")
public class JiraDto {

    private String JIRA_URL;
    private String ALL_PROJECT_URI;
    private String PROJECT_URI;
    private String BUG_ALL_URI;
    private String REPAIRED_URI;
    private String COMPLETED_URI;
    private String ONLINE_URI;
    private String UPCOMING_URI;
    private String DEV_URI;
    private String ISSUETABLE_URI;

}
