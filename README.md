The basic data model consists of the following class:

Account, which has an account id and a balance.

The account id is a string while the balance is an Integer.

Dropwizard provides basic healthcheck and metrics, and I added the following endpoints which can provide limity sanity checking on a deployed system.

`/api/ping -- returns "pong" and shows service is alive`(actually this endpoint was provided).

`/api/version -- version of service. 1.0.0 is synchronous and has potentially lower performance.`

`/api/postgres -- checks the version of deployed postgres and is useful in case postgres seems to have anomalous behavior.`

Here are the main REST API endpoints.

**HTTP GET**

`/accounts -- Lists all the accounts and details of each`

`/accounts/{id} -- Lists the details of account {id} (error code if account nonexistent -- maybe should be revisited)`

`/accounts/balance/{id} -- Returns the balance of account {id} (0 if account nonexistent)`
    

**HTTP POST**

`/api/accounts/balance/{id}?amount=Integer -- Sets the balance of account {id} to (non-negative)amount with account creation if necessary`

`/api/accounts/addtobalance/{id}?amount=Integer -- Adds amount, which may be negative, to balance of account {id}, result >= 0, returns number of modified rows`

`/api/accounts/transfer?srcid=String&dstid=String&amount=Integer -- Transfers positive amount from account srcid to dstid if both srcid and dstid exist.`
    
 **HTTP POST v2** 
 
 In this case the "in progress" response is immediate and the database action takes place in a separate thread which runs to completion. 
 
 One could provide some web pages that track the movement of funds rather as FedEx, the USPS, etc. provide tracking for a package. It's a more realistic approach to the transfer of funds especially if the real accounts are in different countries and managed by different organizations.
 
 This approach should provide higher performance. The transaction along with success or failure can be logged to another database that should be accessible from a web browser.
 
 The actual account database should probably be sharded to increase possible parallelism, and it should be replicated to increase system persistence and accessibility.
 
 Even without the existence of a log database that indicates success or failure of the POST operation, success or failure indication can be found through the GET operations.
 
 Note that it might be worthwhile to couple the asynchronous interface with some sort of fault recovery system like Hystrix.
 
 An alternative to a transaction log database might be using Prometheus gauges to keep track of the balances in accounts.
 
 https://prometheus.io/docs/introduction/overview/
 
 **V2 Endpoints**
 
` /api/accounts/balance/v2/{id}?amount=Integer -- Sets the balance of account {id} to (non-negative)amount with account creation if necessary`
 
 `/api/accounts/addtobalance/v2/{id}?amount=Integer -- Adds amount, which may be negative, to balance of account {id}, result >= 0, returns number of modified rows`
 
 `/api/accounts/transfer/v2?srcid=String&dstid=String&amount=Integer -- transfers positive amount from account srcid to dstid if both srcid and dstid exist.`
     
 **CLIENT API**
 
 The client api is found in the moneytransferclient project. I modeled it loosely on the Oanda FXApi. The endpoint to Oanda portal is assumed to be a constant string and is not provided as an argument to the methods.
 
 I could probably redo the two separate projects as a single maven projec,t which had a maven parent directory moneytransfer and two maven modules/children named moneytransferservice and moneytransferclient. The Account class should really be in a jar used by both the service and the client. 
 
 For now I will not do the work to restructure the maven projects. 
 
 _classes_
  
     Account
     AccessClient
 
 _methods of AccessClient_
     
     static public void initializeClient()
     static public String sendInterviewPing()
     static public String getInterviewVersion() 
     static public String getInterviewPostgresVersion()
     
     static public List<Account> getInterviewAccount()
     static public Account getInterviewAccount(String id)
     static public Integer getInterviewAccountBalance(String id)
     
     static public void setInterviewAccountBalance(String acct, int amount)
     static public void addInterviewAccountBalance(String acct, int amount)
     static public void makeTransfer(String src, String dst, int amount) 
     
      static public void setInterviewAccountBalanceV2(String acct, int amount)
      static public void addInterviewAccountBalanceV2(String acct, int amount)
      static public void makeTransferV2(String src, String dst, int amount)     

**NOTES**

Note that initializeClient() may not be necessary. I had difficulty in finding a way to convert the JSON representation of a JSON list of JSON objects into a Java list of Java objects. Some the proposed solutions claimed the problem was initialization.

Apparently, many have had difficulty getting JSON deserialization to work correctly -- especially with JSON arrays.
https://github.com/FasterXML/jackson-core/issues/32
http://stackoverflow.com/questions/28418564/jackson-deserialization-with-anonymous-classes
http://stackoverflow.com/questions/6890796/jackson-ioexception-can-not-deserialize-class-com-mycompany-models-personaddre
http://stackoverflow.com/questions/836805/in-xstream-is-there-a-better-way-to-marshall-unmarshall-listobjects-in-json-a
https://crunchify.com/how-to-serialize-deserialize-list-of-objects-in-java-java-serialization-example/
http://stackoverflow.com/questions/6349421/how-to-use-jackson-to-deserialise-an-array-of-objects
http://tutorials.jenkov.com/java-json/jackson-objectmapper.html
http://stackoverflow.com/questions/3395729/convert-json-array-to-normal-java-array

Somewhere online there is probably a solution (or I could just write the code), but it became tedious to try non-working examples.

In the end I just brute-forced a solution in static public String getInterviewVersion(). It is quite ugly.

The example ping endpoint does not return a correct JSON string, which would be "pong" and not pong. I have run into this problem frequently.

In a real service for moving money/financial instruments between accounts, we would probably use classes like Currency and BigDecimal.

https://docs.oracle.com/javase/8/docs/api/java/util/Currency.html

https://docs.oracle.com/javase/8/docs/api/java/math/BigDecimal.html

A generic financial/quant package that covers the universe of financial instruments does not seem to be available.

_Starting up and Terminating the Postgres Server_

(A note to me)
    
    sudo docker run --name circle_postgres -p 5432:5432 -e POSTGRES_PASSWORD=circle -e POSTGRES_USER=circle -e POSTGRES_DB=circle postgres
    
Note that the following command is used to free up the container name from the docker daemon. 
    
    sudo docker rm /circle_postgres

(The freeing could be made automatic by adding -rm to the above command line.)

Leaving this image in the local docker registry is okay because I am not debugging it. I can just start it at need. Leaving dockerized images of the test application service is not such a good idea and quickly becomes confusing.

_Persisting the Postgress Database_

On startup the application cleans and migrates the database, there might be some value to turning that off, and it seems configurable in the configuration.yml file, but I did not try it.

I became curious about persistence of the postgres database within docker container. This is mostly a note to myself, but this URL immediately following explains how to make the docker container postgres database data persistent.
 
 https://www.andreagrandi.it/2015/02/21/how-to-create-a-docker-image-for-postgresql-and-persist-data/
    

_Creating the Docker Application Service_

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

In creating the client library that contains an example main entry point, I also added the same xml code to the client libraries pom.xml so that I could run the library standalong for testing.
    
_Increasing and Improving Peformance in the Cloud_

More performance can be achieved by deployment of multiple instances in a private cloud in which multiple instances of the service are instantiating in addition to sharding (and replicating) the database.

In a typical cloud environment like AWS, I would use Concourse (for deployment) + rundeck + singularity (which runs on top of Mesos and which generalizes docker) + barragon + nginx.

I have started to experiment with this technology. (Hubspot seems to provide a lot of interesting software.)

(I would probably probably add prometheus for white box diagnosis of the system as well as hystrix to manage recovery from failures when possible.)

More performance might be achievable by using Scala actors instead of Java threads, but I would have to experiment. Despite claimed compatibility between Java and Scala objects, I have run into obscure problems in mixing Scala and Java code.

_Getting into the Container Bridge_

We need to know the IP address of the docker postgres service on the docker bridge in order to configure the docker application service to communicate with the docker postgress service.

The command _sudo docker networker inspect bridge_ identifies the IP addresses of attached docker services. I used busybox to check out the internal logical LAN configuration.

Then I set up the docker-configuration.yml which is used by dockerized application service. It all seems to work properly with the money-transfer-client-1.0-SNAPSHOT.jar.

The next step would be setting up an AWS cloud and automatic deployment from the github repository  with load-sharing, reverse proxy portal, etc.

I have set up github repositories for the moneytransfer service and the moneytransferclient libary. 

I have pushed the docker-application-service to the docker hub. It is public in thorsprovoni/interview.

Some useful commands. 

    sudo docker run -itd --name container1 busybox

    sudo docker network inspect bridge

    sudo docker attach container1
    
    docker ps -a -q
    
    docker rmi image ...
    
    docker start image
    
    docker rm containerid
    
On the docker bridge the circle_postgres service can be pinged from container1 busy box. Without DNS the IP address must be used.

The docker-configuration.yml file must use the IP address to access postgres unless I set up DNS on the Container Bridge.

In a real money transfer service, I would create a login service and build authentication into the system.








    
    
    
    
