# Access Points Pipeline

This is the actual GATE pipeline for EHRI access points. Build instructions:
```
mvn clean install
cd ..
./assemble-xgapp.py -c pipeline-configuration.json -u kim-user -p <password> -l xgapp-access-points/target/xgapp-access-points-pipeline.zip -t out/
```

