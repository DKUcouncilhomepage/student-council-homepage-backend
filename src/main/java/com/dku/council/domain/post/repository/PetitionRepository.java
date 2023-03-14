package com.dku.council.domain.post.repository;

import com.dku.council.domain.post.model.entity.posttype.Petition;

import java.util.List;

public interface PetitionRepository extends GenericPostRepository<Petition> {
    List<Petition> findTop5ByOrderByCreatedAtDesc();
}