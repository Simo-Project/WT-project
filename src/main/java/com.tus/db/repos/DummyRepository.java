package com.tus.db.repos;

import com.tus.db.models.Dummy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyRepository extends CrudRepository<Dummy, Long> {

}