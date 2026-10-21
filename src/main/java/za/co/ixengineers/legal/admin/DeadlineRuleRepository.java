package za.co.ixengineers.legal.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeadlineRuleRepository
        extends JpaRepository<DeadlineRule, String> {

    List<DeadlineRule> findAllByOrderByNameAsc();

    List<DeadlineRule> findByDirtyTrue();

    boolean existsByDirtyTrue();
}
