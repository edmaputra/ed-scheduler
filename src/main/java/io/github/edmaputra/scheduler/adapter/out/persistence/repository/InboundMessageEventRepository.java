package io.github.edmaputra.scheduler.adapter.out.persistence.repository;

import io.github.edmaputra.scheduler.domain.InboundMessageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InboundMessageEventRepository extends JpaRepository<InboundMessageEvent, String> {
}
