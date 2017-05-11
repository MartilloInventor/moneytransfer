package com.interview;

import javax.ws.rs.*;

import javax.ws.rs.core.MediaType;
import java.util.List;

import static jdk.nashorn.internal.runtime.PropertyDescriptor.GET;

/**
 * http://www.dropwizard.io/1.0.6/docs/manual/core.html#resources
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceResource {

    private ServiceDAO dao;

    ServiceResource(ServiceDAO dao) {
        this.dao = dao;
    }

    @GET
    @Path("/ping")
    public String ping() {
        /* Must add the \"'s to be proper JSON object to be returned to browser */
        return "\"Pong\"";
    }

    @GET
    @Path("/accounts")
    public List<Account> getAllAccounts() {
        return dao.getAllAccounts();
    }

    @GET
    @Path("/accounts/{id}")
    public Account getAllAccounts(@PathParam("id") String id) {
        return dao.getAccount(id);
    }

    @GET
    @Path("/accounts/balance/{id}")
    public Integer getAccountBalance(@PathParam("id") String id) {
        return dao.getAccountBalance(id);
    }

    @GET
    @Path("/postgres")
    public String getPostgresVersion() {
        return dao.getPostgresVersion();
    }

    @POST
    @Path("/accounts/balance/{id}")
    public Integer setAccountBalance(@PathParam("id") String id, @HeaderParam( "amount" ) Integer amount) {
        return dao.setAccountBalance(id,amount);

    }
    @POST
    @Path("/accounts/transfer")
    public List<Account> makeTransfer(@HeaderParam("srcid") String srcid,
                                      @HeaderParam("destid") String dstid,
                                      @HeaderParam("amount") Integer amount) {

        return null ;//dao.makeTransfer( srcid, dstid, amount, means);
    }

}
