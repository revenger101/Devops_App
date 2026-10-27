package tn.esprit.rh.invoice.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tn.esprit.rh.invoice.entities.Facture;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

	// open-in-view is off, so the lines must be loaded with the invoice to be serialized
	@Override
	@EntityGraph(attributePaths = "detailsFacture")
	List<Facture> findAll();

	@Override
	@EntityGraph(attributePaths = "detailsFacture")
	Optional<Facture> findById(Long id);

	@EntityGraph(attributePaths = "detailsFacture")
	List<Facture> findByFournisseurId(Long fournisseurId);

	@Query("SELECT sum(f.montantFacture) FROM Facture f where f.dateCreationFacture between :startDate"
			+ " and :endDate and f.archivee=false")
	Float getTotalFacturesEntreDeuxDates(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	@Modifying
	@Query("update Facture f set f.archivee=true where f.idFacture=?1")
	void updateFacture(Long id);
}
