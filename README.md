# Googol (Meta 1) — Java/RMI Skeleton

## Requisitos
- JDK 17+
- Maven 3.9+
- VS Code + Extension Pack for Java (recomendado)

## Compilar
```bash
mvn -q -DskipTests package
```

## Arranque (terminais separados)
Barrel (porta 1099):
```bash
java -cp barrel/target/googol-barrel-0.1.0.jar:common/target/googol-common-0.1.0.jar pt.uc.sd.googol.barrel.BarrelMain
```

Gateway (porta 1100):
```bash
java -cp gateway/target/googol-gateway-0.1.0.jar:common/target/googol-common-0.1.0.jar   pt.uc.sd.googol.gateway.GatewayMain localhost 1100 rmi://localhost:1099/Barrel1
```

Client (teste):
```bash
java -cp client/target/googol-client-0.1.0.jar:common/target/googol-common-0.1.0.jar   pt.uc.sd.googol.client.ClientMain rmi://localhost:1100/Gateway
```

Downloader (placeholder):
```bash
java -cp downloader/target/googol-downloader-0.1.0.jar:common/target/googol-common-0.1.0.jar   pt.uc.sd.googol.downloader.DownloaderMain rmi://localhost:1100/Queue rmi://localhost:1099/Barrel1
```
