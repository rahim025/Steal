# Steal — Assistant vocal Android 100% offline

Contrôle ton téléphone Android uniquement en parlant : ouvrir des applications,
envoyer des messages (WhatsApp en premier), passer des appels, régler le
volume, naviguer dans l'interface — sans connexion internet, grâce à une
reconnaissance vocale embarquée (Vosk).

## Fonctionnalités actuelles (squelette)

- Reconnaissance vocale continue et 100% offline (moteur Vosk)
- Ouverture d'applications par la voix
- Envoi de message par la voix sur **WhatsApp, Messenger, Instagram, TikTok et Facebook**, avec un enchaînement fiable des étapes (chaque clic attend que l'écran suivant soit réellement chargé, via `waitForScreenChange`, au lieu d'enchaîner en rafale)
- **Appels réels** : résolution du nom prononcé en numéro de téléphone via le carnet de contacts (`ContactResolver`), puis déclenchement de l'appel
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
Le fichier `MessagingAppProfile.kt` explique comment les retrouver avec
l'outil `uiautomatorviewer`. **Seul le profil WhatsApp a été documenté avec
un peu plus de certitude ; les profils Messenger, Instagram, TikTok et
Facebook sont des points de départ plausibles à vérifier et corriger avant
usage réel.** Pour Facebook en particulier, la conversation peut rediriger
vers l'app Messenger séparée selon la version installée — dans ce cas,
utiliser directement la commande avec "messenger" plutôt que "facebook".

### Publication sur le Play Store
Google est strict sur les apps utilisant l'accessibilité à ces fins ; une
publication publique demanderait une justification (cas d'usage
accessibilité). Pour un usage personnel via `.apk` installé manuellement
(comme prévu ici), aucune restriction.

## Structure du projet

```
app/src/main/java/com/steal/voiceassistant/
├── ui/                          Écrans (MainActivity, SettingsActivity, EnrollPassphraseActivity)
├── service/
│   ├── VoiceRecognitionService       Écoute vocale continue (Vosk)
│   ├── UnlockAccessibilityService    Actions UI, enchaînement fiable, tentative de déverrouillage
│   └── LockScreenPassphraseListener  Vérification de la phrase de déverrouillage
├── voiceprint/
│   ├── FeatureExtractor         Extraction d'empreinte vocale (comparaison de similarité)
│   └── VoicePassphraseManager   Enrôlement et vérification de la phrase de déverrouillage
└── model/
    ├── CommandParser            Texte reconnu → commande structurée
    ├── CommandRouter            Commande → exécution concrète
    ├── MessagingAppProfile      Identifiants UI des 4 apps de messagerie supportées
    └── ContactResolver          Résolution nom → numéro de téléphone (pour les appels)
```

## Prochaines étapes suggérées

- Vérifier et corriger les resource-id de Messenger, Instagram et TikTok avec `uiautomatorviewer` (voir `MessagingAppProfile.kt`)
- Contrôle de la lampe torche (CameraManager) et du WiFi
- Mot-clé de réveil dédié (ex: "Steal, ...") pour éviter les déclenchements accidentels
- Gérer le cas où un contact a plusieurs numéros (choisir ou demander lequel)

## Licence

À définir (ex: MIT) avant publication sur GitHub.
