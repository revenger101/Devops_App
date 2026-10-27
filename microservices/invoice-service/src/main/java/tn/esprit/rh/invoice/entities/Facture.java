package tn.esprit.rh.invoice.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Facture implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idFacture;
	private float montantRemise;
	private float montantFacture;
	@Temporal(TemporalType.DATE)
	private Date dateCreationFacture;
	@Temporal(TemporalType.DATE)
	private Date dateDerniereModificationFacture;
	private Boolean archivee;

	@OneToMany(mappedBy = "facture")
	private Set<DetailFacture> detailsFacture;

	// Owned by supplier-service: stored as a plain id, validated through a Feign call.
	private Long fournisseurId;

	@ManyToOne
	@JsonIgnore
	private Operateur operateur;

	@OneToMany(mappedBy = "facture")
	@JsonIgnore
	private Set<Reglement> reglements;
}
