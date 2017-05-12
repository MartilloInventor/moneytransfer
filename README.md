The basic data model consists of the following class:

Account,which has an account id and an amount.

Currently, no health checks or metrics are present, but one could consider the following endpoints to provide limity sanity checking.

/api/ping -- returns "pong" and shows service is alive

/api/version -- version of service. 1.0.0 is synchronous and has potentially lower performance.

/api/postgres -- checks the version of deployed postgres and is useful in case postgres seems to have anomalous behavior.

Here are the main REST API endpoints.

HTTP GET

/accounts
    Lists all the accounts and details of each

/accounts/{id}
    Lists the details of account {id} (error code if account nonexistent -- maybe should be revisited)

/accounts/balance/{id}
    Returns the balance of account {id} (0 if account nonexistent)
    

HTTP POST

/api/accounts/balance/{id} amount=Integer 
    Sets the balance of account {id} to (non-negative)amount with account creation if necessary

/api/accounts/addtobalance/{id} amount=Integer
    Adds amount, which may be negative, to balance of account {id}, result >= 0, returns number of modified rows

/api/accounts/transfer srcid=String dstid=String amount=Integer
    transfers positive amount from account srcid to dstid if both srcid and dstid exist.
    
 HTTP POST v2 
 In this case the "in progress" response is immediate and the database action takes place in a separate thread which runs to completion. 
 
 This approach should provide higher performance. The transaction along with success or failure can be logged to another database that should be accessible from a web browser.
 
 The actual account database should probably be sharded to increase possible parallelism, and it should be replicated to increase system persistence and accessibility.
 
 Even without the existence of a log database that indicates success or failure of the POST operation, success or failure indication can be found through the GET operations.
 
 Note that it might be worthwhile to couple the asynchronous interface with some sort of fault recovery system like Hystrix.
 
 Note that the postgres database in the docker container is not persistent. This URL immediately following explains how to make the postgres database data persistent.
 
 https://www.andreagrandi.it/2015/02/21/how-to-create-a-docker-image-for-postgresql-and-persist-data/
 
 V2 Endpoints
 
 /api/accounts/balance/v2/{id} amount=Integer 
     Sets the balance of account {id} to (non-negative)amount with account creation if necessary
 
 /api/accounts/addtobalance/v2/{id} amount=Integer
     Adds amount, which may be negative, to balance of account {id}, result >= 0, returns number of modified rows
 
 /api/accounts/transfer/v2 srcid=String dstid=String amount=Integer
     transfers positive amount from account srcid to dstid if both srcid and dstid exist.
     
 CLIENT API
 
 class 
     Account
     AccessClient
 
 methods
  
     static public String sendInterviewPing()
     static public String getInterviewVersion() 
     static public String getInterviewPostgresVersion()
     
     static public List<Account> getInterviewAccount()
     static public Account getInterviewAccount(String id)
     static public Integer getInterviewAccountBalance(String id)
     
     static public void setInterviewAccountBalance(String acct, int amount)
     static public void addInterviewAccountBalance(String acct, int amount)
     static public void makeTransfer(String src, String dst, int amount) 




