# GBK2UTF8


### Description

GBK2UTF8 is an encoding converter for Java/Android projects. (Usually GBK to UTF-8)

And the project is made by Eclipse.


### Usage

Run GBK2UTF8 from command line by passing the path of Java/Android projects that you want to convert its encoding to UTF-8.

**Examples of execution**
```
java -jar GBK2UTF8.jar E:\Android\MyApplication
```
or
```
java -jar GBK2UTF8.jar E:\Android\MyApplication.zip
```
or
```
java -jar GBK2UTF8.jar E:\Android\MainActivity.java
```

**Example of output**
```
=== GBK2UTF8 for Java/Android Projects ===
===          v1.0.2            @By_syk ===
CodeFileEncoder - START: "E:\Android\MyApplication"
...
CodeFileEncoder - CONVERTED: "E:\Android\MyApplication\app\src\main\java\com\by_syk\myapplication\MainActivity.java" -> "E:\Android\MyApplication_U\app\src\main\java\com\by_syk\myapplication\MainActivity.java"
...
CodeFileEncoder - COPIED: "E:\Android\MyApplication\app\src\main\assets\fonts\comic.ttf" -> "E:\Android\MyApplication_U\app\src\main\assets\fonts\comic.ttf"
...
CodeFileEncoder - DONE: "E:\Android\MyApplication_U"
```


### References

* [cpdetector](http://cpdetector.sourceforge.net/index.shtml "cpdetector")
* [zip4k](http://www.lingala.net/zip4j "zip4j")


### Download JAR

* Get it [here](GBK2UTF8_v1.0.2.jar "GBK2UTF8").


### Changelog

* View it [here](CHANGELOG.txt "Changelog").


### Contact author

* E-mail: [By_syk@163.com](mailto:By_syk@163.com "By_syk")


*Copyright &#169; 2016 By_syk. All rights reserved.*