package controllers;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import models.Person;
import models.PersonRepository;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {

    @Inject
    PersonRepository repo;

    @GET
    public List<Person> list(){
        return repo.listAll();
    }

    @POST
    @Transactional
    public Person add(Person person){
        repo.persist(person);
        return person;
    }

}
