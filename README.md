# try_jqwik_java

## 環境構築

- [Maven in 5 Minuts](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)
- [Apache Maven 入門](https://zenn.dev/caunus/books/apache-maven-introduction)

```shell
$ mvn -v
Apache Maven 3.6.3
Maven home: /usr/share/maven
Java version: 17.0.13, vendor: Ubuntu, runtime: /usr/lib/jvm/java-17-openjdk-arm64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "5.15.153.1-microsoft-standard-wsl2", arch: "aarch64", family: "unix"
```

### プロジェクト作成

```shell
$ mvn archetype:generate \
-DgroupId=com.github.gbz3.try_jqwik_java \
-DartifactId=my-app \
-DarchetypeArtifactId=maven-archetype-quickstart \
-DarchetypeVersion=1.5 \
-DinteractiveMode=false
```

```shell
$ cd my-app && mvn test
```

### jqwik 有効化

- [User Guide](https://jqwik.net/docs/current/user-guide.html)
- [jqwik-starter-maven](https://github.com/jqwik-team/jqwik-samples/tree/main/jqwik-starter-maven)
- [Kotlin と jqwik で Property Based Testing](https://zenn.dev/msksgm/articles/20221007-kotlin-property-based-testing-with-jqwik)

```shell
## pom.xml に追加
<dependencies>
    ...
    <dependency>
        <groupId>net.jqwik</groupId>
        <artifactId>jqwik</artifactId>
        <version>1.9.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```
