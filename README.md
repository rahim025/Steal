# Steal — Assistant vocal Android 100% offline

Contrôle ton téléphone Android uniquement en parlant : ouvrir des applications,
envoyer des messages (WhatsApp en premier), passer des appels, régler le
volume, naviguer dans l'interface — sans connexion internet, grâce à une
reconnaissance vocale embarquée (Vosk).

## Fonctionnalités actuelles (squelette)

- Reconnaissance vocale continue et 100% offline (moteur Vosk)
- Ouverture d'applications par la voix
- Envoi de message WhatsApp par la voix (profil UI documenté, exemple de référence)
- Actions système : volume, retour, accueil, notifications
- Actions génériques sur l'écran affiché : clic sur un élément par son texte, lecture de l'écran à voix haute
- Tentative de déverrouillage vocal (voir limites ci-dessous)
- **Phrase de déverrouillage personnalisée** : le propriétaire enregistre sa
  propre phrase (3 répétitions) dans l'écran "Créer ma phrase de
  déverrouillage". Au moment de la tentative, l'app vérifie à la fois le
  TEXTE prononcé (Vosk) et une empreinte vocale (comparaison de similarité
  spectrale, voir `voiceprint/`), pour limiter le risque qu'une autre
  personne répétant la même phrase déverrouille le téléphone.

## Installation

1. Cloner le dépôt
2. Télécharger un modèle Vosk français (ex: `vosk-model-small-fr-0.22`) sur
   https://alphacephei.com/vosk/models et le décompresser dans
   `app/src/main/assets/vosk-model-small-fr/`
3. Ouvrir le projet dans Android Studio
4. Compiler et installer le `.apk` directement (hors Play Store)
5. Dans l'app : activer le service d'accessibilité, puis démarrer l'écoute

## Limites importantes à connaître

### Déverrouillage vocal
Android ne permet à aucune application tierce de contourner un code PIN,
un schéma ou une empreinte digitale — c'est une protection de sécurité du
système. Le déverrouillage vocal ne fonctionne donc que si l'utilisateur
**n'a aucune sécurité de verrouillage activée** (glissement simple). C'est
une limite du système Android, pas de cette application.

### Empreinte vocale "maison"
La vérification de la phrase de déverrouillage (`voiceprint/`) utilise une
comparaison de similarité spectrale simplifiée, pas un vrai modèle de
speaker embedding entraîné (type deep learning). C'est largement suffisant
pour un usage perso (distinguer une voix différente prononçant la même
phrase) mais **ne doit pas être considéré comme un niveau de sécurité
biométrique fiable** — quelqu'un qui imite bien la voix et connaît la
phrase pourrait, en théorie, passer le seuil de similarité. Le seuil
(`SIMILARITY_THRESHOLD` dans `VoicePassphraseManager`) peut être resserré
si des faux positifs apparaissent en pratique.

### Automatisation des apps tierces (WhatsApp, Instagram, etc.)
Ces apps n'offrent pas d'API officielle pour ce type d'automatisation
personnelle. Steal simule des clics/saisies dans leur interface via le
service d'accessibilité Android — la même technique que Tasker ou
MacroDroid. Conséquence : **chaque mise à jour de ces apps peut casser
l'automatisation** (changement des identifiants internes de l'interface).
Le fichier `WhatsAppProfile.kt` explique comment les retrouver avec l'outil
`uiautomatorviewer`.

### Publication sur le Play Store
Google est strict sur les apps utilisant l'accessibilité à ces fins ; une
publication publique demanderait une justification (cas d'usage
accessibilité). Pour un usage personnel via `.apk` installé manuellement
(comme prévu ici), aucune restriction.

## Structure du projet

```
app/src/main/java/com/steal/voiceassistant/
├── ui/                          Écrans (MainActivity, SettingsActivity)
├── service/
│   ├── VoiceRecognitionService  Écoute vocale continue (Vosk)
│   └── UnlockAccessibilityService  Actions UI + tentative de déverrouillage
└── model/
    ├── CommandParser            Texte reconnu → commande structurée
    ├── CommandRouter            Commande → exécution concrète
    └── WhatsAppProfile          Identifiants UI WhatsApp
```

## Prochaines étapes suggérées

- Ajouter les profils Messenger, Instagram, Facebook, TikTok (même méthode que WhatsApp)
- Gérer l'enchaînement fiable des étapes UI (attendre le chargement de chaque écran au lieu d'exécuter les clics en rafale)
- Résolution des contacts par nom (READ_CONTACTS) pour les appels
- Contrôle de la lampe torche (CameraManager) et du WiFi
- Mot-clé de réveil dédié (ex: "Steal, ...") pour éviter les déclenchements accidentels

## Licence

À définir (ex: MIT) avant publication sur GitHub.
