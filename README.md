# MY EV CAR — Android V37

Application Android officielle **MY EV CAR**, compilée à partir du code source avec Android Gradle Plugin 8.6.1, Gradle 8.10.2 et Android SDK 35.

Ce projet remplace les anciennes versions expérimentales qui se fermaient à l'ouverture.

## Téléchargement de l'APK
Dans GitHub, aller dans **Actions** → **Compiler MY EV CAR Android** → sélectionner l'exécution verte → télécharger **MY_EV_CAR_ANDROID_V37_APK** dans *Artifacts*. Extraire le ZIP puis installer `app-debug.apk` sur le téléphone.

Le dépôt GitHub est privé. L'APK est signé avec la clé de développement automatique GitHub Actions (une signature différente peut empêcher de mettre à jour une version précédente : dans ce cas, désinstaller l'ancienne application avant d'installer la nouvelle).

## Contenu de l'application
WebView affichant le site existant :
https://broad-moon-14ca.phmelloul.workers.dev/

**Connexion Internet obligatoire** : cette application charge le site déployé sur Cloudflare ; elle n'embarque pas les images dans l'APK. Pour voir les 3 articles et le bouton WhatsApp, la V36 doit être effectivement déployée sur cette adresse.

Ne pas soumettre un APK de debug directement sur Google Play.
