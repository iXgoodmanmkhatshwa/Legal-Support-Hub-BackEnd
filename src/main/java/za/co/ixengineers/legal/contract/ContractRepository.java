package za.co.ixengineers.legal.contract;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepository
        extends JpaRepository<ContractRecord, UUID> {

    List<ContractRecord> findAllByOrderByCreatedAtDesc();

    Optional<ContractRecord> findByReference(String reference);
}