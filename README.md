# AIM Coding Assessment

The following is code for a set of tests to verify the results
of a [SKU API](https://coderbyte.com/question/api-test-automation-ieh70kchsk)
with the intention of exercising its basic functionality as
a web service by invoking positive and negative test cases.

## Setup and Running

Running the code requires installations of 
[Java JDK 1.7](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html), 
[Maven](https://maven.apache.org/download.cgi?Preferred=ftp://ftp.osuosl.org/pub/apache/),
and of course
[git](https://git-scm.com/downloads)
to clone the project.
To run all the tests, please execute the following 
from the command line at the base of the cloned project:

```
mvn clean test
```

## Test Design Choices

The tests take advantage of the 
[Maven Build Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
and use 
[Rest Assured](https://rest-assured.io/) 
as the REST Api test framework.  The test framework is 
[TestNg](https://testng.org/doc/index.html) in Java.
The tests look for seemingly correct results and consistency
between API calls.  JSON objects are handled using
[FasterXML/jackson](https://github.com/FasterXML/jackson).

In addition, the tests take into consideration that the
stored data accessible through the API's may be needed
by other users, and seeks to avoid modifying
or deleting the data already there.  The tests involving
POST and DELETE, therefore, only operate on data created 
internally within the tests.  
For this reason, the positive test cases are primarily
end-to-end rather than tests that target one API at a time.  

In a real world scenario, it would be
better to consult with teammates while making such a choice.
A collaborative effort could set up a test backend where
data is disposable, or provide hooks to pre-seed the data,
or other provisions to avoid problematic issues.

## Test Scenarios

The following test scenarios are covered

1. GET-All, followed by GET for a particular sku, 
checking for consistency between the two
2. validate the following end-to-end scenario
   1. POST
   2. GET to check that the POST populated the data
   3. POST to update the values in step 1
   4. GET to check the update
   5. DELETE the sku from step 1
   6. GET to check that the deleted sku no longer shows up
3. negative tests for invalid POST bodies where 
certain fields are empty
4. negative test to GET an unknown sku
5. negative test to DELETE an unknown sku

It is not guaranteed that all the tests pass, but
the tests attempt to convey reasons for failures when
the APIs behave in unexpected ways.