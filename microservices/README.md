# Achat - microservices

Spring Boot 2.5.3 / Spring Cloud 2020.0.3 / Java 8 version of the `achat` monolith (kept untouched in `../achat`).

```
client -> api-gateway :8080 --JWT check--> auth-service     :8081  auth_db
                                       \-> product-service  :8082  product_db   Produit, CategorieProduit, Stock
                                       \-> supplier-service :8083  supplier_db  Fournisseur, DetailFournisseur, SecteurActivite
                                       \-> invoice-service  :8084  invoice_db   Facture, DetailFacture, Operateur, Reglement
discovery-server :8761 (Eureka)     config-server :8888 (serves ./config-repo)
```

invoice-service calls product-service (prices) and supplier-service (existence check) with OpenFeign behind a
Resilience4j circuit breaker. It stores `produitId` / `fournisseurId` instead of JPA relations.

## Run everything

```bash
cp .env.example .env        # fill in the passwords, JWT_SECRET (>= 32 chars) and optionally ADMIN_PASSWORD
docker compose up -d --build
```

Only the gateway (`localhost:8080`) and the Eureka dashboard (`localhost:8761`) are published.

```bash
# create an account and call a protected endpoint
TOKEN=$(curl -s -X POST localhost:8080/auth/register -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"a-long-password"}' | python -c "import sys,json;print(json.load(sys.stdin)['token'])")
curl -H "Authorization: Bearer $TOKEN" localhost:8080/produit/retrieve-all-produits
```

Endpoints keep the monolith paths (`/produit/**`, `/stock/**`, `/facture/**`, ...) minus the old `/SpringMVC` prefix.

## Build and test

```bash
mvn verify          # all modules; on JDK 23+ a profile enables Lombok processing automatically
```

## Configuration

* `config-repo/application.yml` - shared (Eureka, actuator, logging).
* `config-repo/<service>.yml` - port, datasource, resilience settings. Secrets are `${ENV}` placeholders, resolved by
  each service from its own environment; nothing sensitive is committed.
* Run a service outside Docker: start `discovery-server`, then `config-server`, export `DB_USER`, `DB_PASSWORD`
  (and `JWT_SECRET` for `auth-service` / `api-gateway`), then start the service.
