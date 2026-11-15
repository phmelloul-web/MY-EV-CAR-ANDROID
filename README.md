# MY EV CAR Android V38 — site intégré

Application Android MY EV CAR. La V38 embarque exactement le site statique V36 (HTML, JavaScript et fichiers images locaux) à l'intérieur de l'APK. Le site local est servi par WebView via l'origine HTTPS interne appassets.androidplatform.net. Aucun serveur n'est nécessaire pour l'accueil, les fiches véhicules et les articles intégrés.

Les liens et images hébergés par des sites tiers nécessitent toujours Internet. Le bouton WhatsApp reste configurable en modifiant whatsapp-config.js dans l'archive source, puis en reconstruisant l'APK.

## Compilation
GitHub Actions > Compiler MY EV CAR Android > Run workflow. L'artefact MY_EV_CAR_ANDROID_V38_COMPLET contient l'APK de test signé automatiquement par Gradle. Si le téléphone a une ancienne application signée avec une autre clé, la désinstaller d'abord.

## Intégrité
L'archive du site V36 originale (11 894 821 octets) a été séparée en 23 morceaux binaires dans site_package. GitHub Actions les réassemble, vérifie SHA-256, puis les extrait dans app/src/main/assets/site avant de compiler. Cela garantit l'absence de perte ou de substitution de photos.
