package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("SELECT c FROM Category c WHERE "
            + "(:name IS NULL OR c.name LIKE %:name%) AND "
            + "(:description IS NULL OR c.description LIKE %:description%)")
    List<Category> findCategories(Pageable pageable,
                                  @Param("name") String name,
                                  @Param("description") String description);
}