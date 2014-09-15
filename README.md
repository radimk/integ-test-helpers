### Support for integration tests

#### Multi test runner

`AbstractMultiTestRunner` is a JUnit test runner that allows to run the same test several times
during one test run using different setup.
Gradle project uses this for example to run a test against several version of some external tool.

#### Test directory provider

A JUnit rule creating a directory used in test execution.
It takes care of directory naming, creatins and cleanup is test passes (directory is kept in case of failure to allow investigation).
