package me.xiaokui.modules.quartz.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmind.core.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: XMindUtil
 * @Description: 解析XMind
 * @Author xuxk
 * @Date 2021-03-23 10:35
 * @Memo 备注信息
 **/
public class XMindUtil {

    private static IWorkbookBuilder builder = null;
    private static List<String> list = new ArrayList<>();
    private static Logger logger = LoggerFactory.getLogger(XMindUtil.class);
    /**
     * 获取工作簿
     * IWorkbook：表示整个思维导图
     * @param xmindPath:xmind文件路径
     */
    public static IWorkbook getIWorkbook(File xmindPath) throws IOException, CoreException {
        IWorkbook workbook = null;
        File file = new File(String.valueOf(xmindPath));
        FileInputStream fileInputStream=new FileInputStream(file);
        if (builder == null){
            // 初始化builder
            builder = Core.getWorkbookBuilder();
        }
        System.out.println("查看文件内容大小：" + file.length());
        workbook = builder.loadFromStream(fileInputStream,".");

        return workbook;
    }

    /**
     * 获取根节点
     * @param  iWorkbook:加载的思维导图
     */
    public static ITopic getRootTopic(IWorkbook iWorkbook){
        return iWorkbook.getPrimarySheet().getRootTopic();
    }

    /**
     * 获取从根目录到每一个叶子节点的的路径
     */
    public static List<String> getAllPath(ITopic rootTopic){
        return getAllPathIter(rootTopic.getTitleText(),rootTopic.getAllChildren());
    }

    public static List<String> getAllPathIter(String parentContext,List<ITopic> childrens){
        for(ITopic children:childrens){
            if(children.getAllChildren().size() == 0){
                list.add(parentContext+" —— "+children.getTitleText());
            }else {
                getAllPathIter(parentContext+" —— "+children.getTitleText(), children.getAllChildren());
            }
        }
        return list;
    }

    /**
     * 解析Xmind文件
     */
    public static List<String> xmindToList(File xmindPath) throws IOException, CoreException {
        return getAllPath(getRootTopic(getIWorkbook(xmindPath)));
    }

    public static void main(String[] agrs) throws IOException, CoreException {
        String rootPath = System.getProperty("user.dir");
        File xmindPath = new File(rootPath + "/UIAutoTest.xmind");
        logger.info("xmind文件路径：" + xmindPath);

        List<String> lists = xmindToList(xmindPath);

        for(String list:lists){
            System.out.println(list);
        }
    }

}

