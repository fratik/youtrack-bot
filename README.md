# YouTrack Bot
Prosty bot na Discorda pokazujący detale o danym zgłoszeniu, odpowiadający na jego numer.

## Zewnętrzne linki  
[YouTrack](https://issues.fratikbot.pl)  
[TeamCity (CI)](https://ci.fratikbot.pl)

## Kompilacja
Użyj `gradlew.bat core:build` (Windows) / `./gradlew core:build` (Linux/macOS). Gotowy jar zostanie umieszczony w folderze `build/libs`. Plik ten również możesz pobrać z CI.

## Użycie
### Wymagania
- Java (w produkcji używane OpenJDK w wersji 8),
- YouTrack (w produkcji używany w wersji 2020.1),
- Hub z założonym service'em dla projektu (client id i client secret należy podać do konfiguracji).

### Uruchomienie
```shell script
java -jar <skompilowany plik .jar> <token>
```

### Konfiguracja
Po pierwszym uruchomieniu bot utworzy plik `config.json`, należy go ustawić.

---
_zawiera kod dumnie zajumany z [FratikB0Ta](https://fratikbot.pl)_