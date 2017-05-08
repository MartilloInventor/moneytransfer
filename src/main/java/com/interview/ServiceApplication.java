package com.interview;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

/**
 * http://www.dropwizard.io/1.0.6/docs/manual/core.html#application
 */
public class ServiceApplication extends Application<ServiceConfiguration> {
    public static void main(String[] args) throws Exception {
        new ServiceApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        super.initialize(bootstrap);
        bootstrap.addBundle(new FlywayBundle<ServiceConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(ServiceConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }

            @Override
            public FlywayFactory getFlywayFactory(ServiceConfiguration configuration) {
                return configuration.getFlywayFactory();
            }
        });
    }

    @Override
    public void run(ServiceConfiguration config, Environment env) throws Exception {

        // cleans database
        config.getFlywayFactory().build(config.getDataSourceFactory().build(env.metrics(), "db")).clean();

        // re-run flyway migrations
        config.getFlywayFactory().build(config.getDataSourceFactory().build(env.metrics(), "db")).migrate();


        // Datasource factory, jdbi connections
        final DBIFactory factory = new DBIFactory();
        final DBI dbi = factory.build(env, config.getDataSourceFactory(), "postgresql");

        // Register Result Set Mapper for POJO Account with UUID id.
        dbi.registerMapper(new AccountResultMapper());


        ServiceDAO serviceDAO = new ServiceDAO(dbi);
        ServiceResource serviceResource = new ServiceResource(serviceDAO);

        //register your API resource here
        env.jersey().register(serviceResource);
    }
}
