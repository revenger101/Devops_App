package tn.esprit.rh.invoice.services;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.esprit.rh.invoice.client.FournisseurClient;
import tn.esprit.rh.invoice.client.FournisseurDto;
import tn.esprit.rh.invoice.client.ProduitClient;
import tn.esprit.rh.invoice.client.ProduitDto;
import tn.esprit.rh.invoice.common.ApiException;
import tn.esprit.rh.invoice.entities.DetailFacture;
import tn.esprit.rh.invoice.entities.Facture;
import tn.esprit.rh.invoice.entities.Operateur;
import tn.esprit.rh.invoice.repositories.DetailFactureRepository;
import tn.esprit.rh.invoice.repositories.FactureRepository;
import tn.esprit.rh.invoice.repositories.OperateurRepository;

@Service
@Slf4j
@Transactional
public class FactureServiceImpl implements IFactureService {

	private final FactureRepository factureRepository;
	private final OperateurRepository operateurRepository;
	private final DetailFactureRepository detailFactureRepository;
	private final ReglementServiceImpl reglementService;
	private final ProduitClient produitClient;
	private final FournisseurClient fournisseurClient;

	public FactureServiceImpl(FactureRepository factureRepository, OperateurRepository operateurRepository,
			DetailFactureRepository detailFactureRepository, ReglementServiceImpl reglementService,
			ProduitClient produitClient, FournisseurClient fournisseurClient) {
		this.factureRepository = factureRepository;
		this.operateurRepository = operateurRepository;
		this.detailFactureRepository = detailFactureRepository;
		this.reglementService = reglementService;
		this.produitClient = produitClient;
		this.fournisseurClient = fournisseurClient;
	}

	@Override
	public List<Facture> retrieveAllFactures() {
		List<Facture> factures = factureRepository.findAll();
		log.info("retrieved {} factures", factures.size());
		return factures;
	}

	/**
	 * Creates an invoice. The supplier and every product are owned by other services, so they are
	 * validated / priced through Feign calls before anything is written.
	 */
	@Override
	public Facture addFacture(Facture f) {
		if (f.getFournisseurId() != null) {
			FournisseurDto fournisseur = fournisseurClient.retrieveFournisseur(f.getFournisseurId());
			if (fournisseur == null) {
				throw new ApiException(404, "fournisseur_not_found",
						"Le fournisseur " + f.getFournisseurId() + " est introuvable.");
			}
		}

		Set<DetailFacture> details = f.getDetailsFacture() == null ? new HashSet<>() : f.getDetailsFacture();
		float montantFacture = 0;
		float montantRemise = 0;
		for (DetailFacture detail : details) {
			ProduitDto produit = detail.getProduitId() == null ? null : produitClient.retrieveProduit(detail.getProduitId());
			if (produit == null) {
				throw new ApiException(404, "produit_not_found",
						"Le produit " + detail.getProduitId() + " est introuvable.");
			}
			float prixTotalDetail = detail.getQteCommandee() * produit.getPrix();
			int pourcentage = detail.getPourcentageRemise() == null ? 0 : detail.getPourcentageRemise();
			float montantRemiseDetail = (prixTotalDetail * pourcentage) / 100;
			float prixTotalDetailRemise = prixTotalDetail - montantRemiseDetail;
			detail.setMontantRemise(montantRemiseDetail);
			detail.setPrixTotalDetail(prixTotalDetailRemise);
			montantFacture += prixTotalDetailRemise;
			montantRemise += montantRemiseDetail;
		}

		f.setMontantFacture(montantFacture);
		f.setMontantRemise(montantRemise);
		Date now = new Date();
		f.setDateCreationFacture(f.getDateCreationFacture() == null ? now : f.getDateCreationFacture());
		f.setDateDerniereModificationFacture(now);
		f.setArchivee(Boolean.FALSE);
		f.setDetailsFacture(null);
		Facture saved = factureRepository.save(f);

		for (DetailFacture detail : details) {
			detail.setFacture(saved);
			detailFactureRepository.save(detail);
		}
		saved.setDetailsFacture(details);
		return saved;
	}

	@Override
	public void cancelFacture(Long factureId) {
		Facture facture = factureRepository.findById(factureId)
				.orElseThrow(() -> new ApiException(404, "facture_not_found", "La facture " + factureId + " est introuvable."));
		facture.setArchivee(true);
		factureRepository.save(facture);
	}

	@Override
	public Facture retrieveFacture(Long factureId) {
		Facture facture = factureRepository.findById(factureId).orElse(null);
		log.info("facture : {}", factureId);
		return facture;
	}

	@Override
	public List<Facture> getFacturesByFournisseur(Long idFournisseur) {
		return factureRepository.findByFournisseurId(idFournisseur);
	}

	@Override
	public void assignOperateurToFacture(Long idOperateur, Long idFacture) {
		Facture facture = factureRepository.findById(idFacture)
				.orElseThrow(() -> new ApiException(404, "facture_not_found", "La facture " + idFacture + " est introuvable."));
		Operateur operateur = operateurRepository.findById(idOperateur)
				.orElseThrow(() -> new ApiException(404, "operateur_not_found", "L operateur " + idOperateur + " est introuvable."));
		facture.setOperateur(operateur);
		factureRepository.save(facture);
	}

	@Override
	public float pourcentageRecouvrement(Date startDate, Date endDate) {
		Float totalFactures = factureRepository.getTotalFacturesEntreDeuxDates(startDate, endDate);
		if (totalFactures == null || totalFactures == 0) {
			return 0;
		}
		float totalRecouvrement = reglementService.getChiffreAffaireEntreDeuxDate(startDate, endDate);
		return (totalRecouvrement / totalFactures) * 100;
	}
}
