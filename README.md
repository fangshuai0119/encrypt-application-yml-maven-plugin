# 说明

## 一、工程描述

> 注：本项目使用基础为spring boot 项目

因为最近需要做配置文件的加密，使用`jasypt `的方式，密码之类的信息在配置文件还是存在，且比较容易发现，故需将整个配置文件进行加密

- 代码说明
  - 代码只是对配置文件进行了加密，并且将密码藏于加密文件中，此处我只写了藏到头部，可以使用策略模式对密码的位置进行不同策略的设置(TODO)

## 二、使用方式

1. 安装Plugin 

```shell
mvn clean install
```

2. 在项目的`pom.xml`中进行配置

```xml
<build>
	...
    <plugins>
    	...
        <plugin>
                <groupId>com.fs</groupId>
                <artifactId>encrypt-application-yml-maven-plugin</artifactId>
                <version>1.0.0</version>
                <configuration>
                    <!-- 需要加密的配置文件 --> 
		    <applicationYmlFile>${project.build.outputDirectory}/config/application-${profileActive}.yml</applicationYmlFile>
                    <!-- 可以指定密码，也可不指定，会随机生成 -->
                    <password>1234567812345678</password>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
    </plugins>
</build>
```

3. 在使用插件的项目中，添加对加密配置文件的配置

```java
public class EncryptYamlPropertySourceLoader implements PropertySourceLoader {

    @Override
    public String[] getFileExtensions() {
        return new String[] { "enc" };
    }

    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
        byte[] configBytes = IoUtil.readBytes(resource.getInputStream());
        AES aes = SecureUtil.aes(ArrayUtil.sub(configBytes, 0, 16));
        ByteArrayResource byteArrayResource = new ByteArrayResource(aes.decrypt(Base64.decode(ArrayUtil.sub(configBytes, 16, configBytes.length))));
        resource = byteArrayResource;

        List<Map<String, Object>> loaded = new OriginTrackedYamlLoader(resource).load();
        if (loaded.isEmpty()) {
            return Collections.emptyList();
        }
        List<PropertySource<?>> propertySources = new ArrayList<>(loaded.size());
        for (int i = 0; i < loaded.size(); i++) {
            String documentNumber = (loaded.size() != 1) ? " (document #" + i + ")" : "";
            propertySources.add(new OriginTrackedMapPropertySource(name + documentNumber,
                    Collections.unmodifiableMap(loaded.get(i)), true));
        }
        return propertySources;
    }

}
```

4. 在spring.factories 中声明配置文件的解析

```properties
org.springframework.boot.env.PropertySourceLoader=\
  ${packageName}.EncryptYamlPropertySourceLoader
```

