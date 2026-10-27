package tn.esprit.rh.invoice.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.rh.invoice.entities.Operateur;
import tn.esprit.rh.invoice.services.IOperateurService;

import java.util.List;

@RestController
@Tag(name = "Gestion des opérateurs")
@RequestMapping("/operateur")
public class OperateurController {

	@Autowired
	IOperateurService operateurService;
	
	// http://localhost:8080/operateur/retrieve-all-operateurs
	@GetMapping("/retrieve-all-operateurs")
	@ResponseBody
	public List<Operateur> getOperateurs() {
		List<Operateur> list = operateurService.retrieveAllOperateurs();
		return list;
	}

	// http://localhost:8080/operateur/retrieve-operateur/8
	@GetMapping("/retrieve-operateur/{operateur-id}")
	@ResponseBody
	public Operateur retrieveOperateur(@PathVariable("operateur-id") Long operateurId) {
		return operateurService.retrieveOperateur(operateurId);
	}

	// http://localhost:8080/operateur/add-operateur
	@PostMapping("/add-operateur")
	@ResponseBody
	public Operateur addOperateur(@RequestBody Operateur op) {
		Operateur operateur = operateurService.addOperateur(op);
		return operateur;
	}

	// http://localhost:8080/operateur/remove-operateur/{operateur-id}
	@DeleteMapping("/remove-operateur/{operateur-id}")
	@ResponseBody
	public void removeOperateur(@PathVariable("operateur-id") Long operateurId) {
		operateurService.deleteOperateur(operateurId);
	}

	// http://localhost:8080/operateur/modify-operateur
	@PutMapping("/modify-operateur")
	@ResponseBody
	public Operateur modifyOperateur(@RequestBody Operateur operateur) {
		return operateurService.updateOperateur(operateur);
	}

	
}
