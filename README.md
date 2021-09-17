# AIM Coding Assessment

The following is code for a set of tests to verify the results
of a [SKU API](https://coderbyte.com/question/api-test-automation-ieh70kchsk).

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
as the REST Api test framework.

In addition, the tests take into consideration that the
stored data accessible through the API's may be needed
by other users, and seek to avoid modifying
or deleting the data already there.  The tests involving
POST and DELETE therefore only operate on data created by
the user.  For this reason, the test suite contains primarily
end-to-end scenarios rather than tests that target
one API alone.  In a real world scenario, it would be
better to consult with teammates while making such a choice.

The tests look for seemingly correct results and consistency
between API calls.

## Test Scenarios

The following test scenarios are covered

1. GET-All, followed by GET for a particular sku, 
checking for consistency between the two
2. validate the following end-to-end scenario
   1. POST
   2. GET to check that the POST arrived
   3. POST to update the values in step 1
   4. GET to check the update
   5. DELETE the sku from step 1
   6. GET to check that the sku no longer shows up
3. negative tests invalid POST body where certain fields
are empty
4. negative test to GET an unknown sku
5. negative test to DELETE an unknown sku