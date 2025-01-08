---
id: core-installation
title: Installation
sidebar_position: 1
---

## Installation: Add Rage4J-Core to Your Project

To start using Rage4J-Core, add the following Maven dependency to your `pom.xml` file:

``` xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

Once added, Maven will download and include the library in your project automatically.

---

## Debugging: Enable Detailed Logs for Metric Calculations

To enable detailed logs for metric calculations during testing, use the following Maven command:

```bash
mvn test -Dshow.metric.logs=true
```

---

Explore more about Rage4J:

1. [RAGE4j-Core](/docs/category/rage4j-core)
3. [RAGE4j-Assert](/docs/category/rage4j-assert)
4. [Contribution guide](/docs/contribution)