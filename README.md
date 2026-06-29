# ArmaCos Life 📊

Une appli Android (pensée pour le **Galaxy S23 Ultra**) pour **noter en un geste tout ce qui se passe dans ta journée** : argent dépensé, verres bus, lieux, personnes vues, humeur, activités, pas… et faire des **rétrospectives** par jour / semaine / mois / année.

Le principe central : **tu crées tes propres stats en 2 taps** (un nom, un type). Rien n'est codé en dur — tu ajoutes autant de stats que tu veux, sur absolument tout.

> 100 % local et privé : toutes les données restent sur le téléphone (aucun compte, aucun cloud). Le « jour » se réinitialise à minuit, mais l'historique est conservé pour toujours.

---

## ✨ Fonctionnalités (v1)

- **Stats personnalisables** — 10 types de saisie : Compteur, Argent, Nombre, Durée, Note /5, Oui/Non, Choix dans une liste, Personnes, Lieu, Note libre.
- **Écran « Aujourd'hui »** — toutes tes stats du jour, compteurs « +1 » instantanés, objectifs quotidiens.
- **Widget d'écran d'accueil** — note tes compteurs en un seul tap sans ouvrir l'appli ; les autres stats ouvrent une saisie rapide.
- **Saisie ultra-rapide** — l'écran de saisie s'adapte au type (montants pré-réglés, étoiles, puces de choix, multi-sélection de personnes…).
- **Rétrospectives** — pour chaque stat : graphe + total / moyenne / record / série en cours, par Jour / Semaine / Mois / Année.
- **Journal d'un jour** — rouvre n'importe quelle journée passée et vois tout ce que tu y as noté.
- **Podomètre** — la stat « Pas » se remplit toute seule via le capteur du téléphone.
- **Personnes & lieux** — un carnet réutilisable pour « qui j'ai vu » et « où je suis allé ».
- **Sauvegarde** — export / import de toutes les données en JSON.
- **Material You** — couleurs dérivées de ton fond d'écran, thème clair/sombre automatique.

## 🧱 Pile technique

Kotlin · Jetpack Compose (Material 3) · **Glance** (widget) · Room (base locale) · WorkManager (podomètre + reset minuit) · kotlinx.serialization (sauvegarde). `minSdk 26`, `targetSdk 34`. Aucune dépendance réseau.

---

## 🚀 Compiler & installer (Android Studio)

1. **Ouvre le projet** dans Android Studio (Hedgehog ou plus récent) et laisse-le synchroniser. Le projet est réglé sur **Gradle 8.7** ; si Android Studio signale un wrapper Gradle manquant, accepte qu'il le régénère (ou lance `gradle wrapper` une fois).
2. Branche ton **Galaxy S23 Ultra** en USB avec le **débogage USB** activé (Réglages → Options pour développeurs).
3. Choisis ton téléphone dans la barre du haut, puis **Run ▶** (`Shift+F10`).
4. L'appli s'installe et se lance. Au premier lancement, ~10 stats de démo sont créées (modifiables/supprimables).

> Pas d'Android Studio sous la main ? Un workflow **GitHub Actions** (`.github/workflows/android.yml`) compile un **APK debug** à chaque push : onglet **Actions** → dernier run → **Artifacts** → `armacoslife-debug-apk`. Transfère le `.apk` sur le téléphone et installe-le (autorise « installer des applis inconnues »).

## 📲 Ajouter le widget à l'écran d'accueil

Appui long sur l'écran d'accueil → **Widgets** → cherche **ArmaCos Life** → glisse-le. Redimensionne-le à ta guise. Un tap sur une tuile « compteur » l'incrémente directement ; le **＋** ouvre l'appli.

## ➕ Ajouter une stat (le cœur de l'appli)

Écran **Aujourd'hui** (ou **Gérer**) → bouton **« Nouvelle stat »** → tape un **nom**, choisis un **emoji** et un **type** → **Créer**. Les options avancées (unité, objectif, accès rapides, agrégation, épinglage widget) sont facultatives et pré-remplies intelligemment.

## 💾 Sauvegarder ses données

**Gérer** → ⚙️ **Réglages** → **Exporter** (crée un fichier `.json`) / **Importer** (le remplace). Pratique pour changer de téléphone.

---

## 🔭 Prévu pour la V2

- Trajet **GPS** de la journée (carte des lieux).
- **Temps passé par application** (`UsageStatsManager`).
- Activité de messagerie via les **notifications reçues**.
- Synchronisation / multi-appareils.

> ⚠️ **Limite Android, à savoir :** il est **impossible** de lire le contenu des messages que tu **envoies** dans WhatsApp, Instagram, etc. — le système l'interdit. Le plus proche faisable (V2) sera de **compter les notifications reçues** par appli comme indicateur d'activité, jamais le texte envoyé.

## 🗂️ Structure du projet

```
app/src/main/java/com/armacos/life/
├─ data/            # Room : entités, DAOs, repository, stats par défaut, export JSON
├─ domain/          # DayKey (minuit), Aggregator (rétrospectives), formatage
├─ ui/              # Compose : today, entry, manage, history, people, thème, nav
├─ widget/          # Widget Glance + actions (+1 / ouvrir)
├─ sensor/          # Lecture du capteur de pas
└─ work/            # WorkManager : échantillonnage des pas + reset de minuit
```
