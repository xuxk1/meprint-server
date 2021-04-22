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
import me.xiaokui.annotation.DataPermission;
import me.xiaokui.annotation.Query;

import java.sql.Timestamp;
import java.util.List;

/**
* @author Zheng Jie
* @date 2019-03-25
*/
@Data
@DataPermission(fieldName = "id")
public class DeptQueryCriteria{

    @me.xiaokui.annotation.Query(type = me.xiaokui.annotation.Query.Type.INNER_LIKE)
    private String name;

    @me.xiaokui.annotation.Query
    private Boolean enabled;

    @me.xiaokui.annotation.Query
    private Long pid;

    @me.xiaokui.annotation.Query(type = me.xiaokui.annotation.Query.Type.IS_NULL, propName = "pid")
    private Boolean pidIsNull;

    @me.xiaokui.annotation.Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;
}
