package tn.esprit.rh.supplier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import tn.esprit.rh.supplier.entities.Fournisseur;
import tn.esprit.rh.supplier.entities.SecteurActivite;
import tn.esprit.rh.supplier.repositories.DetailFournisseurRepository;
import tn.esprit.rh.supplier.repositories.FournisseurRepository;
import tn.esprit.rh.supplier.repositories.SecteurActiviteRepository;
import tn.esprit.rh.supplier.services.FournisseurServiceImpl;

class FournisseurServiceImplTest {

	private final FournisseurRepository fournisseurs = mock(FournisseurRepository.class);
	private final SecteurActiviteRepository secteurs = mock(SecteurActiviteRepository.class);
	private final FournisseurServiceImpl service = new FournisseurServiceImpl();

	FournisseurServiceImplTest() {
		ReflectionTestUtils.setField(service, "fournisseurRepository", fournisseurs);
		ReflectionTestUtils.setField(service, "secteurActiviteRepository", secteurs);
		ReflectionTestUtils.setField(service, "detailFournisseurRepository", mock(DetailFournisseurRepository.class));
	}

	@Test
	void addFournisseurCreatesItsDetailWithACollaborationStartDate() {
		Fournisseur saved = service.addFournisseur(new Fournisseur());

		assertNotNull(saved.getDetailFournisseur());
		assertNotNull(saved.getDetailFournisseur().getDateDebutCollaboration());
	}

	@Test
	void assignSecteurActiviteLinksBothSides() {
		Fournisseur f = new Fournisseur();
		f.setSecteurActivites(new HashSet<>());
		SecteurActivite s = new SecteurActivite();
		when(fournisseurs.findById(1L)).thenReturn(Optional.of(f));
		when(secteurs.findById(2L)).thenReturn(Optional.of(s));

		service.assignSecteurActiviteToFournisseur(2L, 1L);

		assertTrue(f.getSecteurActivites().contains(s));
		verify(fournisseurs).save(f);
	}
}
