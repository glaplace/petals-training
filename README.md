petals-training
================
Repository helping learning the basics of developing a service on esb petals.

[![Build Status](https://github.com/glaplace/petals-training/actions/workflows/maven.yml/badge.svg)](https://github.com/glaplace/petals-training/actions/workflows/maven.yml) 
[![codecov](https://codecov.io/gh/glaplace/petals-training/branch/main/graph/badge.svg?token=9JU2OFJ2PC)](https://codecov.io/gh/glaplace/petals-training) [![versionjava](https://img.shields.io/badge/jdk-8-brightgreen.svg?logo=java)](https://adoptium.net/?variant=openjdk8&jvmVariant=hotspot)
[![License](https://img.shields.io/badge/Licence-GPL%20v3-blue)](https://www.gnu.org/licenses/gpl-3.0.html)

## architecture
L'architecture est basée sur le modèle du département ille et vilaine. 

Elle est composée de module maven, chacun réalisant une tâche. 

## outils et version
### petals
Ce projet se base sur la version courante de petals : 5.2.
### java
Il faut un jdk 8 (utilisation de openjdk)

### autres outils
* intellij
* soap-ui
* insomnia
* mailhog => src/main/docker/mailhog

## properties esb
Ci dessous la liste des « properties » de configuration utilisées dans le projets. <br/> 
Les properties permettent d'externaliser une partie de la configuration afin d'éviter, par exemple, des releases pour changer un paramètre ou une url.<br/>
Cela permet aussi d'avoir le même livrable sur tout les environnements (test, qualif, ppr, prd) seules les properties changes et elles ne sont pas dans le livrable.

```
ausy.training.openlibrary.url=https://openlibrary.org
ausy.training.relance.cron=0 * * * *
ausy.training.sql.jdbc.driver=org.h2.Driver
ausy.training.sql.jdbc.url=jdbc:h2:file:/path/to/petals-training/h2db
ausy.training.sql.jdbc.user=sa

ausy.training.mail.smtp.hostname=localhost
ausy.training.mail.smtp.port=1025
ausy.training.mail.default.to=moa@ausy.com
ausy.training.mail.default.from=no-reply.training@ausy.com
```
