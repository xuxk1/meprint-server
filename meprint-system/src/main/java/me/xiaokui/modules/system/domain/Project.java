package me.xiaokui.modules.system.domain;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import me.xiaokui.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * @ClassName: SysProjectEntity
 * @Description:
 * @Author xuxk
 * @Date 2021-04-10 23:30
 * @Memo 备注信息
 **/
@Entity
@Getter
@Setter
@Table(name = "sys_project")
public class Project extends me.xiaokui.base.BaseEntity implements Serializable {

    @Id
    @Column(name = "project_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JSONField(serialize = false)
    @ManyToMany(mappedBy = "depts")
    @ApiModelProperty(value = "角色")
    private Set<Role> roles;

    @Basic
    @Column(name = "pid")
    @ApiModelProperty(value = "上级项目")
    private Long pid;

    @Basic
    @Column(name = "sub_count")
    @ApiModelProperty(value = "子节点数目", hidden = true)
    private Integer subCount;

    @Basic
    @NotBlank
    @Column(name = "name")
    @ApiModelProperty(value = "项目名称")
    private String name;

    @Basic
    @Column(name = "project_sort")
    @ApiModelProperty(value = "排序")
    private Integer projectSort;

    @NotNull
    @Column(name = "enabled")
    @ApiModelProperty(value = "是否启用")
    private Boolean enabled;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Project project = (Project) o;
        return Objects.equals(id, project.id) &&
                Objects.equals(name, project.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}

