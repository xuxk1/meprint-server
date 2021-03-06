package me.xiaokui.modules.system.domain.request.cases;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.xiaokui.modules.system.domain.request.ParamValidate;
import me.xiaokui.modules.system.domain.request.ParamValidate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用例 导入文件并上传
 *
 * @author hcy
 * @date 2020/10/22
 */
@Data
@AllArgsConstructor
public class FileImportReq implements ParamValidate {

    private MultipartFile file;

    private String creator;

    private Long productLineId;

    private String title;

    private String description;

    private Long projectId;

    /**
     * 默认为0，如果有其他需求，可以变为其他数字
     */

    private Integer caseType;

    /**
     * 默认为1，如果有其他需求，可以变为其他数字
     */
    private Integer channel;

    private String requirementId;

    private String bizId;

    @Override
    public void validate() {
        if (caseType == null || caseType < 0) {
            throw new IllegalArgumentException("用例类型为空或者非法");
        }
        if (channel == null || channel < 0) {
            throw new IllegalArgumentException("渠道为空或者非法");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请先上传文件");
        }
        if (productLineId == null || productLineId < 0L) {
            throw new IllegalArgumentException("业务线id为空或者非法");
        }
        if (StringUtils.isEmpty(title)) {
            throw new IllegalArgumentException("标题为空");
        }
        if (StringUtils.isEmpty(creator)) {
            throw new IllegalArgumentException("创建人为空");
        }
        if (StringUtils.isEmpty(bizId)) {
            // 特殊点，没有传bizId就给-1，而不是报错
            throw new IllegalArgumentException("文件夹id");
        }
        if (projectId == null || projectId < 0L) {
            throw new IllegalArgumentException("项目id为空或者非法");
        }
    }
}
