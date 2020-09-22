package com.fs;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * 用于将application-*.yml 生成 application-*.enc
 * 对配置文件进行加密
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class EncryptApplicationYmlMojo extends AbstractMojo {

    /**
     * 加密密码
     */
    @Parameter(property = "password", defaultValue = "")
    private String password;

    /**
     * 需要进行加密的文件
     */
    @Parameter(property = "application-yml-file", required = true)
    private File applicationYmlFile;

    /**
     * 加密生成文件的后缀
     */
    @Parameter(property = "encrypt-file-suffix", defaultValue = ".enc")
    private String encryptFileSuffix;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (StrUtil.isEmpty(password)) {
            password = RandomUtil.randomString("abcdefghijklmnopqrstuvxwyzABCDEFGHIJKLMNOPQRSTUVXWYZ1234567890", 16);
        }
        AES aes = SecureUtil.aes(password.getBytes());
        String applicationEncFile = FileUtil.getParent(applicationYmlFile, 1).getAbsolutePath() + File.separator + FileUtil.getName(applicationYmlFile).replace(".yml", encryptFileSuffix);
        FileUtil.writeString(password, new File(applicationEncFile), CharsetUtil.UTF_8);
        FileUtil.appendString(Base64.encodeUrlSafe(aes.encrypt(FileUtil.getInputStream(applicationYmlFile))), new File(applicationEncFile), CharsetUtil.UTF_8);
        FileUtil.del(applicationYmlFile);
        this.getLog().debug("applicationEncFile -> " + applicationEncFile);
        this.getLog().debug("password -> " + password);
        this.getLog().debug("application yml file -> " + applicationYmlFile);
        this.getLog().debug("encrypt file suffix -> " + encryptFileSuffix);

    }
}
