# brXM Checker Tool Lambda

This is a simple AWS Lambda-wrapper for the Hippo Checker tool that is invoked regularly to make
sure the version history and data storage objects (binaries) are properly removed from the JCR.

You can find the documentation for the Checker tool here: 

* https://xmdocumentation.bloomreach.com/library/administration/maintenance/checker-tool.html

What this lambda does:

* run the Checker's version history cleanup
* run the Checker's binary cleanup
* include the Checker tool JAR as a dependency
* allow the user to configure repository information through environment variables

The `repository.xml` is generated based on the values in the environment variables:

* `MYSQL_HOST`: where the mysql host lives
* `MYSQL_USERNAME`: the username to connect with
* `MYSQL_PASSWORD`: the password to connect with
* `MYSQL_DATABASE`: the name of the database
* `MYSQL_PORT`: the port at which the server runs (optional, default: 3306)

The `storage` location of the repository points at `/tmp`. Use the ephemeral storage settings in the lambda
to provide it sufficient space for the repository you're working with. 

### Build runnable JAR

Build the lambda by executing:

    $ mvn package

Your target folder will now have two files:

    brxm-janitor-lambda-1.1-SNAPSHOT-jar-with-dependencies.jar
    brxm-janitor-lambda-1.1-SNAPSHOT.jar

Upload the `with-dependencies` version to your Lambda.

### Lambda settings

Some advice for creating the lambda:

* Make sure your lambda has a long max run time 15 minutes
* Your lambda should be in a VPC, so it has a security group that can chat to your MySQL RDS
* Your lambda should have sufficient ephemeral storage to house the repository information
* Your lambda should have sufficient memory: 2gb? 
* Your lambda should have a some outputs so that you can be notified whether it was successful or not. 
* Your lambda should call this handler: `nz.xinsolutions.janitor.LambdaHippoChecker::run`

### Combine with Repository Logs lambda

As outlined in the Bloomreach documentation, not only should you periodically run the Checker tool
to clean up binaries and orphaned version history nodes, you should also remove old JCR cluster logs from the
database.

To do this another Lambda repo exists:

* https://github.com/XIN-Solutions/brxm-repositorylogs-maintenance-lambda

### When not to use as a Lambda

If your repository is sufficiently large so that the operations cannot be completed within 15 minutes
(the longest runtime a Lambda allows) you should probably not use this, or use it on a normal computer the first time
and run it daily at night. However, I imagine for most smaller repositories this will be a useful tool. 

### Run as docker

Use the `xinsolutions/brxm-repository-janitor` and start it with the following environment variables to get a weekly 
repository cleanup every sunday midnight in whatever the timezone of your server is set to.

* MYSQL_HOST
* MYSQL_USERNAME
* MYSQL_PASSWORD
* MYSQL_DATABASE

Update `cronlauncher` and build your own version of the Docker image to change the time at which the cron should run.