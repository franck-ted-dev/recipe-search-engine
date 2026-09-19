# Recipe Search Engine — Cahier des charges

> Document de référence du projet. À lire en premier par toute nouvelle conversation Claude qui reprend ce travail : il contient la vision, la méthode de travail à respecter, et le journal des décisions déjà prises. Ce document est mis à jour au fil des décisions — il fait foi sur l'état réel du projet, pas la mémoire d'une conversation précédente.

## Origine

Ce projet est un fork de `simple-search-engine`, un projet Hyperskill (JetBrains Academy) sur lequel Franck a appris Java en concevant lui-même l'architecture (interfaces, stratégies, index inversé), avec l'aide de Claude en mode guidage. Le stage 6/6 de ce projet original a été terminé (tests inclus, version `1.0.0`).

`recipe-search-engine` reprend le cœur de ce moteur — l'index inversé et le pattern Strategy (`ALL`/`ANY`/`NONE`) — pour en faire un outil réel, utilisable par n'importe qui, au-delà de l'exercice académique.

## Vision du produit

Un moteur de recherche de recettes de cuisine par ingrédients, exposé via une API REST et une interface web.

L'utilisateur renseigne trois listes d'ingrédients :
- **ALL** — ingrédients qu'il veut absolument retrouver dans la recette (ce qu'il a dans son frigo, par exemple).
- **ANY** — ingrédients dont la présence d'au moins un suffit.
- **NONE** — ingrédients à exclure totalement (allergies, aversions).

Les données proviennent d'une vraie API publique de recettes (à choisir), pas d'un jeu de données fictif.

## Méthode de travail (important — à respecter par toute session future)

Franck conçoit l'architecture lui-même. Claude ne doit **jamais** décider à sa place des choix de conception (packages, classes, interfaces vs classes abstraites, structures de données, etc.). Le rôle de Claude est de poser des questions guidées, façon socratique, pour amener Franck à raisonner et trouver lui-même les réponses.

Exception explicite : quand il s'agit de théorie nouvelle (une API Java qu'il ne connaît pas, un concept qu'il ne maîtrise pas encore) et qu'il est bloqué, Claude peut et doit l'expliquer directement — plutôt que de le laisser deviner indéfiniment.

Quand Franck demande explicitement à Claude d'écrire du code à sa place (pour aller vite sur une tâche mécanique déjà entièrement conçue ensemble, par exemple), c'est une exception ponctuelle à la règle ci-dessus, pas un changement de méthode par défaut.

Préférence de formulation : séparer clairement les sujets distincts plutôt que de les mélanger (par exemple, couvrir la théorie générale avant de passer à l'API spécifique d'un langage).

## Ce qui est acquis (hérité de `simple-search-engine`)

- `Storage` (interface) / `DynamicSizeStorage` — stockage dynamique de lignes de texte.
- `SearchEngine` (interface) : `List<String> search(String query)`.
- `InvertedIndexSearchEngine` — construit un index inversé mot → positions de lignes ; utilise un champ `SearchStrategy` (avec setter) pour déléguer la logique de correspondance.
- `SearchStrategy` (interface) : `Set<Integer> executeStrategy(List<Set<Integer>> setsOfPositions, int totalLines)`, avec trois implémentations `AllStrategy`, `AnyStrategy`, `NoneStrategy` — toutes testées (y compris un bug d'aliasing corrigé : copier les `Set` avant de les muter, pour ne jamais modifier l'index inversé lui-même).
- Architecture Maven, Java 21, JUnit 5.

## État actuel de `recipe-search-engine`

Projet tout juste dupliqué (`git clone` local depuis `simple-search-engine`) — aucune modification encore apportée. Prochaine étape : adapter le modèle de données pour représenter une recette (voir décision ouverte ci-dessous).

## Décisions ouvertes (non tranchées)

- **Modèle de données** : garder le modèle actuel (une recette = une ligne de texte aplatie) et réutiliser `Storage`/`InvertedIndexSearchEngine` tel quel, OU introduire une classe `Recipe` structurée (nom, ingrédients, instructions...) et faire évoluer l'architecture en conséquence. *(Question posée à Franck, réponse en attente.)*
- **Source de données** : quelle API publique de recettes utiliser (TheMealDB, Spoonacular, autre) — critères à définir (gratuité, richesse des données, limites de requêtes).
- **Couche REST** : quel framework/outil (HttpServer natif, Spring Boot, autre).
- **Interface web** : techno à définir, viendra après la couche REST.

## Journal des décisions prises

*(À compléter au fil de l'avancement — une ligne par décision significative, avec sa justification.)*

- Nom du projet : `recipe-search-engine` (plutôt que `ingredient-finder`, jugé trompeur puisqu'on cherche des recettes, pas des ingrédients).
