Thread Pool with Job Affinity
=============================

Problem
-------
* All the jobs given to thread pool with the same job id will be executed by the same thread.  
* Multiple jobs submitted for a given job id should be executed in the order of submission.

Idea
----
* For a given Job id, create a Single Threaded executor, Maintain a map from Job id to single Threaded executor
* Since this is a fixed sized thread pool and to accommodate all JobIds, map key is generated by doing operation (hashcode of JobId) % poolSize  
* Shutdown operation shuts down all single threaded executors and waits for termination
* Submission of a job operation checks if single threaded executor is already created. If not, create it and submit given Job to it.

Tests
-----
Two test classes have been added to test  
1. Job submissions by single thread.  
2. Job submission by multiple threads.


How to Compile and Run
----------------------
Maven supported is added. Execute **`mvn clean install`** to compile, to run tests, to build jar and to install jar to local maven repo