package tn.esprit.rh.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tn.esprit.rh.invoice.client.FournisseurClient;
import tn.esprit.rh.invoice.client.FournisseurDto;
import tn.esprit.rh.invoice.client.ProduitClient;
import tn.esprit.rh.invoice.client.ProduitDto;
import tn.esprit.rh.invoice.common.ApiException;
import tn.esprit.rh.invoice.entities.DetailFacture;
import tn.esprit.rh.invoice.entities.Facture;
import tn.esprit.rh.invoice.repositories.DetailFactureRepository;
import tn.esprit.rh.invoice.repositories.FactureRepository;
import tn.esprit.rh.invoice.repositories.OperateurRepository;
import tn.esprit.rh.invoice.services.FactureServiceImpl;
import tn.esprit.rh.invoice.services.ReglementServiceImpl;

class FactureServiceImplTest {

	private final FactureRepository factures = mock(FactureRepository.class);
	private final DetailFactureRepository details = mock(DetailFactureRepository.class);
	private final ReglementServiceImpl reglements = mock(ReglementServiceImpl.class);
	private final ProduitClient produitClient = mock(ProduitClient.class);
	private final FournisseurClient fournisseurClient = mock(FournisseurClient.class);
	private FactureServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new FactureServiceImpl(factures, mock(OperateurRepository.class), details, reglements,
				produitClient, fournisseurClient);
		when(factures.save(any(Facture.class))).thenAnswer(i -> i.getArgument(0));
	}

	private DetailFacture line(Long produitId, int qte, int remise) {
		DetailFacture d = new DetailFacture();
		d.setProduitId(produitId);
		d.setQteCommandee(qte);
		d.setPourcentageRemise(remise);
		return d;
	}

	private ProduitDto produit(Long id, float prix) {
		ProduitDto p = new ProduitDto();
		p.setIdProduit(id);
		p.setPrix(prix);
		return p;
	}

	@Test
	void addFactureComputesTotalsFromRemotePrices() {
		when(produitClient.retrieveProduit(1L)).thenReturn(produit(1L, 10f));
		when(produitClient.retrieveProduit(2L)).thenReturn(produit(2L, 5f));
		Facture f = new Facture();
		Set<DetailFacture> lines = new HashSet<>();
		lines.add(line(1L, 3, 10)); // 30 - 3 = 27
		lines.add(line(2L, 2, 0)); // 10
		f.setDetailsFacture(lines);

		Facture saved = service.addFacture(f);

		assertEquals(37f, saved.getMontantFacture(), 0.001);
		assertEquals(3f, saved.getMontantRemise(), 0.001);
		assertEquals(Boolean.FALSE, saved.getArchivee());
		verify(details, org.mockito.Mockito.times(2)).save(any(DetailFacture.class));
	}

	@Test
	void addFactureRejectsUnknownProductWithoutWritingAnything() {
		when(produitClient.retrieveProduit(99L)).thenReturn(null);
		Facture f = new Facture();
		f.setDetailsFacture(Collections.singleton(line(99L, 1, 0)));

		ApiException e = assertThrows(ApiException.class, () -> service.addFacture(f));

		assertEquals(404, e.getStatus());
		assertEquals("produit_not_found", e.getCode());
		verify(factures, never()).save(any());
	}

	@Test
	void addFactureRejectsUnknownSupplier() {
		when(fournisseurClient.retrieveFournisseur(7L)).thenReturn(null);
		Facture f = new Facture();
		f.setFournisseurId(7L);

		ApiException e = assertThrows(ApiException.class, () -> service.addFacture(f));

		assertEquals("fournisseur_not_found", e.getCode());
		verify(factures, never()).save(any());
	}

	@Test
	void addFactureAcceptsKnownSupplier() {
		FournisseurDto dto = new FournisseurDto();
		dto.setIdFournisseur(7L);
		when(fournisseurClient.retrieveFournisseur(7L)).thenReturn(dto);
		Facture f = new Facture();
		f.setFournisseurId(7L);

		Facture saved = service.addFacture(f);

		assertEquals(7L, saved.getFournisseurId());
	}

	@Test
	void recouvrementIsZeroWhenNothingWasInvoiced() {
		when(factures.getTotalFacturesEntreDeuxDates(any(Date.class), any(Date.class))).thenReturn(null);

		assertEquals(0f, service.pourcentageRecouvrement(new Date(), new Date()), 0.001);
	}

	@Test
	void recouvrementIsPaidOverInvoiced() {
		when(factures.getTotalFacturesEntreDeuxDates(any(Date.class), any(Date.class))).thenReturn(200f);
		when(reglements.getChiffreAffaireEntreDeuxDate(any(Date.class), any(Date.class))).thenReturn(50f);

		assertEquals(25f, service.pourcentageRecouvrement(new Date(), new Date()), 0.001);
	}
}
