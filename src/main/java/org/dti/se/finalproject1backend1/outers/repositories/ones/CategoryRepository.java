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

    @Query("SELECT c FROM Category c WHERE" +
            "(:search IS NULL OR c.name LIKE %:search%) AND " +
            "(:filters IS NULL OR c.type = :filters)")
    List<Category> findCategories(Pageable pageable,
                                  @Param("filters") String filters,
                                  @Param("search") String search);
}