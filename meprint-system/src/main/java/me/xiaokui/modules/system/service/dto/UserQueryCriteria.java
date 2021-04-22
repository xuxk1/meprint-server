/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.xiaokui.modules.system.service.dto;

import lombok.Data;
import me.xiaokui.annotation.Query;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 */
@Data
public class UserQueryCriteria implements Serializable {

    @me.xiaokui.annotation.Query
    private Long id;

    @me.xiaokui.annotation.Query(propName = "id", type = me.xiaokui.annotation.Query.Type.IN, joinName = "dept")
    private Set<Long> deptIds = new HashSet<>();

    @me.xiaokui.annotation.Query(blurry = "email,username,nickName")
    private String blurry;

    @me.xiaokui.annotation.Query
    private Boolean enabled;

    private Long deptId;

    @me.xiaokui.annotation.Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;
}
