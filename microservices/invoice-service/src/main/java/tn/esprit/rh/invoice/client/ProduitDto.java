package tn.esprit.rh.invoice.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Subset of the product-service Produit that invoicing needs. */
@Getter
@Setter
@NoArgsConstructor
public class ProduitDto {
	private Long idProduit;
	private float prix;
}
