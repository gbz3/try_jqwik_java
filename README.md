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