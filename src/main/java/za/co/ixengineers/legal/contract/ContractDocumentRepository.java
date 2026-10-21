package za.co.ixengineers.legal.contract;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContractDocumentRepository
        extends JpaRepository<ContractDocument, UUID> {

    List<ContractDocument> findByContractIdOrderByPrimaryDocumentDescUploadedAtAsc(
            UUID contractId
    );
}