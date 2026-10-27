package tn.esprit.rh.invoice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", fallbackFactory = ProduitClientFallbackFactory.class)
public interface ProduitClient {

	@GetMapping("/produit/retrieve-produit/{id}")
	ProduitDto retrieveProduit(@PathVariable("id") Long id);
}
