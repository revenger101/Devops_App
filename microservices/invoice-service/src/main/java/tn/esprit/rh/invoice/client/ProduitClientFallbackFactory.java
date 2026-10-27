package tn.esprit.rh.invoice.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import tn.esprit.rh.invoice.common.ApiException;

/** Circuit-breaker fallback: fail fast with a clear 503 instead of hanging the invoice request. */
@Component
public class ProduitClientFallbackFactory implements FallbackFactory<ProduitClient> {

	@Override
	public ProduitClient create(Throwable cause) {
		return id -> {
			throw new ApiException(503, "product_service_unavailable",
					"Le service produits est indisponible. Reessayez dans quelques instants.");
		};
	}
}
