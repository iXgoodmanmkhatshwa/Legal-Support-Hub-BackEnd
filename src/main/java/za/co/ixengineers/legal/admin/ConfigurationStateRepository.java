package za.co.ixengineers.legal.admin;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationStateRepository
        extends JpaRepository<ConfigurationState, String> {
}