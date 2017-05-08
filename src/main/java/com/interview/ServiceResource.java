package com.interview;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * http://www.dropwizard.io/1.0.6/docs/manual/core.html#resources
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceResource {

    private ServiceDAO dao;

    public ServiceResource(ServiceDAO dao) {
        this.dao = dao;
    }

    @GET
    @Path("ping")
    public String ping() {
        return "Pong";
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

}
