package me.xiaokui.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.Assert.*;

public class FileUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtilTest.class);

    @Test
    public void testToFile() {
        long retval = FileUtil.toFile(new MockMultipartFile("foo", (byte[]) null)).getTotalSpace();
        LOGGER.info("retval======" + retval);
        assertEquals(98522210304L, retval);
    }

    @Test
    public void testGetExtensionName() {
        Assert.assertEquals("foo", FileUtil.getExtensionName("foo"));
        Assert.assertEquals("exe", FileUtil.getExtensionName("bar.exe"));
    }

    @Test
    public void testGetFileNameNoEx() {
        Assert.assertEquals("foo", FileUtil.getFileNameNoEx("foo"));
        Assert.assertEquals("bar", FileUtil.getFileNameNoEx("bar.txt"));
    }

    @Test
    public void testGetSize() {
        Assert.assertEquals("1000B   ", FileUtil.getSize(1000));
        Assert.assertEquals("1.00KB   ", FileUtil.getSize(1024));
        Assert.assertEquals("1.00MB   ", FileUtil.getSize(1048576));
        Assert.assertEquals("1.00GB   ", FileUtil.getSize(1073741824));
    }
}
