package tn.esprit.rh.invoice.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Subset of the supplier-service Fournisseur that invoicing needs. */
@Getter
@Setter
@NoArgsConstructor
public class FournisseurDto {
	private Long idFournisseur;
}
