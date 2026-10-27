package tn.esprit.rh.invoice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "supplier-service", fallbackFactory = FournisseurClientFallbackFactory.class)
public interface FournisseurClient {

	@GetMapping("/fournisseur/retrieve-fournisseur/{id}")
	FournisseurDto retrieveFournisseur(@PathVariable("id") Long id);
}
