package tn.esprit.rh.invoice.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import tn.esprit.rh.invoice.common.ApiException;

@Component
public class FournisseurClientFallbackFactory implements FallbackFactory<FournisseurClient> {

	@Override
	public FournisseurClient create(Throwable cause) {
		return id -> {
			throw new ApiException(503, "supplier_service_unavailable",
					"Le service fournisseurs est indisponible. Reessayez dans quelques instants.");
		};
	}
}
