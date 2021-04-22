package me.xiaokui.modules.mapper;

import me.xiaokui.modules.persistent.Biz;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 文件夹映射
 *
 * @author didi
 * @date 2020/9/9
 * @see Biz
 */
@Resource
@Repository
public interface BizMapper {

    /**
     * 插入文件夹
     *
     * @param biz 文件夹实体
     * @return bizId
     */
    int insert(Biz biz);

    /**
     * 通过主键修改文件夹
     *
     * @param biz 文件夹实体
     * @return bizId
     */
    int update(Biz biz);

    /**
     * 根据渠道和业务线获取文件夹
     *
     * @param productLineId 业务线id
     * @param channel 渠道
     * @return 文件夹实体
     */
    Biz selectOne(Long productLineId, Integer channel);

    /**
     * 根据渠道和业务线获取文件夹
     *
     * @param productLineId 业务线id
     * @param channel 渠道
     * @return 文件夹实体
     */
    List<Biz> selectAll(Long productLineId, Integer channel);

    /**
     * 更新文件夹树内容
     *
     * @param productLineId 业务线id
     * @param content 文件夹内容
     * @param channel 渠道
     */
    void updateContent(Long productLineId, String content, Integer channel);

}
