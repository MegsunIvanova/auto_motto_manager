package bg.softuni.autho_moto_manager.repository;

import bg.softuni.autho_moto_manager.model.entity.SaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<SaleEntity, Long> {

    Optional<SaleEntity> findByVehicle_Uuid(String uuid);
}
