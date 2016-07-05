# Access Points Pipeline

This is the actual GATE pipeline for EHRI access points. Build instructions:
```
mvn clean install
./assemble-xgapp.py \
  --user kim-user \
  --password <kim-user password> \
  --target xgapp-access-points/target/ \
  --pipeline xgapp-access-points/target/xgapp-access-points-pipeline.zip \
  --configuration-file pipeline-configuration.json
```

