Redis-launcher is an Java library for controlling a [redis server](http://redis.io/) programmatically.
It is released under the [MIT license](http://www.opensource.org/licenses/mit-license.php).

Installation
============

Installing from Maven Central
-----------------------------
    <dependency>
    	<groupId>uk.co.datumedge</groupId>
    	<artifactId>redis-launcher</artifactId>
    	<version>0.3</version>
    </dependency>


Installing from source
-----------------------------
    mvn -Dredislauncher.command=/path/to/redis-server install

Getting started
===============

The simplest way to start a redis server is to set the `redislauncher.command` system property as the path to the redis-server executable, then use the following code:

```java
RedisServer redisServer = LocalRedisServer.newInstance();
try {
   redisServer.start();
   ...
} finally {
   redisServer.stop();
}
```
    
Resources
=========
 * [redis-launcher website](http://datumedge.co.uk/redis-launcher/)
 * [API documentation](http://datumedge.co.uk/redis-launcher/apidocs/index.html)