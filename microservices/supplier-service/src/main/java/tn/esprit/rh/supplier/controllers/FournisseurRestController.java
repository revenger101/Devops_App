package tn.esprit.rh.supplier.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.rh.supplier.entities.Fournisseur;
import tn.esprit.rh.supplier.services.IFournisseurService;

import java.util.List;


@RestController
@Tag(name = "Gestion des fournisseurss")
@RequestMapping("/fournisseur")
public class FournisseurRestController {

	@Autowired
	IFournisseurService fournisseurService;

	// http://localhost:8080/fournisseur/retrieve-all-fournisseurs
	@GetMapping("/retrieve-all-fournisseurs")
	@ResponseBody
	public List<Fournisseur> getFournisseurs() {
		List<Fournisseur> fournisseurs = fournisseurService.retrieveAllFournisseurs();
		return fournisseurs;
	}

	// http://localhost:8080/fournisseur/retrieve-fournisseur/8
	@GetMapping("/retrieve-fournisseur/{fournisseur-id}")
	@ResponseBody
	public Fournisseur retrieveFournisseur(@PathVariable("fournisseur-id") Long fournisseurId) {
		return fournisseurService.retrieveFournisseur(fournisseurId);
	}

	// http://localhost:8080/fournisseur/add-fournisseur
	@PostMapping("/add-fournisseur")
	@ResponseBody
	public Fournisseur addFournisseur(@RequestBody Fournisseur f) {
		Fournisseur fournisseur = fournisseurService.addFournisseur(f);
		return fournisseur;
	}

	// http://localhost:8080/fournisseur/remove-fournisseur/{fournisseur-id}
	@DeleteMapping("/remove-fournisseur/{fournisseur-id}")
	@ResponseBody
	public void removeFournisseur(@PathVariable("fournisseur-id") Long fournisseurId) {
		fournisseurService.deleteFournisseur(fournisseurId);
	}

	// http://localhost:8080/fournisseur/modify-fournisseur
	@PutMapping("/modify-fournisseur")
	@ResponseBody
	public Fournisseur modifyFournisseur(@RequestBody Fournisseur fournisseur) {
		return fournisseurService.updateFournisseur(fournisseur);
	}

	// http://localhost:8080/fournisseur/assignSecteurActiviteToFournisseur/1/5
		@PutMapping(value = "/assignSecteurActiviteToFournisseur/{idSecteurActivite}/{idFournisseur}")
		public void assignProduitToStock(@PathVariable("idSecteurActivite") Long idSecteurActivite, @PathVariable("idFournisseur") Long idFournisseur) {
			fournisseurService.assignSecteurActiviteToFournisseur(idSecteurActivite, idFournisseur);
		}

}
