package me.xiaokui.modules.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.xiaokui.constants.SystemConstant;
import me.xiaokui.constants.XmindConstant;
import me.xiaokui.modules.mapper.TestCaseMapper;
import me.xiaokui.modules.system.domain.request.cases.FileImportReq;
import me.xiaokui.modules.system.domain.response.cases.ExportXmindResp;
import me.xiaokui.modules.system.service.FileService;
import me.xiaokui.modules.util.FileUtil;
import me.xiaokui.modules.util.TreeUtil;
import me.xiaokui.modules.util.enums.StatusCode;
import me.xiaokui.modules.util.exception.CaseServerException;
import me.xiaokui.modules.mapper.TestCaseMapper;
import me.xiaokui.modules.util.enums.StatusCode;
import me.xiaokui.modules.persistent.TestCase;
import me.xiaokui.modules.util.exception.CaseServerException;
import me.xiaokui.modules.system.domain.request.cases.CaseCreateReq;
import me.xiaokui.modules.system.domain.request.cases.FileImportReq;
import me.xiaokui.modules.system.domain.response.cases.ExportXmindResp;
import me.xiaokui.modules.system.service.CaseService;
import me.xiaokui.modules.system.service.FileService;
import me.xiaokui.modules.util.FileUtil;
import me.xiaokui.modules.util.TreeUtil;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.xiaokui.constants.SystemConstant.POINT;
import static me.xiaokui.constants.XmindConstant.*;

/**
 * 文件上传与导出实现类
 *
 * @author didi
 * @date 2020/10/22
 */
@Service
public class FileServiceImpl implements FileService {

    private CaseService caseService;

    private TestCaseMapper caseMapper;

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importXmindFile(FileImportReq req) throws Exception {
        String fileName = req.getFile().getOriginalFilename();

        if (!StringUtils.isEmpty(fileName)) {
            // 得到上传文件的扩展名
            String suffix = fileName.substring(fileName.lastIndexOf(SystemConstant.POINT) + 1).toLowerCase();
            if (!suffix.equals(XmindConstant.XMIND_SUFFIX) && !suffix.equals(XmindConstant.ZIP_SUFFIX)) {
                throw new CaseServerException("上传的文件格式不正确", StatusCode.FILE_FORMAT_ERROR);
            }

            // 把文件放到本地
            File file = new File("");
            String filePath = "";
            try {
                filePath = file.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String desPath = filePath + XmindConstant.TEMP_FOLDER;

            File pathFile = new File(desPath);
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }
            desPath = desPath + fileName;
            File dest = new File(desPath);
            Long time = System.currentTimeMillis();
            String desc = filePath + XmindConstant.TEMP_FOLDER + fileName.split("\\.")[0] + "_" + time.toString() + "/";

            // 开始转换
            req.getFile().transferTo(dest);
            if (!FileUtil.decompressZip(desPath, desc)) {
                throw new CaseServerException("解析失败", StatusCode.FILE_IMPORT_ERROR);
            }

            // 导入用例
            File jsonFile = new File((desc + XmindConstant.CONTENT_JSON).replace("/", File.separator));
            CaseCreateReq caseCreateReq = jsonFile.exists() ? buildCaseByJson(req, desc) : buildCaseByXml(req, desc);
            return caseService.insertOrDuplicateCase(caseCreateReq);
        }
        throw new CaseServerException("传入的文件名非法", StatusCode.FILE_IMPORT_ERROR);
    }

    @Override
    public ExportXmindResp exportXmindFile(Long id, String userAgent) throws Exception {

        ExportXmindResp resp = new ExportXmindResp();

        //将用例内容写内容入xml文件
        Map<String,String> pathMap= createFile(id);

        //压缩文件夹成xmind文件
        String filePath = pathMap.get("exportPath") + ".xmind";
        FileUtil.compressZip(pathMap.get("exportPath") ,filePath);
        // 输出
        ByteArrayOutputStream byteArrayOutputStream = outPutFile(filePath);
        resp.setFileName(pathMap.get("exportFileName"));
        resp.setData(byteArrayOutputStream.toByteArray());

        return resp;
    }

    private ByteArrayOutputStream outPutFile(String filePath){

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        try{
            InputStream fis = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            int r = 0;
            while ((r = fis.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, r);
            }
            fis.close();
            bufferedOutputStream.flush();
            IOUtils.closeQuietly(bufferedOutputStream);
            IOUtils.closeQuietly(byteArrayOutputStream);
        }
        catch (Exception e){
            e.printStackTrace();
            IOUtils.closeQuietly(bufferedOutputStream);
            IOUtils.closeQuietly(byteArrayOutputStream);
            throw new CaseServerException("导出失败", StatusCode.FILE_EXPORT_ERROR);
        }
        return byteArrayOutputStream;
    }

    private void writeMetaXml(String path)
    {
        // 1、创建document对象
        Document document = DocumentHelper.createDocument();
        // 2、创建根节点root
        document.addElement("meta").addAttribute("xmlns", XmindConstant.XMIND_META_XMLNS).addAttribute("version","2.0");
        path = path + "/meta.xml";
        writeXml(path,document);
    }

    private void writeManifestXml(String path){
        // 1、创建document对象
        Document document = DocumentHelper.createDocument();
        // 2、创建根节点root
        Element root = document.addElement("manifest").addAttribute("xmlns", XmindConstant.XMIND_MAINFEST_XMLNS);
        // 3、生成子节点及子节点内容
        root.addElement("file-entry")
                .addAttribute("full-path","content.xml")
                .addAttribute("media-type","text/xml");
        root.addElement("file-entry")
                .addAttribute("full-path","content.xml")
                .addAttribute("media-type","text/xml");
        root.addElement("file-entry")
                .addAttribute("full-path","content.xml")
                .addAttribute("media-type","text/xml");
        root.addElement("file-entry")
                .addAttribute("full-path","content.xml")
                .addAttribute("media-type","text/xml");

        String targetPath = path + "/META-INF";

        File targetFolder = new File(targetPath);
        if(!targetFolder.exists())
             targetFolder.mkdirs();
        path = targetPath + "/manifest.xml";

        writeXml(path,document);
    }

    private void writeXml(String path, Document document)
    {
        OutputFormat format = OutputFormat.createPrettyPrint();
        // 设置编码格式
        format.setEncoding("UTF-8");
        File xmlFile = new File(path);
        try {
            XMLWriter writer = new XMLWriter(new FileOutputStream(xmlFile), format);
            // 设置是否转义，默认使用转义字符
            writer.setEscapeText(false);
            writer.write(document);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CaseServerException("导入失败，写文件失败：" + e.getMessage(), StatusCode.FILE_IMPORT_ERROR);
        }
    }

    //根据用例生成相应的文件
    private Map<String,String> createFile(Long id)
    {
        //createContentXml
        TestCase testCase = caseMapper.selectOne(id);
        if (testCase == null || StringUtils.isEmpty(testCase.getCaseContent())) {
            throw new CaseServerException("用例不存在或者content为空", StatusCode.FILE_EXPORT_ERROR);
        }

        String path = creteFolder(testCase);
        //写入content.xml内容
        writeContentXml(testCase,path);
        //写Meta文件
        writeMetaXml(path);
        //写MainFest文件
        writeManifestXml(path);

        Map<String,String> pathMap = new HashMap<>();
        pathMap.put("exportPath",path);
        pathMap.put("exportFileName",testCase.getTitle() + ".xmind");
        return pathMap;
    }

    //拼接xml内容
    private void writeContentXml(TestCase testCase,String path){
        // 1、创建document对象
        Document document = DocumentHelper.createDocument();
        // 2、创建根节点root
        Element root = document.addElement("xmap-content");
        // 3、生成子节点及子节点内容
        Element sheet = root.addElement("sheet")
                .addAttribute("id", XmindConstant.ZEN_ROOT_VERSION)
                .addAttribute("modified-by", XmindConstant.XMIND_MODIFIED_VERSION)
                .addAttribute("theme", XmindConstant.XMIND_THEME_VERSION)
                .addAttribute("timestamp", XmindConstant.XMIND_CREATED_VERSION);

        JSONObject rootObj = JSON.parseObject(testCase.getCaseContent()).getJSONObject(XmindConstant.ROOT);
        Element topic = sheet.addElement("topic")
                .addAttribute("id",rootObj.getJSONObject(XmindConstant.DATA).getString("id"))
                .addAttribute("modified-by","didi")
                .addAttribute("timestamp",rootObj.getJSONObject(XmindConstant.DATA).getString("created"));

        Element title = topic.addElement("title");
        String text = rootObj.getJSONObject(XmindConstant.DATA).getString("text");
        text = text.replace("<","&lt;");
        text = text.replace(">","&gt;");
        title.setText(text);
        TreeUtil.exportDataToXml(rootObj.getJSONArray("children"), topic);
        String targetPath = path  + "/content.xml";
        //写入xml
        writeXml(targetPath,document);
    }

    //创建要写入的文件夹
    private String creteFolder(TestCase testCase){
        String filePath = "";
        try{
            filePath = new File("").getCanonicalPath();
        }catch (Exception e){
            e.printStackTrace();
        }
        String folderName = testCase.getTitle().replace(" ","")+ "_" + System.currentTimeMillis();
        String desPath = filePath + XmindConstant.TEMP_FOLDER_EXPORT + folderName;
        File pathFile = new File(desPath);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        return  desPath;
    }

    private CaseCreateReq buildCaseByJson(FileImportReq request, String fileName) {
        // 开始读取文件中的json内容了
        String s = FileUtil.readJsonFile(fileName);
        JSONArray parseArray = JSONObject.parseArray(s);
        JSONObject getObj = parseArray.getJSONObject(0);
        JSONObject rootTopic = getObj.getJSONObject("rootTopic");

        // case-content设置
        JSONArray jsonArray = new JSONArray();
        TreeUtil.importDataByJson(jsonArray, rootTopic);

        return buildCaseCreateReq(request, jsonArray);
    }

    //xmind8从content文件读取用例内容
    public CaseCreateReq buildCaseByXml(FileImportReq request, String fileName) throws Exception {

        // 开始读取文件中的xml内容了
        JSONArray jsonArray = new JSONArray();
        String fileXml = "content.xml";
        fileName = (fileName + fileXml).replace("/", File.separator);
        File file = new File(fileName);
        if (file.exists()) {
            SAXReader reade = new SAXReader();
            Document doc = reade.read(file);
            Element rootElement = doc.getRootElement();
            List<Element> elementList = rootElement.elements();
            Element childElement = elementList.get(0);
            String eleName = childElement.getName();
            if (eleName.equalsIgnoreCase("sheet")) {
                jsonArray = TreeUtil.importDataByXml(childElement);
            }
            return buildCaseCreateReq(request, jsonArray);
        } else {
            throw new CaseServerException("导入失败，文件不存在", StatusCode.FILE_IMPORT_ERROR);
        }
    }


    private CaseCreateReq buildCaseCreateReq(FileImportReq request, JSONArray jsonArray) {
        // 构建content
        JSONObject caseObj = new JSONObject();
        caseObj.put(XmindConstant.ROOT, jsonArray.get(0));
        caseObj.put(XmindConstant.TEMPLATE, XmindConstant.TEMPLATE_RIGHT);
        caseObj.put(XmindConstant.THEME, XmindConstant.THEME_DEFAULT);
        caseObj.put(XmindConstant.VERSION, XmindConstant.VERSION_DEFAULT);
        caseObj.put(XmindConstant.BASE, XmindConstant.BASE_DEFAULT);

        CaseCreateReq testCase = new CaseCreateReq();
        testCase.setProductLineId(request.getProductLineId());
        testCase.setCreator(request.getCreator());
        if(request.getRequirementId().equals("undefined")) {
            testCase.setRequirementId("");
        }else {
            testCase.setRequirementId(request.getRequirementId());
        }
        testCase.setProductLineId(request.getProductLineId());
        testCase.setDescription(request.getDescription());
        testCase.setTitle(request.getTitle());
        testCase.setCaseType(0);
        testCase.setCaseContent(caseObj.toJSONString());
        testCase.setChannel(request.getChannel());
        testCase.setBizId(request.getBizId());
        return testCase;
    }
}
