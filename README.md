The basic data model consists of the following class:

Account, which has an account id and an amount.

Currently, no health checks or metrics are present, but one could consider the following endpoints to provide limity sanity checking.

`/api/ping -- returns "pong" and shows service is alive`

`/api/version -- version of service. 1.0.0 is synchronous and has potentially lower performance.`

`/api/postgres -- checks the version of deployed postgres and is useful in case postgres seems to have anomalous behavior.`

Here are the main REST API endpoints.

**HTTP GET**

`/accounts -- Lists all the accounts and details of each`

`/accounts/{id} -- Lists the details of account {id} (error code if account nonexistent -- maybe should be revisited)`

`/accounts/balance/{id} -- Returns the balance of account {id} (0 if account nonexistent)`
    

**HTTP POST**

`/api/accounts/balance/{id} amount=Integer -- Sets the balance of account {id} to (non-negative)amount with account creation if necessary`

`/api/accounts/addtobalance/{id} amount=Integer -- Adds amount, which may be negative, to balance of account {id}, result >= 0, returns number of modified rows`

`/api/accounts/transfer srcid=String dstid=String amount=Integer -- Transfers positive amount from account srcid to dstid if both srcid and dstid exist.`
    
 **HTTP POST v2** 
 
 In this case the "in progress" response is immediate and the database action takes place in a separate thread which runs to completion. 
 
 This approach should provide higher performance. The transaction along with success or failure can be logged to another database that should be accessible from a web browser.
 
 The actual account database should probably be sharded to increase possible parallelism, and it should be replicated to increase system persistence and accessibility.
 
 Even without the existence of a log database that indicates success or failure of the POST operation, success or failure indication can be found through the GET operations.
 
 Note that it might be worthwhile to couple the asynchronous interface with some sort of fault recovery system like Hystrix.
 
 **V2 Endpoints**
 
` /api/accounts/balance/v2/{id} amount=Integer -- Sets the balance of account {id} to (non-negative)amount with account creation if necessary`
 
 `/api/accounts/addtobalance/v2/{id} amount=Integer -- Adds amount, which may be negative, to balance of account {id}, result >= 0, returns number of modified rows`
 
 `/api/accounts/transfer/v2 srcid=String dstid=String amount=Integer -- ransfers positive amount from account srcid to dstid if both srcid and dstid exist.`
     
 **CLIENT API**
 
 The client api is found in the moneytransferclient project. I could probably redo this as a single maven project which had a maven parent directory moneytransfer and two maven modules/children named moneytransferserver and moneytransferclient. 
 
 For now I will not do the work to restructure the maven projects. 
 
 _classes_
  
     Account
     AccessClient
 
 _methods of AccessClient_
  
     static public String sendInterviewPing()
     static public String getInterviewVersion() 
     static public String getInterviewPostgresVersion()
     
     static public List<Account> getInterviewAccount()
     static public Account getInterviewAccount(String id)
     static public Integer getInterviewAccountBalance(String id)
     
     static public void setInterviewAccountBalance(String acct, int amount)
     static public void addInterviewAccountBalance(String acct, int amount)
     static public void makeTransfer(String src, String dst, int amount) 

**NOTES**

In a real service for moving money/financial instruments between accounts, we would probably use classes like Currency and BigDecimal.

https://docs.oracle.com/javase/8/docs/api/java/util/Currency.html

https://docs.oracle.com/javase/8/docs/api/java/math/BigDecimal.html

A generic financial/quant package that covers the universe of financial instruments does not seem to be available.

_Starting up and Terminating the Postgres Server_
    
`sudo docker run --name circle_postgres -p 5432:5432 -e POSTGRES_PASSWORD=circle -e POSTGRES_USER=circle -e POSTGRES_DB=circle postgres`
    
Note that the following command is used to free up the container name from the docker daemon.
    
`sudo docker rm /circle_postgres`

_Persisting the Postgress Database_

The service can clean, repair, migrate, vali. It might be more fun if the database were persistent independent of terminating and restarting the service. To do so would entail modifying the startup code, but more would need to be done.

 Note that the postgres database in the docker container is not persistent. This URL immediately following explains how to make the postgres database data persistent.
 
 https://www.andreagrandi.it/2015/02/21/how-to-create-a-docker-image-for-postgresql-and-persist-data/
 
 After I get everything to work, I may experiment with the persistence issues. In doing the quiz, it is good for the database to be reinitialized because exceptions can easily cause the database to be corrupted.
    

_Creating the Docker Server_

The first problem was the creation of a shaded executable jar that could read the configuration.yml file.

I had to add the following <plugin> xml to the <build> section of the pom.xml.

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>2.3</version>
                <configuration>
                    <createDependencyReducedPom>true</createDependencyReducedPom>
                    <filters>
                        <filter>
                            <artifact>*:*</artifact>
                            <excludes>
                                <exclude>META-INF/*.SF</exclude>
                                <exclude>META-INF/*.DSA</exclude>
                                <exclude>META-INF/*.RSA</exclude>
                            </excludes>
                        </filter>
                    </filters>
                </configuration>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer
                                        implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                                <transformer
                                        implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>${mainClass}</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

    
_Increasing and Improving Peformance in the Cloud_

More performance can be achieved by deployment of multiple instances in a private cloud in which multiple instances of the service are instantiating in addition to sharding (and replicating) the database.

In a typical cloud environment like AWS, I would use Concourse (for deployment) + rundeck + singularity (which runs on top of Mesos and which generalizes docker) + barragon + nginx.

I have started to experiment with this technology. (Hubspot seems to provide a lot of interesting software.)

(I would probably probably add prometheus for white box diagnosis of the system as well as hystrix to manage recovery from failures when possible.)


_Getting into the Container Bridge_

Some useful commands. 

    sudo docker run -itd --name container1 busybox

    sudo docker network inspect bridge

    sudo docker attach container1
    
    docker ps -a -q
    
    docker rmi image ...
    
    docker rm containerid
    
Now the circle_postgres service can be pinged from container1 busy box. Without DNS the IP address must be used.

The docker-configuration.yml file must use the IP address to access postgres unless I set up DNS on the Container Bridge.




    
    
    
    
