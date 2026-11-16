# myevcarv1 - application Android de test

Cette nouvelle application est installee independamment de la precedente (applicationId: com.myevcar.v1).

- Nom affiche sur le telephone: myevcarv1
- Site charge: https://broad-moon-14ca.phmelloul.workers.dev/
- Chargement WebView direct, sans copie V36 integree
- Cache desactive pour faciliter le diagnostic des modifications du site
- Internet necessaire

Compilation: Actions > Compiler myevcarv1 Android > artefact myevcarv1-android-test, fichier myevcarv1.apk.

Il s'agit d'une APK de test signee avec la cle de debug de GitHub Actions. Ne pas publier sur le Play Store en l'etat.
