package com.dku.council.domain.post.repository;

import com.dku.council.domain.post.model.entity.posttype.Conference;

import java.util.List;

public interface ConferenceRepository extends GenericPostRepository<Conference> {
    List<Conference> findTop5ByOrderByCreatedAtDesc();

}